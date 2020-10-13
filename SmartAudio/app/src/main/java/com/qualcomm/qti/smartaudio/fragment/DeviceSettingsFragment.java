/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.onSystemListener;
import com.qualcomm.qti.smartaudio.util.NameSpeakerAsyncTask;
import com.qualcomm.qti.smartaudio.util.RequestAsyncTask;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.smartaudio.util.Utils;

import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import com.qualcomm.qti.iotcontrollersdk.constants.ConnectionState;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.constants.NetworkInterface;
import com.qualcomm.qti.iotcontrollersdk.constants.UpdateStatus;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.UserPassword;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BatteryStatusAttr;

import java.util.ArrayList;
import java.util.List;

import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDevicePasswordRequestedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnDeviceStateChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnHomeTheaterChannelChangedListener;
import com.qualcomm.qti.smartaudio.util.NameSpeakerAsyncTask.DeviceNameRequestListener;

public class DeviceSettingsFragment extends BaseFragment implements ExpandableListView.OnChildClickListener,
        OnDeviceListChangedListener, OnDeviceStateChangedListener,
        OnGroupListChangedListener, OnGroupInfoStateChangedListener, OnDevicePasswordRequestedListener,
        onSystemListener,
        DeviceNameRequestListener {

    private static final String TAG = "DeviceSettingsFragment";

    private static final String EXTRA_ID = "DEVICE_ID";
    private static final String EXTRA_HOST = "HOST_NAME";

    private static final String DIALOG_SPEAKER_NAME_TAG = "DialogSpeakerNameTag";
    private static final String RESTART_TAG = "RestartTag";
    private static final String RESTART_DEVICE_ERROR_TAG = "RestartDeviceErrorTag";
    private static final String FACTORY_RESET_TAG = "FactoryResetTag";
    private static final String FACTORY_RESET_DEVICE_ERROR_TAG = "FactoryResetDeviceErrorTag";
    private static final String DIALOG_SET_PASSWORD_TAG = "DialogSetPasswordTag";
    private static final String PASSWORD_ERROR_TAG = "PasswordErrorTag";
    private static final String DIALOG_PASSWORD_TAG = "DialogPasswordTag";
    private static final String DEVICE_INFO_ERROR_TAG = "DeviceInfoErrorTag";
    private static final String FIRMWARE_UPDATE_TAG = "FirmwareUpdateTag";
    private static final String NO_FIRMWARE_UPDATE_TAG = "FirmwareUpdateTag";
    private static final String SURROUNDS_UPDATE_TAG = "SurroundsUpdateTag";
    private static final String LEFT_SURROUND_UPDATE_TAG = "LeftSurroundUpdateTag";
    private static final String RIGHT_SURROUND_UPDATE_TAG = "RightSurroundUpdateTag";
    private boolean IS_SET_PASSWORD_CANCELLED = false;
    private static final int WAIT_TIME = 60000;

    private DeviceSettingsAdapter mAdapter;
    private String mID;
    private String mHost;
    private TextView mTitleTextView;
    private RestartDeviceTask mRestartDeviceTask;
    private FactoryResetDeviceTask mFactoryResetDeviceTask;
    private PasswordTask mPasswordTask;
    private final Object mPasswordObject = new Object();
    private GetDeviceInfoTask mGetDeviceInfoTask;
    private NameSpeakerAsyncTask mNameSpeakerAsyncTask;
    private FirmwareUpdateTask mFirmwareUpdateTask = null;
    private CheckFirmwareTask mCheckFirmwareTask = null;
    private SurroundFirmwareUpdateTask mSurroundFirmwareUpdateTask = null;
    private CustomDialogFragment mSurroundsUpdateDialogFragment = null;

    @Override
    public void onRequestResult(boolean result) {
        mNameSpeakerAsyncTask = null;
    }

    public enum DeviceSettingsGroupType {
        DEVICE_SETTINGS,
        BLUETOOTH,
        NETWORK,
        FIRMWARE,
        RESTRICT_PLAYBACK,
        RESTART_RESET
    }

    public enum DeviceSettingsItemType {
        DEVICE_NAME,
        VOICE_CONTROL,
        AUDIO_SETTING,
        ZIGBEE,
        BLUETOOTH_DEVICES,
        BATTERY,
        NETWORK,
        SIGNAL_STRENGTH,
        IP_ADDRESS,
        MAC_ADDRESS,
        FIRMWARE_VERSION,
        UPDATE_AUTOMATICALLY,
        CHECK_UPDATES,
        PASSWORD_PROTECTION,
        RESTART,
        ADJUST_AUDIO,
        FACTORY_RESET
    }

    public static DeviceSettingsFragment newInstance(String id, String host) {
        DeviceSettingsFragment fragment = new DeviceSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ID, id);
        bundle.putString(EXTRA_HOST, host);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Bundle arg = getArguments();
        mID = arg.getString(EXTRA_ID);
        mHost = arg.getString(EXTRA_HOST);
        mHost = ControllerSdkUtils.stripHostName(mHost);

        mTitleTextView = view.findViewById(R.id.settings_app_bar_text_view);
        mTitleTextView.setContentDescription(getString(R.string.cont_desc_screen_device_settings));

        ExpandableListView expandableListView = view.findViewById(R.id.settings_activity_expand_listview);

        mAdapter = new DeviceSettingsAdapter();
        expandableListView.setAdapter(mAdapter);

        expandableListView.setOnChildClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((mApp != null) && mApp.isInit()) {
            mAllPlayManager.addOnDeviceListChangedListener(this);
            mAllPlayManager.addOnDeviceStateChangedListener(this);
            mAllPlayManager.addOnZoneListChangedListener(this);
            mAllPlayManager.addOnZoneStateChangedListener(this);
            mAllPlayManager.addOnDevicePasswordRequestedListener(this);
            try {
                IoTSysManager.getInstance().addSystemListener(this);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            updateState();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAllPlayManager != null) {
            mAllPlayManager.removeOnDeviceListChangedListener(this);
            mAllPlayManager.removeOnDeviceStateChangedListener(this);
            mAllPlayManager.removeOnZoneListChangedListener(this);
            mAllPlayManager.removeOnZoneStateChangedListener(this);
            mAllPlayManager.removeOnDevicePasswordRequestedListener(this);

            try {
                IoTSysManager.getInstance().removeSystemListener(this);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateState() {
        // id will be null only if its tablet and no players found, updateState ui text
        if (mID == null) {
            // TODO : UX : updateState UI with no players found text - it will show last device details for now
            return;
        }

        if (mGetDeviceInfoTask == null) {
            mGetDeviceInfoTask = new GetDeviceInfoTask(getString(R.string.loading));
            mBaseActivity.addTaskToQueue(mGetDeviceInfoTask);
        }
    }

    private void updateSettings() {
        IoTDevice selectedDevice = mAllPlayManager.getDeviceByHostName(mHost);
        IoTPlayer player = mAllPlayManager.getPlayerByHostName(mHost);
        String deviceDisplayName = getDeviceDisplayName(selectedDevice);

        // no selected device or selected device lost and no player found, finish the current activity for handset;
        // for tablet a different device detail will show or no device found text will be shown earlier
        if (deviceDisplayName == null) {
            //TODO : UX : show dialog saying device not found and finish? tablet?
            mBaseActivity.finish();
            return;
        }

        mTitleTextView.setText(deviceDisplayName);

        final List<DeviceSettingsGroup> deviceSettingsGroups = createDeviceSettingsItem(deviceDisplayName,
                                                                                        selectedDevice, player);
        if (mAdapter != null) {
            mAdapter.updateDeviceSettings(deviceSettingsGroups);
        }
    }

    public String getDeviceDisplayName(IoTDevice selectedDevice) {
        String deviceDisplayName = null;

        if (selectedDevice != null) {
            deviceDisplayName = selectedDevice.getName();
        }
        else {
            List<IoTPlayer> players = mAllPlayManager.getPlayers();
            // get device display name from player if device is null
            // if zone changes come after device changes this will still show the player name, hence added zone
            // listeners
            for (IoTPlayer player : players) {
                if ((mID != null) && (player.getPlayerId() != null) && player.getPlayerId().equals(mID)) {
                    deviceDisplayName = player.getName();
                    break;
                }
            }
        }
        return deviceDisplayName;
    }

    private List<DeviceSettingsGroup> createDeviceSettingsItem(String deviceDisplayName, IoTDevice selectedDevice,
                                                               IoTPlayer player) {

        List<DeviceSettingsGroup> deviceSettingsGroups = new ArrayList<>();

        List<DeviceSettingsItem> speakerSettingsDetails = new ArrayList<>();

        DeviceSettingsItem speakerNameSetting =
                new DeviceSettingsItem(DeviceSettingsItemType.DEVICE_NAME,
                                       getString(R.string.speaker_name),
                                       getString(R.string.cont_desc_settings_item_set_name));
        speakerNameSetting.setDescription(deviceDisplayName);
        speakerSettingsDetails.add(speakerNameSetting);

        DeviceSettingsGroup speakerSettingsItem = new DeviceSettingsGroup(DeviceSettingsGroupType.DEVICE_SETTINGS,
                                                                          getString(R.string.speaker_settings),
                                                                          speakerSettingsDetails,
                                                                          getString(R.string.cont_desc_settings_group_settings));
        deviceSettingsGroups.add(speakerSettingsItem);

        if (selectedDevice != null) {

            DeviceSettingsItem voiceControlItem = new DeviceSettingsItem(
                    DeviceSettingsItemType.VOICE_CONTROL, getString(R.string.voice_control_setting),
                    getString(R.string.cont_desc_settings_item_voice_control));
            speakerSettingsDetails.add(voiceControlItem);

            if (selectedDevice.isZigbeeAvailable()) {
                DeviceSettingsItem item = new DeviceSettingsItem(
                        DeviceSettingsItemType.ZIGBEE, getString(R.string.zigbee_setting),
                        getString(R.string.cont_desc_settings_item_zigbee));
                speakerSettingsDetails.add(item);
            }

            // Bluetooth
            if (selectedDevice.isBluetoothAvailable()) {
                // add access to the Bluetooth settings
                List<DeviceSettingsItem> bluetoothSettings = new ArrayList<>();
                DeviceSettingsGroup bluetoothSettingsItem = new DeviceSettingsGroup(DeviceSettingsGroupType.BLUETOOTH,
                                                                                    getString(R.string.settings_bluetooth_title),
                                                                                    bluetoothSettings,
                                                                                    getString(R.string.cont_desc_settings_group_bluetooth));
                deviceSettingsGroups.add(bluetoothSettingsItem);
                DeviceSettingsItem bluetoothItem = new DeviceSettingsItem(
                        DeviceSettingsItemType.BLUETOOTH_DEVICES,
                        getString(R.string.settings_bluetooth_devices),
                        getString(R.string.cont_desc_settings_item_bluetooth));
                bluetoothSettings.add(bluetoothItem);
            }
        }

        if (player != null) {
            if (selectedDevice != null && selectedDevice.isNetworkInfoAvailable()) {

                List<DeviceSettingsItem> networkSettingsDetails = new ArrayList<DeviceSettingsItem>();
                NetworkInterface networkInterface = selectedDevice.getNetworkInterface();

                String title, subTitle, ipAddress, macAddress;

                if (networkInterface == NetworkInterface.WIFI) {
                    title = getString(R.string.wireless_network);
                    subTitle = selectedDevice.getWifiSSID();
                    ipAddress = selectedDevice.getWifiIPAddress();
                    macAddress = selectedDevice.getWifiMacAddress();
                }
                else {
                    title = getString(R.string.wired_network);
                    subTitle = getString(R.string.wired_network_name);
                    ipAddress = selectedDevice.getEthernetIPAddress();
                    macAddress = selectedDevice.getEthernetMacAddress();
                }

                DeviceSettingsItem networkTypeSetting = new DeviceSettingsItem(
                        DeviceSettingsItemType.NETWORK, title,
                        getString(R.string.cont_desc_settings_item_connection_type));
                networkTypeSetting.setDescription(subTitle);
                networkSettingsDetails.add(networkTypeSetting);

                //signal settings to be shown only for wifi
                if (networkInterface == NetworkInterface.WIFI) {
                    DeviceSettingsItem signalSetting = new DeviceSettingsItem(
                            DeviceSettingsItemType.SIGNAL_STRENGTH, getString(R.string.signal_strength),
                            getString(R.string.cont_desc_settings_item_signal));
                    networkSettingsDetails.add(signalSetting);
                }

                DeviceSettingsItem ipAddressSetting = new DeviceSettingsItem(
                        DeviceSettingsItemType.IP_ADDRESS, getString(R.string.ip_address),
                        getString(R.string.cont_desc_settings_ip_address));
                ipAddressSetting.setDescription(ipAddress);
                networkSettingsDetails.add(ipAddressSetting);

                DeviceSettingsItem macAddressSetting = new DeviceSettingsItem(
                        DeviceSettingsItemType.MAC_ADDRESS, getString(R.string.mac_address),
                        getString(R.string.cont_desc_settings_mac_address));
                macAddressSetting.setDescription(macAddress);
                networkSettingsDetails.add(macAddressSetting);

                DeviceSettingsGroup networkSettingsItem = new DeviceSettingsGroup(
                        DeviceSettingsGroupType.NETWORK, getString(R.string.network), networkSettingsDetails,
                        getString(R.string.cont_desc_settings_group_network));
                deviceSettingsGroups.add(networkSettingsItem);
            }
        }
        return deviceSettingsGroups;
    }

    public void updateId(String id) {
        mID = id;
        updateInUiThread();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        DeviceSettingsGroupType itemType =
                ((DeviceSettingsGroup) parent.getExpandableListAdapter().getGroup(groupPosition)).getItemType();
        DeviceSettingsItem childItem =
                (DeviceSettingsItem) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
        switch (itemType) {
            case DEVICE_SETTINGS:
                DeviceSettingsItemType itemDetailsType = childItem.getType();
                switch (itemDetailsType) {
                    case DEVICE_NAME:
                        final EditTextDialogFragment customNameDialogFragment = EditTextDialogFragment
                                .newEditTextDialog(DIALOG_SPEAKER_NAME_TAG, getString(R.string.custom_name_title),
                                                   childItem.getDescription(), "",
                                                   getString(R.string.button_set), getString(R.string.button_cancel));
                        customNameDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {

                            @Override
                            public void onPositiveButtonClicked(String tag) {
                                String name = customNameDialogFragment.getEditText().getText().toString();
                                try {
                                    IoTSysManager.getInstance().setDeviceName(mHost, name, success -> Log.d(TAG, "Set" +
                                            " device name :" + success));
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNegativeButtonClicked(String tag) {
                            }
                        });

                        mBaseActivity.showDialog(customNameDialogFragment, DIALOG_SPEAKER_NAME_TAG);
                        break;
                    case VOICE_CONTROL:
                        IoTDevice device = mAllPlayManager.getDeviceByHostName(mHost);
                        if (device != null) {
                            showFragment(VoiceControlFragment.newInstance(device.getId(), mHost));
                        }
                        else {
                            Log.e(TAG, "invalid Device ID!");
                        }
                        break;
                    case AUDIO_SETTING:
                        showFragment(AudioSettingFragment.newInstance(mID, mHost));
                        break;
                    case ZIGBEE:
                        device = mAllPlayManager.getDeviceByHostName(mHost);
                        if (device != null) {
                            showFragment(ZigbeeConfigurationFragment.newInstance(device.getId(), mHost));
                        }
                        else {
                            Log.e(TAG, "invalid Device ID!");
                        }
                        break;
                }
                break;
            case FIRMWARE:
                DeviceSettingsItemType firmwareDetailsType = childItem.getType();
                switch (firmwareDetailsType) {
                    case CHECK_UPDATES:
                        if (isFirmwareUpdateAvailable()) {
                            showFirmwareUpdateDialog();
                        }
                        else {
                            if (mCheckFirmwareTask == null) {
                                mCheckFirmwareTask = new CheckFirmwareTask(getString(R.string.firmware_checking));
                                mBaseActivity.addTaskToQueue(mCheckFirmwareTask);
                            }
                        }
                        break;
                }
                break;
            case RESTART_RESET:
                if (DeviceSettingsItemType.RESTART.equals(childItem.getType())) {
                    CustomDialogFragment restartDialogFragment = CustomDialogFragment.newDialog(RESTART_TAG, "",
                                                                                                getString(R.string.restart_message, mAllPlayManager.getDeviceByHostName(mHost).getName()), getString(R.string.settings_restart), getString(R.string.cancel));
                    restartDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked(String tag) {
                            if (mRestartDeviceTask == null) {
                            }
                        }

                        @Override
                        public void onNegativeButtonClicked(String tag) {
                        }
                    });
                    mBaseActivity.showDialog(restartDialogFragment, RESTART_TAG);
                }
                else {
                    CustomDialogFragment factoryResetDialogFragment =
                            CustomDialogFragment.newDialog(FACTORY_RESET_TAG, "", getString(R.string.reset_message,
                                                                                            mAllPlayManager.getDeviceByHostName(mHost).getName()), getString(R.string.settings_reset), getString(R.string.cancel));
                    factoryResetDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked(String tag) {
                            if (mFactoryResetDeviceTask == null) {
                            }
                        }

                        @Override
                        public void onNegativeButtonClicked(String tag) {
                        }
                    });
                    mBaseActivity.showDialog(factoryResetDialogFragment, FACTORY_RESET_TAG);
                }
                return true;

            case BLUETOOTH:
                onBluetoothGroupClick(childItem);
                return true;
        }
        return false;
    }

    private void showFirmwareUpdateDialog() {
        if (mAllPlayManager.getDeviceByHostName(mHost).haveNewFirmware()) {
            CustomDialogFragment factoryResetDialogFragment = CustomDialogFragment.newDialog(FIRMWARE_UPDATE_TAG,
                                                                                             getString(R.string.firmware_update_available_title), getString(R.string.firmware_update_available_message, ""), getString(R.string.update), getString(R.string.not_now));
            factoryResetDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                @Override
                public void onPositiveButtonClicked(String tag) {
                    performFirmwareUpdate(mAllPlayManager.getDevice(mID));
                }

                @Override
                public void onNegativeButtonClicked(String tag) {
                }
            });
            mBaseActivity.showDialog(factoryResetDialogFragment, FIRMWARE_UPDATE_TAG);
        }
        else {
            IoTPlayer player = getPlayerIfDeviceIsSoundbarWithSurrounds(mAllPlayManager.getDeviceByHostName(mHost));
            if (player != null) {
                showSurroundUpdateDialog(player);
            }
        }
    }

    /**
     * <p>This method is called when once of the items of the Bluetooth group is clicked on.</p>
     * <p>This method acts depending on the item which had been selected.</p>
     *
     * @param childItem
     *         The item which had been clicked on.
     */
    private void onBluetoothGroupClick(@NonNull DeviceSettingsItem childItem) {
        DeviceSettingsItemType bluetoothDetailsType = childItem.getType();
        switch (bluetoothDetailsType) {
            case BLUETOOTH_DEVICES:
                showFragment(BluetoothSettingsFragment.newInstance(mHost));
                break;
        }
    }

    private IoTPlayer getPlayerIfDeviceIsSoundbarWithSurrounds(IoTDevice device) {
        final IoTPlayer player = mAllPlayManager.getPlayer(device.getId());
        if ((player != null) && ((player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) || (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND)))) {
            return player;
        }
        return null;
    }

    private void showSurroundUpdateDialog(final IoTPlayer player) {
        if ((player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) && (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND))) {
            mSurroundsUpdateDialogFragment = CustomDialogFragment.newFirmwareUpdateDialog(SURROUNDS_UPDATE_TAG,
                                                                                          getString(R.string.firmware_update_available_title), getString(R.string.firmware_message_title), getString(R.string.update), getString(R.string.not_now));
            mSurroundsUpdateDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                @Override
                public void onPositiveButtonClicked(String tag) {
                    if (mSurroundsUpdateDialogFragment.getLeftSurroundCheckBox().isChecked()) {
                        updateSurroundFirmware(player, HomeTheaterChannel.LEFT_SURROUND);
                    }
                    else if (mSurroundsUpdateDialogFragment.getRightSurroundCheckBox().isChecked()) {
                        updateSurroundFirmware(player, HomeTheaterChannel.RIGHT_SURROUND);
                    }
                }

                @Override
                public void onNegativeButtonClicked(String tag) {
                }
            });
            mBaseActivity.showDialog(mSurroundsUpdateDialogFragment, SURROUNDS_UPDATE_TAG);
        }
        else if (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) {
            CustomDialogFragment surroundsUpdateDialogFragment =
                    CustomDialogFragment.newDialog(LEFT_SURROUND_UPDATE_TAG,
                                                   getString(R.string.firmware_update_available_title),
                                                   getString(R.string.firmware_update_available_message,
                                                             getString(R.string.firmware_update_available_left_surround_message)), getString(R.string.update), getString(R.string.not_now));
            surroundsUpdateDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                @Override
                public void onPositiveButtonClicked(String tag) {
                    updateSurroundFirmware(player, HomeTheaterChannel.LEFT_SURROUND);
                }

                @Override
                public void onNegativeButtonClicked(String tag) {
                }
            });
            mBaseActivity.showDialog(surroundsUpdateDialogFragment, LEFT_SURROUND_UPDATE_TAG);
        }
        else if (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND)) {
            CustomDialogFragment surroundsUpdateDialogFragment =
                    CustomDialogFragment.newDialog(RIGHT_SURROUND_UPDATE_TAG,
                                                   getString(R.string.firmware_update_available_title),
                                                   getString(R.string.firmware_update_available_message,
                                                             getString(R.string.firmware_update_available_right_surround_message)), getString(R.string.update), getString(R.string.not_now));
            surroundsUpdateDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                @Override
                public void onPositiveButtonClicked(String tag) {
                    updateSurroundFirmware(player, HomeTheaterChannel.RIGHT_SURROUND);
                }

                @Override
                public void onNegativeButtonClicked(String tag) {
                }
            });
            mBaseActivity.showDialog(surroundsUpdateDialogFragment, RIGHT_SURROUND_UPDATE_TAG);
        }
    }

    public void updateSurroundFirmware(IoTPlayer player, HomeTheaterChannel channel) {
        if (mSurroundFirmwareUpdateTask == null) {
            mSurroundFirmwareUpdateTask = new SurroundFirmwareUpdateTask(getString(R.string.progress_firmware_update)
                    , player, channel);
            mBaseActivity.addTaskToQueue(mSurroundFirmwareUpdateTask);
        }
        ;
    }

    private boolean isFirmwareUpdateAvailable() {
        List<IoTDevice> devices = mApp.getAllPlayManager().getDevices();
        for (IoTDevice device : devices) {
            IoTPlayer player = mAllPlayManager.getPlayer(device.getId());

            if (((device.getUpdateStatus() == UpdateStatus.NONE) && device.haveNewFirmware()) ||
                    ((device.getUpdateStatus() == UpdateStatus.NONE) && device.isPhysicalRebootRequired()) || ((player != null) && ((player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.LEFT_SURROUND)) || (player.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND))))) {
                Log.d(TAG, "fw updateState " + device.getName());
                return true;
            }
        }
        Log.d(TAG, "no fw updateState");
        return false;
    }

    public void performFirmwareUpdate(IoTDevice device) {
        if (mFirmwareUpdateTask == null) {
            mFirmwareUpdateTask = new FirmwareUpdateTask(getString(R.string.progress_firmware_update), device);
            mBaseActivity.addTaskToQueue(mFirmwareUpdateTask);
        }
    }

    private void startMultiChannelSetupActivity(final MultichannelSetupActivity.SetupType setupType) {
        Intent intent = new Intent(getActivity(), MultichannelSetupActivity.class);
        intent.putExtra(MultichannelSetupActivity.SOUNDBAR_ID_KEY, mID);
        intent.putExtra(MultichannelSetupActivity.SETUP_TYPE_KEY, setupType);
        startActivity(intent);
    }

    @Override
    public void onDeviceListChanged() {
        updateInUiThread();
    }

    @Override
    public void onDeviceStateChanged(IoTDevice device) {
        updateInUiThread();
    }

    @Override
    public void onDeviceConnectionStateChanged(IoTDevice device, ConnectionState connectionState) {

    }

    @Override
    public void onZoneListChanged() {
        // added this callback to handle zone lost changes after device lost since player name is displayed even if
        // we dont find a device
        updateInUiThread();
    }

    @Override
    public void onGroupInfoStateChanged() {
        updateInUiThread();
    }

    @Override
    public UserPassword onDevicePasswordRequested(IoTDevice device) {
        Log.d(TAG, "onDevicePasswordRequested(): device = " + device.getName());
        UserPasswordRequestedRunnable passwordRunnable = new UserPasswordRequestedRunnable();
        mBaseActivity.runOnUiThread(passwordRunnable);
        synchronized (mPasswordObject) {
            try {
                mPasswordObject.wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return passwordRunnable.getPassword();
    }

    private class UserPasswordRequestedRunnable implements Runnable {

        private UserPassword mUserPassword = new UserPassword();

        public UserPassword getPassword() {
            return mUserPassword;
        }

        @Override
        public void run() {
            final EditTextDialogFragment passwordDialogFragment = PasswordEditTextDialogFragment
                    .newEditTextDialog(DIALOG_PASSWORD_TAG, getString(R.string.settings_password_title),
                                       getString(R.string.password_hint), getString(R.string.ok),
                                       getString(R.string.button_cancel), 0);

            passwordDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                @Override
                public void onPositiveButtonClicked(String tag) {
                    setUserPassword(passwordDialogFragment.getEditText().getText().toString(), false);
                }

                @Override
                public void onNegativeButtonClicked(String tag) {
                    IS_SET_PASSWORD_CANCELLED = true;
                    setUserPassword("", true);
                }
            });

            passwordDialogFragment.setEditTextKeyClickedListener(new EditTextDialogFragment.OnEditTextKeyClickedListener() {
                @Override
                public void onDoneClicked(String tag, EditText editText) {
                    setUserPassword(editText.getText().toString(), false);
                }
            });

            IS_SET_PASSWORD_CANCELLED = false;
            mBaseActivity.showDialog(passwordDialogFragment, DIALOG_PASSWORD_TAG);
        }

        private void setUserPassword(final String password, final boolean cancel) {
            mUserPassword.setPassword(password);
            mUserPassword.setUserCancelAuth(cancel);
            synchronized (mPasswordObject) {
                mPasswordObject.notify();
            }
        }
    }

    private class DeviceSettingsItem {

        private DeviceSettingsItemType mType;
        private String mTitle;
        private String mDescription;
        private String mContentDescription;

        DeviceSettingsItem(DeviceSettingsItemType type, String title,
                           String contentDescription) {
            mType = type;
            mTitle = title;
            mContentDescription = contentDescription;
        }

        void setDescription(String description) {
            mDescription = description;
        }

        String getTitle() {
            return mTitle;
        }

        String getDescription() {
            return mDescription;
        }

        DeviceSettingsItemType getType() {
            return mType;
        }

        String getContentDescription() {
            return mContentDescription;
        }
    }

    private class DeviceSettingsGroup {

        private DeviceSettingsGroupType mItemType;
        private String mTitle;
        private List<DeviceSettingsItem> mChildItems;
        private String mContentDescription;

        DeviceSettingsGroup(DeviceSettingsGroupType itemType, String title, List<DeviceSettingsItem> childItems,
                            String contentDescription) {
            mItemType = itemType;
            mTitle = title;
            mChildItems = childItems;
            mContentDescription = contentDescription;
        }

        DeviceSettingsGroupType getItemType() {
            return mItemType;
        }

        String getTitle() {
            return mTitle;
        }

        List<DeviceSettingsItem> getChildItems() {
            return mChildItems;
        }

        String getContentDescription() {
            return mContentDescription;
        }
    }

    private class DeviceSettingsAdapter extends BaseExpandableListAdapter {

        private List<DeviceSettingsGroup> mDeviceSettingsGroup = new ArrayList<DeviceSettingsGroup>();

        public void updateDeviceSettings(List<DeviceSettingsGroup> deviceSettingsGroup) {
            mDeviceSettingsGroup = deviceSettingsGroup;
            notifyDataSetChanged();
        }

        @Override
        public int getGroupCount() {
            return mDeviceSettingsGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mDeviceSettingsGroup.get(groupPosition).getChildItems().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mDeviceSettingsGroup.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mDeviceSettingsGroup.get(groupPosition).getChildItems().get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_settings_headers, parent, false);
            }

            DeviceSettingsGroup group = (DeviceSettingsGroup) getGroup(groupPosition);

            if (group != null) {
                convertView.setContentDescription(group.getContentDescription());
                TextView textView = convertView.findViewById(R.id.settings_header_text_view);
                textView.setText(group.getTitle());
            }

            ExpandableListView listView = (ExpandableListView) parent;
            listView.expandGroup(groupPosition);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                                 ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_item_device_settings_child, parent, false);
            }

            DeviceSettingsItem itemDetails = ((DeviceSettingsItem) getChild(groupPosition,
                                                                            childPosition));
            if (itemDetails != null) {
                convertView.setContentDescription(itemDetails.getContentDescription());

                TextView titleView = convertView.findViewById(R.id.device_settings_child_text_view);
                titleView.setText(itemDetails.getTitle());
                titleView.setContentDescription(getString(R.string.cont_desc_item_title, itemDetails.getContentDescription()));

                TextView descriptionView = convertView.findViewById(R.id.device_settings_child_sub_text_view);
                descriptionView.setVisibility(View.GONE);
                descriptionView.setContentDescription(getString(R.string.cont_desc_item_description,
                                                                itemDetails.getContentDescription()));

                final Switch switchView = convertView.findViewById(R.id.device_settings_child_switch_view);
                switchView.setVisibility(View.GONE);
                switchView.setContentDescription(getString(R.string.cont_desc_item_control,
                                                                itemDetails.getContentDescription()));

                RelativeLayout textViewLayout = convertView.findViewById(R.id.device_settings_child_text_view_layout);
                ViewGroup.LayoutParams layoutParams = textViewLayout.getLayoutParams();
                layoutParams.height =
                        (int) getResources().getDimension(R.dimen.settings_activity_list_item_settings_child_height);

                if (!Utils.isStringEmpty(itemDetails.getDescription())) {
                    layoutParams.height =
                            (int) getResources().getDimension(R.dimen.device_settings_activity_list_item_settings_sub_child_height);
                    descriptionView.setVisibility(View.VISIBLE);
                    descriptionView.setText(itemDetails.getDescription());
                }

                textViewLayout.setLayoutParams(layoutParams);

                switch ((itemDetails.getType())) {
                    case UPDATE_AUTOMATICALLY:
                        break;
                    case PASSWORD_PROTECTION:
                        switchView.setVisibility(View.VISIBLE);
                        final IoTDevice device = mAllPlayManager.getDeviceByHostName(mHost);
                        final boolean isPasswordSupported = device.isPasswordSupported;
                        final boolean isPasswordSet = device.isPasswordSet;
                        if (isPasswordSupported) {
                            switchView.setClickable(true);
                            switchView.setChecked(isPasswordSet);
                        }
                        else {
                            // TODO : UX : how to notify user that password is not supported
                            switchView.setClickable(false);
                        }

                        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (!isPasswordSet) {
                                    final CreaterPasswordDialogFragment setPasswordDialogFragment =
                                            CreaterPasswordDialogFragment
                                                    .newCreatePasswordDialog(DIALOG_SET_PASSWORD_TAG,
                                                                             getString(R.string.settings_password_title),
                                                                             getString(R.string.ok),
                                                                             getString(R.string.button_cancel));

                                    setPasswordDialogFragment.setCreatePasswordKeyClickedListener(new CreaterPasswordDialogFragment.OnCreatePasswordKeyClickedListener() {
                                        @Override
                                        public void onDoneClicked(String tag, EditText editText) {
                                            setPassword(editText.getText().toString());
                                        }
                                    });

                                    mBaseActivity.showDialog(setPasswordDialogFragment, DIALOG_SET_PASSWORD_TAG);
                                }
                                else {
                                    if (mPasswordTask == null) {
                                        mPasswordTask = new PasswordTask(getString(R.string.progress_password),
                                                                         CreaterPasswordDialogFragment.DEFAULT_PASSWORD);
                                        mBaseActivity.addTaskToQueue(mPasswordTask);
                                    }
                                }
                            }

                            public void setPassword(String password) {
                                if (mPasswordTask == null) {
                                    // TODO : UX : progress text?
                                    mPasswordTask = new PasswordTask(getString(R.string.progress_password), password);
                                    mBaseActivity.addTaskToQueue(mPasswordTask);
                                }
                            }
                        });
                        break;
                }
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private class RestartDeviceTask extends RequestAsyncTask implements OnGroupListChangedListener {

        public RestartDeviceTask(String progressTitle) {
            super(progressTitle, null, mBaseActivity, null);
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                }

                @Override
                public void onRequestFailed() {
                    CustomDialogFragment removeSubDialogFragment =
                            CustomDialogFragment.newDialog(RESTART_DEVICE_ERROR_TAG,
                                                           getString(R.string.restart_device_error_title),
                                                           getString(R.string.restart_device_error_message,
                                                                     mAllPlayManager.getDevice(mID).getName()),
                                                           getString(R.string.ok), null);
                    removeSubDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked(String tag) {
                        }

                        @Override
                        public void onNegativeButtonClicked(String tag) {
                        }
                    });

                    mBaseActivity.showDialog(removeSubDialogFragment, RESTART_DEVICE_ERROR_TAG);
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAllPlayManager.addOnZoneListChangedListener(this);
            IoTDevice device = mAllPlayManager.getDevice(mID);
            if (device != null) {
                mResult = (device.reboot() == IoTError.NONE);
                doWait(WAIT_TIME);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mAllPlayManager.removeOnZoneListChangedListener(this);
            super.onPostExecute(param);
            mRestartDeviceTask = null;
            mBaseActivity.finish();
        }

        @Override
        public void onZoneListChanged() {
            if (mAllPlayManager.getDevice(mID) == null) {
                interrupt();
            }
        }
    }

    private class FactoryResetDeviceTask extends RequestAsyncTask implements
            OnGroupListChangedListener {

        public FactoryResetDeviceTask(String progressTitle) {
            super(progressTitle, null, mBaseActivity, null);
            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                }

                @Override
                public void onRequestFailed() {
                    CustomDialogFragment removeSubDialogFragment =
                            CustomDialogFragment.newDialog(FACTORY_RESET_DEVICE_ERROR_TAG,
                                                           getString(R.string.factory_reset_device_error_title),
                                                           getString(R.string.factory_reset_device_error_message,
                                                                     mAllPlayManager.getDevice(mID).getName()),
                                                           getString(R.string.ok), null);
                    removeSubDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked(String tag) {
                        }

                        @Override
                        public void onNegativeButtonClicked(String tag) {
                        }
                    });

                    mBaseActivity.showDialog(removeSubDialogFragment, FACTORY_RESET_DEVICE_ERROR_TAG);
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAllPlayManager.addOnZoneListChangedListener(this);
            IoTDevice device = mAllPlayManager.getDevice(mID);
            if (device != null) {
                mResult = (device.factoryReset() == IoTError.NONE);
                doWait(WAIT_TIME);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mAllPlayManager.removeOnZoneListChangedListener(this);
            super.onPostExecute(param);
            mFactoryResetDeviceTask = null;
            mBaseActivity.finish();
        }

        @Override
        public void onZoneListChanged() {
            if (mAllPlayManager.getDevice(mID) == null) {
                interrupt();
            }
        }
    }

    private class PasswordTask extends RequestAsyncTask implements OnDeviceListChangedListener {

        String mPassword;

        public PasswordTask(String progressTitle, String password) {
            super(progressTitle, null, mBaseActivity, null);
            mPassword = password;

            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                }

                @Override
                public void onRequestFailed() {
                    // on password protection off, if cancel is clicked on the enter password dialog, result is
                    // ER_AUTH_USER_REJECT and thus mResult is false. But for cancel we dont need to show any dialog.
                    if (!IS_SET_PASSWORD_CANCELLED) {
                        CustomDialogFragment passwordErrorDialogFragment =
                                CustomDialogFragment.newDialog(PASSWORD_ERROR_TAG,
                                                               getString(R.string.settings_password_turn_off_error_title), getString(R.string.settings_password_turn_off_error_message, mAllPlayManager.getDevice(mID).getName()), getString(R.string.ok), null);
                        passwordErrorDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                            @Override
                            public void onPositiveButtonClicked(String tag) {
                            }

                            @Override
                            public void onNegativeButtonClicked(String tag) {
                            }
                        });

                        mBaseActivity.showDialog(passwordErrorDialogFragment, PASSWORD_ERROR_TAG);
                    }
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAllPlayManager.addOnDeviceListChangedListener(this);
            IoTDevice device = mAllPlayManager.getDevice(mID);
            if (device != null) {
                mResult = (device.setPassword(mPassword) == IoTError.NONE);
                if (mResult == true) {
                    doWait(WAIT_TIME);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            mAllPlayManager.removeOnDeviceListChangedListener(this);
            super.onPostExecute(param);
            mPasswordTask = null;
            if (mResult == true) {
                mBaseActivity.finish();
            }
        }

        @Override
        public void onDeviceListChanged() {
            if (mAllPlayManager.getDevice(mID) == null) {
                interrupt();
            }
        }
    }

    private class GetDeviceInfoTask extends RequestAsyncTask {

        public GetDeviceInfoTask(String progressTitle) {
            super(progressTitle, null, mBaseActivity, null);

            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                }

                @Override
                public void onRequestFailed() {
                    // TODO : UX : what error to show on failing to retrieve device info
                    CustomDialogFragment passwordErrorDialogFragment =
                            CustomDialogFragment.newDialog(DEVICE_INFO_ERROR_TAG, "", "", getString(R.string.ok), null);
                    passwordErrorDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                        @Override
                        public void onPositiveButtonClicked(String tag) {
                        }

                        @Override
                        public void onNegativeButtonClicked(String tag) {
                        }
                    });

                    mBaseActivity.showDialog(passwordErrorDialogFragment, DEVICE_INFO_ERROR_TAG);
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            IoTDevice device = mAllPlayManager.getDeviceByHostName(mHost);
            if (device != null) {
                mResult = (device.updateNetworkInfo() == IoTError.NONE);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mGetDeviceInfoTask = null;
            updateSettings();
        }
    }

    private class CheckFirmwareTask extends RequestAsyncTask {

        public CheckFirmwareTask(String progressTitle) {
            super(progressTitle, null, mBaseActivity, null);

            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    if (isFirmwareUpdateAvailable()) {
                        showFirmwareUpdateDialog();
                    }
                    else {
                        CustomDialogFragment factoryResetDialogFragment =
                                CustomDialogFragment.newDialog(NO_FIRMWARE_UPDATE_TAG, "",
                                                               getString(R.string.firmware_update_not_available_message), getString(R.string.ok), "");
                        factoryResetDialogFragment.setButtonClickedListener(new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                            @Override
                            public void onPositiveButtonClicked(String tag) {
                            }

                            @Override
                            public void onNegativeButtonClicked(String tag) {
                            }
                        });
                        mBaseActivity.showDialog(factoryResetDialogFragment, NO_FIRMWARE_UPDATE_TAG);
                    }
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            IoTDevice device = mAllPlayManager.getDeviceByHostName(mHost);
            if (device != null) {
                mResult = (device.checkForNewFirmware() == IoTError.NONE);
                IoTPlayer player = mAllPlayManager.getPlayer(device.getId());
                if ((player != null) && player.isSoundBar()) {
                    Log.d(TAG, "check for left and right surround updateState");
                    player.checkNewHomeTheaterChannelFirmwareUpdate(HomeTheaterChannel.LEFT_SURROUND);
                    player.checkNewHomeTheaterChannelFirmwareUpdate(HomeTheaterChannel.RIGHT_SURROUND);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mCheckFirmwareTask = null;
        }
    }

    private class FirmwareUpdateTask extends RequestAsyncTask {

        IoTDevice mDevice;

        public FirmwareUpdateTask(String progressTitle, IoTDevice device) {
            super(progressTitle, null, mBaseActivity, null);
            mDevice = device;

            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                }

                // TODO : UX : check what message to display on firmware updateState failure
                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mDevice != null) {
                mResult = (mDevice.updateFirmware() == IoTError.NONE);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mFirmwareUpdateTask = null;
        }
    }

    @Override
    public void didChangeName(String name) {
        UiThreadExecutor.getInstance().execute(() -> updateSettings());
    }

    @Override
    public void deviceDidChangeBatteryState(BatteryStatusAttr attr) {

    }

    private class SurroundFirmwareUpdateTask extends RequestAsyncTask implements
            OnHomeTheaterChannelChangedListener {

        IoTPlayer mPlayer;
        HomeTheaterChannel mChannel;
        private static final int SURROUNDS_UPDATE_WAIT_TIME = 120000;

        public SurroundFirmwareUpdateTask(String progressTitle, IoTPlayer player, HomeTheaterChannel channel) {
            super(progressTitle, null, mBaseActivity, null);
            mPlayer = player;
            mChannel = channel;

            mListener = new RequestListener() {
                @Override
                public void onRequestSuccess() {
                    if (mPlayer.haveNewHomeTheaterChannelFirmware(HomeTheaterChannel.RIGHT_SURROUND)) {
                        updateSurroundFirmware(mPlayer, HomeTheaterChannel.RIGHT_SURROUND);
                    }
                }

                @Override
                public void onRequestFailed() {
                }
            };
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mPlayer != null) {
                mResult = (mPlayer.updateHomeTheaterChannelFirmware(mChannel) == IoTError.NONE);
            }
            doWait(SURROUNDS_UPDATE_WAIT_TIME);

            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);
            mSurroundFirmwareUpdateTask = null;
        }

        @Override
        public void onHomeTheaterChannelUpdate(IoTPlayer player, HomeTheaterChannelMap channelMap) {

        }


        @Override
        public void onHomeTheaterChannelFirmwareUpdateStatusChanged(IoTPlayer player, HomeTheaterChannel channel,
                                                                    UpdateStatus updateStatus) {
            Log.d(TAG,
                  "[onHomeTheaterChannelFirmwareUpdateStatusChanged] " + player.getName() + " channel " + channel +
                          " updateState status " + updateStatus);
            if (player.equals(mPlayer) && updateStatus == UpdateStatus.SUCCESSFUL) {
                interrupt();
            }
        }

        @Override
        public void onHomeTheaterChannelFirmwareUpdateProgressChanged(IoTPlayer player, HomeTheaterChannel channel,
                                                                      double progress) {

        }
    }
}
