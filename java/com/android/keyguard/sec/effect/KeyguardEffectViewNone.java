package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.android.keyguard.C0302R;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.samsung.android.visualeffect.EffectDataObj;
import com.samsung.android.visualeffect.EffectView;
import java.util.HashMap;

public class KeyguardEffectViewNone extends FrameLayout implements KeyguardEffectViewBase {
    private static final String LOCK_SOUND_PATH = "/system/media/audio/ui/ve_none_lock.ogg";
    public static final int TYPE_SHORTCUT = 1;
    public static final int TYPE_UNLOCK = 0;
    private static final String UNLOCK_SOUND_PATH = "/system/media/audio/ui/ve_none_unlock.ogg";
    private static LockSoundInfo mLockSoundInfo = new LockSoundInfo(LOCK_SOUND_PATH, UNLOCK_SOUND_PATH);
    private final boolean DBG = true;
    private final String TAG = "VisualEffectCircleUnlockEffect";
    private EffectView circleEffect;
    private Context mContext;
    private int mDisplayId = -1;
    private KeyguardUpdateMonitorCallback mInfoCallback = new C05081();
    private HashMap<String, Object> touchHashmap;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewNone$1 */
    class C05081 extends KeyguardUpdateMonitorCallback {
        C05081() {
        }

        public void onOpenThemeChanged() {
            Log.d("VisualEffectCircleUnlockEffect", "KeyguardUpdateMonitorCallback : onOpenThemeChanged");
            KeyguardEffectViewNone.this.reloadResForOpenTheme();
        }
    }

    public KeyguardEffectViewNone(Context context) {
        super(context);
        init(context, 0, null, true, 0);
    }

    public KeyguardEffectViewNone(Context context, KeyguardWindowCallback callback) {
        super(context);
        init(context, 0, callback, true, 0);
    }

