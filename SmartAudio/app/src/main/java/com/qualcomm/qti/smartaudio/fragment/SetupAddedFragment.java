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

public class SetupAddedFragment extends SetupFragment {

	protected TextView mAddedText = null;
	protected TextView mGreatText = null;
	protected TextView mAddedSubText = null;
	protected ImageView mImageView = null;

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View frameView = inflater.inflate(R.layout.frame_setup_added, mFrameLayout, true);

		mImageView = (ImageView) frameView.findViewById(R.id.setup_added_image);
		mGreatText = (TextView) frameView.findViewById(R.id.setup_great_text);
		mAddedText = (TextView)frameView.findViewById(R.id.setup_added_text);
		mAddedSubText = (TextView) frameView.findViewById(R.id.setup_added_subtext_text);

		return view;
	}
}
