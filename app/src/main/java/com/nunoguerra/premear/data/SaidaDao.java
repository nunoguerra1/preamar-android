package com.nunoguerra.preamar.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * CRUD completo entra na Entrega 3/4. Por ora a interface já define o
 * contrato de acesso a dados usado pelas telas (SaídasFragment, Estatísticas).
 */
@Dao
public interface SaidaDao {

    @Insert
    long insert(Saida saida);

    @Update
    void update(Saida saida);

    @Delete
    void delete(Saida saida);

    @Query("SELECT * FROM saidas ORDER BY dataHoraMillis DESC")
    LiveData<List<Saida>> getAll();

    @Query("SELECT * FROM saidas WHERE id = :id")
    Saida getById(long id);
}
