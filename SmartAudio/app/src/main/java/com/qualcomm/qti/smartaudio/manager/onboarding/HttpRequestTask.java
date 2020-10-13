/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.GuardedBy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qualcomm.qti.smartaudio.DEBUG;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestType;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.HttpRequest;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class HttpRequestTask extends AsyncTask<Void, Void, RequestResult> {

    private static final String TAG = "HttpRequestTask";
    private static final boolean LOG_ALL = DEBUG.OnBoarding.HTTP;
    @SuppressWarnings("InjectedReferences") private static final String UTF_8 = "UTF_8";
    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String ACCEPT_CHARSET_KEY = "Accept-Charset";
    private static final int CONNECT_TIMEOUT_IN_MS = 10000;
    private final HttpRequest mRequest;
    private String mResponseData;
    private int mResponseCode = -1;
    private final Object mListenerLock = new Object();
    @Nullable @GuardedBy("mListenerLock") private RequestTaskListener mListener;

    HttpRequestTask(@Nullable RequestTaskListener listener, HttpRequest request) {
        mRequest = request;
        mResponseData = "";
        mListener = listener;
    }

    @Override
    protected RequestResult doInBackground(Void... voids) {
        // the request is attempted
        mRequest.incrementAttempts();

        // 1. getting the HTTP connection
        HttpURLConnection connection = openHttpConnection(mRequest.getUrl());

        if (connection == null) {
            return RequestResult.BUILDING_REQUEST_FAILED;
        }

        // 2. preparing the HTTP connection
        RequestResult preparationResult = prepareHttpConnection(connection, mRequest);
        if (!RequestResult.SUCCESS.equals(preparationResult)) {
            connection.disconnect();
            return preparationResult;
        }

        // 3. executing the request
        return sendHttpRequest(connection);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        log(mRequest.getRequestType(), "onCancelled");
        release();
    }

    @Override
    protected void onCancelled(RequestResult requestResult) {
        super.onCancelled(requestResult);
        log(mRequest.getRequestType(), String.format("onCancelled, HttpResult=%s", requestResult));
        release();
    }

    private RequestTaskListener release() {
        mRequest.release();
        RequestTaskListener listener;
        synchronized (mListenerLock) {
            listener = mListener;
            mListener = null;
        }
        return listener;
    }

    private void onComplete() {
        mRequest.release();
        RequestTaskListener listener = release();
        if (listener != null) {
            listener.onComplete(this);
        }
    }

    @Override
    protected void onPostExecute(RequestResult requestResult) {
        // called in UI thread
        super.onPostExecute(requestResult);

        switch (requestResult) {
            case SUCCESS:
                mRequest.onResponse(mResponseData);
                break;

            case HTTP_REQUEST_FAILED:
                mRequest.onError(RequestResult.HTTP_REQUEST_FAILED, mResponseCode);
                break;

            case REQUEST_TIMEOUT:
            case SENDING_REQUEST_FAILED:
                // the request is attempted again until its number of maximum attempts is reached
                onTimeOut();
                return;

            case OVER_MAX_ATTEMPTS:
            case CREATE_REQUEST_FAILED:
                // those are unexpected: raised when trying to initiate a request not while executing it
                Log.i(TAG, "[onPostExecute] unexpected error: " + requestResult);
            case BUILDING_REQUEST_FAILED:
            case READING_RESPONSE_FAILED:
            default:
                mRequest.onError(requestResult);
        }

        onComplete();
    }

    private void onTimeOut() {
        RequestTaskListener listener = release();
        if (listener != null) {
            listener.onTimeOut(this, mRequest);
        }
    }

    private RequestResult sendHttpRequest(@NonNull HttpURLConnection connection) {
        log(mRequest.getRequestType(), String.format("sending HTTP request %s", connection.getURL()));
        long startTime = System.currentTimeMillis();

        // 1. executing the request
        try {
            mResponseCode = connection.getResponseCode();
        }
        catch (SocketTimeoutException exception) {
            Log.w(TAG, String.format("[sendHttpRequest] The request has timed out either during connection (%dms) or " +
                    "read (%dms)", connection.getConnectTimeout(), connection.getReadTimeout()));
            return onRequestComplete(connection, RequestResult.REQUEST_TIMEOUT, startTime);
        }
        catch (IOException exception) {
            Log.w(TAG, "[sendHttpRequest] Sending the request failed with an exception.");
            exception.printStackTrace();
            return onRequestComplete(connection, RequestResult.SENDING_REQUEST_FAILED, startTime);
        }

        // 2. analysing the response
        if (mResponseCode != HttpURLConnection.HTTP_OK) {
            return onRequestComplete(connection, RequestResult.HTTP_REQUEST_FAILED, startTime);
        }

        // 3. get the data from the response
        try {
            mResponseData = getResponseData(connection);
        }
        catch (Exception exception) {
            Log.w(TAG, "[sendHttpRequest] Failed to read the response due to the following exception ");
            exception.printStackTrace();
            return onRequestComplete(connection, RequestResult.READING_RESPONSE_FAILED, startTime);
        }

        // 4. request done, logging and disconnecting
        return onRequestComplete(connection, RequestResult.SUCCESS, startTime);
    }

    @SuppressLint("DefaultLocale") // triggered for the log, not an issue
    private RequestResult onRequestComplete(@NonNull HttpURLConnection connection,
                                            RequestResult result, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        log(mRequest.getRequestType(), String.format("sending HTTP request: time=%dms, HttpResult=%s", elapsedTime,
                                                     result));
        connection.disconnect();
        return result;
    }

    private void log(RequestType requestType, String message) {
        log(String.format("[HTTP REQUEST] %s - %s", requestType, message));
    }

    private void log(String message) {
        if (LOG_ALL) {
            Log.d(TAG, message);
        }
    }

    private static HttpURLConnection openHttpConnection(String requestUrl) {
        if (requestUrl == null || requestUrl.isEmpty()) {
            Log.w(TAG, "[prepareHttpConnection] URL is null or empty");
            return null;
        }

        HttpURLConnection httpConnection;

        try {
            URL url = new URL(requestUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
        }
        catch (MalformedURLException exception) {
            Log.w(TAG,
                  String.format("[prepareHttpConnection] Exception occurred when building URL \"%s\"", requestUrl));
            exception.printStackTrace();
            return null;
        }
        catch (IOException exception) {
            Log.w(TAG,
                  String.format("[prepareHttpConnection] Exception occurred when opening connection for URL \"%s\"",
                                requestUrl));
            exception.printStackTrace();
            return null;
        }
        catch (Exception exception) {
            Log.w(TAG,
                  String.format("[prepareHttpConnection] Exception occurred for URL \"%s\"", requestUrl));
            exception.printStackTrace();
            return null;
        }

        return httpConnection;
    }

    private static RequestResult prepareHttpConnection(@NonNull HttpURLConnection connection, HttpRequest request) {
        connection.setInstanceFollowRedirects(HttpURLConnection.getFollowRedirects());
        connection.setRequestProperty(ACCEPT_CHARSET_KEY, UTF_8);

        try {
            connection.setRequestMethod(request.getMethod().toString());
        }
        catch (ProtocolException e) {
            Log.w(TAG, String.format("[prepareHttpConnection] request %1$s: Exception occurs when setting method " +
                                             "\"%3$s\" for url \"%2$s\"",
                                     request.getRequestType(), connection.getURL(), request.getMethod()));
            e.printStackTrace();
            return RequestResult.BUILDING_REQUEST_FAILED;
        }

        connection.setConnectTimeout(CONNECT_TIMEOUT_IN_MS);
        connection.setReadTimeout(request.getTimeOut());
        connection.setUseCaches(false);
        connection.setDoInput(true);

        return addBody(connection, request);
    }

    private static RequestResult addBody(HttpURLConnection connection, HttpRequest request) {
        if (!request.hasBody()) {
            // body is empty, nothing to add.
            return RequestResult.SUCCESS;
        }

        connection.setDoOutput(true);
        connection.setRequestProperty(CONTENT_TYPE_KEY, request.getContentType());

        OutputStream outputStream = null;
        String body = request.getBody();

        try {
            outputStream = connection.getOutputStream();
            outputStream.write(body.getBytes(UTF_8));
        }
        catch (Exception e) {
            Log.w(TAG, String.format("[addBody] request %s: Exception occurs when writing body for url \"%s\" with " +
                                             "body \"%s\"",
                                     request.getRequestType(), connection.getURL(), body));
            e.printStackTrace();
            return RequestResult.BUILDING_REQUEST_FAILED;
        }
        finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                }
                catch (Exception e) {
                    Log.w(TAG, String.format("[addBody] request %s: Exception occurs when closing stream for url " +
                                                     "\"%s\" with body \"%s\"",
                                             request.getRequestType(), connection.getURL(), body));
                    e.printStackTrace();
                }
            }
        }

        return RequestResult.SUCCESS;
    }

    private static String getResponseData(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String value;
        StringBuilder builder = new StringBuilder();
        while ((value = bufferedReader.readLine()) != null) {
            builder.append(value);
        }

        return builder.toString();
    }

    public interface RequestTaskListener {
        void onComplete(HttpRequestTask task);
        void onTimeOut(HttpRequestTask task, HttpRequest request);
    }
}
