package com.nunoguerra.preamar.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Estimativa astronômica de maré (modelo semidiurno simplificado).
 *
 * A maré segue o ângulo horário da Lua: preamar a cada ~12h25min (meio dia lunar),
 * com amplitude maior em lua nova/cheia (sizígia) e menor nos quartos (quadratura).
 * É uma APROXIMAÇÃO local e offline: não substitui a tábua de marés oficial.
 */
public class TideCalculator {

    public enum Estado {
        ALTA("Maré alta"),
        BAIXA("Maré baixa"),
        SUBINDO("Maré enchendo"),
        DESCENDO("Maré vazando");

        private final String rotulo;

        Estado(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getRotulo() {
            return rotulo;
        }

        public static Estado fromName(String name) {
            for (Estado e : values()) {
                if (e.name().equals(name)) return e;
            }
            return SUBINDO;
        }
    }

    public enum Tipo {
        SIZIGIA("Sizígia (maré viva)"),
        QUADRATURA("Quadratura (maré morta)"),
        INTERMEDIARIA("Maré intermediária");

        private final String rotulo;

        Tipo(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getRotulo() {
            return rotulo;
        }

        public static Tipo fromName(String name) {
            for (Tipo t : values()) {
                if (t.name().equals(name)) return t;
            }
            return INTERMEDIARIA;
        }
    }

    /** Preamar ou baixa-mar prevista. */
    public static class Evento {
        public final long epochMs;
        public final boolean alta;
        public final double altura; // -1 a 1 (relativa)

        public Evento(long epochMs, boolean alta, double altura) {
            this.epochMs = epochMs;
            this.alta = alta;
            this.altura = altura;
        }
    }

    /** Longitude aproximada deduzida do fuso do aparelho (15° por hora de fuso). */
    public static double longitudePadrao(long epochMs) {
        return TimeZone.getDefault().getOffset(epochMs) / 3600000.0 * 15.0;
    }

    /** Ângulo horário da Lua em horas (valor contínuo, só faz sentido módulo 24). */
    private static double anguloHorarioLuaH(long epochMs, double longitudeGraus) {
        double horasUtc = ((epochMs / 3600000.0) % 24.0 + 24.0) % 24.0;
        double tempoSolarLocal = ((horasUtc + longitudeGraus / 15.0) % 24.0 + 24.0) % 24.0;
        double idade = MoonPhaseCalculator.idadeEmDias(epochMs);
        return tempoSolarLocal - 12.0 - 24.0 * idade / MoonPhaseCalculator.MES_SINODICO_DIAS;
    }

    /** Fator de amplitude: 1,0 em lua nova/cheia e 0,4 nos quartos. */
    public static double amplitude(long epochMs) {
        double idade = MoonPhaseCalculator.idadeEmDias(epochMs);
        return 0.7 + 0.3 * Math.cos(4.0 * Math.PI * idade / MoonPhaseCalculator.MES_SINODICO_DIAS);
    }

    /** Altura relativa da maré (-1 baixa-mar de sizígia ... +1 preamar de sizígia). */
    public static double altura(long epochMs, double longitudeGraus) {
        double x = anguloHorarioLuaH(epochMs, longitudeGraus);
        return amplitude(epochMs) * Math.cos(2.0 * Math.PI * x / 12.0);
    }

    public static double altura(long epochMs) {
        return altura(epochMs, longitudePadrao(epochMs));
    }

    /** Posição dentro do ciclo de maré em horas [0,12): 0 = preamar, 6 = baixa-mar. */
    private static double posicaoCicloH(long epochMs, double longitudeGraus) {
        double x = anguloHorarioLuaH(epochMs, longitudeGraus) % 12.0;
        if (x < 0) x += 12.0;
        return x;
    }

    public static Tipo tipo(long epochMs) {
        double amp = amplitude(epochMs);
        if (amp >= 0.85) return Tipo.SIZIGIA;
        if (amp <= 0.55) return Tipo.QUADRATURA;
        return Tipo.INTERMEDIARIA;
    }

    public static Estado estado(long epochMs, double longitudeGraus) {
        double x = posicaoCicloH(epochMs, longitudeGraus);
        double distAlta = Math.min(x, 12.0 - x);
        double distBaixa = Math.abs(x - 6.0);
        if (distAlta <= 0.75) return Estado.ALTA;
        if (distBaixa <= 0.75) return Estado.BAIXA;
        return x > 6.0 ? Estado.SUBINDO : Estado.DESCENDO;
    }

    public static Estado estado(long epochMs) {
        return estado(epochMs, longitudePadrao(epochMs));
    }

    /** Preamares e baixa-mares previstas no intervalo [deMs, ateMs]. */
    public static List<Evento> eventos(long deMs, long ateMs, double longitudeGraus) {
        final long passo = 60_000L; // 1 minuto
        List<Evento> lista = new ArrayList<>();
        double anterior = altura(deMs - passo, longitudeGraus);
        double atual = altura(deMs, longitudeGraus);
        for (long t = deMs; t <= ateMs; t += passo) {
            double proximo = altura(t + passo, longitudeGraus);
            if (anterior < atual && atual >= proximo) {
                lista.add(new Evento(t, true, atual));
            } else if (anterior > atual && atual <= proximo) {
                lista.add(new Evento(t, false, atual));
            }
            anterior = atual;
            atual = proximo;
        }
        return lista;
    }

    public static List<Evento> eventos(long deMs, long ateMs) {
        return eventos(deMs, ateMs, longitudePadrao(deMs));
    }
}
