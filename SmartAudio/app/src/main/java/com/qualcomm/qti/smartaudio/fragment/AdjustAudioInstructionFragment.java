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

public class AdjustAudioInstructionFragment extends SetupInstructionFragment implements
		CustomDialogFragment.OnCustomDialogButtonClickedListener {
	private final static String TAG = AdjustAudioInstructionFragment.class.getSimpleName();

	private MultichannelSetupActivity.SetupType mSetupType = MultichannelSetupActivity.SetupType.ADJUST_AUDIO;
	private boolean mPreviouslyKnown = false;

	private static final String DIALOG_PREVIOUS_SURROUND_SETTINGS_FOUND_TAG = "DialogPreviousSurroundSettingsFoundTag";

	private OnPreviousSurroundSettingsListener mOnPreviousSurroundSettingsListener = null;

	public static AdjustAudioInstructionFragment newInstance(final String tag, final MultichannelSetupActivity.SetupType type,
															 final boolean previouslyKnown) {
		AdjustAudioInstructionFragment fragment = new AdjustAudioInstructionFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		args.putSerializable(MultichannelSetupActivity.SETUP_TYPE_KEY, type);
		args.putInt(MultichannelSetupActivity.PREVIOUSLY_KNOWN_KEY, previouslyKnown ? 1 : 0);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mSetupType = (MultichannelSetupActivity.SetupType)getArguments().get(MultichannelSetupActivity.SETUP_TYPE_KEY);
		mPreviouslyKnown = (getArguments().getInt(MultichannelSetupActivity.PREVIOUSLY_KNOWN_KEY) == 1);
		mOnPreviousSurroundSettingsListener = (OnPreviousSurroundSettingsListener)context;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		mActionBarTitleTextView.setText(getString(R.string.adjust_audio));

		mInstructionTextView.setText(getString(R.string.adjust_audio_instruction));

		mSetupImage.setImageResource(R.drawable.ic_room_diagram);

		setOneButtonSetup();

		mBottomButton.setText(getString(R.string.next));

		return view;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.setup_bottom_button) {
			if ((mSetupType == MultichannelSetupActivity.SetupType.ADD_SURROUNDS) && mPreviouslyKnown) {
				showPreviousSurroundSettingsFound();
				return;
			}
		}
		super.onClick(view);
	}

	private void showPreviousSurroundSettingsFound() {
		CustomDialogFragment previousSurroundSettingsFragment = CustomDialogFragment.newDialog(DIALOG_PREVIOUS_SURROUND_SETTINGS_FOUND_TAG,
				getString(R.string.previous_surround_settings_title), getString(R.string.previous_surround_settings_message),
				getString(R.string.default_settings), getString(R.string.previous_settings));
		previousSurroundSettingsFragment.setButtonClickedListener(this);

		mBaseActivity.showDialog(previousSurroundSettingsFragment, DIALOG_PREVIOUS_SURROUND_SETTINGS_FOUND_TAG);
	}

	private void onPreviousSurroundSettingsClicked(final boolean userPreviousSetting) {
		if (mOnPreviousSurroundSettingsListener != null) {
			mOnPreviousSurroundSettingsListener.onPreviousSurroundSettingsClicked(userPreviousSetting);
		}
	}

	@Override
	public void onPositiveButtonClicked(String tag) {
		onPreviousSurroundSettingsClicked(false);
	}

	@Override
	public void onNegativeButtonClicked(String tag) {
		onPreviousSurroundSettingsClicked(true);
	}

	public interface OnPreviousSurroundSettingsListener {
		void onPreviousSurroundSettingsClicked(final boolean usePreviousSetting);
	}
}
