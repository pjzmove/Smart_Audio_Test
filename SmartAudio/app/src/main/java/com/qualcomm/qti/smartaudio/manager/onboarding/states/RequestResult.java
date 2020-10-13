/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.states;

public enum RequestResult {
    SUCCESS,
    CREATE_REQUEST_FAILED,
    BUILDING_REQUEST_FAILED,
    SENDING_REQUEST_FAILED,
    HTTP_REQUEST_FAILED,
    READING_RESPONSE_FAILED,
    OVER_MAX_ATTEMPTS,
    REQUEST_TIMEOUT,
    UNKNOWN
}
