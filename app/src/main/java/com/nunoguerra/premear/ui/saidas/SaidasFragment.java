package com.nunoguerra.preamar.ui.saidas;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nunoguerra.preamar.R;

/**
 * Lista de saídas registradas pelo usuário. O RecyclerView já está com
 * LayoutManager configurado; o Adapter + Room entram na Entrega 3/4 (CRUD).
 */
public class SaidasFragment extends Fragment {

    public SaidasFragment() {
        super(R.layout.fragment_saidas);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_saidas);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // TODO (Entrega 3): recyclerView.setAdapter(new SaidaAdapter(...));

        View emptyState = view.findViewById(R.id.empty_state);
        // TODO (Entrega 3): alternar a visibilidade de emptyState vs recyclerView
        // conforme o resultado do SaidaDao.getAll() observado via ViewModel.
        emptyState.setVisibility(View.VISIBLE);

        View fabNewTrip = view.findViewById(R.id.fab_new_trip);
        fabNewTrip.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_saidas_to_novaSaida)
        );
    }
}
