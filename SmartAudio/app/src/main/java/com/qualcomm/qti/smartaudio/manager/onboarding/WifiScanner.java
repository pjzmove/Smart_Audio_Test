/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.models.Capabilities;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides the APIs to start and stop scanning for Wifi networks. This scanner also gets the result and
 * provides them to the attached {@link WifiScannerListener WifiScannerListener}.
 */
public class WifiScanner {

    /**
     * The tag to use to log information.
     */
    private static final String TAG = "WifiScanner";
    /**
     * The listener to inform about any result.
     */
    private WifiScannerListener mListener;
    /**
     * To know if this scanner is in scanning mode. Being in scanning mode means that this scanner has requested the
     * system to scan and is continuously listening for some results.
     */
    private boolean mIsScanning = false;
    /**
     * The system wifi manager in order to start scanning and receive scan results.
     */
    private final WifiManager mWifiManager;

    /**
     * To get the system to broadcast the results to this scanner.
     */
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                // Received intent with unexpected null action
                return;
            }

            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                onScanResultsAvailable(success);
            }
        }
    };

    /**
     * All the scanning errors which can happen.
     */
    @IntDef(flag = true, value = { WifiScannerError.SCAN_FAILED, WifiScannerError.STARTING_SCAN_FAILED })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface WifiScannerError {
        /**
         * This occurs when the system informs that it could not update the results. This can be for any of the
         * following reasons:
         * <ol>
         *     <li>The app has requested to many scans and needs to wait before asking again.</li>
         *     <li>The device is idle and scanning is disabled.</li>
         *     <li>The hardware has reported a scan failure.</li>
         * </ol>
         */
        int SCAN_FAILED = 0;
        /**
         * This occurs as a result of requesting the system to scan.
         */
        int STARTING_SCAN_FAILED = 1;
    }

    /**
     * To build o new instance of this scanner.
     *
     * @param context The context of the application in order to register and unregister a broadcast receiver and access
     * the {@link WifiManager WifiManager}.
     */
    public WifiScanner(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * <p>This method puts the scanner in scanning mode by listening for scan updates from the system.</p>
     * <p>This method requests the system to scan for Wifi networks.</p>
     *
     * @param listener The listener this scanner will notify about any event related to wifi scanning.
     */
    public boolean startScanning(Context context, WifiScannerListener listener) {
        mListener = listener;

        if (!mIsScanning) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(mWifiReceiver, intentFilter);
            mIsScanning = true;
        }

        return refresh();
    }

    /**
     * <p>To request the system to refresh the list of scanned networks by scanning for networks.</p>
     *
     * @return returns True if the system could start the scan for the application, false otherwise.<br/><i>Note: the
     * system can refuse the request if the application has requested this too many times within a certain time.</i>
     */
    public boolean refresh() {
        return mIsScanning && mWifiManager.startScan(); // TODO: /!\ this will be deprecated in API 29
    }

    /**
     * <p>This method stops this scanner to listen for any incoming wifi scan results. To do so it unregisters any
     * registered broadcast receiver.</p>
     */
    public void stopScanning(Context context) {
        if (mIsScanning) {
            context.unregisterReceiver(mWifiReceiver);
            mIsScanning = false;
            mListener = null;
        }
    }

    /**
     * <p>This method retrieves the latest list of scanned networks from the system and parses them into
     * {@link WifiNetwork WifiNetwork} objects.</p>
     *
     * @return the list of scanned wifi networks.
     */
    public List< WifiNetwork> getNetworks() {
        List<ScanResult> results = mWifiManager.getScanResults();
        List< WifiNetwork> networks = new ArrayList<>();

        for (ScanResult result : results) {
            WifiNetwork network = new WifiNetwork(result.SSID, result.level, checkIsOpen(result.capabilities));
            if (network.isOpenNetwork()) {
                WifiNetwork.addNetwork(networks, network);
            }
        }

        return networks;
    }

    /**
     * <p>This method checks the capabilities of a {@link ScanResult ScanResult} to determine if they describe an open
     * network.</p>
     * <p>If the capabilities contain {@link Capabilities#ESS ESS} and do not contain any security types, see
     * {@link Capabilities#SECURITY_TYPES SECURITY_TYPES}, it is then known as open.</p>
     *
     * @param capabilities The capabilities to check.
     *
     * @return True if the capabilities describes an open network, false otherwise.
     */
    private static boolean checkIsOpen(String capabilities) {
        if (capabilities == null) {
            return false;
        }

        for (String capability : Capabilities.SECURITY_TYPES) {
            if (capabilities.toUpperCase().contains(capability)) {
                return false;
            }
        }
        return capabilities.toUpperCase().contains(Capabilities.ESS);
    }

    /**
     * <p>This method is called when the receiver is notified by the system of an update regarding the Wifi scan
     * results.</p>
     * <p>This method checks if the update was successful and if it was gets the latest results and sends them to
     * its listener. If it was unsuccessful, this method informs any listener about the unsuccess.</p>
     *
     * @param success True if the update of the networks was successful.
     */
    private void onScanResultsAvailable(boolean success) {
        if (success) {
            List<WifiNetwork> networks = getNetworks();
            if (mListener != null) mListener.onNetworksUpdated(networks);
        }
        else {
            Log.w(TAG, "[onScanResultsAvailable] Unsuccessful scan.");
            if (mListener != null) mListener.onError(WifiScannerError.SCAN_FAILED);
        }
    }

    /**
     * A listener to be notified about scanned Wifi networks and/or scanning errors.
     */
    public interface WifiScannerListener {

        /**
         * <p>This is called when the list of scanned wifi networks has been updated.</p>
         *
         * @param networks The new list of Wifi networks.
         */
        void onNetworksUpdated(List<WifiNetwork> networks);

        /**
         * <p>This is called when a scanning issue has occurred.</p>
         *
         * @param error The error which occurred.
         */
        void onError(@WifiScannerError int error);
    }
}
