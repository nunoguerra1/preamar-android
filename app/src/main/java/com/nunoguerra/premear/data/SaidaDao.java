package com.nunoguerra.preamar.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SaidaDao {

    @Insert
    long insert(Saida saida);

    @Update
    void update(Saida saida);

    @Delete
    void delete(Saida saida);

    @Query("DELETE FROM saidas WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM saidas ORDER BY dataHoraMillis DESC")
    LiveData<List<Saida>> getAll();

    @Query("SELECT * FROM saidas WHERE id = :id")
    LiveData<Saida> observarPorId(long id);

    @Query("SELECT * FROM saidas ORDER BY dataHoraMillis DESC LIMIT 1")
    LiveData<Saida> observarUltima();

    @Query("SELECT * FROM saidas WHERE id = :id")
    Saida getById(long id);
}
