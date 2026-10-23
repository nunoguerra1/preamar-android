package com.nunoguerra.preamar.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Motor de recomendação: cruza maré e lua com a atividade escolhida e
 * sugere as melhores janelas de horário. As regras são heurísticas
 * (boas práticas gerais de cada atividade), não ciência exata.
 */
public class Recomendador {

    public static class Janela {
        public final Atividade atividade;
        public final long inicioMs;
        public final long fimMs;
        public final int nota; // 0..100
        public final String motivo;

        Janela(Atividade atividade, long inicioMs, long fimMs, int nota, String motivo) {
            this.atividade = atividade;
            this.inicioMs = inicioMs;
            this.fimMs = fimMs;
            this.nota = nota;
            this.motivo = motivo;
        }
    }

    private static final long PASSO_MS = 30L * 60L * 1000L;

    private static double horaLocal(long epochMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(epochMs);
        return c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 60.0;
    }

    private static double luzNatural(double hora) {
        if (hora >= 6 && hora < 18) return 1.0;
        if ((hora >= 5 && hora < 6) || (hora >= 18 && hora < 19)) return 0.6;
        return 0.15;
    }

    private static double limitar(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    /** Nota de 0 a 100 para a atividade no instante informado. */
    public static double pontuar(Atividade atividade, long epochMs) {
        double h = TideCalculator.altura(epochMs);
        double amp = TideCalculator.amplitude(epochMs);
        double amp01 = limitar((amp - 0.4) / 0.6, 0, 1);
        double rel = limitar(h / amp, -1, 1);          // -1 = baixa-mar, +1 = preamar
        double estofa = rel * rel;                     // 1 = água parada (extremos)
        double movimento = 1.0 - estofa;               // 1 = maré a meio caminho
        double hora = horaLocal(epochMs);
        double luz = luzNatural(hora);

        double nota;
        switch (atividade) {
            case TIDE_POOLING: {
                double baixa = Math.max(0, -rel);
                nota = (0.65 * baixa + 0.35 * amp01) * luz;
                break;
            }
            case MERGULHO: {
                double preamar = Math.max(0, rel);
                nota = (0.50 * estofa + 0.20 * preamar + 0.30 * (1.0 - amp01)) * luz;
                break;
            }
            case PESCA_SUSTENTAVEL: {
                boolean amanheceEntardece = (hora >= 5 && hora < 8) || (hora >= 16 && hora < 19);
                double periodo = amanheceEntardece ? 1.0 : (luz > 0.5 ? 0.8 : 0.6);
                nota = (0.60 * movimento + 0.40 * amp01) * periodo;
                break;
            }
            case OBSERVACAO:
            default: {
                double sol = Math.exp(-Math.pow((hora - 12.5) / 3.5, 2));
                nota = (0.40 * estofa + 0.30 * sol + 0.30 * (1.0 - amp01)) * luz;
                break;
            }
        }
        return limitar(nota * 100.0, 0, 100);
    }

    private static String explicar(Atividade atividade, long epochMs) {
        Condicoes c = Condicoes.em(epochMs);
        String base = c.estadoMare.getRotulo() + " · " + c.tipoMare.getRotulo();
        switch (atividade) {
            case TIDE_POOLING:
                return base + " — poças de maré mais expostas";
            case MERGULHO:
                return base + " — água mais parada, menos correnteza";
            case PESCA_SUSTENTAVEL:
                return base + " — água em movimento, peixes mais ativos";
            case OBSERVACAO:
            default:
                return base + " — mar calmo e boa luz";
        }
    }

    /**
     * Melhores janelas nas próximas {@code horas}, em ordem cronológica.
     * Sempre devolve ao menos a melhor janela encontrada.
     */
    public static List<Janela> melhoresJanelas(Atividade atividade, long deMs, int horas, int maximo) {
        int n = (int) (horas * 60L * 60L * 1000L / PASSO_MS) + 1;
        long[] tempos = new long[n];
        double[] notas = new double[n];
        for (int i = 0; i < n; i++) {
            tempos[i] = deMs + i * PASSO_MS;
            notas[i] = pontuar(atividade, tempos[i]);
        }

        List<Integer> picos = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double ant = i > 0 ? notas[i - 1] : -1;
            double prox = i < n - 1 ? notas[i + 1] : -1;
            if (notas[i] > ant && notas[i] >= prox) picos.add(i);
        }
        final double[] notasFinal = notas;
        Collections.sort(picos, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Double.compare(notasFinal[b], notasFinal[a]);
            }
        });

        List<Integer> escolhidos = new ArrayList<>();
        for (int idx : picos) {
            if (escolhidos.size() >= maximo) break;
            boolean longe = true;
            for (int e : escolhidos) {
                if (Math.abs(e - idx) < 8) { // pelo menos 4 h entre janelas
                    longe = false;
                    break;
                }
            }
            if (!longe) continue;
            if (!escolhidos.isEmpty() && notas[idx] < 30) continue;
            escolhidos.add(idx);
        }

        List<Janela> janelas = new ArrayList<>();
        for (int idx : escolhidos) {
            double limite = notas[idx] * 0.85;
            int ini = idx;
            int fim = idx;
            while (ini > 0 && notas[ini - 1] >= limite) ini--;
            while (fim < n - 1 && notas[fim + 1] >= limite) fim++;
            janelas.add(new Janela(atividade, tempos[ini], tempos[fim] + PASSO_MS,
                    (int) Math.round(notas[idx]), explicar(atividade, tempos[idx])));
        }
        Collections.sort(janelas, new Comparator<Janela>() {
            @Override
            public int compare(Janela a, Janela b) {
                return Long.compare(a.inicioMs, b.inicioMs);
            }
        });
        return janelas;
    }

    public static Janela melhor(List<Janela> janelas) {
        Janela melhor = null;
        for (Janela j : janelas) {
            if (melhor == null || j.nota > melhor.nota) melhor = j;
        }
        return melhor;
    }
}
