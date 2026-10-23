package com.nunoguerra.preamar.ui.saidas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nunoguerra.preamar.R;
import com.nunoguerra.preamar.data.Saida;
import com.nunoguerra.preamar.ui.widget.MoonPhaseView;
import com.nunoguerra.preamar.util.Atividade;
import com.nunoguerra.preamar.util.FormatoData;
import com.nunoguerra.preamar.util.MoonPhaseCalculator;
import com.nunoguerra.preamar.util.TideCalculator;

import java.util.Locale;

/** Detalhe de uma saída: dados informados + condições calculadas na hora do registro. */
public class SaidaDetalheFragment extends Fragment {

    private long saidaId;
    private SaidaViewModel viewModel;

    public SaidaDetalheFragment() {
        super(R.layout.fragment_saida_detalhe);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        saidaId = requireArguments().getLong("saidaId", -1L);
        viewModel = new ViewModelProvider(requireActivity()).get(SaidaViewModel.class);

        TextView tvAtividade = view.findViewById(R.id.tv_detalhe_atividade);
        TextView tvLocal = view.findViewById(R.id.tv_detalhe_local);
        TextView tvQuando = view.findViewById(R.id.tv_detalhe_quando);
        TextView tvCondicoes = view.findViewById(R.id.tv_detalhe_condicoes);
        TextView tvObs = view.findViewById(R.id.tv_detalhe_obs);
        MoonPhaseView moon = view.findViewById(R.id.moon_detalhe);

        viewModel.saida(saidaId).observe(getViewLifecycleOwner(), (Saida s) -> {
            if (s == null) return; // já excluída
            tvAtividade.setText(Atividade.fromName(s.atividade).getRotulo().toUpperCase(Locale.getDefault()));
            tvLocal.setText(s.local);
            tvQuando.setText(FormatoData.dataHora(s.dataHoraMillis));
            moon.setPosicao(s.idadeLuaDias / MoonPhaseCalculator.MES_SINODICO_DIAS);

            String condicoes =
                    getString(R.string.detalhe_fase) + ": "
                            + MoonPhaseCalculator.Fase.fromName(s.faseLunar).getRotulo() + "\n"
                            + getString(R.string.detalhe_ilum) + ": " + s.iluminacaoPct + "%\n"
                            + getString(R.string.detalhe_idade) + ": "
                            + String.format(new Locale("pt", "BR"), "%.1f dias", s.idadeLuaDias) + "\n"
                            + getString(R.string.detalhe_mare_tipo) + ": "
                            + TideCalculator.Tipo.fromName(s.tipoMare).getRotulo() + "\n"
                            + getString(R.string.detalhe_mare_estado) + ": "
                            + TideCalculator.Estado.fromName(s.estadoMare).getRotulo();
            tvCondicoes.setText(condicoes);

            boolean temObs = s.observacoes != null && !s.observacoes.trim().isEmpty();
            tvObs.setText(temObs ? s.observacoes : getString(R.string.detalhe_sem_obs));
        });

        view.findViewById(R.id.btn_excluir).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.detalhe_excluir_titulo)
                        .setMessage(R.string.detalhe_excluir_msg)
                        .setNegativeButton(R.string.detalhe_cancelar, null)
                        .setPositiveButton(R.string.detalhe_confirmar_excluir, (d, w) ->
                                viewModel.excluir(saidaId, () -> {
                                    if (isAdded()) {
                                        Navigation.findNavController(requireView()).popBackStack();
                                    }
                                }))
                        .show());
    }
}
