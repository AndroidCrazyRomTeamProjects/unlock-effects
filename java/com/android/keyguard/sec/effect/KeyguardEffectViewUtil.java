package com.android.keyguard.sec.effect;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.android.keyguard.C0302R;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.SecKeyguardStatusUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class KeyguardEffectViewUtil {
    public static final String ACTION_UPDATE_LOCKSCREEN_WALLPAPER = "edm.intent.action.internal.sec.LSO_CONFIG_CHANGED";
    public static final String ADMIN_WALLPAPER_PORTRAIT = "/data/system/enterprise/lso/lockscreen_wallpaper.jpg";
    public static final String ADMIN_WALLPAPER_RIPPLE = "/data/system/enterprise/lso/lockscreen_wallpaper_ripple.jpg";
    public static final String DEFAULT_CSC_WALLPAPER_IMAGE_PATH = "//system/csc_contents/lockscreen_default_wallpaper.jpg";
    public static final String DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG = "//system/csc_contents/lockscreen_default_wallpaper.png";
    public static final String DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG_SUB = "//system/csc_contents/lockscreen_default_wallpaper_sub.png";
    public static final String DEFAULT_CSC_WALLPAPER_IMAGE_PATH_SUB = "//system/csc_contents/lockscreen_default_wallpaper_sub.jpg";
    public static final String DEFAULT_WALLPAPER_IMAGE_PATH = "//system/wallpaper/lockscreen_default_wallpaper.jpg";
    public static final String DEFAULT_WALLPAPER_IMAGE_PATH_PNG = "//system/wallpaper/lockscreen_default_wallpaper.png";
    public static final String DEFAULT_WALLPAPER_IMAGE_PATH_PNG_SUB = "//system/wallpaper/lockscreen_default_wallpaper_sub.png";
    public static final String DEFAULT_WALLPAPER_IMAGE_PATH_SUB = "//system/wallpaper/lockscreen_default_wallpaper_sub.jpg";
    public static final String KEY_CURRENT_WALLPAPER_FILE_PATH = "keyguard_current_wallpaper_file_path";
    public static final String KEY_CURRENT_WALLPAPER_FILE_PATH_SUB = "keyguard_current_wallpaper_file_path_sub";
    public static final String KEY_CURRENT_WALLPAPER_RES_ID = "keyguard_current_wallpaper_res_id";
    public static final String KEY_CURRENT_WALLPAPER_RES_ID_SUB = "keyguard_current_wallpaper_res_id_sub";
    public static final String KEY_CURRENT_WALLPAPER_TYPE = "keyguard_current_wallpaper_type";
    public static final String KEY_CURRENT_WALLPAPER_TYPE_SUB = "keyguard_current_wallpaper_type_sub";
    public static final String KEY_DEFAULT_WALLPAPER_RES_ID = "keyguard_default_wallpaper_res_id";
    public static final String KEY_DEFAULT_WALLPAPER_RES_ID_SUB = "keyguard_default_wallpaper_res_id_sub";
    private static final int KNOX_MODE_USER_ID = 100;
    public static final String LANDSCAPE_WALLPAPER_IMAGE_PATH = "/data/data/com.sec.android.gallery3d/lockscreen_wallpaper_land.jpg";
    public static final String LANDSCAPE_WALLPAPER_IMAGE_PATH_SUB = "/data/data/com.sec.android.gallery3d/lockscreen_wallpaper_land_sub.jpg";
    public static final String PORTRAIT_WALLPAPER_IMAGE_PATH = "/data/data/com.sec.android.gallery3d/lockscreen_wallpaper.jpg";
    public static final String PORTRAIT_WALLPAPER_IMAGE_PATH_SUB = "/data/data/com.sec.android.gallery3d/lockscreen_wallpaper_sub.jpg";
    public static final String RIPPLE_WALLPAPER_IMAGE_PATH = "/data/data/com.sec.android.gallery3d/lockscreen_wallpaper_ripple.jpg";
    public static final int SHARED_WALLPAPER_TYPE_FILE = 1;
    public static final int SHARED_WALLPAPER_TYPE_NONE = 0;
    public static final int SHARED_WALLPAPER_TYPE_RESOURCE = 2;
    private static final String TAG = "KeyguardEffectViewUtil";
    public static final int WALLPAPER_TYPE_FILE = 1;
    public static final int WALLPAPER_TYPE_HOME = 3;
    public static final int WALLPAPER_TYPE_JUST_ON_LOCK_LIVE = 2;
    public static final int WALLPAPER_TYPE_LIVE = 0;
    private static boolean mAdminWallpaper = false;
    private static boolean mUpdatedAdminWallpaperValue = false;
    private static String mWallpaperPath = null;
    private static String mWallpaperPathSub = null;
    private static int mWallpaperResId = -1;
    private static int mWallpaperResIdSub = -1;
    private static int mWallpaperType = 0;
    private static int mWallpaperTypeSub = 0;

    public static boolean isKeyguardEffectViewWallpaper(Context context) {
        return !isAdminWallpaper() && (isLiveWallpaper(context) || isHomeWallpaper(context));
    }

    public static boolean isLiveWallpaper(Context context) {
        int wallpaperType = getWallpaperType(context);
        Log.d(TAG, "wallpaperType :" + wallpaperType);
        return wallpaperType == 0;
    }

    public static boolean isHomeWallpaper(Context context) {
        return getWallpaperType(context) == 3;
    }

    public static int getWallpaperType(Context context) {
        return getWallpaperType(context, 0);
    }

    public static int getWallpaperType(Context context, int displayId) {
        if (context == null) {
            return 1;
        }
        if (KeyguardProperties.isDcmLauncherWithoutSecure(context) && ActivityManager.getCurrentUser() < 100) {
            return 3;
        }
        if (displayId == 0) {
            return System.getIntForUser(context.getContentResolver(), "lockscreen_wallpaper", 1, -2);
        }
        return System.getIntForUser(context.getContentResolver(), "lockscreen_wallpaper_sub", 1, -2);
    }

    public static boolean isAdminWallpaper() {
        if (!mUpdatedAdminWallpaperValue) {
            updateIsAdminWallpaper();
        }
        return mAdminWallpaper;
    }

    public static void updateIsAdminWallpaper() {
        mUpdatedAdminWallpaperValue = true;
        mAdminWallpaper = new File(ADMIN_WALLPAPER_PORTRAIT).exists();
    }

    public static BitmapDrawable getCurrentWallpaper(Context context) {
        return getCurrentWallpaper(context, 0);
    }

    public static BitmapDrawable getCurrentWallpaper(Context context, int displayId) {
        int i = 1;
        String wallpaperPath = null;
        if (displayId == 0) {
            wallpaperPath = System.getStringForUser(context.getContentResolver(), "lockscreen_wallpaper_path", -2);
        } else if (1 == displayId) {
            wallpaperPath = System.getStringForUser(context.getContentResolver(), "lockscreen_wallpaper_path_sub", -2);
        }
        BitmapDrawable bmpDrawable = getCurrentWallpaper(context, wallpaperPath, displayId);
        if (KeyguardProperties.isLatestPhoneUX() || KeyguardProperties.isLatestTabletUX()) {
            boolean oldVal;
            Log.d(TAG, "checkWhiteLockscreenWallpaper() start");
            boolean newVal = checkWhiteLockscreenWallpaper(bmpDrawable);
            Log.d(TAG, "checkWhiteLockscreenWallpaper() end");
            if (Global.getInt(context.getContentResolver(), "white_lockscreen_wallpaper", 0) == 1) {
                oldVal = true;
            } else {
                oldVal = false;
            }
            if (oldVal != newVal) {
                ContentResolver contentResolver = context.getContentResolver();
                String str = "white_lockscreen_wallpaper";
                if (!newVal) {
                    i = 0;
                }
                Global.putInt(contentResolver, str, i);
                Log.d(TAG, "whiteLockscreenWallpaper : " + newVal);
            }
        }
        return bmpDrawable;
    }

    private static boolean checkWhiteLockscreenWallpaper(BitmapDrawable bd) {
        float[] pixelHSV;
        int sampleCount;
        int h;
        int step;
        int x;
        int y;
        Bitmap bmp = bd.getBitmap();
        float sumSaturation = 0.0f;
        float sumValue = 0.0f;
        try {
            int w;
            pixelHSV = new float[3];
            sampleCount = 0;
            w = bmp.getWidth();
            h = bmp.getHeight() / 2;
            step = w > h ? (int) (((float) h) / 100.0f) : (int) (((float) w) / 100.0f);
            if (step <= 0) {
                step = 1;
            }
            for (x = 0; x < w; x += step) {
                for (y = 0; y < h; y += step) {
                    Color.colorToHSV(bmp.getPixel(x, y), pixelHSV);
                    sumSaturation += pixelHSV[1];
                    sumValue += pixelHSV[2];
                    sampleCount++;
                }
            }
            float val = sumValue / ((float) sampleCount);
            if (sumSaturation / ((float) sampleCount) < 0.3f && val >= 0.88f) {
                return true;
            }
        } catch (Exception e) {
        }
        sumSaturation = 0.0f;
        sumValue = 0.0f;
        try {
            pixelHSV = new float[3];
            sampleCount = 0;
            w = bmp.getWidth();
            int m = bmp.getHeight() / 2;
            h = bmp.getHeight();
            step = w > m ? (int) (((float) m) / 100.0f) : (int) (((float) w) / 100.0f);
            if (step <= 0) {
                step = 1;
            }
            for (x = 0; x < w; x += step) {
                for (y = m; y < h; y += step) {
                    Color.colorToHSV(bmp.getPixel(x, y), pixelHSV);
                    sumSaturation += pixelHSV[1];
                    sumValue += pixelHSV[2];
                    sampleCount++;
                }
            }
            val = sumValue / ((float) sampleCount);
            if (sumSaturation / ((float) sampleCount) < 0.3f && val >= 0.88f) {
                return true;
            }
        } catch (Exception e2) {
        }
        return false;
    }

    public static BitmapDrawable getCurrentWallpaper(Context context, String galleryWallpaperFilePath) {
        return getCurrentWallpaper(context, galleryWallpaperFilePath, 0);
    }

    public static BitmapDrawable getCurrentWallpaper(Context context, String galleryWallpaperFilePath, int displayId) {
        Exception ex;
        String str = null;
        if (context == null) {
            return str;
        }
        if (displayId == 0) {
            mWallpaperPath = galleryWallpaperFilePath;
            Log.d(TAG, "getCurrentWallpaper() mWallpaperPath : " + mWallpaperPath);
        } else if (1 == displayId) {
            mWallpaperPathSub = galleryWallpaperFilePath;
            Log.d(TAG, "getCurrentWallpaper() mWallpaperPathSub : " + mWallpaperPathSub);
        }
        BitmapDrawable wallpaperDrawable;
        if (SecKeyguardStatusUtils.isEmergencyMode(context)) {
            InputStream is = getDefaultLonglifeInputStream(context);
            wallpaperDrawable = null;
            Log.d(TAG, "EmergencyMode");
            if (is != null) {
                wallpaperDrawable = new BitmapDrawable(context.getResources(), is);
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
        }
        BitmapDrawable wallpaperDrawable2;
        File file;
        if (isAdminWallpaper()) {
            if (displayId == 0) {
                mWallpaperPath = ADMIN_WALLPAPER_PORTRAIT;
            } else if (1 == displayId) {
                mWallpaperPathSub = ADMIN_WALLPAPER_PORTRAIT;
            }
            wallpaperDrawable = null;
            if (displayId == 0) {
                try {
                    wallpaperDrawable2 = new BitmapDrawable(context.getResources(), mWallpaperPath);
                } catch (Exception e2) {
                    ex = e2;
                    Log.d(TAG, "Loading Admin wallpaper EX:" + ex.toString());
                    if (displayId == 0) {
                        mWallpaperPath = str;
                        mWallpaperType = 0;
                    } else if (1 == displayId) {
                        mWallpaperPathSub = str;
                        mWallpaperTypeSub = 0;
                    }
                    if (displayId != 0) {
                    }
                    if (1 == displayId) {
                    }
                    if (displayId != 0) {
                        mWallpaperPathSub = PORTRAIT_WALLPAPER_IMAGE_PATH_SUB;
                    } else if (mWallpaperPath == null) {
                        mWallpaperPath = PORTRAIT_WALLPAPER_IMAGE_PATH;
                    }
                    file = null;
                    if (displayId != 0) {
                        file = new File(mWallpaperPath);
                    } else if (1 == displayId) {
                        file = new File(mWallpaperPathSub);
                    }
                    wallpaperDrawable = null;
                    if (file != null) {
                    }
                    return getDefaultWallpaper(context, displayId);
                }
                try {
                    mWallpaperType = 1;
                    wallpaperDrawable = wallpaperDrawable2;
                } catch (Exception e3) {
                    ex = e3;
                    wallpaperDrawable = wallpaperDrawable2;
                    Log.d(TAG, "Loading Admin wallpaper EX:" + ex.toString());
                    if (displayId == 0) {
                        mWallpaperPath = str;
                        mWallpaperType = 0;
                    } else if (1 == displayId) {
                        mWallpaperPathSub = str;
                        mWallpaperTypeSub = 0;
                    }
                    if (displayId != 0) {
                    }
                    if (1 == displayId) {
                    }
                    if (displayId != 0) {
                        mWallpaperPathSub = PORTRAIT_WALLPAPER_IMAGE_PATH_SUB;
                    } else if (mWallpaperPath == null) {
                        mWallpaperPath = PORTRAIT_WALLPAPER_IMAGE_PATH;
                    }
                    file = null;
                    if (displayId != 0) {
                        file = new File(mWallpaperPath);
                    } else if (1 == displayId) {
                        file = new File(mWallpaperPathSub);
                    }
                    wallpaperDrawable = null;
                    if (file != null) {
                    }
                    return getDefaultWallpaper(context, displayId);
                }
            } else if (1 == displayId) {
                wallpaperDrawable2 = new BitmapDrawable(context.getResources(), mWallpaperPathSub);
                mWallpaperTypeSub = 1;
                wallpaperDrawable = wallpaperDrawable2;
            }
            return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
        }
        if (displayId != 0 && mWallpaperPath != null && mWallpaperPath.contains(KeyguardEffectViewController.SlidingWallpaperPath)) {
            wallpaperDrawable = KeyguardEffectViewController.getScaledBitmapDrawable(context);
            mWallpaperType = 1;
            return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
        } else if (1 == displayId || mWallpaperPathSub == null || !mWallpaperPathSub.contains(KeyguardEffectViewController.SlidingWallpaperPath)) {
            if (displayId != 0) {
                if (mWallpaperPath == null) {
                    mWallpaperPath = PORTRAIT_WALLPAPER_IMAGE_PATH;
                }
            } else if (1 == displayId && mWallpaperPathSub == null) {
                mWallpaperPathSub = PORTRAIT_WALLPAPER_IMAGE_PATH_SUB;
            }
            file = null;
            if (displayId != 0) {
                file = new File(mWallpaperPath);
            } else if (1 == displayId) {
                file = new File(mWallpaperPathSub);
            }
            wallpaperDrawable = null;
            if (file != null || !file.exists() || !file.canRead()) {
                return getDefaultWallpaper(context, displayId);
            }
            if (displayId == 0) {
                try {
                    wallpaperDrawable2 = new BitmapDrawable(context.getResources(), mWallpaperPath);
                    try {
                        mWallpaperType = 1;
                        wallpaperDrawable = wallpaperDrawable2;
                    } catch (Exception e4) {
                        wallpaperDrawable = wallpaperDrawable2;
                        return getDefaultWallpaper(context, displayId);
                    }
                } catch (Exception e5) {
                    return getDefaultWallpaper(context, displayId);
                }
            } else if (1 == displayId) {
                wallpaperDrawable2 = new BitmapDrawable(context.getResources(), mWallpaperPathSub);
                mWallpaperTypeSub = 1;
                wallpaperDrawable = wallpaperDrawable2;
            }
            return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
        } else {
            wallpaperDrawable = KeyguardEffectViewController.getScaledBitmapDrawable(context);
            mWallpaperTypeSub = 1;
            return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
        }
    }

    public static BitmapDrawable getDefaultWallpaper(Context context) {
        return getDefaultWallpaper(context, 0);
    }

    public static BitmapDrawable getDefaultWallpaper(Context context, int displayId) {
        DisplayMetrics metrics;
        int scaleFactor;
        BitmapDrawable bitmapDrawable;
        if (context == null) {
            return null;
        }
        boolean isDefaultDisplay = displayId == 0;
        File file = new File(isDefaultDisplay ? DEFAULT_WALLPAPER_IMAGE_PATH : DEFAULT_WALLPAPER_IMAGE_PATH_SUB);
        File fileMultiCSC = new File(isDefaultDisplay ? DEFAULT_CSC_WALLPAPER_IMAGE_PATH : DEFAULT_CSC_WALLPAPER_IMAGE_PATH_SUB);
        File filePng = new File(isDefaultDisplay ? DEFAULT_WALLPAPER_IMAGE_PATH_PNG : DEFAULT_WALLPAPER_IMAGE_PATH_PNG_SUB);
        File fileMultiCSCPng = new File(isDefaultDisplay ? DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG : DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG_SUB);
        InputStream is = null;
        BitmapDrawable wallpaperDrawable = null;
        Options bmOptions = new Options();
        bmOptions.inJustDecodeBounds = true;
        InputStream is2;
        if (fileMultiCSCPng.exists()) {
            try {
                is2 = new FileInputStream(fileMultiCSCPng);
                if (isDefaultDisplay) {
                    try {
                        BitmapFactory.decodeFile(DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG, bmOptions);
                        mWallpaperPath = DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG;
                        mWallpaperType = 1;
                    } catch (IOException e) {
                        is = is2;
                        is = null;
                        if (is == null) {
                            is = getDefaultInputStream(context, displayId);
                            BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                        }
                        if (is != null) {
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                            if (scaleFactor < 1) {
                                scaleFactor = 1;
                            }
                            bmOptions.inJustDecodeBounds = false;
                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;
                            bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                            try {
                                is.close();
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }
                        return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
                    }
                }
                BitmapFactory.decodeFile(DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG_SUB, bmOptions);
                mWallpaperPathSub = DEFAULT_CSC_WALLPAPER_IMAGE_PATH_PNG_SUB;
                mWallpaperTypeSub = 1;
                is = is2;
            } catch (IOException e3) {
                is = null;
                if (is == null) {
                    is = getDefaultInputStream(context, displayId);
                    BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                }
                if (is != null) {
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                    if (scaleFactor < 1) {
                        scaleFactor = 1;
                    }
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;
                    bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                    is.close();
                }
                return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
            }
        } else if (fileMultiCSC.exists()) {
            try {
                is2 = new FileInputStream(fileMultiCSC);
                if (isDefaultDisplay) {
                    try {
                        BitmapFactory.decodeFile(DEFAULT_CSC_WALLPAPER_IMAGE_PATH, bmOptions);
                        mWallpaperPath = DEFAULT_CSC_WALLPAPER_IMAGE_PATH;
                        mWallpaperType = 1;
                    } catch (IOException e4) {
                        is = is2;
                        is = null;
                        if (is == null) {
                            is = getDefaultInputStream(context, displayId);
                            BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                        }
                        if (is != null) {
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                            if (scaleFactor < 1) {
                                scaleFactor = 1;
                            }
                            bmOptions.inJustDecodeBounds = false;
                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;
                            bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                            is.close();
                        }
                        return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
                    }
                }
                BitmapFactory.decodeFile(DEFAULT_CSC_WALLPAPER_IMAGE_PATH_SUB, bmOptions);
                mWallpaperPathSub = DEFAULT_CSC_WALLPAPER_IMAGE_PATH_SUB;
                mWallpaperTypeSub = 1;
                is = is2;
            } catch (IOException e5) {
                is = null;
                if (is == null) {
                    is = getDefaultInputStream(context, displayId);
                    BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                }
                if (is != null) {
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                    if (scaleFactor < 1) {
                        scaleFactor = 1;
                    }
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;
                    bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                    is.close();
                }
                return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
            }
        } else if (filePng.exists()) {
            try {
                is2 = new FileInputStream(filePng);
                if (isDefaultDisplay) {
                    try {
                        BitmapFactory.decodeFile(DEFAULT_WALLPAPER_IMAGE_PATH_PNG, bmOptions);
                        mWallpaperPath = DEFAULT_WALLPAPER_IMAGE_PATH_PNG;
                        mWallpaperType = 1;
                    } catch (IOException e6) {
                        is = is2;
                        is = null;
                        if (is == null) {
                            is = getDefaultInputStream(context, displayId);
                            BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                        }
                        if (is != null) {
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                            if (scaleFactor < 1) {
                                scaleFactor = 1;
                            }
                            bmOptions.inJustDecodeBounds = false;
                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;
                            bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                            is.close();
                        }
                        return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
                    }
                }
                BitmapFactory.decodeFile(DEFAULT_WALLPAPER_IMAGE_PATH_PNG_SUB, bmOptions);
                mWallpaperPathSub = DEFAULT_WALLPAPER_IMAGE_PATH_PNG_SUB;
                mWallpaperTypeSub = 1;
                is = is2;
            } catch (IOException e7) {
                is = null;
                if (is == null) {
                    is = getDefaultInputStream(context, displayId);
                    BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                }
                if (is != null) {
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                    if (scaleFactor < 1) {
                        scaleFactor = 1;
                    }
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;
                    bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                    is.close();
                }
                return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
            }
        } else if (file.exists()) {
            try {
                is2 = new FileInputStream(file);
                if (isDefaultDisplay) {
                    try {
                        BitmapFactory.decodeFile(DEFAULT_WALLPAPER_IMAGE_PATH, bmOptions);
                        mWallpaperPath = DEFAULT_WALLPAPER_IMAGE_PATH;
                        mWallpaperType = 1;
                    } catch (IOException e8) {
                        is = is2;
                        is = null;
                        if (is == null) {
                            is = getDefaultInputStream(context, displayId);
                            BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                        }
                        if (is != null) {
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                            if (scaleFactor < 1) {
                                scaleFactor = 1;
                            }
                            bmOptions.inJustDecodeBounds = false;
                            bmOptions.inSampleSize = scaleFactor;
                            bmOptions.inPurgeable = true;
                            bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                            is.close();
                        }
                        return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
                    }
                }
                BitmapFactory.decodeFile(DEFAULT_WALLPAPER_IMAGE_PATH_SUB, bmOptions);
                mWallpaperPathSub = DEFAULT_WALLPAPER_IMAGE_PATH_SUB;
                mWallpaperTypeSub = 1;
                is = is2;
            } catch (IOException e9) {
                is = null;
                if (is == null) {
                    is = getDefaultInputStream(context, displayId);
                    BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
                }
                if (is != null) {
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
                    if (scaleFactor < 1) {
                        scaleFactor = 1;
                    }
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;
                    bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
                    is.close();
                }
                return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
            }
        }
        if (is == null) {
            is = getDefaultInputStream(context, displayId);
            BitmapFactory.decodeStream(getDefaultInputStream(context, displayId), null, bmOptions);
        }
        if (is != null) {
            metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
            scaleFactor = Math.min(bmOptions.outWidth / metrics.widthPixels, bmOptions.outHeight / metrics.heightPixels);
            if (scaleFactor < 1) {
                scaleFactor = 1;
            }
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            bitmapDrawable = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(is, null, bmOptions));
            is.close();
        }
        return checkWallpaperDrawableAndReturn(context, wallpaperDrawable, displayId);
    }

    public static InputStream getDefaultInputStream(Context context) {
        return getDefaultInputStream(context, 0);
    }

    public static InputStream getDefaultInputStream(Context context, int displayId) {
        if (context == null) {
            return null;
        }
        if (displayId == 0) {
            mWallpaperResId = C0302R.drawable.keyguard_default_wallpaper;
            mWallpaperType = 2;
            return context.getResources().openRawResource(mWallpaperResId);
        }
        mWallpaperResIdSub = C0302R.drawable.sub_keyguard_default_wallpaper;
        mWallpaperTypeSub = 2;
        return context.getResources().openRawResource(mWallpaperResIdSub);
    }

    public static InputStream getDefaultLonglifeInputStream(Context context) {
        if (context == null) {
            return null;
        }
        mWallpaperResId = C0302R.drawable.keyguard_longlife_wallpaper_black;
        mWallpaperType = 2;
        return context.getResources().openRawResource(mWallpaperResId);
    }

    private static BitmapDrawable checkWallpaperDrawableAndReturn(Context context, BitmapDrawable wallpaperDrawable, int displayId) {
        if (context == null) {
            return wallpaperDrawable;
        }
        if (wallpaperDrawable != null) {
            Bitmap pBitmap = wallpaperDrawable.getBitmap();
            if (!(pBitmap == null || pBitmap.getWidth() == 0 || pBitmap.getHeight() == 0)) {
                return writeWallpaperInfoAndReturn(context, wallpaperDrawable, displayId);
            }
        }
        InputStream is = getDefaultInputStream(context, displayId);
        BitmapDrawable newWallpaperDrawable = null;
        if (is != null) {
            newWallpaperDrawable = new BitmapDrawable(context.getResources(), is);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writeWallpaperInfoAndReturn(context, newWallpaperDrawable, displayId);
    }

    private static BitmapDrawable writeWallpaperInfoAndReturn(Context context, BitmapDrawable wallpaperDrawable, int displayId) {
        writeKeyguardCurrentWallpaperInfo(context, displayId);
        return wallpaperDrawable;
    }

    public static void writeKeyguardCurrentWallpaperInfo(Context context, int displayId) {
        if (context != null) {
            Log.i(TAG, "set current wallpaper info");
            if (displayId == 0) {
                System.putIntForUser(context.getContentResolver(), KEY_CURRENT_WALLPAPER_TYPE, mWallpaperType, -2);
                System.putStringForUser(context.getContentResolver(), KEY_CURRENT_WALLPAPER_FILE_PATH, mWallpaperPath, -2);
                System.putIntForUser(context.getContentResolver(), KEY_CURRENT_WALLPAPER_RES_ID, mWallpaperResId, -2);
            } else if (displayId == 1) {
                System.putIntForUser(context.getContentResolver(), KEY_CURRENT_WALLPAPER_TYPE_SUB, mWallpaperTypeSub, -2);
                System.putStringForUser(context.getContentResolver(), KEY_CURRENT_WALLPAPER_FILE_PATH_SUB, mWallpaperPathSub, -2);
                System.putIntForUser(context.getContentResolver(), KEY_CURRENT_WALLPAPER_RES_ID_SUB, mWallpaperResIdSub, -2);
            }
        }
    }

    public static void writeKeyguardDefaultWallpaperResId(Context context) {
        if (context != null) {
            Log.i(TAG, "set resource id");
            System.putIntForUser(context.getContentResolver(), KEY_DEFAULT_WALLPAPER_RES_ID, C0302R.drawable.keyguard_default_wallpaper, -2);
        }
    }

    public static boolean isZoomPanningEffectEnabled(Context context) {
        boolean isZoomPanningEffect = true;
        if (SecKeyguardStatusUtils.isPowerSavingMode(context) || SecKeyguardStatusUtils.isUltraPowerSavingMode(context)) {
            return false;
        }
        if (System.getIntForUser(context.getContentResolver(), "lockscreen_zoom_panning_effect", 0, -2) != 1) {
            isZoomPanningEffect = false;
        }
        return isZoomPanningEffect;
    }

    public static boolean isOpenThemeWallpaperEnabled(Context context) {
        boolean isEnabled = false;
        if (context == null) {
            return false;
        }
        if (System.getIntForUser(context.getContentResolver(), "opne_theme_effect_lockscreen_wallpaper", 0, -2) != 0) {
            isEnabled = true;
        }
        String packageName = System.getString(context.getContentResolver(), "current_sec_theme_package_open_theme");
        if (!isEnabled) {
            return isEnabled;
        }
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        return isEnabled;
    }

    public static Bitmap getPreferredConfigBitmap(Bitmap srcBitmap, Config config) {
        if (srcBitmap == null) {
            return null;
        }
        if (srcBitmap.getConfig() == config) {
            return srcBitmap;
        }
        Log.i(TAG, "start to convert album art");
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), config);
        new Canvas(destBitmap).drawBitmap(srcBitmap, 0.0f, 0.0f, null);
        return destBitmap;
    }
}
