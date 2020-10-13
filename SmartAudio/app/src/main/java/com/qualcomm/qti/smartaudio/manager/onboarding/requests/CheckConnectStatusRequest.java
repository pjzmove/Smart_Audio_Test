/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.support.annotation.Nullable;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.models.ConnectionStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;

public class CheckConnectStatusRequest extends DefaultGetRequest {

    private static final String TAG = "CheckConnectStatusRequest";

    private static final RequestType REQUEST_TYPE = RequestType.CHECK_CONNECT_STATUS;

    CheckConnectStatusRequest(String url, @Nullable RequestListener listener) {
        super(REQUEST_TYPE, url, listener);
    }

    @Override
    public void onResponse(String response) {
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

        onComplete(ConnectionStatus.valueOf(value));
    }

}
