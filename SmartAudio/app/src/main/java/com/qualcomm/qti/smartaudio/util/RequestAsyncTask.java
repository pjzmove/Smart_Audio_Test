/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import com.qualcomm.qti.smartaudio.activity.BaseActivity;

/**
 * This is the base class for all BaseAsyncTasks that requires a progress dialog
 */
public abstract class RequestAsyncTask extends BaseAsyncTask {
	private static final String TAG = RequestAsyncTask.class.getSimpleName();

	public static int DEFAULT_WAIT_TIME = 1000; // Have progress dialog up at least 1 seconds

	private long mStartTime = 0;

	private String mProgressTitle = null;
	private String mProgressMessage = null;

	// The TAG it uses to keep track of its dialog
	private static final String DIALOG_REQUEST_ASYNC_TASK_PROGRESS_TAG = "DialogRequestAsyncTaskProgressTag";

	/**
	 * Constructor
	 * @param progressTitle the title of the progress dialog
	 * @param progressMessage the message of the progress dialog
	 * @param baseActivity the BaseActivity object
	 * @param listener the RequestListener callback object
	 */
	public RequestAsyncTask(final String progressTitle, final String progressMessage,
							final BaseActivity baseActivity, final RequestListener listener) {
		super(baseActivity, listener);
		mProgressTitle = progressTitle;
		mProgressMessage = progressMessage;
	}

	@Override
	protected void onPreExecute() {
		showProgressDialog();

		super.onPreExecute();
		// We start the progress dialog up time.
		startTime();
	}

	@Override
	protected void onPostExecute(final Void param) {
		// Dismiss the dialog
		dismissProgressDialog();

		super.onPostExecute(param);
	}

	@Override
	protected void onCancelled(final Void voidParam) {
		// We want to interrupt the wait for dialog here.
		interrupt();
		// Dismiss the dialog
		dismissProgressDialog();
		super.onCancelled(voidParam);
	}

	private void showProgressDialog() {
		final BaseActivity baseActivity = getActiveActivity();
		if (baseActivity == null) {
			return;
		}
		baseActivity.showProgressDialog(DIALOG_REQUEST_ASYNC_TASK_PROGRESS_TAG, mProgressTitle, mProgressMessage);
	}

	private void dismissProgressDialog() {
		final BaseActivity baseActivity = getActiveActivity();
		if (baseActivity == null) {
			return;
		}
		baseActivity.dismissDialog(DIALOG_REQUEST_ASYNC_TASK_PROGRESS_TAG);
	}


	/**
	 * This interrupts the dialog wait time.
	 */
	protected void interrupt() {
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * Save the start time when dialog is up.
	 */
	protected void startTime() {
		mStartTime = System.currentTimeMillis();
	}

	/**
	 * This waits for specified time for the task.
	 * @param timeout the time to wait
	 */
	protected void doWait(final int timeout) {
		long currentTime = System.currentTimeMillis();
		if ((currentTime - mStartTime) < timeout) {
			synchronized (this) {
				try {
					this.wait(timeout - (currentTime - mStartTime));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
