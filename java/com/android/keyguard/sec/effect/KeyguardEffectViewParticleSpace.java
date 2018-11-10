package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.samsung.android.visualeffect.EffectView;
import java.util.HashMap;

public class KeyguardEffectViewParticleSpace extends FrameLayout implements KeyguardEffectViewBase {
    private static final String DRAG_SOUND_PATH = "/system/media/audio/ui/ve_poppingcolours_drag.ogg";
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_poppingcolours_lock.ogg";
    private static final String TAP_SOUND_PATH = "/system/media/audio/ui/ve_poppingcolours_tap.ogg";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_poppingcolours_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, UNLOCK_SOUND_PATH);
    private final boolean DBG = true;
    final int DRAG_SOUND_COUNT_INTERVAL = 60;
    final int DRAG_SOUND_COUNT_START_POINT = 40;
    final int SOUND_ID_DRAG = 1;
    final int SOUND_ID_TAB = 0;
    private final String TAG = "VisualEffectParticleEffect";
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    private int cpuMinValue;
    private int dragSoundCount = 0;
    private int gpuMaxValue;
    private boolean hasWindowFocus = false;
    private boolean isUnlocking = false;
    private int lastPlayedIdBeforeUnlock;
    private float leftVolumeMax = 0.3f;
    private Context mContext;
    private int mDisplayId = -1;
    private KeyguardEffectSound mKeyguardEffectSound;
    private KeyguardWindowCallback mKeyguardWindowCallback;
    private SoundPool mSoundPool = null;
    private WindowManager mWindowManager;
    private EffectView particleSpaceEffect;
    private float rightVolumeMax = 0.3f;
    private int[] sounds = null;
    private boolean useCPUMinClock = false;
    private boolean useGPUMaxClock = false;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewParticleSpace$1 */
    class C05091 implements Runnable {
        C05091() {
        }

        public void run() {
            Log.d("VisualEffectParticleEffect", "dispatchDraw() mKeyguardWindowCallback.onShown()");
            if (KeyguardEffectViewParticleSpace.this.mKeyguardWindowCallback != null) {
                KeyguardEffectViewParticleSpace.this.mKeyguardWindowCallback.onShown();
            }
        }
    }

    public KeyguardEffectViewParticleSpace(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewParticleSpace(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewParticleSpace(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : Constructor");
        Log.d("VisualEffectParticleEffect", "KeyguardWindowCallback = " + callback);
        this.mContext = context;
        this.mDisplayId = displayId;
        this.mKeyguardWindowCallback = callback;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, "VisualEffectParticleEffect");
        this.particleSpaceEffect = new EffectView(context);
        this.particleSpaceEffect.setEffect(3);
        addView(this.particleSpaceEffect);
    }

    private void makeSound() {
        if ((KeyguardProperties.isEffectProcessSeparated() || KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted()) && this.mSoundPool == null) {
            Log.d("VisualEffectParticleEffect", "new SoundPool");
            this.sounds = new int[3];
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(TAP_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(DRAG_SOUND_PATH, 1);
        }
    }

    private void releaseSound() {
        if (this.mSoundPool != null) {
            Log.d("VisualEffectParticleEffect", "releaseSound");
            this.mSoundPool.release();
            this.mSoundPool = null;
        }
    }

    private void playSound(int soundId) {
        if (!this.isUnlocking || soundId != 1) {
            Log.d("VisualEffectParticleEffect", "playSound() -  mSoundPool = " + this.mSoundPool);
            if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
                Log.d("VisualEffectParticleEffect", "playSound() - soundId = " + soundId);
                this.lastPlayedIdBeforeUnlock = this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
            }
        }
    }

    public void show() {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : show");
        this.isUnlocking = false;
        if (this.particleSpaceEffect != null) {
            this.particleSpaceEffect.clearScreen();
        }
        makeSound();
    }

    public void reset() {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : reset");
    }

    public void cleanUp() {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : cleanUp");
        this.particleSpaceEffect.clearScreen();
        releaseSound();
    }

    public void update() {
        Log.i("VisualEffectParticleEffect", "update");
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
        if (newBitmapDrawable == null) {
            Log.i("VisualEffectParticleEffect", "newBitmapDrawable  is null");
            return;
        }
        Bitmap originBitmap = newBitmapDrawable.getBitmap();
        if (originBitmap != null) {
            setBitmap(originBitmap);
        }
    }

    private void setBitmap(Bitmap originBitmap) {
        HashMap<String, Bitmap> map = new HashMap();
        map.put("BGBitmap", originBitmap);
        this.particleSpaceEffect.handleCustomEvent(0, map);
    }

    public void screenTurnedOn() {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : screenTurnedOn");
        this.particleSpaceEffect.handleCustomEvent(99, null);
    }

    public void screenTurnedOff() {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : screenTurnedOff");
        this.particleSpaceEffect.clearScreen();
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : showUnlockAffordance, startDelay = " + startDelay);
        HashMap<String, Object> hm1 = new HashMap();
        hm1.put("StartDelay", Long.valueOf(startDelay));
        hm1.put("Rect", rect);
        this.particleSpaceEffect.handleCustomEvent(1, hm1);
    }

    public long getUnlockDelay() {
        return 300;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : handleUnlock");
        this.isUnlocking = true;
        this.particleSpaceEffect.handleCustomEvent(2, null);
    }

    public void playLockSound() {
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : playLockSound");
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (event.getActionMasked() == 0) {
            this.dragSoundCount = 40;
            if (this.mSoundPool == null) {
                Log.d("VisualEffectParticleEffect", "ACTION_DOWN, mSoundPool == null");
                makeSound();
            }
            playSound(0);
        } else if (event.getActionMasked() == 2) {
            this.dragSoundCount++;
            if (this.dragSoundCount >= 60) {
                playSound(1);
                this.dragSoundCount = 0;
            }
        }
        this.particleSpaceEffect.handleTouchEvent(event, view);
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    public void setHidden(boolean isHidden) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        this.hasWindowFocus = hasWindowFocus;
        Log.d("VisualEffectParticleEffect", "KeyguardEffectViewParticleSpace : onWindowFocusChanged - " + hasWindowFocus);
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mKeyguardWindowCallback != null) {
            postDelayed(new C05091(), 100);
        }
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
        update();
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.i("VisualEffectParticleEffect", "setContextualWallpaper");
        if (bmp == null) {
            Log.i("VisualEffectParticleEffect", "bmp  is null");
        } else {
            setBitmap(KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888));
        }
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
