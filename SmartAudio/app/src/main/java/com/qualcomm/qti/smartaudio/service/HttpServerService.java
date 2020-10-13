/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;

import android.util.Log;
import java.io.IOException;

/**
 * The debate here is if we can just do a simple IntentService.
 * I actually don't know if this service is stopped, the http server is cleaned up correctly.
 */

public class HttpServerService extends Service implements HttpServer.HttpServerListener {
	private static final String TAG = HttpServerService.class.getSimpleName();

	private HttpServer mServer = null;

	private final int ACTION_START = 0;
	private final int ACTION_STOP = 1;

	public static boolean sIsHttpServerServiceStarted = false;

	private HttpServerServiceHandler mHttpServerServiceHandler = null;

	private final class HttpServerServiceHandler extends Handler {
		public HttpServerServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ACTION_START:
					if (mServer == null) {
						mServer = new HttpServer(HttpServerService.this, HttpServerService.this);
					}
					if (!mServer.isAlive()) {
						try {
							mServer.start();
							sIsHttpServerServiceStarted = mServer.isAlive();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
				case ACTION_STOP:
					if ((mServer != null) && mServer.isAlive()) {
						mServer.stop();
					}
					mServer = null;
					sIsHttpServerServiceStarted = false;
					break;
			}
		}
	}

	public static boolean isHttpServerServiceStarted() {
		return sIsHttpServerServiceStarted;
	}

	@Override
	public void onCreate() {
		HandlerThread thread = new HandlerThread("HttpServerServiceHandlerThread", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mHttpServerServiceHandler = new HttpServerServiceHandler(thread.getLooper());
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		mHttpServerServiceHandler.sendEmptyMessage(ACTION_START);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		mHttpServerServiceHandler.sendEmptyMessage(ACTION_STOP);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onIdleTimedOut() {
	  Log.d(TAG,"HTTP server stopped!");
		stopSelf();
	}
}
