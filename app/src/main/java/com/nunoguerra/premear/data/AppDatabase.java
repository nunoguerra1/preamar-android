package com.nunoguerra.preamar.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Saida.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static volatile AppDatabase INSTANCE;

    public abstract SaidaDao saidaDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "preamar.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
