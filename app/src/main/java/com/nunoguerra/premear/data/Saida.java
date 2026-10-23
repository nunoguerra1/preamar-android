package com.nunoguerra.preamar.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "saidas")
public class Saida {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String local = "";

    @NonNull
    public String atividade = "MERGULHO";

    public long dataHoraMillis;

    public String observacoes;

    public String faseLunar;
    public String tipoMare;
    public String estadoMare;

    public int iluminacaoPct;
    public double idadeLuaDias;
}
