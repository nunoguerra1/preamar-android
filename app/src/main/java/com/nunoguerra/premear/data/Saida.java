package com.nunoguerra.preamar.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Representa uma saída costeira registrada pelo usuário (mergulho, tide
 * pooling, pesca sustentável, observação de vida marinha...).
 *
 * As condições de maré/lua são preenchidas automaticamente pelo
 * TideCalculator/MoonPhaseCalculator no momento do registro — o usuário
 * nunca as digita manualmente.
 */
@Entity(tableName = "saidas")
public class Saida {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String local = "";

    @NonNull
    public String atividade = "";

    /** Epoch millis da data/hora da saída. */
    public long dataHoraMillis;

    public String observacoes;

    /** Preenchidos automaticamente a partir do motor de previsão (Entrega 3). */
    public String faseLunar;
    public String tipoMare;
}
