/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.os.Bundle;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.FirmwareUpdateFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupListFragment;
import com.qualcomm.qti.smartaudio.util.FragmentController;

public class FirmwareUpdateActivity extends BaseActivity implements SetupFragment.SetupFragmentListener, SetupListFragment.SetupListFragmentListener {
	private static final String TAG = FirmwareUpdateActivity.class.getSimpleName();
	private FragmentController mFragmentController;
	private static final String FIRMWARE_UPDATE_FRAGMENT = "FIRMWARE_UPDATE_FRAGMENT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (mApp.isInit()) {
			setContentView(R.layout.activity_firmware_update);
			mFragmentController = new FragmentController(getSupportFragmentManager(), R.id.firmware_update_fragment);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		showFirmwareUpdateFragment();
	}

	@Override
	protected void update() {
		super.update();

	}

	private void showFirmwareUpdateFragment() {
		mFragmentController.startFragment(FirmwareUpdateFragment.newInstance(FIRMWARE_UPDATE_FRAGMENT), FIRMWARE_UPDATE_FRAGMENT, true);
	}

	@Override
	public void onTopButtonClicked(String tag) {

	}

	@Override
	public void onMiddleButtonClicked(String tag) {

	}

	@Override
	public void onBottomButtonClicked(String tag) {

	}

	@Override
	public void onItemClicked(String tag, Object object) {

	}
}
