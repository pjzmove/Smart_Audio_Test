/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.qualcomm.qti.smartaudio.R;

public class SmallPlayingAnimationView extends PlayingAnimationView {
	public SmallPlayingAnimationView(Context context) {
		super(context);
	}

	public SmallPlayingAnimationView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public SmallPlayingAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void setPlayingAnimation() {
		setImageResource(R.drawable.anim_playing_small);
	}

	@Override
	protected void setPausedAnimation() {
		setImageResource(R.drawable.playing_animation_small_paused);
	}
}
