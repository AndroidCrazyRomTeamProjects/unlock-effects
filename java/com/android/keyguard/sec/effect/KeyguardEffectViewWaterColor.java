package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.android.systemui.statusbar.Feature;
import com.samsung.android.visualeffect.EffectView;
import com.samsung.android.visualeffect.IEffectListener;
import com.samsung.android.visualeffect.utils.VisualEffectDVFS;
import java.util.HashMap;

public class KeyguardEffectViewWaterColor extends EffectView implements KeyguardEffectViewBase {
    private static final String SILENCE_SOUND_PATH = "/system/media/audio/ui/ve_silence.ogg";
    private static final String TAG = "WaterColor_Keyguard";
    private static final String TAP_SOUND_PATH = "/system/media/audio/ui/ve_watercolour_tap.ogg";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_watercolour_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(Feature.mLocalSecurity, SILENCE_SOUND_PATH);
    final int SOUND_ID_TAB = 0;
    final int SOUND_ID_UNLOCK = 1;
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    private IEffectListener callBackListener;
    private int cpuMaxValue;
    private int gpuMaxValue;
    private boolean hasWindowFocus = true;
    private boolean isUnlocked = false;
    private float leftVolumeMax = 1.0f;
    private Context mContext;
    private int mDisplayId = -1;
    private EffectHandler mHandler;
    ImageView mImageView = null;
    private KeyguardEffectSound mKeyguardEffectSound;
    long mLongPressTime = 411;
    Message mMsg;
    private SoundPool mSoundPool = null;
    private Runnable releaseSoundRunnable = null;
    private float rightVolumeMax = 1.0f;
    private int[] sounds = null;
    private long touchDownTime = 0;
    private long touchMoveDiffTime = 0;
    private boolean useCPUMaxClock = false;
    private boolean useGPUMaxClock = false;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWaterColor$1 */
    class C05211 implements IEffectListener {
        C05211() {
        }

