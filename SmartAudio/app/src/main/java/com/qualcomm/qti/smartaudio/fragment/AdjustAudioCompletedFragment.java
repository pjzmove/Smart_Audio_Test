/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;

public class AdjustAudioCompletedFragment extends SetupAddedFragment {
	private final static String TAG = AdjustAudioInstructionFragment.class.getSimpleName();

	public static AdjustAudioCompletedFragment newInstance(final String tag) {
		AdjustAudioCompletedFragment fragment = new AdjustAudioCompletedFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mActionBarTitleTextView.setText(getString(R.string.setup_complete));
		mImageView.setImageResource(R.drawable.ic_success);
		mGreatText.setText(getString(R.string.success));
		mAddedText.setText(getString(R.string.adjust_audio_complete));

		setOneButtonSetup();
		mBottomButton.setText(getString(R.string.exit_setup));

		return view;
	}
}
