/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;

public class IncompatibleSpeakerInstructionFragment extends SetupAddedFragment {

	private static final String TAG = IncompatibleSpeakerInstructionFragment.class.getSimpleName();

	public static IncompatibleSpeakerInstructionFragment newInstance(String tag) {
		IncompatibleSpeakerInstructionFragment fragment = new IncompatibleSpeakerInstructionFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mActionBarTitleTextView.setText(getString(R.string.incompatible_speaker));

		mImageView.setVisibility(View.GONE);
		mGreatText.setVisibility(View.GONE);
		mAddedText.setText(getString(R.string.incompatible_speaker_message));

		mAddedText.setGravity(Gravity.LEFT);

		setTwoButtonsSetup();
		mMiddleButton.setText(getString(R.string.open_settings));
		mBottomButton.setText(getString(R.string.next));

		return view;
	}
}
