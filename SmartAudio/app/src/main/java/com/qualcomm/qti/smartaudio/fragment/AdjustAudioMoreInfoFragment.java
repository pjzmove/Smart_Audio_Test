/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.CustomTextAppearanceSpan;

public class AdjustAudioMoreInfoFragment extends SetupFragment {
	private final static String TAG = AdjustAudioInstructionFragment.class.getSimpleName();

	public static AdjustAudioMoreInfoFragment newInstance(final String tag) {
		AdjustAudioMoreInfoFragment fragment = new AdjustAudioMoreInfoFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		inflater.inflate(R.layout.frame_adjust_audio_more_info, mFrameLayout, true);

		final String adjust_audio = getString(R.string.adjust_audio);
		final String dash_more_info = getString(R.string.dash_more_info);

		final String completeText = adjust_audio + dash_more_info;

		Spannable sb = new SpannableString(completeText);
		sb.setSpan(new CustomTextAppearanceSpan(getContext(), R.style.SetupFragmentAppBarSubTextView),
				completeText.indexOf(dash_more_info),
				completeText.indexOf(dash_more_info) + dash_more_info.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		mActionBarTitleTextView.setText(sb);

		setNoButtonSetup();

		return view;
	}
}
