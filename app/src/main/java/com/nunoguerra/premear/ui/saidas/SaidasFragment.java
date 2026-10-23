package com.nunoguerra.preamar.ui.saidas;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nunoguerra.preamar.R;

/** Lista as saídas gravadas no Room; toque abre o detalhe. */
public class SaidasFragment extends Fragment {

    public SaidasFragment() {
        super(R.layout.fragment_saidas);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SaidaViewModel viewModel = new ViewModelProvider(requireActivity()).get(SaidaViewModel.class);

        RecyclerView recycler = view.findViewById(R.id.recycler_saidas);
        View vazio = view.findViewById(R.id.empty_state);

        SaidaAdapter adapter = new SaidaAdapter(saida -> {
            Bundle args = new Bundle();
            args.putLong("saidaId", saida.id);
            Navigation.findNavController(view).navigate(R.id.action_saidas_to_detalhe, args);
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        viewModel.saidas.observe(getViewLifecycleOwner(), lista -> {
            adapter.submitList(lista);
            boolean semItens = lista == null || lista.isEmpty();
            vazio.setVisibility(semItens ? View.VISIBLE : View.GONE);
            recycler.setVisibility(semItens ? View.GONE : View.VISIBLE);
        });

        view.findViewById(R.id.fab_new_trip).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_saidas_to_novaSaida));
    }
}
