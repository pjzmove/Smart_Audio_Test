/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;

import org.iotivity.base.OcException;

/**
 * This button class simplifies the Play/Pause updates when user press it
 */
public class PlayPauseButton extends AppCompatImageButton {

    private IoTGroup mGroup = null;
    private boolean mPlayImage = true;
    private BaseActivity mBaseActivity = null;

    private boolean mPlayPause = true;
    private Boolean mSetting = false;

    public PlayPauseButton(Context context) {
        super(context);
    }

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayPauseButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBaseActivity(final BaseActivity baseActivity) {
        synchronized (this) {
            mBaseActivity = baseActivity;
        }
    }

    private BaseActivity getBaseActivity() {
        synchronized (this) {
            return mBaseActivity;
        }
    }

    public void setGroup(IoTGroup zone) {
        synchronized (this) {
            mGroup = zone;
        }
        reset();
        update();
    }

    private IoTGroup getGroup() {
        synchronized (this) {
            return mGroup;
        }
    }

    /**
     * Return to see if the image is showing paused or playing image
     */
    public boolean isPlaying() {
        final IoTGroup zone = getGroup();
        return (zone != null) && (zone.getPlayerState() != PlayState.kStopped) && (zone.getPlayerState()
                != PlayState.kPaused);
    }

    /**
     * Update the image based on the PlayerState
     */
    protected void update() {
        final IoTGroup zone = getGroup();
        if (zone == null) {
            setPlayImage();
            return;
        }

        setEnabled(!mSetting);

        PlayState playerState = zone.getPlayerState();
        if (playerState == null) {
            return;
        }

        switch (playerState) {
            case kStopped:
            case kPaused:
                setPlayImage();
                break;
            case kPlaying:
            default:
                setPauseImage();
                break;
        }
    }

    protected void setPlayImage() {
        setImageResource(R.drawable.ic_play_album_art);
        setContentDescription(getResources().getString(R.string.cont_desc_zone_pause, getGroup().getDisplayName()));
        mPlayImage = true;
    }

    protected void setPauseImage() {
        setImageResource(R.drawable.ic_pause_album_art);
        setContentDescription(getResources().getString(R.string.cont_desc_zone_play, getGroup().getDisplayName()));
        mPlayImage = false;
    }

    /**
     * Override performClick to set the new PlayerState
     *
     * @return return performClicked from super
     */
    @Override
    public boolean performClick() {
        boolean click = super.performClick();
        final IoTGroup group = getGroup();
        if (group != null) {
            if (mPlayImage) {
                setPauseImage();
                togglePlayPause(group, true);
            }
            else {
                setPlayImage();
                togglePlayPause(group, false);
            }
        }
        return click;
    }

    private void reset() {
        synchronized (mSetting) {
            mSetting = false;
        }
    }

    private void togglePlayPause(IoTGroup group, boolean playPause) {
        final BaseActivity baseActivity = getBaseActivity();

        if ((group == null) || (baseActivity == null)) {
            reset();
            return;
        }

        mPlayPause = playPause;
        synchronized (mSetting) {
            if (!mSetting) {
                mSetting = true;
            }

            setEnabled(!mSetting);
        }

        try {
            play(playPause);
        }
        catch (OcException e) {
            e.printStackTrace();
        }

    }

    private void play(boolean playPauseTarget) throws OcException {
        if (mGroup != null) {
            if (playPauseTarget) {

                if (mGroup.getPlayerState() != PlayState.kPaused) {
                    mGroup.playAtIndex(mGroup.getIndexPlaying());
                }
                else {
                    mGroup.play(success -> {
                    });
                }
            }
            else {
                try {
                    mGroup.pause(success -> {
                        if (success) {
                        }
                    });
                }
                catch (OcException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}