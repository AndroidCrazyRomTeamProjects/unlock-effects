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
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.android.systemui.statusbar.Feature;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import com.samsung.android.visualeffect.IEffectListener;
import java.io.InputStream;
import java.util.HashMap;

public class KeyguardEffectViewIndigoDiffusion extends EffectView implements KeyguardEffectViewBase {
    private static final String DOWN_SOUND_PATH = "/system/media/audio/ui/ve_indigodiffusion_ripple_down.ogg";
    public static final int IMAGE_TYPE_BURGUNDY_RED = 1;
    public static final int IMAGE_TYPE_MIDNIGHT_BLUE_OR_NORMAL = 0;
    public static final int UPDATE_TYPE_CHANGE_BACKGROUND = 1;
    public static final int UPDATE_TYPE_USER_SWITCHING = 2;
    private static final String UP_SOUND_PATH = "/system/media/audio/ui/ve_indigodiffusion_ripple_up.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(Feature.mLocalSecurity, Feature.mLocalSecurity);
    final int SOUND_ID_DOWN = 0;
    final int SOUND_ID_DRAG = 1;
    private final String TAG = "KeyguardEffectViewIndigoDiffusion";
    private final long UNLOCK_SOUND_PLAY_TIME = 2000;
    int imageType = 0;
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

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewIndigoDiffusion$1 */
    class C05021 implements IEffectListener {
        C05021() {
        }

