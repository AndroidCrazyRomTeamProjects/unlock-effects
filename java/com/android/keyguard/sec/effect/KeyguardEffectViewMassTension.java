package com.android.keyguard.sec.effect;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.SystemClock;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.C0302R;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import java.io.File;

public class KeyguardEffectViewMassTension extends FrameLayout implements KeyguardEffectViewBase {
    private static final String sound_tap_path = "/system/media/audio/ui/Tap_tension.ogg";
    private final int CIRCLE_MAX_ALPHA;
    private final float CIRCLE_MAX_ALPHA_FACTOR;
    private final int CIRCLE_MIN_ALPHA;
    protected String TAG;
    private final int TENSION_BETWEEN_FACTOR;
    private final int TENSION_CIRCLE_PLACE_ADJUST;
    private final int TENSION_LINE_DELETE;
    private final float TENSION_LINE_MIN;
    private final float TENSION_RELEASE_FACTOR;
    protected final double UNLOCK_DRAG_THRESHOLD;
    protected final double UNLOCK_RELEASE_THRESHOLD;
    private final long UNLOCK_SOUND_PLAY_TIME;
    protected final double UNLOCK_TEMP_THRESHOLD;
    private int betweenLineX;
    private int betweenLineY;
    private double degree;
    private long diffPressTime;
    private boolean isIgnoreTouch;
    private boolean isSystemSoundChecked;
    private float lineSize;
    private ImageView mCircleCenterDot;
    private ImageView mCircleCenterDotAfter;
    private Animation mCircleCenterDotAnim;
    Point mCircleCenterDotFromPoint;
    private Animation mCircleCenterDotReleaseAnim;
    protected RelativeLayout mCircleCenterDotRoot;
    Point mCircleCenterDotToPoint;
    private ImageView mCircleFinger;
    private ImageView mCircleFingerAfter;
    private Animation mCircleFingerAnim;
    private Animation mCircleFingerReleaseAnim;
    protected RelativeLayout mCircleFingerRoot;
    private ImageView mCircleLine;
    private ImageView mCircleLineAfter;
    protected RelativeLayout mCircleLineRoot;
    private ImageView mCircleOuter;
    private ImageView mCircleOuterAfter;
    private Animation mCircleOuterAnim;
    protected RelativeLayout mCircleOuterRoot;
    private final Context mContext;
    private int mDisplayId;
    private double mDistanceRatio;
    private ScaleAnimation mLineAnim;
    private final float mLockSoundVolume;
    private SoundPool mSoundPool;
    protected float mX;
    protected float mY;
    private double outerTensionFactorX;
    private double outerTensionFactorY;
    private long prevPressTime;
    private double radian;
    private Runnable releaseSoundRunnable;
    private int sounds;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewMassTension$1 */
    class C05061 implements AnimationListener {
        C05061() {
        }

