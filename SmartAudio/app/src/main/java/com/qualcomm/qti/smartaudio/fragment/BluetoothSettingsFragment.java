/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BluetoothOnBoardingActivity;
import com.qualcomm.qti.smartaudio.adapter.SettingsAdapter;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.OnBluetoothListener;
import com.qualcomm.qti.smartaudio.model.SettingsGroup;
import com.qualcomm.qti.smartaudio.model.SettingsItem;
import com.qualcomm.qti.smartaudio.view.SettingsItemView;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.qualcomm.qti.smartaudio.model.SettingsGroup.GroupStates;
import static com.qualcomm.qti.smartaudio.model.SettingsItem.ItemStates;
import static com.qualcomm.qti.smartaudio.view.SettingsItemView.Types;
import static com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothAdapterState;
import static com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothDiscoverableState;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTBluetoothDevice;

import static com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothError;

/**
 * <p>A fragment to manage the display of the Bluetooth settings of a device (speaker):
 * <ul>
 * <li>Turning the BT on and off,</li>
 * <li>Make the device discoverable,</li>
 * <li>Display the list of paired devices to connect, disconnect or unpair them,</li>
 * <li>To pair a new Bluetooth device.</li>
 * </ul></p>
 */
public class BluetoothSettingsFragment extends BaseSettingsFragment {

    // ========================================================================
    // FIELDS

    /**
     * For debug mode, the tag to display for logs.
     */
    private final String TAG = "BluetoothSettingsFragment";
    /**
     * The key to put the host name of the device in the arguments.
     */
    private static final String EXTRA_HOST = "HOST_NAME";
    /**
     * The tag to identify the dialog which informs the user that the request failed.
     */
    private static final String REQUEST_FAILED_DIALOG_TAG = "REQUEST_FAILED_DIALOG_TAG";
    /**
     * The tag to identify the dialog which informs the user that the request failed.
     */
    private static final String BLUETOOTH_ERROR_DIALOG_TAG = "BLUETOOTH_ERROR_DIALOG";
    /**
     * The time out within which an update is expected after a request had been processed.
     */
    private static final long REQUEST_UPDATE_TIMEOUT_IN_MS = 30000;
    /**
     * The host name of the device to set up the Bluetooth device for.
     */
    private String mHost;
    /**
     * The device for which Bluetooth settings are managed through this fragment.
     */
    private IoTDevice mDevice;
    /**
     * <p>A handler to delay tasks.</p>
     */
    private Handler mHandler = new Handler();
    /**
     * <p>The list of {@link CallBackRunnable CallBackRunnable} currently posted through a {@link
     * Handler Hanlder} which are waiting for a completion callback and a state update from the
     * controller.</p>
     */
    private final ArrayMap<Integer, Runnable> mCallbackRunnableList = new ArrayMap<>();
    /**
     * <p>The list of IoTBluetoothDevices which are displayed in the UI. This is used to get the
     * IoTBluetoothDevice when the user interacts with the view. The keys are the hashcode of the
     * IoTBluetoothDevice.</p>
     */
    private final ArrayMap<Integer, IoTBluetoothDevice> mDevices = new ArrayMap<>();
    /**
     * <p>A comparator to sort a list of {@link IoTBluetoothDevice IoTBluetoothDevice}.</p>
     */
    private final Comparator<IoTBluetoothDevice> mDevicesComparator = Comparator
            .comparing(IoTBluetoothDevice::getName)
            .thenComparing(IoTBluetoothDevice::getAddress);


    // ========================================================================
    // ENUMERATIONS

