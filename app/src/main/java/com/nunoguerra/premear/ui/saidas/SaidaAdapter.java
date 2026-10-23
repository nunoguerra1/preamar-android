package com.nunoguerra.preamar.ui.saidas;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.nunoguerra.preamar.R;
import com.nunoguerra.preamar.data.Saida;
import com.nunoguerra.preamar.util.Atividade;
import com.nunoguerra.preamar.util.FormatoData;
import com.nunoguerra.preamar.util.MoonPhaseCalculator;
import com.nunoguerra.preamar.util.TideCalculator;

/** Lista de saídas (RecyclerView + DiffUtil). */
public class SaidaAdapter extends ListAdapter<Saida, SaidaAdapter.VH> {

    public interface OnSaidaClick {
        void onClick(Saida saida);
    }

    private final OnSaidaClick listener;

    public SaidaAdapter(OnSaidaClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Saida> DIFF = new DiffUtil.ItemCallback<Saida>() {
        @Override
        public boolean areItemsTheSame(@NonNull Saida a, @NonNull Saida b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Saida a, @NonNull Saida b) {
            return a.local.equals(b.local)
                    && a.atividade.equals(b.atividade)
                    && a.dataHoraMillis == b.dataHoraMillis
                    && java.util.Objects.equals(a.observacoes, b.observacoes);
        }
    };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saida, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Saida s = getItem(position);
        Atividade a = Atividade.fromName(s.atividade);
        Context ctx = h.itemView.getContext();

        h.local.setText(s.local);
        h.quando.setText(FormatoData.diaSemanaData(s.dataHoraMillis) + " · "
                + FormatoData.hora(s.dataHoraMillis) + " · " + a.getRotulo());
        h.condicoes.setText(MoonPhaseCalculator.Fase.fromName(s.faseLunar).getRotulo() + " · "
                + TideCalculator.Tipo.fromName(s.tipoMare).getRotulo() + " · "
                + TideCalculator.Estado.fromName(s.estadoMare).getRotulo());
        h.badge.setBackgroundTintList(ColorStateList.valueOf(corDaAtividade(ctx, a)));
        h.itemView.setOnClickListener(v -> listener.onClick(s));
    }

    private static int corDaAtividade(Context ctx, Atividade a) {
        int res;
        switch (a) {
            case TIDE_POOLING:
                res = R.color.teal_accent;
                break;
            case PESCA_SUSTENTAVEL:
                res = R.color.sand_coral;
                break;
            case OBSERVACAO:
                res = R.color.moss;
                break;
            case MERGULHO:
            default:
                res = R.color.ocean_deep;
                break;
        }
        return ctx.getResources().getColor(res, null);
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView badge;
        final TextView local;
        final TextView quando;
        final TextView condicoes;

        VH(@NonNull View itemView) {
            super(itemView);
            badge = itemView.findViewById(R.id.img_badge);
            local = itemView.findViewById(R.id.tv_local);
            quando = itemView.findViewById(R.id.tv_quando);
            condicoes = itemView.findViewById(R.id.tv_condicoes);
        }
    }
}
