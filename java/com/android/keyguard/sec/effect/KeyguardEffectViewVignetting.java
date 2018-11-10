package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFilter;
import android.graphics.ImageFilter.BitmapColorMaskFilter;
import android.graphics.ImageFilterSet;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.keyguard.sec.KeyguardProperties;

public class KeyguardEffectViewVignetting extends FrameLayout {
    private static int BOTTOM_DIM_ALPHA = 51;
    private static final int BOTTOM_TO_TOP = 1;
    private static final int DEFAULT_COLOR = 110;
    private static final String TAG = "KeyguardEffectViewVignetting";
    private static int TOP_DIM_ALPHA = 77;
    private static final int TOP_TO_BOTTOM = 0;
    private static float VIGNETTING_BOTTOM_RATIO = 0.117f;
    private static float VIGNETTING_TOP_RATIO = 0.1f;
    private final int LIMITTED_VALUE = 200;
    private int mDefaultColor = Color.argb(255, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR);
    private ImageView mFilterImageView;
    private int mWindowHeight = 0;
    private int mWindowWidth = 0;

    public KeyguardEffectViewVignetting(Context context) {
        super(context);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mWindowWidth = displayMetrics.widthPixels;
        this.mWindowHeight = displayMetrics.heightPixels;
        init();
        this.mFilterImageView = new ImageView(context);
        this.mFilterImageView.setLayerType(2, null);
        this.mFilterImageView.setBackgroundColor(ViewCompat.MEASURED_STATE_TOO_SMALL);
        addView(this.mFilterImageView);
    }

    private void init() {
        if (KeyguardProperties.isLatestTabletUX()) {
            TOP_DIM_ALPHA = convertToAlphaValue(30);
            BOTTOM_DIM_ALPHA = convertToAlphaValue(20);
            VIGNETTING_TOP_RATIO = 0.11f;
            VIGNETTING_BOTTOM_RATIO = 0.14f;
        }
    }

    private int convertToAlphaValue(int percent) {
        return (percent * 255) / 100;
    }

    public void resetBlendedFilter() {
        Log.d(TAG, "resetBlendedFilter()");
        if (this.mFilterImageView != null) {
            ImageFilterSet filterSet = new ImageFilterSet();
            BitmapColorMaskFilter topFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
            BitmapColorMaskFilter bottomFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
            topFilter.resetGradient();
            bottomFilter.resetGradient();
            filterSet.addFilter(topFilter);
            filterSet.addFilter(bottomFilter);
            this.mFilterImageView.setImageFilter(filterSet);
        }
    }

