package com.nunoguerra.preamar.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.nunoguerra.preamar.R;

/**
 * Desenha a fase da lua (disco escuro + parte iluminada) a partir da posição no ciclo
 * (0 = nova, 0,5 = cheia). Espelhada para o hemisfério sul (como vemos no Brasil).
 */
public class MoonPhaseView extends View {

    private final Paint paintEscuro = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintClaro = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBorda = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path recorte = new Path();
    private final RectF oval = new RectF();

    private double posicao = 0.0;
    private boolean hemisferioSul = true;

    public MoonPhaseView(Context context) {
        this(context, null);
    }

    public MoonPhaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoonPhaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paintEscuro.setStyle(Paint.Style.FILL);
        paintEscuro.setColor(context.getResources().getColor(R.color.ocean_deeper, null));
        paintClaro.setStyle(Paint.Style.FILL);
        paintClaro.setColor(context.getResources().getColor(R.color.cream, null));
        paintBorda.setStyle(Paint.Style.STROKE);
        paintBorda.setStrokeWidth(2f * context.getResources().getDisplayMetrics().density);
        paintBorda.setColor(context.getResources().getColor(R.color.teal_accent, null));
    }

    /** @param posicao 0..1 dentro do ciclo sinódico */
    public void setPosicao(double posicao) {
        this.posicao = posicao;
        invalidate();
    }

    public void setHemisferioSul(boolean hemisferioSul) {
        this.hemisferioSul = hemisferioSul;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float r = Math.min(w, h) / 2f - paintBorda.getStrokeWidth();
        float cx = w / 2f;
        float cy = h / 2f;

        canvas.drawCircle(cx, cy, r, paintEscuro);

        canvas.save();
        recorte.reset();
        recorte.addCircle(cx, cy, r, Path.Direction.CW);
        canvas.clipPath(recorte);

        boolean crescente = posicao < 0.5;
        boolean ladoDireito = crescente != hemisferioSul; // no sul a crescente ilumina a esquerda
        if (ladoDireito) {
            canvas.drawRect(cx, cy - r, cx + r, cy + r, paintClaro);
        } else {
            canvas.drawRect(cx - r, cy - r, cx, cy + r, paintClaro);
        }

        double cosv = Math.cos(2.0 * Math.PI * posicao);
        float rx = (float) (r * Math.abs(cosv));
        oval.set(cx - rx, cy - r, cx + rx, cy + r);
        canvas.drawOval(oval, cosv < 0 ? paintClaro : paintEscuro);
        canvas.restore();

        canvas.drawCircle(cx, cy, r, paintBorda);
    }
}
