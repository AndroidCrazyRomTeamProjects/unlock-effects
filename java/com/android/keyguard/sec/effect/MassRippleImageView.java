package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.widget.ImageView;

public class MassRippleImageView extends ImageView {
    private static final String TAG = "MassRippleImageView";
    float INTERVAL_STROKE;
    int IntrinsicHeight;
    int IntrinsicWidth;
    float WORKING_GAP;
    Path mPath;
    float originalStroke;
    RectF oval;
    long prevTime;
    ShapeDrawable rippleCircle;
    float stroke;

    public void setPivotX(float pivotX) {
        super.setPivotX(pivotX);
    }

    public void setPivotY(float pivotY) {
        super.setPivotY(pivotY);
    }

    protected void onAnimationStart() {
        super.onAnimationStart();
        this.stroke = this.originalStroke;
    }

    protected void onAnimationEnd() {
        super.onAnimationEnd();
        this.stroke = this.originalStroke;
    }

    public final Animation getAnimation() {
        this.stroke -= this.INTERVAL_STROKE;
        if (this.stroke < 1.0f) {
            this.stroke = 1.0f;
        }
        if (!isTooEarly()) {
            this.mPath = new Path();
            this.oval = new RectF(this.stroke, this.stroke, ((float) this.IntrinsicWidth) - this.stroke, ((float) this.IntrinsicHeight) - this.stroke);
            this.mPath.addOval(this.oval, Direction.CW);
            this.rippleCircle = null;
            this.rippleCircle = new ShapeDrawable(new PathShape(this.mPath, (float) this.IntrinsicWidth, (float) this.IntrinsicHeight));
            this.rippleCircle.getPaint().setColor(-1);
            this.rippleCircle.getPaint().setStyle(Style.STROKE);
            this.rippleCircle.getPaint().setStrokeWidth(this.stroke);
            this.rippleCircle.setIntrinsicHeight(this.IntrinsicHeight);
            this.rippleCircle.setIntrinsicWidth(this.IntrinsicWidth);
            setBackground(this.rippleCircle);
        }
        return super.getAnimation();
    }

    public float translatedFromDPToPixel(float dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        float ret = dp * (((float) getResources().getDisplayMetrics().densityDpi) / 160.0f);
        Log.i(TAG, "dp = " + dp + ", to Pixel = " + ret);
        return ret;
    }

    public MassRippleImageView(Context context) {
        super(context);
        this.stroke = 26.6f;
        this.originalStroke = 26.6f;
        this.INTERVAL_STROKE = 0.05f;
        this.IntrinsicHeight = 200;
        this.IntrinsicWidth = 200;
        setVisibility(4);
        Log.i(TAG, TAG);
    }

    public MassRippleImageView(Context context, float stroke, int width, int height, float duration) {
        this(context);
        Log.i(TAG, TAG);
        this.stroke = translatedFromDPToPixel(stroke);
        this.originalStroke = translatedFromDPToPixel(stroke);
        this.IntrinsicHeight = (int) translatedFromDPToPixel((float) height);
        this.IntrinsicWidth = (int) translatedFromDPToPixel((float) width);
        this.INTERVAL_STROKE = (this.originalStroke / duration) * 20.0f;
        this.WORKING_GAP = duration / 20.0f;
        getAnimation();
    }

    private boolean isTooEarly() {
        long now = System.currentTimeMillis();
        if (((float) (now - this.prevTime)) <= this.WORKING_GAP) {
            return true;
        }
        this.prevTime = now;
        return false;
    }
}
