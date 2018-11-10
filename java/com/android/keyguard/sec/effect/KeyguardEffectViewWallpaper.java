package com.android.keyguard.sec.effect;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.interpolator.CubicEaseOut;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;

public class KeyguardEffectViewWallpaper extends FrameLayout implements KeyguardEffectViewBase {
    private final boolean DBG;
    private final int SCREEN_ON_ANIMATION_DURATION;
    private final float SCREEN_ON_BACKGROUND_SCALE;
    private final String TAG;
    private boolean isSupportMobileKeyboard;
    private Choreographer mChoreographer;
    private Context mContext;
    private int mCurrentMobileKeyboard;
    private int mDisplayId;
    private boolean mIsLiveWallpaper;
    private KeyguardWindowCallback mKeyguardWindowCallback;
    private ImageView mLockScreenWallpaperImage;
    private ValueAnimator mScreenOnAnim;
    private float mScreenOnAnimationValue;
    private Runnable mScreenOnRunnable;
    private String mWallpaperPath;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWallpaper$1 */
    class C05181 implements AnimatorUpdateListener {
        C05181() {
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            KeyguardEffectViewWallpaper.this.mScreenOnAnimationValue = ((Float) animation.getAnimatedValue()).floatValue();
            float wallpaperScale = (0.049999952f * KeyguardEffectViewWallpaper.this.mScreenOnAnimationValue) + 1.0f;
            KeyguardEffectViewWallpaper.this.setScaleX(wallpaperScale);
            KeyguardEffectViewWallpaper.this.setScaleY(wallpaperScale);
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWallpaper$2 */
    class C05192 implements Runnable {
        C05192() {
        }

        public void run() {
            KeyguardEffectViewWallpaper.this.mScreenOnAnim.start();
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWallpaper$3 */
    class C05203 implements Runnable {
        C05203() {
        }

        public void run() {
            Log.d("WallpaperWidget", "dispatchDraw() mKeyguardWindowCallback.onShown()");
            if (KeyguardEffectViewWallpaper.this.mKeyguardWindowCallback != null) {
                KeyguardEffectViewWallpaper.this.mKeyguardWindowCallback.onShown();
            }
        }
    }

    private void setAnimator() {
        this.mScreenOnAnim = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        this.mScreenOnAnim.setInterpolator(new CubicEaseOut());
        this.mScreenOnAnim.setDuration(700);
        this.mScreenOnAnim.addUpdateListener(new C05181());
        this.mScreenOnRunnable = new C05192();
    }

    public KeyguardEffectViewWallpaper(Context context) {
        this(context, null);
    }

    public KeyguardEffectViewWallpaper(Context context, KeyguardWindowCallback callback) {
        this(context, null, false, 0);
    }

    public KeyguardEffectViewWallpaper(Context context, KeyguardWindowCallback callback, boolean isProcessSeparated, int displayId) {
        super(context);
        this.TAG = "WallpaperWidget";
        this.DBG = true;
        this.mLockScreenWallpaperImage = null;
        this.mChoreographer = Choreographer.getInstance();
        this.SCREEN_ON_ANIMATION_DURATION = 700;
        this.SCREEN_ON_BACKGROUND_SCALE = 1.05f;
        this.isSupportMobileKeyboard = false;
        this.mCurrentMobileKeyboard = 0;
        this.mDisplayId = -1;
        Log.d("WallpaperWidget", "KeyguardEffectViewWallpaper isProcessSeparated : " + isProcessSeparated);
        Log.d("WallpaperWidget", "KeyguardEffectViewWallpaper isDualScreen : " + displayId);
        this.mContext = context;
        this.mKeyguardWindowCallback = callback;
        this.isSupportMobileKeyboard = KeyguardProperties.isSupportMobileKeyboard();
        this.mDisplayId = displayId;
        this.mCurrentMobileKeyboard = getResources().getConfiguration().mobileKeyboardCovered;
        if (!KeyguardEffectViewUtil.isKeyguardEffectViewWallpaper(context)) {
            updateWallpaper(this.mContext);
            if (KeyguardProperties.useWaterDropletEffect(this.mContext)) {
                setAnimator();
            }
        }
    }

    private void updateWallpaper(Context mContext) {
        this.mLockScreenWallpaperImage = new ImageView(mContext);
        this.mLockScreenWallpaperImage.setScaleType(ScaleType.CENTER_CROP);
        this.mLockScreenWallpaperImage.setDrawingCacheEnabled(true);
        int heights = -1;
        if (this.mCurrentMobileKeyboard == 1) {
            heights = mContext.getResources().getDisplayMetrics().heightPixels;
        }
        addView(this.mLockScreenWallpaperImage, -1, heights);
        setLockScreenWallpaper();
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mKeyguardWindowCallback != null) {
            postDelayed(new C05203(), 100);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.isSupportMobileKeyboard && this.mCurrentMobileKeyboard != newConfig.mobileKeyboardCovered) {
            Log.d("WallpaperWidget", "keyboard ConfigurationChange");
            this.mCurrentMobileKeyboard = newConfig.mobileKeyboardCovered;
            updateWallpaper(this.mContext);
        }
    }

    public void cleanUp() {
    }

    public void setLockScreenWallpaper() {
        Log.d("WallpaperWidget", "setLockScreenWallpaper()");
        if (this.mLockScreenWallpaperImage != null) {
            this.mLockScreenWallpaperImage.setImageDrawable(KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId));
        }
    }

    public void show() {
    }

    public void reset() {
    }

    public void update() {
        setLockScreenWallpaper();
    }

    public void screenTurnedOn() {
        if (this.mScreenOnRunnable != null) {
            this.mChoreographer.postCallback(1, this.mScreenOnRunnable, null);
        }
    }

    public void screenTurnedOff() {
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
    }

    public long getUnlockDelay() {
        return 0;
    }

    public void handleUnlock(View view, MotionEvent event) {
    }

    public void playLockSound() {
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        return false;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    public ImageView getWallpaperImage() {
        if (this.mLockScreenWallpaperImage != null) {
            return this.mLockScreenWallpaperImage;
        }
        return null;
    }

    public Bitmap getBitmapWallpaperImage() {
        Log.d("WallpaperWidget", "getWallpaperImage()");
        if (this.mLockScreenWallpaperImage == null) {
            return null;
        }
        this.mLockScreenWallpaperImage.buildDrawingCache();
        return this.mLockScreenWallpaperImage.getDrawingCache();
    }

    public void setHidden(boolean isHidden) {
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.d("WallpaperWidget", "setContextualWallpaper()");
        if (this.mLockScreenWallpaperImage != null) {
            this.mLockScreenWallpaperImage.setImageBitmap(bmp);
        }
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
