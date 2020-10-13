/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.adapter.BluetoothOnBoardingDeviceListAdapter;
import com.qualcomm.qti.smartaudio.adapter.BluetoothOnBoardingDeviceListAdapter.BluetoothOnBoardingDeviceListAdapterListener;
import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment;
import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment.OnCustomDialogButtonClickedListener;
import com.qualcomm.qti.smartaudio.manager.IoTSysManager.OnBluetoothListener;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothAdapterState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTBluetoothDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothError;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * <p>This class manages the display of a list of IoT Bluetooth devices in order to pair with them.
 * This class also manages the pairing process with the device selected by a user.</p>
 */
public class BluetoothOnBoardingActivity extends BaseActivity implements
        BluetoothOnBoardingDeviceListAdapterListener {

    // ========================================================================
    // FIELDS

    /**
     * The tag to use when displaying logs from this activity.
     */
    private final static String TAG = "BluetoothOnBoardingActivity";
    /**
     * The device which is managed through this fragment.
     */
    private IoTDevice mDevice;
    /**
     * The key to put the host name of the device in the arguments.
     */
    public static final String EXTRA_HOST = "HOST_NAME";
    /**
     * The device which is pairing, this is null if no device has been selected yet.
     */
    private IoTBluetoothDevice mPairingDevice;
    /**
     * The view to display the title for this activity.
     */
    private TextView mViewTitle;
    /**
     * The view to display when no devices can be found.
     */
    private View mListEmptyMessageView;
    /**
     * The layout which is in charge of the "pull to refresh" feature.
     */
    private SwipeRefreshLayout mRefreshLayout;
    /**
     * The adapter to manage the display of the IoT Bluetooth devices list.
     */
    private final BluetoothOnBoardingDeviceListAdapter mDevicesListAdapter = new BluetoothOnBoardingDeviceListAdapter(
            this);
    /**
     * <p>This is a listener to be notified when the Bluetooth states change.</p>
     */
    OnBluetoothListener mOnBluetoothListener = new OnBluetoothListener() {
        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothAdapterStateChanged(IoTDevice device,
                                                   IoTDevice.IoTBluetoothAdapterState state) {
            if (mDevice != null && mDevice.equals(device) && state == IoTBluetoothAdapterState.Disabled) {
                // the Bluetooth has been disabled on the IoT device => error
                updateInUiThread(Events.BLUETOOTH_DISABLED);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothDiscoverableStateChanged(IoTDevice device,
                                                        IoTDevice.IoTBluetoothDiscoverableState state) {
            // no action
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onPairedDevicesUpdated(IoTDevice device, List<IoTBluetoothDevice> devices) {
            // no action
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothError(IoTDevice device, IoTBluetoothError error) {
            if (mDevice != null && mDevice.equals(device)) {
                // a Bluetooth error occurred with the IoT device
                // dismissing the pairing dialog if displayed
                updateInUiThread(Events.DISMISS_DIALOG, Dialogs.PAIRING_DIALOG);
                // displaying error
                String message = (error.getAddress() == null || error.getAddress().length() <= 0) ?
                        getString(R.string.bluetooth_error_message_without_device, error.getStatus())
                        : getString(R.string.bluetooth_error_message_with_device, error.getAddress(),
                                    error.getStatus());
                updateInUiThread(Events.SHOW_ERROR_DIALOG, getString(R.string.bluetooth_error_title),
                                 message);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onConnectedBluetoothDeviceChanged(IoTDevice device,
                                                      IoTBluetoothDevice bluetoothDevice) {
            // no action
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothScanStateChanged(IoTDevice device, boolean scanning) {
            updateInUiThread(Events.UPDATE_SCANNING_STATE, scanning);
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothDeviceDiscovered(IoTDevice device, IoTBluetoothDevice scanned) {
            if (mDevice != null && mDevice.equals(device)) {
                updateInUiThread(Events.ADD_BLUETOOTH_DEVICE, scanned);
            }
        }

        @Override // IoTSysManager.OnBluetoothListener
        public void onBluetoothPairStateUpdated(IoTDevice device, IoTBluetoothDevice pairDevice, boolean paired) {
            if (mDevice != null && mDevice.equals(device)
                    && mPairingDevice != null && mPairingDevice.equals(pairDevice)) {
                // dismissing the pairing dialog if displayed
                updateInUiThread(Events.DISMISS_DIALOG, Dialogs.PAIRING_DIALOG);
                if (paired) {
                    updateInUiThread(Events.SHOW_PAIRED_DIALOG, pairDevice);
                }
                else {
                    // device not paired => error
                    updateInUiThread(Events.SHOW_ERROR_DIALOG, getString(R.string.bluetooth_error_title),
                                     getString(R.string.bluetooth_pairing_failed_message,
                                               getDeviceDisplayName(pairDevice)));
                }
            }
        }
    };


    // ========================================================================
    // ENUMERATIONS

    /**
     * All the tags to identify the different (custom) dialogs displayed by this activity.
     */
    @StringDef({Dialogs.BLUETOOTH_ERROR_DIALOG, Dialogs.DEVICE_PAIRED_DIALOG,
            Dialogs.PAIRING_DIALOG})
    @Retention(RetentionPolicy.SOURCE)
    @interface Dialogs {

        /**
         * The tag to identify the dialog which informs the user that the request has failed.
         */
        String BLUETOOTH_ERROR_DIALOG = "BLUETOOTH_ERROR_DIALOG";
        /**
         * The tag to identify the dialog which informs the user that the device is paired.
         */
        String DEVICE_PAIRED_DIALOG = "DEVICE_PAIRED_DIALOG";
        /**
         * The tag to identify the dialog which informs the user that the device is pairing.
         */
        String PAIRING_DIALOG = "PAIRING_DIALOG";
    }

    /**
     * All instructions to be executed in the UI thread using
     * {@link #updateInUiThread(Object...) updateInUiThread(Object...)}. The events are used as the
     * first parameter of the method. When calling that method, the parameters require for the
     * instructions are also passed to the method.
     */
    @IntDef({Events.SHOW_ERROR_DIALOG, Events.SHOW_PAIRED_DIALOG, Events.ADD_BLUETOOTH_DEVICE,
            Events.UPDATE_SCANNING_STATE, Events.DISMISS_DIALOG, Events.SCAN_FAILED,
            Events.BLUETOOTH_DISABLED, Events.PAIRING_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    @interface Events {

        /**
         * <p>This event is to display a dialog about an error which occurs.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = A {@link String String} title for the dialog.</li>
         * <li><code>arguments[1]</code> = A {@link String String} message for the dialog.</li>
         * </ul></p>
         */
        int SHOW_ERROR_DIALOG = 0;
        /**
         * <p>This event is to display a dialog to indicate the successful state of pairing a device
         * with the IoTDevice.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = the {@link IoTBluetoothDevice IoTBluetoothDevice}
         * which has been paired.</li>
         * </ul></p>
         */
        int SHOW_PAIRED_DIALOG = 1;
        /**
         * <p>This event is to add a Bluetooth device in the list of displayed devices which can be
         * paired.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = the {@link IoTBluetoothDevice IoTBluetoothDevice}
         * to add to the list.</li>
         * </ul></p>
         */
        int ADD_BLUETOOTH_DEVICE = 2;
        /**
         * <p>This event is to update the display of the scanning state in the UI.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = a {@link Boolean Boolean} sets to True if scanning is in
         * progress, Flse otherwise.</li>
         * </ul></p>
         */
        int UPDATE_SCANNING_STATE = 3;
        /**
         * <p>This event is to dismiss the dialog which corresponds to the given tag.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = the {@link Dialogs Dialogs} tag of the dialog to dismiss.</li>
         * </ul></p>
         */
        int DISMISS_DIALOG = 4;
        /**
         * <p>This event is to inform the user that scanning for BLuetooth devices has failed.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * </ul></p>
         */
        int SCAN_FAILED = 5;
        /**
         * <p>This event is to inform the user that the Bluetooth has been disabled on the IoTDevice.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * </ul></p>
         */
        int BLUETOOTH_DISABLED = 6;
        /**
         * <p>This event is to inform the user that the pairing with an IoT Bluetooth device has
         * failed.</p>
         * <p>When used with {@link #updateInUiThread(Object...) updateInUiThread(Object...)}, this
         * event as the following parameters:
         * <ul>
         * <li><code>arguments[0]</code> = SHOW_ERROR_DIALOG</li>
         * <li><code>arguments[1]</code> = the {@link IoTBluetoothDevice IoTBluetoothDevice}
         * with which the pairing failed.</li>
         * </ul></p>
         */
        int PAIRING_FAILED = 7;
    }


    // ========================================================================
    // UI

    @Override // BaseActivity
    protected void update(Object... arguments) {
        if (arguments.length < 1 || !(arguments[0] instanceof Integer)) {
            // unexpected arguments
            return;
        }

        @Events int event = (int) arguments[0];

        switch (event) {
            case Events.SHOW_ERROR_DIALOG:
                String title =
                        arguments.length >= 2 && arguments[1] instanceof String ? (String) arguments[1] : "";
                String message =
                        arguments.length >= 3 && arguments[2] instanceof String ? (String) arguments[2] : "";
                showErrorDialog(title, message);
                break;

            case Events.ADD_BLUETOOTH_DEVICE:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothDevice) {
                    if (mDevicesListAdapter.getItemCount() == 0) {
                        showEmptyListMessage(false);
                    }
                    mDevicesListAdapter.add((IoTBluetoothDevice) arguments[1]);
                }
                break;

            case Events.SHOW_PAIRED_DIALOG:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothDevice) {
                    dismissDialog(Dialogs.PAIRING_DIALOG);
                    showDevicePairedDialog((IoTBluetoothDevice) arguments[1]);
                }
                break;

            case Events.UPDATE_SCANNING_STATE:
                if (arguments.length >= 2 && arguments[1] instanceof Boolean) {
                    mRefreshLayout.setRefreshing((boolean) arguments[1]);
                }
                break;

            case Events.DISMISS_DIALOG:
                if (arguments.length >= 2 && arguments[1] instanceof String) {
                    dismissDialog((String) arguments[1]);
                }
                break;
            case Events.SCAN_FAILED:
                onScanFailed();
                break;

            case Events.BLUETOOTH_DISABLED:
                dismissDialog(Dialogs.PAIRING_DIALOG);
                showErrorDialog(getString(R.string.bluetooth_error_title),
                                getString(R.string.bluetooth_off_error_message));
                break;

            case Events.PAIRING_FAILED:
                if (arguments.length >= 2 && arguments[1] instanceof IoTBluetoothDevice) {
                    onPairingFailed((IoTBluetoothDevice) arguments[1]);
                }
                break;
        }
    }

    @Override // BaseActivity
    public void dismissDialog(@Dialogs String dialog) {
        if (isDialogShown(dialog)) {
            super.dismissDialog(dialog);
        }
    }

    /**
     * <p>To display a message indicating that the list is empty and devices are looked fo.</p>
     *
     * @param show
     *         True to display the message, false otherwise.
     */
    private void showEmptyListMessage(boolean show) {
        mListEmptyMessageView.setVisibility(show ? View.VISIBLE : View.GONE);
        mViewTitle.setText(show ? R.string.bluetooth_add_device : R.string.bluetooth_devices_found);
    }

    /**
     * <p>To initialise the content of the message displayed when the list is empty.</p>
     */
    private void initEmptyListMessage() {
        // no image
        View imageView = mListEmptyMessageView.findViewById(R.id.empty_view_icon);
        imageView.setVisibility(View.GONE);
        // title
        TextView titleView = mListEmptyMessageView.findViewById(R.id.empty_view_text);
        titleView.setText(R.string.bluetooth_empty_list_title);
        // details
        TextView detailsView = mListEmptyMessageView.findViewById(R.id.empty_view_detail_text);
        detailsView.setText(R.string.bluetooth_empty_list_details);
    }

    /**
     * <p>To inform the user that the scan has failed. This method updates the scanning state and
     * displays an error dialog to inform the user.</p>
     */
    private void onScanFailed() {
        mRefreshLayout.setRefreshing(false);
        showErrorDialog(getString(R.string.bluetooth_scan_error_title),
                        getString(R.string.bluetooth_scan_error_message));
    }

    /**
     * <p>To get a displayed name for the given Bluetooth device.</p>
     * <p>This method builds the displayed name based on the device name, if that one is empty it uses
     * the Bluetooth address of the device as the display name.</p>
     *
     * @param device
     *         the device to get the displayed name from.
     *
     * @return the displayed name of the device.
     */
    private static String getDeviceDisplayName(IoTBluetoothDevice device) {
        if (device == null) {
            return "";
        }

        String name = device.getName();
        name = name == null || name.length() < 1 ? device.getAddress() : name;
        return name == null ? "" : name;
    }

    /**
     * <p>To display a "pairing failed" dialog to inform the user that the pairing has failed.</p>
     *
     * @param device
     *         the device with which the pairing has failed.
     */
    private void onPairingFailed(IoTBluetoothDevice device) {
        dismissDialog(Dialogs.PAIRING_DIALOG);
        showErrorDialog(getString(R.string.bluetooth_error_title),
                        getString(R.string.bluetooth_pairing_failed_message, getDeviceDisplayName(device)));
    }


    // ========================================================================
    // ACTIVITY

    @Override // Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);

        // initialise the action bar
        Toolbar toolbar = findViewById(R.id.bluetooth_scan_app_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // bind components
        RecyclerView recyclerView = findViewById(R.id.bluetooth_scan_devices_list);
        mViewTitle = findViewById(R.id.bluetooth_app_bar_title);
        mListEmptyMessageView = findViewById(R.id.bluetooth_scan_empty_list_message);
        View appBarBackButton = toolbar.findViewById(R.id.bluetooth_app_bar_back_button);
        mRefreshLayout = findViewById(R.id.bluetooth_scan_refresh_layout);

        // set up default values
        mViewTitle.setText(R.string.bluetooth_add_device);
        mViewTitle.setContentDescription(getString(R.string.cont_desc_screen_bluetooth_on_boarding));

        // set up the UI components for the list of devices
        // use a linear layout manager for the recycler view
        LinearLayoutManager devicesListLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(devicesListLayoutManager);
        recyclerView.setHasFixedSize(true);
        // specify an adapter for the recycler view
        recyclerView.setAdapter(mDevicesListAdapter);
        // set up an item divider
        DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),
                                                                     devicesListLayoutManager.getOrientation());
        recyclerView.addItemDecoration(decoration);

        appBarBackButton.setOnClickListener(view -> finish());

        // set up swipe to refresh
        mRefreshLayout.setOnRefreshListener(() -> {
            mDevicesListAdapter.reset();
            showEmptyListMessage(true);
            startDeviceBluetoothScan();
        });

        // set up the message for when the list is empty
        initEmptyListMessage();
    }

    @Override // Activity
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();

        if (intent == null || mApp == null || !mApp.isInit()) {
            Log.w(TAG, "[onResume] process cannot be initiated: intent null or app not initialised.");
            showErrorDialog(getString(R.string.bluetooth_error_title),
                            getString(R.string.bluetooth_iot_device_error_message));
            return;
        }

        mDevice = mAllPlayManager.getDeviceByHostName(intent.getStringExtra(EXTRA_HOST));
        mIoTSysManager.addOnBluetoothListener(mOnBluetoothListener);

        if (mDevice == null) {
            Log.w(TAG, "[onResume] process cannot be initiated: no IoTDevice.");
            showErrorDialog(getString(R.string.bluetooth_error_title),
                            getString(R.string.bluetooth_iot_device_error_message));
            return;
        }

        startDeviceBluetoothScan();
    }

    @Override // Activity
    protected void onPause() {
        super.onPause();
        stopDeviceBluetoothScan();
    }


    // ========================================================================
    // IOT SYS REQUESTS

    /**
     * <p>To request the IoTDevice to scan for Bluetooth devices.</p>
     */
    private void startDeviceBluetoothScan() {
        mRefreshLayout.setRefreshing(true);
        if (mDevice == null || !mDevice.startBluetoothScan(mScanCallback)) {
            onScanFailed();
        }
    }

    /**
     * <p>To request the IoTDevice to stop to scan for Bluetooth devices.</p>
     */
    private void stopDeviceBluetoothScan() {
        if (mDevice != null) {
            mDevice.stopBluetoothScan(null);
        }
    }

    /**
     * <p>The callback to give to the IoT system when requesting the Bluetooth scan to start.</p>
     */
    private IoTCompletionCallback mScanCallback = success -> {
        if (!success) {
            updateInUiThread(Events.SCAN_FAILED);
        }
    };

    /**
     * <p>To request the IoTDevice to pair with the given device.</p>
     * <p>This method also displays an in progress dialog to inform the user.</p>
     *
     * @param device
     *         the device to pair with.
     */
    private void pairDevice(IoTBluetoothDevice device) {
        showProgressDialog(Dialogs.PAIRING_DIALOG, getString(R.string.bluetooth_pairing), null);
        mPairingDevice = device;
        if (device == null || mDevice == null
                || !mDevice.pairBluetoothDevice(device, new PairingCallback(device))) {
            onPairingFailed(device);
        }
    }

    /**
     * <p>The callback to give to the IoT system when requesting to pair with a Bluetooth device.</p>
     */
    private class PairingCallback implements IoTCompletionCallback {

        /**
         * <p>the device with which the IoT system is pairing.</p>
         */
        private final IoTBluetoothDevice mmDevice;

        /**
         * <p>To build a new instance of PairingCallback.</p>
         *
         * @param device
         *         the device the IoT system is pairing with.
         */
        private PairingCallback(IoTBluetoothDevice device) {
            mmDevice = device;
        }

        @Override // IoTCompletionCallback
        public void onCompletion(boolean success) {
            if (!success) {
                updateInUiThread(Events.PAIRING_FAILED, mmDevice);
            }
        }
    }


    // ========================================================================
    // USER ACTIONS

    @Override // BluetoothOnBoardingDeviceListAdapterListener
    public void onItemSelected(IoTBluetoothDevice device) {
        if (device == null) {
            Log.w(TAG, "[onItemSelected] device is null");
            updateInUiThread(Events.SHOW_ERROR_DIALOG, getString(R.string.bluetooth_request_failed_title),
                             getString(R.string.bluetooth_device_not_found_error_message));
            return;
        }

        pairDevice(device);
    }


    // ========================================================================
    // DIALOGS

    /**
     * <p>This method displays a dialog informing the user about an error which occurred. If a similar
     * dialog is already displayed, calling this method has no effect.</p>
     *
     * @param title
     *         the title to display in the dialog.
     * @param message
     *         the message to display in the dialog.
     */
    private void showErrorDialog(String title, String message) {
        if (!isDialogShown(Dialogs.BLUETOOTH_ERROR_DIALOG)) {
            CustomDialogFragment dialogFragment =
                    CustomDialogFragment.newDialog(Dialogs.BLUETOOTH_ERROR_DIALOG,
                                                   title,
                                                   message,
                                                   getString(R.string.ok), null);
            dialogFragment.setButtonClickedListener(null);
            showDialog(dialogFragment, Dialogs.BLUETOOTH_ERROR_DIALOG);
        }
    }

    /**
     * <p>This method displays a dialog informing the user that the device has been successfully
     * paired.</p>
     *
     * @param device
     *         The device which had been paired.
     */
    private void showDevicePairedDialog(IoTBluetoothDevice device) {
        String message = getString(R.string.bluetooth_paired, getDeviceDisplayName(device));
        CustomDialogFragment dialogFragment =
                CustomDialogFragment.newDoneDialog(Dialogs.DEVICE_PAIRED_DIALOG,
                                                   null,
                                                   message, getString(R.string.ok));
        dialogFragment.setButtonClickedListener(new OnCustomDialogButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked(String tag) {
                // process has successfully ended and users knows it, this activity can be terminated
                finish();
            }

            @Override
            public void onNegativeButtonClicked(String tag) {
                // no negative button
            }
        });
        dialogFragment.setCancelable(false);
        showDialog(dialogFragment, Dialogs.DEVICE_PAIRED_DIALOG);
    }

}