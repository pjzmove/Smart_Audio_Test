/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.app;

import static com.qualcomm.qti.iotcontrollersdk.controller.IoTConstants.OCF_RESOURCE_TYPE_ALLPLAY;
import static com.qualcomm.qti.iotcontrollersdk.controller.IoTConstants.OCF_RESOURCE_TYPE_IOTSYS;

import android.app.Application;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.OnBoardingManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.receiver.ConnectivityReceiver;
import com.qualcomm.qti.smartaudio.service.HttpServerService;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTDiscovery;

import java.util.Arrays;

import com.qualcomm.qti.smartaudio.receiver.ConnectivityReceiver.ConnectivityChangedListener;


/**
 * The Android Application class that SmartAudio will create when it starts.
 * Since there will only be a single application object, it will contain most of the managers
 */
public class SmartAudioApplication extends Application implements ConnectivityChangedListener {

    private static final String TAG = "SmartAudioApplication";

    private boolean mIsInit = false;
    private ConnectivityReceiver mConnectivityReceiver = null;
    private AllPlayManager mAllPlayManager = null;
    private boolean mResetApp = false;

    private final int HTTP_START_RETRY = 150;
    private final int HTTP_START_WAIT_TIMEOUT = 100;

    private Thread.UncaughtExceptionHandler mUncaughtException = (thread, ex) -> Log.e(TAG, ex.toString());


    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(mUncaughtException);
        Log.e("Rocky", "delete no use files");
    }

    /**
     * Initialize internal application objects
     */
    public void init() {
        IoTService.init(Arrays.asList(OCF_RESOURCE_TYPE_ALLPLAY, OCF_RESOURCE_TYPE_IOTSYS), IoTDiscovery.getInstance());

        AllPlayManager.init(this);
        IoTSysManager.init(this);

        mAllPlayManager = AllPlayManager.getInstance();
        mAllPlayManager.start();

        mConnectivityReceiver = new ConnectivityReceiver(this);
        mConnectivityReceiver.addConnectivityChangedListener(this);

        startHttpService();

        mIsInit = true;
    }

    /**
     * Check if internal application objects are initialized
     *
     * @return true if internal application objects are initialized
     */
    public boolean isInit() {
        return mIsInit;
    }

    public AllPlayManager getAllPlayManager() {
        return mAllPlayManager;
    }

    /**
     * <p>This method loads the onboarding manager and stops the all play service.</p>
     */
    public void loadOnBoarding() {
        mAllPlayManager.stop();
        OnBoardingManager.prepare(this);
    }

    /**
     * <p>This method unloads the onboarding manager and restarts the all play service.</p>
     */
    public void unloadOnBoarding() {
        OnBoardingManager.release(this);
        mAllPlayManager.start();
    }

    /**
     * <p>This method provides the onboarding manager in order to board a device.</p>
     *
     * @return the instance if the onboarding manager or null if the manager hasn't been loaded or has been unloaded.
     */
    public OnBoardingManager getOnBoardingManager() {
        return OnBoardingManager.getInstance();
    }

    public ConnectivityReceiver getConnectivityReceiver() {
        return mConnectivityReceiver;
    }

    @Override
    public void onConnectivityChanged(boolean connected) {
        if (!isInit()) {
            return;
        }
    }

    public boolean startHttpService() {
        if (HttpServerService.isHttpServerServiceStarted()) {
            return true;
        }

        Intent startIntent = new Intent(this, HttpServerService.class);
        startService(startIntent);

        int i = 0;
        while (!HttpServerService.isHttpServerServiceStarted() && (i < HTTP_START_RETRY)) {
            try {
                Thread.sleep(HTTP_START_WAIT_TIMEOUT);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        return HttpServerService.isHttpServerServiceStarted();
    }

    private final static int MONITOR_MESSAGE_HTTP_SERVER_STATUS = 1;
    private final static int CHECK_MESSAGE_HTTP_SERVER_STATUS = 2;
    public final static int MESSAGE_HTTP_SERVER_STARTED = 3;
    public final static int MESSAGE_HTTP_SERVER_FAILED_STARTED = 4;
    private int mRetryNum = 3;
    private Handler internalHandler;
    private HandlerThread mStatusMonitor;

    public void restartHttpService(Handler handle) {
        if (HttpServerService.isHttpServerServiceStarted()) {
            return;
        }

        if (handle == null) {
            return;
        }

        Log.d(TAG, "Restart HTTP service!");

        Intent startIntent = new Intent(this, HttpServerService.class);
        startService(startIntent);

        mStatusMonitor = new HandlerThread("Monitor");
        mStatusMonitor.start();
        mRetryNum = 0;

        internalHandler = new Handler(mStatusMonitor.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what != MONITOR_MESSAGE_HTTP_SERVER_STATUS && msg.what != CHECK_MESSAGE_HTTP_SERVER_STATUS) {
                    // other messages are not expected here.
                    return;
                }

                if (!HttpServerService.isHttpServerServiceStarted() && mRetryNum < HTTP_START_RETRY) {
                    mRetryNum++;
                    sendMessageDelayed(obtainMessage(CHECK_MESSAGE_HTTP_SERVER_STATUS),
                                       HTTP_START_WAIT_TIMEOUT);
                }
                else if (!HttpServerService.isHttpServerServiceStarted()) {
                    handle.sendMessage(handle.obtainMessage(MESSAGE_HTTP_SERVER_FAILED_STARTED));
                    stopHandler();
                }
                else {
                    handle.sendMessage(handle.obtainMessage(MESSAGE_HTTP_SERVER_STARTED));
                }
                super.handleMessage(msg);
            }
        };

        internalHandler.sendMessage(internalHandler.obtainMessage(MONITOR_MESSAGE_HTTP_SERVER_STATUS));
    }

    private void stopHandler() {
        internalHandler.removeCallbacksAndMessages(null);
        mStatusMonitor.quitSafely();
        internalHandler = null;
        mStatusMonitor = null;
    }

    synchronized public void setResetApp(final boolean reset) {
        mResetApp = reset;
        if (reset) {
            reset();
        }
    }

    public void stop() {
        Intent intent = new Intent(this, HttpServerService.class);
        stopService(intent);
        reset();
    }

    public synchronized void reset() {
        // release onboarding if running
        unloadOnBoarding();

        if (mAllPlayManager.isStarted()) {
            mAllPlayManager.stop();
        }

        try {
            if (mConnectivityReceiver != null) {
                unregisterReceiver(mConnectivityReceiver);
                mConnectivityReceiver = null;
            }
        }
        catch (IllegalArgumentException e) {
            Log.e(TAG, "Reset Connection Receiver - ", e);
        }
        mIsInit = false;
    }
}
