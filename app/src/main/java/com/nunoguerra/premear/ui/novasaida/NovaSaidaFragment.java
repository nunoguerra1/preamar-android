package com.nunoguerra.preamar.ui.novasaida;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.nunoguerra.preamar.R;

/**
 * Formulário de cadastro de uma nova saída costeira.
 * Os campos já estão montados (TextInputLayout); a gravação via Room
 * (SaidaDao.insert) e a validação entram na Entrega 3/4.
 */
public class NovaSaidaFragment extends Fragment {

    public NovaSaidaFragment() {
        super(R.layout.fragment_nova_saida);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View btnSalvar = view.findViewById(R.id.btn_salvar_saida);
        btnSalvar.setOnClickListener(v -> {
            // TODO (Entrega 3/4): validar campos, montar um Saida, chamar
            // SaidaDao.insert(saida) numa thread de background e then navegar de volta.
            Navigation.findNavController(v).popBackStack();
        });
    }
}
