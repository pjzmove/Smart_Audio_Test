/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.util.Timer;
import java.util.TimerTask;

public class TrackSeekBar extends AppCompatSeekBar {

	private final int UPDATE_TIMER = 1000;

	private IoTGroup mZone = null;
	private Timer mTimer = null;
	private boolean mEnableTouch = true;

	public TrackSeekBar(Context context) {
		super(context);
	}

	public TrackSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TrackSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setZone(final IoTGroup zone) {
		synchronized (this) {
			mZone = zone;
		}
		update();
	}

	private IoTGroup getGroup() {
		synchronized (this) {
			return mZone;
		}
	}

	public void enableTouch(final boolean enableTouch) {
		mEnableTouch = enableTouch;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mEnableTouch) {
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			stopUpdateSeekBarThread();
		}

		return super.onTouchEvent(event);
	}

	protected void update() {

		stopUpdateSeekBarThread();

		final IoTGroup group = getGroup();
		if (group != null) {
      final MediaItem mediaItem = group.getCurrentItem();
      if (mediaItem != null) {
        long duration = mediaItem.getDuration();
        long position = group.getPlayPosition();
        if (duration > 0) {
          setEnabled(true);
          setMax(100);
          setProgress((int) (Math.min((float) position / (float) duration, 1.0) * 100));

          if (group.getPlayerState() == PlayState.kPlaying) {
            startUpdateSeekBarThread();
          }
        }
        return;
      }
		}

    setEnabled(false);
    setProgress(0);
    setSecondaryProgress(0);
    setMax(0);
	}

	private void startUpdateSeekBarThread() {
		stopUpdateSeekBarThread();
		mTimer = new Timer();
		mTimer.schedule(new UpdateSeekBar(), 0, UPDATE_TIMER);
	}

	private void stopUpdateSeekBarThread() {
		if (mTimer == null) {
			return;
		}
		mTimer.cancel();
		mTimer = null;
	}

	private class UpdateSeekBar extends TimerTask {
		@Override
		public void run() {
			final IoTGroup group = getGroup();
			if (group == null) {
				return;
			}

			final MediaItem mediaItem = group.getCurrentItem();
			if (mediaItem == null) {
				return;
			}

			if (!isPressed() && (group.getPlayerState() == PlayState.kPlaying)) {
			  long progress = group.getPlayPosition();
			  long duration = mediaItem.getDuration();
			  if(duration > 0L)
				  setProgress((int)(Math.min((float)progress/(float)duration, 1.0) * 100));
			}
		}
	}

}
