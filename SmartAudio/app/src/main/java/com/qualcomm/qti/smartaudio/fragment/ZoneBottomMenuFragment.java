/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */
package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.app.SmartAudioApplication;
import com.qualcomm.qti.smartaudio.interfaces.BottomDialogResultListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager;
import com.qualcomm.qti.smartaudio.view.BottomMenuItem;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.qualcomm.qti.smartaudio.activity.MainActivity.ExtraKeys;

public class ZoneBottomMenuFragment extends BottomSheetDialogFragment {

    @IntDef(value = {ZoneBottomMenuSelection.PAIR_BT, ZoneBottomMenuSelection.PAIR_ZIGBEE,
            ZoneBottomMenuSelection.MANAGE_BT, ZoneBottomMenuSelection.MANAGE_ZIGBEE,
            ZoneBottomMenuSelection.NAVIGATE_SETTINGS_PAGE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ZoneBottomMenuSelection {
        int PAIR_BT = 301;
        int PAIR_ZIGBEE = 302;
        int MANAGE_BT = 303;
        int MANAGE_ZIGBEE = 304;
        int NAVIGATE_SETTINGS_PAGE = 305;
    }

    private static final String TAG = "ZoneBottomMenuFragment";

    private int mSheetId;
    private BottomDialogResultListener mListener;

    private String mPlayerId;
    private String mPlayerHostname;

    private LinearLayout mOptionsLayout;

    private AllPlayManager mAllPlayManager;
    private IoTType mType;

    private Context mContext;

    /**
     * Sets the listener for the result of the BottomSheet.
     *
     * @param listener The listener that subscribes to get the result.
     */
    public void setBottomDialogResultListener(BottomDialogResultListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        SmartAudioApplication app = (SmartAudioApplication) activity.getApplication();
        if (app == null) return;
        mAllPlayManager = app.getAllPlayManager();
        mContext = context;

    }

    private void initValues() {
        Bundle bundle = getArguments();

        if (bundle == null) return;
        mSheetId = bundle.getInt(ExtraKeys.SHEET_ID_EXTRA);
        mPlayerId = bundle.getString(ExtraKeys.PLAYER_ID_EXTRA);
        mPlayerHostname = bundle.getString(ExtraKeys.HOSTNAME_EXTRA);
        mType = IoTType.fromValue(bundle.getInt(ExtraKeys.TYPE_EXTRA));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_navigation_speaker, container, false);

        initValues();

        mOptionsLayout = view.findViewById(R.id.bottom_menu_zone_linear_layout);
        ImageView mDeviceIconImage = view.findViewById(R.id.bottom_sheet_device_icon);
        TextView deviceNameTextView = view.findViewById(R.id.bottom_sheet_device_name);

        String deviceName = "";

        // Selected device is of type GROUP
        if (mType.equals(IoTType.GROUP)) {
            IoTGroup group = mAllPlayManager.getZone(mPlayerId);
            IoTDevice device = mAllPlayManager.getDeviceByHostName(mPlayerHostname);
            if (device != null) {
                deviceName = device.getName();
            }
            addBluetoothMenuOptions(device);
            addZigbeeMenuOptions(device);
            if(group.isSinglePlayer()) {
                mDeviceIconImage.setImageResource(R.drawable.ic_speaker_23dp);
            } else {
                mDeviceIconImage.setImageResource(R.drawable.ic_group_list_23dp);
            }
        }

        // Selected device is of type SOUND_BAR or PLAYER (single speaker (not in convenience group?))
        if (mType == IoTType.SOUND_BAR || mType == IoTType.PLAYER) {
            IoTDevice device = mAllPlayManager.getDeviceByHostName(mPlayerHostname);
            if (device != null) {
                deviceName = device.getName();
            }
            addBluetoothMenuOptions(device);
            addZigbeeMenuOptions(device);
            mDeviceIconImage.setImageResource(R.drawable.ic_speaker_23dp);
        }

        // Selected device is of type BLUETOOTH_DEVICE
        if (mType == IoTType.BLUETOOTH_DEVICE) {
            IoTDevice device = mAllPlayManager.getDeviceByHostName(mPlayerHostname);
            if (device != null) {
                deviceName = device.getName();
            }
            addBluetoothMenuOptions(device);
        }

        // Selected device is of type ZIGBEE_DEVICE
        if (mType == IoTType.ZIGBEE_DEVICE) {
            IoTDevice device = mAllPlayManager.getDeviceByHostName(mPlayerHostname);
            if (device != null) {
                deviceName = device.getName();
            }
            addZigbeeMenuOptions(device);
        }

        deviceNameTextView.setText(deviceName.toUpperCase());

        BottomMenuItem item = new BottomMenuItem(
                mContext,
                R.string.view_settings_page,
                R.drawable.ic_settings_23dp,
                view1 -> returnDialogResult(ZoneBottomMenuSelection.NAVIGATE_SETTINGS_PAGE));
        mOptionsLayout.addView(item);

        return view;
    }

    /**
     * Adds relevant menu options for Bluetooth depending on:
     * <ol>
     * <li>Bluetooth availability on the device.</li>
     * <li>Number of already paired devices.</li>
     * </ol>
     *
     * @param device The selected device.
     */
    private void addBluetoothMenuOptions(IoTDevice device) {
        if (device == null) return;
        if (device.isBluetoothAvailable()) {
            if (device.getPairedBluetoothDevices().size() > 0) {
                BottomMenuItem item = new BottomMenuItem(
                        mContext,
                        R.string.manage_bt_devices,
                        R.drawable.ic_bt_headphones_list_23dp,
                        view1 -> returnDialogResult(ZoneBottomMenuSelection.MANAGE_BT));
                mOptionsLayout.addView(item);
            } else {
                BottomMenuItem item = new BottomMenuItem(
                        mContext,
                        R.string.pair_bt_device,
                        R.drawable.ic_bt_headphones_list_23dp,
                        view1 -> returnDialogResult(ZoneBottomMenuSelection.PAIR_BT));
                mOptionsLayout.addView(item);
            }
        }
    }

    /**
     * Adds relevant menu options for Zigbee depending on:
     * <ol>
     * <li>Zigbee availability on the device.</li>
     * <li>Number of already zigbee-paired devices.</li>
     * </ol>
     *
     * @param device The selected device.
     */
    private void addZigbeeMenuOptions(IoTDevice device) {
        if (device == null) return;
        if (device.isZigbeeAvailable()) {
            if (device.getZGJoinedDevices().size() > 0) {
                BottomMenuItem item = new BottomMenuItem(
                        mContext,
                        R.string.manage_zigbee_device,
                        R.drawable.ic_zigbee_static_23dp,
                        view1 -> returnDialogResult(ZoneBottomMenuSelection.MANAGE_ZIGBEE));
                mOptionsLayout.addView(item);
            } else {
                BottomMenuItem item = new BottomMenuItem(
                        mContext,
                        R.string.add_zigbee_device,
                        R.drawable.ic_zigbee_static_23dp,
                        view1 -> returnDialogResult(ZoneBottomMenuSelection.PAIR_ZIGBEE));
                mOptionsLayout.addView(item);
            }
        }
    }

    /**
     * Notifies the listener of the result.
     *
     * @param resultId The id of the result (selected action).
     */
    private void returnDialogResult(int resultId) {
        if (mListener == null) return;
        mListener.onZoneBottomDialogResult(this.mSheetId, resultId, mPlayerId, mPlayerHostname);
        dismiss();
    }
}
