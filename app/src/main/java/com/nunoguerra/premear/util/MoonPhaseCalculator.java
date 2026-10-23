package com.nunoguerra.preamar.util;

import java.util.Date;

/**
 * Cálculo local da fase lunar a partir do ciclo sinódico médio (~29,53 dias),
 * usando uma lua nova de referência (06/01/2000 18:14 UTC). Sem rede e sem API.
 * Precisão: erro de algumas horas em relação à fase "real" (suficiente para o app).
 */
public class MoonPhaseCalculator {

    public static final double MES_SINODICO_DIAS = 29.530588853;
    private static final long LUA_NOVA_REF_MS = 947182440000L; // 2000-01-06 18:14 UTC
    private static final double MS_POR_DIA = 86400000.0;

    public enum Fase {
        NOVA("Lua nova"),
        CRESCENTE("Lua crescente"),
        QUARTO_CRESCENTE("Quarto crescente"),
        GIBOSA_CRESCENTE("Gibosa crescente"),
        CHEIA("Lua cheia"),
        GIBOSA_MINGUANTE("Gibosa minguante"),
        QUARTO_MINGUANTE("Quarto minguante"),
        MINGUANTE("Lua minguante");

        private final String rotulo;

        Fase(String rotulo) {
            this.rotulo = rotulo;
        }

        public String getRotulo() {
            return rotulo;
        }

        public static Fase fromName(String name) {
            for (Fase f : values()) {
                if (f.name().equals(name)) return f;
            }
            return NOVA;
        }
    }

    /** Idade da lua em dias desde a última lua nova (0 a ~29,53). */
    public static double idadeEmDias(long epochMs) {
        double dias = (epochMs - LUA_NOVA_REF_MS) / MS_POR_DIA;
        double idade = dias % MES_SINODICO_DIAS;
        if (idade < 0) idade += MES_SINODICO_DIAS;
        return idade;
    }

    /** Posição no ciclo: 0 = nova, 0,5 = cheia, volta a 1 na próxima nova. */
    public static double posicaoNoCiclo(long epochMs) {
        return idadeEmDias(epochMs) / MES_SINODICO_DIAS;
    }

    /** Fração iluminada do disco lunar (0 a 1). */
    public static double iluminacao(long epochMs) {
        return (1.0 - Math.cos(2.0 * Math.PI * posicaoNoCiclo(epochMs))) / 2.0;
    }

    public static Fase calcular(long epochMs) {
        int indice = (int) Math.floor(posicaoNoCiclo(epochMs) * 8.0 + 0.5) % 8;
        return Fase.values()[indice];
    }

    public static Fase calcular(Date data) {
        return calcular(data.getTime());
    }
}
