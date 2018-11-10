package com.android.keyguard.sec.effect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorEvent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.support.v4.view.ViewCompat;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.android.internal.policy.IKeyguardShowCallback;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.C0302R;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.sec.KeyguardFestivalEffect;
import com.android.keyguard.sec.KeyguardOpenThemeManager;
import com.android.keyguard.sec.KeyguardPreviewContainer;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.KeyguardUnlockViewSub;
import com.android.keyguard.sec.SecAttributionInfoView;
import com.android.keyguard.sec.SecKeyguardStatusUtils;
import com.android.keyguard.sec.rich.KeyguardEffectViewWallpaperSlider;
import com.android.keyguard.sec.rich.WallpaperWidgetController;
import com.android.keyguard.sec.wallpaper.IKeyguardWallpaperService.Stub;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperServiceWrapper;
import com.android.systemui.statusbar.Feature;
import com.samsung.android.theme.SThemeManager;
import com.sec.android.cover.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class KeyguardEffectViewController implements KeyguardEffectViewBase {
    private static final String ACTION_IMAGES_CHANGED = "com.sec.android.slidingGallery.LOCKSCREEN_IMAGE_CHANGED";
    public static final String ACTION_LOCKSCREEN_IMAGE_CHANGED = "com.sec.android.gallery3d.LOCKSCREEN_IMAGE_CHANGED";
    public static final int EFFECT_ABSTRACTTILE = 11;
    public static final int EFFECT_AUTUMN = 83;
    public static final int EFFECT_BLIND = 5;
    public static final int EFFECT_BRILLIANTCUT = 9;
    public static final int EFFECT_BRILLIANTRING = 8;
    public static final int EFFECT_COLOURDROPLET = 15;
    public static final int EFFECT_GEOMETRICMOSAIC = 12;
    public static final int EFFECT_HOME = -2;
    public static final int EFFECT_JUST_LOCK_LIVE_WALLPAPER = 100;
    public static final int EFFECT_LIGHT = 2;
    public static final int EFFECT_LIQUID = 13;
    public static final int EFFECT_LIVEWALLPAPER = -1;
    public static final int EFFECT_MASS_RIPPLE = 7;
    public static final int EFFECT_MASS_TENSION = 6;
    public static final int EFFECT_MONTBLANC = 10;
    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_PARTICLE = 14;
    public static final int EFFECT_POPPING_COLOR = 3;
    public static final int EFFECT_RIPPLE = 1;
    public static final int EFFECT_SEASONAL = 85;
    public static final int EFFECT_SPRING = 81;
    public static final int EFFECT_SUMMER = 82;
    public static final int EFFECT_TILT = 101;
    public static final int EFFECT_WATERCOLOR = 4;
    public static final int EFFECT_WINTER = 84;
    public static final int EFFECT_ZOOM_PANNING = 80;
    private static final String EMPTY_WALLPAPER_IMAGE_PATH = "/system/wallpaper/keyguard_empty_image.jpg";
    public static final int KEYGUARD_DEFAULT_WALLPAPER_TYPE_BRILLIANT = 1;
    public static final String KEYGUARD_WALLPAPER_CLASS = "com.android.systemui.keyguard.KeyguardWallpaperService";
    public static final String KEYGUARD_WALLPAPER_PACKAGE = "com.android.systemui";
    public static final String LOCK_SOUND_AUTUMN = "/system/media/audio/ui/autumn_lock.ogg";
    public static final String LOCK_SOUND_NONE = "/system/media/audio/ui/Lock_none_effect.ogg";
    public static final String LOCK_SOUND_SPRING = "/system/media/audio/ui/spring_lock.ogg";
    public static final String LOCK_SOUND_SUMMER = "/system/media/audio/ui/summer_lock.ogg";
    public static final String LOCK_SOUND_WINTER = "/system/media/audio/ui/winter_lock.ogg";
    private static final int MSG_CHARGE_STATE_CHANGED = 4852;
    private static final int MSG_WALLPAPER_FILE_CHANGED = 4850;
    private static final int MSG_WALLPAPER_PATH_CHANGED = 4849;
    private static final int MSG_WALLPAPER_PRELOAD_CHANGED = 4851;
    public static final int MSG_WALLPAPER_TYPE_CHANGED = 4848;
    private static final String RICH_LOCK_CATEGORIES_WALLPAPER_ROOT_PATH = "/data/data/com.samsung.android.keyguardwallpaperupdator/files/wallpaper_images";
    private static final String RICH_LOCK_WALLPAPER_ROOT_PATH = "/data/data/com.samsung.android.keyguardwallpaperupdator";
    public static final String SETTING_KEYGUARD_DEFAULT_WALLPAPER_TYPE_FOR_EFFECT = "keyguard_default_wallpaper_type_for_effect";
    public static final String SETTING_KEYGUARD_SET_DEFAULT_WALLPAPER = "keyguard_set_default_wallpaper";
    public static final String SETTING_LOCKSCREEN_MONTBLANC_WALLPAPER = "lockscreen_montblanc_wallpaper";
    public static final int SLIDING_INTERNAL_EVERY_12HOUR = 2;
    public static final int SLIDING_INTERNAL_EVERY_1HOUR = 1;
    public static final int SLIDING_INTERNAL_EVERY_24HOUR = 3;
    public static final int SLIDING_INTERNAL_SCREENOFF = 0;
    public static final String SlidingWallpaperPath = "com.sec.android.slidingGallery";
    private static final String TAG = "KeyguardEffectViewController";
    public static final String UNLOCK_SOUND_AUTUMN = "/system/media/audio/ui/autumn_unlock.ogg";
    public static final String UNLOCK_SOUND_NONE = "/system/media/audio/ui/Unlock_none_effect.ogg";
    public static final String UNLOCK_SOUND_SPRING = "/system/media/audio/ui/spring_unlock.ogg";
    public static final String UNLOCK_SOUND_SUMMER = "/system/media/audio/ui/summer_unlock.ogg";
    public static final String UNLOCK_SOUND_WINTER = "/system/media/audio/ui/winter_unlock.ogg";
    private static int displayHeight;
    private static int displayWidth;
    private static int mOrientation = 0;
    private static int mSlidingHour = 0;
    private static int mSlidingInterval = 0;
    private static int mSlidingMin = 0;
    private static long mSlidingScreenOffTime = 0;
    private static long mSlidingTime = 0;
    private static int mSlidingTotalCount = 0;
    private static int mSlidingWallpaperIndex = 0;
    private static KeyguardEffectViewController sKeyguardEffectViewController;
    public static ArrayList<String> uriArray;
    private final BroadcastReceiver mAdminReceiver = new C04924();
    private Rect mAffordanceRect = new Rect();
    private SecAttributionInfoView mAttributionInfoView = null;
    private FrameLayout mBackgroundRootLayout = null;
    private KeyguardEffectViewBase mBackgroundView = null;
    private boolean mBgHasAddChargeView = false;
    private boolean mBootCompleted = false;
    private KeyguardEffectViewBase mChargeView = null;
    private ContentObserver mContentObserver;
    private Context mContext;
    private int mCurrentEffect;
    private int mCurrentUserId = 0;
    private boolean mEmergencyModeStateChanged = false;
    private KeyguardFestivalEffect mFestivalEffect;
    private boolean mFestivalEffectEnabled = false;
    private FileObserver mFileObserver;
    private KeyguardEffectViewBase mForegroundCircleShortcutView = null;
    private KeyguardEffectViewBase mForegroundCircleView = null;
    private KeyguardEffectViewBase mForegroundCircleViewSub = null;
    private FrameLayout mForegroundRootLayout = null;
    private FrameLayout mForegroundRootLayoutSub = null;
    private KeyguardEffectViewBase mForegroundView = null;
    private final Handler mHandler = new C04891();
    KeyguardUpdateMonitorCallback mInfoCallback = new C04902();
    private boolean mIsShowing;
    private boolean mIsVisible = true;
    private boolean mIsWallpaperServiceBound = false;
    private KeyguardUnlockViewSub mKeyguardUnlockViewSub;
    private final ServiceConnection mKeyguardWallpaperConnection = new C04935();
    private KeyguardWallpaperServiceWrapper mKeyguardWallpaperService;
    private LockPatternUtils mLockPatternUtils;
    private LockSoundChangeCallback mLockSoundChangeCallback;
    private boolean mMusicBackgroundSet = false;
    private boolean mNeedTwoCircleView = false;
    private boolean mNeedUpdateEffectScreen = false;
    private FrameLayout mNotificationPanel = null;
    private int mOldEffect;
    private String mOldPrimaryEffect = "NULL";
    KeyguardOpenThemeManager mOpenThemeManager;
    private PowerManager mPowerManager;
    private KeyguardPreviewContainer mPreviewContainer;
    private boolean mRegisterReceiver = false;
    private Runnable mResetPreviewRunnable = new Runnable() {
        public void run() {
            Log.d(KeyguardEffectViewController.TAG, "Preview 5000ms timeout expired!");
            KeyguardEffectViewController.this.resetPreviewView();
        }
    };
    private KeyguardEffectViewWallpaperSlider mRichSliderView = null;
    private ScreenOnCallback mScreenOnCallback;
    private View mStatusBarGradationView;
    private KeyguardEffectViewBase mUnlockEffectView = null;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallbacks = new C04913();
    private boolean mUserSwitching = false;
    private ViewMediatorCallback mViewMediatorCallback;
    private KeyguardEffectViewVignetting mVignetting;
    private int mVisibleNotificationBottom = 0;
    protected String mWallpaperPath;
    private boolean mWallpaperProcessSeparated = false;
    private KeyguardWallpaperShowCallback mWallpaperShowCallback = new KeyguardWallpaperShowCallback();
    private int mWallpaperType;

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewController$1 */
    class C04891 extends Handler {
        C04891() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED /*4848*/:
                    KeyguardEffectViewController.this.handleWallpaperTypeChanged();
                    return;
                case KeyguardEffectViewController.MSG_WALLPAPER_PATH_CHANGED /*4849*/:
                case KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED /*4850*/:
                    KeyguardEffectViewController.this.handleWallpaperImageChanged();
                    return;
                case KeyguardEffectViewController.MSG_WALLPAPER_PRELOAD_CHANGED /*4851*/:
                    KeyguardEffectViewController.this.handleSetGradationLayer();
                    return;
                case KeyguardEffectViewController.MSG_CHARGE_STATE_CHANGED /*4852*/:
                    KeyguardEffectViewController.this.handleChargeStateChange();
                    return;
                default:
                    return;
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewController$2 */
    class C04902 extends KeyguardUpdateMonitorCallback {
        C04902() {
        }

        public void onUserSwitching(int userId) {
            Log.d(KeyguardEffectViewController.TAG, "*** onUserSwitching - userId :" + userId);
            if (userId != KeyguardEffectViewController.this.mCurrentUserId && KeyguardEffectViewController.this.mWallpaperProcessSeparated) {
                KeyguardEffectViewController.this.unbindWallpaperService();
                KeyguardEffectViewController.this.mCurrentUserId = userId;
                if (KeyguardEffectViewController.this.mIsShowing) {
                    KeyguardEffectViewController.this.bindWallpaperService();
                }
            }
            KeyguardEffectViewController.this.mUserSwitching = true;
            KeyguardEffectViewController.this.handleWallpaperTypeChanged();
            KeyguardEffectViewController.this.setWallpaperFileObserver();
            KeyguardEffectViewController.this.mNeedUpdateEffectScreen = true;
        }

        public void onScreenTurnedOn() {
            KeyguardEffectViewController.this.chenckSlidingWallpaperByScreenon();
            KeyguardEffectViewController.this.screenTurnedOn();
        }

        public void onScreenTurnedOff(int why) {
            super.onScreenTurnedOff(why);
            KeyguardEffectViewController.this.resetPreviewView();
            KeyguardEffectViewController.mSlidingScreenOffTime = System.currentTimeMillis();
            if (KeyguardEffectViewController.this.mRegisterReceiver) {
                KeyguardEffectViewController.this.mContext.unregisterReceiver(KeyguardEffectViewController.this.mAdminReceiver);
                KeyguardEffectViewController.this.mRegisterReceiver = false;
            }
            KeyguardEffectViewController.this.screenTurnedOff();
            KeyguardEffectViewController.this.updateIsAdminWallpaper();
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewController$3 */
    class C04913 extends KeyguardUpdateMonitorCallback {
        C04913() {
        }

        public void onBootCompleted() {
            Log.d(KeyguardEffectViewController.TAG, "onBootCompleted: mBootCompleted = true;");
            KeyguardEffectViewController.this.mBootCompleted = true;
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewController$4 */
    class C04924 extends BroadcastReceiver {
        C04924() {
        }

        public void onReceive(Context cxt, Intent intent) {
            String action = intent.getAction();
            Log.d(KeyguardEffectViewController.TAG, "onReceive action : " + intent.getAction());
            if (action.equals(KeyguardEffectViewController.ACTION_IMAGES_CHANGED)) {
                KeyguardEffectViewController.this.getDataFromSlideshow(cxt);
            } else if (action.equals(KeyguardEffectViewUtil.ACTION_UPDATE_LOCKSCREEN_WALLPAPER)) {
                if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED)) {
                    KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED);
                }
                KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED);
            }
        }
    }

    /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewController$5 */
    class C04935 implements ServiceConnection {
        C04935() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(KeyguardEffectViewController.TAG, "*** Keyguard wallpaper service connected (yay!)");
            KeyguardEffectViewController.this.mKeyguardWallpaperService = new KeyguardWallpaperServiceWrapper(Stub.asInterface(service));
            if (KeyguardEffectViewController.this.mKeyguardWallpaperService != null) {
                try {
                    KeyguardEffectViewController.this.mKeyguardWallpaperService.executeCommand(1, 0);
                } catch (RemoteException e) {
                    Log.e(KeyguardEffectViewController.TAG, "RemoteException occured while show()");
                }
                if (KeyguardEffectViewController.this.mScreenOnCallback != null) {
                    try {
                        KeyguardEffectViewController.this.mKeyguardWallpaperService.setKeyguardWallpaperShowCallback(KeyguardEffectViewController.this.mWallpaperShowCallback);
                        KeyguardEffectViewController.this.mKeyguardWallpaperService.executeCommand(5, 0);
                        if (KeyguardEffectViewController.this.mAffordanceRect.left != 0 || KeyguardEffectViewController.this.mAffordanceRect.top != 0 || KeyguardEffectViewController.this.mAffordanceRect.right != 0 || KeyguardEffectViewController.this.mAffordanceRect.bottom != 0) {
                            KeyguardEffectViewController.this.mKeyguardWallpaperService.showUnlockAffordance(0, KeyguardEffectViewController.this.mAffordanceRect, 0);
                            KeyguardEffectViewController.this.mAffordanceRect.set(0, 0, 0, 0);
                        }
                    } catch (RemoteException e2) {
                        Log.e(KeyguardEffectViewController.TAG, "RemoteException occured while isDrawFinished()");
                    }
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.v(KeyguardEffectViewController.TAG, "*** Keyguard wallpaper service disconnected (boo!)");
            KeyguardEffectViewController.this.mKeyguardWallpaperService = null;
            KeyguardEffectViewController.this.mScreenOnCallback = null;
        }
    }

    public final class KeyguardWallpaperShowCallback extends IKeyguardShowCallback.Stub {
        KeyguardWallpaperShowCallback() {
        }

        public void onShown(IBinder windowToken) throws RemoteException {
            Log.v(KeyguardEffectViewController.TAG, "KeyguardWallpaperShowCallback **** SHOWN CALLED ****");
            if (KeyguardEffectViewController.this.mScreenOnCallback != null) {
                KeyguardEffectViewController.this.mScreenOnCallback.screenOn();
            }
        }
    }

    public interface LaunchTransitionCallback {
        void onAnimationToSideCancelled();

        void onAnimationToSideEnded();

        void onAnimationToSideStarted(boolean z);

        void onAnimationToSideStarted(boolean z, float f, float f2);
    }

    public interface LockSoundChangeCallback {
        void reloadLockSound(LockSoundInfo lockSoundInfo);
    }

    public interface ScreenOnCallback {
        void screenOn();
    }

    public KeyguardEffectViewController(Context context) {
        boolean z;
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mWallpaperPath = System.getStringForUser(this.mContext.getContentResolver(), "lockscreen_wallpaper_path", -2);
        if (this.mWallpaperPath != null && this.mWallpaperPath.contains(SlidingWallpaperPath)) {
            getDataFromSlideshow(this.mContext);
            Log.d(TAG, "KeyguardEffectViewMain SlidingWallpaperPath = " + this.mWallpaperPath);
        }
        if ("2".equals(new SThemeManager(context).getVersionFromFeature(1))) {
            this.mFestivalEffectEnabled = true;
        } else {
            this.mFestivalEffectEnabled = false;
        }
        this.mFestivalEffect = new KeyguardFestivalEffect(context, this.mHandler);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        setWallpaperContentObservers();
        displayWidth = context.getResources().getDisplayMetrics().widthPixels;
        displayHeight = context.getResources().getDisplayMetrics().heightPixels;
        this.mWallpaperProcessSeparated = KeyguardProperties.isEffectProcessSeparated();
        if (this.mWallpaperProcessSeparated) {
            bindWallpaperService();
        }
        if (((int) this.mContext.getResources().getDimension(C0302R.dimen.keyguard_lockscreen_first_border)) != ((int) this.mContext.getResources().getDimension(C0302R.dimen.keyguard_lockscreen_first_border_shortcut))) {
            z = true;
        } else {
            z = false;
        }
        this.mNeedTwoCircleView = z;
    }

    public void bindWallpaperService() {
        if (this.mIsWallpaperServiceBound) {
            Log.v(TAG, "*** Keyguard wallpaper service already started");
            return;
        }
        Intent intent = new Intent();
        intent.setClassName("com.android.systemui", KEYGUARD_WALLPAPER_CLASS);
        if (this.mContext.bindServiceAsUser(intent, this.mKeyguardWallpaperConnection, 1, this.mCurrentUserId == 0 ? UserHandle.CURRENT : new UserHandle(this.mCurrentUserId))) {
            this.mIsWallpaperServiceBound = true;
            Log.v(TAG, "*** Keyguard wallpaper service started");
            return;
        }
        this.mIsWallpaperServiceBound = false;
        Log.v(TAG, "*** Keyguard: can't bind to com.android.systemui.keyguard.KeyguardWallpaperService");
    }

    public synchronized void unbindWallpaperService() {
        if (this.mIsWallpaperServiceBound) {
            this.mContext.unbindService(this.mKeyguardWallpaperConnection);
            this.mIsWallpaperServiceBound = false;
            this.mKeyguardWallpaperService = null;
            Log.v(TAG, "*** Keyguard wallpaper service unbounded");
        } else {
            Log.v(TAG, "*** Keyguard wallpaper service already unbounded");
        }
    }

    public static KeyguardEffectViewController getInstance(Context context) {
        if (sKeyguardEffectViewController == null) {
            sKeyguardEffectViewController = new KeyguardEffectViewController(context);
            Log.i(TAG, "*** KeyguardEffectView create instance ***");
        }
        return sKeyguardEffectViewController;
    }

    public static KeyguardEffectViewController getInstanceIfExists(Context context) {
        Log.i(TAG, "*** KeyguardEffectView getInstanceIfExists ***");
        return sKeyguardEffectViewController;
    }

    public static boolean isLockScreenEffect(int effectType) {
        if (effectType == 0 || effectType == 1 || effectType == 2 || effectType == 3 || effectType == 4 || effectType == 5 || effectType == 6 || effectType == 7 || effectType == 8 || effectType == 9 || effectType == 80 || effectType == 10 || effectType == 100 || effectType == EFFECT_TILT || effectType == 81 || effectType == 82 || effectType == 83 || effectType == 84 || effectType == 85 || effectType == 11 || effectType == 12 || effectType == 13 || effectType == 14 || effectType == 15) {
            return true;
        }
        return false;
    }

    public boolean getIsShowing() {
        return this.mIsShowing;
    }

    private final void setWallpaperContentObservers() {
        this.mContentObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean selfChange, Uri uri) {
                if (uri != null) {
                    if (uri.equals(System.getUriFor("lockscreen_wallpaper"))) {
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                        }
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                    } else if (uri.equals(System.getUriFor("lockscreen_ripple_effect"))) {
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                        }
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                    } else if (uri.equals(System.getUriFor("opne_theme_effect_lockscreen_wallpaper")) || uri.equals(System.getUriFor("current_sec_theme_package_open_theme"))) {
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                        }
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                    } else if (uri.equals(System.getUriFor("lockscreen_wallpaper_path")) || uri.equals(System.getUriFor("lockscreen_wallpaper_path_2"))) {
                        super.onChange(selfChange);
                        KeyguardEffectViewController.this.setWallpaperFileObserver();
                        Log.d(KeyguardEffectViewController.TAG, "mWallpaperPath = " + KeyguardEffectViewController.this.mWallpaperPath);
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_PATH_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_PATH_CHANGED);
                        }
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_PATH_CHANGED);
                    } else if (uri.equals(System.getUriFor("emergency_mode"))) {
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED);
                        }
                        KeyguardEffectViewController.this.mEmergencyModeStateChanged = true;
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED);
                    } else if (uri.equals(System.getUriFor("lockscreen_wallpaper_transparent"))) {
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_PRELOAD_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_PRELOAD_CHANGED);
                        }
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_PRELOAD_CHANGED);
                    } else if (uri.equals(System.getUriFor("lockscreen_zoom_panning_effect"))) {
                        if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED)) {
                            KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                        }
                        KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_TYPE_CHANGED);
                    }
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("lockscreen_ripple_effect"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("lockscreen_wallpaper"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("opne_theme_effect_lockscreen_wallpaper"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("current_sec_theme_package_open_theme"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("lockscreen_wallpaper_path"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("lockscreen_wallpaper_path_2"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("emergency_mode"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("lockscreen_wallpaper_transparent"), false, this.mContentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("lockscreen_zoom_panning_effect"), false, this.mContentObserver, -1);
        setWallpaperFileObserver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_IMAGES_CHANGED);
        filter.addAction(KeyguardEffectViewUtil.ACTION_UPDATE_LOCKSCREEN_WALLPAPER);
        this.mContext.registerReceiver(this.mAdminReceiver, filter);
        this.mRegisterReceiver = true;
    }

    private void setWallpaperFileObserver() {
        this.mWallpaperPath = System.getStringForUser(this.mContext.getContentResolver(), "lockscreen_wallpaper_path", -2);
        Log.d(TAG, "mWallpaperPath = " + this.mWallpaperPath);
        if (this.mWallpaperPath != null) {
            if (this.mWallpaperPath != null && this.mWallpaperPath.contains(SlidingWallpaperPath)) {
                getDataFromSlideshow(this.mContext);
            }
            if (this.mFileObserver != null) {
                this.mFileObserver.stopWatching();
                this.mFileObserver = null;
            }
            this.mFileObserver = new FileObserver(this.mWallpaperPath) {
                public void onEvent(int event, String path) {
                    switch (event) {
                        case 8:
                            break;
                        case 512:
                        case 1024:
                            System.putStringForUser(KeyguardEffectViewController.this.mContext.getContentResolver(), "lockscreen_wallpaper_path", Feature.mLocalSecurity, -2);
                            break;
                        default:
                            return;
                    }
                    Log.i(KeyguardEffectViewController.TAG, "CLOSE_WRITE");
                    if (KeyguardEffectViewController.this.mHandler.hasMessages(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED)) {
                        KeyguardEffectViewController.this.mHandler.removeMessages(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED);
                    }
                    KeyguardEffectViewController.this.mHandler.sendEmptyMessage(KeyguardEffectViewController.MSG_WALLPAPER_FILE_CHANGED);
                }
            };
            this.mFileObserver.startWatching();
        }
    }

    private boolean handleFestivalEffect() {
        if ((!this.mFestivalEffect.isEnabled() && !this.mFestivalEffect.isCommonDayShowFestivalWallpaper()) || SecKeyguardStatusUtils.isUltraPowerSavingMode(this.mContext)) {
            return false;
        }
        KeyguardEffectViewBase festivalView = this.mFestivalEffect.getFestivalView();
        if (festivalView == null) {
            return false;
        }
        if (this.mBackgroundView != festivalView) {
            this.mBackgroundView = festivalView;
            Log.i(TAG, "handleFestivalEffect mIsShowing = " + this.mIsShowing);
            setBackground();
        }
        if (this.mFestivalEffect.isUnlockEffectEnabled()) {
            this.mForegroundView = this.mFestivalEffect.getUnlockEffectView();
            this.mUnlockEffectView = this.mForegroundView;
            this.mCurrentEffect = 85;
            setForeground();
        } else {
            this.mCurrentEffect = 0;
            this.mUnlockEffectView = this.mForegroundCircleView;
        }
        return true;
    }

    private void handleWallpaperTypeChanged() {
        int i = 0;
        if (this.mBackgroundRootLayout != null) {
            this.mCurrentEffect = System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_ripple_effect", 0, -2);
            if (this.mForegroundCircleView == null) {
                this.mForegroundCircleView = new KeyguardEffectViewNone(this.mContext, 0);
            }
            if (this.mForegroundCircleShortcutView == null && this.mNeedTwoCircleView) {
                this.mForegroundCircleShortcutView = new KeyguardEffectViewNone(this.mContext, 1);
            }
            this.mWallpaperType = getDefaultWallpaperTypeForEffect();
            if (handleFestivalEffect()) {
                this.mOldPrimaryEffect = null;
                return;
            }
            int wallpaperType = KeyguardEffectViewUtil.getWallpaperType(this.mContext);
            if (1 != wallpaperType) {
                Global.putInt(this.mContext.getContentResolver(), "white_lockscreen_wallpaper", 0);
                Log.d(TAG, "Remove whiteLockscreenWallpaper value due to !WALLPAPER_TYPE_FILE.");
            }
            switch (wallpaperType) {
                case 0:
                    this.mCurrentEffect = -1;
                    this.mBackgroundRootLayout.setBackgroundColor(0);
                    break;
                case 2:
                    this.mCurrentEffect = 100;
                    this.mBackgroundRootLayout.setBackgroundColor(0);
                    break;
                case 3:
                    this.mCurrentEffect = -2;
                    this.mBackgroundRootLayout.setBackgroundColor(0);
                    break;
                default:
                    FrameLayout frameLayout = this.mBackgroundRootLayout;
                    if (!this.mWallpaperProcessSeparated) {
                        i = ViewCompat.MEASURED_STATE_MASK;
                    }
                    frameLayout.setBackgroundColor(i);
                    break;
            }
            handleSetGradationLayer();
            if (this.mOldEffect != this.mCurrentEffect) {
                this.mOldEffect = this.mCurrentEffect;
                reloadLockSound();
            }
            makeEffectViews(this.mCurrentEffect);
            setLayerAndBitmapForPoppingColorEffect();
            updateAttributionInfoView();
        }
    }

    public boolean isJustLockLiveEnabled() {
        return this.mCurrentEffect == 100;
    }

    private void handleChargeStateChange() {
        if (this.mFestivalEffect.isChargeState()) {
            if (this.mBgHasAddChargeView) {
                this.mBackgroundRootLayout.removeView((View) this.mChargeView);
                this.mBgHasAddChargeView = false;
                this.mChargeView = null;
            }
            this.mChargeView = this.mFestivalEffect.getChargeEffectView();
            if (this.mChargeView != null) {
                if (this.mBackgroundView == null) {
                    this.mBackgroundRootLayout.addView((View) this.mChargeView, 0);
                } else {
                    this.mBackgroundRootLayout.addView((View) this.mChargeView, 1);
                }
                this.mBgHasAddChargeView = true;
            }
        } else if (this.mBgHasAddChargeView) {
            this.mBackgroundRootLayout.removeView((View) this.mChargeView);
            this.mBgHasAddChargeView = false;
            this.mChargeView = null;
        }
    }

    public boolean isZoomPanningEffectEnabled() {
        return KeyguardEffectViewUtil.isZoomPanningEffectEnabled(this.mContext);
    }

    public boolean isRichLockWallpaper() {
        if (this.mWallpaperPath == null) {
            return false;
        }
        boolean isRichLock = this.mWallpaperPath.startsWith(RICH_LOCK_WALLPAPER_ROOT_PATH);
        Log.d(TAG, "isRichLockWallpaper() = " + isRichLock);
        return isRichLock;
    }

    public boolean isCategoriesWallpaper() {
        boolean z = true;
        if (this.mWallpaperPath == null) {
            return false;
        }
        boolean isLiveWallpaper;
        boolean isHomeWallpaper;
        boolean isCategoriesWallpaper = this.mWallpaperPath.startsWith(RICH_LOCK_CATEGORIES_WALLPAPER_ROOT_PATH);
        if (this.mCurrentEffect == -1) {
            isLiveWallpaper = true;
        } else {
            isLiveWallpaper = false;
        }
        if (this.mCurrentEffect == -2) {
            isHomeWallpaper = true;
        } else {
            isHomeWallpaper = false;
        }
        Log.d(TAG, "isCategoriesWallpaper = " + isCategoriesWallpaper + ", isLiveWallpaper = " + isLiveWallpaper + "isHomeWallpaper = " + isHomeWallpaper);
        if (!isCategoriesWallpaper || isLiveWallpaper || isHomeWallpaper) {
            z = false;
        }
        return z;
    }

    private void setForeground() {
        if (this.mForegroundRootLayout != null) {
            this.mForegroundRootLayout.removeAllViews();
            if (this.mForegroundView != null) {
                this.mForegroundRootLayout.addView((View) this.mForegroundView, -1, -1);
                if (this.mForegroundCircleShortcutView != null) {
                    this.mForegroundRootLayout.addView((View) this.mForegroundCircleShortcutView, -1, -1);
                    return;
                } else if (this.mForegroundView != this.mForegroundCircleView) {
                    this.mForegroundRootLayout.addView((View) this.mForegroundCircleView, -1, -1);
                    return;
                } else {
                    return;
                }
            }
            this.mForegroundRootLayout.addView((View) this.mForegroundCircleView, -1, -1);
            if (this.mForegroundCircleShortcutView != null) {
                this.mForegroundRootLayout.addView((View) this.mForegroundCircleShortcutView, -1, -1);
            }
        }
    }

    private void setBackground() {
        if (this.mChargeView != null) {
            this.mChargeView.cleanUp();
        }
        this.mBackgroundRootLayout.removeAllViews();
        if (this.mBackgroundView != null) {
            this.mBackgroundRootLayout.addView((View) this.mBackgroundView, -1, -1);
            handleSetGradationLayer();
        }
        if (KeyguardProperties.isSupportBlendedFilter()) {
            if (this.mVignetting == null) {
                this.mVignetting = new KeyguardEffectViewVignetting(this.mContext);
            }
            Log.d(TAG, "Apply BlendedFilter from setBackground()");
            this.mBackgroundRootLayout.addView(this.mVignetting, -1, -1);
            if (System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_wallpaper_transparent", 1, -2) == 1) {
                this.mVignetting.resetBlendedFilter();
            } else {
                this.mVignetting.applyBlendedFilter(KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext).getBitmap());
            }
        }
        setWallpaperSlider(shouldShowAttributionInfoView());
        if (this.mFestivalEffect.isChargeState()) {
            this.mChargeView = this.mFestivalEffect.getChargeEffectView();
            if (this.mChargeView != null) {
                this.mBackgroundRootLayout.addView((View) this.mChargeView, -1, -1);
                this.mBgHasAddChargeView = true;
            }
        }
    }

    private void handleWallpaperImageChanged() {
        if ((!this.mMusicBackgroundSet || (!isRichLockWallpaper() && !isCategoriesWallpaper())) && this.mBackgroundView != null) {
            if (!isRichLockWallpaper()) {
                System.putIntForUser(this.mContext.getContentResolver(), "lockscreen_zoom_panning_effect", 0, -2);
            }
            if (!shouldShowAttributionInfoView()) {
                setWallpaperSlider(false);
            } else if (this.mRichSliderView == null || isZoomPanningEffectEnabled()) {
                setWallpaperSlider(true);
            } else {
                this.mRichSliderView.handleShowSlideEffect();
            }
            if (KeyguardProperties.isBrilliantCutSpecialTypeEnabled()) {
                changeEffectType();
            }
            handleWallpaperTypeChanged();
            updateMontblancEffectType();
            if (this.mBackgroundView != null) {
                this.mBackgroundView.update();
            }
        }
    }

    public void setLiveWallpaperBg(Bitmap bmp) {
        int wallpaperType = KeyguardEffectViewUtil.getWallpaperType(this.mContext);
        if (wallpaperType == 0 || wallpaperType == 2) {
            Log.d(TAG, "setLiveWallpaperBg = " + (bmp != null));
            ((EffectBehindView) this.mBackgroundRootLayout).setLiveWallpaperBg(bmp);
        }
    }

    public void setWallpaperSlider(boolean state) {
        if (!state || this.mWallpaperProcessSeparated) {
            if (this.mRichSliderView != null) {
                Log.d(TAG, "remove WallpaperSlider.");
                this.mBackgroundRootLayout.removeView(this.mRichSliderView);
                this.mRichSliderView = null;
                WallpaperWidgetController.getInstance(this.mContext).notifyWallpaperStateChanged();
            }
        } else if (this.mRichSliderView == null) {
            Log.d(TAG, "add WallpaperSlider.");
            this.mRichSliderView = new KeyguardEffectViewWallpaperSlider(this.mContext);
            this.mBackgroundRootLayout.addView(this.mRichSliderView, -1, -1);
            WallpaperWidgetController.getInstance(this.mContext).notifyWallpaperStateChanged();
        }
    }

    public void handleUpdateKeyguardMusicBackground(Bitmap bmp) {
        Log.d(TAG, "handleUpdateKeyguardMusicBackground(): bmp=" + bmp);
        if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            try {
                this.mKeyguardWallpaperService.setContextualWallpaper(bmp, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while setContextualWallpaper()");
            }
        }
        this.mMusicBackgroundSet = true;
        if (this.mBackgroundView != null) {
            updateAttributionInfoView();
            this.mBackgroundView.setContextualWallpaper(bmp);
        }
    }

    public void updateMontblancEffectType() {
        if (this.mBackgroundView instanceof KeyguardEffectViewIndigoDiffusion) {
            int type = 0;
            if (System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_montblanc_wallpaper", 0, -2) == 2) {
                type = 1;
            }
            ((KeyguardEffectViewIndigoDiffusion) this.mBackgroundView).settingsForImageType(type);
        }
    }

    public void show() {
        Log.d(TAG, "show()");
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
        if (this.mWallpaperProcessSeparated) {
            handleWallpaperTypeChanged();
            bindWallpaperService();
            if (this.mKeyguardWallpaperService != null) {
                try {
                    this.mKeyguardWallpaperService.executeCommand(1, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException occured while handing screenTurnedOff()");
                }
            }
        }
        this.mFestivalEffect.SetShowState(true);
        if (this.mForegroundView != null) {
            this.mForegroundView.show();
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.show();
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.show();
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.show();
        }
        if (this.mFestivalEffect.isChargeState() && this.mFestivalEffect.GetScreenState() && this.mChargeView != null) {
            this.mChargeView.show();
            this.mFestivalEffect.pauseAnimation();
        }
    }

    public void cleanUp() {
        Log.d(TAG, "cleanUp()");
        this.mFestivalEffect.SetShowState(false);
        if (this.mForegroundView != null) {
            this.mForegroundView.cleanUp();
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.cleanUp();
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.cleanUp();
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.cleanUp();
        }
        if (this.mChargeView != null) {
            this.mChargeView.cleanUp();
        }
        if (this.mRichSliderView != null) {
            this.mRichSliderView.cleanUp();
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
    }

    public void reset() {
        Log.d(TAG, "reset()");
        if (this.mWallpaperProcessSeparated) {
            bindWallpaperService();
            if (this.mKeyguardWallpaperService != null) {
                try {
                    this.mKeyguardWallpaperService.executeCommand(2, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException occured while handing screenTurnedOff()");
                }
            }
        }
        if (this.mForegroundView != null) {
            this.mForegroundView.reset();
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.reset();
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.reset();
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.reset();
        }
        if (this.mNeedUpdateEffectScreen) {
            Log.d(TAG, "Update screen by User change");
            if (this.mForegroundView != null) {
                this.mForegroundView.update();
            }
            if (this.mForegroundCircleView != null) {
                this.mForegroundCircleView.update();
            }
            if (this.mForegroundCircleShortcutView != null) {
                this.mForegroundCircleShortcutView.update();
            }
            if (this.mBackgroundView != null) {
                this.mBackgroundView.update();
            }
            this.mNeedUpdateEffectScreen = false;
        }
    }

    public void screenTurnedOn() {
        if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            try {
                this.mKeyguardWallpaperService.executeCommand(5, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while handing screenTurnedOn()");
            }
        }
        this.mFestivalEffect.SetScreenState(true);
        if (this.mForegroundView != null) {
            this.mForegroundView.screenTurnedOn();
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.screenTurnedOn();
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.screenTurnedOn();
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.screenTurnedOn();
        }
        if (this.mFestivalEffect.isChargeState() && this.mChargeView != null) {
            this.mChargeView.screenTurnedOn();
            this.mFestivalEffect.pauseAnimation();
        }
        if (this.mRichSliderView != null) {
            this.mRichSliderView.screenTurnedOn();
        }
    }

    public long getUnlockDelay() {
        long j = 0;
        if (this.mUnlockEffectView != null) {
            return this.mUnlockEffectView.getUnlockDelay();
        }
        if (!this.mWallpaperProcessSeparated || this.mKeyguardWallpaperService == null) {
            return j;
        }
        try {
            return this.mKeyguardWallpaperService.getUnlockDelay();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException occured while handing getUnlockDelay()");
            return j;
        }
    }

    public void playLockSound() {
        if (this.mUnlockEffectView != null) {
            this.mUnlockEffectView.playLockSound();
        }
    }

    public void screenTurnedOff() {
        if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            this.mScreenOnCallback = null;
            try {
                this.mKeyguardWallpaperService.executeCommand(6, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while handing screenTurnedOff()");
            }
        }
        this.mFestivalEffect.SetScreenState(false);
        if (this.mForegroundView != null) {
            this.mForegroundView.screenTurnedOff();
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.screenTurnedOff();
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.screenTurnedOff();
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.screenTurnedOff();
        }
        if (this.mChargeView != null) {
            this.mChargeView.screenTurnedOff();
        }
        if (this.mRichSliderView != null) {
            this.mRichSliderView.screenTurnedOff();
        }
    }

    public void update() {
        if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            try {
                this.mKeyguardWallpaperService.executeCommand(4, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while handing update()");
            }
        }
        if (this.mForegroundView != null) {
            this.mForegroundView.update();
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.update();
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.update();
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.update();
        }
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        if (KeyguardProperties.isMovableAffordance() && this.mVisibleNotificationBottom > 0) {
            Log.d(TAG, "showUnlockAffordance = " + this.mVisibleNotificationBottom);
            rect.top = this.mVisibleNotificationBottom;
        }
        if (this.mWallpaperProcessSeparated) {
            this.mAffordanceRect.set(rect);
            if (this.mKeyguardWallpaperService != null) {
                try {
                    this.mKeyguardWallpaperService.showUnlockAffordance(startDelay, this.mAffordanceRect, 0);
                    this.mAffordanceRect.set(0, 0, 0, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException occured while handing showUnlockAffordance()");
                }
            }
        }
        if (this.mUnlockEffectView != null) {
            this.mUnlockEffectView.showUnlockAffordance(startDelay, rect);
        }
    }

    public void handleUnlock(View view, MotionEvent event) {
        if (this.mUnlockEffectView != null) {
            this.mUnlockEffectView.handleUnlock(view, event);
        } else if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            try {
                this.mKeyguardWallpaperService.handleUnlock(event);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while handing handleUnlock()");
            }
        }
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        if (this.mUnlockEffectView != null) {
            return this.mUnlockEffectView.handleTouchEvent(view, event);
        }
        if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            try {
                this.mKeyguardWallpaperService.sendTouchEvent(MotionEvent.obtain(event));
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while handing handleTouchEvent()");
            }
        }
        return true;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        if (this.mUnlockEffectView != null) {
            return this.mUnlockEffectView.handleTouchEventForPatternLock(view, event);
        }
        return false;
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        if (this.mUnlockEffectView == null || !(this.mUnlockEffectView instanceof KeyguardEffectViewRippleInk)) {
            return false;
        }
        return ((KeyguardEffectViewRippleInk) this.mUnlockEffectView).handleHoverEvent(view, event);
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
    }

    public boolean isBackgroundEffect() {
        return false;
    }

    public String getCounterEffectName() {
        return null;
    }

    public boolean handleSensorEvent(SensorEvent event) {
        return this.mMusicBackgroundSet ? false : false;
    }

    public FrameLayout getForegroundLayout() {
        return this.mForegroundRootLayout;
    }

    public FrameLayout getBackgroundLayout() {
        return this.mBackgroundRootLayout;
    }

    public static String getSlidingWallpaperPath(Context context) {
        String ret;
        if (uriArray != null) {
            Log.d(TAG, "mSlidingWallpaperIndex = " + mSlidingWallpaperIndex + " , uriArray.size(): " + uriArray.size());
            if (mSlidingWallpaperIndex >= uriArray.size()) {
                mSlidingWallpaperIndex = 0;
            }
            ret = (String) uriArray.get(mSlidingWallpaperIndex);
            if (!new File(ret).exists()) {
                ret = EMPTY_WALLPAPER_IMAGE_PATH;
            }
        } else {
            ret = EMPTY_WALLPAPER_IMAGE_PATH;
        }
        Log.d(TAG, "getSlidingWallpaperPath = " + ret);
        return ret;
    }

    private void chenckSlidingWallpaperByScreenon() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_IMAGES_CHANGED);
        filter.addAction(KeyguardEffectViewUtil.ACTION_UPDATE_LOCKSCREEN_WALLPAPER);
        if (KeyguardProperties.isLegacyUX()) {
            filter.addAction(ACTION_LOCKSCREEN_IMAGE_CHANGED);
        }
        this.mContext.registerReceiver(this.mAdminReceiver, filter);
        this.mRegisterReceiver = true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean setSlidingWallpaperInfo(android.content.Context r18, android.content.Intent r19) {
        /*
        r12 = 0;
        r0 = r19.getAction();
        r13 = "KeyguardEffectViewController";
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = "mSlidingInterval = ";
        r14 = r14.append(r15);
        r15 = mSlidingInterval;
        r14 = r14.append(r15);
        r15 = " , mSlidingWallpaperIndex:";
        r14 = r14.append(r15);
        r15 = mSlidingWallpaperIndex;
        r14 = r14.append(r15);
        r14 = r14.toString();
        android.util.Log.d(r13, r14);
        r8 = 0;
        r13 = mSlidingInterval;
        switch(r13) {
            case 0: goto L_0x0068;
            case 1: goto L_0x0072;
            case 2: goto L_0x007a;
            case 3: goto L_0x0082;
            default: goto L_0x0032;
        };
    L_0x0032:
        if (r12 == 0) goto L_0x0043;
    L_0x0034:
        r13 = mSlidingWallpaperIndex;
        r13 = r13 + 1;
        mSlidingWallpaperIndex = r13;
        r13 = mSlidingWallpaperIndex;
        r14 = mSlidingTotalCount;
        if (r13 < r14) goto L_0x0043;
    L_0x0040:
        r13 = 0;
        mSlidingWallpaperIndex = r13;
    L_0x0043:
        r13 = "KeyguardEffectViewController";
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = "results = ";
        r14 = r14.append(r15);
        r14 = r14.append(r12);
        r15 = " , mSlidingWallpaperIndex:";
        r14 = r14.append(r15);
        r15 = mSlidingWallpaperIndex;
        r14 = r14.append(r15);
        r14 = r14.toString();
        android.util.Log.d(r13, r14);
        return r12;
    L_0x0068:
        r13 = "android.intent.action.SCREEN_OFF";
        r13 = r0.equals(r13);
        if (r13 == 0) goto L_0x0032;
    L_0x0070:
        r12 = 1;
        goto L_0x0032;
    L_0x0072:
        r14 = 0;
        r13 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));
        if (r13 != 0) goto L_0x007a;
    L_0x0078:
        r8 = 60;
    L_0x007a:
        r14 = 0;
        r13 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));
        if (r13 != 0) goto L_0x0082;
    L_0x0080:
        r8 = 720; // 0x2d0 float:1.009E-42 double:3.557E-321;
    L_0x0082:
        r14 = 0;
        r13 = (r8 > r14 ? 1 : (r8 == r14 ? 0 : -1));
        if (r13 != 0) goto L_0x008a;
    L_0x0088:
        r8 = 1440; // 0x5a0 float:2.018E-42 double:7.115E-321;
    L_0x008a:
        r14 = 60;
        r14 = r14 * r8;
        r16 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r8 = r14 * r16;
        r13 = "android.intent.action.SCREEN_ON";
        r13 = r0.equals(r13);
        if (r13 == 0) goto L_0x00f7;
    L_0x0099:
        r14 = java.lang.System.currentTimeMillis();
        r16 = mSlidingScreenOffTime;
        r14 = r14 - r16;
        r10 = java.lang.Math.abs(r14);
        r14 = mSlidingScreenOffTime;
        r16 = mSlidingTime;
        r13 = (r14 > r16 ? 1 : (r14 == r16 ? 0 : -1));
        if (r13 <= 0) goto L_0x00e8;
    L_0x00ad:
        r14 = mSlidingScreenOffTime;
        r16 = mSlidingTime;
        r14 = r14 - r16;
        r14 = java.lang.Math.abs(r14);
        r14 = r14 % r8;
        r10 = r10 + r14;
    L_0x00b9:
        r13 = "KeyguardEffectViewController";
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = "(minutesCount ";
        r14 = r14.append(r15);
        r14 = r14.append(r10);
        r15 = " minuteThreshold ";
        r14 = r14.append(r15);
        r14 = r14.append(r8);
        r15 = ")";
        r14 = r14.append(r15);
        r14 = r14.toString();
        android.util.Log.d(r13, r14);
        r13 = (r10 > r8 ? 1 : (r10 == r8 ? 0 : -1));
        if (r13 < 0) goto L_0x0032;
    L_0x00e5:
        r12 = 1;
        goto L_0x0032;
    L_0x00e8:
        r14 = mSlidingScreenOffTime;
        r16 = mSlidingTime;
        r14 = r14 - r16;
        r14 = java.lang.Math.abs(r14);
        r14 = r14 % r8;
        r14 = r8 - r14;
        r10 = r10 + r14;
        goto L_0x00b9;
    L_0x00f7:
        r13 = "android.intent.action.TIME_TICK";
        r13 = r0.equals(r13);
        if (r13 == 0) goto L_0x0032;
    L_0x00ff:
        r1 = java.util.Calendar.getInstance();
        r14 = java.lang.System.currentTimeMillis();
        r1.setTimeInMillis(r14);
        r13 = new java.text.SimpleDateFormat;
        r14 = "HH";
        r13.<init>(r14);
        r14 = r1.getTime();
        r2 = r13.format(r14);
        r13 = new java.text.SimpleDateFormat;
        r14 = "hh";
        r13.<init>(r14);
        r14 = r1.getTime();
        r3 = r13.format(r14);
        r13 = new java.text.SimpleDateFormat;
        r14 = "mm";
        r13.<init>(r14);
        r14 = r1.getTime();
        r7 = r13.format(r14);
        r13 = java.lang.Integer.valueOf(r2);
        r4 = r13.intValue();
        r13 = java.lang.Integer.valueOf(r3);
        r5 = r13.intValue();
        r13 = java.lang.Integer.valueOf(r7);
        r6 = r13.intValue();
        r13 = "KeyguardEffectViewController";
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = " ";
        r14 = r14.append(r15);
        r14 = r14.append(r4);
        r15 = " , : ";
        r14 = r14.append(r15);
        r14 = r14.append(r6);
        r15 = " , ";
        r14 = r14.append(r15);
        r15 = mSlidingHour;
        r14 = r14.append(r15);
        r15 = ",  :";
        r14 = r14.append(r15);
        r15 = mSlidingMin;
        r14 = r14.append(r15);
        r14 = r14.toString();
        android.util.Log.d(r13, r14);
        r13 = mSlidingInterval;
        r14 = 1;
        if (r13 != r14) goto L_0x0195;
    L_0x018e:
        r13 = mSlidingMin;
        if (r6 != r13) goto L_0x0032;
    L_0x0192:
        r12 = 1;
        goto L_0x0032;
    L_0x0195:
        r13 = mSlidingInterval;
        r14 = 2;
        if (r13 != r14) goto L_0x01a5;
    L_0x019a:
        r13 = mSlidingHour;
        if (r5 != r13) goto L_0x0032;
    L_0x019e:
        r13 = mSlidingMin;
        if (r6 != r13) goto L_0x0032;
    L_0x01a2:
        r12 = 1;
        goto L_0x0032;
    L_0x01a5:
        r13 = mSlidingInterval;
        r14 = 3;
        if (r13 != r14) goto L_0x0032;
    L_0x01aa:
        r13 = mSlidingHour;
        if (r4 != r13) goto L_0x0032;
    L_0x01ae:
        r13 = mSlidingMin;
        if (r6 != r13) goto L_0x0032;
    L_0x01b2:
        r12 = 1;
        goto L_0x0032;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.sec.effect.KeyguardEffectViewController.setSlidingWallpaperInfo(android.content.Context, android.content.Intent):boolean");
    }

    private void getDataFromSlideshow(Context context) {
        boolean isEmergencyMode = System.getIntForUser(context.getContentResolver(), "emergency_mode", 0, -2) == 1;
        boolean isUltraPowerSavingMode = System.getIntForUser(context.getContentResolver(), "ultra_powersaving_mode", 0, -2) == 1;
        Log.d(TAG, "EMMode : " + isEmergencyMode + ",UPSMode :" + isUltraPowerSavingMode);
        if (isEmergencyMode || isUltraPowerSavingMode) {
            uriArray = null;
            return;
        }
        Bundle bundle = context.getContentResolver().call(Uri.parse("content://com.samsung.slidinggallery"), "getData", null, null);
        if (bundle != null) {
            mSlidingInterval = bundle.getInt("interval");
            mSlidingTotalCount = bundle.getInt("imgCount");
            mSlidingTime = bundle.getLong(Constants.REMOTE_VIEW_INFO_TIME);
            mSlidingHour = bundle.getInt("hour");
            mSlidingMin = bundle.getInt("minute");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mSlidingTime);
            String hourText = new SimpleDateFormat("HH").format(calendar.getTime());
            String minuteText = new SimpleDateFormat("mm").format(calendar.getTime());
            if (mSlidingInterval == 2) {
                hourText = new SimpleDateFormat("hh").format(calendar.getTime());
            }
            mSlidingHour = Integer.valueOf(hourText).intValue();
            mSlidingMin = Integer.valueOf(minuteText).intValue();
            uriArray = bundle.getStringArrayList("uriArray");
            Log.d(TAG, "interval: " + mSlidingInterval);
            Log.d(TAG, "imgCount: " + mSlidingTotalCount);
            Log.d(TAG, "mSlidingTime: " + mSlidingTime + " ,Hour :" + mSlidingHour + " ,Min:" + mSlidingMin);
            if (uriArray != null) {
                Iterator i$ = uriArray.iterator();
                while (i$.hasNext()) {
                    Log.d(TAG, "uriStr: " + ((String) i$.next()));
                }
                return;
            }
            return;
        }
        uriArray = null;
    }

    public static BitmapDrawable getScaledBitmapDrawable(Context context) {
        String tempPath = getSlidingWallpaperPath(context);
        Log.d(TAG, "getScaledBitmapDrawable Path = " + tempPath);
        if (tempPath.contains(EMPTY_WALLPAPER_IMAGE_PATH)) {
            return new BitmapDrawable(context.getResources(), context.getResources().openRawResource(C0302R.drawable.intro_bg));
        }
        InputStream inputStream;
        BitmapDrawable tempBitmapDrawable;
        try {
            File mdmFile = new File(tempPath);
            InputStream sis = new FileInputStream(mdmFile);
            try {
                mOrientation = getSlidingWallpaperDegree(tempPath);
                Options opts = new Options();
                opts.inSampleSize = 4;
                Bitmap tempBitmap = BitmapFactory.decodeStream(sis, null, opts);
                if (mOrientation > 0) {
                    tempBitmap = adjustPhotoRotation(tempBitmap, mOrientation);
                }
                Log.d(TAG, "getScaledBitmapDrawable File = " + mdmFile.exists() + " ,canRead() : " + mdmFile.canRead() + " tempBitmap:" + tempBitmap + " displayWidth=" + displayWidth + " displayHeight=" + displayHeight);
                if (tempBitmap != null && mdmFile.exists() && mdmFile.canRead()) {
                    tempBitmap = Bitmap.createScaledBitmap(tempBitmap, displayWidth, displayHeight, true);
                    inputStream = sis;
                } else {
                    inputStream = context.getResources().openRawResource(C0302R.drawable.intro_bg);
                    tempBitmap = new BitmapDrawable(inputStream).getBitmap();
                }
                tempBitmapDrawable = new BitmapDrawable(tempBitmap);
            } catch (IOException e) {
                inputStream = sis;
                inputStream = context.getResources().openRawResource(C0302R.drawable.intro_bg);
                tempBitmapDrawable = new BitmapDrawable(context.getResources(), inputStream);
                inputStream.close();
                return tempBitmapDrawable;
            }
        } catch (IOException e2) {
            inputStream = context.getResources().openRawResource(C0302R.drawable.intro_bg);
            tempBitmapDrawable = new BitmapDrawable(context.getResources(), inputStream);
            inputStream.close();
            return tempBitmapDrawable;
        }
        try {
            inputStream.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return tempBitmapDrawable;
    }

    private static int getSlidingWallpaperDegree(String path) {
        try {
            switch (new ExifInterface(path).getAttributeInt("Orientation", 1)) {
                case 3:
                    return 180;
                case 6:
                    return 90;
                case 8:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static Bitmap adjustPhotoRotation(Bitmap bm, int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate((float) orientationDegree, ((float) bm.getWidth()) / 2.0f, ((float) bm.getHeight()) / 2.0f);
        try {
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    public void setHidden(boolean isHidden) {
        boolean z = false;
        Log.i(TAG, "setHidden = " + isHidden);
        if (!(!this.mWallpaperProcessSeparated || isHidden || this.mKeyguardWallpaperService == null)) {
            try {
                this.mKeyguardWallpaperService.executeCommand(7, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while handing setHidden(false)");
            }
        }
        if (this.mForegroundView != null) {
            this.mForegroundView.setHidden(isHidden);
        }
        if (this.mForegroundCircleView != null) {
            this.mForegroundCircleView.setHidden(isHidden);
        }
        if (this.mForegroundCircleShortcutView != null) {
            this.mForegroundCircleShortcutView.setHidden(isHidden);
        }
        if (this.mBackgroundView != null) {
            this.mBackgroundView.setHidden(isHidden);
        }
        if (!isHidden) {
            z = true;
        }
        this.mIsVisible = z;
    }

    public void setLayerAndBitmapForPoppingColorEffect() {
        if (this.mBackgroundView instanceof KeyguardEffectViewZoomPanning) {
            this.mBackgroundView.setLayers(this.mNotificationPanel);
        }
    }

    public void removeMusicWallpaper() {
        if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
            try {
                if (this.mKeyguardWallpaperService != null) {
                    this.mKeyguardWallpaperService.setContextualWallpaper(null, 0);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while setContextualWallpaper()");
            }
        }
        this.mMusicBackgroundSet = false;
        updateAttributionInfoView();
        if (this.mBackgroundView instanceof KeyguardEffectViewZoomPanning) {
            ((KeyguardEffectViewZoomPanning) this.mBackgroundView).removeMusicWallpaper();
        }
        int changedDefaultWallpaperType = getDefaultWallpaperTypeForEffect();
    }

    public int getDefaultWallpaperTypeForEffect() {
        return System.getIntForUser(this.mContext.getContentResolver(), SETTING_KEYGUARD_DEFAULT_WALLPAPER_TYPE_FOR_EFFECT, 1, -2);
    }

    public void changeEffectType() {
        Log.d(TAG, "changeEffectType()");
        if (this.mEmergencyModeStateChanged) {
            this.mEmergencyModeStateChanged = false;
            return;
        }
        int changedDefaultWallpaperType = System.getIntForUser(this.mContext.getContentResolver(), SETTING_KEYGUARD_SET_DEFAULT_WALLPAPER, 1, -2);
        if (KeyguardProperties.isBrilliantCutSpecialTypeEnabled() && (this.mBackgroundView instanceof KeyguardEffectViewBrilliantCut)) {
            ((KeyguardEffectViewBrilliantCut) this.mBackgroundView).settingsForImageType(changedDefaultWallpaperType);
        }
        System.putIntForUser(this.mContext.getContentResolver(), SETTING_KEYGUARD_DEFAULT_WALLPAPER_TYPE_FOR_EFFECT, changedDefaultWallpaperType, -2);
    }

    public void changeWallpaperByScreenOff() {
        Log.d(TAG, "changeWallpaperByScreenOff()");
        if (this.mWallpaperPath != null && this.mWallpaperPath.contains(SlidingWallpaperPath)) {
            Intent intent = new Intent("android.intent.action.SCREEN_OFF");
            Log.d(TAG, "change sliding wallpaper()");
            if (setSlidingWallpaperInfo(this.mContext, intent)) {
                if (this.mHandler.hasMessages(MSG_WALLPAPER_FILE_CHANGED)) {
                    this.mHandler.removeMessages(MSG_WALLPAPER_FILE_CHANGED);
                }
                this.mHandler.sendEmptyMessage(MSG_WALLPAPER_FILE_CHANGED);
            }
        }
    }

    public boolean isMusicBackgroundSet() {
        return this.mMusicBackgroundSet;
    }

    public boolean shouldShowAttributionInfoView() {
        if (SecKeyguardStatusUtils.isUltraPowerSavingMode(this.mContext) || !isCategoriesWallpaper() || isJustLockLiveEnabled()) {
            return false;
        }
        return true;
    }

    public void setAttributionInfoView(SecAttributionInfoView view) {
        this.mAttributionInfoView = view;
        updateAttributionInfoView();
    }

    public void updateAttributionInfoView() {
        if (this.mAttributionInfoView != null) {
            this.mAttributionInfoView.update();
        }
    }

    public void setEffectLayout(View background, View foreground, View panel, View previewContainer) {
        this.mBackgroundRootLayout = (FrameLayout) background;
        this.mForegroundRootLayout = (FrameLayout) foreground;
        this.mNotificationPanel = (FrameLayout) panel;
        if (previewContainer != null) {
            this.mPreviewContainer = (KeyguardPreviewContainer) previewContainer;
        }
        handleWallpaperTypeChanged();
    }

    public void setKeyguardShowing(boolean isActuallyShowing, boolean isShowing) {
        int visibility = 8;
        Log.i(TAG, "setKeyguardShowing = " + isActuallyShowing);
        if (isActuallyShowing) {
            visibility = 0;
        }
        this.mBackgroundRootLayout.setVisibility(visibility);
        this.mForegroundRootLayout.setVisibility(visibility);
        if (isActuallyShowing) {
            if (this.mIsShowing) {
                reset();
            } else {
                show();
                setHidden(false);
            }
            if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
                try {
                    this.mKeyguardWallpaperService.executeCommand(8, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException occured while COMMAND_SET_WINDOW_ANIM");
                }
            }
        } else {
            if (this.mWallpaperProcessSeparated) {
                if (isShowing) {
                    if (this.mKeyguardWallpaperService != null) {
                        try {
                            this.mKeyguardWallpaperService.executeCommand(3, 0);
                        } catch (RemoteException e2) {
                            Log.e(TAG, "RemoteException occured while handing screenTurnedOff()");
                        }
                    }
                    bindWallpaperService();
                } else if (this.mScreenOnCallback != null) {
                    Log.w(TAG, "setKeyguardShowing: **** SHOWN CALLED to turn on forcingly ****");
                    this.mScreenOnCallback.screenOn();
                }
            }
            cleanUp();
        }
        this.mIsShowing = isActuallyShowing;
        setFestivalKeyguardShowing(isActuallyShowing, visibility);
    }

    public void setFestivalKeyguardShowing(boolean showing, int visibility) {
        Log.i(TAG, "setFestivalKeyguardShowing = " + showing + " ,:" + isFestivalActivated());
        if (isFestivalActivated() && this.mBackgroundView != null) {
            ViewGroup convertedView = this.mBackgroundView;
            for (int childIdx = 0; childIdx < convertedView.getChildCount(); childIdx++) {
                View childView = convertedView.getChildAt(childIdx);
                if (childView != null) {
                    childView.setVisibility(visibility);
                }
            }
            convertedView.setVisibility(visibility);
            convertedView.invalidate();
        }
    }

    public void setLockSoundChangeCallback(LockSoundChangeCallback callback) {
        this.mLockSoundChangeCallback = callback;
        reloadLockSound();
    }

    public void reloadLockSound() {
        LockSoundInfo info;
        Log.d(TAG, "reloadLockSound()");
        if (!isFestivalActivated()) {
            switch (this.mCurrentEffect) {
                case 1:
                case 7:
                case 10:
                    info = KeyguardEffectViewRippleInk.getLockSoundInfo();
                    break;
                case 2:
                    info = KeyguardEffectViewLensFlare.getLockSoundInfo();
                    break;
                case 3:
                    info = KeyguardEffectViewParticleSpace.getLockSoundInfo();
                    break;
                case 4:
                    info = KeyguardEffectViewWaterColor.getLockSoundInfo();
                    break;
                case 5:
                    info = KeyguardEffectViewBlind.getLockSoundInfo();
                    break;
                case 8:
                    info = KeyguardEffectViewBrilliantRing.getLockSoundInfo();
                    break;
                case 9:
                    info = KeyguardEffectViewBrilliantCut.getLockSoundInfo();
                    break;
                case 11:
                    info = KeyguardEffectViewAbstractTile.getLockSoundInfo();
                    break;
                case 12:
                    info = KeyguardEffectViewGeometricMosaic.getLockSoundInfo();
                    break;
                case 13:
                    info = KeyguardEffectViewWaterDroplet.getLockSoundInfo();
                    break;
                case 14:
                    info = KeyguardEffectViewSparklingBubbles.getLockSoundInfo();
                    break;
                case 15:
                    info = KeyguardEffectViewColourDroplet.getLockSoundInfo();
                    break;
                case EFFECT_SPRING /*81*/:
                case EFFECT_SUMMER /*82*/:
                case EFFECT_AUTUMN /*83*/:
                case EFFECT_WINTER /*84*/:
                    info = new LockSoundInfo(getSeasonalLockSoundPath(this.mCurrentEffect, true), getSeasonalLockSoundPath(this.mCurrentEffect, false));
                    break;
                case EFFECT_SEASONAL /*85*/:
                    int season = getCurrentSeasonEffect();
                    info = new LockSoundInfo(getSeasonalLockSoundPath(season, true), getSeasonalLockSoundPath(season, false));
                    break;
                default:
                    info = KeyguardEffectViewNone.getLockSoundInfo();
                    break;
            }
        }
        switch (this.mCurrentEffect) {
            case EFFECT_SEASONAL /*85*/:
                season = getCurrentSeasonEffect();
                info = new LockSoundInfo(getSeasonalLockSoundPath(season, true), getSeasonalLockSoundPath(season, false));
                break;
            default:
                info = KeyguardEffectViewNone.getLockSoundInfo();
                break;
        }
        if (this.mLockSoundChangeCallback != null) {
            this.mLockSoundChangeCallback.reloadLockSound(info);
        }
    }

    public boolean isFestivalActivated() {
        Log.d(TAG, "isFestivalActivated()" + this.mFestivalEffectEnabled);
        if (this.mFestivalEffectEnabled && this.mFestivalEffect != null && this.mFestivalEffect.isActivated()) {
            return true;
        }
        return false;
    }

    private int getCurrentSeasonEffect() {
        int season = 0;
        Time time = new Time("GMT+8");
        time.setToNow();
        int year = time.year;
        int month = time.month + 1;
        int day = time.monthDay;
        int springday = (int) (((((double) (year - 2000)) * 0.2422d) + 3.87d) - ((double) ((year - 2000) / 4)));
        int summerday = (int) (((((double) (year - 2000)) * 0.2422d) + 5.52d) - ((double) ((year - 2000) / 4)));
        int autumnday = (int) (((((double) (year - 2000)) * 0.2422d) + 7.5d) - ((double) ((year - 2000) / 4)));
        int winterday = (int) (((((double) (year - 2000)) * 0.2422d) + 7.438d) - ((double) ((year - 2000) / 4)));
        if (month == 2) {
            if (day >= springday) {
                season = 81;
            } else {
                season = 84;
            }
        } else if (2 < month && month < 5) {
            season = 81;
        } else if (month == 5) {
            if (day >= summerday) {
                season = 82;
            } else {
                season = 81;
            }
        } else if (5 < month && month < 8) {
            season = 82;
        } else if (month == 8) {
            if (day >= autumnday) {
                season = 83;
            } else {
                season = 82;
            }
        } else if (8 < month && month < 11) {
            season = 83;
        } else if (month == 11) {
            if (day >= winterday) {
                season = 84;
            } else {
                season = 83;
            }
        } else if (month > 11 || month < 2) {
            season = 84;
        }
        Log.i(TAG, "CurrentSeason: " + season);
        return season;
    }

    private String getSeasonalLockSoundPath(int season, boolean isLock) {
        switch (season) {
            case EFFECT_SPRING /*81*/:
                if (isLock) {
                    return LOCK_SOUND_SPRING;
                }
                return UNLOCK_SOUND_SPRING;
            case EFFECT_SUMMER /*82*/:
                if (isLock) {
                    return LOCK_SOUND_SUMMER;
                }
                return UNLOCK_SOUND_SUMMER;
            case EFFECT_AUTUMN /*83*/:
                if (isLock) {
                    return LOCK_SOUND_AUTUMN;
                }
                return UNLOCK_SOUND_AUTUMN;
            case EFFECT_WINTER /*84*/:
                if (isLock) {
                    return LOCK_SOUND_WINTER;
                }
                return UNLOCK_SOUND_WINTER;
            default:
                return null;
        }
    }

    private void handleSetGradationLayer() {
        boolean isPreloadedWallpaper = true;
        if (this.mBackgroundRootLayout != null) {
            if (System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_wallpaper_transparent", 1, -2) != 1) {
                isPreloadedWallpaper = false;
            }
            Log.d(TAG, "isPreloadedWallpaper=" + isPreloadedWallpaper);
            if ((!isPreloadedWallpaper && !KeyguardProperties.isSupportBlendedFilter()) || KeyguardProperties.isDcmLauncherWithoutSecure(this.mContext)) {
                if (this.mStatusBarGradationView == null) {
                    this.mStatusBarGradationView = new View(this.mContext);
                    this.mStatusBarGradationView.setBackgroundResource(C0302R.drawable.gradation_indi_bg);
                    this.mStatusBarGradationView.setLayoutParams(new LayoutParams(-1, -2));
                }
                if (this.mBackgroundRootLayout.indexOfChild(this.mStatusBarGradationView) == -1) {
                    this.mBackgroundRootLayout.addView(this.mStatusBarGradationView, -1, -2);
                } else {
                    this.mBackgroundRootLayout.bringChildToFront(this.mStatusBarGradationView);
                }
            } else if (this.mStatusBarGradationView != null) {
                this.mBackgroundRootLayout.removeView(this.mStatusBarGradationView);
                this.mStatusBarGradationView = null;
            }
        }
    }

    public KeyguardEffectViewBase getCircleView() {
        if (this.mNeedTwoCircleView) {
            return this.mForegroundCircleShortcutView;
        }
        return this.mForegroundCircleView;
    }

    public void setVisibleNotificationBottom(int visibleNotificationBottom) {
        this.mVisibleNotificationBottom = visibleNotificationBottom;
    }

    public boolean setScreenOnCallback(ScreenOnCallback callback) {
        if (!this.mWallpaperProcessSeparated || KeyguardEffectViewUtil.isKeyguardEffectViewWallpaper(this.mContext) || SecKeyguardStatusUtils.isUltraPowerSavingMode(this.mContext)) {
            return false;
        }
        this.mScreenOnCallback = callback;
        if (this.mKeyguardWallpaperService != null) {
            try {
                this.mKeyguardWallpaperService.setKeyguardWallpaperShowCallback(this.mWallpaperShowCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException occured while setKeyguardWallpaperShowCallback()");
            }
        }
        return true;
    }

    public void startPreviewAnimation(final boolean isRight) {
        if (this.mPreviewContainer != null) {
            Log.d(TAG, "startPreviewAnimation()");
            if (this.mWallpaperProcessSeparated && this.mKeyguardWallpaperService != null) {
                try {
                    Log.d(TAG, "COMMAND_REMOVE_WINDOW_ANIM");
                    this.mKeyguardWallpaperService.executeCommand(9, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException occured while COMMAND_REMOVE_WINDOW_ANIM");
                }
            }
            this.mPreviewContainer.startAnimation(new AnimatorListenerAdapter() {
                boolean mIsCancelled = false;

                public void onAnimationStart(Animator animation) {
                    this.mIsCancelled = false;
                    ((LaunchTransitionCallback) KeyguardEffectViewController.this.mNotificationPanel).onAnimationToSideStarted(isRight);
                }

                public void onAnimationEnd(Animator animation) {
                    Log.d(KeyguardEffectViewController.TAG, "Animation ended");
                    if (!this.mIsCancelled) {
                        ((LaunchTransitionCallback) KeyguardEffectViewController.this.mNotificationPanel).onAnimationToSideEnded();
                        KeyguardEffectViewController.this.mHandler.postDelayed(KeyguardEffectViewController.this.mResetPreviewRunnable, 5000);
                    }
                }

                public void onAnimationCancel(Animator animation) {
                    Log.d(KeyguardEffectViewController.TAG, "Animation cancelled");
                    this.mIsCancelled = true;
                    ((LaunchTransitionCallback) KeyguardEffectViewController.this.mNotificationPanel).onAnimationToSideCancelled();
                }
            });
        }
    }

    public void setPreviewView(boolean isRight) {
        if (this.mPreviewContainer != null) {
            this.mPreviewContainer.setPreviewView(isRight);
        }
    }

    public void resetPreviewView() {
        if (this.mPreviewContainer != null) {
            this.mHandler.removeCallbacks(this.mResetPreviewRunnable);
            this.mPreviewContainer.resetPreviewView();
        }
    }

    public KeyguardPreviewContainer getPreviewContainer() {
        return this.mPreviewContainer;
    }

    public void updateIsAdminWallpaper() {
        new Thread(new Runnable() {
            public void run() {
                KeyguardEffectViewUtil.updateIsAdminWallpaper();
            }
        }).start();
    }

    private String getEffectName(int effect) {
        boolean isOpenThemeEnabled = true;
        if (isZoomPanningEffectEnabled() && isRichLockWallpaper() && !isJustLockLiveEnabled() && KeyguardEffectViewUtil.getWallpaperType(this.mContext) != 0) {
            return "ZoomPanning";
        }
        if (!(KeyguardProperties.isSupportElasticPlugin() && KeyguardEffectViewUtil.isOpenThemeWallpaperEnabled(this.mContext) && !SecKeyguardStatusUtils.isEmergencyMode(this.mContext) && KeyguardEffectViewUtil.getWallpaperType(this.mContext) == 1)) {
            isOpenThemeEnabled = false;
        }
        if (!isOpenThemeEnabled) {
            String nameOfEffect;
            switch (effect) {
                case -2:
                case -1:
                    nameOfEffect = null;
                    break;
                case 0:
                    nameOfEffect = "None";
                    break;
                case 1:
                    nameOfEffect = "RippleInk";
                    break;
                case 2:
                    nameOfEffect = "LensFlare";
                    break;
                case 3:
                    nameOfEffect = "ParticleSpace";
                    break;
                case 4:
                    nameOfEffect = "WaterColor";
                    break;
                case 5:
                    nameOfEffect = "Blind";
                    break;
                case 6:
                    nameOfEffect = "MassTension";
                    break;
                case 7:
                    nameOfEffect = "MassRipple";
                    break;
                case 8:
                    nameOfEffect = "BrilliantRing";
                    break;
                case 9:
                    nameOfEffect = "BrilliantCut";
                    break;
                case 10:
                    nameOfEffect = "IndigoDiffusion";
                    break;
                case 11:
                    nameOfEffect = "AbstractTile";
                    break;
                case 12:
                    nameOfEffect = "GeometricMosaic";
                    break;
                case 13:
                    nameOfEffect = "WaterDroplet";
                    break;
                case 14:
                    nameOfEffect = "SparklingBubbles";
                    break;
                case 15:
                    nameOfEffect = "ColourDroplet";
                    break;
                case EFFECT_SPRING /*81*/:
                    nameOfEffect = "Spring";
                    break;
                case EFFECT_SUMMER /*82*/:
                    nameOfEffect = "Summer";
                    break;
                case EFFECT_AUTUMN /*83*/:
                    nameOfEffect = "Autumn";
                    break;
                case EFFECT_WINTER /*84*/:
                    nameOfEffect = "Winter";
                    break;
                case EFFECT_SEASONAL /*85*/:
                    nameOfEffect = "Seasonal";
                    break;
                case 100:
                    nameOfEffect = "LiveWallpaper";
                    break;
                default:
                    nameOfEffect = "None";
                    break;
            }
            return nameOfEffect;
        } else if (this.mWallpaperProcessSeparated) {
            return "None";
        } else {
            return "OpenTheme";
        }
    }

    private String getEffectClassName(String nameOfEffect) {
        if (nameOfEffect == null || nameOfEffect.length() == 0) {
            return null;
        }
        if ("LiveWallpaper".equals(nameOfEffect)) {
            return "com.android.keyguard.sec.rich.KeyguardEffectView" + nameOfEffect;
        }
        if ("OpenTheme".equals(nameOfEffect)) {
            if (this.mOpenThemeManager == null || this.mOpenThemeManager.isRecreateNeeded()) {
                this.mOpenThemeManager = new KeyguardOpenThemeManager(this.mContext);
            }
            if (this.mOpenThemeManager.getNameOfClass() != null) {
                return this.mOpenThemeManager.getNameOfClass();
            }
            return "com.android.keyguard.sec.effect.KeyguardEffectViewNone";
        } else if ("Spring".equals(nameOfEffect) || "Summer".equals(nameOfEffect) || "Autumn".equals(nameOfEffect) || "Winter".equals(nameOfEffect) || "Seasonal".equals(nameOfEffect)) {
            return this.mFestivalEffect.getFestivalEffectClassName(nameOfEffect);
        } else {
            return "com.android.keyguard.sec.effect.KeyguardEffectView" + nameOfEffect;
        }
    }

    private KeyguardEffectViewBase createEffectInstance(String nameOfClass) {
        KeyguardEffectViewBase createdEffect = null;
        if (nameOfClass == null) {
            return createdEffect;
        }
        try {
            return (KeyguardEffectViewBase) Class.forName(nameOfClass).getConstructor(new Class[]{Context.class, KeyguardWindowCallback.class, Boolean.TYPE, Integer.TYPE}).newInstance(new Object[]{this.mContext, null, Boolean.valueOf(false), Integer.valueOf(0)});
        } catch (ClassNotFoundException e) {
            Log.w(TAG, nameOfClass + " ClassNotFoundException");
            return createdEffect;
        } catch (NoSuchMethodException e2) {
            Log.w(TAG, nameOfClass + " NoSuchMethodException");
            return createdEffect;
        } catch (SecurityException e3) {
            Log.w(TAG, nameOfClass + " SecurityException");
            return createdEffect;
        } catch (InstantiationException e4) {
            Log.w(TAG, nameOfClass + " InstantiationException");
            return createdEffect;
        } catch (IllegalAccessException e5) {
            Log.w(TAG, nameOfClass + " IllegalAccessException");
            return createdEffect;
        } catch (IllegalArgumentException e6) {
            Log.w(TAG, nameOfClass + " IllegalArgumentException");
            return createdEffect;
        } catch (InvocationTargetException e7) {
            Log.w(TAG, nameOfClass + " InvocationTargetException");
            return createdEffect;
        }
    }

    private boolean isBackgroundEffect(String nameOfClass) {
        boolean retValue = false;
        if (nameOfClass != null) {
            try {
                retValue = ((Boolean) Class.forName(nameOfClass).getDeclaredMethod("isBackgroundEffect", (Class[]) null).invoke((Object[]) null, (Object[]) null)).booleanValue();
            } catch (ClassNotFoundException e) {
                Log.w(TAG, nameOfClass + " ClassNotFoundException");
            } catch (NoSuchMethodException e2) {
                Log.w(TAG, nameOfClass + " NoSuchMethodException");
            } catch (SecurityException e3) {
                Log.w(TAG, nameOfClass + " SecurityException");
            } catch (IllegalAccessException e4) {
                Log.w(TAG, nameOfClass + " IllegalAccessException");
            } catch (IllegalArgumentException e5) {
                Log.w(TAG, nameOfClass + " IllegalArgumentException");
            } catch (InvocationTargetException e6) {
                Log.w(TAG, nameOfClass + " InvocationTargetException");
            }
        }
        return retValue;
    }

    private String getCounterEffectName(String nameOfClass) {
        String className = null;
        if (nameOfClass == null) {
            return className;
        }
        try {
            return (String) Class.forName(nameOfClass).getDeclaredMethod("getCounterEffectName", (Class[]) null).invoke((Object[]) null, (Object[]) null);
        } catch (ClassNotFoundException e) {
            Log.w(TAG, nameOfClass + " ClassNotFoundException");
            return className;
        } catch (NoSuchMethodException e2) {
            Log.w(TAG, nameOfClass + " NoSuchMethodException");
            return className;
        } catch (SecurityException e3) {
            Log.w(TAG, nameOfClass + " SecurityException");
            return className;
        } catch (IllegalAccessException e4) {
            Log.w(TAG, nameOfClass + " IllegalAccessException");
            return className;
        } catch (IllegalArgumentException e5) {
            Log.w(TAG, nameOfClass + " IllegalArgumentException");
            return className;
        } catch (InvocationTargetException e6) {
            Log.w(TAG, nameOfClass + " InvocationTargetException");
            return className;
        }
    }

    private void makeEffectViews(int effect) {
        boolean isOpenThemeEnabled = true;
        if (!(KeyguardProperties.isSupportElasticPlugin() && KeyguardEffectViewUtil.isOpenThemeWallpaperEnabled(this.mContext) && !SecKeyguardStatusUtils.isEmergencyMode(this.mContext) && KeyguardEffectViewUtil.getWallpaperType(this.mContext) == 1)) {
            isOpenThemeEnabled = false;
        }
        String nameOfClass = getEffectClassName(getEffectName(effect));
        if ((nameOfClass == null || nameOfClass.equals(this.mOldPrimaryEffect)) && (nameOfClass != null || this.mOldPrimaryEffect == null)) {
            if (!this.mWallpaperProcessSeparated) {
                setBackground();
                if (this.mBackgroundView != null) {
                    this.mBackgroundView.update();
                }
                this.mUserSwitching = false;
            }
        } else if (this.mWallpaperProcessSeparated) {
            this.mOldPrimaryEffect = nameOfClass;
            if (isOpenThemeEnabled || nameOfClass == null || "LiveWallpaper".equals(getEffectName(effect)) || "None".equals(getEffectName(effect))) {
                this.mBackgroundView = null;
                setBackground();
                this.mForegroundView = null;
                setForeground();
                this.mUnlockEffectView = this.mForegroundCircleView;
                return;
            }
            boolean isPrimaryBackground;
            String tempClassName;
            if (isBackgroundEffect(nameOfClass)) {
                isPrimaryBackground = true;
                tempClassName = getEffectClassName(getCounterEffectName(nameOfClass));
            } else {
                isPrimaryBackground = false;
                tempClassName = nameOfClass;
            }
            this.mForegroundView = createEffectInstance(tempClassName);
            if (isPrimaryBackground) {
                this.mUnlockEffectView = null;
            } else if (this.mForegroundView != null) {
                this.mUnlockEffectView = this.mForegroundView;
            } else {
                this.mUnlockEffectView = this.mForegroundCircleView;
            }
            Log.d(TAG, "mUnlockEffectView = " + this.mUnlockEffectView);
            setForeground();
        } else {
            KeyguardEffectViewBase primaryEffect;
            if (isOpenThemeEnabled) {
                this.mCurrentEffect = 0;
                primaryEffect = this.mForegroundCircleView;
            } else if ("None".equals(getEffectName(effect))) {
                primaryEffect = this.mForegroundCircleView;
            } else {
                primaryEffect = createEffectInstance(nameOfClass);
            }
            if (primaryEffect == null) {
                this.mOldPrimaryEffect = null;
                this.mBackgroundView = null;
                setBackground();
                this.mForegroundView = null;
                setForeground();
                this.mUnlockEffectView = this.mForegroundCircleView;
                return;
            }
            if (isBackgroundEffect(primaryEffect.getClass().getName().toString())) {
                this.mBackgroundView = primaryEffect;
                this.mOldPrimaryEffect = this.mBackgroundView.getClass().getName().toString();
                if (isOpenThemeEnabled || isJustLockLiveEnabled()) {
                    this.mUnlockEffectView = this.mForegroundCircleView;
                } else {
                    this.mUnlockEffectView = this.mBackgroundView;
                }
                Log.d(TAG, "Sets backgound view first");
            } else {
                if (primaryEffect != this.mForegroundCircleView) {
                    this.mForegroundView = primaryEffect;
                } else {
                    this.mForegroundView = null;
                }
                this.mOldPrimaryEffect = primaryEffect.getClass().getName().toString();
                this.mUnlockEffectView = primaryEffect;
                Log.d(TAG, "Sets foreground view first");
            }
            if (isOpenThemeEnabled) {
                if (this.mOpenThemeManager == null || !this.mOpenThemeManager.isEnabled() || this.mOpenThemeManager.getOpenThemeWallpaperView() == null) {
                    nameOfClass = getEffectClassName("None");
                } else {
                    KeyguardEffectViewBase themeView = this.mOpenThemeManager.getOpenThemeWallpaperView();
                    if (this.mBackgroundView != themeView) {
                        Log.d("KeyguardOpenThemeEffect", "change OpenTheme");
                        this.mBackgroundView = themeView;
                        setBackground();
                        this.mBackgroundView.update();
                        setForeground();
                        this.mUserSwitching = false;
                    }
                    this.mOldPrimaryEffect = themeView.getClass().getName().toString();
                    return;
                }
            }
            KeyguardEffectViewBase secondaryEffect = createEffectInstance(getEffectClassName(getCounterEffectName(nameOfClass)));
            if (this.mBackgroundView == primaryEffect) {
                this.mForegroundView = secondaryEffect;
                Log.d(TAG, "Sets foreground view later");
            } else {
                this.mBackgroundView = secondaryEffect;
                Log.d(TAG, "Sets background view later");
            }
            if (this.mBackgroundView != null) {
                Log.d(TAG, "mBackgroundView is not null");
                if (KeyguardProperties.isBrilliantCutSpecialTypeEnabled() && (this.mBackgroundView instanceof KeyguardEffectViewBrilliantCut)) {
                    ((KeyguardEffectViewBrilliantCut) this.mBackgroundView).settingsForImageType(getDefaultWallpaperTypeForEffect());
                }
                setBackground();
                this.mBackgroundView.updateAfterCreation();
            }
            setForeground();
            this.mUserSwitching = false;
        }
    }

    public void setSubEffectLayout(View foreground) {
        this.mForegroundRootLayoutSub = (FrameLayout) foreground;
    }

    public void setViewMediatorCallback(ViewMediatorCallback callback) {
        this.mViewMediatorCallback = callback;
    }
}
