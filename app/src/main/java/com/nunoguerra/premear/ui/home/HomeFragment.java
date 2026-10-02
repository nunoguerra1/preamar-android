package com.nunoguerra.preamar.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.nunoguerra.preamar.R;

/**
 * Tela inicial: header com parallax (MotionLayout) + dashboard resumido.
 * O cálculo real de maré/lua (Preamar engine) entra na Entrega 3.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View fabNewTrip = view.findViewById(R.id.fab_new_trip);
        fabNewTrip.setOnClickListener(v ->
                Navigation.findNavController(v)
                        .navigate(R.id.action_home_to_novaSaida)
        );
    }
}
