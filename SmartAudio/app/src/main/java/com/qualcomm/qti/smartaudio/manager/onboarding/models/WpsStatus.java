/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.models;

/**
 * <p>All responses receives by the HTTP request
 * {@link com.qualcomm.qti.smartaudio.manager.onboarding.requests.WpsRequest WpsRequest}.</p>
 */
public enum WpsStatus {
    SUCCESS,
    UNKNOWN;
    
    public static WpsStatus valueOf(int value) {
        return (value == 0) ? SUCCESS : UNKNOWN;
    }
}
