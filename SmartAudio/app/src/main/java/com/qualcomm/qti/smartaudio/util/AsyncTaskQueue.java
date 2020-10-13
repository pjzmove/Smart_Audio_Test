/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.qualcomm.qti.smartaudio.activity.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTaskQueue {
	private static final String TAG = AsyncTaskQueue.class.getSimpleName();

	private WeakReference<BaseActivity> mActivityRef = null;
	private final Queue<BaseAsyncTask> mQueue = new ConcurrentLinkedQueue<>();
	private final ExecutorService mService = Executors.newSingleThreadExecutor();
	private final Map<BaseAsyncTask.RequestListener, Boolean> mResumeListenerMap = new HashMap<>();

	private Boolean mOnPause = false;
	private Handler mHandler = null;

	// Only 1 thread, so it is not a thread pool.
	private Thread mThread = null;

	public AsyncTaskQueue(final BaseActivity activity) {
		mActivityRef = new WeakReference<>(activity);
		mHandler = new Handler(Looper.getMainLooper());
	}

	void addRequestListener(final BaseAsyncTask.RequestListener listener, final boolean result) {
		synchronized (mResumeListenerMap) {
			mResumeListenerMap.put(listener, new Boolean(result));
		}
	}

	public void onResume() {
		synchronized (mOnPause) {
			if (mOnPause) {
				mOnPause.notifyAll();
				mOnPause = false;
			}
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (mResumeListenerMap) {
					for (Map.Entry<BaseAsyncTask.RequestListener, Boolean> entry : mResumeListenerMap.entrySet()) {
						BaseAsyncTask.RequestListener listener = entry.getKey();
						Boolean result = entry.getValue();
						BaseAsyncTask.notifyRequestListener(listener, result);
					}
					mResumeListenerMap.clear();
				}
			}
		});
	}

	public void onPause() {
		synchronized (mOnPause) {
			mOnPause = true;
		}
		mQueue.clear();
	}

	/**
	 * This adds a new task into the queue.  If the thread is not created, it will create and run the tasks in the queue.
	 * @param newTask the new BaseAsyncTask task
	 */
	public void add(final BaseAsyncTask newTask) {
		if (newTask == null) {
			// No need to add a null task
			return;
		}

		// No need to add if paused
		synchronized (mOnPause) {
			if (mOnPause) {
				return;
			}
		}
		// Add the task to the queue.
		mQueue.add(newTask);
		synchronized (this) {
			// If the thread is null, start a new one.
			if (mThread == null) {
				mThread = new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							// Infinite loop until it breaks
							if (mQueue.isEmpty()) {
								// The queue is empty, this thread can be closed
								synchronized (AsyncTaskQueue.this) {
									mThread = null;
								}
								// Break out of the thread.
								break;
							}

							synchronized (mOnPause) {
								if (mOnPause) {
									try {
										mOnPause.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}

							// Infinite loop until it breaks
							if (mQueue.isEmpty()) {
								// The queue is empty, this thread can be closed
								synchronized (AsyncTaskQueue.this) {
									mThread = null;
								}
								// Break out of the thread.
								break;
							}

							// Get the head task
							BaseAsyncTask baseAsyncTask = mQueue.poll();
							AsyncTask.Status status = baseAsyncTask.getStatus();
							while ((status == AsyncTask.Status.FINISHED) || baseAsyncTask.isCancelled()) {
								baseAsyncTask = mQueue.poll();
								if(baseAsyncTask!= null)
								  status = baseAsyncTask.getStatus();
							}

							final BaseAsyncTask runTask = baseAsyncTask;
							runTask.setAsyncTaskQueue(AsyncTaskQueue.this);

							// We run the execute on UI thread because we need the pre and post to run on UI thread.
							final BaseActivity baseActivity = mActivityRef.get();
							if (Utils.isActivityActive(baseActivity)) {
								baseActivity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										runTask.executeOnExecutor(mService);
									}
								});
								if (runTask.getStatus() != AsyncTask.Status.FINISHED) {
									runTask.waitUntilFinished();
								}
							}
						}
					}
				});
				// Start the thread
				mThread.start();
			}
		}
	}
}
