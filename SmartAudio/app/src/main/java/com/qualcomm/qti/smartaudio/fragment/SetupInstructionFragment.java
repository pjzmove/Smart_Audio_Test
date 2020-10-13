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
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

public class SetupInstructionFragment extends SetupFragment {

	protected TextView mInstructionHeaderTextView = null;
	protected TextView mInstructionTextView = null;
	protected TextView mInstructionSubtTextView = null;
	protected ImageView mSetupImage = null;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View frameView = inflater.inflate(R.layout.frame_setup_instruction, mFrameLayout, true);

		mInstructionHeaderTextView = (TextView)frameView.findViewById(R.id.setup_instruction_header_text);
		mSetupImage = (ImageView)frameView.findViewById(R.id.setup_instruction_image);
		mInstructionTextView = (TextView) frameView.findViewById(R.id.setup_instruction_text);
		mInstructionSubtTextView = (TextView) frameView.findViewById(R.id.setup_instruction_subtext_text);

		return view;
	}
}
