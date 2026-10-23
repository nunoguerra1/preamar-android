package com.nunoguerra.preamar.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.chip.ChipGroup;
import com.nunoguerra.preamar.R;
import com.nunoguerra.preamar.data.Saida;
import com.nunoguerra.preamar.ui.saidas.SaidaViewModel;
import com.nunoguerra.preamar.ui.widget.MoonPhaseView;
import com.nunoguerra.preamar.ui.widget.TideChartView;
import com.nunoguerra.preamar.util.Atividade;
import com.nunoguerra.preamar.util.Condicoes;
import com.nunoguerra.preamar.util.FormatoData;
import com.nunoguerra.preamar.util.MoonPhaseCalculator;
import com.nunoguerra.preamar.util.Recomendador;
import com.nunoguerra.preamar.util.TideCalculator;

import java.util.List;
import java.util.Locale;

/**
 * Tela inicial: escolha da atividade, melhores janelas (motor de recomendação),
 * gráfico de maré, fase da lua e última saída registrada (Room).
 */
public class HomeFragment extends Fragment {

    private static final String PREFS = "preamar_prefs";
    private static final String KEY_ATIVIDADE = "atividade_selecionada";

    private SaidaViewModel viewModel;
    private Atividade atividade = Atividade.MERGULHO;
    private long ultimaSaidaId = -1;

