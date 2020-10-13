/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.os.AsyncTask;

import com.qualcomm.qti.smartaudio.activity.BaseActivity;

import java.lang.ref.WeakReference;

public abstract class BaseAsyncTask extends AsyncTask<Void, Void, Void> {

	private WeakReference<BaseActivity> mActivityRef = null;
	private Object mWaitCondition = new Object();

	private AsyncTaskQueue mAsyncTaskQueue = null;

	protected boolean mResult = true;
	protected RequestListener mListener = null;

	public void waitUntilFinished() {
		synchronized (mWaitCondition) {
			try {
				mWaitCondition.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected BaseActivity getActiveActivity() {
		final BaseActivity baseActivity = mActivityRef.get();
		return (Utils.isActivityActive(baseActivity)) ? baseActivity : null;
	}

	protected void finished() {
		synchronized (mWaitCondition) {
			mWaitCondition.notifyAll();
		}
	}

	/**
	 * This should be more of a helper function if additional calls needed for onPreExecute call.
	 */
	protected void prepare() {}

	/**
	 * This should be more of a helper function if additional calls needed to clean up for onPostExecute call.
	 */
	protected void clean() {}

	public BaseAsyncTask(final BaseActivity baseActivity) {
		mActivityRef = new WeakReference<>(baseActivity);
	}

	public BaseAsyncTask(final BaseActivity baseActivity, final RequestListener listener) {
		mActivityRef = new WeakReference<>(baseActivity);
		mListener = listener;
	}

	public void setRequestListener(final RequestListener listener) {
		mListener = listener;
	}

	void setAsyncTaskQueue(final AsyncTaskQueue asyncTaskQueue) {
		mAsyncTaskQueue = asyncTaskQueue;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		prepare();
	}

	@Override
	protected void onPostExecute(final Void voidParam) {
		super.onPostExecute(voidParam);
		clean();
		checkForListener();
	}

	protected void checkForListener() {
		final BaseActivity baseActivity = mActivityRef.get();
		if (!Utils.isActivityActive(baseActivity)) {
			if ((mAsyncTaskQueue != null) && (mListener != null)) {
				mAsyncTaskQueue.addRequestListener(mListener, mResult);
				finished();
				return;
			}
		}
		if (mListener != null) {
			notifyRequestListener(mListener, mResult);
		}
		finished();
	}

	public static void notifyRequestListener(final RequestListener listener, final boolean result) {
		if (listener != null) {
			if (result) {
				listener.onRequestSuccess();
			} else {
				listener.onRequestFailed();
			}
		}
	}

	@Override
	protected void onCancelled(final Void voidParam) {
		super.onCancelled(voidParam);
		clean();
		finished();
	}

	public interface RequestListener {
		void onRequestSuccess();
		void onRequestFailed();
	}
}
