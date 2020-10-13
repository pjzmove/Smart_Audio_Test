/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.util.AttributeSet;

import com.qualcomm.qti.smartaudio.R;

public class NowPlayingPlayPauseButton extends PlayPauseButton {

	public NowPlayingPlayPauseButton(Context context) {
		super(context);
	}

	public NowPlayingPlayPauseButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NowPlayingPlayPauseButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void setPlayImage() {
		super.setPlayImage();
		setImageResource(R.drawable.btn_play);
	}

	@Override
	protected void setPauseImage() {
		super.setPauseImage();
		setImageResource(R.drawable.btn_pause);
	}
}