    private TextView tvJanelaPrincipal;
    private TextView tvJanelaDetalhe;
    private TextView tvOutras;
    private TextView tvMareAgora;
    private TextView tvLuaFase;
    private TextView tvLuaDetalhe;
    private TextView tvUltimaTitulo;
    private TextView tvUltimaDetalhe;
    private TideChartView tideChart;
    private MoonPhaseView moonView;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SaidaViewModel.class);

        tvJanelaPrincipal = view.findViewById(R.id.tv_janela_principal);
        tvJanelaDetalhe = view.findViewById(R.id.tv_janela_detalhe);
        tvOutras = view.findViewById(R.id.tv_outras_janelas);
        tvMareAgora = view.findViewById(R.id.tv_mare_agora);
        tvLuaFase = view.findViewById(R.id.tv_lua_fase);
        tvLuaDetalhe = view.findViewById(R.id.tv_lua_detalhe);
        tvUltimaTitulo = view.findViewById(R.id.tv_ultima_titulo);
        tvUltimaDetalhe = view.findViewById(R.id.tv_ultima_detalhe);
        tideChart = view.findViewById(R.id.tide_chart);
        moonView = view.findViewById(R.id.moon_view);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        atividade = Atividade.fromName(prefs.getString(KEY_ATIVIDADE, Atividade.MERGULHO.name()));

        ChipGroup chips = view.findViewById(R.id.chip_group_atividade);
        chips.check(chipIdDe(atividade));
        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            atividade = atividadeDe(ids.get(0));
            prefs.edit().putString(KEY_ATIVIDADE, atividade.name()).apply();
            atualizarRecomendacoes();
        });

        view.findViewById(R.id.fab_new_trip).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_novaSaida));

        view.findViewById(R.id.card_ultima).setOnClickListener(v -> {
            if (ultimaSaidaId >= 0) {
                Bundle args = new Bundle();
                args.putLong("saidaId", ultimaSaidaId);
                Navigation.findNavController(v).navigate(R.id.action_home_to_detalhe, args);
            }
        });

        viewModel.ultimaSaida.observe(getViewLifecycleOwner(), this::mostrarUltimaSaida);

        animarEntrada(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        atualizarRecomendacoes();
        atualizarMareELua();
    }

    private void atualizarRecomendacoes() {
        long agora = System.currentTimeMillis();
        List<Recomendador.Janela> janelas = Recomendador.melhoresJanelas(atividade, agora, 48, 3);
        Recomendador.Janela melhor = Recomendador.melhor(janelas);
        if (melhor == null) {
            tvJanelaPrincipal.setText(R.string.home_sem_janelas);
            tvJanelaDetalhe.setText("");
            tvOutras.setText("");
            return;
        }
        tvJanelaPrincipal.setText(FormatoData.janela(melhor.inicioMs, melhor.fimMs));
        tvJanelaDetalhe.setText(getString(R.string.home_nota_format, melhor.nota) + " · " + melhor.motivo);

        StringBuilder outras = new StringBuilder();
        for (Recomendador.Janela j : janelas) {
            if (j == melhor) continue;
            if (outras.length() > 0) outras.append('\n');
            outras.append("• ").append(FormatoData.janela(j.inicioMs, j.fimMs))
                    .append(" (nota ").append(j.nota).append(")");
        }
        tvOutras.setText(outras.toString());
        tvOutras.setVisibility(outras.length() == 0 ? View.GONE : View.VISIBLE);
    }

    private void atualizarMareELua() {
        long agora = System.currentTimeMillis();
        Condicoes c = Condicoes.em(agora);

        tideChart.setJanela(agora - 2L * 3_600_000L, 24);
        tvMareAgora.setText(getString(R.string.home_mare_agora,
                c.estadoMare.getRotulo(), c.tipoMare.getRotulo()));

        moonView.setPosicao(MoonPhaseCalculator.posicaoNoCiclo(agora));
        tvLuaFase.setText(c.fase.getRotulo());
        tvLuaDetalhe.setText(getString(R.string.home_lua_detalhe, c.iluminacaoPercentual(),
                String.format(new Locale("pt", "BR"), "%.1f", c.idadeLuaDias)));
    }

    private void mostrarUltimaSaida(@Nullable Saida saida) {
        if (saida == null) {
            ultimaSaidaId = -1;
            tvUltimaTitulo.setText(R.string.home_card_last_trip_empty);
            tvUltimaDetalhe.setVisibility(View.GONE);
            return;
        }
        ultimaSaidaId = saida.id;
        tvUltimaTitulo.setText(saida.local + " · " + Atividade.fromName(saida.atividade).getRotulo());
        String resumo = FormatoData.dataHora(saida.dataHoraMillis) + "\n"
                + MoonPhaseCalculator.Fase.fromName(saida.faseLunar).getRotulo() + " · "
                + TideCalculator.Tipo.fromName(saida.tipoMare).getRotulo() + " · "
                + TideCalculator.Estado.fromName(saida.estadoMare).getRotulo();
        tvUltimaDetalhe.setText(resumo);
        tvUltimaDetalhe.setVisibility(View.VISIBLE);
    }

    /** Entrada escalonada dos cards (fade + subida suave). */
    private void animarEntrada(View raiz) {
        int[] ids = {R.id.card_janelas, R.id.card_mare, R.id.card_lua, R.id.card_ultima};
        float deslocamento = 28f * getResources().getDisplayMetrics().density;
        for (int i = 0; i < ids.length; i++) {
            View card = raiz.findViewById(ids[i]);
            card.setAlpha(0f);
            card.setTranslationY(deslocamento);
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(120L + 90L * i)
                    .setDuration(450L)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    private int chipIdDe(Atividade a) {
        switch (a) {
            case TIDE_POOLING:
                return R.id.chip_tide_pooling;
            case PESCA_SUSTENTAVEL:
                return R.id.chip_pesca;
            case OBSERVACAO:
                return R.id.chip_observacao;
            case MERGULHO:
            default:
                return R.id.chip_mergulho;
        }
    }

    private Atividade atividadeDe(int chipId) {
        if (chipId == R.id.chip_tide_pooling) return Atividade.TIDE_POOLING;
        if (chipId == R.id.chip_pesca) return Atividade.PESCA_SUSTENTAVEL;
        if (chipId == R.id.chip_observacao) return Atividade.OBSERVACAO;
        return Atividade.MERGULHO;
    }
}
