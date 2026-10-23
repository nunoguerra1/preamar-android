package com.nunoguerra.preamar.util;

public enum Atividade {
    MERGULHO("Mergulho"),
    TIDE_POOLING("Tide pooling"),
    PESCA_SUSTENTAVEL("Pesca sustentável"),
    OBSERVACAO("Observação de vida marinha");

    private final String rotulo;

    Atividade(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }

    public static Atividade fromName(String name) {
        for (Atividade a : values()) {
            if (a.name().equals(name)) return a;
        }
        return MERGULHO;
    }

    public static Atividade fromRotulo(String rotulo) {
        for (Atividade a : values()) {
            if (a.rotulo.equals(rotulo)) return a;
        }
        return MERGULHO;
    }
}
