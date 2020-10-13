/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.models;

import android.util.Log;

/**
 * <p>All responses receives by the HTTP request
 * {@link com.qualcomm.qti.smartaudio.manager.onboarding.requests.VerifyConnectingRequest VerifyConnectingRequest}.</p>
 */
public enum ConnectingStatus {
    SUCCESS,
    CONNECTING,
    UNKNOWN;

    private final static String TAG = "ConnectingStatus";
    
    public static ConnectingStatus valueOf(int value) {
        switch (value) {
            case 0:
                return ConnectingStatus.SUCCESS;
            case 1:
                return ConnectingStatus.CONNECTING;
            default:
                Log.w(TAG, "[valueOf] unknown connecting status value: " + value);
                return ConnectingStatus.UNKNOWN;
        }
    }
}
