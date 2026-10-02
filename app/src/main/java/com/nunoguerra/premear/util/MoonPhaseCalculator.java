package com.nunoguerra.preamar.util;

import java.util.Date;

/**
 * Motor de cálculo de fase lunar — núcleo algorítmico do Preamar.
 *
 * Implementação prevista pra Entrega 3: ciclo sinódico de ~29,53 dias a
 * partir de uma lua nova de referência conhecida (ex: 06/01/2000 18:14 UTC),
 * sem depender de API externa.
 */
public class MoonPhaseCalculator {

    public enum Fase {
        NOVA, CRESCENTE, QUARTO_CRESCENTE, GIBOSA_CRESCENTE,
        CHEIA, GIBOSA_MINGUANTE, QUARTO_MINGUANTE, MINGUANTE
    }

    /**
     * TODO (Entrega 3): calcular a fase lunar real pra {@code data} usando
     * o ciclo sinódico. Por enquanto retorna um valor fixo pra não quebrar
     * a UI que já consome esse método.
     */
    public static Fase calcular(Date data) {
        return Fase.CHEIA;
    }
}