    /**
     * <p>All the groups of settings managed within this fragment.</p>
     */
    @IntDef({Groups.BLUETOOTH, Groups.PAIRED_DEVICES, Groups.PAIR_A_DEVICE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Groups {
        int BLUETOOTH = 0;
        int PAIRED_DEVICES = 1;
        int PAIR_A_DEVICE = 2;
    }

    /**
     * The list of all settings to display. This enumeration is used to identify the settings when the
     * user interacts with them.
     */
    @IntDef({Items.BLUETOOTH, Items.DISCOVERABLE, Items.PAIR_A_DEVICE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Items {
        int BLUETOOTH = 0;
        int DISCOVERABLE = 1;
        int PAIR_A_DEVICE = 2;
    }

    /**
     * All events which can be received in this fragment and passed as parameters to be interpreted in
     * the UI thread using {@link #updateInUiThread(Object...) updateInUiThread(Object...)}. The
     * events are used as the first parameter of the method.
     */
    @IntDef({Events.BLUETOOTH_ADAPTER_STATE, Events.DISCOVERABLE_STATE, Events.PAIRED_DEVICES,
            Events.BLUETOOTH_ERROR, Events.CONNECTED_DEVICE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Events {
        /**
         * <p>This event represents an update of the Bluetooth Adapter state.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = the Bluetooth adapter <code>state</code> which the object
         * type is {@link com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothAdapterState
         * IoTBluetoothAdapterState }</li>
         * </ul></p>
         */
        int BLUETOOTH_ADAPTER_STATE = 0;
        /**
         * <p>This event represents an update of the discoverable state.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = DISCOVERABLE_STATE</li>
         * <li><code>arguments[1]</code> = the discoverable <code>state</code> which the object type is
         * {@link IoTBluetoothDiscoverableState IoTBluetoothDiscoverableState}</li>
         * </ul></p>
         */
        int DISCOVERABLE_STATE = 1;
        /**
         * <p>This event represents an update of the list of devices paired with the device.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = PAIRED_DEVICES</li>
         * <li><code>arguments[1]</code> = the {@link List List} of paired
         * {@link IoTBluetoothDevice IoTBluetoothDevice}.</li>
         * </ul></p>
         */
        int PAIRED_DEVICES = 2;
        /**
         * <p>This event represents an update of the connection state of a Bluetooth device with the
         * IoT
         * Device.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = CONNECTED_DEVICE</li>
         * <li><code>arguments[1]</code> = the
         * {@link IoTBluetoothDevice IoTBluetoothDevice} for which the connection state with the IoT
         * Device has changed.</li>
         * </ul></p>
         */
        int CONNECTED_DEVICE = 3;
        /**
         * <p>This event represents a Bluetooth error received from the device.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = BLUETOOTH_ERROR</li>
         * <li><code>arguments[1]</code> = The
         * {@link com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothError IoTBluetoothError}
         * which occurs.</li>
         * </ul></p>
         */
        int BLUETOOTH_ERROR = 4;
    }


    // ========================================================================
    // INSTANTIATION

    /**
     * <p>To create a new instance of this fragment to be set up with the given parameters.</p>
     *
     * @param host
     *         The host of the device to set up.
     *
     * @return A new instance with the parameters set up as arguments for the fragment.
     */
    public static BluetoothSettingsFragment newInstance(String host) {
        BluetoothSettingsFragment fragment = new BluetoothSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_HOST, host);
        fragment.setArguments(bundle);
        return fragment;
    }


    // ========================================================================
    // FRAGMENT

    @Override // Fragment
    public void onResume() {
        super.onResume();
        Bundle arg = getArguments();

        if ((mApp != null) && mApp.isInit() && arg != null) {
            mHost = arg.getString(EXTRA_HOST);
            mDevice = mAllPlayManager.getDeviceByHostName(mHost);
            mIoTSysManager.addOnBluetoothListener(mOnBluetoothListener);
        }
        else {
            showRequestFailedDialog();
        }
        updateState();
    }

    @Override // Fragment
    public void onPause() {
        super.onPause();
        if (mApp.isInit()) {
            mIoTSysManager.removeOnBluetoothListener(mOnBluetoothListener);
        }
        for (int event : mCallbackRunnableList.keySet()) {
            cancelCallbackRunnable(event);
        }
    }


    // ========================================================================
    // SETTINGS

    @Override // BaseSettingsFragment
    protected void onViewCreated() {
        setViewTitle(R.string.bluetooth_settings_title, R.string.cont_desc_screen_bluetooth);
        initSettings();
    }

    /**
     * <p>This method initialises the settings by creating all {@link SettingsGroup GroupData} and
     * {@link SettingsItem SettingsItem} which will be displayed within this view.</p>
     */
    private void initSettings() {
        // building general bluetooth settings data
        addSettingsGroup(Groups.BLUETOOTH);
        addSettingsItem(Groups.BLUETOOTH, Items.BLUETOOTH,
                        getString(R.string.bluetooth_settings_bluetooth), "", Types.SWITCH,
                        getString(R.string.cont_desc_settings_item_bluetooth_service));
        addSettingsItem(Groups.BLUETOOTH, Items.DISCOVERABLE,
                        getString(R.string.bluetooth_settings_discoverable),
                        getString(R.string.bluetooth_settings_discoverable_description), Types.SWITCH,
                        getString(R.string.cont_desc_settings_item_bluetooth_discoverability));

        // building paired devices data
        addSettingsGroup(Groups.PAIRED_DEVICES, getString(R.string.bluetooth_settings_paired_devices));

        // building settings to add a devices
        addSettingsGroup(Groups.PAIR_A_DEVICE);
        addSettingsItem(Groups.PAIR_A_DEVICE, Items.PAIR_A_DEVICE,
                        getString(R.string.bluetooth_settings_pair_device), "", Types.DEFAULT,
                        getString(R.string.cont_desc_settings_item_pair_bluetooth_device));
    }

    @Override // BaseSettingsFragment->SettingsAdapterListener
    public void initItemViewDisplay(SettingsItemView view, SettingsGroup groupData,
                                    SettingsItem data) {
    }

    @Override // BaseSettingsFragment->SettingsAdapterListener
    public boolean onSettingsItemEvent(int event, int groupId, int itemId, Object data) {
        if (event == SettingsItemView.Events.VALUE_CHANGED && groupId == Groups.BLUETOOTH
                && itemId == Items.BLUETOOTH && data instanceof Boolean) {
            boolean checked = (Boolean) data;
            onUserActionBluetoothEnabled(checked);
            return true;
        }
        else if (event == SettingsItemView.Events.VALUE_CHANGED && groupId == Groups.BLUETOOTH
                && itemId == Items.DISCOVERABLE && data instanceof Boolean) {
            boolean checked = (Boolean) data;
            onUserActionDiscoverableEnabled(checked);
            return true;
        }
        else if (event == SettingsItemView.Events.OPTION_CLICK && groupId == Groups.PAIRED_DEVICES
                && data instanceof Integer) {
            int option = (int) data;
            return onUserActionMenuOptionSelected(option, itemId);
        }
        else if (event == SettingsItemView.Events.ITEM_CLICK && groupId == Groups.PAIR_A_DEVICE
                && itemId == Items.PAIR_A_DEVICE) {
            onUserActionPairANewDevice();
            return true;
        }

        // unrecognised action
        return false;
    }


    // ========================================================================
    // UI

    @Override // BaseFragment
    protected void updateState() {
        // called when #updateInUiThread is called without arguments
        // or to update the UI data

        // update the settings
        updateBluetoothAdapterState(getBluetoothAdapterState());
        updateDiscoverableState(getBluetoothDiscoverableState());
        updatePairedDevices(getPairedBluetoothDevices());

        // updating the UI
        updateUI();
    }

    /**
     * @param arguments
     *         Expects at least one <code>int</code> argument as one of {@link Events
     *         Events}. See {@link Events Events} for more information on other expected arguments.
     */
    @Override // BaseFragment
    protected void updateState(Object... arguments) {
        if (arguments.length < 1 || !(arguments[0] instanceof Integer)) {
            // unexpected arguments
            return;
        }

        @Events int event = (int) arguments[0];

        cancelCallbackRunnable(event);

        switch (event) {
            case Events.BLUETOOTH_ADAPTER_STATE:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothAdapterState) {
                    onBluetoothAdapterStateUpdated((IoTBluetoothAdapterState) arguments[1]);
                }
                break;

            case Events.DISCOVERABLE_STATE:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothDiscoverableState) {
                    onDiscoverableStateUpdated((IoTBluetoothDiscoverableState) arguments[1]);
                }
                break;

            case Events.PAIRED_DEVICES:
                if (arguments.length >= 2 && arguments[1] instanceof List) {
                    List<IoTBluetoothDevice> devices = (List<IoTBluetoothDevice>) arguments[1];
                    onPairedDevicesUpdated(devices);
                }
                break;

            case Events.BLUETOOTH_ERROR:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothError) {
                    IoTBluetoothError error = (IoTBluetoothError) arguments[1];
                    showBluetoothErrorDialog(error.getAddress(), error.getStatus());
                    updateState();
                }
                break;

            case Events.CONNECTED_DEVICE:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothDevice) {
                    IoTBluetoothDevice device = (IoTBluetoothDevice) arguments[1];
                    updateConnectedDevice(device);
                }
                break;
        }

    }


    // ========================================================================
    // IOT SYS EVENTS & GETTERS

    /**
     * <p>This is a listener to be notified when the Bluetooth states change.</p>
     */
    OnBluetoothListener mOnBluetoothListener = new OnBluetoothListener() {
        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothAdapterStateChanged(IoTDevice device,
                                                   IoTDevice.IoTBluetoothAdapterState state) {
            if (mDevice != null && mDevice.equals(device)) {
                updateInUiThread(Events.BLUETOOTH_ADAPTER_STATE, state);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothDiscoverableStateChanged(IoTDevice device,
                                                        IoTDevice.IoTBluetoothDiscoverableState state) {
            if (mDevice != null && mDevice.equals(device)) {
                updateInUiThread(Events.DISCOVERABLE_STATE, state);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onPairedDevicesUpdated(IoTDevice device, List<IoTBluetoothDevice> devices) {
            if (mDevice != null && mDevice.equals(device)) {
                updateInUiThread(Events.PAIRED_DEVICES, devices);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothError(IoTDevice device, IoTBluetoothError error) {
            if (mDevice != null && mDevice.equals(device)) {
                updateInUiThread(Events.BLUETOOTH_ERROR, error);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onConnectedBluetoothDeviceChanged(IoTDevice device,
                                                      IoTBluetoothDevice bluetoothDevice) {
            if (mDevice != null && mDevice.equals(device)) {
                updateInUiThread(Events.CONNECTED_DEVICE, bluetoothDevice);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothScanStateChanged(IoTDevice device, boolean scanning) {
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothDeviceDiscovered(IoTDevice device, IoTBluetoothDevice scanned) {
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothPairStateUpdated(IoTDevice device, IoTBluetoothDevice pairDevice, boolean paired) {
            // wait for the new list of paired devices to update the UI
        }
    };

    /**
     * <p>To update the Bluetooth Adapter state within the UI once the controller has notified its
     * listener about a new state. This method updates the UI.</p>
     *
     * @param state
     *         The new state of the Bluetooth Adapter.
     */
    private void onBluetoothAdapterStateUpdated(IoTBluetoothAdapterState state) {
        updateBluetoothAdapterState(state);
        updateUI();
    }

    /**
     * <p>To update the Bluetooth Adapter state within the UI once the controller has notified its
     * listener about a new state. This method updates the UI.</p>
     *
     * @param state
     *         The new state of the Bluetooth Adapter.
     */
    private void onDiscoverableStateUpdated(IoTBluetoothDiscoverableState state) {
        updateDiscoverableState(state);
        updateUI();
    }

    /**
     * <p>To update the list of devices displayed in the UI once the controller has notified its
     * listener about a new list. This method updates the UI.</p>
     *
     * @param devices
     *         The new list of paired devices.
     */
    private void onPairedDevicesUpdated(List<IoTBluetoothDevice> devices) {
        updatePairedDevices(devices);
        updateUI();
    }

    /**
     * <p>To get the Bluetooth Adapter state of the device for which the Bluetooth settings are
     * managed through this fragment.</p>
     *
     * @return the state of the Bluetooth Adapter if the device could be found, it returns {@link
     * IoTBluetoothAdapterState#Unknown Unknown} otherwise.
     */
    private IoTBluetoothAdapterState getBluetoothAdapterState() {
        return mDevice != null ? mDevice.getBluetoothAdapterState() : IoTBluetoothAdapterState.Unknown;
    }

    /**
     * <p>To get the Bluetooth Discoverable state of the device for which the Bluetooth settings are
     * managed through this fragment.</p>
     *
     * @return the discoverable state if the device could be found, it returns {@link
     * IoTBluetoothDiscoverableState#Unknown Unknown} otherwise.
     */
    private IoTBluetoothDiscoverableState getBluetoothDiscoverableState() {
        return mDevice != null ? mDevice.getBluetoothDiscoverableState()
                : IoTBluetoothDiscoverableState.Unknown;
    }

    /**
     * <p>To get the list of devices paired with the device for which the Bluetooth settings are
     * managed through this fragment</p>
     *
     * @return the list of paired devices if the device could be found, it returns an empty list
     * otherwise.
     */
    private List<IoTBluetoothDevice> getPairedBluetoothDevices() {
        return mDevice != null ? mDevice.getPairedBluetoothDevices() : Collections.emptyList();
    }


    // ========================================================================
    // USER ACTIONS

    /**
     * <p>This method dispatches the user request related to a device from one of the options
     * menu.</p>
     *
     * @param option
     *         The menu option the user selected.
     * @param deviceId
     *         The device Id for which the user has selected an option.
     *
     * @return True if the event could be handled, false otherwise.
     */
    private boolean onUserActionMenuOptionSelected(int option, int deviceId) {
        switch (option) {
            case R.id.menu_connect:
                onUserActionConnectDevice(deviceId);
                return true;
            case R.id.menu_disconnect:
                onUserActionDisconnectDevice(deviceId);
                return true;
            case R.id.menu_unpair:
                onUserActionUnPairDevice(deviceId);
                return true;
        }
        return false;
    }

    /**
     * <p>This method dispatches the user request to unpair the given device.</p>
     *
     * @param deviceId
     *         The ID of the Bluetooth device to unpair.
     */
    private void onUserActionUnPairDevice(int deviceId) {
        setSettingItemState(Groups.PAIRED_DEVICES, deviceId, ItemStates.UPDATING, true);
        updateUI();
        startCallbackRunnable(Events.PAIRED_DEVICES);
        IoTBluetoothDevice device = mDevices.get(deviceId);
        if (device == null || !mDevice
                .unpairBluetoothDevice(device, new BluetoothCompletionCallback(Events.PAIRED_DEVICES))) {
            onRequestFailed(Events.PAIRED_DEVICES);
        }
    }

    /**
     * <p>This method dispatches the user request to disconnect the given device.</p>
     *
     * @param deviceId
     *         The ID of the Bluetooth device to disconnect.
     */
    private void onUserActionDisconnectDevice(int deviceId) {
        setSettingItemState(Groups.PAIRED_DEVICES, deviceId, ItemStates.UPDATING, true);
        updateUI();
        startCallbackRunnable(Events.CONNECTED_DEVICE);
        IoTBluetoothDevice device = mDevices.get(deviceId);
        if (device == null || !mDevice.disconnectBluetoothDevice(device,
                                                                 new BluetoothCompletionCallback(Events.CONNECTED_DEVICE))) {
            onRequestFailed(Events.CONNECTED_DEVICE);
        }
    }

    /**
     * <p>This method dispatches the user request to connect the given device.</p>
     *
     * @param deviceId
     *         The ID of the Bluetooth device to connect with.
     */
    private void onUserActionConnectDevice(int deviceId) {
        setSettingItemState(Groups.PAIRED_DEVICES, deviceId, ItemStates.UPDATING, true);
        updateUI();
        startCallbackRunnable(Events.CONNECTED_DEVICE);
        IoTBluetoothDevice device = mDevices.get(deviceId);
        if (device == null || !mDevice
                .connectBluetoothDevice(device, new BluetoothCompletionCallback(Events.CONNECTED_DEVICE))) {
            onRequestFailed(Events.CONNECTED_DEVICE);
        }
    }

    /**
     * <p>This method dispatches the user request to put the device in its BT discoverable mode.</p>
     *
     * @param enabled
     *         True to enable the mode, false to disable it.
     */
    private void onUserActionDiscoverableEnabled(boolean enabled) {
        setSettingItemState(Groups.BLUETOOTH, Items.DISCOVERABLE, ItemStates.UPDATING, true);
        updateUI();
        startCallbackRunnable(Events.DISCOVERABLE_STATE);
        int timeout = 60;

        if (mDevice == null) {
            onRequestFailed(Events.DISCOVERABLE_STATE);
        }

        if (enabled) {
            if (!mDevice.startBluetoothDiscoveryMode(timeout,
                                                     new BluetoothCompletionCallback(Events.DISCOVERABLE_STATE))) {
                onRequestFailed(Events.DISCOVERABLE_STATE);
            }
        }
        else /*if (!enabled)*/ {
            if (!mDevice
                    .stopBluetoothDiscoveryMode(new BluetoothCompletionCallback(Events.DISCOVERABLE_STATE))) {
                onRequestFailed(Events.DISCOVERABLE_STATE);
            }
        }
    }

    /**
     * <p>This method dispatches the user request to enable the Bluetooth features of the device.</p>
     *
     * @param enabled
     *         True to enable the feature, false to disable it.
     */
    private void onUserActionBluetoothEnabled(boolean enabled) {
        setSettingItemState(Groups.BLUETOOTH, Items.BLUETOOTH, ItemStates.UPDATING, true);
        showOnlyBluetoothSetting(true);
        updateUI();
        startCallbackRunnable(Events.BLUETOOTH_ADAPTER_STATE);
        if (mDevice == null || !mDevice.enableBluetoothAdapter(enabled,
                                                               new BluetoothCompletionCallback(Events.BLUETOOTH_ADAPTER_STATE))) {
            onRequestFailed(Events.BLUETOOTH_ADAPTER_STATE);
        }
    }

    /**
     * <p>This methods initiates the discovery process of Bluetooth device to pair with them.</p>
     */
    private void onUserActionPairANewDevice() {
        Intent intent = new Intent(getActivity(), BluetoothOnBoardingActivity.class);
        intent.putExtra(BluetoothOnBoardingActivity.EXTRA_HOST, mHost);
        startActivity(intent);
    }


    // ========================================================================
    // DATA TO DISPLAY

    /**
     * <p>This method adds a device within the view of paired devices.</p>
     * <p>This method only updates the settings data and do not call
     * {@link SettingsAdapter#notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param deviceId
     *         The ID of the device in order to identify it when the user interacts with it.
     * @param name
     *         The name of the device to display.
     * @param address
     *         The BT address to display.
     */
    private void addPairedDevice(int deviceId, String name, String address) {
        String title = name != null && name.length() > 0 ? name : address;
        addSettingsItem(Groups.PAIRED_DEVICES, deviceId, title, "", Types.OPTIONS,
                        R.menu.bluetooth_disconnected_paired_device_menu,
                        getString(R.string.cont_desc_settings_item_bluetooth_device, title));
    }

    /**
     * <p>This method updates all the settings states and values which depends on the Bluetooth
     * Adapter state.</p>
     * <p>This method only updates the settings data and do not call
     * {@link SettingsAdapter#notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param state
     *         The <code>state</code> of the Bluetooth Adapter to update the states with.
     */
    private void updateBluetoothAdapterState(IoTBluetoothAdapterState state) {
        // update the BT state
        boolean bluetoothEnabled = state == IoTBluetoothAdapterState.Enabled;

        // update the Bluetooth adapter settings states and value
        setSettingsGroupState(Groups.BLUETOOTH, GroupStates.DISPLAYED, true);
        setSettingItemState(Groups.BLUETOOTH, Items.BLUETOOTH, ItemStates.DISPLAYED, true);
        setSettingItemState(Groups.BLUETOOTH, Items.BLUETOOTH, ItemStates.ENABLED, true);
        setSettingItemState(Groups.BLUETOOTH, Items.BLUETOOTH, ItemStates.UPDATING, false);
        setSettingsItemValue(Groups.BLUETOOTH, Items.BLUETOOTH, bluetoothEnabled);

        // hide or show all other settings depending on bluetooth being enabled
        showOnlyBluetoothSetting(!bluetoothEnabled);
    }

    /**
     * <p>This method update all the settings states and values which depends on the Discoverable
     * state.</p>
     * <p>This method only updates the settings data and do not call
     * {@link SettingsAdapter#notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param state
     *         The <code>state</code> of the Bluetooth Adapter to update the states with.
     */
    private void updateDiscoverableState(IoTBluetoothDiscoverableState state) {
        boolean enabled = state == IoTBluetoothDiscoverableState.Discoverable;
        setSettingsItemValue(Groups.BLUETOOTH, Items.DISCOVERABLE, enabled);
        setSettingItemState(Groups.BLUETOOTH, Items.DISCOVERABLE, ItemStates.UPDATING, false);
        setSettingItemState(Groups.BLUETOOTH, Items.DISCOVERABLE, ItemStates.ENABLED, true);
    }

    /**
     * <p>This method update all the settings states and values which depends on the Discoverable
     * state.</p>
     * <p>This method only updates the settings data and do not call
     * {@link SettingsAdapter#notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param devices
     *         The <code>state</code> of the Bluetooth Adapter to update the states with.
     */
    private void updatePairedDevices(List<IoTBluetoothDevice> devices) {
        // empty the data list
        removeAllSettingsFromGroup(Groups.PAIRED_DEVICES);

        // prepare the list of data
        devices.sort(mDevicesComparator);

        // update the data in this fragment and in the adapter.
        mDevices.clear();
        for (IoTBluetoothDevice device : devices) {
            int deviceId = device.hashCode();
            addPairedDevice(deviceId, device.getName(), device.getAddress());
            if (device.isConnected()) {
                updateConnectedDevice(device);
            }
            mDevices.put(deviceId, device);
        }
    }

    /**
     * <p>This method updates the connection status of the given device. It displays
     * {@link R.string#bluetooth_device_state_connected connected} as the description of the item or
     * removes the description, depending in the value of <code>connected</code>.</p>
     * <p>This method only updates the settings data and do not call
     * {@link SettingsAdapter#notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param device
     *         The Bluetooth device for which to update the connection state.
     */
    private void updateConnectedDevice(@NonNull IoTBluetoothDevice device) {
        // get the changes
        String description = device.isConnected() ? getString(R.string.bluetooth_device_state_connected) : "";
        int menu = device.isConnected() ? R.menu.bluetooth_connected_paired_device_menu
                : R.menu.bluetooth_disconnected_paired_device_menu;

        // update the UI
        int deviceId = device.hashCode();
        setSettingsItemDescription(Groups.PAIRED_DEVICES, deviceId, description);
        setSettingsItemValue(Groups.PAIRED_DEVICES, deviceId, menu);
        setSettingItemState(Groups.PAIRED_DEVICES, deviceId, ItemStates.UPDATING, false);
        updateUI();
    }

    /**
     * <p>This method hides all the settings except the one allowing to enable and disable the
     * Bluetooth Adapter.</p>
     *
     * @param only
     *         True to only display the Bluetooth setting, false to display all settings.
     */
    private void showOnlyBluetoothSetting(boolean only) {
        setSettingItemState(Groups.BLUETOOTH, Items.DISCOVERABLE, ItemStates.DISPLAYED, !only);
        setSettingsGroupState(Groups.PAIRED_DEVICES, GroupStates.DISPLAYED, !only);
        setSettingsGroupState(Groups.PAIR_A_DEVICE, GroupStates.DISPLAYED, !only);
    }


    // ========================================================================
    // DIALOGS

    /**
     * <p>This method displays a dialog informing the user that the request failed. If a similar
     * dialog is already displayed this method has no effect.</p>
     */
    private void showRequestFailedDialog() {
        if (!mBaseActivity.isDialogShown(REQUEST_FAILED_DIALOG_TAG)) {
            CustomDialogFragment dialogFragment =
                    CustomDialogFragment.newDialog(REQUEST_FAILED_DIALOG_TAG,
                                                   getString(R.string.bluetooth_request_failed_title),
                                                   getString(R.string.bluetooth_request_failed_message),
                                                   getString(R.string.ok), null);
            dialogFragment.setButtonClickedListener(null);
            mBaseActivity.showDialog(dialogFragment, REQUEST_FAILED_DIALOG_TAG);
        }
    }

    /**
     * <p>This method displays a dialog informing the user that a Bluetooth error occurred on the
     * device.</p>
     */
    private void showBluetoothErrorDialog(String address, String error) {
        String message = (address == null || address.length() <= 0) ?
                getString(R.string.bluetooth_error_message_without_device, error)
                : getString(R.string.bluetooth_error_message_with_device, address, error);
        CustomDialogFragment dialogFragment =
                CustomDialogFragment.newDialog(BLUETOOTH_ERROR_DIALOG_TAG,
                                               getString(R.string.bluetooth_error_title),
                                               message,
                                               getString(R.string.ok), null);
        mBaseActivity.showDialog(dialogFragment, BLUETOOTH_ERROR_DIALOG_TAG);
    }


    // ========================================================================
    // CALLBACKS

    /**
     * <p>This method creates and posts a {@link CallBackRunnable CallBackRunnable} for the given
     * {@link Events Events}.</p>
     *
     * @param event
     *         The event which expects to receive an update.
     */
    private void startCallbackRunnable(@Events int event) {
        synchronized (mCallbackRunnableList) {
            mCallbackRunnableList.remove(event);
            Runnable runnable = new CallBackRunnable(event);
            mCallbackRunnableList.put(event, runnable);
            mHandler.postDelayed(runnable, REQUEST_UPDATE_TIMEOUT_IN_MS);
        }
    }

    /**
     * <p>To cancel a posted {@link CallBackRunnable CallBackRunnable} which could be triggered
     * anytime for the given event.</p>
     *
     * @param event
     *         The vent to cancel the runnable for.
     */
    private void cancelCallbackRunnable(@Events int event) {
        synchronized (mCallbackRunnableList) {
            Runnable runnable = mCallbackRunnableList.remove(event);
            if (runnable != null) {
                mHandler.removeCallbacks(runnable);
            }
        }
    }


    // ========================================================================
    // REQUEST FAIL

    /**
     * <p>This method calls {@link #onRequestFailed(int, int) onRequestFailed(int, int}} with the
     * <code>message</code> parameter set up using
     * {@link #getRequestFailedMessageFromEvent(int) getRequestFailedMessageFromEvent(int)}</p>
     *
     * @param event
     *         The expected event for which the request has failed.
     */
    private void onRequestFailed(@Events int event) {
        onRequestFailed(event, getRequestFailedMessageFromEvent(event));
    }

    /**
     * <p>This method cancels any {@link CallBackRunnable CallBackRunnable} corresponding to the
     * event, displays an error toast and resets the UI in the UI thread using
     * {@link #updateInUiThread() updateInUiThread()}.</p>
     *
     * @param event
     *         The expected event for which the request has failed.
     * @param message
     *         the toast message to display within the UI.
     */
    private void onRequestFailed(@Events int event, int message) {
        cancelCallbackRunnable(event);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        // resetting the UI within the UI thread as this can be called from another thread
        updateInUiThread();
    }

    /**
     * <p>To get a message to display within the UI when the requests for the given <code>event</code>
     * fails.</p>
     *
     * @param event
     *         the event to get a human readable message for when its request failed.
     *
     * @return the corresponding human readable message.
     */
    private static int getRequestFailedMessageFromEvent(@Events int event) {
        switch (event) {
            case Events.BLUETOOTH_ADAPTER_STATE:
                return R.string.toast_bluetooth_set_adapter_state_failed;
            case Events.CONNECTED_DEVICE:
                return R.string.toast_bluetooth_set_connection_state_failed;
            case Events.DISCOVERABLE_STATE:
                return R.string.toast_bluetooth_set_discoverable_state_failed;
            case Events.PAIRED_DEVICES:
                return R.string.toast_bluetooth_unpairing_failed;

            case Events.BLUETOOTH_ERROR:
                // only used to update the UI when an error is received
                // this should not be triggered here
                break;
        }

        return R.string.toast_bluetooth_request_failed;
    }


    // ========================================================================
    // INNER CLASSES

    /**
     * <p>A Runnable for when this fragment excepts a callback and/or an update from the controller
     * after applying of a user event.</p>
     * <p>When run, this Runnable removes itself from the list of awaiting Runnable and displays a
     * toast.</p>
     * <p>This Runnable exists to avoid to block one of the settings in an {@link ItemStates#UPDATING
     * UPDATING} states for a missing callback/update from the backend controller.</p>
     */
    private class CallBackRunnable implements Runnable {

        /**
         * <p>To know to which event the callback/update is expected for.</p>
         */
        private @Events int mmEvent;

        /**
         * <p>To build a new instance of {@link CallBackRunnable CallBackRunnable}.</p>
         *
         * @param event
         *         The event this Runnable is related to.
         */
        CallBackRunnable(@Events int event) {
            mmEvent = event;
        }

        @Override // Runnable
        public void run() {
            synchronized (mCallbackRunnableList) {
                mCallbackRunnableList.remove(mmEvent);
                Log.w(TAG, "[CallBackRunnable->run()] No update received for event " + mmEvent);
                onRequestFailed(mmEvent, R.string.toast_bluetooth_request_time_out);
            }
        }
    }

    /**
     * <p>This class implements {@link IoTCompletionCallback IoTCompletionCallback} to provide when
     * sending requests to the controller.</p>
     */
    private class BluetoothCompletionCallback implements IoTCompletionCallback {

        /**
         * <p>To know to which event the callback is expected for.</p>
         */
        private @Events int mmEvent;

        /**
         * <p>To build a new instance of {@link BluetoothCompletionCallback
         * BluetoothCompletionCallback}.</p>
         *
         * @param event
         *         The event this Callback is related to.
         */
        BluetoothCompletionCallback(@Events int event) {
            mmEvent = event;
        }

        @Override // IoTCompletionCallback
        public void onCompletion(boolean success) {
            if (!success) {
                onRequestFailed(mmEvent);
            }
        }
    }

}