package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class EffectBehindView extends FrameLayout {
    private ImageView mBgImageView;
    private Bitmap mCustomBackground;
    private Runnable mOnVisibilityChangedRunnable;

    public EffectBehindView(Context context) {
        super(context);
    }

    public EffectBehindView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectBehindView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EffectBehindView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && this.mOnVisibilityChangedRunnable != null) {
            this.mOnVisibilityChangedRunnable.run();
        }
    }

    public void setOnVisibilityChangedRunnable(Runnable runnable) {
        this.mOnVisibilityChangedRunnable = runnable;
    }

    public void setLiveWallpaperBg(Bitmap bitmap) {
        if (this.mCustomBackground != bitmap) {
            this.mCustomBackground = bitmap;
            setLiveWallpaperBg();
        }
    }

    private void setLiveWallpaperBg() {
        if (this.mBgImageView == null && this.mCustomBackground != null) {
            this.mBgImageView = new ImageView(this.mContext);
            this.mBgImageView.setScaleType(ScaleType.CENTER_CROP);
            this.mBgImageView.setImageBitmap(this.mCustomBackground);
            addView(this.mBgImageView, -1, -1);
        } else if (this.mCustomBackground != null) {
            this.mBgImageView.setImageBitmap(this.mCustomBackground);
        } else {
            if (this.mBgImageView != null && indexOfChild(this.mBgImageView) > -1) {
                removeView(this.mBgImageView);
            }
            this.mBgImageView = null;
        }
    }
}
