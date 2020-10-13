/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.models.WpsStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;

public class WpsRequest extends DefaultPostRequest {

    private static final String TAG = "WpsRequest";
    private static final String PARAMETERS_FORMAT = "ssid=%s";


    WpsRequest(String url, String ssid, RequestListener listener) {
        super(RequestType.WPS, url, buildRequestBody(ssid), listener);
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

        onComplete(WpsStatus.valueOf(value));
    }

    private static String buildRequestBody(String ssid) {
        ssid = ssid == null ? "" : ssid;
        return String.format(PARAMETERS_FORMAT, ssid);
    }

}
