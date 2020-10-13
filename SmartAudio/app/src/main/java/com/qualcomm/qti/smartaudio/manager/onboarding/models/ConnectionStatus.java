/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.models;

import android.util.Log;

/**
 * <p>All responses receives by the HTTP request
 * {@link com.qualcomm.qti.smartaudio.manager.onboarding.requests.CheckConnectStatusRequest CheckConnectStatusRequest}.</p>
 */
public enum ConnectionStatus {

    SUCCESS,
    FAILED,
    UNKNOWN;

    private final static String TAG = "ConnectionStatus";

    public static ConnectionStatus valueOf(int value) {
        switch (value) {
            case 0:
                return ConnectionStatus.SUCCESS;
            case 1:
                return ConnectionStatus.FAILED;
            default:
                Log.w(TAG, "[valueOf] unknown connection status value: " + value);
                return ConnectionStatus.UNKNOWN;
        }
    }
}
