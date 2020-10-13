/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.PleaseWaitAsyncTask;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.util.List;
import org.iotivity.base.OcException;

public class PlaySoundFragment extends SetupInstructionFragment {
	private static final String TAG = PlaySoundFragment.class.getSimpleName();
	private static final String DEVICE_ID = "DEVICE_ID";
	private String mDeviceId;

	public static PlaySoundFragment newInstance(String tag, String deviceId) {
		PlaySoundFragment fragment = new PlaySoundFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		args.putString(DEVICE_ID, deviceId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mDeviceId = getArguments().getString(DEVICE_ID);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mActionBarTitleTextView.setText(getString(R.string.speaker_found));

		setOneButtonSetup();

		mBottomButton.setText(getString(R.string.next));

		mInstructionHeaderTextView.setText(getString(R.string.play_sound_header));

		// TODO : UX : no image and do we need animation
		mSetupImage.setImageResource(R.drawable.playing_animation_01);

		mInstructionSubtTextView.setVisibility(View.VISIBLE);
		mInstructionSubtTextView.setText(getString(R.string.play_sound_note));

		IoTGroup currentZone = null;
		List<IoTGroup> groups = mAllPlayManager.getGroups();
		for (IoTGroup zone : groups) {
			for (IoTPlayer player : zone.getPlayers()) {
				if (player.getPlayerId().equals(mDeviceId)) {
					currentZone = zone;
					// TODO : UX : check if soft AP name is needed
					mInstructionTextView.setVisibility(View.VISIBLE);
					mInstructionTextView.setText(player.getName());
					break;
				}
			}
		}

		if (currentZone != null) {
			mBaseActivity.addTaskToQueue(new PlaySoundAsyncTask(currentZone));
		}

		return view;
	}

	public class PlaySoundAsyncTask extends PleaseWaitAsyncTask {
		final private IoTGroup mZone;

		public PlaySoundAsyncTask(final IoTGroup zone) {
			super(mBaseActivity, null);
			mZone = zone;
		}

		@Override
		protected Void doInBackground(Void... params) {
			mResult = playSound(mZone, new MediaItem());
			return null;
		}
	}

	private boolean playSound(final IoTGroup playerGroup, final MediaItem mediaItem) {

		if ((playerGroup == null) || (mediaItem == null)) {
			return false;
		}

    MediaItem item = new MediaItem();
    item.setStreamUrl(playerGroup.getCurrentItem().getStreamUrl());

    try {
      playerGroup.playMediaItem(item, status -> {
        Log.d(TAG, "Start playing status:" + status);

      });
    } catch(OcException e) {
      e.printStackTrace();
    }
		/*if (!mApp.startHttpService()) {
			return false;
		}

		mediaItem.setStreamUrl(HttpServer.buildHttpUrl(mApp.getConnectivityReceiver().getIPAddress(), HttpServer.HTTP_TEST_AUDIO, "1"));

		int size = playlist.size();
		IoTError error = playlist.addMediaItem(size, mediaItem, true, null);*/
		return true;
	}
}
