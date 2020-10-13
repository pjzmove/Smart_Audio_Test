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
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;
import com.qualcomm.qti.iotcontrollersdk.constants.NetworkInterface;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;


public class ConnectEthernetFragment extends SetupInstructionFragment implements AllPlayManager.OnDeviceListChangedListener {

	private MultichannelSetupActivity.SetupType mSetupType = MultichannelSetupActivity.SetupType.ADD_SUBWOOFER;

	private WaitForEthernetTask mWaitForEthernetTask = null;
	private String mSoundbarID = null;

	public static ConnectEthernetFragment newInstance(final String tag, final MultichannelSetupActivity.SetupType type,
													  final String soundbarID) {
		ConnectEthernetFragment fragment = new ConnectEthernetFragment();
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
	public void onResume() {
		super.onResume();
		if (mAllPlayManager != null) {
			mAllPlayManager.addOnDeviceListChangedListener(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAllPlayManager != null) {
			mAllPlayManager.removeOnDeviceListChangedListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.setup_middle_button:
				waitForEthernetConnection();
				return;
			default:
				break;
		}
		super.onClick(view);
	}

	private void waitForEthernetConnection() {
		if (mWaitForEthernetTask == null) {
			mWaitForEthernetTask = new WaitForEthernetTask();
			mWaitForEthernetTask.execute();
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		boolean isSubwoofer = (mSetupType == MultichannelSetupActivity.SetupType.ADD_SUBWOOFER);
		mActionBarTitleTextView.setText(getString((isSubwoofer) ? R.string.add_subwoofer : R.string.add_surrounds));

		mInstructionHeaderTextView.setText(getString(R.string.connect_ethernet_detail));

		mSetupImage.setImageResource(R.drawable.ic_setup_connect_ethernet_287x174dp);

		setTwoButtonsSetup();

		mMiddleButton.setText(getString(R.string.try_again));
		mBottomButton.setText(getString(R.string.exit_setup));

		return view;
	}

	@Override
	public void onDeviceListChanged() {
		if (isDeviceOnEthernet()) {
			waitForEthernetConnection();
		}
	}

	private boolean isDeviceOnEthernet() {
		IoTDevice device = mAllPlayManager.getDevice(mSoundbarID);
		return ((device != null) && (device.getNetworkInterface() == NetworkInterface.ETHERNET));
	}

	private class WaitForEthernetTask extends RequestAsyncTask implements AllPlayManager.OnDeviceListChangedListener {
		private int WAIT_FOR_ETHERNET_TIME = 60000;

		public WaitForEthernetTask() {
			super(getString(R.string.connecting), null, mBaseActivity, null);
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					if (isDeviceOnEthernet()) {
						onMiddleButtonClicked();
					}
				}

				@Override
				public void onRequestFailed() {

				}
			};
		}

		@Override
		public void onDeviceListChanged() {
			if (isDeviceOnEthernet()) {
				synchronized (this) {
					notifyAll();
				}
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mAllPlayManager.addOnDeviceListChangedListener(this);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			IoTDevice device = mAllPlayManager.getDevice(mSoundbarID);
			int waitTime = WAIT_FOR_ETHERNET_TIME;
			if (device != null) {
				if (device.getNetworkInterface() == NetworkInterface.ETHERNET) {
					waitTime = DEFAULT_WAIT_TIME;
				}
			}
			doWait(waitTime);
			return null;
		}

		@Override
		protected void onPostExecute(final Void param) {
			mWaitForEthernetTask = null;
			mAllPlayManager.removeOnDeviceListChangedListener(this);

			super.onPostExecute(param);
		}
	}
}
