/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.qualcomm.qti.smartaudio.R;

public class VolumeSliderLockButton extends ImageButton {

	private boolean mLocked = true;

	public VolumeSliderLockButton(Context context) {
		super(context);
		setLocked(true);
	}

	public VolumeSliderLockButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLocked(true);
	}

	public VolumeSliderLockButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setLocked(true);
	}

	public void setLocked(final boolean lock) {
		mLocked = lock;
		update();
	}

	public boolean isLocked() {
		return mLocked;
	}

	private void update() {
		setImageResource(mLocked ? R.drawable.btn_slider_locked : R.drawable.btn_slider_unlocked);
	}

	@Override
	public boolean performClick() {
		if (mLocked) {
			setLocked(false);
		} else {
			setLocked(true);
		}
		return super.performClick();
	}
}
