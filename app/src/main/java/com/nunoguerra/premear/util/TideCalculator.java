package com.nunoguerra.preamar.util;

import java.util.Date;

/**
 * Motor de previsão de maré — calcula o ciclo semidiurno aproximado
 * (período de ~12h25min) a partir de uma maré de referência, cruzado com
 * a fase lunar calculada por {@link MoonPhaseCalculator}.
 *
 * Implementação completa prevista pra Entrega 3.
 */
public class TideCalculator {

    public enum TipoMare { ALTA, BAIXA, SUBINDO, DESCENDO }

    /**
     * TODO (Entrega 3): calcular o tipo/janela de maré pra {@code data}
     * usando o ciclo semidiurno aproximado. Valor fixo por enquanto.
     */
    public static TipoMare calcular(Date data) {
        return TipoMare.SUBINDO;
    }
}
