/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.onVoiceUiListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.AVSOnboardingErrorAttr.Error;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTVoiceUIClient;

import java.util.List;

public class AvsOnBoardingWebViewFragment extends BaseFragment implements onVoiceUiListener {

    private final static String TAG = "AvsOnBoardingWebViewFragment";

    private final static String EXTRA_KEYS_URL = "Avs_url";
    private final static String EXTRA_KEYS_CODE = "Avs_code";

    private IoTSysManager mIoTSysManager;
    private TextView mStatusTextView;
    private WebView mWebView;
    private String mCode;
    private String mUrl;


    public static AvsOnBoardingWebViewFragment newInstance(String url, String code) {
        AvsOnBoardingWebViewFragment fragment = new AvsOnBoardingWebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEYS_URL, url);
        bundle.putString(EXTRA_KEYS_CODE, code);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_avs_on_boarding_web_view, container, false);

        TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
        titleView.setText(R.string.avs_on_boarding_web_view_title);
        titleView.setContentDescription(getString(R.string.cont_desc_screen_avs_web_view));

        Bundle arg = getArguments();
        mUrl = arg.getString(EXTRA_KEYS_URL);
        mCode = arg.getString(EXTRA_KEYS_CODE);
        mWebView = view.findViewById(R.id.avs_web_view);
        Log.d(TAG, String.format("Url:%s, code:%s", mUrl, mCode));
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl(mUrl);
        mStatusTextView = view.findViewById(R.id.avs_status_code);

        mStatusTextView.setText(String.format("Code: %s", mCode));
        mStatusTextView.setOnClickListener(v -> mStatusTextView.setText(mCode));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            mIoTSysManager = IoTSysManager.getInstance();
            mIoTSysManager.addVoiceUiListener(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mIoTSysManager.removeVoiceUiListener(this);
    }

    private void updateStatus() {
        WebView web = mWebView.findViewById(R.id.avs_web_view);
        web.loadUrl(mUrl);
        mStatusTextView = mWebView.findViewById(R.id.avs_status_code);
        mStatusTextView.setText(mCode);
    }

    @Override
    public void voiceUIClientsDidChange(List<IoTVoiceUIClient> voiceUIClients) {

    }

    @Override
    public void voiceUIEnabledStateDidChange(boolean enabled) {
        Log.d(TAG, "voiceUIEnabledStateDidChange:" + enabled);
        if (enabled) {
            UiThreadExecutor.getInstance().execute(() -> mStatusTextView.setText(getString(R.string.avs_on_boarded)));
        }
    }

    @Override
    public void voiceUIDefaultClientDidChange(IoTVoiceUIClient voiceUIClient) {

    }

    @Override
    public void voiceUIDidProvideAVSAuthenticationCode(String code, String url) {
        mUrl = url;
        mCode = code;
        UiThreadExecutor.getInstance().execute(() -> updateStatus());
    }

    @Override
    public void voiceUIOnboardingDidErrorWithTimeout(Error error, int reattempt) {
        boolean isTimeout = error == Error.kTimedout;
        if (isTimeout) {
            String status = "OnBoarding time out:" + String.format("Retry %d", reattempt);
            UiThreadExecutor.getInstance().execute(() -> mStatusTextView.setText(status));
        }
    }


}
