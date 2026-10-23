package com.nunoguerra.preamar.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.nunoguerra.preamar.R;
import com.nunoguerra.preamar.util.TideCalculator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Curva da maré estimada para as próximas horas, com preamares/baixa-mares e marcador de "agora". */
public class TideChartView extends View {

    private static final int PASSO_MIN = 15;

    private final Paint paintLinha = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintPreenchimento = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintGrade = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintAgora = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintPonto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path caminho = new Path();
    private final Path preenchimento = new Path();
    private final SimpleDateFormat formato = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private long inicioMs;
    private int horas = 24;
    private double[] alturas = new double[0];
    private List<TideCalculator.Evento> eventos;

    public TideChartView(Context context) {
        this(context, null);
    }

    public TideChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TideChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int teal = context.getResources().getColor(R.color.teal_accent, null);
        paintLinha.setStyle(Paint.Style.STROKE);
        paintLinha.setStrokeWidth(dp(2.5f));
        paintLinha.setStrokeCap(Paint.Cap.ROUND);
        paintLinha.setStrokeJoin(Paint.Join.ROUND);
        paintLinha.setColor(teal);

        paintPreenchimento.setStyle(Paint.Style.FILL);

        paintGrade.setStyle(Paint.Style.STROKE);
        paintGrade.setStrokeWidth(dp(1f));
        paintGrade.setColor(context.getResources().getColor(R.color.divider, null));
        paintGrade.setPathEffect(new DashPathEffect(new float[]{dp(4), dp(4)}, 0));

        paintTexto.setColor(context.getResources().getColor(R.color.text_muted, null));
        paintTexto.setTextSize(sp(10.5f));
        paintTexto.setTextAlign(Paint.Align.CENTER);

        paintAgora.setStyle(Paint.Style.STROKE);
        paintAgora.setStrokeWidth(dp(1.5f));
        paintAgora.setColor(context.getResources().getColor(R.color.sand_coral, null));

        paintPonto.setStyle(Paint.Style.FILL);
        paintPonto.setColor(context.getResources().getColor(R.color.ocean_deep, null));
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, getResources().getDisplayMetrics());
    }

    /** Define a janela exibida: começa em {@code inicioMs} e cobre {@code horas} horas. */
    public void setJanela(long inicioMs, int horas) {
        this.inicioMs = inicioMs;
        this.horas = horas;
        double lon = TideCalculator.longitudePadrao(inicioMs);
        int n = horas * 60 / PASSO_MIN + 1;
        alturas = new double[n];
        for (int i = 0; i < n; i++) {
            alturas[i] = TideCalculator.altura(inicioMs + i * PASSO_MIN * 60_000L, lon);
        }
        eventos = TideCalculator.eventos(inicioMs, inicioMs + horas * 3_600_000L, lon);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int teal = getResources().getColor(R.color.teal_accent, null);
        int topo = (teal & 0x00FFFFFF) | (0x66 << 24);
        int base = teal & 0x00FFFFFF;
        paintPreenchimento.setShader(new LinearGradient(0, dp(24), 0, h - dp(28), topo, base, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (alturas.length < 2) return;

        float esquerda = dp(14);
        float direita = getWidth() - dp(14);
        float topo = dp(24);
        float base = getHeight() - dp(28);
        float largura = direita - esquerda;

        float yMedio = yDe(0, topo, base);
        canvas.drawLine(esquerda, yMedio, direita, yMedio, paintGrade);

        caminho.reset();
        preenchimento.reset();
        for (int i = 0; i < alturas.length; i++) {
            float x = esquerda + largura * i / (alturas.length - 1);
            float y = yDe(alturas[i], topo, base);
            if (i == 0) {
                caminho.moveTo(x, y);
                preenchimento.moveTo(x, base);
                preenchimento.lineTo(x, y);
            } else {
                caminho.lineTo(x, y);
                preenchimento.lineTo(x, y);
            }
        }
        preenchimento.lineTo(direita, base);
        preenchimento.close();
        canvas.drawPath(preenchimento, paintPreenchimento);
        canvas.drawPath(caminho, paintLinha);

        // rótulos de hora a cada 6 h
        for (int h = 0; h <= horas; h += 6) {
            float x = esquerda + largura * h / horas;
            canvas.drawText(formato.format(new Date(inicioMs + h * 3_600_000L)), x, getHeight() - dp(8), paintTexto);
        }

        // preamares e baixa-mares
        if (eventos != null) {
            for (TideCalculator.Evento e : eventos) {
                float x = esquerda + largura * (e.epochMs - inicioMs) / (horas * 3_600_000f);
                float y = yDe(e.altura, topo, base);
                canvas.drawCircle(x, y, dp(3.5f), paintPonto);
                float yTexto = e.alta ? y - dp(8) : y + dp(16);
                canvas.drawText(formato.format(new Date(e.epochMs)), x, yTexto, paintTexto);
            }
        }

        // agora
        long agora = System.currentTimeMillis();
        if (agora >= inicioMs && agora <= inicioMs + horas * 3_600_000L) {
            float x = esquerda + largura * (agora - inicioMs) / (horas * 3_600_000f);
            canvas.drawLine(x, topo - dp(6), x, base, paintAgora);
        }
    }

    private float yDe(double altura, float topo, float base) {
        double norm = (altura + 1.0) / 2.0; // 0..1
        return (float) (base - norm * (base - topo));
    }
}