        public void onReceive(int status, HashMap<?, ?> hashMap) {
            if (status == 0 && KeyguardEffectViewWaterColor.this.mHandler != null) {
                KeyguardEffectViewWaterColor.this.mMsg = KeyguardEffectViewWaterColor.this.mHandler.obtainMessage();
                KeyguardEffectViewWaterColor.this.mMsg.what = 0;
                KeyguardEffectViewWaterColor.this.mHandler.sendMessage(KeyguardEffectViewWaterColor.this.mMsg);
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWaterColor$2 */
    class C05222 implements Runnable {
        C05222() {
        }

        public void run() {
            KeyguardEffectViewWaterColor.this.clearScreen();
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWaterColor$3 */
    class C05233 implements OnLoadCompleteListener {
        C05233() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            Log.d(KeyguardEffectViewWaterColor.TAG, "sound : onLoadComplete");
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewWaterColor$4 */
    class C05244 implements Runnable {
        C05244() {
        }

        public void run() {
            if (KeyguardEffectViewWaterColor.this.mSoundPool != null) {
                Log.d(KeyguardEffectViewWaterColor.TAG, "WaterColor sound : release SoundPool");
                KeyguardEffectViewWaterColor.this.mSoundPool.release();
                KeyguardEffectViewWaterColor.this.mSoundPool = null;
            }
            KeyguardEffectViewWaterColor.this.releaseSoundRunnable = null;
        }
    }

    public class EffectHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (KeyguardEffectViewWaterColor.this.mImageView != null) {
                        Log.d(KeyguardEffectViewWaterColor.TAG, "removeView mImageView");
                        KeyguardEffectViewWaterColor.this.mImageView.setImageBitmap(null);
                        KeyguardEffectViewWaterColor.this.removeView(KeyguardEffectViewWaterColor.this.mImageView);
                        KeyguardEffectViewWaterColor.this.mImageView = null;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public KeyguardEffectViewWaterColor(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewWaterColor(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewWaterColor(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d(TAG, "KeyguardEffectViewWaterColor Constructor");
        this.mContext = context;
        setEffect(5);
        this.sounds = new int[2];
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, TAG);
        this.mDisplayId = displayId;
        this.mImageView = new ImageView(this.mContext);
        this.mImageView.setScaleType(ScaleType.CENTER_CROP);
        addView(this.mImageView, -1, -1);
        if (this.mHandler == null) {
            Log.d(TAG, "new SoundHandler()");
            this.mHandler = new EffectHandler();
        }
        this.callBackListener = new C05211();
        setListener(this.callBackListener);
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.setLimit(this.mContext, 17, this.gpuMaxValue, 40000);
        }
        if (this.useCPUMaxClock) {
            VisualEffectDVFS.setLimit(this.mContext, 13, this.cpuMaxValue, 40000);
        }
    }

    public void show() {
        Log.i(TAG, "show");
        makeSound();
        clearScreen();
        this.isUnlocked = false;
    }

    public void reset() {
        Log.i(TAG, "reset");
        this.isUnlocked = false;
        clearScreen();
    }

    public void cleanUp() {
        Log.i(TAG, "cleanUp");
        stopReleaseSound();
        releaseSound();
        postDelayed(new C05222(), 400);
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMaxClock) {
            VisualEffectDVFS.release(13);
        }
    }

    public void update() {
        Log.i(TAG, "update");
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
        if (newBitmapDrawable == null) {
            Log.i(TAG, "newBitmapDrawable  is null");
            return;
        }
        Bitmap originBitmap = newBitmapDrawable.getBitmap();
        if (originBitmap == null) {
            Log.d(TAG, "originBitmap is null");
            return;
        }
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(originBitmap);
        }
        setBitmap(originBitmap);
    }

    private void setBitmap(Bitmap originBitmap) {
        HashMap<String, Bitmap> map = new HashMap();
        map.put("Bitmap", originBitmap);
        handleCustomEvent(0, map);
    }

    public void screenTurnedOn() {
        Log.i(TAG, "screenTurnedOn");
        this.isUnlocked = false;
        clearScreen();
        if (this.useGPUMaxClock && this.hasWindowFocus) {
            VisualEffectDVFS.lock(17);
        }
        if (this.useCPUMaxClock && this.hasWindowFocus) {
            VisualEffectDVFS.lock(13);
        }
    }

    public void screenTurnedOff() {
        Log.i(TAG, "screenTurnedOff");
        clearScreen();
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMaxClock) {
            VisualEffectDVFS.release(13);
        }
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.i(TAG, "showUnlockAffordance");
        this.isUnlocked = false;
        HashMap<Object, Object> map = new HashMap();
        map.put("StartDelay", Long.valueOf(startDelay));
        map.put("Rect", rect);
        handleCustomEvent(1, map);
    }

    public long getUnlockDelay() {
        return 250;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.i(TAG, "handleUnlock");
        handleCustomEvent(2, null);
        this.isUnlocked = true;
        playSound(1);
    }

    public void playLockSound() {
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (!this.isUnlocked) {
            int action = event.getActionMasked();
            if (action == 0) {
                Log.i(TAG, "handleTouchEvent action : " + action);
                stopReleaseSound();
                this.touchDownTime = System.currentTimeMillis();
                if (this.mSoundPool == null) {
                    Log.d(TAG, "ACTION_DOWN, mSoundPool == null");
                    makeSound();
                }
                Log.d(TAG, "SOUND PLAY SOUND_ID_TAB");
                playSound(0);
            } else if (action != 2 && (action == 1 || action == 3 || action == 4)) {
                Log.i(TAG, "handleTouchEvent action : " + action);
                this.touchMoveDiffTime = System.currentTimeMillis() - this.touchDownTime;
                if (this.touchMoveDiffTime > this.mLongPressTime) {
                    playSound(0);
                }
            }
            handleTouchEvent(event, view);
        }
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        int action = event.getActionMasked();
        MotionEvent eventForPattern = MotionEvent.obtain(event);
        if (action == 0) {
            Log.i(TAG, "handleTouchEventForPatternLock action DOWN : " + action);
            eventForPattern.setAction(9);
        } else if (action == 2) {
            eventForPattern.setAction(7);
        } else if (action == 1 || action == 3 || action == 4) {
            Log.i(TAG, "handleTouchEventForPatternLock action UP : " + action);
            eventForPattern.setAction(10);
        }
        new HashMap().put("MotionEvent", eventForPattern);
        handleTouchEvent(event, view);
        eventForPattern.recycle();
        return true;
    }

    public void setHidden(boolean isHidden) {
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(0);
            this.mHandler = null;
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        this.hasWindowFocus = hasWindowFocus;
        Log.d(TAG, "onWindowFocusChanged - " + hasWindowFocus);
        if (this.useGPUMaxClock && !hasWindowFocus) {
            VisualEffectDVFS.release(17);
        }
        if (this.useCPUMaxClock && !hasWindowFocus) {
            VisualEffectDVFS.release(13);
        }
        if (!hasWindowFocus && this.isUnlocked) {
        }
    }

    private void makeSound() {
        stopReleaseSound();
        if ((KeyguardProperties.isEffectProcessSeparated() || KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted()) && this.mSoundPool == null) {
            Log.d(TAG, "sound : new SoundPool");
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(TAP_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(UNLOCK_SOUND_PATH, 1);
            this.mSoundPool.setOnLoadCompleteListener(new C05233());
        }
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C05244();
        postDelayed(this.releaseSoundRunnable, 2000);
    }

    private void playSound(int soundId) {
        Log.d(TAG, "playSound() -  mSoundPool = " + this.mSoundPool);
        if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
            Log.d(TAG, "playSound() - soundId = " + soundId);
            this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
        }
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
        update();
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.d(TAG, "setContextualWallpaper");
        if (bmp == null) {
            Log.d(TAG, "bmp is null");
            return;
        }
        bmp = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888);
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(bmp);
        }
        setBitmap(bmp);
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
