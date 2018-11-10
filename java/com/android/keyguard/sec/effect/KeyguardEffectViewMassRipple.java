package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.SoundPool;
import android.os.DVFSHelper;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import com.android.keyguard.C0302R;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import java.io.File;

public class KeyguardEffectViewMassRipple extends FrameLayout implements KeyguardEffectViewBase {
    protected static final String TAG = "KeyguardEffectViewMassRipple";
    private static final String mRDownPath = "/system/media/audio/ui/simple_ripple_down.ogg";
    private static final String mRUpPath = "/system/media/audio/ui/simple_ripple_up.ogg";
    private final int ACQUIRE_DVFS;
    protected final float ANIMATION_DURATION;
    final float CIRCLE_AFFORDANCE_ROUND_SIZE;
    final float CIRCLE_ROUND_SIZE;
    private final int COUNT_ANIMATION;
    private int CPU_CLOCK_NUM;
    private final int CPU_CLOK_CONTROL;
    private boolean DEBUG;
    private int GPU_FREQUNCY_NUM;
    private final int GPU_FREQ_CONTROL;
    final int MAX_RIPPLE_COUNT;
    final int MSG_AFFORDANCE_TOUCH;
    final int MSG_FIRST_TOUCH;
    final int MSG_TIME_TICK;
    private final int RELEASE_DVFS;
    final int SOUND_ID_DOWN;
    final int SOUND_ID_UP;
    private int TIME_FOR_CPU_GPU_MAX_LOCK;
    protected final double UNLOCK_DRAG_THRESHOLD;
    protected final double UNLOCK_RELEASE_THRESHOLD;
    DVFSHelper cpuMaxClockBooster;
    private long diffPressTime;
    int drawRippleCount;
    int firstTouch_X;
    int firstTouch_Y;
    DVFSHelper gpuMaxFreqBooster;
    private int indexAni;
    private boolean isRestrictCPUClock;
    private boolean isRestrictGPUFreq;
    private boolean isStartUnlock;
    private boolean isSystemSoundChecked;
    protected FrameLayout mCircleMain;
    private Context mContext;
    private DVFSHandlerForMassRipple mDVFSHandlerMassRipple;
    private int mDisplayId;
    private double mDistanceRatio;
    Handler mHandler;
    int mMovingRippleCount;
    private int mRDownId;
    private int mRUpId;
    private SoundPool mSoundPool;
    private ImageView[] massRipple;
    float prevMovingDistance;
    private long prevPressTime;
    Animation[] scale;
    private int soundNum;
    private int soundTime;
    private int[] sounds;
    int[] supportedCPUClockTable;
    int[] supportedGPUFreqTable;
    float touchedEventX;
    float touchedEventY;
    float[] typeStorke;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewMassRipple$1 */
    class C05051 extends Handler {
        C05051() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                KeyguardEffectViewMassRipple.this.rippeDown((float) msg.arg1, (float) msg.arg2, 1, false);
            } else if (msg.what == 1) {
                KeyguardEffectViewMassRipple.this.rippeDown((float) msg.arg1, (float) msg.arg2, 1, true);
            }
        }
    }

    class DVFSHandlerForMassRipple extends Handler {
        DVFSHandlerForMassRipple() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    KeyguardEffectViewMassRipple.this.aquireCpuGpuMaxLock();
                    break;
                case 1:
                    KeyguardEffectViewMassRipple.this.releaseCpuGpuMaxLock();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    class SoundPoolThread extends Thread {
        private int streamID;

        public SoundPoolThread(int tStreamID) {
            this.streamID = tStreamID;
        }

        public void run() {
            float leftVolume = 1.0f;
            float rightVolume = 1.0f;
            float decreaseUnit = 1.0f / ((float) KeyguardEffectViewMassRipple.this.soundNum);
            if (KeyguardEffectViewMassRipple.this.isSystemSoundChecked && KeyguardEffectViewMassRipple.this.mSoundPool != null) {
                int i = 0;
                while (i < KeyguardEffectViewMassRipple.this.soundNum) {
                    if (leftVolume <= 1.5f * decreaseUnit) {
                        leftVolume = 0.0f;
                        rightVolume = 0.0f;
                    } else {
                        leftVolume -= decreaseUnit;
                        rightVolume -= decreaseUnit;
                    }
                    if (KeyguardEffectViewMassRipple.this.mSoundPool != null) {
                        KeyguardEffectViewMassRipple.this.mSoundPool.setVolume(this.streamID, leftVolume, rightVolume);
                        SystemClock.sleep((long) KeyguardEffectViewMassRipple.this.soundTime);
                        i++;
                    } else {
                        return;
                    }
                }
            }
        }
    }

    public KeyguardEffectViewMassRipple(Context context) {
        this(context, null, false, 0);
    }

    public KeyguardEffectViewMassRipple(Context context, KeyguardWindowCallback callback) {
        this(context, callback, false, 0);
    }

    public KeyguardEffectViewMassRipple(Context context, KeyguardWindowCallback callback, boolean isProcessSeparated, int displayId) {
        super(context);
        this.DEBUG = false;
        this.indexAni = 0;
        this.COUNT_ANIMATION = 3;
        this.mDisplayId = -1;
        this.scale = new Animation[6];
        this.UNLOCK_RELEASE_THRESHOLD = 0.800000011920929d;
        this.UNLOCK_DRAG_THRESHOLD = 1.2999999523162842d;
        this.ANIMATION_DURATION = 1300.0f;
        this.firstTouch_X = -1;
        this.firstTouch_Y = -1;
        this.prevMovingDistance = 0.0f;
        this.drawRippleCount = 0;
        this.typeStorke = new float[]{49.0f, 26.6f, 37.0f, 30.0f};
        this.CIRCLE_ROUND_SIZE = 290.0f;
        this.CIRCLE_AFFORDANCE_ROUND_SIZE = 224.0f;
        this.MSG_FIRST_TOUCH = 0;
        this.MSG_AFFORDANCE_TOUCH = 1;
        this.MSG_TIME_TICK = 1;
        this.MAX_RIPPLE_COUNT = 2;
        this.mMovingRippleCount = 0;
        this.isStartUnlock = false;
        this.CPU_CLOCK_NUM = -1;
        this.cpuMaxClockBooster = null;
        this.supportedCPUClockTable = null;
        this.isRestrictCPUClock = false;
        this.GPU_FREQUNCY_NUM = -1;
        this.gpuMaxFreqBooster = null;
        this.supportedGPUFreqTable = null;
        this.isRestrictGPUFreq = false;
        this.CPU_CLOK_CONTROL = 0;
        this.GPU_FREQ_CONTROL = 1;
        this.TIME_FOR_CPU_GPU_MAX_LOCK = 35000;
        this.ACQUIRE_DVFS = 0;
        this.RELEASE_DVFS = 1;
        this.mDVFSHandlerMassRipple = null;
        this.mHandler = new C05051();
        this.mSoundPool = null;
        this.sounds = null;
        this.diffPressTime = 0;
        this.prevPressTime = 0;
        this.soundNum = 5;
        this.soundTime = 1;
        this.SOUND_ID_DOWN = 0;
        this.SOUND_ID_UP = 1;
        this.mRDownId = C0302R.raw.simple_ripple_down;
        this.mRUpId = C0302R.raw.simple_ripple_up;
        this.isSystemSoundChecked = true;
        this.mContext = context;
        this.mDisplayId = displayId;
        String productName = SystemProperties.get("ro.product.name");
        if (productName != null && productName.contains("lentislte")) {
            setValueOfDVFS(true, 1574400, true, 300000000);
        }
        LayoutInflater.from(this.mContext).inflate(C0302R.layout.keyguard_mass_ripple_effect_view, this, true);
        this.massRipple = new ImageView[6];
        setLayout();
    }

    private void setLayout() {
        this.scale[0] = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.scale_1);
        this.scale[1] = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.scale_2);
        this.scale[2] = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.scale_3);
        this.scale[3] = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.scale_4);
        this.scale[4] = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.scale_5);
        this.scale[5] = AnimationUtils.loadAnimation(this.mContext, C0302R.anim.scale_6);
        for (Animation duration : this.scale) {
            duration.setDuration(1300);
        }
        this.mCircleMain = (FrameLayout) findViewById(C0302R.id.keyguard_circle_effect_circle_main);
        this.mCircleMain.removeAllViews();
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        float insdieViewTouchedEventX = event.getRawX();
        float insdieViewTouchedEventY = event.getRawY();
        if (this.DEBUG) {
            Log.d(TAG, "insdieViewTouchedEventX = " + insdieViewTouchedEventX + "insdieViewTouchedEventY = " + insdieViewTouchedEventY);
        }
        if (event.getActionMasked() == 0) {
            Log.i(TAG, "ACTION_DOWN");
            if (this.firstTouch_X == -1 && this.firstTouch_Y == -1) {
                Log.i(TAG, "ACTION_DOWN First Touch");
                this.firstTouch_X = (int) insdieViewTouchedEventX;
                this.firstTouch_Y = (int) insdieViewTouchedEventY;
            } else {
                this.firstTouch_X = -1;
                this.firstTouch_Y = -1;
                this.prevMovingDistance = 0.0f;
                this.mDistanceRatio = 0.0d;
            }
            this.mMovingRippleCount = 0;
            if (this.mSoundPool == null) {
                this.mSoundPool = new SoundPool(10, 1, 0);
                this.sounds = new int[2];
                File checkDownFile = new File(mRDownPath);
                if (checkDownFile == null || !checkDownFile.exists()) {
                    this.sounds[0] = this.mSoundPool.load(this.mContext, this.mRDownId, 1);
                } else {
                    this.sounds[0] = this.mSoundPool.load(mRDownPath, 1);
                }
                File checkUpFile = new File(mRUpPath);
                if (checkUpFile == null || !checkUpFile.exists()) {
                    this.sounds[1] = this.mSoundPool.load(this.mContext, this.mRUpId, 1);
                } else {
                    this.sounds[1] = this.mSoundPool.load(mRUpPath, 1);
                }
            }
            this.prevPressTime = SystemClock.uptimeMillis();
            this.diffPressTime = 0;
            rippeDown(this.touchedEventX, this.touchedEventY, 0, false);
            playSound(0);
            Message message = new Message();
            message.what = 0;
            message.arg1 = (int) this.touchedEventX;
            message.arg2 = (int) this.touchedEventY;
            this.mHandler.sendMessageDelayed(message, 400);
            if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple != null) {
                this.mDVFSHandlerMassRipple.sendEmptyMessage(0);
            }
        } else if (event.getActionMasked() == 2 && event.getActionIndex() == 0) {
            if (this.DEBUG) {
                Log.i(TAG, "ACTION_MOVE");
            }
            if (moveToDistanceIs20percent(insdieViewTouchedEventX, insdieViewTouchedEventY)) {
                if (this.drawRippleCount % 2 == 0) {
                    rippeDown(insdieViewTouchedEventX, insdieViewTouchedEventY, 2, false);
                } else {
                    rippeDown(insdieViewTouchedEventX, insdieViewTouchedEventY, 3, false);
                }
                this.drawRippleCount++;
                this.mMovingRippleCount++;
                playDragSound(1);
            }
            if (this.mDistanceRatio > 1.2999999523162842d) {
                Log.i(TAG, "mDistanceRatio ove DRAG threshold " + this.mDistanceRatio);
            }
        } else if (event.getActionMasked() == 1 || event.getActionMasked() == 3) {
            Log.i(TAG, "ACTION_UP");
            this.firstTouch_X = -1;
            this.firstTouch_Y = -1;
            this.prevMovingDistance = 0.0f;
            this.diffPressTime = SystemClock.uptimeMillis() - this.prevPressTime;
            if (this.diffPressTime > 600) {
                playSound(0);
            }
            if (this.mDistanceRatio > 0.800000011920929d) {
                Log.i(TAG, "mDistanceRatio ove RELEASE threshold " + this.mDistanceRatio);
            }
        } else if (event.getActionMasked() == 9) {
            Log.d(TAG, "ACTION_HOVER_ENTER");
            if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple != null) {
                this.mDVFSHandlerMassRipple.sendEmptyMessage(0);
            }
        }
        return true;
    }

    public void rippeDown(float x, float y, int lineStroke, boolean isAffordance) {
        if (this.mMovingRippleCount <= 2) {
            if (this.massRipple[this.indexAni] != null) {
                this.massRipple[this.indexAni].clearAnimation();
                this.massRipple[this.indexAni].setVisibility(8);
                this.mCircleMain.removeView(this.massRipple[this.indexAni]);
                this.massRipple[this.indexAni] = null;
            }
            float CIRCLE_MAX_SIZE = getCircleSize(lineStroke, isAffordance);
            this.massRipple[this.indexAni] = new MassRippleImageView(this.mContext, this.typeStorke[lineStroke], (int) CIRCLE_MAX_SIZE, (int) CIRCLE_MAX_SIZE, 1300.0f);
            this.mCircleMain.addView(this.massRipple[this.indexAni], new LayoutParams(-2, -2));
            int moveX = ((int) x) - (this.massRipple[this.indexAni].getBackground().getIntrinsicWidth() / 2);
            int moveY = ((int) y) - (this.massRipple[this.indexAni].getBackground().getIntrinsicHeight() / 2);
            Log.d(TAG, "moveX X = " + moveX + "moveY = " + moveY);
            LayoutParams params = new LayoutParams(-2, -2);
            params.leftMargin = moveX;
            params.topMargin = moveY;
            params.width = this.massRipple[this.indexAni].getBackground().getIntrinsicWidth();
            params.height = this.massRipple[this.indexAni].getBackground().getIntrinsicHeight();
            this.massRipple[this.indexAni].setLayoutParams(params);
            this.massRipple[this.indexAni].startAnimation(this.scale[this.indexAni]);
            int i = this.indexAni + 1;
            this.indexAni = i;
            this.indexAni = i % 6;
            Log.i(TAG, "lineStroke = " + lineStroke);
            Log.i(TAG, "indexAni = " + this.indexAni);
        }
    }

    public float getCircleSize(int lineStorke, boolean isAffordance) {
        if (isAffordance) {
            return 224.0f;
        }
        return 290.0f - (((((float) this.mMovingRippleCount) * 290.0f) * 20.0f) / 100.0f);
    }

    public float translatedFromDPToPixel(float dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        float ret = dp * (((float) getResources().getDisplayMetrics().densityDpi) / 160.0f);
        if (this.DEBUG) {
            Log.i(TAG, "dp = " + dp + ", to Pixel = " + ret);
        }
        return ret;
    }

    public boolean moveToDistanceIs20percent(float x, float y) {
        if (this.firstTouch_X == -1 && this.firstTouch_Y == -1) {
            return false;
        }
        int min;
        int diffX = Math.abs(this.firstTouch_X - ((int) x));
        int diffY = Math.abs(this.firstTouch_Y - ((int) y));
        if (this.DEBUG) {
            Log.d(TAG, "onTouchEvent() : diffX=" + diffX + ",diffY=" + diffY);
        }
        double distance = Math.sqrt(Math.pow((double) diffX, 2.0d) + Math.pow((double) diffY, 2.0d));
        if (this.mCircleMain.getWidth() < this.mCircleMain.getHeight()) {
            min = this.mCircleMain.getWidth();
        } else {
            min = this.mCircleMain.getHeight();
        }
        double threshold = ((double) min) / 2.0d;
        this.mDistanceRatio = distance / threshold;
        Log.d(TAG, "onTouchEvent() : threshold=" + threshold + ",mDistanceRatio=" + this.mDistanceRatio);
        if (this.mDistanceRatio < 0.45d) {
            return false;
        }
        if (Math.abs(((double) this.prevMovingDistance) - this.mDistanceRatio) <= 0.45d) {
            return false;
        }
        this.prevMovingDistance = (float) this.mDistanceRatio;
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        float originalCircleX = event.getX();
        float originalCircleY = event.getY();
        if (this.DEBUG) {
            Log.i(TAG, " originalCircleX = " + originalCircleX + ", originalCircleY" + originalCircleY);
            Log.i(TAG, " touchedEventX = " + this.touchedEventX + ", touchedEventY" + this.touchedEventY);
        }
        this.touchedEventX = originalCircleX;
        this.touchedEventY = originalCircleY;
        return false;
    }

    private void clearAllViews() {
        this.mCircleMain.setVisibility(4);
        for (int i = 0; i < this.massRipple.length; i++) {
            if (this.massRipple[i] != null) {
                this.massRipple[i].clearAnimation();
                this.massRipple[i].setVisibility(8);
                this.mCircleMain.removeView(this.massRipple[i]);
                this.massRipple[i] = null;
            }
        }
    }

    public void handleUnlock(View view, MotionEvent event) {
        if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple != null) {
            this.mDVFSHandlerMassRipple.sendEmptyMessage(0);
        }
        this.firstTouch_X = -1;
        this.firstTouch_Y = -1;
        this.prevMovingDistance = 0.0f;
        this.isStartUnlock = true;
    }

    public void show() {
        this.mCircleMain.setVisibility(0);
        checkSound();
        setSound();
        if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple == null) {
            Log.d(TAG, "== DVFS == new DVFSHandlerForMassRipple");
            this.mDVFSHandlerMassRipple = new DVFSHandlerForMassRipple();
            if (this.cpuMaxClockBooster == null) {
                Log.d(TAG, "== DVFS == new cpuMaxClockBooster");
                this.cpuMaxClockBooster = new DVFSHelper(this.mContext, 13);
            }
            if (this.gpuMaxFreqBooster == null) {
                Log.d(TAG, "== DVFS == new gpuMaxFreqBooster");
                this.gpuMaxFreqBooster = new DVFSHelper(this.mContext, 17);
            }
        }
    }

    public void cleanUp() {
        clearAllViews();
        if (this.mSoundPool != null) {
            this.mSoundPool.release();
            this.mSoundPool = null;
            this.sounds = null;
        }
        if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple != null) {
            this.mDVFSHandlerMassRipple.sendEmptyMessage(1);
        }
    }

    public void reset() {
        if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple != null) {
            this.mDVFSHandlerMassRipple.sendEmptyMessage(1);
        }
        Log.d(TAG, "reset");
    }

    public void screenTurnedOn() {
        Log.d(TAG, "screenTurnedOn");
        this.mCircleMain.setVisibility(0);
        this.isStartUnlock = false;
    }

    public long getUnlockDelay() {
        return 301;
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.d(TAG, "showUnlockAffordance : " + rect.left + ", " + rect.right + ", " + rect.top + ", " + rect.bottom + ", startDelay : " + startDelay);
        this.mCircleMain.setVisibility(0);
        this.mMovingRippleCount = 0;
        this.touchedEventX = (float) (rect.left + ((rect.right - rect.left) / 2));
        this.touchedEventY = (float) (rect.top + ((rect.bottom - rect.top) / 2));
        rippeDown(this.touchedEventX, this.touchedEventY, 0, true);
        Message message = new Message();
        message.what = 1;
        message.arg1 = (int) this.touchedEventX;
        message.arg2 = (int) this.touchedEventY;
        this.mHandler.sendMessageDelayed(message, 400);
    }

    public void playLockSound() {
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
            Log.d(TAG, "show mSoundPool is null");
            this.mSoundPool = new SoundPool(10, 1, 0);
            this.sounds = new int[2];
            File checkDownFile = new File(mRDownPath);
            if (checkDownFile == null || !checkDownFile.exists()) {
                this.sounds[0] = this.mSoundPool.load(this.mContext, this.mRDownId, 1);
            } else {
                this.sounds[0] = this.mSoundPool.load(mRDownPath, 1);
            }
            File checkUpFile = new File(mRUpPath);
            if (checkUpFile == null || !checkUpFile.exists()) {
                this.sounds[1] = this.mSoundPool.load(this.mContext, this.mRUpId, 1);
            } else {
                this.sounds[1] = this.mSoundPool.load(mRUpPath, 1);
            }
        }
    }

    private void playSound(int soundId) {
        if (!this.isStartUnlock && this.isSystemSoundChecked && this.mSoundPool != null) {
            this.mSoundPool.play(this.sounds[soundId], 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void playDragSound(int soundId) {
        if (!this.isStartUnlock && this.isSystemSoundChecked && this.mSoundPool != null) {
            new SoundPoolThread(this.mSoundPool.play(this.sounds[soundId], 1.0f, 1.0f, 0, 0, 1.0f) - 1).run();
        }
    }

    public void update() {
    }

    public void screenTurnedOff() {
        clearAllViews();
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        int action = event.getActionMasked();
        Log.d(TAG, "handleTouchEventForPatternLock action = " + action);
        switch (action) {
            case 0:
                Log.d(TAG, "ACTION_DOWN => ACTION_HOVER_ENTER");
                if ((this.isRestrictCPUClock || this.isRestrictGPUFreq) && this.mDVFSHandlerMassRipple != null) {
                    this.mDVFSHandlerMassRipple.sendEmptyMessage(0);
                    break;
                }
        }
        return false;
    }

    public void setHidden(boolean isHidden) {
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    private void aquireCpuGpuMaxLock() {
        if (this.isRestrictCPUClock) {
            acquireBooster(0);
        }
        if (this.isRestrictGPUFreq) {
            acquireBooster(1);
        }
    }

    private void releaseCpuGpuMaxLock() {
        if (this.isRestrictCPUClock) {
            releaseBooster(0);
        }
        if (this.isRestrictGPUFreq) {
            releaseBooster(1);
        }
    }

    private void acquireBooster(int type) {
        if (type == 0) {
            if (this.cpuMaxClockBooster != null && this.supportedCPUClockTable == null) {
                this.supportedCPUClockTable = this.cpuMaxClockBooster.getSupportedCPUFrequency();
                if (this.supportedCPUClockTable != null) {
                    int bestCpuClock = getBestMaxFreq(this.supportedCPUClockTable, this.CPU_CLOCK_NUM);
                    Log.d(TAG, "== DVFS == acquire!!! CPU, [" + this.supportedCPUClockTable[bestCpuClock] + "]");
                    this.cpuMaxClockBooster.addExtraOption("CPU", (long) this.supportedCPUClockTable[bestCpuClock]);
                    this.cpuMaxClockBooster.acquire(this.TIME_FOR_CPU_GPU_MAX_LOCK);
                }
            }
        } else if (this.gpuMaxFreqBooster != null && this.supportedGPUFreqTable == null) {
            this.supportedGPUFreqTable = this.gpuMaxFreqBooster.getSupportedGPUFrequency();
            if (this.supportedGPUFreqTable != null) {
                int bestGpuFreq = getBestMaxFreq(this.supportedGPUFreqTable, this.GPU_FREQUNCY_NUM);
                Log.d(TAG, "== DVFS == acquire!!! GPU, [" + this.supportedGPUFreqTable[bestGpuFreq] + "]");
                this.gpuMaxFreqBooster.addExtraOption("GPU", (long) this.supportedGPUFreqTable[bestGpuFreq]);
                this.gpuMaxFreqBooster.acquire(this.TIME_FOR_CPU_GPU_MAX_LOCK);
            }
        }
    }

    public void releaseBooster(int type) {
        if (type == 0) {
            if (this.supportedCPUClockTable != null) {
                if (this.cpuMaxClockBooster != null) {
                    Log.d(TAG, "== DVFS == cpu MaxClock Booster.release()!!!");
                    this.cpuMaxClockBooster.release();
                }
                this.supportedCPUClockTable = null;
            }
        } else if (this.supportedGPUFreqTable != null) {
            if (this.gpuMaxFreqBooster != null) {
                Log.d(TAG, "== DVFS == gpu MaxFreq Booster.release()!!!");
                this.gpuMaxFreqBooster.release();
            }
            this.supportedGPUFreqTable = null;
        }
    }

    private int getBestMaxFreq(int[] pArray, int bestValue) {
        int value = 0;
        int prevdiff = Integer.MAX_VALUE;
        int arrayLenth = pArray.length;
        for (int i = 0; i < arrayLenth; i++) {
            int currdiff = Math.abs(bestValue - pArray[i]);
            if (currdiff < prevdiff) {
                value = i;
                prevdiff = currdiff;
            }
        }
        return value;
    }

    private void setValueOfDVFS(boolean cpuClock, int cpuClockIndex, boolean gpuFreq, int gpuFreqIndex) {
        this.isRestrictCPUClock = cpuClock;
        this.CPU_CLOCK_NUM = cpuClockIndex;
        Log.d(TAG, "== DVFS == isRestrictCPUClock = " + this.isRestrictCPUClock);
        Log.d(TAG, "== DVFS == CPU_CLOCK_NUM = " + this.CPU_CLOCK_NUM);
        this.isRestrictGPUFreq = gpuFreq;
        this.GPU_FREQUNCY_NUM = gpuFreqIndex;
        Log.d(TAG, "== DVFS == isRestrictGPUFreq = " + this.isRestrictGPUFreq);
        Log.d(TAG, "== DVFS == GPU_FREQUNCY_NUM = " + this.GPU_FREQUNCY_NUM);
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
