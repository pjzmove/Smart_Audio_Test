/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.AddChannelInstructionFragment;
import com.qualcomm.qti.smartaudio.fragment.AdjustAudioFragment;
import com.qualcomm.qti.smartaudio.fragment.ChannelAddedFragment;
import com.qualcomm.qti.smartaudio.fragment.ChooseChannelFragment;
import com.qualcomm.qti.smartaudio.fragment.ChooseChannelFragment.SurroundDiscoveryListener;
import com.qualcomm.qti.smartaudio.fragment.ConnectEthernetFragment;
import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupListFragment;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnSurroundSpeakerDiscoveredListener;
import com.qualcomm.qti.smartaudio.util.FragmentController;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.AddHomeTheaterChannelData;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.ScanInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.ScanInfo.AuthType;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.constants.NetworkInterface;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.smartaudio.fragment.AdjustAudioInstructionFragment.OnPreviousSurroundSettingsListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceListChangedListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MultichannelSetupActivity extends WifiScanBaseActivity implements OnGroupListChangedListener,
    OnDeviceListChangedListener, OnPreviousSurroundSettingsListener,
		SetupFragment.SetupFragmentListener, SetupListFragment.SetupListFragmentListener,
		CustomDialogFragment.OnCustomDialogButtonClickedListener, AdjustAudioFragment.OnMoreInfoClickedListener,
    OnSurroundSpeakerDiscoveredListener {
	private static final String TAG = MultichannelSetupActivity.class.getSimpleName();

	private static final String ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT = "ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT";
	private static final String ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT = "ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT";
	private static final String ADD_SUBWOOFER_INSTRUCTION_FRAGMENT = "ADD_SUBWOOFER_INSTRUCTION_FRAGMENT";
	private static final String ADD_SUBWOOFER_FRAGMENT = "ADD_SUBWOOFER_FRAGMENT";
	private static final String SUBWOOFER_ADDED_FRAGMENT = "SUBWOOFER_ADDED_FRAGMENT";
	private static final String ADD_SURROUNDS_INSTRUCTION_FRAGMENT = "ADD_SURROUNDS_INSTRUCTION_FRAGMENT";
	private static final String ADD_REAR_SURROUNDS_INSTRUCTION_FRAGMENT = "ADD_REAR_SURROUNDS_INSTR";
	private static final String ADD_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT = "ADD_UPFIRING_SURROUNDS_INSTR";
	private static final String ADD_REAR_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT = "ADD_REAR_UPFIRING_SURROUNDS_INSTR";
	private static final String ADD_LEFT_SURROUND_FRAGMENT = "ADD_LEFT_SURROUND_FRAGMENT";
	private static final String ADD_LEFT_REAR_SURROUND_FRAGMENT = "ADD_LEFT_REAR_SURROUND_FRAGMENT";
	private static final String ADD_RIGHT_REAR_SURROUND_FRAGMENT = "ADD_RIGHT_REAR_SURROUND_FRAGMENT";
	private static final String ADD_LEFT_UPFIRING_SURROUND_FRAGMENT = "ADD_LEFT_UPFIRING_SURROUND_FRAGMENT";
	private static final String ADD_RIGHT_UPFIRING_SURROUND_FRAGMENT = "ADD_RIGHT_UPFIRING_SURROUND_FRAGMENT";
	private static final String ADD_LEFT_REAR_UPFIRING_SURROUND_FRAGMENT = "ADD_LEFT_REAR_UPFIRING_SURROUND_FRAGMENT";
	private static final String ADD_RIGHT_REAR_UPFIRING_URROUND_FRAGMENT = "ADD_RIGHT_REAR_UPFIRING_SURROUND_FRAGMENT";
	private static final String LEFT_SURROUND_FOUND_FRAGMENT = "LEFT_SURROUND_FOUND_FRAGMENT"; // audio
	private static final String LEFT_SURROUND_ADDED_FRAGMENT = "LEFT_SURROUND_ADDED_FRAGMENT";
	private static final String ADD_RIGHT_SURROUND_FRAGMENT = "ADD_RIGHT_SURROUND_FRAGMENT";
	private static final String RIGHT_SURROUND_FOUND_FRAGMENT = "RIGHT_SURROUND_FOUND_FRAGMENT";
	private static final String RIGHT_SURROUND_ADDED_FRAGMENT = "RIGHT_SURROUND_ADDED_FRAGMENT";
  private static final String LEFT_REARUPFIRING_SURROUND_ADDED_FRAGMENT = "LEFT_REARUPFIRING_SURROUND_ADDED_FRAGMENT";
  private static final String RIGHT_REARUPFIRING_SURROUND_ADDED_FRAGMENT = "RIGHT_REARUPFIRING_SURROUND_ADDED_FRAGMENT";
	private static final String LEFT_REAR_SURROUND_ADDED_FRAGMENT = "LEFT_REAR_SURROUND_ADDED_FRAGMENT";
	private static final String RIGHT_REAR_SURROUND_ADDED_FRAGMENT = "RIGHT_REAR_SURROUND_ADDED_FRAGMENT";
	private static final String LEFT_UPFIRING_SURROUND_ADDED_FRAGMENT = "LEFT_UPFIRING_SURROUND_ADDED_FRAGMENT";
	private static final String RIGHT_UPFIRING_SURROUND_ADDED_FRAGMENT = "RIGHT_UPFIRING_SURROUND_ADDED_FRAGMENT";
	private static final String ADJUST_AUDIO_FRAGMENT = "ADJUST_AUDIO_FRAGMENT";
	private static final String ADJUST_AUDIO_INSTRUCTION_FRAGMENT = "ADJUST_AUDIO_INSTRUCTION_FRAGMENT";
	private static final String ADJUST_AUDIO_COMPLETED_FRAGMENT = "ADJUST_AUDIO_COMPLETED_FRAGMENT";
	private static final String ADJUST_AUDIO_MORE_INFO_FRAGMENT = "ADJUST_AUDIO_MORE_INFO_FRAGMENT";

	public static final String SOUNDBAR_ID_KEY = "SOUNDBAR_ID_KEY";
	public static final String SETUP_TYPE_KEY = "SETUP_TYPE_KEY";
	public static final String HOME_THEATER_CHANNEL_KEY = "HOME_THEATER_CHANNEL_KEY";
	public static final String PREVIOUSLY_KNOWN_KEY = "PREVIOUSLY_KNOWN_KEY";

	private static final String DIALOG_SOUNDBAR_LOST_TAG = "DialogSoundbarLostTag";
	private static final String DIALOG_SOUNDBAR_RETRIED_TAG = "DialogSoundbarRetriedTag";
	private static final String DIALOG_SURROUNDS_NOT_ADDED_TAG = "DialogSurroundsNotAddedTag";

	private AllPlayManager mAllPlayManager = null;
	private String mSoundbarID = null;
	private SetupType mSetupType = SetupType.ADD_SUBWOOFER;
	private FragmentController mFragmentController = null;

	private WaitForSoundbarTask mWaitForSoundbarTask = null;
	private RemoveSurroundsTask mRemoveSurroundsTask = null;

	private boolean mPreviouslyKnownLeft = false;
	private boolean mPreviouslyKnownRight = false;

	public enum SetupType {
		ADD_SUBWOOFER,
		ADD_SURROUNDS,
		ADD_REAR_SURROUNDS,
		ADD_UPFIRING_SURROUNDS,
    ADD_REAR_UPFIRING_SURROUNDS,
		ADJUST_AUDIO
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mApp.isInit()) {
			mFragmentController = new FragmentController(getSupportFragmentManager(), R.id.multichannel_setup_frame);
			mAllPlayManager = mApp.getAllPlayManager();
			setContentView(R.layout.activity_multichannel_setup);
			mSoundbarID = getIntent().getStringExtra(SOUNDBAR_ID_KEY);
			mSetupType = (SetupType)getIntent().getSerializableExtra(SETUP_TYPE_KEY);
			if (mSetupType == SetupType.ADJUST_AUDIO) {
				mPreviouslyKnownLeft = mPreviouslyKnownRight = true;
			}

      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
      intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
      registerReceiver(mWifiScanReceiver, intentFilter);
		}

		mUiHandler = new Handler();

	}

	@Override
	public void onStart() {
		super.onStart();
		mCurrentSsid = null;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mAllPlayManager != null) {
			mAllPlayManager.addOnZoneListChangedListener(this);
			mAllPlayManager.addOnDeviceListChangedListener(this);
			mAllPlayManager.registerSurroundDiscoveryListener(this);
		}

		IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
		if (mFragmentController.getCount() == 0) {
			if (mSetupType == SetupType.ADJUST_AUDIO) {
				showAdjustAudioInstructionFragment();
			} else {
				if (player != null) {
						if (mSetupType == SetupType.ADD_SUBWOOFER) {
							showAddSubwooferInstructionFragment();
						} else if (mSetupType == SetupType.ADD_SURROUNDS) {
							showAddSurroundsInstructionFragment();
						} else if(mSetupType == SetupType.ADD_REAR_SURROUNDS) {
						  showAddRearSurroundsInstructionFragment();
            } else if(mSetupType == SetupType.ADD_UPFIRING_SURROUNDS) {
              showAddUpfiringSurroundsInstructionFragment();
            } else if(mSetupType == SetupType.ADD_REAR_UPFIRING_SURROUNDS) {
              showAddRearUpfiringAddSurroundsInstructionFragment();
            }
				 /*} else {
						if (mSetupType == SetupType.ADD_SUBWOOFER) {
							showConnectEthernetInstructionForSubwooferFragment();
						} else {
							showConnectEthernetInstructionForSurroundsFragment();
						}
					}*/
				}
			}
		}
		if (player == null) {
			showSoundbarLost();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mAllPlayManager != null) {
			mAllPlayManager.removeOnZoneListChangedListener(this);
			mAllPlayManager.removeOnDeviceListChangedListener(this);
			mAllPlayManager.unRegisterSurroundDiscoveryListener(this);
		}
	}

  public WifiManager getWifiManager() {
    return mWifiManager;
  }

  public Runnable getRunnable() {
    return mEnableOnboardeeRunnable;
  }

  public void setOnboardingSpeakerSsid(String ssid) {
    mCurrentSsid = ssid;
  }

  public String getOnBoardingSpeakerSsid() {
    return mCurrentSsid;
  }

  public String getHomeApSsid() {
    return mHomeApSsid;
  }

  public Handler getUiHandler() {
    return mUiHandler;
  }

  @Override
  void handleConnectivity(Context context, String action) {
    boolean connected;
    String networkName;

    NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
    if (info != null) {
      WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
      networkName = wifiInfo.getSSID();
      connected = info.isConnected();

      String name = Utils.stripSSIDQuotes(networkName);
      if(mSurroundDiscoveryListener != null)
        mSurroundDiscoveryListener.onWifiNetworkChanged(name,connected);
    }
  }

  private SurroundDiscoveryListener mSurroundDiscoveryListener;
  public void setSurroundDiscoveryListener(SurroundDiscoveryListener listener) {

      if(mSurroundDiscoveryListener != null)
        mSurroundDiscoveryListener = null;

      mSurroundDiscoveryListener = listener;
  }

  List<ScanInfo> mScanResult;

  protected void handleScanResult() {
    List<ScanResult> results = mWifiManager.getScanResults();
    if(mScanResult == null)
      mScanResult = Collections.synchronizedList(new ArrayList<>());
    else
      mScanResult.clear();

    for (ScanResult rst : results) {
      int wifiQuality = mWifiManager.calculateSignalLevel(rst.level,5);
      if(ScanInfo.capabilitiesToAuthType(rst.capabilities) == AuthType.OPEN)
        mScanResult.add(new ScanInfo(rst.SSID, AuthType.OPEN, wifiQuality,false));
    }
    Log.d(TAG,"Thread id:"+Thread.currentThread().getName()+",id:"+Thread.currentThread().getId());
    if(!mScanResult.isEmpty()) {
      mOnDeviceWifiScanListChangedListeners.forEach(listener ->listener.onDeviceWifiScanListChanged(mScanResult));
    }
  }

  public List<ScanInfo> getWifiScanList() {
    return mScanResult;
  }

  private final List<OnDeviceWifiScanListChangedListener> mOnDeviceWifiScanListChangedListeners = new ArrayList<>();

  public interface OnDeviceWifiScanListChangedListener {
    void onDeviceWifiScanListChanged(List<ScanInfo> wifiScanList);
  }

  public void addOnDeviceWifiScanListChangedListener(final OnDeviceWifiScanListChangedListener listener) {
		synchronized (mOnDeviceWifiScanListChangedListeners) {
			if (listener != null && !mOnDeviceWifiScanListChangedListeners.contains(listener)) {
				mOnDeviceWifiScanListChangedListeners.add(listener);
			}
		}
	}

	public void removeOnDeviceWifiScanListChangedListener(final OnDeviceWifiScanListChangedListener listener) {
		synchronized (mOnDeviceWifiScanListChangedListeners) {
			if (listener != null) {
				mOnDeviceWifiScanListChangedListeners.remove(listener);
			}
		}
	}

  public void wifiScan() {
    if(mWifiManager != null) {
      mWifiManager.startScan();
    }
  }

	private void showSoundbarLost() {
		final String title = getString(R.string.connection_lost_title);
		final String message = getString(R.string.connection_lost_message);
		final String positiveText = getString(R.string.try_again);
		final String negativeText = getString(R.string.exit_setup);

		CustomDialogFragment soundbarLostFragment = CustomDialogFragment.newDialog(DIALOG_SOUNDBAR_LOST_TAG, title, message, positiveText, negativeText);

		soundbarLostFragment.setButtonClickedListener(this);
		showDialog(soundbarLostFragment, DIALOG_SOUNDBAR_LOST_TAG);
	}

	private void showSoundbarLostRetried() {
		final String title = getString(R.string.connection_lost_title);
		final String message = getString(R.string.connection_lost_retried_message);
		final String negativeText = getString(R.string.exit_setup);

		CustomDialogFragment soundbarRetriedFragment = CustomDialogFragment.newDialog(DIALOG_SOUNDBAR_RETRIED_TAG, title, message, null, negativeText);

		soundbarRetriedFragment.setButtonClickedListener(this);
		showDialog(soundbarRetriedFragment, DIALOG_SOUNDBAR_RETRIED_TAG);
	}

	private void showAdjustAudioInstructionFragment() {
		mFragmentController.startFragment(com.qualcomm.qti.smartaudio.fragment.AdjustAudioInstructionFragment.newInstance(ADJUST_AUDIO_INSTRUCTION_FRAGMENT,
                                                                                                                          mSetupType, (mPreviouslyKnownLeft && mPreviouslyKnownRight)), ADJUST_AUDIO_INSTRUCTION_FRAGMENT, (
        mFragmentController.getCount() > 0));
	}

	private void showAdjustAudioFragment() {
		mFragmentController.push(com.qualcomm.qti.smartaudio.fragment.AdjustAudioFragment.newInstance(ADJUST_AUDIO_FRAGMENT,
                                                                                                      mSoundbarID, (mPreviouslyKnownLeft && mPreviouslyKnownRight)), ADJUST_AUDIO_FRAGMENT);
	}

	private void showAdjustAudioCompletedFragment() {
		mFragmentController.push(com.qualcomm.qti.smartaudio.fragment.AdjustAudioCompletedFragment.newInstance(ADJUST_AUDIO_COMPLETED_FRAGMENT),
				ADJUST_AUDIO_COMPLETED_FRAGMENT);
	}

	private void showAdjustAudioMoreInfoFragment() {
		mFragmentController.push(com.qualcomm.qti.smartaudio.fragment.AdjustAudioMoreInfoFragment.newInstance(ADJUST_AUDIO_MORE_INFO_FRAGMENT),
				ADJUST_AUDIO_MORE_INFO_FRAGMENT);
	}

	private void showConnectEthernetInstructionForSubwooferFragment() {
		mFragmentController.startFragment(ConnectEthernetFragment.newInstance(ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT,
				SetupType.ADD_SUBWOOFER, mSoundbarID),
				ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT, false);
	}

	private void showConnectEthernetInstructionForSurroundsFragment() {
		mFragmentController.startFragment(ConnectEthernetFragment.newInstance(ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT,
				SetupType.ADD_SURROUNDS, mSoundbarID),
				ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT, false);
	}

	private void showAddSurroundsInstructionFragment() {
		mFragmentController.startFragment(AddChannelInstructionFragment.newInstance(ADD_SURROUNDS_INSTRUCTION_FRAGMENT,
				mSetupType, mSoundbarID),
				ADD_SURROUNDS_INSTRUCTION_FRAGMENT, (mFragmentController.getCount() > 0));
	}

	private void showAddRearSurroundsInstructionFragment() {
		mFragmentController.startFragment(AddChannelInstructionFragment.newInstance(ADD_REAR_SURROUNDS_INSTRUCTION_FRAGMENT,
				mSetupType, mSoundbarID),
				ADD_REAR_SURROUNDS_INSTRUCTION_FRAGMENT, (mFragmentController.getCount() > 0));
	}

	private void showAddUpfiringSurroundsInstructionFragment() {
		mFragmentController.startFragment(AddChannelInstructionFragment.newInstance(ADD_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT,
				mSetupType, mSoundbarID),
				ADD_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT, (mFragmentController.getCount() > 0));
	}

	private void showAddRearUpfiringAddSurroundsInstructionFragment() {
		mFragmentController.startFragment(AddChannelInstructionFragment.newInstance(ADD_REAR_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT,
				mSetupType, mSoundbarID),
				ADD_REAR_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT, (mFragmentController.getCount() > 0));
	}

	private void showAddSubwooferInstructionFragment() {
		mFragmentController.startFragment(AddChannelInstructionFragment.newInstance(ADD_SUBWOOFER_INSTRUCTION_FRAGMENT,
				mSetupType, mSoundbarID),
				ADD_SUBWOOFER_INSTRUCTION_FRAGMENT, (mFragmentController.getCount() > 0));
	}

	private void showChooseLeftSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_LEFT_SURROUND_FRAGMENT,
						HomeTheaterChannel.LEFT_SURROUND, mSoundbarID), ADD_LEFT_SURROUND_FRAGMENT);
	}

	private void showChooseRightSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_RIGHT_SURROUND_FRAGMENT,
						HomeTheaterChannel.RIGHT_SURROUND, mSoundbarID), ADD_RIGHT_SURROUND_FRAGMENT);
	}

	private void showChooseSubwooferFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_SUBWOOFER_FRAGMENT,
						HomeTheaterChannel.SUBWOOFER, mSoundbarID), ADD_SUBWOOFER_FRAGMENT);
	}

	private void showLeftSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(LEFT_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.LEFT_SURROUND),
				LEFT_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showRightSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(RIGHT_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.RIGHT_SURROUND),
				RIGHT_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showSubwooferAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(SUBWOOFER_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.SUBWOOFER),
				SUBWOOFER_ADDED_FRAGMENT, true);
	}

	private void showChooseLeftRearSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_LEFT_REAR_SURROUND_FRAGMENT,
						HomeTheaterChannel.LEFT_REAR_SURROUND, mSoundbarID), ADD_LEFT_REAR_SURROUND_FRAGMENT);
	}

	private void showChooseRightRearSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_RIGHT_REAR_SURROUND_FRAGMENT,
						HomeTheaterChannel.RIGHT_REAR_SURROUND, mSoundbarID), ADD_RIGHT_REAR_SURROUND_FRAGMENT);
	}

	private void showChooseLeftUpfiringSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_LEFT_UPFIRING_SURROUND_FRAGMENT,
						HomeTheaterChannel.LEFT_UPFIRING_SURROUND, mSoundbarID), ADD_LEFT_UPFIRING_SURROUND_FRAGMENT);
	}

	private void showChooseRightUpfiringSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_RIGHT_UPFIRING_SURROUND_FRAGMENT,
						HomeTheaterChannel.RIGHT_UPFIRING_SURROUND, mSoundbarID), ADD_RIGHT_UPFIRING_SURROUND_FRAGMENT);
	}

	private void showChooseLeftRearUpfiringSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_LEFT_REAR_UPFIRING_SURROUND_FRAGMENT,
						HomeTheaterChannel.LEFT_UPFIRING_SURROUND, mSoundbarID), ADD_LEFT_REAR_UPFIRING_SURROUND_FRAGMENT);
	}

	private void showChooseRightRearUpfiringSurroundFragment() {
		mFragmentController.push(
				ChooseChannelFragment.newInstance(ADD_RIGHT_REAR_UPFIRING_URROUND_FRAGMENT,
						HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND, mSoundbarID), ADD_RIGHT_REAR_UPFIRING_URROUND_FRAGMENT);
	}

	private void showLeftRearSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(LEFT_REAR_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.LEFT_REAR_SURROUND),
				LEFT_REAR_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showRightRearSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(RIGHT_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.RIGHT_REAR_SURROUND),
				RIGHT_REAR_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showLeftUpfiringSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(LEFT_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.LEFT_UPFIRING_SURROUND),
				LEFT_UPFIRING_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showRightUpfiringSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(RIGHT_UPFIRING_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.RIGHT_UPFIRING_SURROUND),
				RIGHT_UPFIRING_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showLeftRearUpfiringSurroundAddedFragment() {
		mFragmentController.startFragment(
				ChannelAddedFragment.newInstance(LEFT_REARUPFIRING_SURROUND_ADDED_FRAGMENT, mSoundbarID, HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND),
				LEFT_REARUPFIRING_SURROUND_ADDED_FRAGMENT, true);
	}

	private void showRightRearUpfiringSurroundAddedFragment() {
    mFragmentController.startFragment(
        ChannelAddedFragment.newInstance(RIGHT_REARUPFIRING_SURROUND_ADDED_FRAGMENT, mSoundbarID,
            HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND),
        RIGHT_REARUPFIRING_SURROUND_ADDED_FRAGMENT, true);
  }

	@Override
	public void onBackPressed() {
		String tag = mFragmentController.getCurrentFragmentTag();
		if (ADD_SUBWOOFER_INSTRUCTION_FRAGMENT.equals(tag) ||
				ADD_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag) ||
				ADD_REAR_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag) ||
				ADD_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag) ||
				ADD_REAR_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag) ||
				ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT.equals(tag) ||
				ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT.equals(tag) ||
				ADJUST_AUDIO_INSTRUCTION_FRAGMENT.equals(tag) ||
				RIGHT_SURROUND_ADDED_FRAGMENT.equals(tag) ||
				RIGHT_REAR_SURROUND_ADDED_FRAGMENT.equals(tag) ||
				RIGHT_UPFIRING_SURROUND_ADDED_FRAGMENT.equals(tag)) {
			finish();
		} else if (LEFT_SURROUND_ADDED_FRAGMENT.equals(tag) ||
		          LEFT_REAR_SURROUND_ADDED_FRAGMENT.equals(tag) ||
		          LEFT_UPFIRING_SURROUND_ADDED_FRAGMENT.equals(tag) ||
		          LEFT_REARUPFIRING_SURROUND_ADDED_FRAGMENT.equals(tag)) {
			showSurroundsNotAdded();
		} else {
			mFragmentController.pop();
		}
	}

	@Override
	public void onZoneListChanged() {

		IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);

		if (player == null) {
			String tag = mFragmentController.getCurrentFragmentTag();
			if (ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT.equals(tag) ||
					ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT.equals(tag)) {
				// We are waiting for soundbar to come back, let fragment take care of it
			} else {
				showSoundbarLost();
			}

		} else if (isDialogShown(DIALOG_SOUNDBAR_LOST_TAG) || isDialogShown(DIALOG_SOUNDBAR_RETRIED_TAG)) {
			dismissDialog(DIALOG_SOUNDBAR_LOST_TAG);
			dismissDialog(DIALOG_SOUNDBAR_RETRIED_TAG);
			showCorrectSetupFragmentAfterLost();
		}
	}


  @Override
  public void onSurroundSpeakerDiscovered(String playerId, String deviceId) {
    if( !playerId.equalsIgnoreCase(mSoundbarID)) {
      WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
      if(wifiInfo != null) {
        String ssid = Utils.stripSSIDQuotes(wifiInfo.getSSID());
        if (ssid != null && ssid.equalsIgnoreCase(mCurrentSsid) && mSurroundDiscoveryListener != null) {
            mSurroundDiscoveryListener.onSurroundSpeakerDiscovered(playerId, deviceId);
          }
        }
      }
  }

  @Override
	public void onDeviceListChanged() {
		if (isDialogShown(DIALOG_SOUNDBAR_LOST_TAG) || isDialogShown(DIALOG_SOUNDBAR_RETRIED_TAG)) {
			dismissDialog(DIALOG_SOUNDBAR_LOST_TAG);
			dismissDialog(DIALOG_SOUNDBAR_RETRIED_TAG);
			showCorrectSetupFragmentAfterLost();
		}
	}

	private boolean isSoundbarConnected() {
		IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
		IoTDevice device = mAllPlayManager.getDevice(mSoundbarID);
		return (player != null) && (device != null);
	}

	public void showSurroundsNotAdded() {
		CustomDialogFragment surroundsNotAddedFragment = CustomDialogFragment.newDialog(DIALOG_SURROUNDS_NOT_ADDED_TAG,
				getString(R.string.surrounds_not_added_title), getString(R.string.surrounds_not_added_message),
				getString(R.string.try_again), getString(R.string.exit_setup));

		surroundsNotAddedFragment.setButtonClickedListener(this);
		showDialog(surroundsNotAddedFragment, DIALOG_SURROUNDS_NOT_ADDED_TAG);
	}

	@Override
	public void onPositiveButtonClicked(final String tag) {
		if (DIALOG_SOUNDBAR_LOST_TAG.equals(tag)) {
			waitForSoundbar();
		}
	}

	@Override
	public void onNegativeButtonClicked(final String tag) {
		if (DIALOG_SURROUNDS_NOT_ADDED_TAG.equals(tag)) {
			removeSurrounds(mFragmentController.getCurrentFragmentTag());
			return;
		}
		finish();
	}

	@Override
	public void onTopButtonClicked(String tag) {

	}

	@Override
	public void onMiddleButtonClicked(String tag) {
		if (ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT.equals(tag)) {
			showAddSubwooferInstructionFragment();
		} else if (ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT.equals(tag)) {
			showAddSurroundsInstructionFragment();
		} else if (ADJUST_AUDIO_INSTRUCTION_FRAGMENT.equals(tag)) {
			showAdjustAudioCompletedFragment();
		} else if (ADJUST_AUDIO_FRAGMENT.equals(tag)) {
			finish();
		}
	}

	@Override
	public void onBottomButtonClicked(String tag) {
		if (ADD_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag)) {
			IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
			if ((player != null) &&
					(player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_SURROUND)
           || player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_SURROUND))) {
				removeSurrounds(tag);
			} else {
				showChooseLeftSurroundFragment();
			}
		} else if (ADD_REAR_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag)) {
			IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
			if ((player != null) &&
					(player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_REAR_SURROUND)
           || player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_REAR_SURROUND))) {
				removeSurrounds(tag);
			} else {
				showChooseLeftRearSurroundFragment();
			}
		} else if (ADD_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag)) {
			IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
			if ((player != null) &&
					(player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_UPFIRING_SURROUND)
           || player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_UPFIRING_SURROUND))) {
				removeSurrounds(tag);
			} else {
				showChooseLeftUpfiringSurroundFragment();
			}
		} else if (ADD_REAR_UPFIRING_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag)) {
			IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
			if ((player != null) &&
					(player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND)
           || player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND))) {
				removeSurrounds(tag);
			} else {
				showChooseLeftRearUpfiringSurroundFragment();
			}
		}  else if (ADD_SUBWOOFER_INSTRUCTION_FRAGMENT.equals(tag)) {
			showChooseSubwooferFragment();
		} else if (LEFT_SURROUND_ADDED_FRAGMENT.equals(tag)) {
			showChooseRightSurroundFragment();
		} else if(LEFT_REAR_SURROUND_ADDED_FRAGMENT.equals(tag)) {
		  showChooseRightRearSurroundFragment();
    } else if(LEFT_UPFIRING_SURROUND_ADDED_FRAGMENT.equals(tag)) {
      showChooseRightUpfiringSurroundFragment();
    } else if (LEFT_REARUPFIRING_SURROUND_ADDED_FRAGMENT.equals(tag)) {
      showChooseRightRearUpfiringSurroundFragment();
    } else if (ADD_SUBWOOFER_CONNECT_ETHERNET_FRAGMENT.equals(tag) ||
				ADD_SURROUNDS_CONNECT_ETHERNET_FRAGMENT.equals(tag) ||
				ADD_LEFT_SURROUND_FRAGMENT.equals(tag) ||
				ADD_SUBWOOFER_FRAGMENT.equals(tag) ||
				ADJUST_AUDIO_COMPLETED_FRAGMENT.equals(tag)) {
			// for now, we done
			finish();
		} else if (ADD_RIGHT_SURROUND_FRAGMENT.equals(tag)) {
			showSurroundsNotAdded();
		} else if (ADJUST_AUDIO_INSTRUCTION_FRAGMENT.equals(tag)) {
			showAdjustAudioFragment();
		} else if (ADJUST_AUDIO_FRAGMENT.equals(tag)) {
			showAdjustAudioCompletedFragment();
		} else if (RIGHT_SURROUND_ADDED_FRAGMENT.equals(tag)) {
			showAdjustAudioInstructionFragment();
		}
	}

	@Override
	public void onItemClicked(String tag, Object object) {
		// This should be used normally, but
		AddHomeTheaterChannelData addHomeTheaterChannelData = null;
		if (object instanceof AddHomeTheaterChannelData) {
			addHomeTheaterChannelData = (AddHomeTheaterChannelData)object;
		}
		if (ADD_LEFT_SURROUND_FRAGMENT.equals(tag)) {
			// TODO: show audio, for now show left added
			showLeftSurroundAddedFragment();
			if (addHomeTheaterChannelData != null) {
				mPreviouslyKnownLeft = addHomeTheaterChannelData.previouslyKnown;
			}
		} else if (ADD_RIGHT_SURROUND_FRAGMENT.equals(tag)) {
			// TODO: show audio, for now show right added
			showRightSurroundAddedFragment();
			if (addHomeTheaterChannelData != null) {
				mPreviouslyKnownRight = addHomeTheaterChannelData.previouslyKnown;
			}
		} else if (ADD_SUBWOOFER_FRAGMENT.equals(tag)) {
			// TODO: show audio, for now show sub added
			showSubwooferAddedFragment();
		}
	}

	@Override
	public void onPreviousSurroundSettingsClicked(boolean usePreviousSetting) {
		mPreviouslyKnownRight = mPreviouslyKnownLeft = usePreviousSetting;
		showAdjustAudioFragment();
		if (mPreviouslyKnownRight && mPreviouslyKnownLeft) {
			showAdjustAudioCompletedFragment();
		}
	}

	@Override
	public void onMoreInfoClicked(String tag) {
		if (ADJUST_AUDIO_FRAGMENT.equals(tag)) {
			showAdjustAudioMoreInfoFragment();
		}
	}

	private void waitForSoundbar() {
		if (mWaitForSoundbarTask == null) {
			mWaitForSoundbarTask = new WaitForSoundbarTask();
			addTaskToQueue(mWaitForSoundbarTask);
		}
	}

	private boolean isDeviceOnEthernet() {
		IoTDevice device = mAllPlayManager.getDeviceById(mSoundbarID);
		return ((device != null) && (device.getNetworkInterface() == NetworkInterface.ETHERNET));
	}

	private void showCorrectSetupFragmentAfterLost() {
    IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
    if (player == null) {
      return;
    }
    if (mSetupType == SetupType.ADD_SUBWOOFER) {
      if (!isDeviceOnEthernet()) {
        showConnectEthernetInstructionForSubwooferFragment();
      } else if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.SUBWOOFER)) {
        showSubwooferAddedFragment();
      }
    } else if (mSetupType == SetupType.ADD_SURROUNDS) {
      if (!isDeviceOnEthernet()) {
        showConnectEthernetInstructionForSurroundsFragment();
      } else if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_SURROUND) &&
          player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_SURROUND)) {
        showRightSurroundAddedFragment();
      } else if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_SURROUND)) {
        if (ADD_LEFT_SURROUND_FRAGMENT.equals(mFragmentController.getCurrentFragmentTag())) {
          showLeftSurroundAddedFragment();
        }
      }
    } else if (mSetupType == SetupType.ADD_REAR_SURROUNDS) {
      if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_REAR_SURROUND) &&
          player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_REAR_SURROUND)) {
          showRightRearSurroundAddedFragment();
      } else if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_REAR_SURROUND)) {
        if (ADD_LEFT_SURROUND_FRAGMENT.equals(mFragmentController.getCurrentFragmentTag())) {
          showLeftRearSurroundAddedFragment();
      }
    } else if (mSetupType == SetupType.ADD_UPFIRING_SURROUNDS) {
      if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_UPFIRING_SURROUND) &&
          player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_UPFIRING_SURROUND)) {
          showRightUpfiringSurroundAddedFragment();
      } else if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_UPFIRING_SURROUND)) {
        if (ADD_LEFT_SURROUND_FRAGMENT.equals(mFragmentController.getCurrentFragmentTag())) {
          showLeftUpfiringSurroundAddedFragment();
        }
      }
    } else if (mSetupType == SetupType.ADD_REAR_UPFIRING_SURROUNDS) {
      if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND) &&
          player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND)) {
          showRightRearUpfiringSurroundAddedFragment();
        }
      } else if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND)) {
        if (ADD_LEFT_SURROUND_FRAGMENT.equals(mFragmentController.getCurrentFragmentTag())) {
          showLeftRearUpfiringSurroundAddedFragment();
        }
      }
    }else {
			//I think we might be ok here
		}
	}

	private class WaitForSoundbarTask extends RequestAsyncTask implements OnGroupListChangedListener,
			AllPlayManager.OnDeviceListChangedListener {
		private int WAIT_FOR_SOUNDBAR_TIME = 10000;

		public WaitForSoundbarTask() {
			super(getString(R.string.connecting), null, MultichannelSetupActivity.this, null);
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					if (!isSoundbarConnected()) {
						showSoundbarLostRetried();
					} else {
						showCorrectSetupFragmentAfterLost();
					}
				}

				@Override
				public void onRequestFailed() {

				}
			};
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mAllPlayManager.addOnZoneListChangedListener(this);
			mAllPlayManager.addOnDeviceListChangedListener(this);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			int waitTime = WAIT_FOR_SOUNDBAR_TIME;
			if (isSoundbarConnected()) {
				waitTime = DEFAULT_WAIT_TIME;
			}
			doWait(waitTime);
			return null;
		}

		@Override
		protected void onPostExecute(final Void param) {
			mWaitForSoundbarTask = null;
			mAllPlayManager.removeOnZoneListChangedListener(this);
			mAllPlayManager.removeOnDeviceListChangedListener(this);

			super.onPostExecute(param);
		}

		private void notifyIfSoundbarConnected() {
			if (isSoundbarConnected()) {
				synchronized (this) {
					notifyAll();
				}
			}
		}

		@Override
		public void onZoneListChanged() {
		  IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
		   if(player != null) {
			  notifyIfSoundbarConnected();
      }
		}

    @Override
		public void onDeviceListChanged() {
			notifyIfSoundbarConnected();
		}
	}

	private void removeSurrounds(final String tag) {
    IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
    if (player != null) {
      player.removeHomeTheaterChannelSurrounds(success -> {
        if(success) {
          if (ADD_SURROUNDS_INSTRUCTION_FRAGMENT.equals(tag)) {
          showChooseLeftSurroundFragment();
        } else if (ADD_RIGHT_SURROUND_FRAGMENT.equals(tag) ||
            LEFT_SURROUND_ADDED_FRAGMENT.equals(tag)) {
          MultichannelSetupActivity.this.finish();
        }
        }
      });
    }
	}

	private class RemoveSurroundsTask extends RequestAsyncTask {
		final String mTag;
		public RemoveSurroundsTask(final String tag) {
			super(getString(R.string.progress_removing_surrounds), null, MultichannelSetupActivity.this, null);
			mTag = tag;
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					if (ADD_SURROUNDS_INSTRUCTION_FRAGMENT.equals(mTag)) {
						showChooseLeftSurroundFragment();
					} else if (ADD_RIGHT_SURROUND_FRAGMENT.equals(mTag) ||
							LEFT_SURROUND_ADDED_FRAGMENT.equals(mTag)) {
						MultichannelSetupActivity.this.finish();
					}
				}

				@Override
				public void onRequestFailed() {

				}
			};
		}

		@Override
		protected Void doInBackground(Void... voids) {
			IoTPlayer player = mAllPlayManager.getPlayer(mSoundbarID);
			if (player != null) {
				player.removeHomeTheaterChannelSurrounds(success -> {
				  if(success)
				    Log.d(TAG,"Remove all satellite speakers:" + success);
				});
			}
			doWait(DEFAULT_WAIT_TIME);
			return null;
		}

		@Override
		protected void onPostExecute(final Void param) {
			mRemoveSurroundsTask = null;
			super.onPostExecute(param);
		}
	}
}
