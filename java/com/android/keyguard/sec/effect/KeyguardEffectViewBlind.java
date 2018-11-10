package com.android.keyguard.sec.effect;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.keyguard.C0302R;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import java.util.HashMap;

public class KeyguardEffectViewBlind extends FrameLayout implements KeyguardEffectViewBase {
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_blind_lock.ogg";
    private static final String SILENCE_SOUND_PATH = "/system/media/audio/ui/ve_silence.ogg";
    private static final String TOUCH_SOUND_PATH = "/system/media/audio/ui/ve_blind_touch.ogg";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_blind_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, SILENCE_SOUND_PATH);
    private final boolean DBG = true;
    final int DRAG_SOUND_COUNT_INTERVAL = 25;
    final int DRAG_SOUND_COUNT_START_POINT = 20;
    final int DRAG_SOUNT_DISTANCE_THRESHOLD = 200;
    final int SOUND_ID_TAB = 0;
    final int SOUND_ID_UNLOC = 1;
    private final String TAG = "BlindEffect";
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    private long affordanceDelay;
    private Rect affordanceRect;
    private EffectView blindEffect;
    private EffectDataObj data;
    private initBlindEffectByAsyncTask initBlind;
    private boolean isAsyncPostExecuted = false;
    private boolean isCleanUp = false;
    private boolean isHandleUnlock = false;
    private boolean isOnConfigurationChanged = false;
    private boolean isShow = false;
    private boolean isShowUnlockAffordance = false;
    private boolean isUpdate = false;
    private boolean isWindowFocused = true;
    private float leftVolumeMax = 1.0f;
    private Context mContext;
    private int mDisplayId = -1;
    private ImageView mImageView;
    private KeyguardEffectSound mKeyguardEffectSound;
    private SoundPool mSoundPool = null;
    private WindowManager mWindowManager;
    private Runnable releaseSoundRunnable = null;
    private float rightVolumeMax = 1.0f;
    private int[] sounds = null;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewBlind$1 */
    class C04741 implements Runnable {
        C04741() {
        }

        public void run() {
            if (KeyguardEffectViewBlind.this.mSoundPool != null) {
                Log.d("BlindEffect", "releaseSound");
                KeyguardEffectViewBlind.this.mSoundPool.release();
                KeyguardEffectViewBlind.this.mSoundPool = null;
            }
            KeyguardEffectViewBlind.this.releaseSoundRunnable = null;
        }
    }

    public class initBlindEffectByAsyncTask extends AsyncTask<Void, Void, Void> {
        Bitmap light;
        Bitmap wallpaper;

        protected void onPreExecute() {
            Log.d("BlindEffect", "KeyguardEffectViewBlind : onPreExecute");
            Log.d("BlindEffect", "KeyguardEffectViewBlind : isAsyncPostExecuted = " + KeyguardEffectViewBlind.this.isAsyncPostExecuted);
            this.light = BitmapFactory.decodeResource(KeyguardEffectViewBlind.this.getContext().getResources(), C0302R.drawable.keyguard_blind_light);
            KeyguardEffectViewBlind.this.mImageView.setScaleType(ScaleType.CENTER_CROP);
            this.wallpaper = KeyguardEffectViewBlind.this.setBackground();
            KeyguardEffectViewBlind.this.mImageView.setImageBitmap(this.wallpaper);
            KeyguardEffectViewBlind.this.addView(KeyguardEffectViewBlind.this.mImageView, -1, -1);
            Log.d("BlindEffect", "KeyguardEffectViewBlind : addView mImageView");
            KeyguardEffectViewBlind.this.blindEffect = new EffectView(KeyguardEffectViewBlind.this.mContext);
            KeyguardEffectViewBlind.this.blindEffect.setEffect(10);
            KeyguardEffectViewBlind.this.data = new EffectDataObj();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {
            HashMap<String, Long> hm0 = new HashMap();
            hm0.put("unlockDelay", Long.valueOf(KeyguardEffectViewBlind.this.getUnlockDelay()));
            KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(2, hm0);
            HashMap<String, Bitmap> hm1 = new HashMap();
            hm1.put("light", this.light);
            hm1.put("background", this.wallpaper);
            KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(0, hm1);
            KeyguardEffectViewBlind.this.blindEffect.init(KeyguardEffectViewBlind.this.data);
            Log.d("BlindEffect", "KeyguardEffectViewBlind : doInBackground");
            return null;
        }

        protected void onPostExecute(Void result) {
            Log.d("BlindEffect", "KeyguardEffectViewBlind : onPostExecute");
            KeyguardEffectViewBlind.this.isAsyncPostExecuted = true;
            Log.d("BlindEffect", "KeyguardEffectViewBlind : isAsyncPostExecuted = " + KeyguardEffectViewBlind.this.isAsyncPostExecuted);
            KeyguardEffectViewBlind.this.addView(KeyguardEffectViewBlind.this.blindEffect);
            if (KeyguardEffectViewBlind.this.mImageView != null) {
                KeyguardEffectViewBlind.this.mImageView.setImageBitmap(null);
                KeyguardEffectViewBlind.this.removeView(KeyguardEffectViewBlind.this.mImageView);
                KeyguardEffectViewBlind.this.mImageView = null;
                this.wallpaper = null;
                Log.d("BlindEffect", "KeyguardEffectViewBlind : removeView mImageView");
            }
            if (KeyguardEffectViewBlind.this.isOnConfigurationChanged) {
                HashMap<String, Boolean> hm0 = new HashMap();
                hm0.put("onConfigurationChanged", Boolean.valueOf(KeyguardEffectViewBlind.this.isWindowFocused));
                KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(99, hm0);
                Log.d("BlindEffect", "AsyncTask : onConfigurationChanged() start");
                KeyguardEffectViewBlind.this.isOnConfigurationChanged = false;
            }
            if (KeyguardEffectViewBlind.this.isShow) {
                hm0 = new HashMap();
                hm0.put("show", Boolean.valueOf(true));
                KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(99, hm0);
                Log.d("BlindEffect", "AsyncTask : show() start");
                KeyguardEffectViewBlind.this.isShow = false;
            }
            if (KeyguardEffectViewBlind.this.isCleanUp) {
                KeyguardEffectViewBlind.this.blindEffect.clearScreen();
                Log.d("BlindEffect", "AsyncTask : cleanUp() start");
                KeyguardEffectViewBlind.this.isCleanUp = false;
            }
            if (KeyguardEffectViewBlind.this.isUpdate) {
                HashMap<String, Bitmap> hm02 = new HashMap();
                hm02.put("background", KeyguardEffectViewBlind.this.setBackground());
                KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(0, hm02);
                Log.d("BlindEffect", "AsyncTask : update() start");
                KeyguardEffectViewBlind.this.isUpdate = false;
            }
            if (KeyguardEffectViewBlind.this.isHandleUnlock) {
                hm0 = new HashMap();
                hm0.put("unlock", Boolean.valueOf(true));
                KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(2, hm0);
                Log.d("BlindEffect", "AsyncTask : handleUnlock() start");
                KeyguardEffectViewBlind.this.isHandleUnlock = false;
            }
            if (KeyguardEffectViewBlind.this.isShowUnlockAffordance) {
                HashMap<String, Object> hm03 = new HashMap();
                hm03.put("StartDelay", Long.valueOf(KeyguardEffectViewBlind.this.affordanceDelay));
                hm03.put("Rect", KeyguardEffectViewBlind.this.affordanceRect);
                KeyguardEffectViewBlind.this.blindEffect.handleCustomEvent(1, hm03);
                Log.d("BlindEffect", "AsyncTask : showUnlockAffordance() start");
                KeyguardEffectViewBlind.this.isShowUnlockAffordance = false;
            }
            super.onPostExecute(result);
        }
    }

    public KeyguardEffectViewBlind(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewBlind(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewBlind(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : Constructor");
        Log.d("BlindEffect", "KeyguardEffectViewBlind displayId : " + displayId);
        this.mContext = context.getApplicationContext();
        this.mDisplayId = displayId;
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, "BlindEffect");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mImageView = new ImageView(this.mContext);
        this.initBlind = new initBlindEffectByAsyncTask();
        this.initBlind = (initBlindEffectByAsyncTask) new initBlindEffectByAsyncTask().execute(new Void[]{(Void) null});
    }

    private Bitmap setBackground() {
        Log.d("BlindEffect", "setBackground");
        Bitmap pBitmap = null;
        try {
            BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
            if (newBitmapDrawable != null) {
                pBitmap = newBitmapDrawable.getBitmap();
                Log.d("BlindEffect", "pBitmap.width = " + pBitmap.getWidth() + ", pBitmap.height = " + pBitmap.getHeight());
                for (int i = 1; i <= 2; i++) {
                    for (int j = 1; j <= 2; j++) {
                        int pixel = pBitmap.getPixel((pBitmap.getWidth() / 3) * i, (pBitmap.getHeight() / 3) * j);
                        int redValue = Color.red(pixel);
                        Log.d("BlindEffect", "pBitmap.getPixel(" + ((pBitmap.getWidth() / 3) * i) + "," + ((pBitmap.getHeight() / 3) * j) + ") : " + redValue + " " + Color.green(pixel) + " " + Color.blue(pixel));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pBitmap;
    }

    private void makeSound() {
        stopReleaseSound();
        if (KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted() && this.mSoundPool == null) {
            Log.d("BlindEffect", "new SoundPool");
            this.sounds = new int[2];
            this.mSoundPool = new SoundPool(10, 1, 0);
            this.sounds[0] = this.mSoundPool.load(TOUCH_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(UNLOCK_SOUND_PATH, 1);
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C04741();
        postDelayed(this.releaseSoundRunnable, 2000);
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    private void playSound(int soundId) {
        Log.d("BlindEffect", "playSound() -  mSoundPool = " + this.mSoundPool);
        if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
            Log.d("BlindEffect", "playSound() - soundId = " + soundId);
            this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.isWindowFocused) {
            Log.d("BlindEffect", "KeyguardEffectViewBlind : onConfigurationChanged");
        }
        if (this.isAsyncPostExecuted) {
            HashMap<String, Boolean> hm0 = new HashMap();
            hm0.put("onConfigurationChanged", Boolean.valueOf(this.isWindowFocused));
            this.blindEffect.handleCustomEvent(99, hm0);
            return;
        }
        this.isOnConfigurationChanged = true;
    }

    public void show() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : show");
        if (this.isAsyncPostExecuted) {
            HashMap<String, Boolean> hm0 = new HashMap();
            hm0.put("show", Boolean.valueOf(true));
            this.blindEffect.handleCustomEvent(99, hm0);
        } else {
            this.isShow = true;
        }
        makeSound();
    }

    public void reset() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : reset");
    }

    public void cleanUp() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : cleanUp");
        if (this.isAsyncPostExecuted) {
            this.blindEffect.clearScreen();
        } else {
            this.isCleanUp = true;
        }
        stopReleaseSound();
        releaseSound();
    }

    public void update() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : update");
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(setBackground());
            Log.d("BlindEffect", "KeyguardEffectViewBlind : setImageBitmap mImageView");
        }
        if (this.isAsyncPostExecuted) {
            HashMap<String, Bitmap> hm0 = new HashMap();
            hm0.put("background", setBackground());
            this.blindEffect.handleCustomEvent(0, hm0);
            return;
        }
        this.isUpdate = true;
    }

    public void screenTurnedOn() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : screenTurnedOn");
    }

    public void screenTurnedOff() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : screenTurnedOff");
        if (this.isAsyncPostExecuted) {
            HashMap<String, Boolean> hm0 = new HashMap();
            hm0.put("initAnimationValue", Boolean.valueOf(true));
            this.blindEffect.clearScreen();
            this.blindEffect.handleCustomEvent(99, hm0);
        }
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : showUnlockAffordance");
        if (this.isAsyncPostExecuted) {
            HashMap<String, Object> hm0 = new HashMap();
            hm0.put("StartDelay", Long.valueOf(startDelay));
            hm0.put("Rect", rect);
            this.blindEffect.handleCustomEvent(1, hm0);
            return;
        }
        this.affordanceDelay = startDelay;
        this.affordanceRect = rect;
        this.isShowUnlockAffordance = true;
    }

    public long getUnlockDelay() {
        return 0;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : handleUnlock (exit xml animation removed)");
        if (this.isAsyncPostExecuted) {
            HashMap<String, Boolean> hm0 = new HashMap();
            hm0.put("unlock", Boolean.valueOf(true));
            this.blindEffect.handleCustomEvent(2, hm0);
        } else {
            this.isHandleUnlock = true;
        }
        playSound(1);
    }

    public void playLockSound() {
        Log.d("BlindEffect", "KeyguardEffectViewBlind : playLockSound");
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (this.isAsyncPostExecuted) {
            this.blindEffect.handleTouchEvent(event, view);
            if (event.getActionMasked() == 0) {
                stopReleaseSound();
                if (this.mSoundPool == null) {
                    Log.d("BlindEffect", "ACTION_DOWN, mSoundPool == null");
                    makeSound();
                }
                playSound(0);
            }
        } else {
            Log.d("BlindEffect", "isAsyncPostExecuted=false (handleTouchEvent)");
        }
        return true;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("BlindEffect", "KeyguardEffectViewBlind : onWindowFocusChanged " + hasWindowFocus);
        this.isWindowFocused = hasWindowFocus;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    public void setHidden(boolean isHidden) {
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
