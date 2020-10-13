/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.manager.BlurManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.smartaudio.util.AsyncTaskQueue;
import com.qualcomm.qti.smartaudio.util.BaseAsyncTask;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.receiver.ConnectivityReceiver.ConnectivityChangedListener;
import java.lang.ref.WeakReference;


public class BaseActivity extends AppCompatActivity implements ConnectivityChangedListener {

	/**
 	 * The tag to display logs from this class.
 	 */
 	private static final String TAG = "BaseActivity";

	public static final int RESULT_RESET = 1;

	public static final String DIALOG_NO_WIFI_TAG = "DialogNoWifiTag";

	// Simple flag for read storage permission check
	public static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
	public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
	public static final String DIALOG_COMMUNICATION_PROBLEM_TAG = "DialogCommunicationProblemTag";

	protected boolean mIsSaveStateCalled = false;
	protected boolean mIsTablet = false;
	protected boolean mIsListeningConnectivity = true;
	
	private AsyncTaskQueue mAsyncTaskQueue;
	
	// Use this to access few singleton managers
	protected SmartAudioApplication mApp = null;
	protected BlurManager mBlurManager;
	protected AllPlayManager mAllPlayManager = null;
	/**
 	 * To manage the IoT related features.
 	 */
  protected IoTSysManager mIoTSysManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIsSaveStateCalled = false;
		mApp = (SmartAudioApplication) getApplicationContext();
		if (mApp.isInit()) {
			if (mAllPlayManager == null) {
				mAllPlayManager = mApp.getAllPlayManager();
			}
			// getting the IoT system manager
 			if (mIoTSysManager == null) {
 				try {
 					mIoTSysManager = IoTSysManager.getInstance();
 				} catch (Exception e) {
 					Log.w(TAG, "[onCreate] getting IoTSysManager failed with exception\n" + e.getMessage());
 				}
 			}
		}
		mBlurManager = new BlurManager(this);
		mAsyncTaskQueue = new AsyncTaskQueue(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mIsSaveStateCalled = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsSaveStateCalled = false;
		mBlurManager.onResume();
		mAsyncTaskQueue.onResume();
		if (mApp.isInit() && mIsListeningConnectivity) {
		  if(mApp.getConnectivityReceiver() != null)
			  mApp.getConnectivityReceiver().addConnectivityChangedListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mIsSaveStateCalled = true;
		mBlurManager.onPause();
		mAsyncTaskQueue.onPause();
		if (mApp.isInit() && mIsListeningConnectivity) {
		  if(mApp.getConnectivityReceiver() != null)
			  mApp.getConnectivityReceiver().removeConnectivityChangedListener(this);
		}
	}

	protected void update() {}

	protected void updateInUiThread() {
		if (Utils.isActivityActive(this)) {
			runOnUiThread(() -> {
        if (Utils.isActivityActive(BaseActivity.this)) {
          update();
        }
      });
		}
	}

  /**
 	 * <p>This method is called by {@link #updateInUiThread(Object...) updateInUiThread(Object...)}
 	 * and must be implemented by child class if child class uses the calling method.</p>
 	 * <p>This method allows to run tasks within the UI thread.</p>
 	 *
 	 * @param arguments The <code>arguments</code> required to update the state(s). These arguments
 	 * must be given to the calling method.
 	 */
 	protected void update(Object... arguments) {
 	}

 	/**
 	 * <p>This method runs {@link #update(Object...) update(Object...)} in the UI thread and passes
 	 * it
 	 * the given arguments.</p>
 	 *
 	 * @param arguments The arguments to pass to {@link #update(Object...) updateState(Object...)}
 	 */
 	public void updateInUiThread(Object... arguments) {
 		if (Utils.isActivityActive(this)) {
 			runOnUiThread(() -> {
 				if (Utils.isActivityActive(BaseActivity.this)) {
 					update(arguments);
 				}
 			});
 		}
 	}



	public boolean isSaveStateCalled() {
		return mIsSaveStateCalled;
	}

	public boolean isTablet() { return mIsTablet; }

	public boolean isConnectedToNetwork() {
		return mApp.getConnectivityReceiver().isConnected();
	}

	public void addTaskToQueue(final BaseAsyncTask task) { mAsyncTaskQueue.add(task); }

	public void runTask(final BaseAsyncTask task) {
		if ((task == null) || (task.getStatus() != AsyncTask.Status.PENDING)) {
			return;
		}
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
	}

	protected boolean hasAskedForStoragePermission() {
		// We need to do a storage permission check for Android M and above
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
					ActivityCompat.requestPermissions(this,
							new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE },
							MY_PERMISSIONS_REQUEST_READ_STORAGE);
					return false;
				}
			}
		}
		return true;
	}

	protected boolean hasAskedForAccessCoarseLocationPermission() {
		// We need to do a location permission check for Android M and above
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
				if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
					ActivityCompat.requestPermissions(this,
							new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION },
							MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void onConnectivityChanged(boolean connected) {
		if (connected) {
			// We are connected, dismiss no wifi dialog
			if (isDialogShown(DIALOG_NO_WIFI_TAG)) {
				dismissDialog(DIALOG_NO_WIFI_TAG);
			}
		} else {
			// Show the no wifi dialog
			showNoWifiDialog();
		}
	}

	public void showNoWifiDialog() {
		CustomDialogFragment dialogFragment = CustomDialogFragment.newNoWifiDialog();
		showDialog(dialogFragment, DIALOG_NO_WIFI_TAG);
	}

	public WeakReference<CustomDialogFragment> showProgressDialog(final String tag, final String title, final String message) {
		CustomDialogFragment dialogFragment = CustomDialogFragment.newProgressDialog(tag, title, message);
		showDialog(dialogFragment, tag);
		return new WeakReference<>(dialogFragment);
	}

	public void showDialog(CustomDialogFragment dialogFragment, String tag) {
		mBlurManager.showDialog(dialogFragment, tag);
	}

	public String getCurrentDialogTag() {
		return mBlurManager.getCurrentDialogTag();
	}

	public void showNavigationDrawer(final BlurManager.BlurListener listener) {
		mBlurManager.showNavigationDrawer(listener);
	}

	public void showPopupWindow(final BlurManager.BlurListener listener) {
		mBlurManager.showPopupWindow(listener);
	}

	public void showGroupView(final BlurManager.BlurListener listener) {
		mBlurManager.showGroupView(listener);
	}

	public boolean isDialogShown(final String tag) {
		return mBlurManager.isDialogShown(tag);
	}

	public void dismissDialog(final String tag) {
		mBlurManager.dismissDialog(tag);
	}

	public void dismiss(final BlurManager.BlurListener listener) {
		mBlurManager.dismiss(listener);
	}

	public void showCommunicationProblem(final CustomDialogFragment.OnCustomDialogButtonClickedListener listener) {
		final CustomDialogFragment customDialogFragment = CustomDialogFragment
				.newDialog(DIALOG_COMMUNICATION_PROBLEM_TAG, getString(R.string.communication_problem_title), getString(R.string.communication_problem_message),
						getString(R.string.try_again), getString(R.string.exit_setup));

		customDialogFragment.setButtonClickedListener(listener);
		showDialog(customDialogFragment, DIALOG_COMMUNICATION_PROBLEM_TAG);
	}
}
