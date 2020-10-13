/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.fragment.ChooseNewSpeakerFragment;
import com.qualcomm.qti.smartaudio.fragment.ChooseWifiFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupCompleteFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupFragment;
import com.qualcomm.qti.smartaudio.fragment.SetupListFragment;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;
import com.qualcomm.qti.smartaudio.util.FragmentController;

public class SetupActivity extends BaseActivity implements SetupFragment.SetupFragmentListener,
        SetupListFragment.SetupListFragmentListener {

    private static final String TAG = "SetupActivity";

    private static final String CHOOSE_NEW_SPEAKER_FRAGMENT = "CHOOSE_NEW_SPEAKER_FRAGMENT";
    private static final String CHOOSE_WIFI_FRAGMENT = "CHOOSE_WIFI_FRAGMENT";
    private static final String SETUP_COMPLETE_FRAGMENT = "SETUP_COMPLETE_FRAGMENT";

    private FragmentController mFragmentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mApp.isInit()) {
            setContentView(R.layout.activity_setup);
            mFragmentController = new FragmentController(getSupportFragmentManager(), R.id.setup_frame);
        }
        ((SmartAudioApplication) getApplicationContext()).loadOnBoarding();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String tag = null;
        if (mFragmentController != null) {
            tag = mFragmentController.getCurrentFragmentTag();
        }
        if (tag == null) {
            showChooseNewSpeakerFragment();
        }
    }

    @Override
    public void onBackPressed() {
        String tag = mFragmentController.getCurrentFragmentTag();
        switch (tag) {
            case CHOOSE_NEW_SPEAKER_FRAGMENT:
                finishThis(true);
                return;
            case CHOOSE_WIFI_FRAGMENT:
                mFragmentController.pop();
                return;
            case SETUP_COMPLETE_FRAGMENT:
                finishThis(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showChooseNewSpeakerFragment() {
        mFragmentController.startFragment(ChooseNewSpeakerFragment.newInstance(CHOOSE_NEW_SPEAKER_FRAGMENT),
                                          CHOOSE_NEW_SPEAKER_FRAGMENT, true);
    }

    private void showChooseWifiFragment(String ssid) {
        ChooseWifiFragment fragment = ChooseWifiFragment.newInstance(CHOOSE_WIFI_FRAGMENT, ssid);
        mFragmentController.push(fragment, CHOOSE_WIFI_FRAGMENT);
    }

    private void showSetupCompleteFragment() {
        mFragmentController.startFragment(
                SetupCompleteFragment.newInstance(SETUP_COMPLETE_FRAGMENT), SETUP_COMPLETE_FRAGMENT, true);
    }

    @Override
    public void onConnectivityChanged(boolean connected) {
        // progress dialog for connecting to new speaker shows no wifi dialog when handset connects to new speaker,
        // to avoid that we need to override this function
    }

    @Override
    public void onTopButtonClicked(String tag) {
        // top button never used here.
    }

    @Override
    public void onMiddleButtonClicked(String tag) {
        switch (tag) {
            case SETUP_COMPLETE_FRAGMENT:
                // workaround to re-start the activity
                Intent intent = new Intent(this, SetupActivity.class);
                startActivity(intent);
                finishThis(false);
                break;
        }
    }

    @Override
    public void onBottomButtonClicked(String tag) {
        switch (tag) {
            case SETUP_COMPLETE_FRAGMENT:
                // exit setup (from SetupComplete), next (from Incompatible speaker) pressed
                finishThis(true);
                break;
        }
    }

    @Override
    public void onItemClicked(String tag, Object object) {
        switch (tag) {
            case CHOOSE_NEW_SPEAKER_FRAGMENT:
                // new speaker is selected to onboard
                if (object instanceof String) {
                    String ssid = (String) object;
                    showChooseWifiFragment(ssid);
                }
                return;

            case CHOOSE_WIFI_FRAGMENT:
                showSetupCompleteFragment();
                break;
        }
    }

    /**
     * <p>When this activity should finish it also has to unload the OnBoardingManager if it won't be of any use
     * anymore.</p>
     * <p>While onboarding is loaded, the iotsys and allplay systems are disabled.</p>
     *
     * @param unload
     *          True to also unload the onboarding manager, false to keep it.
     */
    private void finishThis(boolean unload) {
        if (unload) {
            ((SmartAudioApplication) getApplicationContext()).unloadOnBoarding();
        }

        finish();
    }

}
