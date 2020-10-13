/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.support.annotation.Nullable;

abstract class DefaultPostRequest extends HttpRequest {

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8";

    private static final int TIME_OUT_MS = 60000; // DUT tries for 40s

    DefaultPostRequest(RequestType type, String url, String body, @Nullable RequestListener listener) {
        super(type, url, Method.POST, listener, TIME_OUT_MS);
        setBody(CONTENT_TYPE, body);
    }
}
