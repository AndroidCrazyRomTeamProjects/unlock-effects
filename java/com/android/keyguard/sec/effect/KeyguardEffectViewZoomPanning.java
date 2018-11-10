package com.android.keyguard.sec.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.interpolator.SineEaseInOut;
import android.view.animation.interpolator.SineInOut33;
import android.widget.FrameLayout;
import com.android.keyguard.C0302R;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.sec.KeyguardProperties;
import com.android.keyguard.sec.wallpaper.KeyguardWallpaperMediator.KeyguardWindowCallback;
import java.util.ArrayList;

public class KeyguardEffectViewZoomPanning extends FrameLayout implements KeyguardEffectViewBase {
    private final boolean DEBUG;
    private final String TAG;
    private View mBottomArea;
    private int mDisplayId;
    private boolean mIsDoubleTapPuase;
    private boolean mIsForceUpdateMusicBitmap;
    private boolean mIsRemoveMusicBitmapByScreenOff;
    private MovingImageView mMovableImageView;
    private Bitmap mMusicWallpaper;
    private View mNotificationPanel;
    private String mStrMusicBitmapId;
    private float mTouchStartX;
    private float mTouchStartY;
    private boolean mUpdateDelayed;
    private Interpolator mWidgetReleaseInterpolator;

    enum CameraState {
        ZoomIn,
        ZoomOut,
        PinchZoom,
        DoubleTapPause,
        PanningRandom,
        PanningFace,
        TouchDown,
        TouchRelease,
        Unlock,
        Overlap
    }

    class MovingImageView extends View {
        private static final float BASIC_IMAGE_SIZE = 2560.0f;
        private static final String CATEGORY_CODE_PET = "BGA";
        private static final int DEFAULT_ANIMATION_FRAME = 1000;
        private static final float DEFAULT_CAMERA_ZOOM = 1.5f;
        private static final float DEFAULT_CAMERA_ZOOM_FOR_PET = 1.2f;
        private static final int DOUBLE_TAP_ANIMATION_FRAME = 8;
        private static final int FACE_ZOOMIN_ANIMATION_FRAME = 600;
        private static final String KEY_WALLPAPER_INFO_CATEGORY_CODE = "keyguard_wu_wallpaper_info_category_code";
        private static final float MAXIMUM_CAMERA_ZOOM = 2.5f;
        private static final float MAXIMUM_CAMERA_ZOOM_FOR_PET = 1.5f;
        private static final int MAX_GOAL_POINT = 2;
        private static final int OVERLAP_ANIMATION_FRAME = 40;
        public static final int RANDOM = -1;
        private static final int REVERT_ANIMATION_FRAME = 8;
        private static final float THRESHOLD_CAMERA_ZOOM = 1.2f;
        private static final float TOUCH_MOVE_COE = 0.5f;
        private int SECOND_BORDER;
        private Handler mAnimationHandler;
        private Bitmap mBitmap;
        private int mBitmapHeight;
        private int mBitmapWidth;
        private float mCameraCenterPosX;
        private float mCameraCenterPosY;
        private float mCameraZoomIn;
        private int mCurrentAnimationFrame;
        private String mCurrentBitmapPath;
        private String mCurrentCategoryCode;
        private int mDefaultCameraHeight;
        private int mDefaultCameraWidth;
        private int mDefaultOverlapCameraHeight;
        private int mDefaultOverlapCameraWidth;
        private float mDistance;
        private Handler mDoubleTapCheckHandler;
        private int mGoalAnimationFrame;
        private boolean mIsAnimating;
        private boolean mIsDoubleTapWaiting;
        private boolean mIsOverlap;
        private boolean mIsPetImage;
        private float mLastPinchDistance;
        private float mLastTouchX;
        private float mLastTouchY;
        private float mMovingCoe;
        private int mNowGoalIndex;
        private Bitmap mOverlapBitmap;
        private Paint mOverlapBitmapDrawingPaint;
        private int mOverlapBitmapHeight;
        private int mOverlapBitmapWidth;
        private float mOverlapCameraCenterPosX;
        private float mOverlapCameraCenterPosY;
        private float mOverlapCameraZoomIn;
        private ArrayList<Integer> mPanningGoalIndexList;
        private float mPanningGoalX;
        private float mPanningGoalY;
        private Interpolator mPanningInterpolator;
        private boolean mPanningRandom;
        private float mPanningStartX;
        private float mPanningStartY;
        private float mPinchZoomPivotX;
        private float mPinchZoomPivotY;
        private int mPinchZoomStartMinThreshold;
        private int mSavedCurrentAnimationFrame;
        private int mSavedGoalAnimationFrame;
        private float mSavedPanningGoalX;
        private float mSavedPanningGoalY;
        private float mSavedPanningStartX;
        private float mSavedPanningStartY;
        private CameraState mSavedState;
        private float mSavedZoomStart;
        private ScaleGestureDetector mScaleDetector;
        private CameraState mState;
        private float mThresholdCameraZoom;
        private float mTouchDownCameraPosX;
        private float mTouchDownCameraPosY;
        private float mTouchDownCameraZoom;
        private float mTouchDownX;
        private float mTouchDownY;
        private int mTouchMoveStartMinThreshold;
        private int mWindowHeight;
        private int mWindowWidth;
        private float mZoomingStart;
        private OnScaleGestureListener onScaleGestureListener;

        /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewZoomPanning$MovingImageView$1 */
        class C05301 extends Handler {
            C05301() {
            }

            public void handleMessage(Message msg) {
                if (KeyguardEffectViewZoomPanning.this.mUpdateDelayed && MovingImageView.this.isUpdatableState()) {
                    if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                        Log.d("KeyguardEffectViewZoom", "handleMessage() : Execution of updates that have been delay");
                    }
                    KeyguardEffectViewZoomPanning.this.update();
                    return;
                }
                switch (MovingImageView.this.mState) {
                    case TouchRelease:
                        MovingImageView.this.zoomAnimation(MovingImageView.this.mTouchDownCameraZoom);
                        MovingImageView.this.panningAnimation();
                        break;
                    case ZoomIn:
                        MovingImageView.this.zoomAnimation(MovingImageView.this.getDefaultCameraZoomByCategory());
                        MovingImageView.this.panningAnimation();
                        break;
                    case PanningRandom:
                    case PanningFace:
                        MovingImageView.this.panningAnimation();
                        break;
                    case Overlap:
                        MovingImageView.this.overlapAnimation();
                        MovingImageView.this.panningAnimation();
                        break;
                    case ZoomOut:
                        MovingImageView.this.zoomAnimation(1.0f);
                        MovingImageView.this.panningAnimation();
                        break;
                    case Unlock:
                        if (!KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                            MovingImageView.this.restoreAnimationInformation();
                            break;
                        }
                        MovingImageView.this.setGoalOfDoubleTabPause();
                        MovingImageView.this.mCurrentAnimationFrame = MovingImageView.this.mGoalAnimationFrame - 1;
                        MovingImageView.this.mState = CameraState.TouchRelease;
                        break;
                }
                MovingImageView.this.invalidate();
                if (MovingImageView.this.mIsAnimating) {
                    sendEmptyMessageDelayed(0, 25);
                }
            }
        }

