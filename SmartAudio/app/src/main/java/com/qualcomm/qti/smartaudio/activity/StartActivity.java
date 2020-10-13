/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.smartaudio.BuildConfig;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.service.ApplicationService;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.ref.WeakReference;
import java.util.List;

public class StartActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "StartActivity";
    private static final String DIALOG_TERMS_OF_SERVICE_TAG = "DIALOG_TERMS_OF_SERVICE_TAG";

    // Boolean check for terms acceptance.
    private boolean mEULAAccepted = false;

    private LinearLayout mStartLayout = null;
    private TextView mAppVersionView;

    private SearchForSpeakerAsyncTask mSearchForSpeakerAsyncTask = null;
    private ConvertSavedPlaylistsAsyncTask mConvertSavedPlaylistsAsyncTask = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);

        mStartLayout = (LinearLayout) findViewById(R.id.start_layout);
        mAppVersionView = findViewById(R.id.launch_screen_version);

        mAppVersionView.setText(BuildConfig.VERSION_NAME);

        mAppVersionView.setContentDescription(getString(R.string.cont_desc_app_version));

        // Check if users have accepted terms
        checkToS();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mEULAAccepted) {
            checkForPermissions();
        }
    }

    private void checkToS() {
        SharedPreferences preferences = getSharedPreferences(ApplicationService.PREFERENCE_SETTINGS, MODE_PRIVATE);
        mEULAAccepted = preferences.getBoolean(ApplicationService.PREFERENCE_EULA_ACCEPTED, false);
        if (!mEULAAccepted) {
            setupToS();
        }
        else {
            setup();
        }
    }

    private void setupToS() {
        mStartLayout.setVisibility(View.VISIBLE);

        Button acceptButton = findViewById(R.id.tos_accept_terms_button);
        acceptButton.setOnClickListener(this);

        Button viewTermsButton = findViewById(R.id.tos_view_terms_button);
        viewTermsButton.setOnClickListener(this);
    }

    private void setup() {
        // We set this to invisible because we need the layout for the top view of logo, but we don't need to show
        // the tos
        mStartLayout.setVisibility(View.INVISIBLE);
    }

    private void loadApplication() {
        ApplicationReceiver receiver = new ApplicationReceiver(this);
        ApplicationService.startActionInit(this, receiver);
    }

    private void checkForPermissions() {
        if (!hasAskedForStoragePermission()) {
            return;
        }
        if (!hasAskedForAccessCoarseLocationPermission()) {
            return;
        }
        loadApplication();
    }

    private void searchForSpeaker() {
        if (mSearchForSpeakerAsyncTask == null) {
            mSearchForSpeakerAsyncTask = new SearchForSpeakerAsyncTask();
            // Add to the AsyncTaskQueue
            addTaskToQueue(mSearchForSpeakerAsyncTask);
        }
    }

    private void stopSearchForSpeaker() {
        if (mSearchForSpeakerAsyncTask != null) {
            mSearchForSpeakerAsyncTask.cancel(true);
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tos_accept_terms_button:
                // Terms accepted.
                mEULAAccepted = true;

                // Save that users have accepted
                SharedPreferences preferences = getSharedPreferences(ApplicationService.PREFERENCE_SETTINGS,
                                                                     MODE_PRIVATE);
                preferences.edit().putBoolean(ApplicationService.PREFERENCE_EULA_ACCEPTED, mEULAAccepted).commit();

                // Go to setup view
                setup();
                checkForPermissions();
                break;
            case R.id.tos_view_terms_button:
                showTermOfService();
                break;
        }
    }

    @Override
    public void onConnectivityChanged(boolean connected) {
        // BaseActivity callback will show no wifi dialog if needed
        super.onConnectivityChanged(connected);
        if (connected) {
            searchForSpeaker();
        }
        else {
            // We are not connected, stop the search for speaker task
            stopSearchForSpeaker();
        }
    }

    @SuppressLint("ParcelCreator")
    private class ApplicationReceiver extends ResultReceiver {

        private final WeakReference<BaseActivity> mActivityRef;

        public ApplicationReceiver(final BaseActivity activity) {
            super(new Handler());
            mActivityRef = new WeakReference<>(activity);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            BaseActivity baseActivity = mActivityRef.get();
            if (!Utils.isActivityActive(baseActivity)) {
                // The result is moot at this point
                return;
            }

            if (resultCode == ApplicationService.STATUS_FINISHED) {
                if (mApp.isInit()) {
                    // Add itself to connectivity listener to get a connectivity callback
                    if (mApp.getConnectivityReceiver() != null) {
                        mApp.getConnectivityReceiver().addConnectivityChangedListener(StartActivity.this);
                    }
                }
            }
        }
    }

    private class ConvertSavedPlaylistsAsyncTask extends RequestAsyncTask {

        public ConvertSavedPlaylistsAsyncTask() {
            super(getString(R.string.converting_saved_playlists), null, StartActivity.this, null);
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    if (mApp.getConnectivityReceiver().isConnected()) {
                        searchForSpeaker();
                    }
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void clean() {
            mConvertSavedPlaylistsAsyncTask = null;
        }
    }

    private class SearchForSpeakerAsyncTask extends RequestAsyncTask implements
            OnGroupListChangedListener {

        // Additional wait time
        private int SEARCH_FOR_SPEAKER_TIME = 1000;

        public SearchForSpeakerAsyncTask() {
            super(getString(R.string.searching_for_speakers), null, StartActivity.this, null);
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    // Start the MainActivity
                    startMainActivity();
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Initial wait time, allow the dialog to be shown at least.
            doWait(DEFAULT_WAIT_TIME);
            boolean isEmpty;

            List<IoTGroup> zones = mApp.getAllPlayManager().getGroups();
            isEmpty = zones.isEmpty();
            if (isEmpty) {
                // If still no speakers, we add the zone listener and wait again
                mApp.getAllPlayManager().addOnZoneListChangedListener(this);
                doWait(SEARCH_FOR_SPEAKER_TIME);
            }

            return null;
        }

        @Override
        protected void clean() {
            // Clean up the variable and remove listener
            mSearchForSpeakerAsyncTask = null;
            mApp.getAllPlayManager().removeOnZoneListChangedListener(this);
        }

        @Override
        public void onZoneListChanged() {
            boolean isEmpty;
            List<IoTGroup> zones = mApp.getAllPlayManager().getGroups();
            isEmpty = zones.isEmpty();
            if (!isEmpty) {
                // We got a speaker, lets stop this task
                interrupt();
            }
        }
    }

    private void showTermOfService() {
        // build spannable content
        final SpannableString content = new SpannableString(getText(R.string.term_of_service_content));
        Linkify.addLinks(content, Linkify.WEB_URLS);

        // create dialog
        CustomDialogFragment tosDialog = CustomDialogFragment.newSpannableStringDialog(DIALOG_TERMS_OF_SERVICE_TAG,
                                                                                       getString(R.string.term_of_service_title), content,
                                                                                       getString(R.string.ok), null);

        // get notified once view is created to add specific behaviour
        tosDialog.setCreatedViewListener((CustomDialogFragment.CreateViewListener) dialog -> {
            //enable links
            TextView textView = dialog.findViewById(R.id.custom_dialog_message_text);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        });

        // show the dialog
        showDialog(tosDialog, DIALOG_TERMS_OF_SERVICE_TAG);
    }

}
