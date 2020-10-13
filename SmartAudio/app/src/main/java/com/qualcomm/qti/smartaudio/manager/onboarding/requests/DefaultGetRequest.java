/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.support.annotation.Nullable;

/**
 * <p>This class manages the configuration of a default request of type GET.</p>
 * <p>This class can be extended to add the parsing of a response by overriding {@link #onResponse(String) onResponse
 * (String) } and calling {@link #onComplete(Object) onComplete(Object)}.</p>
 */
public class DefaultGetRequest extends HttpRequest {

    private static final int TIME_OUT_MS = 5000;
    private static final long DELAY_MS = 1000;
    private static final int MAX_ATTEMPTS = 5; // 30s in total by default

    /**
     * <p>To build a new instance of a GET request with no parameters and no response to parse.</p>
     * <p>This initialise a request with a maximum number of attempts of {@link #MAX_ATTEMPTS MAX_ATTEMPTS}, a time
     * out per attempt of {@link #TIME_OUT_MS TIME_OUT_MS} and a delay between attempts of {@link #DELAY_MS DELAY_MS}
     * .</p>
     *
     * @param requestType
     *         the type to identify this request.
     * @param url
     *         The url to use to send the request.
     * @param listener
     *         the listener to be notified of success or errors.
     */
    DefaultGetRequest(RequestType requestType, String url, @Nullable RequestListener listener) {
        this(requestType, url, listener, MAX_ATTEMPTS);
    }

    /**
     * <p>To build a new instance of a GET request with no parameters and no response to parse.</p>
     * <p>This initialise a request with a maximum number of attempts of <code>maxAttempts</code>, a time
     * out per attempt of {@link #TIME_OUT_MS TIME_OUT_MS} and a delay between attempts of {@link #DELAY_MS DELAY_MS}
     * .</p>
     *
     * @param requestType
     *         the type to identify this request.
     * @param url
     *         The url to use to send the request.
     * @param listener
     *         the listener to be notified of success or errors.
     * @param maxAttempts
     *         The number of attempts this request should be tried before to drop it.
     */
    DefaultGetRequest(RequestType requestType, String url, @Nullable RequestListener listener, int maxAttempts) {
        super(requestType, url, Method.GET, listener, TIME_OUT_MS, DELAY_MS, maxAttempts);
    }

    @Override // HttpRequest
    public void onResponse(String response) {
        // default request, no analyse of the content is expected
        onComplete(response);
    }
}