    public void applyBlendedFilter(Bitmap bgBitmap) {
        Log.d(TAG, "applyBlendedFilter()");
        if (this.mFilterImageView != null && bgBitmap != null) {
            Rect cropRect = getBitmapCenterCropRect(bgBitmap, this.mWindowWidth, this.mWindowHeight);
            ImageFilterSet filterSet = new ImageFilterSet();
            BitmapColorMaskFilter topFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
            BitmapColorMaskFilter bottomFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
            Rect gradientAreaRect = calcGradientArea(cropRect, 0);
            if (gradientAreaRect != null) {
                int extractColor = getDominentColor(bgBitmap, gradientAreaRect);
                Log.d(TAG, "extractedColor of top = " + ((extractColor >> 16) & 255));
                topFilter.resetGradient();
                if (((extractColor >> 16) & 255) > 200) {
                    topFilter.setGradient(0.0f, 1.0f, Color.argb(TOP_DIM_ALPHA, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), 0.0f, 1.0f - VIGNETTING_TOP_RATIO, Color.argb(0, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR));
                } else {
                    topFilter.setGradient(0.0f, 1.0f, Color.argb(TOP_DIM_ALPHA, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), 0.0f, 1.0f - VIGNETTING_TOP_RATIO, Color.argb(0, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)));
                }
                gradientAreaRect = calcGradientArea(cropRect, 1);
                if (gradientAreaRect != null) {
                    extractColor = getDominentColor(bgBitmap, gradientAreaRect);
                    Log.d(TAG, "extractedColor of bottom = " + ((extractColor >> 16) & 255));
                    bottomFilter.resetGradient();
                    if (((extractColor >> 16) & 255) > 200) {
                        bottomFilter.setGradient(0.0f, 0.0f, Color.argb(BOTTOM_DIM_ALPHA, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), 0.0f, VIGNETTING_BOTTOM_RATIO, Color.argb(0, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR));
                    } else {
                        bottomFilter.setGradient(0.0f, 0.0f, Color.argb(BOTTOM_DIM_ALPHA, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), 0.0f, VIGNETTING_BOTTOM_RATIO, Color.argb(0, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)));
                    }
                    filterSet.addFilter(topFilter);
                    filterSet.addFilter(bottomFilter);
                    this.mFilterImageView.setImageFilter(filterSet);
                }
            }
        }
    }

    private Rect getBitmapCenterCropRect(Bitmap bitmap, int width, int height) {
        Log.d(TAG, "getCenterCropRect()");
        Rect cropRect = new Rect();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float ratio = ((float) width) / ((float) height);
        if (((float) bitmapWidth) / ((float) bitmapHeight) > ratio) {
            Log.d(TAG, "left and rigth are cropped");
            int targetWidth = (int) (((float) bitmapHeight) * ratio);
            cropRect.set((bitmapWidth - targetWidth) / 2, 0, targetWidth, bitmapHeight);
        } else {
            Log.d(TAG, "top and bottom are cropped");
            int targetHeight = (int) (((float) bitmapWidth) / ratio);
            cropRect.set(0, (bitmapHeight - targetHeight) / 2, bitmapWidth, targetHeight);
        }
        return cropRect;
    }

    private Rect calcGradientArea(Rect bitmapCropRect, int direction) {
        Rect gradientAreaRect = new Rect();
        int height = bitmapCropRect.bottom - bitmapCropRect.top;
        switch (direction) {
            case 0:
                gradientAreaRect.left = bitmapCropRect.left;
                gradientAreaRect.top = bitmapCropRect.top;
                gradientAreaRect.right = bitmapCropRect.right;
                gradientAreaRect.bottom = (int) (((float) bitmapCropRect.top) + (((float) height) * VIGNETTING_TOP_RATIO));
                return gradientAreaRect;
            case 1:
                gradientAreaRect.left = bitmapCropRect.left;
                gradientAreaRect.top = height - ((int) (((float) height) * VIGNETTING_BOTTOM_RATIO));
                gradientAreaRect.right = bitmapCropRect.right;
                gradientAreaRect.bottom = bitmapCropRect.bottom;
                return gradientAreaRect;
            default:
                return null;
        }
    }

    private int getDominentColor(Bitmap bitmap, Rect region) {
        long sumR = 0;
        long sumG = 0;
        long sumB = 0;
        long sampleCount = 0;
        long lowestR = 0;
        long lowestG = 0;
        long lowestB = 0;
        try {
            float ratio;
            int finalR;
            int finalG;
            int finalB;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int step = w > h ? (int) (((float) h) / 100.0f) : (int) (((float) w) / 100.0f);
            if (step <= 0) {
                step = 1;
            }
            for (int x = 0; x < w; x += step) {
                int y = region.top;
                while (y < region.bottom) {
                    if (!(x == 0 || y == 0)) {
                        int color = bitmap.getPixel(x, y);
                        long curR = (long) Color.red(color);
                        sumR += curR;
                        long curG = (long) Color.green(color);
                        sumG += curG;
                        long curB = (long) Color.blue(color);
                        sumB += curB;
                        sampleCount++;
                        if (lowestR > curR || lowestR == 0) {
                            lowestR = curR;
                        }
                        if (lowestG > curG || lowestG == 0) {
                            lowestG = curG;
                        }
                        if (lowestB > curB || lowestB == 0) {
                            lowestB = curB;
                        }
                    }
                    y += step;
                }
            }
            int everageR = (int) (sumR / sampleCount);
            int everageG = (int) (sumG / sampleCount);
            int everageB = (int) (sumB / sampleCount);
            if (((long) everageR) > lowestR) {
                ratio = ((float) (((long) everageR) - lowestR)) / ((float) everageR);
                if (ratio > 0.25f) {
                    ratio = 0.25f;
                }
                if (ratio > 0.0f) {
                    finalR = (int) ((1.0f - ratio) * ((float) everageR));
                } else {
                    finalR = everageR;
                }
            } else {
                finalR = everageR;
            }
            if (((long) everageG) > lowestG) {
                ratio = ((float) (((long) everageG) - lowestG)) / ((float) everageG);
                if (ratio > 0.25f) {
                    ratio = 0.25f;
                }
                if (ratio > 0.0f) {
                    finalG = (int) ((1.0f - ratio) * ((float) everageG));
                } else {
                    finalG = everageG;
                }
            } else {
                finalG = everageG;
            }
            if (((long) everageB) > lowestB) {
                ratio = ((float) (((long) everageB) - lowestB)) / ((float) everageB);
                if (ratio > 0.25f) {
                    ratio = 0.25f;
                }
                if (ratio > 0.0f) {
                    finalB = (int) ((1.0f - ratio) * ((float) everageB));
                } else {
                    finalB = everageB;
                }
            } else {
                finalB = everageB;
            }
            int resultColor = Color.argb(255, finalR, finalG, finalB);
            if (finalR <= 200 || finalG <= 200 || finalB <= 200) {
                return resultColor;
            }
            return this.mDefaultColor;
        } catch (Exception e) {
            return this.mDefaultColor;
        }
    }
}