        public void onReceive(int status, HashMap<?, ?> params) {
            if (status == 1) {
                if (((String) params.get("sound")).contentEquals("down")) {
                    KeyguardEffectViewIndigoDiffusion.this.playSound(0);
                } else if (((String) params.get("sound")).contentEquals("drag")) {
                    KeyguardEffectViewIndigoDiffusion.this.playSound(1);
                }
            } else if (status == 0) {
                KeyguardEffectViewIndigoDiffusion.this.soundMsg = KeyguardEffectViewIndigoDiffusion.this.mHandler.obtainMessage();
                KeyguardEffectViewIndigoDiffusion.this.soundMsg.what = 0;
                KeyguardEffectViewIndigoDiffusion.this.mHandler.sendMessage(KeyguardEffectViewIndigoDiffusion.this.soundMsg);
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewIndigoDiffusion$2 */
    class C05032 implements Runnable {
        C05032() {
        }

        public void run() {
            if (KeyguardEffectViewIndigoDiffusion.this.mSoundPool != null) {
                Log.d("KeyguardEffectViewIndigoDiffusion", "IndigoDiffusion sound : release SoundPool");
                KeyguardEffectViewIndigoDiffusion.this.mSoundPool.release();
                KeyguardEffectViewIndigoDiffusion.this.mSoundPool = null;
            }
            KeyguardEffectViewIndigoDiffusion.this.releaseSoundRunnable = null;
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewIndigoDiffusion$3 */
    class C05043 implements Runnable {
        C05043() {
        }

        public void run() {
            KeyguardEffectViewIndigoDiffusion.this.clearScreen();
        }
    }

    public class SoundHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (KeyguardEffectViewIndigoDiffusion.this.mImageView != null) {
                        Log.d("KeyguardEffectViewIndigoDiffusion", "removeView mImageView");
                        KeyguardEffectViewIndigoDiffusion.this.mImageView.setImageBitmap(null);
                        KeyguardEffectViewIndigoDiffusion.this.removeView(KeyguardEffectViewIndigoDiffusion.this.mImageView);
                        KeyguardEffectViewIndigoDiffusion.this.mImageView = null;
                        return;
                    }
                    return;
                case 1:
                    if (KeyguardEffectViewIndigoDiffusion.this.mSoundPool != null) {
                        float volume = 0.2f * ((float) KeyguardEffectViewIndigoDiffusion.this.soundMsg.arg2);
                        KeyguardEffectViewIndigoDiffusion.this.mSoundPool.setVolume(KeyguardEffectViewIndigoDiffusion.this.soundMsg.arg1, volume, volume);
                        if (msg.arg2 != 0) {
                            KeyguardEffectViewIndigoDiffusion.this.soundMsg = KeyguardEffectViewIndigoDiffusion.this.mHandler.obtainMessage();
                            KeyguardEffectViewIndigoDiffusion.this.soundMsg.what = 1;
                            KeyguardEffectViewIndigoDiffusion.this.soundMsg.arg1 = msg.arg1;
                            KeyguardEffectViewIndigoDiffusion.this.soundMsg.arg2 = msg.arg2 - 1;
                            sendMessageDelayed(KeyguardEffectViewIndigoDiffusion.this.soundMsg, 10);
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

    public KeyguardEffectViewIndigoDiffusion(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewIndigoDiffusion(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewIndigoDiffusion(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d("KeyguardEffectViewIndigoDiffusion", "KeyguardEffectViewIndigoDiffusion Constructor");
        this.mContext = context;
        this.mDisplayId = displayId;
        this.mKeyguardEffectSound = new KeyguardEffectSound(this.mContext, "KeyguardEffectViewIndigoDiffusion");
        this.keyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.windowWidth = displayMetrics.widthPixels;
        this.windowHeight = displayMetrics.heightPixels;
        setEffect(9);
        EffectDataObj data = new EffectDataObj();
        data.setEffect(9);
        data.indigoDiffuseData.windowWidth = this.windowWidth;
        data.indigoDiffuseData.windowHeight = this.windowHeight;
        data.indigoDiffuseData.reflectionBitmap = makeResBitmap(C0302R.drawable.reflectionmap);
        changeColor(35, 35, 85);
        init(data);
        this.sounds = new int[2];
        HashMap<String, Bitmap> map2 = new HashMap();
        map2.put("Bitmap", setBackground());
        handleCustomEvent(0, map2);
        this.mImageView = new ImageView(this.mContext);
        this.mImageView.setScaleType(ScaleType.CENTER_CROP);
        addView(this.mImageView, -1, -1);
        if (this.mHandler == null) {
            Log.d("KeyguardEffectViewIndigoDiffusion", "new SoundHandler()");
            this.mHandler = new SoundHandler();
        }
        this.mListener = new C05021();
        setListener(this.mListener);
    }

    private Bitmap setBackground() {
        Log.d("KeyguardEffectViewIndigoDiffusion", "setBackground");
        Bitmap pBitmap = null;
        BitmapDrawable newBitmapDrawable = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, this.mDisplayId);
        if (newBitmapDrawable != null) {
            pBitmap = newBitmapDrawable.getBitmap();
            if (pBitmap != null) {
                Log.d("KeyguardEffectViewIndigoDiffusion", "pBitmap.width = " + pBitmap.getWidth() + ", pBitmap.height = " + pBitmap.getHeight());
            } else {
                Log.d("KeyguardEffectViewIndigoDiffusion", "pBitmap is null");
            }
        } else {
            Log.d("KeyguardEffectViewIndigoDiffusion", "newBitmapDrawable is null");
        }
        return pBitmap;
    }

    public void settingsForImageType(int type) {
        Log.d("KeyguardEffectViewIndigoDiffusion", "settingsForImageType");
        if (this.imageType != type) {
            this.imageType = type;
            if (type == 0) {
                changeColor(35, 35, 85);
            } else {
                changeColor(80, 10, 25);
            }
        }
    }

    private void changeColor(int r, int g, int b) {
        EffectDataObj data = new EffectDataObj();
        data.setEffect(9);
        data.indigoDiffuseData.red = r;
        data.indigoDiffuseData.green = g;
        data.indigoDiffuseData.blue = b;
        reInit(data);
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
        if (KeyguardUpdateMonitor.getInstance(this.mContext).hasBootCompleted() && this.mSoundPool == null) {
            Log.d("KeyguardEffectViewIndigoDiffusion", "IndigoDiffusion sound : new SoundPool");
            this.mSoundPool = new Builder().setMaxStreams(10).setAudioAttributes(new AudioAttributes.Builder().setUsage(13).setContentType(4).build()).build();
            this.sounds[0] = this.mSoundPool.load(DOWN_SOUND_PATH, 1);
            this.sounds[1] = this.mSoundPool.load(UP_SOUND_PATH, 1);
        }
        if (this.mHandler == null) {
            Log.d("KeyguardEffectViewIndigoDiffusion", "new SoundHandler()");
            this.mHandler = new SoundHandler();
        }
    }

    private void releaseSound() {
        this.releaseSoundRunnable = new C05032();
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
            Log.d("KeyguardEffectViewIndigoDiffusion", "ACTION_DOWN, mSoundPool == null");
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
        Log.d("KeyguardEffectViewIndigoDiffusion", "show");
        makeSound();
        clearScreen();
        this.isUnlocked = false;
    }

    public void reset() {
        Log.d("KeyguardEffectViewIndigoDiffusion", "reset");
        clearScreen();
    }

    public void cleanUp() {
        Log.d("KeyguardEffectViewIndigoDiffusion", "cleanUp");
        stopReleaseSound();
        releaseSound();
        postDelayed(new C05043(), 400);
    }

    public void update() {
        Log.d("KeyguardEffectViewIndigoDiffusion", "update");
        Bitmap bmp = setBackground();
        HashMap<String, Bitmap> map = new HashMap();
        map.put("Bitmap", bmp);
        handleCustomEvent(0, map);
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(bmp);
        }
    }

    public void update(int updateType) {
        Log.d("KeyguardEffectViewIndigoDiffusion", "changeBackground()");
        Bitmap bmp = setBackground();
        HashMap<String, Bitmap> map = new HashMap();
        map.put("Bitmap", bmp);
        handleCustomEvent(0, map);
        if (this.mImageView != null) {
            this.mImageView.setImageBitmap(bmp);
        }
    }

    public void screenTurnedOn() {
        Log.d("KeyguardEffectViewIndigoDiffusion", "screenTurnedOn");
        this.isUnlocked = false;
    }

    public void screenTurnedOff() {
        Log.d("KeyguardEffectViewIndigoDiffusion", "screenTurnedOff");
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.d("KeyguardEffectViewIndigoDiffusion", "showUnlockAffordance");
        HashMap<Object, Object> map = new HashMap();
        map.put("StartDelay", Long.valueOf(startDelay));
        map.put("Rect", rect);
        handleCustomEvent(1, map);
    }

    public long getUnlockDelay() {
        return 0;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.d("KeyguardEffectViewIndigoDiffusion", "handleUnlock");
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
        Log.d("KeyguardEffectViewIndigoDiffusion", "onDetachedFromWindow");
        if (this.mHandler != null) {
            this.mHandler.removeMessages(0);
            this.mHandler = null;
        }
    }

    public void setHidden(boolean isHidden) {
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        Log.d("KeyguardEffectViewIndigoDiffusion", "onWindowFocusChanged hasWindowFocus = " + hasWindowFocus);
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
        Log.d("KeyguardEffectViewIndigoDiffusion", "setContextualWallpaper");
        if (bmp == null) {
            Log.d("KeyguardEffectViewIndigoDiffusion", "bmp is null" + bmp);
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

    public static LockSoundInfo getLockSoundInfo() {
        return mLockSoundInfo;
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
