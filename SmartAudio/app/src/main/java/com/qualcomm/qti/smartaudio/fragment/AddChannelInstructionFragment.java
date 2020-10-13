/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity.SetupType;

public class AddChannelInstructionFragment extends SetupInstructionFragment {
	private SetupType mSetupType = MultichannelSetupActivity.SetupType.ADD_SUBWOOFER;

	private String mSoundbarID = null;

	public static AddChannelInstructionFragment newInstance(final String tag, final MultichannelSetupActivity.SetupType type,
															final String soundbarID) {
		AddChannelInstructionFragment fragment = new AddChannelInstructionFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		args.putSerializable(MultichannelSetupActivity.SETUP_TYPE_KEY, type);
		args.putString(MultichannelSetupActivity.SOUNDBAR_ID_KEY, soundbarID);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mSetupType = (MultichannelSetupActivity.SetupType)getArguments().get(MultichannelSetupActivity.SETUP_TYPE_KEY);
		mSoundbarID = getArguments().getString(MultichannelSetupActivity.SOUNDBAR_ID_KEY);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);

    boolean isSubwoofer = (mSetupType == MultichannelSetupActivity.SetupType.ADD_SUBWOOFER);
    mActionBarTitleTextView
        .setText(getString((isSubwoofer) ? R.string.add_subwoofer : R.string.add_surrounds));

    String text = getString(R.string.add_subwoofer_detail);
    int imageRes = R.drawable.ic_setup_subwoofer_360x303dp;

    if (!isSubwoofer && mSetupType == SetupType.ADD_SURROUNDS) {
      text = getString(R.string.add_surrounds_detail);
      imageRes = R.drawable.ic_setup_left_right_surrounds_360x303dp;
    } else if (!isSubwoofer && mSetupType == SetupType.ADD_REAR_SURROUNDS) {
      text = getString(R.string.add_surrounds_rear_detail);
      imageRes = R.drawable.ic_setup_rear_surrounds_360x352dp;
    } else if (!isSubwoofer && mSetupType == MultichannelSetupActivity.SetupType.ADD_UPFIRING_SURROUNDS) {
      text = getString(R.string.add_surrounds_upfiring_detail);
      imageRes = R.drawable.ic_setup_front_upfiring_surrounds_360x366dp;
    }else if(!isSubwoofer && mSetupType == MultichannelSetupActivity.SetupType.ADD_REAR_UPFIRING_SURROUNDS) {
      text = getString(R.string.add_surrounds_rear_upfiring_detail);
      imageRes = R.drawable.ic_setup_rear_upfiring_surrounds_360x366dp;
    }
		mInstructionHeaderTextView.setText(text);
		mSetupImage.setImageResource(imageRes);

		setOneButtonSetup();

		mBottomButton.setText(getString(R.string.next));

		return view;
	}
}
