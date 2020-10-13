/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import static com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState.kStopped;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import android.view.View;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;

public class PlayingAnimationView extends AppCompatImageView {

	private PlayState mPlayerState;

	public PlayingAnimationView(Context context) {
		super(context);
	}

	public PlayingAnimationView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public PlayingAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public synchronized void setPlayerState(PlayState playerState) {
		mPlayerState = playerState;
		update();
	}

	protected synchronized void update() {

    if(mPlayerState == null) mPlayerState = kStopped;

		switch (mPlayerState) {
      case kPlaying:
				{
					AnimationDrawable animationDrawable = getAnimationDrawable();
					if (animationDrawable == null) {
						setPlayingAnimation();
						animationDrawable = getAnimationDrawable();
					}else {
            setVisibility(View.VISIBLE);
          }

					if (!animationDrawable.isRunning()) {
						animationDrawable.start();
					}
				}
				break;
      case kStopped:
      case kPaused:
				{
					AnimationDrawable animationDrawable = getAnimationDrawable();
					if (animationDrawable != null) {
						animationDrawable.stop();
					}
					setPausedAnimation();
				}
				break;
		}
	}

	private AnimationDrawable getAnimationDrawable() {
		Drawable drawable = getDrawable();
		if (!(getDrawable() instanceof AnimationDrawable)) {
			return null;
		}
		return (AnimationDrawable)drawable;
	}

	protected void setPlayingAnimation() {
	  setVisibility(View.VISIBLE);
		setImageResource(R.drawable.anim_playing);
	}

	protected void setPausedAnimation() {
		setVisibility(View.GONE);
	}
}
