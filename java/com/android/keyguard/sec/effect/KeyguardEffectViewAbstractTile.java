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
import com.samsung.android.visualeffect.EffectView;
import com.samsung.android.visualeffect.IEffectListener;
import com.samsung.android.visualeffect.utils.VisualEffectDVFS;
import java.util.HashMap;

public class KeyguardEffectViewAbstractTile extends EffectView implements KeyguardEffectViewBase {
    private static final String DRAG_SOUND_PATH = "/system/media/audio/ui/ve_abstracttiles_drag.ogg";
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_abstracttiles_lock.ogg";
    private static final String SILENCE_SOUND_PATH = "/system/media/audio/ui/ve_silence.ogg";
    private static final String TAB_SOUND_PATH = "/system/media/audio/ui/ve_abstracttiles_tap.ogg";
    private static final String TAG = "AbstractTile Keyguard";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_abstracttiles_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, SILENCE_SOUND_PATH);
    final int SOUND_ID_DRAG = 1;
    final int SOUND_ID_TAB = 0;
    final int SOUND_ID_UNLOCK = 2;
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    private IEffectListener callBackListener;
    private int cpuMaxValue;
    private float dragSoudMinusOffset = 0.04f;
    private float dragSoudVolume = 1.0f;
    private int dragStreamID = 0;
    private int gpuMaxValue;
    private boolean hasWindowFocus = true;
    private boolean isFadeOutSound = false;
    private boolean isUnlocked = false;
    private float leftVolumeMax = 1.0f;
    private Context mContext;
    private int mDisplayId = -1;
    private EffectHandler mHandler;
    private ImageView mImageView;
    private KeyguardEffectSound mKeyguardEffectSound;
    private KeyguardWindowCallback mKeyguardWindowCallback;
    long mLongPressTime = 411;
    protected Message mMsg;
    private SoundPool mSoundPool = null;
    private Runnable releaseSoundRunnable = null;
    private float rightVolumeMax = 1.0f;
    private int[] sounds = null;
    private long touchDownTime = 0;
    private long touchMoveDiffTime = 0;
    private boolean useCPUMaxClock = false;
    private boolean useGPUMaxClock = false;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewAbstractTile$1 */
    class C04691 implements IEffectListener {
        C04691() {
        }

        public void onReceive(int status, HashMap<?, ?> hashMap) {
            if (status == 0 && KeyguardEffectViewAbstractTile.this.mHandler != null) {
                KeyguardEffectViewAbstractTile.this.mMsg = KeyguardEffectViewAbstractTile.this.mHandler.obtainMessage();
                KeyguardEffectViewAbstractTile.this.mMsg.what = 0;
                KeyguardEffectViewAbstractTile.this.mHandler.sendMessage(KeyguardEffectViewAbstractTile.this.mMsg);
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewAbstractTile$2 */
    class C04702 implements Runnable {
        C04702() {
        }

        public void run() {
            KeyguardEffectViewAbstractTile.this.clearScreen();
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewAbstractTile$3 */
    class C04713 implements OnLoadCompleteListener {
        C04713() {
        }

        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
            Log.d(KeyguardEffectViewAbstractTile.TAG, "sound : onLoadComplete");
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewAbstractTile$4 */
    class C04724 implements Runnable {
        C04724() {
        }

        public void run() {
            if (KeyguardEffectViewAbstractTile.this.mSoundPool != null) {
                Log.d(KeyguardEffectViewAbstractTile.TAG, "BrilliantRing sound : release SoundPool");
                KeyguardEffectViewAbstractTile.this.mSoundPool.release();
                KeyguardEffectViewAbstractTile.this.mSoundPool = null;
            }
            KeyguardEffectViewAbstractTile.this.releaseSoundRunnable = null;
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewAbstractTile$5 */
    class C04735 implements Runnable {
        C04735() {
        }

        public void run() {
            KeyguardEffectViewAbstractTile.this.fadeOutSound();
        }
    }

    public class EffectHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (KeyguardEffectViewAbstractTile.this.mImageView != null) {
                        Log.d(KeyguardEffectViewAbstractTile.TAG, "removeView mImageView");
                        KeyguardEffectViewAbstractTile.this.mImageView.setImageBitmap(null);
                        KeyguardEffectViewAbstractTile.this.removeView(KeyguardEffectViewAbstractTile.this.mImageView);
                        KeyguardEffectViewAbstractTile.this.mImageView = null;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public KeyguardEffectViewAbstractTile(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewAbstractTile(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewAbstractTile(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        this.mContext = context;
        Log.d(TAG, "KeyguardEffectViewAbstractTile Constructor");
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, TAG);
        this.mKeyguardWindowCallback = callback;
        this.mDisplayId = displayId;
        setEffect(0);
        this.sounds = new int[3];
        if (this.useGPUMaxClock) {
            VisualEffectDVFS.setLimit(this.mContext, 17, this.gpuMaxValue, 40000);
        }
        if (this.useCPUMaxClock) {
            VisualEffectDVFS.setLimit(this.mContext, 13, this.cpuMaxValue, 40000);
        }
        this.mImageView = new ImageView(this.mContext);
        this.mImageView.setScaleType(ScaleType.CENTER_CROP);
        addView(this.mImageView, -1, -1);
        if (this.mHandler == null) {
            Log.d(TAG, "new SoundHandler()");
            this.mHandler = new EffectHandler();
        }
        this.callBackListener = new C04691();
        setListener(this.callBackListener);
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
        postDelayed(new C04702(), 400);
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
        if (originBitmap != null) {
            setBitmap(originBitmap);
            if (this.mImageView != null) {
                this.mImageView.setImageBitmap(originBitmap);
            }
        }
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
        this.dragSoudMinusOffset = 0.059f;
        playSound(2);
        this.isFadeOutSound = true;
        if (this.dragSoudVolume == 1.0f) {
            fadeOutSound();
        }
    }

    public void playLockSound() {
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (this.isUnlocked) {
            Log.i(TAG, "handleTouchEvent isUnlocked : " + this.isUnlocked);
        } else {
            int action = event.getActionMasked();
            if (action == 0) {
                Log.i(TAG, "handleTouchEvent action : " + action);
                this.dragSoudVolume = 1.0f;
                this.isFadeOutSound = false;
                stopReleaseSound();
                this.touchDownTime = System.currentTimeMillis();
                if (this.mSoundPool == null) {
                    Log.d(TAG, "ACTION_DOWN, mSoundPool == null");
                    makeSound();
                }
                Log.d(TAG, "SOUND PLAY SOUND_ID_TAB");
                playSound(0);
                if (this.dragStreamID != 0) {
                    if (this.mSoundPool != null) {
                        this.mSoundPool.stop(this.dragStreamID);
                    }
                    this.dragStreamID = 0;
                }
            } else if (action == 2) {
                if (this.dragStreamID == 0) {
                    this.dragSoudVolume = 1.0f;
                    this.touchMoveDiffTime = System.currentTimeMillis() - this.touchDownTime;
                    if (this.touchMoveDiffTime > this.mLongPressTime && this.touchDownTime != 0) {
                        Log.d(TAG, "SOUND PLAY SOUND_ID_DRAG touchMoveDiff = " + this.touchMoveDiffTime);
                        playSound(1);
                    }
                }
            } else if (action == 1 || action == 3 || action == 4) {
                Log.i(TAG, "handleTouchEvent action : " + action);
                if (this.dragStreamID != 0) {
                    this.dragSoudMinusOffset = 0.039f;
                    this.isFadeOutSound = true;
                    if (this.dragSoudVolume == 1.0f) {
                        fadeOutSound();
                    }
                }
            }
            handleTouchEvent(event, view);
        }
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    public void setHidden(boolean isHidden) {
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
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
        if (!hasWindowFocus && !this.isUnlocked && this.dragStreamID != 0 && !this.isUnlocked) {
            this.dragSoudMinusOffset = 0.039f;
            this.isFadeOutSound = true;
            if (this.dragSoudVolume == 1.0f) {
                fadeOutSound();
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(0);
            this.mHandler = null;
        }
    }

    private void makeSound() {
        stopReleaseSound();
        if ((KeyguardProperties.isEffectProcessSeparated() || KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted()) && this.mSoundPool == null) {
            Log.d(TAG, "sound : new SoundPool");
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(TAB_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(DRAG_SOUND_PATH, 1);
            this.sounds[2] = this.mSoundPool.load(UNLOCK_SOUND_PATH, 1);
            this.mSoundPool.setOnLoadCompleteListener(new C04713());
        }
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C04724();
        postDelayed(this.releaseSoundRunnable, 2000);
    }

    private void playSound(int soundId) {
        Log.d(TAG, "playSound() -  mSoundPool = " + this.mSoundPool);
        if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
            Log.d(TAG, "playSound() - soundId = " + soundId);
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
                postDelayed(new C04735(), 10);
                return;
            }
            Log.d(TAG, "SOUND STOP because UP or Unlock");
            stopReleaseSound();
            releaseSound();
        }
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
        update();
    }

    public void setContextualWallpaper(Bitmap bmp) {
        if (bmp != null) {
            bmp = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888);
            setBitmap(bmp);
            if (this.mImageView != null) {
                this.mImageView.setImageBitmap(bmp);
            }
        }
    }

    public static String getCounterEffectName() {
        return null;
    }

    public static boolean isBackgroundEffect() {
        return true;
    }
}
