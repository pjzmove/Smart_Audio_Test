/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestType;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.HttpRequest;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestFactory;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OnBoardingHttpClient {

    private static final String TAG = "OnBoardingHttpClient";
    private final List<AsyncTask> mTasks = new CopyOnWriteArrayList<>();
    private final Handler mHandler = new Handler();
    private final HttpRequestTask.RequestTaskListener mRequestTaskListener = new HttpRequestTask.RequestTaskListener() {
        @Override
        public void onComplete(HttpRequestTask task) {
            removeTask(task);
        }

        @Override
        public void onTimeOut(HttpRequestTask task, HttpRequest request) {
            removeTask(task);
            processTimeOut(request);
        }
    };

    private void processTimeOut(HttpRequest request) {
        if (request == null) {
            return;
        }

        if (!request.nextAttempt()) {
            // the request has reached its maximum attempts, error sent by the request to its listener.
            request.release();
            return;
        }

        // posting for current task to end before to start next request
        mHandler.postDelayed(() -> executeRequest(request), request.getDelay());

    }

    private void removeTask(HttpRequestTask task) {
        if (task != null) {
            mTasks.remove(task);
        }
    }

    void release() {
        reset();
    }

    public void reset() {
        for (AsyncTask task : mTasks) {
            if (task != null) {
                task.cancel(true);
            }
        }
        mTasks.clear();
    }

    public boolean request(RequestType requestType, String gateway, RequestListener listener, Object... args) {
        String url = getUrl(gateway, requestType);
        HttpRequest request = RequestFactory.buildRequest(requestType, url, listener, args);

        if (request == null) {
            Log.w(TAG, String.format("[request] Build request failed: request=%s, address=%s", requestType.toString(),
                                     gateway));
            return false;
        }

        request.start();
        executeRequest(request);

        return true;
    }

    private void executeRequest(@NonNull HttpRequest request) {
        HttpRequestTask newTask = new HttpRequestTask(mRequestTaskListener, request);
        newTask.execute();
        mTasks.add(newTask);
    }

    private String getUrl(String gateway, RequestType type) {
        return "http://" + gateway + "/config/" + type.getCommand();
    }

}
