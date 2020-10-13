/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;

public abstract class HttpRequest {

    private static final String TAG = "HttpRequest";

    private final Method mMethod;
    private final String mUrl;
    private String mBody;
    private String mContentType;
    private final Object mListenerLock = new Object();
    @Nullable @GuardedBy("mLock") private RequestListener mListener;
    private final RequestType mRequestType;
    private long mStartTime = INIT_START_TIME;
    private static final long INIT_START_TIME = -1;
    private int mAttempts = 0;
    private final int mMaxAttempts;
    private final int mTimeOutMs;
    private final long mDelayMs;

    public enum Method {
        GET,
        POST
    }

    HttpRequest(RequestType type, String url, Method method, @Nullable RequestListener listener,
                int timeOutMs) {
        this(type, url, method, listener, timeOutMs, 0, 1); // only one attempt => delay = 0
    }

    HttpRequest(RequestType type, String url, Method method, @Nullable RequestListener listener,
                int timeOutMs, long delayMs, int maxAttempts) {
        this.mRequestType = type;
        this.mUrl = url;
        this.mMethod = method;
        this.mListener = listener;
        this.mTimeOutMs = timeOutMs;
        this.mMaxAttempts = maxAttempts;
        this.mDelayMs = delayMs;
    }

    public void release() {
        synchronized (mListenerLock) {
            mListener = null;
        }
    }

    public void start() {
        mStartTime = System.currentTimeMillis();
        mAttempts = 0;
    }

    public void incrementAttempts() {
        mAttempts++;
    }

    public boolean nextAttempt() {
        // it is assumed that mStartTime and mAttempts have been initialised by the call to #start()

        long elapsedTime = getElapsedTime();

        if (mMaxAttempts <= mAttempts) {
            Log.w(TAG, String.format("[start] request %s has reached its maximum attempts: elapsedTime=%dms, max=%ds," +
                                             " current=%dms",
                                     mRequestType, elapsedTime, mMaxAttempts, mAttempts));
            if (mMaxAttempts == 1) {
                onError(RequestResult.REQUEST_TIMEOUT, elapsedTime);
            }
            else {
                onError(RequestResult.OVER_MAX_ATTEMPTS, mAttempts, elapsedTime);
            }
            return false;
        }

        return true;
    }

    public long getDelay() {
        return mDelayMs;
    }

    public int getTimeOut() {
        return mTimeOutMs;
    }

    public String getUrl() {
        return mUrl;
    }

    public Method getMethod() {
        return mMethod;
    }

    public RequestType getRequestType() {
        return mRequestType;
    }

    public boolean hasBody() {
        return  mBody != null && !mBody.isEmpty();
    }

    void setBody(String contentType, String requestBody) {
        mContentType = contentType;
        mBody = requestBody;
    }

    public String getBody() {
        return mBody;
    }

    public String getContentType() {
        return mContentType;
    }

    public abstract void onResponse(String response);

    public void onComplete(Object result) {
        RequestListener listener = getListener();
        if (listener == null) {
            return;
        }
        listener.onComplete(mRequestType, result);
    }

    public void onError(RequestResult requestResult, Object... args) {
        RequestListener listener = getListener();
        if (listener == null) {
            Log.i(TAG, "[onError] listener is null");
            return;
        }

        listener.onError(mRequestType, requestResult, args);
    }

    private long getElapsedTime() {
        return System.currentTimeMillis() - mStartTime;
    }

    private @Nullable RequestListener getListener() {
        RequestListener listener;
        synchronized (mListenerLock) {
            listener = mListener;
        }
        return listener;
    }

}
