/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity;
import com.qualcomm.qti.smartaudio.util.CustomTextAppearanceSpan;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;

public class ChannelAddedFragment extends SetupAddedFragment {
	private String mSoundbarID = null;
	private HomeTheaterChannel mHomeTheaterChannel = HomeTheaterChannel.SUBWOOFER;

	public static ChannelAddedFragment newInstance(final String tag, final String soundbarID, final HomeTheaterChannel channel) {
		ChannelAddedFragment fragment = new ChannelAddedFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		args.putString(MultichannelSetupActivity.SOUNDBAR_ID_KEY, soundbarID);
		args.putSerializable(MultichannelSetupActivity.HOME_THEATER_CHANNEL_KEY, channel);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mSoundbarID = getArguments().getString(MultichannelSetupActivity.SOUNDBAR_ID_KEY);
		mHomeTheaterChannel = (HomeTheaterChannel)getArguments().get(MultichannelSetupActivity.HOME_THEATER_CHANNEL_KEY);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		final String begin;
		final String title;

		mImageView.setImageResource(R.drawable.ic_success);
		mGreatText.setText(getString(R.string.success));
		switch (mHomeTheaterChannel) {
			case LEFT_SURROUND:
				begin = getString(R.string.left_surround_added_detail);
				title = getString(R.string.left_surround_added);
				break;
			case RIGHT_SURROUND:
				begin = getString(R.string.right_surround_added_detail);
				title = getString(R.string.right_surround_added);
				break;
			case SUBWOOFER:
			default:
				begin = getString(R.string.subwoofer_added_detail);
				title = getString(R.string.subwoofer_added);
				break;
		}

		IoTPlayer soundbar = mAllPlayManager.getPlayer(mSoundbarID);

		String displayName = (soundbar == null) ? new String() : soundbar.getName();
		String end = getString(R.string.end_soundbar_detail);
		String completeText = begin + displayName + end;

		Spannable sb = new SpannableString(completeText);
		sb.setSpan(new CustomTextAppearanceSpan(getContext(), R.style.SetupAddedFragmentSpecialAddedText), begin.length(),
				begin.length() + displayName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		mAddedText.setText(sb);
		mActionBarTitleTextView.setText(title);

		if (mHomeTheaterChannel == HomeTheaterChannel.SUBWOOFER) {
			boolean oneButton = (soundbar == null) ? true :
					(soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_SURROUND) &&
							soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_SURROUND)
					) ||
					(soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_REAR_SURROUND) &&
							soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_REAR_SURROUND))
					||(soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_UPFIRING_SURROUND) &&
							soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_UPFIRING_SURROUND))
					||(soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND) &&
							soundbar.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND))
							;
			if (oneButton) {
				setOneButtonSetup();
			} else {
				setTwoButtonsSetup();
				mMiddleButton.setText(getString(R.string.add_surrounds));
			}
			mBottomButton.setText(getString(R.string.exit_setup));
		} else {
			setOneButtonSetup();
			mBottomButton.setText(getString(R.string.next));
		}

		return view;
	}
}
