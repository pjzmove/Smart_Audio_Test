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
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;

public class ShuffleModeButton extends AppCompatImageButton {

    private ShuffleMode mShuffleMode = ShuffleMode.kLinear;
    private OnShuffleModeChangedListener mOnShuffleModeChangedListener = null;

    public ShuffleModeButton(Context context) {
        super(context);
    }

    public ShuffleModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShuffleModeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnShuffleModeChangedListener(final OnShuffleModeChangedListener listener) {
        mOnShuffleModeChangedListener = listener;
    }

    public void setShuffleMode(final ShuffleMode shuffleMode) {
        setShuffleMode(shuffleMode, false);
    }

    private void setShuffleMode(final ShuffleMode shuffleMode, final boolean notify) {
        synchronized (this) {
            mShuffleMode = shuffleMode;
        }
        update();
        if (notify && (mOnShuffleModeChangedListener != null)) {
            mOnShuffleModeChangedListener.onShuffleModeChanged(this, shuffleMode);
        }
    }

    private ShuffleMode getShuffleMode() {
        synchronized (this) {
            if (mShuffleMode == null) {
                mShuffleMode = ShuffleMode.kLinear;
            }
            return mShuffleMode;
        }
    }

    protected void update() {
        final ShuffleMode shuffleMode = getShuffleMode();
        switch (shuffleMode) {
            case kLinear:
                setImageResource(R.drawable.btn_shuffle);
                setContentDescription(getResources().getString(R.string.cont_desc_playing_shuffle_mode,
                                                               getResources().getString(R.string.cont_desc_playing_shuffle_off)));
                break;
            case kShuffle:
                setImageResource(R.drawable.btn_shuffle_selected);
                setContentDescription(getResources().getString(R.string.cont_desc_playing_shuffle_mode,
                                                               getResources().getString(R.string.cont_desc_playing_shuffle_on)));
                break;
        }
    }

    @Override
    public boolean performClick() {
        switch (mShuffleMode) {
            case kLinear:
                setShuffleMode(ShuffleMode.kShuffle, true);
                break;
            case kShuffle:
                setShuffleMode(ShuffleMode.kLinear, true);
                break;
        }
        return super.performClick();
    }

    public interface OnShuffleModeChangedListener {

        void onShuffleModeChanged(ShuffleModeButton button, ShuffleMode shuffleMode);
    }
}
