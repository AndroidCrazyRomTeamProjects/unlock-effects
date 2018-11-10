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
import com.android.systemui.statusbar.Feature;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import com.samsung.android.visualeffect.IEffectListener;
import java.io.InputStream;
import java.util.HashMap;

public class KeyguardEffectViewRippleInk extends EffectView implements KeyguardEffectViewBase {
    private static final String DOWN_SOUND_PATH = "/system/media/audio/ui/ve_ripple_down.ogg";
    public static final int UPDATE_TYPE_CHANGE_BACKGROUND = 1;
    public static final int UPDATE_TYPE_USER_SWITCHING = 2;
    private static final String UP_SOUND_PATH = "/system/media/audio/ui/ve_ripple_up.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(Feature.mLocalSecurity, Feature.mLocalSecurity);
    final int SOUND_ID_DOWN = 0;
    final int SOUND_ID_DRAG = 1;
    private final String TAG = "RippleInk_KeyguardEffect";
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    private boolean isUnlocked = false;
    KeyguardManager keyguardManager;
    private float leftVolumeMax = 1.0f;
    private Context mContext;
    private int mDisplayId = -1;
    private SoundHandler mHandler;
    private ImageView mImageView;
    private KeyguardEffectSound mKeyguardEffectSound;
    private IEffectListener mListener;
    private SoundPool mSoundPool = null;
    private Runnable releaseSoundRunnable = null;
    private float rightVolumeMax = 1.0f;
    Message soundMsg;
    private int[] sounds = null;
    private int windowHeight = 0;
    private int windowWidth = 0;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewRippleInk$1 */
    class C05101 implements IEffectListener {
        C05101() {
        }

