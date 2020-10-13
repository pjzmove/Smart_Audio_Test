/*
 *  ************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                     *
 *  * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 *  ************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.IntDef;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.BluetoothSettingsFragment;
import com.qualcomm.qti.smartaudio.fragment.DeviceSettingsFragment;
import com.qualcomm.qti.smartaudio.fragment.SettingsFragment;
import com.qualcomm.qti.smartaudio.fragment.ZigbeeOnboardingFragment;
import com.qualcomm.qti.smartaudio.service.ApplicationService;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseActivity implements SettingsFragment.OnSettingsListener, View.OnClickListener {

    private static final String TAG = "SettingsActivity";

    @IntDef(value = { SettingsRoutes.BLUETOOTH_SETTINGS, SettingsRoutes.ZIGBEE_SETTINGS,
            SettingsRoutes.DEVICE_SETTINGS})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SettingsRoutes{
        int BLUETOOTH_SETTINGS = 500;
        int ZIGBEE_SETTINGS = 501;
        int DEVICE_SETTINGS = 502;
    }

    private String mSelectedPlayerId;
    private String mSelectedPlayerHost;
    private final Handler mHandler = new Handler();
    private ResetAppTask mResetTask;

    private String mId;
    private String mHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupSettingsBar();

        int route = getIntent().getIntExtra(MainActivity.ExtraKeys.ROUTE_EXTRA, SettingsRoutes.DEVICE_SETTINGS);
        mId = getIntent().getStringExtra(MainActivity.ExtraKeys.ID_EXTRA);
        mHost = getIntent().getStringExtra(MainActivity.ExtraKeys.HOST_EXTRA);

        if (mId != null && mHost != null) {
            handleRoute(route);
        }
    }

    private void handleRoute(@SettingsRoutes int route) {
        switch (route) {
            case SettingsRoutes.BLUETOOTH_SETTINGS:
                navigateToBluetoothSettings();
                break;
            case SettingsRoutes.ZIGBEE_SETTINGS:
                navigateToZigbeeSettings();
                break;
            case SettingsRoutes.DEVICE_SETTINGS:
                navigateToDeviceSettings();
                break;
        }
    }

    private void navigateToBluetoothSettings() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_frame, BluetoothSettingsFragment.newInstance(mHost))
                .commitNow();
    }

    private void navigateToZigbeeSettings() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_frame, ZigbeeOnboardingFragment.newInstance(mId, mHost)).commitNow();
    }

    private void navigateToDeviceSettings() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings_fragment_frame, DeviceSettingsFragment.newInstance(mId, mHost))
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                                     R.anim.slide_in_left, R.anim.slide_out_right)
                .commitNow() ;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isTablet()) {
            updateDeviceDetails();
        }
    }

    private void updateDeviceDetails() {
        final List<IoTPlayer> players = mAllPlayManager.getPlayers();
        if ((players == null) || (players.size() == 0)) {
            mSelectedPlayerId = null;
            mSelectedPlayerHost = null;
        } else {
            List<String> playerIds = new ArrayList<String>();
            List<String> playerHost = new ArrayList<String>();
            for (IoTPlayer player : players) {
                playerIds.add(player.getPlayerId());
                playerHost.add(player.getHostName());
            }
            if ((mSelectedPlayerId == null) || ((mSelectedPlayerId != null) && (!playerIds.contains(mSelectedPlayerId)))) {
                mSelectedPlayerId = players.get(0).getPlayerId();
                mSelectedPlayerHost = players.get(0).getHostName();
            }
        }
        onSpeakerSelected(mSelectedPlayerId, mSelectedPlayerHost);
    }

    protected void setupSettingsBar() {
        TextView actionBarTitleText = findViewById(R.id.settings_app_bar_text_view);

        if (actionBarTitleText != null) {
            actionBarTitleText.setText(getString(R.string.speaker_settings));
        }

        ImageButton actionBarCloseButton = findViewById(R.id.settings_app_bar_close_button);
        if (actionBarCloseButton != null) {
            actionBarCloseButton.setOnClickListener(this);
            actionBarCloseButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSpeakerSelected(String id, String host) {
//        if (isTablet()) {
//            mSelectedPlayerId = id;
//            DeviceSettingsFragment deviceSettingsFragment = (DeviceSettingsFragment) getSupportFragmentManager().findFragmentById(R.id.device_settings_fragment);
//            deviceSettingsFragment.updateId(id);
//        } else {
            Intent deviceSettingsIntent = new Intent(this, DeviceSettingsActivity.class);
            deviceSettingsIntent.putExtra(DeviceSettingsActivity.EXTRA_ID, id);
            deviceSettingsIntent.putExtra(DeviceSettingsActivity.EXTRA_HOST, host);
            startActivity(deviceSettingsIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        }
    }

    @Override
    public void onSettingsItemClicked(SettingsFragment.SettingsItemType itemType) {
        switch (itemType) {
            case UPDATE:
                startActivity(new Intent(this, FirmwareUpdateActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case GENERAL:
                ApplicationReceiver sender = new ApplicationReceiver(mHandler);
                Intent intent = new Intent(Intent.ACTION_SYNC, null, SettingsActivity.this, ApplicationService.class);
                intent.putExtra(ApplicationService.EXTRA_RECEIVER, sender);
                intent.putExtra(ApplicationService.EXTRA_COMMAND, ApplicationService.COMMAND_RESET);
                startService(intent);
                break;
        }
    }

    private class ApplicationReceiver extends ResultReceiver {
        public ApplicationReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            int command = resultData.getInt(ApplicationService.EXTRA_COMMAND, 0);
            if (!isFinishing() && !mIsSaveStateCalled) {
                switch (resultCode) {
                    case ApplicationService.STATUS_STARTED:
                        break;
                    case ApplicationService.STATUS_RUNNING:
                        break;
                    case ApplicationService.STATUS_FINISHED:
                        int retCode = resultData.getInt(ApplicationService.EXTRA_RETCODE);
                        switch (retCode) {
                            case 0:
                                switch (command) {
                                    case ApplicationService.COMMAND_RESET:
                                        break;
                                }
                                break;
                            case ApplicationService.RETURN_FINALIZE_RESET:
                                reboot();
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
        }
    }

    public void reboot() {
        if (mResetTask == null) {
            mResetTask = new ResetAppTask(getString(R.string.settings_reset_dialog));
            addTaskToQueue(mResetTask);
        }
    }

    private class ResetAppTask extends RequestAsyncTask {

        public ResetAppTask(String progressTitle) {
            super(progressTitle, null, SettingsActivity.this, null);
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            mApp.setResetApp(true);

            setResult(RESULT_RESET);
            finish();
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mResetTask = null;
        }
    }

    @Override
    public void onZoneChanged() {
        if (isTablet()) {
            updateDeviceDetails();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_app_bar_close_button:
                finish();
                break;
        }
    }
}