    public KeyguardEffectViewNone(Context context, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, 0, callback, mWallpaperProcessSeparated, displayId);
    }

    public KeyguardEffectViewNone(Context context, int type) {
        super(context);
        init(context, type, null, true, 0);
    }

    public KeyguardEffectViewNone(Context context, int type, KeyguardWindowCallback callback) {
        super(context);
        init(context, type, callback, true, 0);
    }

    public KeyguardEffectViewNone(Context context, int type, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        super(context);
        init(context, type, callback, mWallpaperProcessSeparated, displayId);
    }

    public void init(Context context, int type, KeyguardWindowCallback callback, boolean mWallpaperProcessSeparated, int displayId) {
        int smallestWidth;
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : Constructor");
        this.mContext = context.getApplicationContext();
        this.mDisplayId = displayId;
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        if (screenWidth < screenHeight) {
            smallestWidth = screenWidth;
        } else {
            smallestWidth = screenHeight;
        }
        float ratio = ((float) smallestWidth) / 1080.0f;
        Log.d("VisualEffectCircleUnlockEffect", "screenWidth : " + screenWidth);
        Log.d("VisualEffectCircleUnlockEffect", "screenHeight : " + screenHeight);
        Log.d("VisualEffectCircleUnlockEffect", "ratio : " + ratio);
        this.touchHashmap = new HashMap();
        int circleUnlockMaxWidth = 0;
        if (type == 0) {
            circleUnlockMaxWidth = ((int) this.mContext.getResources().getDimension(C0302R.dimen.keyguard_lockscreen_first_border)) * 2;
        } else if (type == 1) {
            circleUnlockMaxWidth = ((int) this.mContext.getResources().getDimension(C0302R.dimen.keyguard_lockscreen_first_border_shortcut)) * 2;
        }
        int outerStrokeWidth = (int) (4.0f * ratio);
        int innerStrokeWidth = (int) (6.0f * ratio);
        int[] lockSequenceImageId = new int[]{C0302R.drawable.keyguard_none_lock_01, C0302R.drawable.keyguard_none_lock_02, C0302R.drawable.keyguard_none_lock_03, C0302R.drawable.keyguard_none_lock_04, C0302R.drawable.keyguard_none_lock_05, C0302R.drawable.keyguard_none_lock_06, C0302R.drawable.keyguard_none_lock_07, C0302R.drawable.keyguard_none_lock_08, C0302R.drawable.keyguard_none_lock_09, C0302R.drawable.keyguard_none_lock_10, C0302R.drawable.keyguard_none_lock_11, C0302R.drawable.keyguard_none_lock_12, C0302R.drawable.keyguard_none_lock_13, C0302R.drawable.keyguard_none_lock_14, C0302R.drawable.keyguard_none_lock_15, C0302R.drawable.keyguard_none_lock_16, C0302R.drawable.keyguard_none_lock_17, C0302R.drawable.keyguard_none_lock_18, C0302R.drawable.keyguard_none_lock_19, C0302R.drawable.keyguard_none_lock_20, C0302R.drawable.keyguard_none_lock_21, C0302R.drawable.keyguard_none_lock_22, C0302R.drawable.keyguard_none_lock_23, C0302R.drawable.keyguard_none_lock_24, C0302R.drawable.keyguard_none_lock_25, C0302R.drawable.keyguard_none_lock_26, C0302R.drawable.keyguard_none_lock_27, C0302R.drawable.keyguard_none_lock_28, C0302R.drawable.keyguard_none_lock_29, C0302R.drawable.keyguard_none_lock_30};
        this.circleEffect = new EffectView(this.mContext);
        this.circleEffect.setEffect(2);
        EffectDataObj data = new EffectDataObj();
        data.setEffect(2);
        data.circleData.circleUnlockMaxWidth = circleUnlockMaxWidth;
        data.circleData.outerStrokeWidth = outerStrokeWidth;
        data.circleData.innerStrokeWidth = innerStrokeWidth;
        data.circleData.lockSequenceImageId = lockSequenceImageId;
        data.circleData.arrowId = C0302R.drawable.keyguard_none_arrow;
        data.circleData.hasNoOuterCircle = KeyguardProperties.isUSAFeature();
        this.circleEffect.init(data);
        if (KeyguardProperties.isLatestPhoneUX() || KeyguardProperties.isLatestTabletUX()) {
            setMinWidthOffset((int) this.mContext.getResources().getDimension(C0302R.dimen.keyguard_shortcut_min_width_offset));
            setArrowForButton(C0302R.drawable.keyguard_shortcut_arrow);
        }
        if (KeyguardProperties.isLatestShortcutEffect()) {
            setOuterCircleType(false);
            showSwipeCircleEffect(false);
        }
        addView(this.circleEffect);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
    }

    public void setMinWidthOffset(int offset) {
        EffectDataObj data = new EffectDataObj();
        data.setEffect(2);
        data.circleData.minWidthOffset = offset;
        this.circleEffect.reInit(data);
    }

    public void showSwipeCircleEffect(boolean value) {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : showSwipeCircleEffect");
        HashMap<String, Boolean> hm = new HashMap();
        hm.put("showSwipeCircleEffect", Boolean.valueOf(value));
        this.circleEffect.handleCustomEvent(99, hm);
    }

    private void setOuterCircleType(boolean isStroke) {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : setOuterCircleType");
        HashMap<String, Boolean> hm = new HashMap();
        hm.put("setOuterCircleType", Boolean.valueOf(isStroke));
        this.circleEffect.handleCustomEvent(99, hm);
    }

    public void setArrowForButton(int arrowForButtonId) {
        EffectDataObj data = new EffectDataObj();
        data.setEffect(2);
        data.circleData.arrowForButtonId = arrowForButtonId;
        this.circleEffect.reInit(data);
    }

    private void reloadResForOpenTheme() {
        HashMap<String, Boolean> hm = new HashMap();
        hm.put("reloadResForOpenTheme", Boolean.valueOf(true));
        this.circleEffect.handleCustomEvent(99, hm);
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        this.circleEffect.handleTouchEvent(event, view);
        return true;
    }

    public void show() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : show");
        if (this.circleEffect != null) {
            this.circleEffect.clearScreen();
        }
    }

    public void reset() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : reset");
        if (this.circleEffect != null) {
            this.circleEffect.clearScreen();
        }
    }

    public void cleanUp() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : cleanUp");
    }

    public void update() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : update");
    }

    public void screenTurnedOn() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : screenTurnedOn");
    }

    public void screenTurnedOff() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : screenTurnedOff");
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : showUnlockAffordance");
        if (this.circleEffect != null) {
            HashMap<String, Object> hm = new HashMap();
            hm.put("StartDelay", Long.valueOf(startDelay));
            hm.put("Rect", rect);
            this.circleEffect.handleCustomEvent(1, hm);
        }
    }

    public long getUnlockDelay() {
        return 0;
    }

    public void handleUnlock(View view, MotionEvent event) {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : handleUnlock");
        if (this.circleEffect != null) {
            this.circleEffect.handleCustomEvent(2, null);
        }
    }

    public void playLockSound() {
        Log.d("VisualEffectCircleUnlockEffect", "KeyguardEffectViewNone : playLockSound");
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    public void setHidden(boolean isHidden) {
        if (this.circleEffect != null) {
            this.circleEffect.clearScreen();
        }
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