        public void onAnimationEnd(Animation animation) {
            KeyguardEffectViewMassTension.this.mCircleCenterDot.setVisibility(4);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewMassTension$2 */
    class C05072 implements AnimationListener {
        C05072() {
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
            int fromX = KeyguardEffectViewMassTension.this.mCircleCenterDotFromPoint.x;
            int fromY = KeyguardEffectViewMassTension.this.mCircleCenterDotFromPoint.y;
            int toX = KeyguardEffectViewMassTension.this.mCircleCenterDotToPoint.x;
            int toY = KeyguardEffectViewMassTension.this.mCircleCenterDotToPoint.y;
            KeyguardEffectViewMassTension.this.mCircleCenterDot.setX((float) fromX);
            KeyguardEffectViewMassTension.this.mCircleCenterDot.setY((float) fromY);
            KeyguardEffectViewMassTension.this.mCircleCenterDot.setVisibility(0);
            AnimationSet animationSet = KeyguardEffectViewMassTension.this.createBouncerAnimation();
            animationSet.setDuration(250);
            KeyguardEffectViewMassTension.this.mCircleCenterDot.startAnimation(animationSet);
        }
    }

    public KeyguardEffectViewMassTension(Context context) {
        this(context, null, false, 0);
    }

    public KeyguardEffectViewMassTension(Context context, KeyguardWindowCallback callback) {
        this(context, callback, false, 0);
    }

    public KeyguardEffectViewMassTension(Context context, KeyguardWindowCallback callback, boolean isProcessSeparated, int displayId) {
        super(context);
        this.TAG = "TensionLockScreen";
        this.CIRCLE_MAX_ALPHA = 255;
        this.CIRCLE_MIN_ALPHA = 50;
        this.UNLOCK_TEMP_THRESHOLD = 1.2000000476837158d;
        this.UNLOCK_RELEASE_THRESHOLD = 1.399999976158142d;
        this.UNLOCK_DRAG_THRESHOLD = 2.0999999046325684d;
        this.TENSION_RELEASE_FACTOR = 0.8f;
        this.CIRCLE_MAX_ALPHA_FACTOR = 0.8f;
        this.TENSION_BETWEEN_FACTOR = 40;
        this.TENSION_LINE_MIN = 0.0f;
        this.TENSION_CIRCLE_PLACE_ADJUST = 5;
        this.mDisplayId = -1;
        this.isIgnoreTouch = false;
        this.UNLOCK_SOUND_PLAY_TIME = 2000;
        this.mCircleCenterDotToPoint = new Point();
        this.mCircleCenterDotFromPoint = new Point();
        this.mSoundPool = null;
        this.diffPressTime = 0;
        this.prevPressTime = 0;
        this.isSystemSoundChecked = true;
        this.mContext = context;
        this.mDisplayId = displayId;
        LayoutInflater.from(this.mContext).inflate(C0302R.layout.keyguard_mass_tension_effect_view, this, true);
        this.TENSION_LINE_DELETE = (int) getResources().getDimension(C0302R.dimen.tension_line_delete);
        setLayout();
        setAnimation();
        setLineAnim(0.0f, 0.0f);
        this.mLockSoundVolume = (float) Math.pow(10.0d, (double) (((float) context.getResources().getInteger(17694725)) / 20.0f));
    }

    private void setLayout() {
        this.mCircleOuter = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_in_press);
        this.mCircleFinger = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_finger);
        this.mCircleCenterDot = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_center_dot);
        this.mCircleLine = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_line);
        this.mCircleOuterRoot = (RelativeLayout) findViewById(C0302R.id.keyguard_tension_effect_tension_in_press_root);
        this.mCircleOuterAfter = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_in_press_after);
        this.mCircleFingerRoot = (RelativeLayout) findViewById(C0302R.id.keyguard_tension_effect_tension_finger_root);
        this.mCircleFingerAfter = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_finger_after);
        this.mCircleCenterDotRoot = (RelativeLayout) findViewById(C0302R.id.keyguard_tension_effect_tension_center_dot_root);
        this.mCircleCenterDotAfter = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_center_dot_after);
        this.mCircleLineRoot = (RelativeLayout) findViewById(C0302R.id.keyguard_tension_effect_tension_line_root);
        this.mCircleLineAfter = (ImageView) findViewById(C0302R.id.keyguard_tension_effect_tension_line_after);
    }

    private void setAnimation() {
        this.mCircleFingerAnim = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.keyguard_tention_animate_fadeout);
        this.mCircleCenterDotAnim = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.keyguard_tention_animate_fadeout);
        this.mCircleOuterAnim = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.keyguard_tention_animate_alpha);
        this.mCircleFingerReleaseAnim = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.keyguard_tention_animate_finger);
        this.mCircleCenterDotReleaseAnim = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.keyguard_tention_animate_centerdot_release);
    }

    private AnimationSet createBouncerAnimation() {
        int fromX = this.mCircleCenterDotFromPoint.x;
        int fromY = this.mCircleCenterDotFromPoint.y;
        int toX = this.mCircleCenterDotToPoint.x;
        int toY = this.mCircleCenterDotToPoint.y;
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translate = new TranslateAnimation(0.0f, (float) (toX - fromX), 0.0f, (float) (toY - fromY));
        translate.setDuration(250);
        translate.setFillAfter(true);
        translate.setInterpolator(new DecelerateInterpolator());
        animationSet.addAnimation(translate);
        animationSet.setAnimationListener(new C05061());
        return animationSet;
    }

    private void setLineAnim(float firstvalue, float lastvalue) {
        this.mLineAnim = new ScaleAnimation(firstvalue, lastvalue, 1.0f, 1.0f);
        this.mLineAnim.setStartOffset(50);
        this.mLineAnim.setDuration(250);
        this.mLineAnim.setFillAfter(true);
        this.mLineAnim.setInterpolator(new DecelerateInterpolator());
        this.mLineAnim.setAnimationListener(new C05072());
    }

    private void setOuterCircle(View v, double value) {
        int alpha = (int) (50.0d + ((255.0d * value) * 0.800000011920929d));
        if (alpha >= 255) {
            alpha = 255;
        }
        this.mCircleOuter.setImageAlpha(alpha);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (this.isIgnoreTouch) {
            if (event.getAction() == 1) {
                this.isIgnoreTouch = false;
            }
            return false;
        }
        switch (event.getAction()) {
            case 0:
                clearAllViews();
                this.mCircleOuter.setImageAlpha(50);
                this.mCircleFinger.setImageAlpha(255);
                this.mCircleCenterDot.setImageAlpha(255);
                this.mCircleLine.setImageAlpha(255);
                this.mX = event.getRawX();
                this.mY = event.getRawY();
                this.mCircleOuter.setVisibility(0);
                this.mCircleOuter.setX((float) (((int) event.getRawX()) - (this.mCircleOuter.getMeasuredWidth() / 2)));
                this.mCircleOuter.setY((float) (((int) event.getRawY()) - (this.mCircleOuter.getMeasuredHeight() / 2)));
                this.mCircleFinger.setVisibility(0);
                this.mCircleFinger.setX((float) (((int) event.getRawX()) - (this.mCircleFinger.getMeasuredWidth() / 2)));
                this.mCircleFinger.setY((float) (((int) event.getRawY()) - (this.mCircleFinger.getMeasuredHeight() / 2)));
                this.mCircleCenterDot.setVisibility(0);
                this.mCircleCenterDot.setX((float) (((int) event.getRawX()) - (this.mCircleCenterDot.getMeasuredWidth() / 2)));
                this.mCircleCenterDot.setY((float) (((int) event.getRawY()) - (this.mCircleCenterDot.getMeasuredHeight() / 2)));
                this.mCircleCenterDotToPoint.x = ((int) event.getRawX()) - (this.mCircleCenterDot.getMeasuredWidth() / 2);
                this.mCircleCenterDotToPoint.y = ((int) event.getRawY()) - (this.mCircleCenterDot.getMeasuredHeight() / 2);
                this.mCircleLine.setVisibility(0);
                this.mCircleLine.setScaleX(0.0f);
                this.prevPressTime = SystemClock.uptimeMillis();
                this.diffPressTime = 0;
                playSound();
                break;
            case 1:
                this.mCircleOuter.setVisibility(4);
                this.mCircleFinger.setVisibility(4);
                this.mCircleLine.setVisibility(4);
                this.mCircleOuterRoot.setX((float) (((int) this.mX) - (this.mCircleOuterAfter.getMeasuredWidth() / 2)));
                this.mCircleOuterRoot.setY((float) (((int) this.mY) - (this.mCircleOuterAfter.getMeasuredHeight() / 2)));
                this.mCircleOuterAfter.startAnimation(this.mCircleOuterAnim);
                this.betweenLineX = (int) (this.mX + ((event.getRawX() - this.mX) / 40.0f));
                this.betweenLineY = (int) (this.mY + ((event.getRawY() - this.mY) / 40.0f));
                this.diffPressTime = SystemClock.uptimeMillis() - this.prevPressTime;
                if (this.mDistanceRatio >= 1.399999976158142d) {
                    if (this.mDistanceRatio >= 1.399999976158142d && this.mDistanceRatio <= 2.0999999046325684d) {
                        this.mCircleFingerRoot.setX((float) (((int) event.getRawX()) - (this.mCircleFingerAfter.getMeasuredWidth() / 2)));
                        this.mCircleFingerRoot.setY((float) (((int) event.getRawY()) - (this.mCircleFingerAfter.getMeasuredHeight() / 2)));
                        this.mCircleFingerAfter.startAnimation(this.mCircleFingerReleaseAnim);
                        this.mCircleLineRoot.setX((float) this.betweenLineX);
                        this.mCircleLineRoot.setY((float) (this.betweenLineY - (this.mCircleLine.getMeasuredHeight() / 2)));
                        this.mCircleLineRoot.setPivotX(0.0f);
                        this.mCircleLineRoot.setPivotY((float) (this.mCircleLineAfter.getMeasuredHeight() / 2));
                        this.mCircleLineRoot.setRotation((float) this.degree);
                        setLineAnim(this.lineSize, 0.0f);
                        this.mCircleLineAfter.startAnimation(this.mLineAnim);
                        this.mCircleCenterDotFromPoint.x = this.betweenLineX - (this.mCircleCenterDot.getMeasuredWidth() / 2);
                        this.mCircleCenterDotFromPoint.y = this.betweenLineY - (this.mCircleCenterDot.getMeasuredHeight() / 2);
                        break;
                    }
                }
                this.mCircleFingerRoot.setX((float) (((int) event.getRawX()) - (this.mCircleFingerAfter.getMeasuredWidth() / 2)));
                this.mCircleFingerRoot.setY((float) (((int) event.getRawY()) - (this.mCircleFingerAfter.getMeasuredHeight() / 2)));
                this.mCircleFingerAfter.startAnimation(this.mCircleFingerAnim);
                this.mCircleCenterDotRoot.setX((float) (this.betweenLineX - (this.mCircleCenterDotAfter.getMeasuredWidth() / 2)));
                this.mCircleCenterDotRoot.setY((float) (this.betweenLineY - (this.mCircleCenterDotAfter.getMeasuredHeight() / 2)));
                this.mCircleCenterDotAfter.startAnimation(this.mCircleCenterDotAnim);
                this.mCircleCenterDot.setVisibility(4);
                if (this.diffPressTime > 600) {
                    playSound();
                    break;
                }
                break;
            case 2:
                this.mDistanceRatio = Math.sqrt(Math.pow((double) ((int) (event.getRawX() - this.mX)), 2.0d) + Math.pow((double) ((int) (event.getRawY() - this.mY)), 2.0d)) / ((double) (this.mCircleOuter.getWidth() / 2));
                this.betweenLineX = (int) (this.mX + ((event.getRawX() - this.mX) / 40.0f));
                this.betweenLineY = (int) (this.mY + ((event.getRawY() - this.mY) / 40.0f));
                setOuterCircle(view, this.mDistanceRatio);
                this.radian = Math.atan2((double) (-1.0f * (event.getRawY() - this.mY)), (double) (event.getRawX() - this.mX));
                this.degree = ((-this.radian) / 3.141592653589793d) * 180.0d;
                this.outerTensionFactorX = ((double) this.mX) + (((double) (((this.mCircleFinger.getMeasuredWidth() / 2) + (this.mCircleOuter.getMeasuredWidth() / 2)) - 5)) * Math.cos((this.degree / 180.0d) * 3.141592653589793d));
                this.outerTensionFactorY = ((double) this.mY) - (((double) (((this.mCircleFinger.getMeasuredHeight() / 2) + (this.mCircleOuter.getMeasuredHeight() / 2)) - 5)) * Math.sin(((-this.degree) / 180.0d) * 3.141592653589793d));
                float lineSizebaseX;
                float lineSizebaseY;
                if (this.mDistanceRatio >= 1.2000000476837158d) {
                    if (this.mDistanceRatio >= 1.2000000476837158d && this.mDistanceRatio <= 2.0999999046325684d) {
                        this.mCircleFinger.setX((float) ((int) (this.outerTensionFactorX - ((double) (this.mCircleFinger.getMeasuredWidth() / 2)))));
                        this.mCircleFinger.setY((float) ((int) (this.outerTensionFactorY - ((double) (this.mCircleFinger.getMeasuredHeight() / 2)))));
                        this.mCircleCenterDot.setX((float) (this.betweenLineX - (this.mCircleCenterDot.getMeasuredWidth() / 2)));
                        this.mCircleCenterDot.setY((float) (this.betweenLineY - (this.mCircleCenterDot.getMeasuredHeight() / 2)));
                        this.mCircleLine.setX((float) this.betweenLineX);
                        this.mCircleLine.setY((float) (this.betweenLineY - (this.mCircleLine.getMeasuredHeight() / 2)));
                        this.mCircleLine.setPivotX(0.0f);
                        this.mCircleLine.setPivotY((float) (this.mCircleLine.getMeasuredHeight() / 2));
                        lineSizebaseX = (float) (this.outerTensionFactorX - ((double) this.betweenLineX));
                        lineSizebaseY = (float) (this.outerTensionFactorY - ((double) this.betweenLineY));
                        this.lineSize = (float) ((Math.sqrt((double) ((lineSizebaseX * lineSizebaseX) + (lineSizebaseY * lineSizebaseY))) - ((double) (this.mCircleCenterDot.getMeasuredWidth() / 2))) - ((double) this.TENSION_LINE_DELETE));
                        this.lineSize = this.lineSize > 0.0f ? this.lineSize : 0.0f;
                        this.mCircleLine.setScaleX(this.lineSize);
                        this.mCircleLine.setRotation((float) this.degree);
                        break;
                    }
                    this.mCircleOuter.setVisibility(4);
                    this.mCircleFinger.setVisibility(4);
                    this.mCircleLine.setVisibility(4);
                    this.mCircleFingerRoot.setX((float) ((int) (event.getRawX() - ((float) (this.mCircleFingerAfter.getMeasuredWidth() / 2)))));
                    this.mCircleFingerRoot.setY((float) ((int) (event.getRawY() - ((float) (this.mCircleFingerAfter.getMeasuredHeight() / 2)))));
                    this.mCircleFingerAfter.startAnimation(this.mCircleFingerReleaseAnim);
                    this.mCircleOuterRoot.setX((float) (((int) this.mX) - (this.mCircleOuterAfter.getMeasuredWidth() / 2)));
                    this.mCircleOuterRoot.setY((float) (((int) this.mY) - (this.mCircleOuterAfter.getMeasuredHeight() / 2)));
                    this.mCircleOuterAfter.startAnimation(this.mCircleOuterAnim);
                    this.mCircleLineRoot.setX((float) this.betweenLineX);
                    this.mCircleLineRoot.setY((float) (this.betweenLineY - (this.mCircleLine.getMeasuredHeight() / 2)));
                    this.mCircleLineRoot.setPivotX(0.0f);
                    this.mCircleLineRoot.setPivotY((float) (this.mCircleLineAfter.getMeasuredHeight() / 2));
                    this.mCircleLineRoot.setRotation((float) this.degree);
                    setLineAnim(this.lineSize, 0.0f);
                    this.mCircleLineAfter.startAnimation(this.mLineAnim);
                    this.mCircleCenterDotFromPoint.x = this.betweenLineX - (this.mCircleCenterDot.getMeasuredWidth() / 2);
                    this.mCircleCenterDotFromPoint.y = this.betweenLineY - (this.mCircleCenterDot.getMeasuredHeight() / 2);
                    break;
                }
                this.mCircleFinger.setX((float) (((int) event.getRawX()) - (this.mCircleFinger.getMeasuredWidth() / 2)));
                this.mCircleFinger.setY((float) (((int) event.getRawY()) - (this.mCircleFinger.getMeasuredHeight() / 2)));
                this.mCircleCenterDot.setX((float) (this.betweenLineX - (this.mCircleCenterDot.getMeasuredWidth() / 2)));
                this.mCircleCenterDot.setY((float) (this.betweenLineY - (this.mCircleCenterDot.getMeasuredHeight() / 2)));
                this.mCircleLine.setX((float) this.betweenLineX);
                this.mCircleLine.setY((float) (this.betweenLineY - (this.mCircleLine.getMeasuredHeight() / 2)));
                this.mCircleLine.setPivotX(0.0f);
                this.mCircleLine.setPivotY((float) (this.mCircleLine.getMeasuredHeight() / 2));
                lineSizebaseX = event.getRawX() - (this.mX + ((event.getRawX() - this.mX) / 40.0f));
                lineSizebaseY = event.getRawY() - (this.mY + ((event.getRawY() - this.mY) / 40.0f));
                this.lineSize = (float) ((Math.sqrt((double) ((lineSizebaseX * lineSizebaseX) + (lineSizebaseY * lineSizebaseY))) - ((double) (this.mCircleCenterDot.getMeasuredWidth() / 2))) - ((double) this.TENSION_LINE_DELETE));
                this.lineSize = this.lineSize > 0.0f ? this.lineSize : 0.0f;
                this.mCircleLine.setScaleX(this.lineSize);
                this.mCircleLine.setRotation((float) this.degree);
                break;
                break;
        }
        return true;
    }

    private void clearAllViews() {
        this.mCircleOuter.setImageAlpha(0);
        this.mCircleFinger.setImageAlpha(0);
        this.mCircleCenterDot.setImageAlpha(0);
        this.mCircleLine.setImageAlpha(0);
        this.mCircleLineAfter.clearAnimation();
        this.mCircleOuter.setVisibility(4);
        this.mCircleCenterDot.setVisibility(4);
        this.mCircleFinger.setVisibility(4);
        this.mCircleLine.setVisibility(4);
    }

    public void handleUnlock(View view, MotionEvent event) {
    }

    public void show() {
        checkSound();
        setSound();
    }

    public void cleanUp() {
        clearAllViews();
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }

    public void reset() {
        Log.d(this.TAG, "reset");
        clearAllViews();
    }

    public void screenTurnedOn() {
        Log.d(this.TAG, "screenTurnedOn");
    }

    public long getUnlockDelay() {
        return 500;
    }

    public void playLockSound() {
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(this.TAG, "onConfigurationChanged");
    }

    public boolean handleHoverEvent(MotionEvent event) {
        return false;
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
    }

    public void update() {
    }

    public void screenTurnedOff() {
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    private void checkSound() {
        int result = 0;
        try {
            result = System.getInt(this.mContext.getContentResolver(), "lockscreen_sounds_enabled");
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (result == 1) {
            this.isSystemSoundChecked = true;
        } else {
            this.isSystemSoundChecked = false;
        }
    }

    private void setSound() {
        if (this.mSoundPool == null) {
            Log.d(this.TAG, "show mSoundPool is null");
            File checkTapFile = new File(sound_tap_path);
            this.mSoundPool = new SoundPool(10, 1, 0);
            this.sounds = this.mSoundPool.load(sound_tap_path, 1);
        }
    }

    private void playSound() {
        if (this.isSystemSoundChecked && this.mSoundPool != null) {
            this.mSoundPool.play(this.sounds, this.mLockSoundVolume, this.mLockSoundVolume, 1, 0, 1.0f);
        }
    }

    public void setHidden(boolean isHidden) {
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
    }

    public static boolean isBackgroundEffect() {
        return false;
    }

    public static String getCounterEffectName() {
        return "Wallpaper";
    }
}
