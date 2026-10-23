package com.nunoguerra.preamar.util;

public class Condicoes {

    public final long epochMs;
    public final MoonPhaseCalculator.Fase fase;
    public final double iluminacao;      // 0..1
    public final double idadeLuaDias;
    public final TideCalculator.Tipo tipoMare;
    public final TideCalculator.Estado estadoMare;
    public final double alturaRelativa;  // -1..1

    private Condicoes(long epochMs) {
        this.epochMs = epochMs;
        this.fase = MoonPhaseCalculator.calcular(epochMs);
        this.iluminacao = MoonPhaseCalculator.iluminacao(epochMs);
        this.idadeLuaDias = MoonPhaseCalculator.idadeEmDias(epochMs);
        this.tipoMare = TideCalculator.tipo(epochMs);
        this.estadoMare = TideCalculator.estado(epochMs);
        this.alturaRelativa = TideCalculator.altura(epochMs);
    }

    public static Condicoes em(long epochMs) {
        return new Condicoes(epochMs);
    }

    public int iluminacaoPercentual() {
        return (int) Math.round(iluminacao * 100.0);
    }

    public String resumo() {
        return fase.getRotulo() + " · " + tipoMare.getRotulo() + " · " + estadoMare.getRotulo();
    }
}
