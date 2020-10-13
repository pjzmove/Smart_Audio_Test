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
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;

public class LoopModeButton extends AppCompatImageButton {

    private LoopMode mLoopMode = LoopMode.kNone;
    private OnLoopModeChangeListener mOnLoopModeChangedListener = null;

    public LoopModeButton(Context context) {
        super(context);
    }

    public LoopModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoopModeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnLoopModeChangedListener(final OnLoopModeChangeListener listener) {
        mOnLoopModeChangedListener = listener;
    }

    public void setLoopMode(final LoopMode loopMode) {
        setLoopMode(loopMode, false);
    }

    private void setLoopMode(final LoopMode loopMode, final boolean notify) {
        synchronized (this) {
            mLoopMode = loopMode;
        }
        update();
        if (notify && (mOnLoopModeChangedListener != null)) {
            mOnLoopModeChangedListener.onLoopModeChanged(this, loopMode);
        }
    }

    private synchronized LoopMode getLoopMode() {
        if (mLoopMode == null) {
            mLoopMode = LoopMode.kNone;
        }

        return mLoopMode;
    }

    protected void update() {
        LoopMode loopMode = getLoopMode();
        switch (loopMode) {
            case kNone:
                setImageResource(R.drawable.btn_repeat);
                setContentDescription(getResources().getString(R.string.cont_desc_playing_repeat_mode,
                                                               getResources().getString(R.string.cont_desc_playing_repeat_none)));
                break;
            case kOne:
                setImageResource(R.drawable.btn_repeat_once_selected);
                setContentDescription(getResources().getString(R.string.cont_desc_playing_repeat_mode,
                                                               getResources().getString(R.string.cont_desc_playing_repeat_one)));
                break;
            case kAll:
                setImageResource(R.drawable.btn_repeat_selected);
                setContentDescription(getResources().getString(R.string.cont_desc_playing_repeat_mode,
                                                               getResources().getString(R.string.cont_desc_playing_repeat_all)));
                break;
        }
    }

    @Override
    public boolean performClick() {
        switch (mLoopMode) {
            case kNone:
                setLoopMode(LoopMode.kOne, true);
                break;
            case kOne:
                setLoopMode(LoopMode.kAll, true);
                break;
            case kAll:
                setLoopMode(LoopMode.kNone, true);
                break;
        }
        return super.performClick();
    }

    public interface OnLoopModeChangeListener {

        void onLoopModeChanged(LoopModeButton button, LoopMode loopMode);
    }
}
