package com.android.keyguard.sec.effect;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public interface KeyguardEffectViewBase {
    void cleanUp();

    long getUnlockDelay();

    boolean handleHoverEvent(View view, MotionEvent motionEvent);

    boolean handleTouchEvent(View view, MotionEvent motionEvent);

    boolean handleTouchEventForPatternLock(View view, MotionEvent motionEvent);

    void handleUnlock(View view, MotionEvent motionEvent);

    void playLockSound();

    void reset();

    void screenTurnedOff();

    void screenTurnedOn();

    void setContextualWallpaper(Bitmap bitmap);

    void setHidden(boolean z);

    void show();

    void showUnlockAffordance(long j, Rect rect);

    void update();

    void updateAfterCreation();
}
