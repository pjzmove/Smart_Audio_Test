/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;

import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.constants.UpdateStatus;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import java.util.ArrayList;
import java.util.List;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceUpdateListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnHomeTheaterChannelChangedListener;


public class FirmwareUpdateFragment extends SetupListFragment implements
    OnDeviceListChangedListener {

	private static final String TAG = FirmwareUpdateFragment.class.getSimpleName();
	private FirmwareUpdateAdapter mAdapter;
	private FirmwareUpdateTask mFirmwareUpdateTask = null;
	private SurroundFirmwareUpdateTask mSurroundFirmwareUpdateTask = null;
	private static final String FIRMWARE_UPDATE_TAG = "FirmwareUpdateTag";
	private static final String SURROUNDS_UPDATE_TAG = "SurroundsUpdateTag";
	private static final String LEFT_SURROUND_UPDATE_TAG = "LeftSurroundUpdateTag";
	private static final String RIGHT_SURROUND_UPDATE_TAG = "RightSurroundUpdateTag";
	private CustomDialogFragment mSurroundsUpdateDialogFragment = null;

	public static FirmwareUpdateFragment newInstance(String tag) {
		FirmwareUpdateFragment fragment = new FirmwareUpdateFragment();
		Bundle args = new Bundle();
		args.putString(SETUP_TAG_KEY, tag);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View view = super.onCreateView(inflater, container, savedInstanceState);

		View frameView = inflater.inflate(R.layout.app_bar_firmware_update, mSetupBarFrameLayout, true);

		TextView actionBarTitleText = (TextView) frameView.findViewById(R.id.firmware_update_app_bar_text_view);

		if (actionBarTitleText != null) {
			actionBarTitleText.setText(getString(R.string.firmware_update));
		}

		ImageButton actionBarCloseButton = (ImageButton) frameView.findViewById(R.id.firmware_update_app_bar_close_button);
		if (actionBarCloseButton != null) {
			actionBarCloseButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});
			actionBarCloseButton.setVisibility(View.VISIBLE);
		}

		mExpandableListView.setVisibility(View.GONE);

		setNoButtonSetup();

		mInstructionTextView.setText(getString(R.string.firmware_update_title));
		mListView.setVisibility(View.VISIBLE);

		mAdapter = new FirmwareUpdateAdapter();
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final IoTDevice device = (IoTDevice) parent.getAdapter().getItem(position);

				if (device.haveNewFirmware()) {
					CustomDialogFragment factoryResetDialogFragment = CustomDialogFragment.newDialog(FIRMWARE_UPDATE_TAG, getString(R.string.firmware_update_available_title), getString(R.string.firmware_update_available_message, ""), getString(R.string.update), getString(R.string.not_now));
					factoryResetDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
						@Override
						public void onPositiveButtonClicked(String tag) {
							performFirmwareUpdate(device);
						}

						@Override
						public void onNegativeButtonClicked(String tag) {
						}
					});
					mBaseActivity.showDialog(factoryResetDialogFragment, FIRMWARE_UPDATE_TAG);
				} else {
					IoTPlayer player = getPlayerIfDeviceIsSoundbarWithSurrounds(device);
					if (player != null) {
						showSurroundUpdateDialog(player);
					}
				}
			}
		});

		return view;
	}

	private IoTPlayer getPlayerIfDeviceIsSoundbarWithSurrounds(IoTDevice device) {
		final IoTPlayer player = mAllPlayManager.getPlayer(device.getId());
		if ((player != null)  && ((player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) || (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND)))) {
			return player;
		}
		return null;
	}

	private void showSurroundUpdateDialog(final IoTPlayer player) {
		if ((player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) && (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND))) {
			mSurroundsUpdateDialogFragment = CustomDialogFragment.newFirmwareUpdateDialog(SURROUNDS_UPDATE_TAG, getString(R.string.firmware_update_available_title), getString(R.string.firmware_message_title), getString(R.string.update), getString(R.string.not_now));
			mSurroundsUpdateDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
				@Override
				public void onPositiveButtonClicked(String tag) {
					if (mSurroundsUpdateDialogFragment.getLeftSurroundCheckBox().isChecked()) {
						updateSurroundFirmware(player, HomeTheaterChannel.LEFT_SURROUND);
					} else if (mSurroundsUpdateDialogFragment.getRightSurroundCheckBox().isChecked()) {
						updateSurroundFirmware(player, HomeTheaterChannel.RIGHT_SURROUND);
					}
				}

				@Override
				public void onNegativeButtonClicked(String tag) {
				}
			});
			mBaseActivity.showDialog(mSurroundsUpdateDialogFragment, SURROUNDS_UPDATE_TAG);
		} else if (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) {
			CustomDialogFragment surroundsUpdateDialogFragment = CustomDialogFragment.newDialog(LEFT_SURROUND_UPDATE_TAG, getString(R.string.firmware_update_available_title), getString(R.string.firmware_update_available_message, getString(R.string.firmware_update_available_left_surround_message)), getString(R.string.update), getString(R.string.not_now));
			surroundsUpdateDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
				@Override
				public void onPositiveButtonClicked(String tag) {
					updateSurroundFirmware(player, HomeTheaterChannel.LEFT_SURROUND);

				}
				@Override
				public void onNegativeButtonClicked(String tag) {
				}
			});
			mBaseActivity.showDialog(surroundsUpdateDialogFragment, LEFT_SURROUND_UPDATE_TAG);
		} else if (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND)) {
			CustomDialogFragment surroundsUpdateDialogFragment = CustomDialogFragment.newDialog(RIGHT_SURROUND_UPDATE_TAG, getString(R.string.firmware_update_available_title), getString(R.string.firmware_update_available_message, getString(R.string.firmware_update_available_right_surround_message)), getString(R.string.update), getString(R.string.not_now));
			surroundsUpdateDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
				@Override
				public void onPositiveButtonClicked(String tag) {
					updateSurroundFirmware(player, HomeTheaterChannel.RIGHT_SURROUND);

				}
				@Override
				public void onNegativeButtonClicked(String tag) {
				}
			});
			mBaseActivity.showDialog(surroundsUpdateDialogFragment, RIGHT_SURROUND_UPDATE_TAG);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		updateState();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	protected void updateState() {
		super.updateState();

		List<IoTDevice> devices = mAllPlayManager.getDevices();
		List<IoTDevice> configuredDevices = new ArrayList<IoTDevice>();
		for (IoTDevice device : devices) {
			if (device.isOnboarded()) {
				configuredDevices.add(device);
			}
		}

		if (mAdapter != null) {
			mAdapter.updateDevices(configuredDevices);
		}
	}

	public void performFirmwareUpdate(IoTDevice device) {
		// Execute this task in 10 seconds for older firmware
		/* if (!device.isFirmwareUpdateProgressSupported()) {
			Timer removeUpdatePlayerTimer = new Timer();
			removeUpdatePlayerTimer.schedule(new RemoveUpdatingDeviceThread(device), 10000);
		}*/

		if (mFirmwareUpdateTask == null) {
			mFirmwareUpdateTask = new FirmwareUpdateTask(getString(R.string.progress_firmware_update), device);
			mBaseActivity.addTaskToQueue(mFirmwareUpdateTask);
		};
	}

	@Override
	public void onDeviceListChanged() {
		updateInUiThread();
	}

	public void updateSurroundFirmware(IoTPlayer player, HomeTheaterChannel channel) {
		if (mSurroundFirmwareUpdateTask == null) {
			mSurroundFirmwareUpdateTask = new SurroundFirmwareUpdateTask(getString(R.string.progress_firmware_update), player, channel);
			mBaseActivity.addTaskToQueue(mSurroundFirmwareUpdateTask);
		};
	}

	public class FirmwareUpdateAdapter extends BaseAdapter {

		private List<IoTDevice> mDevices = null;

		public void updateDevices(final List<IoTDevice> devices) {
			mDevices = devices;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return (mDevices != null) ? mDevices.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			if (mDevices == null) {
				return null;
			}
			return mDevices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final IoTDevice device = (IoTDevice) getItem(position);
			Holder holder = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.list_item_firmware_update, parent, false);
				holder = new Holder(convertView);
			} else {
				holder = (Holder) convertView.getTag();
			}

			holder.setText(device.getName());
			holder.setImageViewVisibility(View.GONE);
			holder.setProgressBarVisibility(View.GONE);
			// TODO : not disabling
			convertView.setEnabled(false);

			UpdateStatus updateStatus = device.getUpdateStatus();
			Log.d(TAG, device.getName() + " have new firmware " + device.haveNewFirmware() + " updateState status " + updateStatus);
			if (((updateStatus == UpdateStatus.NONE) || (updateStatus == UpdateStatus.UPDATE_FAILED)) && device.haveNewFirmware()) {
				convertView.setEnabled(true);
				holder.setImageViewVisibility(View.VISIBLE);
				holder.setSubText(getString(R.string.firmware_update_new_firmware_sub_text));
			} else if ((updateStatus == UpdateStatus.SUCCESSFUL) && device.isPhysicalRebootRequired()) {
				holder.setImageViewVisibility(View.VISIBLE);
				holder.setSubText(getString(R.string.firmware_update_physical_reboot_required));
			} else {
				if (updateStatus == UpdateStatus.LOW_BATTERY) {
					holder.setSubText(getString(R.string.firmware_update_low_battery));
					// TODO : what to do on successful?
				} else if (updateStatus == UpdateStatus.UPDATING || updateStatus == UpdateStatus.SUCCESSFUL) {
					holder.setSubText(getString(R.string.firmware_update_in_progress_message));
					holder.setProgressBarVisibility(View.VISIBLE);
					if (device.isFirmwareUpdateProgressSupported) {
						holder.setProgressBarProgress((int)(device.getFirmwareUpdateProgress()));
					}
				} else {
					holder.setSubText(getString(R.string.firmware_update_updated_sub_text));
				}
			}

			return convertView;
		}

		private class Holder {
			private TextView mTextView = null;
			private TextView mSubTextView = null;
			private ImageView mImageView = null;
			private ProgressBar mProgressBar = null;

			public Holder(final View view) {
				mTextView = (TextView)view.findViewById(R.id.firmware_update_child_text_view);
				mSubTextView = (TextView) view.findViewById(R.id.firmware_update_child_sub_text_view);
				mImageView = (ImageView) view.findViewById(R.id.firmware_update_child_image_view);
				mProgressBar = (ProgressBar) view.findViewById(R.id.firmware_update_progress_bar);
				view.setTag(this);
			}

			public void setText(final String text) {
				if (mTextView != null) {
					mTextView.setText(text);
				}
			}

			public void setSubText(final String text) {
				if (mSubTextView != null) {
					mSubTextView.setText(text);
				}
			}

			public void setImageViewVisibility(int visibility) {
				mImageView.setVisibility(visibility);
			}

			public void setProgressBarVisibility(int visibility) {
				mProgressBar.setVisibility(visibility);
			}

			public void setProgressBarProgress(int progress) {
				mProgressBar.setProgress(progress);
			}
		}
	}

	private class SurroundFirmwareUpdateTask extends RequestAsyncTask implements
      OnHomeTheaterChannelChangedListener {
		IoTPlayer mPlayer;
		HomeTheaterChannel mChannel;
		private static final int SURROUNDS_UPDATE_WAIT_TIME = 120000;

		public SurroundFirmwareUpdateTask(String progressTitle, IoTPlayer player, HomeTheaterChannel channel) {
			super(progressTitle, null, mBaseActivity, null);
			mPlayer = player;
			mChannel = channel;

			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					if (mPlayer.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND)) {
						updateSurroundFirmware(mPlayer, HomeTheaterChannel.RIGHT_SURROUND);
					}
				}

				@Override
				public void onRequestFailed() {
				}
			};
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mPlayer != null) {
				mResult = (mPlayer.updateHomeTheaterChannelFirmware(mChannel) == IoTError.NONE);
			}
			doWait(SURROUNDS_UPDATE_WAIT_TIME);

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			super.onPostExecute(param);
			mSurroundFirmwareUpdateTask = null;
		}

    @Override
    public void onHomeTheaterChannelUpdate(IoTPlayer player, HomeTheaterChannelMap channelMap) {

    }

    @Override
		public void onHomeTheaterChannelFirmwareUpdateStatusChanged(IoTPlayer player, HomeTheaterChannel channel, UpdateStatus updateStatus) {
			Log.d(TAG, "[onHomeTheaterChannelFirmwareUpdateStatusChanged] " + player.getName() + " channel " + channel + " updateState status " + updateStatus);
			if (player.equals(mPlayer) && updateStatus == UpdateStatus.SUCCESSFUL) {
				interrupt();
			}
		}

		@Override
		public void onHomeTheaterChannelFirmwareUpdateProgressChanged(IoTPlayer player, HomeTheaterChannel channel, double progress) {

		}
	}

	private class FirmwareUpdateTask extends RequestAsyncTask implements OnDeviceUpdateListener {
		IoTDevice mDevice;
		UpdateStatus mStatus;
		private static final int FIRMWARE_UPDATE_WAIT_TIME = 120000;

		public FirmwareUpdateTask(String progressTitle, IoTDevice device) {
			super(progressTitle, null, mBaseActivity, null);
			mDevice = device;

			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					updateInUiThread();
					IoTPlayer player = getPlayerIfDeviceIsSoundbarWithSurrounds(mDevice);
					if (player != null && mStatus == UpdateStatus.SUCCESSFUL) {
						showSurroundUpdateDialog(player);
					}
				}

				@Override
				public void onRequestFailed() {
				}
			};
		}

		@Override
		protected Void doInBackground(Void... params) {
			mAllPlayManager.addOnDeviceUpdateListener(this);
			if (mDevice != null) {
				mResult = (mDevice.updateFirmware() == IoTError.NONE);
			}
			doWait(FIRMWARE_UPDATE_WAIT_TIME);

			return null;
		}

		@Override
		protected void onPostExecute(Void param) {
			super.onPostExecute(param);
			mFirmwareUpdateTask = null;
			mAllPlayManager.removeOnDeviceUpdateListener(this);
		}

		@Override
		public void onDeviceAutoUpdateChanged(IoTDevice device, boolean autoUpdate) {

		}

		@Override
		public void onDeviceUpdateAvailable(IoTDevice device) {
			updateInUiThread();
		}

		@Override
		public void onDeviceUpdateStatusChanged(IoTDevice device, UpdateStatus updateStatus) {
			Log.d(TAG,"[onDeviceUpdateStatusChanged] " + device.getName() + " status " + updateStatus);
			if (mDevice.equals(device)) {
				interrupt();
			}
		}

		@Override
		public void onDeviceUpdateProgressChanged(IoTDevice device, double progress) {

		}

		@Override
		public void onDeviceUpdatePhysicalRebootRequired(IoTDevice device) {
		}
	}
}
