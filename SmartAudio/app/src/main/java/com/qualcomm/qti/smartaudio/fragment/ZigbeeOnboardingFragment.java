/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.onZigbeeListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTConstants;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.CoordinatorState;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.JoiningState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import java.lang.ref.WeakReference;

public class ZigbeeOnboardingFragment extends BaseFragment implements onZigbeeListener {

  private final static String TAG = "ZigbeeOnboarding";
  private String mID;
	private String mHost;
	private TextView mProgressTextView;
	private ProgressBar mScanProgressBar;
	private ZigBeeOnboardingState mState;
	private static InternalHandler mHandler;

  private enum ZigBeeOnboardingState {
    INIT,
    FORMING,
    JOINING,
    FINISHED,
    ERROR
  }

  private static class InternalHandler extends Handler {

    private WeakReference<ZigbeeOnboardingFragment> mParent;

    InternalHandler(ZigbeeOnboardingFragment fragment) {
      mParent = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
      switch(msg.what) {
        case IoTConstants.ZIGBEE_FORMATION_REQUEST_TIMEOUT:
        case IoTConstants.ZIGBEE_JOINING_REQUEST_TIMEOUT:
          Log.e(TAG,"Zigbee request timeout:" + msg.what);
          if(mParent.get() != null)
            mParent.get().finished();
        break;
        default:
        break;
      }
    }
  }

  public static ZigbeeOnboardingFragment newInstance(String id, String host) {
     ZigbeeOnboardingFragment fragment = new ZigbeeOnboardingFragment();
     Bundle bundle = new Bundle();
     bundle.putString(EXTRA_ID,id);
     bundle.putString(EXTRA_HOST,host);
     fragment.setArguments(bundle);


     return fragment;
  }

  @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_zigbee_onboarding, container, false);
    Bundle arg = getArguments();
    mID = arg.getString(EXTRA_ID);
    mHost = arg.getString(EXTRA_HOST);
    mHost = ControllerSdkUtils.stripHostName(mHost);
    TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
    titleView.setText(getText(R.string.zigbee_onboarding_title));

		mProgressTextView = view.findViewById(R.id.zigbee_onboarding_progress_text);
    mScanProgressBar = view.findViewById(R.id.zigbee_onboarding_progress);
    mScanProgressBar.setVisibility(View.VISIBLE);
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    if(mIoTSysManager != null) {
      mIoTSysManager.addZigbeeiListener(this);
      mState = ZigBeeOnboardingState.INIT;

      if(mHandler == null)
        mHandler =  new InternalHandler(this);

      startZigbee();
    }
  }

  @Override
  public void onPause() {
    mHandler.removeCallbacksAndMessages(null);
    if(mIoTSysManager != null) {
      mIoTSysManager.removeZigbeeListener(this);
    }
    mHandler = null;
    super.onPause();
  }

  private void startZigbee() {
    IoTDevice selectedDevice = mAllPlayManager.getDeviceByHostName(mHost);
    if(selectedDevice != null) {
      if (selectedDevice.getCoordinatorState() != CoordinatorState.kNominated) {
        Log.d(TAG, "Start formation request!");
        startFormation(selectedDevice);
      } else {
        Log.d(TAG, "Start join request!");
        startJoining(selectedDevice);
      }
    } else {
      showError(getString(R.string.zigbee_error_no_device_found));
      mScanProgressBar.setVisibility(View.GONE);
    }
  }

  private void startFormation(IoTDevice device) {
    waitForCompletion(15,IoTConstants.ZIGBEE_FORMATION_REQUEST_TIMEOUT,getString(R.string.zigbee_onboarding_formation_status_text));
    if(device != null && device.startFormZbNetworkWithCompletion(success -> {
        if(!success)
          showError(getString(R.string.zigbee_formation_request_error_message));
      })) {
    }
    else {
      showError(getString(R.string.zigbee_error_no_device_found));
      mScanProgressBar.setVisibility(View.GONE);
    }
  }

  private void startJoining(IoTDevice device) {
    waitForCompletion(30,IoTConstants.ZIGBEE_JOINING_REQUEST_TIMEOUT,getString(R.string.zigbee_onboarding_join_status_text));
    if(device != null &&
      device.startZbJoiningWithTimeout(30,success -> {
        if(!success)
          showError(getString(R.string.zigbee_joining_request_error_message));
      })) {
    } else {
      showError(getString(R.string.zigbee_error_no_device_found));
      mScanProgressBar.setVisibility(View.GONE);
    }
  }


  private void waitForCompletion(final int timeout, final int what,final String message) {
    mHandler.sendMessageDelayed(Message.obtain(mHandler, what), timeout * 1000);
    UiThreadExecutor.getInstance().execute(()-> {
      mState = (what == IoTConstants.ZIGBEE_FORMATION_REQUEST_TIMEOUT
          ? ZigBeeOnboardingState.FORMING : ZigBeeOnboardingState.JOINING);
      mProgressTextView.setText(message);
    });
  }

  private void showError(String message) {
    UiThreadExecutor.getInstance().execute(()->
      mProgressTextView.setText(message)
    );
  }

  @Override
  public void onZbAdapterStateChanged(IoTDevice device) {
    String id = device.getId();
    Log.d(TAG,"ZG onboarding onZbAdapterStateChanged notify device ID:" + id + ", current id "+mID);
    if(id != null && id.equalsIgnoreCase(mID)) {
      if(!device.isZigbeeEnabled()) {
        finished();
      }
    }
  }

  @Override
  public void onZbCoordinatorStateDidChanged(IoTDevice device) {
      String id = device.getId();
      Log.d(TAG,"ZG onboarding onZbCoordinatorStateDidChanged notify device ID:" + id + ", current id "+mID);
      CoordinatorState newState = device.getCoordinatorState();
      if(id != null && id.equalsIgnoreCase(mID)) {
        boolean isForming;
        synchronized (mState) {
          isForming = (mState == ZigBeeOnboardingState.FORMING);
        }

        if (isForming) {
          mHandler.removeMessages(IoTConstants.ZIGBEE_FORMATION_REQUEST_TIMEOUT);
          if (newState == CoordinatorState.kNominated) {
            waitForCompletion(30, IoTConstants.ZIGBEE_JOINING_REQUEST_TIMEOUT,
                getString(R.string.zigbee_onboarding_join_status_text));
          } else {
            showError(getString(R.string.zigbee_formation_error_message));
          }
        }
      }
  }

  @Override
  public void onZbJoinedDevicesDidChanged(IoTDevice device) {
    Log.d(TAG,"ZG onboarding onZbJoinedDevicesDidChanged notify ");
  }

  @Override
  public void OnZbJoiningStateDidChanged(IoTDevice device, boolean allowed) {
    String id = device.getId();
    Log.d(TAG,"ZG onboarding OnZbJoiningStateDidChanged notify device ID:" + id + ", current id "+mID);
    if(id != null && id.equalsIgnoreCase(mID)) {
      JoiningState newState = device.getJoinZbJoiningState();
      boolean isJoining;
      synchronized (mState) {
        isJoining = (mState == ZigBeeOnboardingState.JOINING);
      }

      if (isJoining) {
        if (allowed) {
          Log.d(TAG, "Zigbee Joining state allowed");
        } else {
          finished();
        }
      }
    }
  }

  private void finished() {
    mHandler.removeCallbacksAndMessages(null);
    UiThreadExecutor.getInstance().execute(() -> {
      if (getActivity() != null) {
        getActivity().onBackPressed();
      }
    });
  }

}
