package com.android.keyguard.sec.effect;

import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import com.samsung.android.sdk.look.airbutton.SlookAirButtonRecentMediaAdapter;

public class KeyguardEffectSound {
    private String TAG = "KeyguardEffectSound";
    private AudioManager mAudioManager;
    private int mCntForLog = 0;
    private Context mContext;

    public KeyguardEffectSound(Context context) {
        init(context);
    }

    public KeyguardEffectSound(Context context, String tag) {
        this.TAG += "-" + tag;
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService(SlookAirButtonRecentMediaAdapter.AUDIO_TYPE);
    }

    public boolean isPlayPossible() {
        this.mCntForLog++;
        int soundsEnabled = 0;
        try {
            soundsEnabled = System.getIntForUser(this.mContext.getContentResolver(), "lockscreen_sounds_enabled", -2);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        if (soundsEnabled == 0) {
            if (this.mCntForLog % 10 != 0) {
                return false;
            }
            Log.d(this.TAG, "isPlayPossible() - Current user's lockscreen sounds option is disabled!!!");
            this.mCntForLog = 0;
            return false;
        } else if (this.mAudioManager != null) {
            int vol = this.mAudioManager.getStreamVolume(1);
            boolean mute = this.mAudioManager.isStreamMute(this.mAudioManager.getMasterStreamType());
            if (!mute && vol > 0) {
                return true;
            }
            if (this.mCntForLog % 10 != 0) {
                return false;
            }
            Log.d(this.TAG, "isPlayPossible() - Muted!!!, isStreamMute = " + mute + ", volume = " + vol);
            this.mCntForLog = 0;
            return false;
        } else if (this.mCntForLog % 10 != 0) {
            return false;
        } else {
            Log.e(this.TAG, "isPlayPossible() - mAudioManager is null!!!");
            this.mCntForLog = 0;
            return false;
        }
    }
}