        /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewZoomPanning$MovingImageView$2 */
        class C05312 extends Handler {
            C05312() {
            }

            public void dispatchMessage(Message msg) {
                MovingImageView.this.mIsDoubleTapWaiting = false;
            }
        }

        /* renamed from: com.android.keyguard.sec.effect.KeyguardEffectViewZoomPanning$MovingImageView$3 */
        class C05323 implements OnScaleGestureListener {
            C05323() {
            }

            public void onScaleEnd(ScaleGestureDetector detector) {
                MovingImageView.this.mState = CameraState.TouchDown;
                if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                    Log.d("KeyguardEffectViewZoom", "end pinch zoom");
                }
            }

            public boolean onScaleBegin(ScaleGestureDetector detector) {
                if (MovingImageView.this.mState != CameraState.Overlap) {
                    MovingImageView.this.mState = CameraState.PinchZoom;
                    if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                        Log.d("KeyguardEffectViewZoom", "start pinch zoom");
                    }
                }
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                return true;
            }
        }

        public MovingImageView(KeyguardEffectViewZoomPanning keyguardEffectViewZoomPanning, Context context) {
            this(context, null);
        }

        public MovingImageView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mMovingCoe = 0.0f;
            this.mCurrentAnimationFrame = 0;
            this.mGoalAnimationFrame = 0;
            this.mPanningStartX = 0.0f;
            this.mPanningStartY = 0.0f;
            this.mPanningGoalX = 0.0f;
            this.mPanningGoalY = 0.0f;
            this.mZoomingStart = 0.0f;
            this.mTouchDownX = 0.0f;
            this.mTouchDownY = 0.0f;
            this.mLastTouchX = 0.0f;
            this.mLastTouchY = 0.0f;
            this.mDistance = 0.0f;
            this.mSavedPanningStartX = 0.0f;
            this.mSavedPanningStartY = 0.0f;
            this.mSavedPanningGoalX = 0.0f;
            this.mSavedPanningGoalY = 0.0f;
            this.mSavedZoomStart = 0.0f;
            this.mSavedCurrentAnimationFrame = 0;
            this.mSavedGoalAnimationFrame = 0;
            this.mSavedState = CameraState.ZoomIn;
            this.mPanningInterpolator = new SineInOut33();
            this.mIsDoubleTapWaiting = false;
            this.mPinchZoomPivotX = 0.0f;
            this.mPinchZoomPivotY = 0.0f;
            this.mLastPinchDistance = 0.0f;
            this.mCurrentCategoryCode = null;
            this.mIsPetImage = false;
            this.mAnimationHandler = new C05301();
            this.mDoubleTapCheckHandler = new C05312();
            this.onScaleGestureListener = new C05323();
            DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
            this.mWindowHeight = displaymetrics.heightPixels;
            this.mWindowWidth = displaymetrics.widthPixels;
            if (KeyguardProperties.hasCocktailBar(this.mContext)) {
                this.mWindowWidth = (int) (((float) this.mWindowWidth) + 160.0f);
                if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                    Log.i("KeyguardEffectViewZoom", "cocktailBarSize = " + 160.0f);
                }
            }
            this.mTouchMoveStartMinThreshold = (int) (((float) this.mWindowWidth) * 0.05f);
            this.mPinchZoomStartMinThreshold = (int) (((float) this.mWindowWidth) * 0.1f);
            this.SECOND_BORDER = (int) context.getResources().getDimension(C0302R.dimen.keyguard_lockscreen_second_border);
            this.mPanningGoalIndexList = new ArrayList();
            this.mCurrentBitmapPath = null;
            this.mThresholdCameraZoom = 1.0f;
            this.mIsDoubleTapWaiting = false;
            this.mScaleDetector = new ScaleGestureDetector(context, this.onScaleGestureListener);
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.i("KeyguardEffectViewZoom", "mWindowWidth : " + this.mWindowWidth + ", mWindowHeight : " + this.mWindowHeight);
            }
            this.mIsOverlap = false;
            this.mBitmap = null;
            this.mOverlapBitmapDrawingPaint = new Paint();
            this.mState = CameraState.ZoomIn;
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.i("KeyguardEffectViewZoom", "MovingImageView() mState = " + this.mState);
            }
            initBitmap();
            updateFaceList();
            initCamera();
            updateGoalList();
            this.mIsAnimating = false;
        }

        public boolean initBitmap() {
            Bitmap newBitmap;
            String newBitmapPath;
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "MovingImageView initBitmap()");
            }
            if (KeyguardEffectViewZoomPanning.this.mMusicWallpaper != null) {
                newBitmap = KeyguardEffectViewZoomPanning.this.mMusicWallpaper;
                newBitmapPath = KeyguardEffectViewZoomPanning.this.mStrMusicBitmapId;
            } else {
                newBitmapPath = System.getStringForUser(this.mContext.getContentResolver(), "lockscreen_wallpaper_path", -2);
                newBitmap = KeyguardEffectViewUtil.getCurrentWallpaper(this.mContext, newBitmapPath, KeyguardEffectViewZoomPanning.this.mDisplayId).getBitmap();
            }
            if (this.mCurrentBitmapPath != null && this.mCurrentBitmapPath.equals(newBitmapPath)) {
                if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                    Log.d("KeyguardEffectViewZoom", "initBitmap() : same bitmap");
                }
                return false;
            } else if (this.mOverlapBitmap == null || !this.mOverlapBitmap.sameAs(newBitmap)) {
                if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                    Log.d("KeyguardEffectViewZoom", "mCurrentBitmapPath() = " + this.mCurrentBitmapPath + ", newBitmapPath = " + newBitmapPath);
                }
                if (this.mBitmap == null) {
                    this.mBitmap = newBitmap;
                    this.mCurrentBitmapPath = newBitmapPath;
                } else if (newBitmap == null) {
                    Log.e("KeyguardEffectViewZoom", "initBitmap() : new bitmap is null");
                    return false;
                } else {
                    if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                        Log.d("KeyguardEffectViewZoom", "initBitmap() : overlap bitmap, mIsAnimating = " + this.mIsAnimating);
                    }
                    if (this.mOverlapBitmap == null) {
                        replaceBitmapAndCameraData();
                    }
                    this.mIsOverlap = true;
                    this.mBitmap = newBitmap;
                    this.mCurrentBitmapPath = newBitmapPath;
                }
                if (this.mBitmap != null) {
                    this.mBitmapHeight = this.mBitmap.getHeight();
                    this.mBitmapWidth = this.mBitmap.getWidth();
                    if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                        Log.i("KeyguardEffectViewZoom", "mBitmapWidth : " + this.mBitmapWidth + ", mBitmapHeight : " + this.mBitmapHeight);
                    }
                    this.mCurrentCategoryCode = System.getStringForUser(this.mContext.getContentResolver(), KEY_WALLPAPER_INFO_CATEGORY_CODE, -2);
                    if (this.mCurrentCategoryCode == null || this.mCurrentCategoryCode.isEmpty() || !CATEGORY_CODE_PET.equalsIgnoreCase(this.mCurrentCategoryCode)) {
                        this.mIsPetImage = false;
                    } else {
                        this.mIsPetImage = true;
                    }
                    return true;
                }
                this.mBitmapHeight = 0;
                this.mBitmapWidth = 0;
                Log.e("KeyguardEffectViewZoom", "mBitmap is null!");
                return false;
            } else {
                if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                    Log.d("KeyguardEffectViewZoom", "initBitmap() : revert bitmap");
                }
                revertBitmapToOverlapBitmap();
                this.mCurrentBitmapPath = newBitmapPath;
                this.mState = CameraState.ZoomOut;
                setGoalOfDoubleTabPause();
                return false;
            }
        }

        public void initCamera() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "MovingImageView initCamera() : mIsOverlap " + this.mIsOverlap + ", mIsDoubleTapPuase : " + KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase);
            }
            float windowAspectRatio = ((float) this.mWindowHeight) / ((float) this.mWindowWidth);
            if (((float) this.mBitmapHeight) / ((float) this.mBitmapWidth) <= windowAspectRatio) {
                this.mDefaultCameraHeight = this.mBitmapHeight;
                this.mDefaultCameraWidth = (int) (((float) this.mDefaultCameraHeight) / windowAspectRatio);
            } else {
                this.mDefaultCameraWidth = this.mBitmapWidth;
                this.mDefaultCameraHeight = (int) (((float) this.mDefaultCameraWidth) * windowAspectRatio);
            }
            if (this.mDefaultCameraWidth > this.mDefaultCameraHeight) {
                int swapTemp = this.mDefaultCameraHeight;
                this.mDefaultCameraHeight = this.mDefaultCameraWidth;
                this.mDefaultCameraWidth = swapTemp;
            }
            this.mMovingCoe = ((float) this.mDefaultCameraHeight) / BASIC_IMAGE_SIZE;
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.i("KeyguardEffectViewZoom", "mDefaultCameraWidth : " + this.mDefaultCameraWidth + ", mDefaultCameraHeight : " + this.mDefaultCameraHeight);
            }
            if (KeyguardEffectViewZoomPanning.this.mMusicWallpaper != null || KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                this.mState = CameraState.Overlap;
                this.mIsOverlap = false;
                this.mCameraCenterPosX = ((float) this.mBitmapWidth) * 0.5f;
                this.mCameraCenterPosY = ((float) this.mBitmapHeight) * 0.5f;
                return;
            }
            if (this.mIsOverlap) {
                this.mState = CameraState.Overlap;
                this.mCameraZoomIn = this.mZoomingStart;
                this.mIsOverlap = false;
            } else {
                this.mState = CameraState.ZoomIn;
                this.mCameraZoomIn = 1.0f;
            }
            int diffHeight = this.mBitmapHeight - this.mDefaultCameraHeight;
            if (diffHeight < 0) {
                diffHeight = 0;
            }
            if (getRandom(0.0f, 2.0f) == 0) {
                this.mCameraCenterPosX = ((float) this.mDefaultCameraWidth) * 0.5f;
            } else {
                this.mCameraCenterPosX = ((float) this.mBitmapWidth) - (((float) this.mDefaultCameraWidth) * 0.5f);
            }
            this.mCameraCenterPosY = ((float) (this.mDefaultCameraHeight + diffHeight)) * 0.5f;
        }

        private void updateFaceList() {
        }

        private void sortFaceListClockwise() {
        }

        private void updateGoalList() {
            this.mPanningGoalIndexList.clear();
            for (int iIdx = this.mPanningGoalIndexList.size(); iIdx < 2; iIdx++) {
                this.mPanningGoalIndexList.add(Integer.valueOf(-1));
            }
            this.mNowGoalIndex = findNearestFaceIndexFromCurrentCameraPos();
            if (this.mState == CameraState.Overlap) {
                setGoalOfOverlap();
            } else if (KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                setGoalOfDoubleTabPause();
            } else {
                setGoalOfPanning();
            }
        }

        private int findNearestFaceIndexFromCurrentCameraPos() {
            return 0;
        }

        private Point makeRandomPanningPoint() {
            boolean isLeftSide = this.mCameraCenterPosX <= ((float) this.mBitmapWidth) / 2.0f;
            float cameraZoom = this.mState == CameraState.PanningRandom ? 1.0f : getDefaultCameraZoomByCategory();
            float leftBorder = (((float) this.mDefaultCameraWidth) / cameraZoom) * 0.5f;
            float rightBorder = ((float) this.mBitmapWidth) - leftBorder;
            float topBorder = (((float) this.mDefaultCameraHeight) / cameraZoom) * 0.5f;
            float bottomBorder = ((float) this.mBitmapHeight) - topBorder;
            if (isLeftSide) {
                leftBorder += ((float) this.mBitmapWidth) / 2.0f;
            } else {
                rightBorder -= ((float) this.mBitmapWidth) / 2.0f;
            }
            if (rightBorder < leftBorder) {
                if (isLeftSide) {
                    leftBorder = rightBorder;
                } else {
                    rightBorder = leftBorder;
                }
            }
            return new Point(getRandom(leftBorder, rightBorder), getRandom(topBorder, bottomBorder));
        }

        public boolean startAnimation() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "startAnimation() mIsAnimating = " + this.mIsAnimating + " -> true, mState : " + this.mState);
            }
            if (this.mIsAnimating) {
                return false;
            }
            this.mAnimationHandler.removeMessages(0);
            this.mIsAnimating = true;
            this.mAnimationHandler.sendEmptyMessage(0);
            return true;
        }

        public boolean stopAnimation() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "stopAnimation() mIsAnimating = " + this.mIsAnimating + " -> false, mState : " + this.mState);
            }
            if (!this.mIsAnimating) {
                return false;
            }
            this.mIsAnimating = false;
            this.mAnimationHandler.removeMessages(0);
            return true;
        }

        private void overlapAnimation() {
            this.mCameraZoomIn = this.mZoomingStart + ((1.0f - this.mZoomingStart) * this.mPanningInterpolator.getInterpolation(((float) this.mCurrentAnimationFrame) / ((float) this.mGoalAnimationFrame)));
            correctCameraLocation();
        }

        private void panningAnimation() {
            if (this.mCurrentAnimationFrame >= this.mGoalAnimationFrame) {
                if (this.mState != CameraState.TouchRelease) {
                    setGoalOfPanning();
                }
                CameraState prevState = this.mState;
                switch (this.mState) {
                    case TouchRelease:
                        if (KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                            this.mState = CameraState.DoubleTapPause;
                            return;
                        } else {
                            restoreAnimationInformation();
                            return;
                        }
                    case ZoomIn:
                        this.mState = CameraState.PanningRandom;
                        break;
                    case PanningRandom:
                        this.mState = CameraState.ZoomOut;
                        break;
                    case PanningFace:
                        this.mState = CameraState.PanningRandom;
                        break;
                    case Overlap:
                        this.mOverlapBitmap = null;
                        break;
                    case ZoomOut:
                        break;
                    default:
                        this.mState = CameraState.PanningRandom;
                        break;
                }
                if (KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                    this.mState = CameraState.DoubleTapPause;
                } else {
                    this.mState = CameraState.ZoomIn;
                }
                if ((this.mState == CameraState.PanningRandom || this.mState == CameraState.ZoomOut) && !this.mPanningRandom) {
                    this.mState = CameraState.PanningFace;
                }
                if (this.mState == CameraState.ZoomIn && !this.mPanningRandom) {
                    this.mGoalAnimationFrame = FACE_ZOOMIN_ANIMATION_FRAME;
                }
                Log.d("KeyguardEffectViewZoom", "panningAnimation() state change :" + prevState + " -> " + this.mState);
            }
            this.mCurrentAnimationFrame++;
            this.mCameraCenterPosX = this.mPanningStartX + ((this.mPanningGoalX - this.mPanningStartX) * this.mPanningInterpolator.getInterpolation(((float) this.mCurrentAnimationFrame) / ((float) this.mGoalAnimationFrame)));
            this.mCameraCenterPosY = this.mPanningStartY + ((this.mPanningGoalY - this.mPanningStartY) * this.mPanningInterpolator.getInterpolation(((float) this.mCurrentAnimationFrame) / ((float) this.mGoalAnimationFrame)));
            correctCameraLocation();
        }

        private void zoomAnimation(float goalZoom) {
            this.mCameraZoomIn = this.mZoomingStart + ((goalZoom - this.mZoomingStart) * this.mPanningInterpolator.getInterpolation(((float) this.mCurrentAnimationFrame) / ((float) this.mGoalAnimationFrame)));
            correctCameraLocation();
        }

        private void zoomAnimationByValue(float zoomValue) {
            this.mCameraZoomIn += zoomValue;
            if (getMaximumCameraZoomByCategory() < this.mCameraZoomIn) {
                this.mCameraZoomIn = getMaximumCameraZoomByCategory();
            } else if (this.mCameraZoomIn < 1.0f) {
                this.mCameraZoomIn = 1.0f;
            }
            correctCameraLocation();
        }

        private void zoomAnimationByRate(float zoomRate) {
            this.mCameraZoomIn *= 1.0f + zoomRate;
            if (getMaximumCameraZoomByCategory() < this.mCameraZoomIn) {
                this.mCameraZoomIn = getMaximumCameraZoomByCategory();
            } else if (this.mCameraZoomIn < 1.0f) {
                this.mCameraZoomIn = 1.0f;
            }
            correctCameraLocation();
        }

        private void handleDoubleTap() {
            if (KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase = false;
                this.mState = CameraState.ZoomIn;
                updateGoalList();
            } else {
                KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase = true;
                this.mState = CameraState.ZoomOut;
                setGoalOfDoubleTabPause();
            }
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "handleDoubleTap() mIsAnimating : " + this.mIsAnimating + ", mIsDoubleTapPuase : " + KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase);
            }
        }

        private void saveCurrentAnimationInformation() {
            this.mSavedPanningStartX = this.mPanningStartX;
            this.mSavedPanningStartY = this.mPanningStartY;
            this.mSavedPanningGoalX = this.mPanningGoalX;
            this.mSavedPanningGoalY = this.mPanningGoalY;
            this.mSavedZoomStart = this.mZoomingStart;
            this.mSavedCurrentAnimationFrame = this.mCurrentAnimationFrame;
            this.mSavedGoalAnimationFrame = this.mGoalAnimationFrame;
            this.mSavedState = this.mState;
        }

        private void restoreAnimationInformation() {
            this.mPanningStartX = this.mSavedPanningStartX;
            this.mPanningStartY = this.mSavedPanningStartY;
            this.mPanningGoalX = this.mSavedPanningGoalX;
            this.mPanningGoalY = this.mSavedPanningGoalY;
            this.mZoomingStart = this.mSavedZoomStart;
            this.mCurrentAnimationFrame = this.mSavedCurrentAnimationFrame;
            this.mGoalAnimationFrame = this.mSavedGoalAnimationFrame;
            this.mState = this.mSavedState;
        }

        private float getDefaultCameraZoomByCategory() {
            if (this.mIsPetImage) {
                return 1.2f;
            }
            return 1.5f;
        }

        private float getMaximumCameraZoomByCategory() {
            if (this.mIsPetImage) {
                return 1.5f;
            }
            return MAXIMUM_CAMERA_ZOOM;
        }

        private void replaceBitmapAndCameraData() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "replaceBitmapAndCameraData()");
            }
            this.mOverlapBitmap = null;
            this.mOverlapBitmapHeight = this.mBitmapHeight;
            this.mOverlapBitmapWidth = this.mBitmapWidth;
            this.mDefaultOverlapCameraHeight = this.mDefaultCameraHeight;
            this.mDefaultOverlapCameraWidth = this.mDefaultCameraWidth;
            this.mOverlapCameraZoomIn = this.mCameraZoomIn;
            this.mOverlapCameraCenterPosX = this.mCameraCenterPosX;
            this.mOverlapCameraCenterPosY = this.mCameraCenterPosY;
            this.mZoomingStart = this.mCameraZoomIn;
            this.mOverlapBitmap = this.mBitmap;
        }

        private void revertBitmapToOverlapBitmap() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "revertBitmapToOverlapBitmap()");
            }
            this.mBitmapHeight = this.mOverlapBitmapHeight;
            this.mBitmapWidth = this.mOverlapBitmapWidth;
            this.mDefaultCameraHeight = this.mDefaultOverlapCameraHeight;
            this.mDefaultCameraWidth = this.mDefaultOverlapCameraWidth;
            this.mCameraZoomIn = this.mOverlapCameraZoomIn;
            this.mCameraCenterPosX = this.mOverlapCameraCenterPosX;
            this.mCameraCenterPosY = this.mOverlapCameraCenterPosY;
            this.mCameraZoomIn = this.mZoomingStart;
            this.mBitmap = this.mOverlapBitmap;
            this.mOverlapBitmap = null;
        }

        private void setGoalOfPanning() {
            this.mCurrentAnimationFrame = 0;
            this.mZoomingStart = this.mCameraZoomIn;
            this.mPanningStartX = this.mCameraCenterPosX;
            this.mPanningStartY = this.mCameraCenterPosY;
            int faceGoalIndex = ((Integer) this.mPanningGoalIndexList.get(this.mNowGoalIndex)).intValue();
            Point goalPoint = makeRandomPanningPoint();
            this.mGoalAnimationFrame = DEFAULT_ANIMATION_FRAME;
            this.mPanningRandom = true;
            this.mPanningGoalX = (float) goalPoint.x;
            this.mPanningGoalY = (float) goalPoint.y;
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "setGoalOfPanning() faceGoalIndex : " + faceGoalIndex + ", go to (" + goalPoint.x + ", " + goalPoint.y + ")");
            }
            float defaultCameraHalfWidth = (((float) this.mDefaultCameraWidth) / getDefaultCameraZoomByCategory()) * 0.5f;
            float defaultCameraHalfHeight = (((float) this.mDefaultCameraHeight) / getDefaultCameraZoomByCategory()) * 0.5f;
            if (this.mPanningGoalX < defaultCameraHalfWidth) {
                this.mPanningGoalX = defaultCameraHalfWidth;
            } else if (this.mPanningGoalX + defaultCameraHalfWidth >= ((float) this.mBitmapWidth)) {
                this.mPanningGoalX = ((float) this.mBitmapWidth) - defaultCameraHalfWidth;
            }
            if (this.mPanningGoalY < defaultCameraHalfHeight) {
                this.mPanningGoalY = defaultCameraHalfHeight;
            } else if (this.mPanningGoalY + defaultCameraHalfHeight >= ((float) this.mBitmapHeight)) {
                this.mPanningGoalY = ((float) this.mBitmapHeight) - defaultCameraHalfHeight;
            }
            this.mNowGoalIndex++;
            if (this.mNowGoalIndex >= this.mPanningGoalIndexList.size()) {
                this.mNowGoalIndex = 0;
            }
        }

        private void setGoalOfOverlap() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "setGoalOfOverlap()");
            }
            this.mZoomingStart = this.mCameraZoomIn;
            float f = this.mCameraCenterPosX;
            this.mPanningGoalX = f;
            this.mPanningStartX = f;
            f = this.mCameraCenterPosY;
            this.mPanningGoalY = f;
            this.mPanningStartY = f;
            this.mCurrentAnimationFrame = 0;
            this.mGoalAnimationFrame = OVERLAP_ANIMATION_FRAME;
        }

        private void setGoalOfDoubleTabPause() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "setGoalOfDoubleTabPause()");
            }
            this.mZoomingStart = this.mCameraZoomIn;
            this.mPanningStartX = this.mCameraCenterPosX;
            this.mPanningStartY = this.mCameraCenterPosY;
            this.mPanningGoalX = ((float) this.mBitmapWidth) * 0.5f;
            this.mPanningGoalY = ((float) this.mBitmapHeight) * 0.5f;
            this.mTouchDownCameraZoom = 1.0f;
            this.mCurrentAnimationFrame = 0;
            this.mGoalAnimationFrame = 8;
        }

        private void setGoalOfTouchRelease() {
            if (KeyguardEffectViewZoomPanning.this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "setGoalOfTouchRelease()");
            }
            this.mZoomingStart = this.mCameraZoomIn;
            this.mPanningStartX = this.mCameraCenterPosX;
            this.mPanningStartY = this.mCameraCenterPosY;
            this.mPanningGoalX = this.mTouchDownCameraPosX;
            this.mPanningGoalY = this.mTouchDownCameraPosY;
            this.mCurrentAnimationFrame = 0;
            this.mGoalAnimationFrame = 8;
        }

        public void correctCameraLocation() {
            float cameraHalfWidth = (((float) this.mDefaultCameraWidth) / this.mCameraZoomIn) * 0.5f;
            float cameraHalfHeight = (((float) this.mDefaultCameraHeight) / this.mCameraZoomIn) * 0.5f;
            if (this.mCameraCenterPosX < cameraHalfWidth) {
                this.mCameraCenterPosX = cameraHalfWidth;
            } else if (this.mCameraCenterPosX + cameraHalfWidth >= ((float) this.mBitmapWidth)) {
                this.mCameraCenterPosX = ((float) this.mBitmapWidth) - cameraHalfWidth;
            }
            if (this.mCameraCenterPosY < cameraHalfHeight) {
                this.mCameraCenterPosY = cameraHalfHeight;
            } else if (this.mCameraCenterPosY + cameraHalfHeight >= ((float) this.mBitmapHeight)) {
                this.mCameraCenterPosY = ((float) this.mBitmapHeight) - cameraHalfHeight;
            }
        }

        public void correctOverlapCameraLocation() {
            float overlapCameraHalfWidth = (((float) this.mDefaultOverlapCameraWidth) / this.mCameraZoomIn) * 0.5f;
            float overlapCameraHalfHeight = (((float) this.mDefaultOverlapCameraHeight) / this.mCameraZoomIn) * 0.5f;
            if (this.mOverlapCameraCenterPosX < overlapCameraHalfWidth) {
                this.mOverlapCameraCenterPosX = overlapCameraHalfWidth;
            } else if (this.mOverlapCameraCenterPosX + overlapCameraHalfWidth >= ((float) this.mOverlapBitmapWidth)) {
                this.mOverlapCameraCenterPosX = ((float) this.mOverlapBitmapWidth) - overlapCameraHalfWidth;
            }
            if (this.mOverlapCameraCenterPosY < overlapCameraHalfHeight) {
                this.mOverlapCameraCenterPosY = overlapCameraHalfHeight;
            } else if (this.mOverlapCameraCenterPosY + overlapCameraHalfHeight >= ((float) this.mOverlapBitmapHeight)) {
                this.mOverlapCameraCenterPosY = ((float) this.mOverlapBitmapHeight) - overlapCameraHalfHeight;
            }
        }

        private void handlePinchZoom(MotionEvent event) {
            if (event.getPointerCount() >= 2) {
                float distance = calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                if (this.mLastPinchDistance == 0.0f) {
                    this.mLastPinchDistance = distance;
                }
                if (this.mLastPinchDistance >= ((float) this.mPinchZoomStartMinThreshold)) {
                    float scaleFactor = ((distance - this.mLastPinchDistance) * this.mMovingCoe) / ((float) this.mDefaultCameraWidth);
                    zoomAnimationByRate(scaleFactor);
                    if (scaleFactor > 0.0f) {
                        this.mCameraCenterPosX += (this.mPinchZoomPivotX - this.mCameraCenterPosX) * scaleFactor;
                        this.mCameraCenterPosY += (this.mPinchZoomPivotY - this.mCameraCenterPosY) * scaleFactor;
                    }
                    this.mLastPinchDistance = distance;
                }
            }
        }

        public boolean handleTouchEvent(MotionEvent event) {
            if (this.mState == CameraState.Unlock || this.mState == CameraState.Overlap) {
                return true;
            }
            if (event.getPointerCount() >= 2) {
                this.mScaleDetector.onTouchEvent(event);
            }
            float x;
            switch (event.getAction() & 255) {
                case 0:
                    if (this.mState != CameraState.TouchRelease) {
                        saveCurrentAnimationInformation();
                        this.mTouchDownCameraPosX = this.mCameraCenterPosX;
                        this.mTouchDownCameraPosY = this.mCameraCenterPosY;
                        this.mTouchDownCameraZoom = this.mCameraZoomIn;
                        this.mThresholdCameraZoom = this.mCameraZoomIn * 1.2f;
                    }
                    if (KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                        this.mState = CameraState.DoubleTapPause;
                    } else {
                        this.mState = CameraState.TouchDown;
                    }
                    x = event.getX();
                    this.mLastTouchX = x;
                    this.mTouchDownX = x;
                    x = event.getY();
                    this.mLastTouchY = x;
                    this.mTouchDownY = x;
                    this.mDistance = 0.0f;
                    break;
                case 1:
                case 3:
                    this.mState = CameraState.TouchRelease;
                    if (KeyguardEffectViewZoomPanning.this.mIsDoubleTapPuase) {
                        setGoalOfDoubleTabPause();
                    } else if (!(this.mState == CameraState.PanningRandom || this.mState == CameraState.PanningFace)) {
                        setGoalOfTouchRelease();
                    }
                    if (KeyguardEffectViewZoomPanning.this.mMusicWallpaper == null && event.getButtonState() == 0) {
                        if (!this.mIsDoubleTapWaiting) {
                            this.mIsDoubleTapWaiting = true;
                            this.mDoubleTapCheckHandler.removeMessages(0);
                            this.mDoubleTapCheckHandler.sendEmptyMessageDelayed(0, 300);
                            break;
                        }
                        this.mIsDoubleTapWaiting = false;
                        this.mDoubleTapCheckHandler.removeMessages(0);
                        handleDoubleTap();
                        break;
                    }
                case 2:
                    if (this.mState == CameraState.PinchZoom) {
                        handlePinchZoom(event);
                    }
                    int diffX = (int) (this.mLastTouchX - event.getX());
                    int diffY = (int) (this.mLastTouchY - event.getY());
                    this.mLastTouchX = event.getX();
                    this.mLastTouchY = event.getY();
                    float distance = calculateDistance(this.mTouchDownX, this.mTouchDownY, this.mLastTouchX, this.mLastTouchY);
                    if (distance >= ((float) this.mTouchMoveStartMinThreshold)) {
                        this.mIsDoubleTapWaiting = false;
                        this.mDoubleTapCheckHandler.removeMessages(0);
                        this.mCameraCenterPosX += (((float) diffX) * 0.5f) * this.mMovingCoe;
                        this.mCameraCenterPosY += (((float) diffY) * 0.5f) * this.mMovingCoe;
                        zoomAnimationByValue(((distance - this.mDistance) / ((float) this.SECOND_BORDER)) * (this.mThresholdCameraZoom - this.mTouchDownCameraZoom));
                        this.mDistance = distance;
                        correctCameraLocation();
                        break;
                    }
                    break;
                case 5:
                    float heightFact = (((float) this.mDefaultCameraHeight) / this.mCameraZoomIn) / ((float) this.mWindowHeight);
                    float cameraHalfWidth = (((float) this.mDefaultCameraWidth) / this.mCameraZoomIn) * 0.5f;
                    float cameraHalfHeight = (((float) this.mDefaultCameraHeight) / this.mCameraZoomIn) * 0.5f;
                    this.mPinchZoomPivotX = ((((this.mTouchDownX + event.getX(1)) * 0.5f) * ((((float) this.mDefaultCameraWidth) / this.mCameraZoomIn) / ((float) this.mWindowWidth))) + this.mCameraCenterPosX) - cameraHalfWidth;
                    this.mPinchZoomPivotY = ((((this.mTouchDownY + event.getY(1)) * 0.5f) * heightFact) + this.mCameraCenterPosY) - cameraHalfHeight;
                    this.mLastPinchDistance = 0.0f;
                    break;
                case 6:
                    for (int iIdx = 0; iIdx < event.getPointerCount(); iIdx++) {
                        if (iIdx != event.getActionIndex()) {
                            x = event.getX(iIdx);
                            this.mLastTouchX = x;
                            this.mTouchDownX = x;
                            x = event.getY(iIdx);
                            this.mLastTouchY = x;
                            this.mTouchDownY = x;
                            break;
                        }
                    }
                    break;
            }
            invalidate();
            return true;
        }

        protected void onDraw(Canvas canvas) {
            if (this.mBitmap != null) {
                float canvasScaleFact = getCanvasScale(false);
                float canvasTranslateX = getCanvasTranslate(false, true);
                float canvasTranslateY = getCanvasTranslate(false, false);
                canvas.save();
                canvas.scale(canvasScaleFact, canvasScaleFact);
                canvas.translate(-canvasTranslateX, -canvasTranslateY);
                canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, null);
                canvas.restore();
                if (this.mState == CameraState.Overlap && this.mOverlapBitmap != null) {
                    this.mOverlapBitmapDrawingPaint.setAlpha((int) ((1.0f - (((float) this.mCurrentAnimationFrame) / ((float) this.mGoalAnimationFrame))) * 255.0f));
                    float canvasOverlapScaleFact = getCanvasScale(true);
                    float canvasOverlapTranslateX = getCanvasTranslate(true, true);
                    float canvasOverlapTranslateY = getCanvasTranslate(true, false);
                    canvas.save();
                    canvas.scale(canvasOverlapScaleFact, canvasOverlapScaleFact);
                    canvas.translate(-canvasOverlapTranslateX, -canvasOverlapTranslateY);
                    canvas.drawBitmap(this.mOverlapBitmap, 0.0f, 0.0f, this.mOverlapBitmapDrawingPaint);
                    canvas.restore();
                }
            }
        }

        public String getCurrentBitmapPath() {
            return this.mCurrentBitmapPath;
        }

        private int getRandom(float min, float max) {
            return (int) ((Math.random() * ((double) (max - min))) + ((double) min));
        }

        private float calculateDistance(float firstX, float firstY, float lastX, float lastY) {
            float diffX = Math.abs(lastX - firstX);
            float diffY = Math.abs(lastY - firstY);
            return (float) Math.sqrt((double) ((diffX * diffX) + (diffY * diffY)));
        }

        private int calculateAnimationFrame(float startX, float startY, float endX, float endY) {
            return (int) ((1000.0f * calculateDistance(startX, startY, endX, endY)) / ((float) this.mBitmapWidth));
        }

        private float getCanvasTranslate(boolean isOverlapCamera, boolean isX) {
            if (isX) {
                float cameraHalfWidth;
                float canvasTranslateX;
                float bitmapWidth;
                if (isOverlapCamera) {
                    cameraHalfWidth = (((float) this.mDefaultOverlapCameraWidth) / this.mOverlapCameraZoomIn) * 0.5f;
                    canvasTranslateX = this.mOverlapCameraCenterPosX - cameraHalfWidth;
                    bitmapWidth = (float) this.mOverlapBitmapWidth;
                } else {
                    cameraHalfWidth = (((float) this.mDefaultCameraWidth) / this.mCameraZoomIn) * 0.5f;
                    canvasTranslateX = this.mCameraCenterPosX - cameraHalfWidth;
                    bitmapWidth = (float) this.mBitmapWidth;
                }
                if (canvasTranslateX < 0.0f) {
                    return 0.0f;
                }
                if (canvasTranslateX > bitmapWidth - (cameraHalfWidth * 2.0f)) {
                    return bitmapWidth - (cameraHalfWidth * 2.0f);
                }
                return canvasTranslateX;
            }
            float cameraHalfHeight;
            float canvasTranslateY;
            float bitmapHeight;
            if (isOverlapCamera) {
                cameraHalfHeight = (((float) this.mDefaultOverlapCameraHeight) / this.mOverlapCameraZoomIn) * 0.5f;
                canvasTranslateY = this.mOverlapCameraCenterPosY - cameraHalfHeight;
                bitmapHeight = (float) this.mOverlapBitmapHeight;
            } else {
                cameraHalfHeight = (((float) this.mDefaultCameraHeight) / this.mCameraZoomIn) * 0.5f;
                canvasTranslateY = this.mCameraCenterPosY - cameraHalfHeight;
                bitmapHeight = (float) this.mBitmapHeight;
            }
            if (canvasTranslateY < 0.0f) {
                canvasTranslateY = 0.0f;
            } else if (canvasTranslateY > bitmapHeight - (cameraHalfHeight * 2.0f)) {
                canvasTranslateY = bitmapHeight - (cameraHalfHeight * 2.0f);
            }
            return canvasTranslateY;
        }

        private float getCanvasScale(boolean isOverlapCamera) {
            float cameraWidth;
            if (isOverlapCamera) {
                cameraWidth = ((float) this.mDefaultOverlapCameraWidth) / this.mOverlapCameraZoomIn;
            } else {
                cameraWidth = ((float) this.mDefaultCameraWidth) / this.mCameraZoomIn;
            }
            return ((float) this.mWindowWidth) / cameraWidth;
        }

        public boolean isUpdatableState() {
            boolean z = false;
            if (this.mState != CameraState.Overlap) {
                if (this.mState == CameraState.PanningRandom || this.mState == CameraState.PanningFace || this.mState == CameraState.ZoomIn || this.mState == CameraState.ZoomOut || this.mState == CameraState.Unlock || this.mState == CameraState.TouchRelease || this.mState == CameraState.DoubleTapPause) {
                    z = true;
                }
            }
            return z;
        }

        public void handleUnlock() {
            if (this.mState != CameraState.Overlap) {
                this.mState = CameraState.Unlock;
            }
        }

        public void cleanUp() {
            this.mOverlapBitmap = null;
            KeyguardEffectViewZoomPanning.this.mMusicWallpaper = null;
        }

        public boolean isAnimating() {
            return this.mIsAnimating;
        }
    }

    public KeyguardEffectViewZoomPanning(Context context) {
        this(context, null, false, 0);
    }

    public KeyguardEffectViewZoomPanning(Context context, KeyguardWindowCallback callback) {
        this(context, callback, false, 0);
    }

    public KeyguardEffectViewZoomPanning(Context context, KeyguardWindowCallback callback, boolean isProcessSeparated, int displayId) {
        super(context);
        this.TAG = "KeyguardEffectViewZoom";
        this.DEBUG = KeyguardHostView.DEBUG;
        this.mDisplayId = -1;
        this.mMovableImageView = null;
        this.mWidgetReleaseInterpolator = new SineEaseInOut();
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "Constructor()");
        }
        this.mContext = context;
        this.mDisplayId = displayId;
        this.mMovableImageView = new MovingImageView(this, context);
        this.mUpdateDelayed = false;
        this.mMusicWallpaper = null;
        this.mStrMusicBitmapId = null;
        this.mIsRemoveMusicBitmapByScreenOff = false;
        this.mIsForceUpdateMusicBitmap = false;
        this.mNotificationPanel = null;
        addView(this.mMovableImageView, -1, -1);
    }

    public void show() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "show()");
        }
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        } else {
            this.mMovableImageView.startAnimation();
        }
    }

    public void reset() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "reset()");
        }
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        }
    }

    public void cleanUp() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "cleanUp()");
        }
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        } else {
            this.mMovableImageView.stopAnimation();
        }
    }

    public void update() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "update()");
        }
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        } else if (this.mMovableImageView.isUpdatableState() || this.mIsForceUpdateMusicBitmap) {
            this.mUpdateDelayed = false;
            this.mIsForceUpdateMusicBitmap = false;
            boolean isSuccessStop = this.mMovableImageView.stopAnimation();
            if (this.mMovableImageView.initBitmap()) {
                this.mMovableImageView.updateFaceList();
                this.mMovableImageView.initCamera();
                this.mMovableImageView.updateGoalList();
            }
            if (isSuccessStop) {
                this.mMovableImageView.startAnimation();
            }
        } else if (!isSameBitmap()) {
            if (this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "update() : is not updatable state");
            }
            this.mUpdateDelayed = true;
        } else if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "update() : no need to update because of same bitmap");
        }
    }

    public void screenTurnedOn() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "screenTurnedOn()");
        }
        startAffordanceAnimation();
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        } else {
            this.mMovableImageView.startAnimation();
        }
    }

    public void screenTurnedOff() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "screenTurnedOff()");
        }
        startResetTranlateAnimation();
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
            return;
        }
        this.mMovableImageView.stopAnimation();
        if (this.mUpdateDelayed) {
            if (this.DEBUG) {
                Log.d("KeyguardEffectViewZoom", "screenTurnedOff() running delayed update");
            }
            update();
        }
    }

    public void showUnlockAffordance(long startDelay, Rect rect) {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "showUnlockAffordance()");
        }
    }

    public long getUnlockDelay() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "getUnlockDelay()");
        }
        return 0;
    }

    public void handleUnlock(View view, MotionEvent event) {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "handleUnlock()");
        }
        startResetTranlateAnimation();
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        } else {
            this.mMovableImageView.handleUnlock();
        }
    }

    public void playLockSound() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "playLockSound()");
        }
    }

    public boolean handleTouchEvent(View view, MotionEvent event) {
        switch (event.getAction() & 255) {
            case 0:
                this.mTouchStartX = event.getX();
                this.mTouchStartY = event.getY();
                if (this.mBottomArea != null) {
                    this.mBottomArea.animate().alpha(0.0f);
                    break;
                }
                break;
            case 1:
                startResetTranlateAnimation();
                if (this.mBottomArea != null) {
                    this.mBottomArea.animate().alpha(1.0f);
                    break;
                }
                break;
            case 2:
                if (event.getPointerCount() == 1) {
                    startWidgetTranslateAnimation((event.getX() - this.mTouchStartX) * 0.25f, (event.getY() - this.mTouchStartY) * 0.25f);
                    break;
                }
                break;
            case 5:
                startResetTranlateAnimation();
                break;
            case 6:
                for (int iIdx = 0; iIdx < event.getPointerCount(); iIdx++) {
                    if (iIdx != event.getActionIndex()) {
                        this.mTouchStartX = event.getX(iIdx);
                        this.mTouchStartY = event.getY(iIdx);
                        break;
                    }
                }
                break;
        }
        if (this.mMovableImageView != null) {
            return this.mMovableImageView.handleTouchEvent(event);
        }
        Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
        return false;
    }

    public boolean handleTouchEventForPatternLock(View view, MotionEvent event) {
        return false;
    }

    public void setHidden(boolean isHidden) {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "setHidden()");
        }
    }

    public boolean handleHoverEvent(View view, MotionEvent event) {
        return false;
    }

    private void startAffordanceAnimation() {
        if (this.mNotificationPanel == null || this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "startAffordanceAnimation() view is null");
            return;
        }
        this.mNotificationPanel.setScaleX(1.2f);
        this.mNotificationPanel.setScaleY(1.2f);
        this.mNotificationPanel.setAlpha(0.0f);
        this.mNotificationPanel.animate().scaleX(1.0f).scaleY(1.0f).setDuration(1000).setStartDelay(0);
        this.mNotificationPanel.animate().alpha(1.0f).setStartDelay(166).setDuration(500).setStartDelay(0);
        this.mMovableImageView.setScaleX(1.05f);
        this.mMovableImageView.setScaleY(1.05f);
        this.mMovableImageView.setAlpha(0.6f);
        this.mMovableImageView.animate().scaleX(1.0f).scaleY(1.0f).setStartDelay(166).setDuration(834).setStartDelay(0);
        this.mMovableImageView.animate().alpha(1.0f).setDuration(666).setStartDelay(0);
        this.mBottomArea = this.mNotificationPanel.findViewById(C0302R.id.sec_keyguard_bottom_area);
    }

    private void startWidgetTranslateAnimation(float translateX, float translateY) {
        if (this.mNotificationPanel == null) {
            Log.e("KeyguardEffectViewZoom", "startWidgetTranslateAnimation() mNotificationStack is null");
            return;
        }
        this.mNotificationPanel.setX(translateX);
        this.mNotificationPanel.setY(translateY);
    }

    private void startResetTranlateAnimation() {
        if (this.mNotificationPanel == null) {
            Log.e("KeyguardEffectViewZoom", "startResetTranlateAnimation() view is null");
        } else {
            this.mNotificationPanel.animate().x(0.0f).y(0.0f).setInterpolator(this.mWidgetReleaseInterpolator).setDuration(200).setStartDelay(0);
        }
    }

    public void setLayers(View panelView) {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "setLayers()");
        }
        this.mNotificationPanel = panelView;
        if (panelView != null) {
            this.mBottomArea = panelView.findViewById(C0302R.id.sec_keyguard_bottom_area);
        }
    }

    public void removeMusicWallpaper() {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "removeMusicWallpaper()");
        }
        if (this.mMovableImageView == null) {
            Log.e("KeyguardEffectViewZoom", "mMovableImageView is null");
            return;
        }
        this.mMusicWallpaper = null;
        this.mStrMusicBitmapId = null;
        this.mIsDoubleTapPuase = false;
        if (!this.mMovableImageView.isAnimating()) {
            this.mIsRemoveMusicBitmapByScreenOff = true;
        }
        update();
    }

    private boolean isSameBitmap() {
        if (this.mMovableImageView == null) {
            return false;
        }
        String newBitmapPath;
        String currentBitmapPath = this.mMovableImageView.getCurrentBitmapPath();
        if (this.mMusicWallpaper != null) {
            newBitmapPath = this.mMusicWallpaper.toString();
        } else {
            newBitmapPath = System.getStringForUser(this.mContext.getContentResolver(), "lockscreen_wallpaper_path", -2);
        }
        return currentBitmapPath.equals(newBitmapPath);
    }

    public void updateAfterCreation() {
    }

    public void setContextualWallpaper(Bitmap bmp) {
        if (this.DEBUG) {
            Log.d("KeyguardEffectViewZoom", "setContextualWallpaper() bmp = " + bmp);
        }
        this.mMusicWallpaper = KeyguardEffectViewUtil.getPreferredConfigBitmap(bmp, Config.ARGB_8888);
        this.mStrMusicBitmapId = this.mMusicWallpaper.toString();
        this.mIsDoubleTapPuase = true;
        if (this.mIsRemoveMusicBitmapByScreenOff) {
            this.mIsRemoveMusicBitmapByScreenOff = false;
            this.mIsForceUpdateMusicBitmap = true;
        }
        update();
    }

    public static boolean isBackgroundEffect() {
        return true;
    }

    public static String getCounterEffectName() {
        return null;
    }
}
