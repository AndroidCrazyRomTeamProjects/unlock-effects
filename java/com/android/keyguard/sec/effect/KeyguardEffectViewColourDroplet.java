package com.android.keyguard.sec.effect;

import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class KeyguardEffectViewColourDroplet extends EffectView implements SensorEventListener, KeyguardEffectViewBase {
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_colourdroplet_lock.ogg";
    private static final int MSG_REGISTER_ACCELROMETER = 999;
    private static final String TAP_SOUND_PATH = "/system/media/audio/ui/ve_colourdroplet_tap.ogg";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_colourdroplet_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, UNLOCK_SOUND_PATH);
    private final boolean DBG = true;
    private final float SCREEN_ON_BACKGROUND_SCALE = 1.05f;
    final int SOUND_ID_TAB = 0;
    final int SOUND_ID_UNLOCK = 1;
    private final String TAG = "KeyguardEffectViewColourDroplet";
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    private int cpuMinValue;
    private ImageView dummyIv = null;
    private int gpuMaxValue;
    private boolean hasWindowFocus = false;
    private boolean isUnlocked = false;
    KeyguardManager keyguardManager;
    private float leftVolumeMax = 1.0f;
    private Context mContext;
    private int mDisplayId = -1;
    private EffectHandler mEffectHandler = null;
    private IEffectListener mIEffectListener;
    private KeyguardEffectSound mKeyguardEffectSound;
    private KeyguardWindowCallback mKeyguardWindowCallback;
    private ImageView mLockScreenWallpaperImage = null;
    protected Message mMsg;
    private Sensor mSensor = null;
    private SensorManager mSensorManager = null;
    private SoundPool mSoundPool = null;
    private boolean mTouchFlagForMobileKeyboard = false;
    private Runnable releaseSoundRunnable = null;
    private float rightVolumeMax = 1.0f;
    private int[] sounds = null;
    private boolean useCPUMinClock = false;
    private boolean useGPUMaxClock = false;
    private int windowHeight = 0;
    private int windowWidth = 0;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewColourDroplet$1 */
    class C04851 implements IEffectListener {
        C04851() {
        }

        public void onReceive(int status, HashMap<?, ?> hashMap) {
            switch (status) {
                case 0:
                    if (KeyguardEffectViewColourDroplet.this.mKeyguardWindowCallback != null) {
                        Log.d("KeyguardEffectViewColourDroplet", "KeyguardEffectViewColourDroplet : mKeyguardWindowCallback is called!!!");
                        KeyguardEffectViewColourDroplet.this.mKeyguardWindowCallback.onShown();
                        if (KeyguardEffectViewColourDroplet.this.mEffectHandler != null) {
                            KeyguardEffectViewColourDroplet.this.mMsg = KeyguardEffectViewColourDroplet.this.mEffectHandler.obtainMessage();
                            KeyguardEffectViewColourDroplet.this.mMsg.what = 0;
                            KeyguardEffectViewColourDroplet.this.mEffectHandler.sendMessage(KeyguardEffectViewColourDroplet.this.mMsg);
                            return;
                        }
                        return;
                    }
                    return;
                case 1:
                    KeyguardEffectViewColourDroplet.this.update(KeyguardEffectViewColourDroplet.this.setBackground(), 1);
                    KeyguardEffectViewColourDroplet.this.mTouchFlagForMobileKeyboard = false;
                    Log.d("KeyguardEffectViewColourDroplet", "mIEffectListener callback, update(1) mTouchFlagForMobileKeyboard = " + KeyguardEffectViewColourDroplet.this.mTouchFlagForMobileKeyboard);
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewColourDroplet$2 */
    class C04862 implements Runnable {
        C04862() {
        }

        public void run() {
            KeyguardEffectViewColourDroplet.this.clearScreen();
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewColourDroplet$3 */
    class C04873 implements OnLoadCompleteListener {
        C04873() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            Log.d("KeyguardEffectViewColourDroplet", "sound : onLoadComplete");
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewColourDroplet$4 */
    class C04884 implements Runnable {
        C04884() {
        }

        public void run() {
            if (KeyguardEffectViewColourDroplet.this.mSoundPool != null) {
                Log.d("KeyguardEffectViewColourDroplet", "WaterDroplet sound : release SoundPool");
                KeyguardEffectViewColourDroplet.this.mSoundPool.release();
                KeyguardEffectViewColourDroplet.this.mSoundPool = null;
            }
            KeyguardEffectViewColourDroplet.this.releaseSoundRunnable = null;
        }
    }

    public class EffectHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (KeyguardEffectViewColourDroplet.this.mLockScreenWallpaperImage != null) {
                        Log.d("KeyguardEffectViewColourDroplet", "removeView mLockScreenWallpaperImage");
                        KeyguardEffectViewColourDroplet.this.mLockScreenWallpaperImage.setImageBitmap(null);
                        KeyguardEffectViewColourDroplet.this.removeView(KeyguardEffectViewColourDroplet.this.mLockScreenWallpaperImage);
                        if (KeyguardEffectViewColourDroplet.this.dummyIv != null) {
                            KeyguardEffectViewColourDroplet.this.dummyIv.setImageBitmap(null);
                            KeyguardEffectViewColourDroplet.this.removeView(KeyguardEffectViewColourDroplet.this.dummyIv);
                            KeyguardEffectViewColourDroplet.this.dummyIv = null;
                        }
                        KeyguardEffectViewColourDroplet.this.mLockScreenWallpaperImage = null;
                        return;
                    }
                    return;
                case KeyguardEffectViewColourDroplet.MSG_REGISTER_ACCELROMETER /*999*/:
                    KeyguardEffectViewColourDroplet.this.registerAccelrometer();
                    return;
                default:
                    return;
            }
        }
    }

    public KeyguardEffectViewColourDroplet(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewColourDroplet(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewColourDroplet(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d("KeyguardEffectViewColourDroplet", "KeyguardEffectViewColourDroplet Constructor mWallpaperProcessSeparated = " + mWallpaperProcessSeparated);
        Log.d("KeyguardEffectViewColourDroplet", "KeyguardEffectViewColourDroplet displayId : " + displayId);
        this.mContext = context;
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, "KeyguardEffectViewColourDroplet");
        if (this.mEffectHandler == null) {
            Log.d("KeyguardEffectViewColourDroplet", "new EffectHandler()");
            this.mEffectHandler = new EffectHandler();
        }
        this.mKeyguardWindowCallback = callback;
        this.mDisplayId = displayId;
        this.mIEffectListener = new C04851();
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
            setEffect(16);
        } else {
            setEffect(17);
        }
        EffectDataObj data = new EffectDataObj();
        data.setEffect(16);
        data.colorDroplet.windowWidth = this.windowWidth;
        data.colorDroplet.windowHeight = this.windowHeight;
        data.colorDroplet.mIEffectListener = this.mIEffectListener;
        data.colorDroplet.resNormal = makeResBitmap(C0302R.drawable.normal_low_z_256);
        if (Math.min(this.windowWidth, this.windowHeight) >= 720) {
            data.colorDroplet.resEdgeDensity = makeResBitmap(C0302R.drawable.edge_density_720);
        } else {
            data.colorDroplet.resEdgeDensity = makeResBitmap(C0302R.drawable.edge_density_360);
        }
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
        Log.d("KeyguardEffectViewColourDroplet", "useGPUMaxClock = " + this.useGPUMaxClock + ", gpuMaxValue = " + this.gpuMaxValue);
        Log.d("KeyguardEffectViewColourDroplet", "useCPUMinClock = " + this.useCPUMinClock + ", cpuMinValue = " + this.cpuMinValue);
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.setLimit(this.mContext, 17, this.gpuMaxValue, 40000);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.setLimit(this.mContext, 12, this.cpuMinValue, 40000);
        }
        this.mSensorManager = (SensorManager) context.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(1);
        addView(this.dummyIv);
        addView(this.mLockScreenWallpaperImage);
    }

    public void show() {
        Log.d("KeyguardEffectViewColourDroplet", "show");
        reInit(null);
        clearScreen();
        this.isUnlocked = false;
        makeSound();
    }

    public void reset() {
        Log.d("KeyguardEffectViewColourDroplet", "reset");
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
        Log.d("KeyguardEffectViewColourDroplet", "cleanUp");
        stopReleaseSound();
        releaseSound();
        postDelayed(new C04862(), 0);
        this.isUnlocked = false;
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.release(12);
        }
        unregisterAccelrometer();
    }

    public void update() {
        Log.d("KeyguardEffectViewColourDroplet", "update(0)");
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

    private void registerAccelrometer() {
        Log.d("KeyguardEffectViewColourDroplet", "registerAccelrometer()");
        if (this.mSensorManager != null && this.mSensor != null) {
            this.mSensorManager.registerListener(this, this.mSensor, 2);
        }
    }

    private void unregisterAccelrometer() {
        Log.d("KeyguardEffectViewColourDroplet", "unregisterAccelrometer()");
        this.mEffectHandler.removeMessages(MSG_REGISTER_ACCELROMETER);
        if (this.mSensorManager != null) {
            this.mSensorManager.unregisterListener(this);
        }
    }

    public void screenTurnedOn() {
        Log.d("KeyguardEffectViewColourDroplet", "screenTurnedOn");
        handleCustomEvent(4, null);
        if (this.useGPUMaxClock && this.hasWindowFocus) {
            VisualEffectDVFS.lock(17);
        }
        if (this.useCPUMinClock && this.hasWindowFocus) {
            VisualEffectDVFS.lock(12);
        }
        Message msg = this.mEffectHandler.obtainMessage();
        msg.what = MSG_REGISTER_ACCELROMETER;
        this.mEffectHandler.sendMessageDelayed(msg, 10);
    }

    public void screenTurnedOff() {
        Log.d("KeyguardEffectViewColourDroplet", "screenTurnedOff");
        handleCustomEvent(3, null);
        this.isUnlocked = false;
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMinClock) {
            VisualEffectDVFS.release(12);
        }
        unregisterAccelrometer();
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.i("KeyguardEffectViewColourDroplet", "showUnlockAffordance");
        HashMap<Object, Object> map = new HashMap();
        map.put("StartDelay", Long.valueOf(startDelay));
        map.put("Rect", rect);
        handleCustomEvent(1, map);
    }

    public long getUnlockDelay() {
        return 400;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.i("KeyguardEffectViewColourDroplet", "handleUnlock");
        handleCustomEvent(2, null);
        this.isUnlocked = true;
        unregisterAccelrometer();
    }

    public void playLockSound() {
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (this.isUnlocked || this.mTouchFlagForMobileKeyboard) {
            Log.i("KeyguardEffectViewColourDroplet", "handleTouchEvent return : isUnlocked = " + this.isUnlocked + ", mTouchFlag" + this.mTouchFlagForMobileKeyboard);
        } else {
            int action = event.getActionMasked();
            if (action == 0) {
                Log.i("KeyguardEffectViewColourDroplet", "ACTION_DOWN, mTouchFlag" + this.mTouchFlagForMobileKeyboard);
                stopReleaseSound();
                if (this.mSoundPool == null) {
                    Log.d("KeyguardEffectViewColourDroplet", "ACTION_DOWN, mSoundPool == null");
                    makeSound();
                }
                Log.d("KeyguardEffectViewColourDroplet", "SOUND PLAY SOUND_ID_TAB");
                playSound(0);
            } else if (!(action == 2 || action == 1 || action == 3 || action != 4)) {
            }
            handleTouchEvent(event, view);
        }
        return true;
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return true;
    }

    public void setHidden(boolean isHidden) {
        Log.d("KeyguardEffectViewColourDroplet", "setHidden() : " + isHidden);
        if (!isHidden) {
            Log.d("KeyguardEffectViewColourDroplet", "setHidden() - call screenTurnedOn() cause by SHOW_WHEN_LOCKED");
            screenTurnedOn();
        }
    }

    private Bitmap setBackground() {
        Log.d("KeyguardEffectViewColourDroplet", "setBackground");
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
        if (newBitmapDrawable == null) {
            Log.i("KeyguardEffectViewColourDroplet", "newBitmapDrawable  is null");
            return null;
        }
        Bitmap pBitmap = newBitmapDrawable.getBitmap();
        if (pBitmap == null) {
            Log.i("KeyguardEffectViewColourDroplet", "pBitmap  is null");
            return pBitmap;
        }
        Log.d("KeyguardEffectViewColourDroplet", "pBitmap.width = " + pBitmap.getWidth() + ", pBitmap.height = " + pBitmap.getHeight());
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

    private void makeSound() {
        stopReleaseSound();
        if ((KeyguardProperties.isEffectProcessSeparated() || KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted()) && this.mSoundPool == null) {
            Log.d("KeyguardEffectViewColourDroplet", "sound : new SoundPool");
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(TAP_SOUND_PATH, 1);
            this.mSoundPool.setOnLoadCompleteListener(new C04873());
        }
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C04884();
        postDelayed(this.releaseSoundRunnable, 2000);
    }

    private void playSound(int soundId) {
        Log.d("KeyguardEffectViewColourDroplet", "playSound() -  mSoundPool = " + this.mSoundPool);
        if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
            Log.d("KeyguardEffectViewColourDroplet", "playSound() - soundId = " + soundId);
            this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("KeyguardEffectViewColourDroplet", "onWindowFocusChanged : hasWindowFocus - " + hasWindowFocus);
        if (!hasWindowFocus) {
            if (this.useGPUMaxClock && !hasWindowFocus) {
                VisualEffectDVFS.release(17);
            }
            if (this.useCPUMinClock && !hasWindowFocus) {
                VisualEffectDVFS.release(12);
            }
            unregisterAccelrometer();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("KeyguardEffectViewColourDroplet", "onDetachedFromWindow");
        unregisterAccelrometer();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("KeyguardEffectViewColourDroplet", "onAttachedToWindow");
    }

    public void onSensorChanged(SensorEvent event) {
        HashMap<Object, Object> map = new HashMap();
        map.put("CustomEvent", "SensorEvent");
        map.put("EventObject", event);
        handleCustomEvent(99, map);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
        update();
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.d("KeyguardEffectViewColourDroplet", "setContextualWallpaper");
        if (bmp == null) {
            Log.d("KeyguardEffectViewColourDroplet", "bmp is null" + bmp);
            return;
        }
        bmp = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888);
        if (this.mLockScreenWallpaperImage != null) {
            this.mLockScreenWallpaperImage.setImageBitmap(bmp);
        }
        Log.d("KeyguardEffectViewColourDroplet", "changeBackground()");
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
        Log.i("KeyguardEffectViewColourDroplet", "onSizeChanged, width = " + width + ", height = " + height + ", oldw = " + oldw + ", oldh =" + oldh);
    }
}
