package com.nunoguerra.preamar.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FormatoData {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private static String formatar(String padrao, long ms) {
        return new SimpleDateFormat(padrao, PT_BR).format(new Date(ms));
    }

    public static String hora(long ms) {
        return formatar("HH:mm", ms);
    }

    public static String dataCompleta(long ms) {
        return formatar("dd/MM/yyyy", ms);
    }

    public static String dataHora(long ms) {
        return formatar("dd/MM/yyyy HH:mm", ms);
    }

    public static String diaSemanaData(long ms) {
        return formatar("EEE, dd/MM", ms);
    }

    private static int diasDesdeEpoca(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return (int) Math.round(c.getTimeInMillis() / 86400000.0);
    }

    /** "Hoje", "Amanhã", "Ontem" ou "qui, 29/10". */
    public static String diaRelativo(long ms) {
        int diff = diasDesdeEpoca(ms) - diasDesdeEpoca(System.currentTimeMillis());
        if (diff == 0) return "Hoje";
        if (diff == 1) return "Amanhã";
        if (diff == -1) return "Ontem";
        return diaSemanaData(ms);
    }

    /** "Hoje · 14:00–15:30". */
    public static String janela(long inicioMs, long fimMs) {
        return diaRelativo(inicioMs) + " · " + hora(inicioMs) + "–" + hora(fimMs);
    }
}
