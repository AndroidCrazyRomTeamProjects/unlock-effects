package com.android.keyguard.sec.effect;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.keyguard.C0302R;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import com.samsung.android.visualeffect.IEffectListener;
import com.samsung.android.visualeffect.utils.VisualEffectDVFS;
import java.io.InputStream;
import java.util.HashMap;

public class KeyguardEffectViewSparklingBubbles extends EffectView implements KeyguardEffectViewBase {
    private static final String DRAG_SOUND_PATH = "/system/media/audio/ui/ve_sparklingbubbles_drag.ogg";
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_sparklingbubbles_lock.ogg";
    private static final String TAP_SOUND_PATH = "/system/media/audio/ui/ve_sparklingbubbles_tap.ogg";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_sparklingbubbles_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, UNLOCK_SOUND_PATH);
    private final boolean DBG;
    private final float SCREEN_ON_BACKGROUND_SCALE;
    final int SOUND_ID_DRAG;
    final int SOUND_ID_TAB;
    private final String TAG;
    private final long UNLOCK_SOUND_PLAY_TIME;
    private int cpuMinValue;
    private float dragSoudMinusOffset;
    private float dragSoudVolume;
    private int dragStreamID;
    private ImageView dummyIv;
    private int gpuMaxValue;
    private boolean hasWindowFocus;
    private boolean isEmptyRender;
    private boolean isFadeOutSound;
    private boolean isUnlocked;
    KeyguardManager keyguardManager;
    private float leftVolumeMax;
    private Context mContext;
    private int mDisplayId;
    private EffectHandler mEffectHandler;
    private IEffectListener mIEffectListener;
    private KeyguardEffectSound mKeyguardEffectSound;
    private KeyguardWindowCallback mKeyguardWindowCallback;
    private ImageView mLockScreenWallpaperImage;
    long mLongPressTime;
    protected Message mMsg;
    private float mPreTouchX;
    private float mPreTouchY;
    private SoundPool mSoundPool;
    private boolean mTouchFlagForMobileKeyboard;
    private String mWallpaperPath;
    private int prevOrientation;
    private Runnable releaseSoundRunnable;
    private float rightVolumeMax;
    private int[] sounds;
    private long touchDownTime;
    private long touchMoveDiffTime;
    private boolean useCPUMinClock;
    private boolean useGPUMaxClock;
    private int windowHeight;
    private int windowWidth;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewSparklingBubbles$1 */
    class C05131 implements IEffectListener {
        C05131() {
        }

        public void onReceive(int status, HashMap<?, ?> hashMap) {
            switch (status) {
                case 0:
                    if (KeyguardEffectViewSparklingBubbles.this.mKeyguardWindowCallback != null) {
                        Log.d("SparklingBubbles_Keyguard", "KeyguardEffectViewSparklingBubbles : mKeyguardWindowCallback is called!!!");
                        KeyguardEffectViewSparklingBubbles.this.mKeyguardWindowCallback.onShown();
                        if (KeyguardEffectViewSparklingBubbles.this.mEffectHandler != null) {
                            KeyguardEffectViewSparklingBubbles.this.mMsg = KeyguardEffectViewSparklingBubbles.this.mEffectHandler.obtainMessage();
                            KeyguardEffectViewSparklingBubbles.this.mMsg.what = 0;
                            KeyguardEffectViewSparklingBubbles.this.mEffectHandler.sendMessage(KeyguardEffectViewSparklingBubbles.this.mMsg);
                            return;
                        }
                        return;
                    }
                    return;
                case 1:
                    KeyguardEffectViewSparklingBubbles.this.update(KeyguardEffectViewSparklingBubbles.this.setBackground(), 1);
                    KeyguardEffectViewSparklingBubbles.this.mTouchFlagForMobileKeyboard = false;
                    Log.d("SparklingBubbles_Keyguard", "mIEffectListener callback, update(1) mTouchFlagForMobileKeyboard = " + KeyguardEffectViewSparklingBubbles.this.mTouchFlagForMobileKeyboard);
                    return;
                case 2:
                    KeyguardEffectViewSparklingBubbles.this.isEmptyRender = true;
                    return;
                case 3:
                    KeyguardEffectViewSparklingBubbles.this.isEmptyRender = false;
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewSparklingBubbles$2 */
    class C05142 implements Runnable {
        C05142() {
        }

        public void run() {
            KeyguardEffectViewSparklingBubbles.this.clearScreen();
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewSparklingBubbles$3 */
    class C05153 implements OnLoadCompleteListener {
        C05153() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            Log.d("SparklingBubbles_Keyguard", "sound : onLoadComplete");
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewSparklingBubbles$4 */
    class C05164 implements Runnable {
        C05164() {
        }

        public void run() {
            if (KeyguardEffectViewSparklingBubbles.this.mSoundPool != null) {
                Log.d("SparklingBubbles_Keyguard", "Sparklingbubbles sound : release SoundPool");
                KeyguardEffectViewSparklingBubbles.this.mSoundPool.release();
                KeyguardEffectViewSparklingBubbles.this.mSoundPool = null;
            }
            KeyguardEffectViewSparklingBubbles.this.releaseSoundRunnable = null;
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewSparklingBubbles$5 */
    class C05175 implements Runnable {
        C05175() {
        }

        public void run() {
            KeyguardEffectViewSparklingBubbles.this.fadeOutSound();
        }
    }

    public class EffectHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (KeyguardEffectViewSparklingBubbles.this.mLockScreenWallpaperImage != null) {
                        Log.d("SparklingBubbles_Keyguard", "removeView mLockScreenWallpaperImage");
                        KeyguardEffectViewSparklingBubbles.this.mLockScreenWallpaperImage.setImageBitmap(null);
                        KeyguardEffectViewSparklingBubbles.this.removeView(KeyguardEffectViewSparklingBubbles.this.mLockScreenWallpaperImage);
                        if (KeyguardEffectViewSparklingBubbles.this.dummyIv != null) {
                            KeyguardEffectViewSparklingBubbles.this.dummyIv.setImageBitmap(null);
                            KeyguardEffectViewSparklingBubbles.this.removeView(KeyguardEffectViewSparklingBubbles.this.dummyIv);
                            KeyguardEffectViewSparklingBubbles.this.dummyIv = null;
                        }
                        KeyguardEffectViewSparklingBubbles.this.mLockScreenWallpaperImage = null;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public KeyguardEffectViewSparklingBubbles(Context context) {
        this(context, null);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewSparklingBubbles(Context context, KeyguardWindowCallback callback) {
        super(context);
        this.TAG = "SparklingBubbles_Keyguard";
        this.mWallpaperPath = null;
        this.mSoundPool = null;
        this.sounds = null;
        this.releaseSoundRunnable = null;
        this.UNLOCK_SOUND_PLAY_TIME = 2000;
        this.touchDownTime = 0;
        this.touchMoveDiffTime = 0;
        this.leftVolumeMax = 1.0f;
        this.rightVolumeMax = 1.0f;
        this.SOUND_ID_TAB = 0;
        this.SOUND_ID_DRAG = 1;
        this.dragStreamID = 0;
        this.mLongPressTime = 1100;
        this.dragSoudVolume = 1.0f;
        this.dragSoudMinusOffset = 0.04f;
        this.isFadeOutSound = false;
        this.DBG = true;
        this.isUnlocked = false;
        this.prevOrientation = -1;
        this.windowWidth = 0;
        this.windowHeight = 0;
        this.useGPUMaxClock = false;
        this.useCPUMinClock = false;
        this.hasWindowFocus = false;
        this.mTouchFlagForMobileKeyboard = false;
        this.isEmptyRender = true;
        this.mDisplayId = -1;
        this.SCREEN_ON_BACKGROUND_SCALE = 1.05f;
        this.mLockScreenWallpaperImage = null;
        this.dummyIv = null;
        this.mEffectHandler = null;
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewSparklingBubbles(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        this.TAG = "SparklingBubbles_Keyguard";
        this.mWallpaperPath = null;
        this.mSoundPool = null;
        this.sounds = null;
        this.releaseSoundRunnable = null;
        this.UNLOCK_SOUND_PLAY_TIME = 2000;
        this.touchDownTime = 0;
        this.touchMoveDiffTime = 0;
        this.leftVolumeMax = 1.0f;
        this.rightVolumeMax = 1.0f;
        this.SOUND_ID_TAB = 0;
        this.SOUND_ID_DRAG = 1;
        this.dragStreamID = 0;
        this.mLongPressTime = 1100;
        this.dragSoudVolume = 1.0f;
        this.dragSoudMinusOffset = 0.04f;
        this.isFadeOutSound = false;
        this.DBG = true;
        this.isUnlocked = false;
        this.prevOrientation = -1;
        this.windowWidth = 0;
        this.windowHeight = 0;
        this.useGPUMaxClock = false;
        this.useCPUMinClock = false;
        this.hasWindowFocus = false;
        this.mTouchFlagForMobileKeyboard = false;
        this.isEmptyRender = true;
        this.mDisplayId = -1;
        this.SCREEN_ON_BACKGROUND_SCALE = 1.05f;
        this.mLockScreenWallpaperImage = null;
        this.dummyIv = null;
        this.mEffectHandler = null;
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d("SparklingBubbles_Keyguard", "KeyguardEffectViewSparklingBubbles Constructor mWallpaperProcessSeparated = " + mWallpaperProcessSeparated);
        Log.d("SparklingBubbles_Keyguard", "KeyguardEffectViewSparklingBubbles displayId : " + displayId);
        this.mContext = context;
        if (this.mEffectHandler == null) {
            Log.d("SparklingBubbles_Keyguard", "new EffectHandler()");
            this.mEffectHandler = new EffectHandler();
        }
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, "SparklingBubbles_Keyguard");
        this.mKeyguardWindowCallback = callback;
        this.mDisplayId = displayId;
        this.mIEffectListener = new C05131();
        this.dummyIv = new ImageView(this.mContext);
        this.dummyIv.setScaleType(ScaleType.MATRIX);
        this.mLockScreenWallpaperImage = new ImageView(this.mContext);
        this.mLockScreenWallpaperImage.setScaleType(ScaleType.CENTER_CROP);
        this.mLockScreenWallpaperImage.setDrawingCacheEnabled(true);
        this.mLockScreenWallpaperImage.setScaleX(1.05f);
        this.mLockScreenWallpaperImage.setScaleY(1.05f);
        this.keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.windowWidth = displayMetrics.widthPixels;
        this.windowHeight = displayMetrics.heightPixels;
        if (mWallpaperProcessSeparated) {
            setEffect(14);
        } else {
            setEffect(15);
        }
        EffectDataObj data = new EffectDataObj();
        data.setEffect(14);
        data.sparklingBubblesData.windowWidth = this.windowWidth;
        data.sparklingBubblesData.windowHeight = this.windowHeight;
        data.sparklingBubblesData.mIEffectListener = this.mIEffectListener;
        data.sparklingBubblesData.resBmp = makeResBitmap(C0302R.drawable.blur_mask);
        init(data);
        this.sounds = new int[2];
        this.useGPUMaxClock = false;
        if (this.useGPUMaxClock) {
            this.gpuMaxValue = Integer.parseInt("389000000");
        }
        this.useCPUMinClock = false;
        if (this.useCPUMinClock) {
            this.cpuMinValue = Integer.parseInt("1574400");
        }
        Log.d("SparklingBubbles_Keyguard", "useGPUMaxClock = " + this.useGPUMaxClock + ", gpuMaxValue = " + this.gpuMaxValue);
        Log.d("SparklingBubbles_Keyguard", "useCPUMinClock = " + this.useCPUMinClock + ", cpuMinValue = " + this.cpuMinValue);
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.setLimit(this.mContext, 17, this.gpuMaxValue, 40000);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.setLimit(this.mContext, 12, this.cpuMinValue, 40000);
        }
        addView(this.dummyIv);
        addView(this.mLockScreenWallpaperImage);
    }

    public void show() {
        Log.d("SparklingBubbles_Keyguard", "show");
        reInit(null);
        clearScreen();
        this.isUnlocked = false;
        makeSound();
    }

    public void reset() {
        Log.d("SparklingBubbles_Keyguard", "reset");
        clearScreen();
        this.isUnlocked = false;
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.release(12);
        }
    }

    public void cleanUp() {
        Log.d("SparklingBubbles_Keyguard", "cleanUp");
        stopReleaseSound();
        releaseSound();
        postDelayed(new C05142(), 0);
        this.isUnlocked = false;
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.release(12);
        }
    }

    public void update() {
        Log.d("SparklingBubbles_Keyguard", "update(0)");
        update(setBackground(), 0);
    }

    private void update(Bitmap bmp, int mode) {
        if (mode == 0 && this.mLockScreenWallpaperImage != null) {
            this.mLockScreenWallpaperImage.setImageBitmap(bmp);
        }
        HashMap<Object, Object> map = new HashMap();
        map.put("Bitmap", bmp);
        map.put("Mode", Integer.valueOf(mode));
        handleCustomEvent(0, map);
    }

    public void screenTurnedOn() {
        Log.d("SparklingBubbles_Keyguard", "screenTurnedOn");
        handleCustomEvent(4, null);
        if (this.useGPUMaxClock && this.hasWindowFocus) {
            VisualEffectDVFS.lock(17);
        }
        if (this.useCPUMinClock && this.hasWindowFocus) {
            VisualEffectDVFS.lock(12);
        }
    }

    public void screenTurnedOff() {
        Log.d("SparklingBubbles_Keyguard", "screenTurnedOff");
        handleCustomEvent(3, null);
        this.isUnlocked = false;
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.release(12);
        }
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.i("SparklingBubbles_Keyguard", "showUnlockAffordance");
        HashMap<Object, Object> map = new HashMap();
        map.put("StartDelay", Long.valueOf(startDelay));
        map.put("Rect", rect);
        handleCustomEvent(1, map);
    }

    public long getUnlockDelay() {
        return 400;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.i("SparklingBubbles_Keyguard", "handleUnlock");
        handleCustomEvent(2, null);
        this.isUnlocked = true;
        this.dragSoudMinusOffset = 0.059f;
        this.isFadeOutSound = true;
        if (this.dragSoudVolume == 1.0f) {
            fadeOutSound();
        }
    }

    public void playLockSound() {
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (this.isUnlocked || this.mTouchFlagForMobileKeyboard) {
            Log.i("SparklingBubbles_Keyguard", "handleTouchEvent return : isUnlocked = " + this.isUnlocked + ", mTouchFlag" + this.mTouchFlagForMobileKeyboard);
        } else {
            int action = event.getActionMasked();
            handleTouchEvent(event, view);
            if (action == 0) {
                Log.i("SparklingBubbles_Keyguard", "ACTION_DOWN, mTouchFlag" + this.mTouchFlagForMobileKeyboard);
                this.dragSoudVolume = 1.0f;
                this.isFadeOutSound = false;
                stopReleaseSound();
                this.touchDownTime = System.currentTimeMillis();
                if (this.mSoundPool == null) {
                    Log.d("SparklingBubbles_Keyguard", "ACTION_DOWN, mSoundPool == null");
                    makeSound();
                }
                Log.d("SparklingBubbles_Keyguard", "SOUND PLAY SOUND_ID_TAB");
                playSound(0);
                if (this.dragStreamID != 0) {
                    if (this.mSoundPool != null) {
                        this.mSoundPool.stop(this.dragStreamID);
                    }
                    this.dragStreamID = 0;
                }
                this.mPreTouchX = event.getX();
                this.mPreTouchY = event.getY();
            } else if (action == 2) {
                if (this.dragStreamID == 0) {
                    this.dragSoudVolume = 1.0f;
                    this.isFadeOutSound = false;
                    this.touchMoveDiffTime = System.currentTimeMillis() - this.touchDownTime;
                    if (this.touchMoveDiffTime > this.mLongPressTime && this.touchDownTime != 0 && Math.sqrt(Math.pow((double) (this.mPreTouchX - event.getX()), 2.0d) + Math.pow((double) (this.mPreTouchY - event.getY()), 2.0d)) >= 120.0d) {
                        Log.d("SparklingBubbles_Keyguard", "SOUND PLAY SOUND_ID_DRAG touchMoveDiff = " + this.touchMoveDiffTime);
                        playSound(1);
                        this.mPreTouchX = event.getX();
                        this.mPreTouchY = event.getY();
                    }
                } else if (this.isEmptyRender) {
                    if (this.isFadeOutSound) {
                        this.isFadeOutSound = false;
                        this.dragSoudVolume = 1.0f;
                        if (this.dragStreamID != 0) {
                            if (this.mSoundPool != null) {
                                this.mSoundPool.stop(this.dragStreamID);
                            }
                            this.dragStreamID = 0;
                        }
                    }
                } else if (this.dragStreamID != 0) {
                    this.dragSoudMinusOffset = 0.039f;
                    this.isFadeOutSound = true;
                    if (this.dragSoudVolume == 1.0f) {
                        fadeOutSound();
                    }
                }
            } else if (action == 1 || action == 3 || action == 4) {
                Log.i("SparklingBubbles_Keyguard", "handleTouchEvent action : " + action);
                if (this.dragStreamID != 0) {
                    this.dragSoudMinusOffset = 0.039f;
                    this.isFadeOutSound = true;
                    if (this.dragSoudVolume == 1.0f) {
                        fadeOutSound();
                    }
                }
            }
        }
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return true;
    }

    public void setHidden(boolean isHidden) {
        Log.d("SparklingBubbles_Keyguard", "setHidden() : " + isHidden);
        if (!isHidden) {
            Log.d("SparklingBubbles_Keyguard", "setHidden() - call screenTurnedOn() cause by SHOW_WHEN_LOCKED");
            screenTurnedOn();
        }
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return true;
    }

    private Bitmap setBackground() {
        Log.d("SparklingBubbles_Keyguard", "setBackground");
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
        if (newBitmapDrawable == null) {
            Log.i("SparklingBubbles_Keyguard", "newBitmapDrawable  is null");
            return null;
        }
        Bitmap pBitmap = newBitmapDrawable.getBitmap();
        if (pBitmap == null) {
            Log.i("SparklingBubbles_Keyguard", "pBitmap  is null");
            return pBitmap;
        }
        Log.d("SparklingBubbles_Keyguard", "pBitmap.width = " + pBitmap.getWidth() + ", pBitmap.height = " + pBitmap.getHeight());
        return pBitmap;
    }

    private Bitmap makeResBitmap(int res) {
        Bitmap result = null;
        try {
            InputStream is = this.mContext.getResources().openRawResource(res);
            result = BitmapFactory.decodeStream(is);
            is.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("SparklingBubbles_Keyguard", "onWindowFocusChanged : hasWindowFocus - " + hasWindowFocus);
        if (!hasWindowFocus) {
            if (this.useGPUMaxClock && !hasWindowFocus) {
                VisualEffectDVFS.release(17);
            }
            if (this.useCPUMinClock && !hasWindowFocus) {
                VisualEffectDVFS.release(12);
            }
            if (!hasWindowFocus && !this.isUnlocked && this.dragStreamID != 0 && !this.isUnlocked) {
                this.dragSoudMinusOffset = 0.039f;
                this.isFadeOutSound = true;
                if (this.dragSoudVolume == 1.0f) {
                    fadeOutSound();
                }
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("SparklingBubbles_Keyguard", "onDetachedFromWindow");
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("SparklingBubbles_Keyguard", "onAttachedToWindow");
    }

    private void makeSound() {
        stopReleaseSound();
        if ((KeyguardProperties.isEffectProcessSeparated() || KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted()) && this.mSoundPool == null) {
            Log.d("SparklingBubbles_Keyguard", "sound : new SoundPool");
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(TAP_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(DRAG_SOUND_PATH, 1);
            this.mSoundPool.setOnLoadCompleteListener(new C05153());
        }
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C05164();
        postDelayed(this.releaseSoundRunnable, 2000);
    }

    private void playSound(int soundId) {
        Log.d("SparklingBubbles_Keyguard", "playSound() -  mSoundPool = " + this.mSoundPool);
        if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
            Log.d("SparklingBubbles_Keyguard", "playSound() - soundId = " + soundId);
            if (soundId == 1) {
                this.dragStreamID = this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, -1, 1.0f);
            } else {
                this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
            }
        }
    }

    private void fadeOutSound() {
        if (this.isFadeOutSound && this.mSoundPool != null) {
            if (this.dragSoudVolume < 0.0f) {
                this.dragSoudVolume = 0.0f;
            }
            this.mSoundPool.setVolume(this.dragStreamID, this.dragSoudVolume, this.dragSoudVolume);
            if (this.dragSoudVolume > 0.0f) {
                this.dragSoudVolume -= this.dragSoudMinusOffset;
                postDelayed(new C05175(), 10);
                return;
            }
            Log.d("SparklingBubbles_Keyguard", "SOUND STOP because UP or Unlock");
            stopReleaseSound();
        }
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
        update();
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.d("SparklingBubbles_Keyguard", "setContextualWallpaper");
        if (bmp == null) {
            Log.d("SparklingBubbles_Keyguard", "bmp is null" + bmp);
            return;
        }
        Log.d("SparklingBubbles_Keyguard", "changeBackground()");
        bmp = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888);
        if (this.mLockScreenWallpaperImage != null) {
            this.mLockScreenWallpaperImage.setImageBitmap(bmp);
        }
        update(bmp, 0);
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }

    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        Log.i("SparklingBubbles_Keyguard", "onSizeChanged, width = " + width + ", height = " + height + ", oldw = " + oldw + ", oldh =" + oldh);
    }
}
