package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.keyguard.C0302R;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import java.util.HashMap;

public class KeyguardEffectViewLensFlare extends FrameLayout implements KeyguardEffectViewBase {
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_lensflare_lock.ogg";
    private static final String SILENCE_SOUND_PATH = "/system/media/audio/ui/ve_silence.ogg";
    private static final String TAG = "KeyguardEffectViewLensFlare";
    private static final String TAP_SOUND_PATH = "/system/media/audio/ui/ve_lensflare_tap.ogg";
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_lensflare_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, SILENCE_SOUND_PATH);
    private EffectView lensFlareEffect;
    private Context mContext;
    private int mDisplayId = -1;

    public KeyguardEffectViewLensFlare(Context context) {
        super(context);
        init(context, null, true, 0);
    }

    public KeyguardEffectViewLensFlare(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, callback, true, 0);
    }

    public KeyguardEffectViewLensFlare(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, callback, mWallpaperProcessSeparated, displayId);
    }

    private void init(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        Log.d(TAG, "KeyguardEffectViewLensFlare Constructor");
        this.mContext = context;
        this.mDisplayId = displayId;
        this.lensFlareEffect = new EffectView(this.mContext);
        this.lensFlareEffect.setEffect(11);
        EffectDataObj data = new EffectDataObj();
        data.setEffect(11);
        data.lensFlareData.hexagon_blue = C0302R.drawable.keyguard_flare_hexagon_blue;
        data.lensFlareData.hexagon_green = C0302R.drawable.keyguard_flare_hexagon_green;
        data.lensFlareData.hexagon_orange = C0302R.drawable.keyguard_flare_hexagon_orange;
        data.lensFlareData.hoverlight = C0302R.drawable.keyguard_flare_hoverlight;
        data.lensFlareData.light = C0302R.drawable.keyguard_flare_light_00040;
        data.lensFlareData.long_light = C0302R.drawable.keyguard_flare_long;
        data.lensFlareData.particle = C0302R.drawable.keyguard_flare_particle;
        data.lensFlareData.rainbow = C0302R.drawable.keyguard_flare_rainbow;
        data.lensFlareData.ring = C0302R.drawable.keyguard_flare_ring;
        data.lensFlareData.vignetting = C0302R.drawable.keyguard_flare_vignetting;
        data.lensFlareData.tapSoundPath = TAP_SOUND_PATH;
        data.lensFlareData.unlockSoundPath = UNLOCK_SOUND_PATH;
        this.lensFlareEffect.init(data);
        addView(this.lensFlareEffect);
    }

    public void show() {
        Log.d(TAG, "show");
        HashMap<String, Object> hm = new HashMap();
        hm.put("show", null);
        this.lensFlareEffect.handleCustomEvent(99, hm);
    }

    public void reset() {
        Log.d(TAG, "reset");
        HashMap<String, Object> hm = new HashMap();
        hm.put("reset", null);
        this.lensFlareEffect.handleCustomEvent(99, hm);
    }

    public void cleanUp() {
        Log.d(TAG, "cleanUp");
        this.lensFlareEffect.clearScreen();
    }

    public void update() {
        Log.d(TAG, "update");
    }

    public void screenTurnedOn() {
        Log.d(TAG, "screenTurnedOn");
        HashMap<String, Object> hm = new HashMap();
        hm.put("screenTurnedOn", null);
        this.lensFlareEffect.handleCustomEvent(99, hm);
    }

    public void screenTurnedOff() {
        Log.d(TAG, "screenTurnedOff");
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.d(TAG, "showUnlockAffordance");
        HashMap<Object, Object> map = new HashMap();
        map.put("startDelay", Long.valueOf(startDelay));
        map.put("rect", rect);
        this.lensFlareEffect.handleCustomEvent(1, map);
    }

    public long getUnlockDelay() {
        return 250;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.d(TAG, "handleUnlock");
        this.lensFlareEffect.handleCustomEvent(2, null);
    }

    public void playLockSound() {
        HashMap<String, Object> hm = new HashMap();
        hm.put("playLockSound", null);
        this.lensFlareEffect.handleCustomEvent(99, hm);
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        this.lensFlareEffect.handleTouchEvent(event, view);
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return true;
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
        return false;
    }

    public static String getCounterEffectName() {
        return "Wallpaper";
    }
}