        public void onReceive(int status, HashMap<?, ?> params) {
            if (status == 1) {
                if (((String) params.get("sound")).contentEquals("down")) {
                    KeyguardEffectViewRippleInk.this.playSound(0);
                } else if (((String) params.get("sound")).contentEquals("drag")) {
                    KeyguardEffectViewRippleInk.this.playSound(1);
                }
            } else if (status == 0) {
                KeyguardEffectViewRippleInk.this.soundMsg = KeyguardEffectViewRippleInk.this.mHandler.obtainMessage();
                KeyguardEffectViewRippleInk.this.soundMsg.what = 0;
                KeyguardEffectViewRippleInk.this.mHandler.sendMessage(KeyguardEffectViewRippleInk.this.soundMsg);
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewRippleInk$2 */
    class C05112 implements Runnable {
        C05112() {
        }

        public void run() {
            if (KeyguardEffectViewRippleInk.this.mSoundPool != null) {
                Log.d("RippleInk_KeyguardEffect", "WaterColor sound : release SoundPool");
                KeyguardEffectViewRippleInk.this.mSoundPool.release();
                KeyguardEffectViewRippleInk.this.mSoundPool = null;
            }
            KeyguardEffectViewRippleInk.this.releaseSoundRunnable = null;
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewRippleInk$3 */
    class C05123 implements Runnable {
        C05123() {
        }

        public void run() {
            KeyguardEffectViewRippleInk.this.clearScreen();
            KeyguardEffectViewRippleInk.this.isUnlocked = false;
        }
    }

    public class SoundHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (KeyguardEffectViewRippleInk.this.mImageView != null) {
                        Log.d("RippleInk_KeyguardEffect", "removeView mImageView");
                        KeyguardEffectViewRippleInk.this.mImageView.setImageBitmap(null);
                        KeyguardEffectViewRippleInk.this.removeView(KeyguardEffectViewRippleInk.this.mImageView);
                        KeyguardEffectViewRippleInk.this.mImageView = null;
                        return;
                    }
                    return;
                case 1:
                    if (KeyguardEffectViewRippleInk.this.mSoundPool != null) {
                        float volume = 0.2f * ((float) KeyguardEffectViewRippleInk.this.soundMsg.arg2);
                        KeyguardEffectViewRippleInk.this.mSoundPool.setVolume(KeyguardEffectViewRippleInk.this.soundMsg.arg1, volume, volume);
                        if (msg.arg2 != 0) {
                            KeyguardEffectViewRippleInk.this.soundMsg = KeyguardEffectViewRippleInk.this.mHandler.obtainMessage();
                            KeyguardEffectViewRippleInk.this.soundMsg.what = 1;
                            KeyguardEffectViewRippleInk.this.soundMsg.arg1 = msg.arg1;
                            KeyguardEffectViewRippleInk.this.soundMsg.arg2 = msg.arg2 - 1;
                            sendMessageDelayed(KeyguardEffectViewRippleInk.this.soundMsg, 10);
                            return;
                        }
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public KeyguardEffectViewRippleInk(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewRippleInk(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewRippleInk(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d("RippleInk_KeyguardEffect", "KeyguardEffectViewRippleInk Constructor");
        this.mContext = context;
        this.mDisplayId = displayId;
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, "RippleInk_KeyguardEffect");
        this.keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.windowWidth = displayMetrics.widthPixels;
        this.windowHeight = displayMetrics.heightPixels;
        Log.d("RippleInk_KeyguardEffect", "KeyguardEffectViewRippleInk windowWidth = " + this.windowWidth + ", windowHeight = " + this.windowHeight);
        setEffect(8);
        EffectDataObj data = new EffectDataObj();
        data.setEffect(8);
        data.rippleInkData.windowWidth = this.windowWidth;
        data.rippleInkData.windowHeight = this.windowHeight;
        data.rippleInkData.reflectionBitmap = makeResBitmap(C0302R.drawable.reflectionmap);
        init(data);
        this.sounds = new int[2];
        HashMap<String, Bitmap> map2 = new HashMap();
        map2.put("Bitmap", setBackground());
        handleCustomEvent(0, map2);
        this.mImageView = new ImageView(this.mContext);
        this.mImageView.setScaleType(ScaleType.CENTER_CROP);
        addView(this.mImageView, -1, -1);
        if (this.mHandler == null) {
            Log.d("RippleInk_KeyguardEffect", "new SoundHandler()");
            this.mHandler = new SoundHandler();
        }
        this.mListener = new C05101();
        setListener(this.mListener);
    }

    private Bitmap setBackground() {
        Log.d("RippleInk_KeyguardEffect", "setBackground");
        Bitmap pBitmap = null;
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
        if (newBitmapDrawable != null) {
            pBitmap = newBitmapDrawable.getBitmap();
            if (pBitmap != null) {
                Log.d("RippleInk_KeyguardEffect", "pBitmap.width = " + pBitmap.getWidth() + ", pBitmap.height = " + pBitmap.getHeight());
            } else {
                Log.d("RippleInk_KeyguardEffect", "pBitmap is null");
            }
        } else {
            Log.d("RippleInk_KeyguardEffect", "newBitmapDrawable is null");
        }
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
            Log.d("RippleInk_KeyguardEffect", "WaterColor sound : new SoundPool");
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(DOWN_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(UP_SOUND_PATH, 1);
        }
        if (this.mHandler == null) {
            Log.d("RippleInk_KeyguardEffect", "new SoundHandler()");
            this.mHandler = new SoundHandler();
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C05112();
        postDelayed(this.releaseSoundRunnable, 2000);
    }

    private void stopReleaseSound() {
        if (this.releaseSoundRunnable != null) {
            removeCallbacks(this.releaseSoundRunnable);
            this.releaseSoundRunnable = null;
        }
    }

    private void playSound(int soundId) {
        stopReleaseSound();
        if (this.mSoundPool == null) {
            Log.d("RippleInk_KeyguardEffect", "ACTION_DOWN, mSoundPool == null");
            makeSound();
        }
        if (this.mKeyguardEffectSound.isPlayPossible() && this.mSoundPool != null) {
            int streanID = this.mSoundPool.play(this.sounds[soundId], this.leftVolumeMax, this.rightVolumeMax, 0, 0, 1.0f);
            if (soundId == 1 && this.mHandler != null) {
                this.soundMsg = this.mHandler.obtainMessage();
                this.soundMsg.what = 1;
                this.soundMsg.arg1 = streanID - 1;
                this.soundMsg.arg2 = 4;
                this.mHandler.sendMessage(this.soundMsg);
            }
        }
    }

    public void show() {
        Log.d("RippleInk_KeyguardEffect", "show");
        makeSound();
        reInit(null);
        clearScreen();
        this.isUnlocked = false;
    }

    public void reset() {
        Log.d("RippleInk_KeyguardEffect", "reset");
        clearScreen();
        this.isUnlocked = false;
    }

    public void cleanUp() {
        Log.d("RippleInk_KeyguardEffect", "cleanUp");
        stopReleaseSound();
        releaseSound();
        postDelayed(new C05123(), 400);
    }

    public void update() {
        Log.d("RippleInk_KeyguardEffect", "update");
        Bitmap bmp = setBackground();
        HashMap<String, Bitmap> map = new HashMap();
        map.put("Bitmap", bmp);
        handleCustomEvent(0, map);
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(bmp);
        }
    }

    public void update(int updateType) {
        Log.d("RippleInk_KeyguardEffect", "changeBackground()");
        Bitmap bmp = setBackground();
        HashMap<String, Bitmap> map = new HashMap();
        map.put("Bitmap", bmp);
        handleCustomEvent(0, map);
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(bmp);
        }
    }

    public void screenTurnedOn() {
        Log.d("RippleInk_KeyguardEffect", "screenTurnedOn");
        this.isUnlocked = false;
    }

    public void screenTurnedOff() {
        Log.d("RippleInk_KeyguardEffect", "screenTurnedOff");
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        HashMap<Object, Object> map = new HashMap();
        map.put("StartDelay", Long.valueOf(startDelay));
        map.put("Rect", rect);
        handleCustomEvent(1, map);
        this.isUnlocked = false;
    }

    public long getUnlockDelay() {
        return 0;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.d("RippleInk_KeyguardEffect", "handleUnlock");
        this.isUnlocked = true;
    }

    public void playLockSound() {
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (!this.isUnlocked) {
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

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("RippleInk_KeyguardEffect", "onDetachedFromWindow");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(0);
            this.mHandler = null;
        }
    }

    public void setHidden(boolean isHidden) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("RippleInk_KeyguardEffect", "onWindowFocusChanged hasWindowFocus = " + hasWindowFocus);
        if (!hasWindowFocus && this.isUnlocked) {
        }
    }

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.d("RippleInk_KeyguardEffect", "setContextualWallpaper");
        if (bmp == null) {
            Log.d("RippleInk_KeyguardEffect", "bmp is null" + bmp);
            return;
        }
        bmp = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888);
        HashMap<String, Bitmap> map = new HashMap();
        map.put("Bitmap", bmp);
        handleCustomEvent(0, map);
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(bmp);
        }
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
