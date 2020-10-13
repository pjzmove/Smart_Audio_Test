/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.support.annotation.Nullable;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.models.ConnectingStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;

/**
 * <
 */
public class VerifyConnectingRequest extends DefaultGetRequest {

    private static final String TAG = "VerifyConnectingRequest";
    private static final int MAX_ATTEMPTS = 10; // this will be attempted 10 times over a period of 60s
    /**
     * An identifier for this request.
     */
    private static final RequestType REQUEST_TYPE = RequestType.VERIFY_CONNECTING;

    VerifyConnectingRequest(String url, @Nullable RequestListener listener) {
        super(REQUEST_TYPE, url, listener, MAX_ATTEMPTS);
    }

    @Override
    public void onResponse(String response) {
        Log.d(TAG, "[VERIFY_CONNECTING - onResponse] response=" + response);
        int value;
        try {
            value = Integer.valueOf(response);
        }
        catch (Exception e) {
            Log.w(TAG, String.format("[onResponse] Exception occurs for response \"%s\": %s",
                                     response, e.getMessage()));
            onError(RequestResult.READING_RESPONSE_FAILED);
            return;
        }

        onComplete(ConnectingStatus.valueOf(value));
    }

}
