package com.android.keyguard.sec.effect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFilter;
import android.graphics.ImageFilter.BitmapColorMaskFilter;
import android.graphics.ImageFilterSet;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;
import com.android.keyguard.sec.KeyguardProperties;

public class VignettingEffect {
    private static int BOTTOM_DIM_ALPHA = 51;
    private static final int BOTTOM_TO_TOP = 1;
    private static final int DEFAULT_COLOR = 110;
    private static final int LIMITTED_VALUE = 200;
    private static final String TAG = "VignettingEffect";
    private static int TOP_DIM_ALPHA = 77;
    private static final int TOP_TO_BOTTOM = 0;
    private static float VIGNETTING_BOTTOM_RATIO = 0.117f;
    private static float VIGNETTING_TOP_RATIO = 0.1f;
    private static int mDefaultColor = Color.argb(255, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR);

    public static void init() {
        if (KeyguardProperties.isTabAProject()) {
            TOP_DIM_ALPHA = convertToAlphaValue(60);
            BOTTOM_DIM_ALPHA = convertToAlphaValue(65);
            VIGNETTING_TOP_RATIO = 0.11f;
            VIGNETTING_BOTTOM_RATIO = 0.14f;
        }
    }

    private static int convertToAlphaValue(int percent) {
        return (percent * 255) / 100;
    }

    public static void resetBlendedFilter(ImageView imageView) {
        if (imageView != null) {
            ImageFilterSet filterSet = new ImageFilterSet();
            imageView.setLayerType(2, null);
            BitmapColorMaskFilter topFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
            BitmapColorMaskFilter bottomFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
            topFilter.resetGradient();
            bottomFilter.resetGradient();
            filterSet.addFilter(topFilter);
            filterSet.addFilter(bottomFilter);
            imageView.setImageFilter(filterSet);
        }
    }

    public static void applyBlendedFilter(ImageView imageView) {
        if (imageView != null) {
            Bitmap wallpaperBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            if (wallpaperBitmap != null) {
                ImageFilterSet filterSet = new ImageFilterSet();
                imageView.setLayerType(2, null);
                BitmapColorMaskFilter topFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
                BitmapColorMaskFilter bottomFilter = (BitmapColorMaskFilter) ImageFilter.createImageFilter(52);
                init();
                Rect mRect = new Rect();
                calcGradientArea(mRect, 0, wallpaperBitmap);
                int extractColor = getDominentColor(wallpaperBitmap, mRect);
                Log.d(TAG, "extractedColor of top = " + ((extractColor >> 16) & 255));
                topFilter.resetGradient();
                if (((extractColor >> 16) & 255) > LIMITTED_VALUE) {
                    topFilter.setGradient(0.0f, 1.0f, Color.argb(TOP_DIM_ALPHA, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), 0.0f, 1.0f - VIGNETTING_TOP_RATIO, Color.argb(0, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR));
                } else {
                    topFilter.setGradient(0.0f, 1.0f, Color.argb(TOP_DIM_ALPHA, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), 0.0f, 1.0f - VIGNETTING_TOP_RATIO, Color.argb(0, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)));
                }
                mRect = new Rect();
                calcGradientArea(mRect, 1, wallpaperBitmap);
                extractColor = getDominentColor(wallpaperBitmap, mRect);
                Log.d(TAG, "extractedColor of bottom = " + ((extractColor >> 16) & 255));
                bottomFilter.resetGradient();
                if (((extractColor >> 16) & 255) > LIMITTED_VALUE) {
                    bottomFilter.setGradient(0.0f, 0.0f, Color.argb(BOTTOM_DIM_ALPHA, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), 0.0f, VIGNETTING_BOTTOM_RATIO, Color.argb(0, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR));
                } else {
                    bottomFilter.setGradient(0.0f, 0.0f, Color.argb(BOTTOM_DIM_ALPHA, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), 0.0f, VIGNETTING_BOTTOM_RATIO, Color.argb(0, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)));
                }
                filterSet.addFilter(topFilter);
                filterSet.addFilter(bottomFilter);
                imageView.setImageFilter(filterSet);
            }
        }
    }

    public static Bitmap applyBlendedFilter(Bitmap wallpaperBitmap) {
        if (wallpaperBitmap != null) {
            Canvas canvas = new Canvas(wallpaperBitmap);
            Paint pnt = new Paint();
            init();
            Rect mRect = new Rect();
            calcGradientArea(mRect, 0, wallpaperBitmap);
            int extractColor = getDominentColor(wallpaperBitmap, mRect);
            if (((extractColor >> 16) & 255) > LIMITTED_VALUE) {
                pnt.setShader(new LinearGradient(((float) mRect.width()) * 0.5f, (float) mRect.top, ((float) mRect.width()) * 0.5f, (float) mRect.bottom, Color.argb(TOP_DIM_ALPHA, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), Color.argb(0, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), TileMode.CLAMP));
            } else {
                pnt.setShader(new LinearGradient(((float) mRect.width()) * 0.5f, (float) mRect.top, ((float) mRect.width()) * 0.5f, (float) mRect.bottom, Color.argb(TOP_DIM_ALPHA, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), Color.argb(0, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), TileMode.CLAMP));
            }
            canvas.drawRect(mRect, pnt);
            mRect = new Rect();
            calcGradientArea(mRect, 1, wallpaperBitmap);
            extractColor = getDominentColor(wallpaperBitmap, mRect);
            if (((extractColor >> 16) & 255) > LIMITTED_VALUE) {
                pnt.setShader(new LinearGradient(((float) mRect.width()) * 0.5f, (float) mRect.top, ((float) mRect.width()) * 0.5f, (float) mRect.bottom, Color.argb(0, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), Color.argb(BOTTOM_DIM_ALPHA, DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR), TileMode.CLAMP));
            } else {
                pnt.setShader(new LinearGradient(((float) mRect.width()) * 0.5f, (float) mRect.top, ((float) mRect.width()) * 0.5f, (float) mRect.bottom, Color.argb(0, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), Color.argb(BOTTOM_DIM_ALPHA, Color.red(extractColor), Color.green(extractColor), Color.blue(extractColor)), TileMode.CLAMP));
            }
            canvas.drawRect(mRect, pnt);
        }
        return wallpaperBitmap;
    }

    private static void calcGradientArea(Rect rect, int direction, Bitmap bitmap) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        switch (direction) {
            case 0:
                rect.left = 0;
                rect.top = 0;
                rect.right = bitmapWidth;
                rect.bottom = (int) (((float) bitmapHeight) * VIGNETTING_TOP_RATIO);
                return;
            case 1:
                rect.left = 0;
                rect.top = bitmapHeight - ((int) (((float) bitmapHeight) * VIGNETTING_BOTTOM_RATIO));
                rect.right = bitmapWidth;
                rect.bottom = bitmapHeight;
                return;
            default:
                return;
        }
    }

    public static int getDominentColor(Bitmap bitmap, Rect region) {
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
            if (finalR <= LIMITTED_VALUE || finalG <= LIMITTED_VALUE || finalB <= LIMITTED_VALUE) {
                return resultColor;
            }
            return mDefaultColor;
        } catch (Exception e) {
            return mDefaultColor;
        }
    }
}
