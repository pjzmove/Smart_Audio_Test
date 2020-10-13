/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;

public interface RequestListener {
    void onError(RequestType type, RequestResult result, Object[] args);
    void onComplete(RequestType type, Object response);
}
