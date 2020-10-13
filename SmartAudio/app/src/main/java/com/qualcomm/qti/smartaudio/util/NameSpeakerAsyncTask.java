/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;

public class NameSpeakerAsyncTask extends RequestAsyncTask  {

	private DeviceNameRequestListener mDeviceNameRequestListener;
	private IoTDevice mDevice;
	private String mDisplayName;

	/**
	 * Constructor
	 *
	 * @param progressTitle   the title of the progress dialog
	 * @param device          device
	 * @param displayName     display name to be set for device
	 * @param baseActivity    BaseActivity
	 */
	public NameSpeakerAsyncTask(String progressTitle, IoTDevice device, String displayName, BaseActivity baseActivity) {
		super(progressTitle, null, baseActivity, null);
		mDevice = device;
		mDisplayName = displayName;
		mListener = new RequestListener() {
			@Override
			public void onRequestSuccess() {
					mDeviceNameRequestListener.onRequestResult(true);
				}

			@Override
			public void onRequestFailed() {
				mDeviceNameRequestListener.onRequestResult(false);
			}
		};
	}

	@Override
	protected Void doInBackground(Void... params) {
		//mDevice.mDisplayName = mDisplayName;

		return null;
	}

	@Override
	protected void onPostExecute(Void param) {
		super.onPostExecute(param);
	}

	public void setDeviceNameRequestListener(DeviceNameRequestListener deviceNameRequestListener) {
		mDeviceNameRequestListener = deviceNameRequestListener;
	}

	public interface DeviceNameRequestListener {
		public void onRequestResult(boolean result);
	}
}