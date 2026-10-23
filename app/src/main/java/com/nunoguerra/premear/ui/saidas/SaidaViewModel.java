package com.nunoguerra.preamar.ui.saidas;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nunoguerra.preamar.data.AppDatabase;
import com.nunoguerra.preamar.data.Saida;
import com.nunoguerra.preamar.data.SaidaDao;
import com.nunoguerra.preamar.util.Atividade;
import com.nunoguerra.preamar.util.Condicoes;

import java.util.List;

public class SaidaViewModel extends AndroidViewModel {

    private final SaidaDao dao;
    private final Handler principal = new Handler(Looper.getMainLooper());

    public final LiveData<List<Saida>> saidas;
    public final LiveData<Saida> ultimaSaida;

    public SaidaViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).saidaDao();
        saidas = dao.getAll();
        ultimaSaida = dao.observarUltima();
    }

    public LiveData<Saida> saida(long id) {
        return dao.observarPorId(id);
    }

    public void registrar(String local, Atividade atividade, long dataHoraMs,
                          String observacoes, Runnable aposSalvar) {
        final Saida s = new Saida();
        s.local = local;
        s.atividade = atividade.name();
        s.dataHoraMillis = dataHoraMs;
        s.observacoes = observacoes;

        Condicoes c = Condicoes.em(dataHoraMs);
        s.faseLunar = c.fase.name();
        s.tipoMare = c.tipoMare.name();
        s.estadoMare = c.estadoMare.name();
        s.iluminacaoPct = c.iluminacaoPercentual();
        s.idadeLuaDias = c.idadeLuaDias;

        AppDatabase.executor.execute(() -> {
            dao.insert(s);
            if (aposSalvar != null) principal.post(aposSalvar);
        });
    }

    public void excluir(long id, Runnable aposExcluir) {
        AppDatabase.executor.execute(() -> {
            dao.deleteById(id);
            if (aposExcluir != null) principal.post(aposExcluir);
        });
    }
}
