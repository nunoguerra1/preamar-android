package com.nunoguerra.preamar.ui.novasaida;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.nunoguerra.preamar.R;
import com.nunoguerra.preamar.ui.saidas.SaidaViewModel;
import com.nunoguerra.preamar.ui.widget.MoonPhaseView;
import com.nunoguerra.preamar.util.Atividade;
import com.nunoguerra.preamar.util.Condicoes;
import com.nunoguerra.preamar.util.FormatoData;
import com.nunoguerra.preamar.util.MoonPhaseCalculator;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Formulário de nova saída. Ao escolher data/hora, o app já mostra as condições
 * de lua e maré calculadas; ao salvar, elas são gravadas junto com a saída (Room).
 */
public class NovaSaidaFragment extends Fragment {

    private final Calendar calendario = Calendar.getInstance();

    private SaidaViewModel viewModel;
    private TextInputLayout tilLocal;
    private TextInputLayout tilAtividade;
    private TextInputEditText etLocal;
    private TextInputEditText etData;
    private TextInputEditText etObs;
    private AutoCompleteTextView actvAtividade;
    private TextView tvPreview;
    private MoonPhaseView moonPreview;
    private View btnSalvar;

    public NovaSaidaFragment() {
        super(R.layout.fragment_nova_saida);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SaidaViewModel.class);

        tilLocal = view.findViewById(R.id.til_local);
        tilAtividade = view.findViewById(R.id.til_atividade);
        etLocal = view.findViewById(R.id.et_local);
        etData = view.findViewById(R.id.et_data);
        etObs = view.findViewById(R.id.et_obs);
        actvAtividade = view.findViewById(R.id.actv_atividade);
        tvPreview = view.findViewById(R.id.tv_preview_condicoes);
        moonPreview = view.findViewById(R.id.moon_preview);
        btnSalvar = view.findViewById(R.id.btn_salvar_saida);

        String[] rotulos = new String[Atividade.values().length];
        for (int i = 0; i < rotulos.length; i++) {
            rotulos[i] = Atividade.values()[i].getRotulo();
        }
        actvAtividade.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, rotulos));

        atualizarDataEPreview();
        etData.setOnClickListener(v -> escolherData());
        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void atualizarDataEPreview() {
        long ms = calendario.getTimeInMillis();
        etData.setText(FormatoData.dataHora(ms));
        Condicoes c = Condicoes.em(ms);
        tvPreview.setText(c.resumo());
        moonPreview.setPosicao(MoonPhaseCalculator.posicaoNoCiclo(ms));
    }

    private void escolherData() {
        // O seletor trabalha em UTC: converte o dia atual do calendário para meia-noite UTC.
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Data da saída")
                .setSelection(utc.getTimeInMillis())
                .build();
        picker.addOnPositiveButtonClickListener(selecao -> {
            Calendar escolhido = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            escolhido.setTimeInMillis(selecao);
            calendario.set(escolhido.get(Calendar.YEAR), escolhido.get(Calendar.MONTH),
                    escolhido.get(Calendar.DAY_OF_MONTH));
            escolherHora();
        });
        picker.show(getParentFragmentManager(), "data_saida");
    }

    private void escolherHora() {
        final MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(calendario.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendario.get(Calendar.MINUTE))
                .setTitleText("Horário da saída")
                .build();
        picker.addOnPositiveButtonClickListener(v -> {
            calendario.set(Calendar.HOUR_OF_DAY, picker.getHour());
            calendario.set(Calendar.MINUTE, picker.getMinute());
            calendario.set(Calendar.SECOND, 0);
            calendario.set(Calendar.MILLISECOND, 0);
            atualizarDataEPreview();
        });
        picker.show(getParentFragmentManager(), "hora_saida");
    }

    private void salvar() {
        String local = etLocal.getText() == null ? "" : etLocal.getText().toString().trim();
        String rotulo = actvAtividade.getText() == null ? "" : actvAtividade.getText().toString().trim();

        boolean valido = true;
        if (TextUtils.isEmpty(local)) {
            tilLocal.setError(getString(R.string.erro_local));
            valido = false;
        } else {
            tilLocal.setError(null);
        }
        if (TextUtils.isEmpty(rotulo)) {
            tilAtividade.setError(getString(R.string.erro_atividade));
            valido = false;
        } else {
            tilAtividade.setError(null);
        }
        if (!valido) return;

        btnSalvar.setEnabled(false);
        String obs = etObs.getText() == null ? "" : etObs.getText().toString().trim();
        viewModel.registrar(local, Atividade.fromRotulo(rotulo), calendario.getTimeInMillis(),
                obs, () -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), R.string.saida_salva, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                });
    }
}
