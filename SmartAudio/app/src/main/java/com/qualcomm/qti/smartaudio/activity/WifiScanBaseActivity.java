/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.ref.WeakReference;
import java.util.List;

public abstract class WifiScanBaseActivity extends BaseActivity {

    private final static String TAG = "WifiScan";

    public final static long ENABLE_NETWORK_TIMEOUT = 8000;
    protected final static String WIFI_SCAN_FRAGMENT_TAG = "WIFI_SCAN";

    protected WifiManager mWifiManager;
    protected ConnectivityManager mConnectivityManager;
    protected String mCurrentSsid;
    protected String mHomeApSsid;
    protected Handler mUiHandler;
    protected WeakReference<CustomDialogFragment> mProgressDialogRef;
    protected ConnectivityManager.NetworkCallback mNetworkCallback;
    protected Network mNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiManager = (WifiManager) mApp.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }


        mConnectivityManager = (ConnectivityManager) mApp.getApplicationContext().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);


        mIsListeningConnectivity = false;

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        if (wifiInfo != null && info != null && info.isConnected()) {
            mHomeApSsid = Utils.stripSSIDQuotes(wifiInfo.getSSID());
        }

    }

    protected BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    handleScanResult();
                }
                else {
                    Log.e(TAG, "wifi scan received error!");
                }
                dismissWifiScanDialog();
            }
            else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                handleConnectivity(c, action);
            }
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                handleConnectivity(c, action);
            }
            else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                handleConnectivity(c, action);
            }
        }
    };

    protected void onDestroy() {
        unregisterNetworkCallback(mNetworkCallback);

        if (mProgressDialogRef != null && mProgressDialogRef.get() != null) {
            mProgressDialogRef.clear();
        }

        disconnectWifiNetwork();
        unregisterReceiver(mWifiScanReceiver);
        super.onDestroy();
    }

    protected WifiConfiguration findConfiguration(String ssid) {
        if ((ssid == null) || (ssid.isEmpty())) {
            return null;
        }
        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        if (configurations == null) {
            return null;
        }
        for (WifiConfiguration configuration : configurations) {
            if (ssid.equals(Utils.stripSSIDQuotes(configuration.SSID))) {
                return configuration;
            }
        }

        return null;
    }

    protected final Runnable mEnableOnboardeeRunnable = new Runnable() {
        @Override
        public void run() {
            {
                WifiConfiguration wifiConfiguration = findConfiguration(mCurrentSsid);
                if (wifiConfiguration != null) {
                    boolean result = mWifiManager.enableNetwork(wifiConfiguration.networkId, true);
                    Log.d(TAG,
                          "[EnableOnboardeeRunnable] enableNetwork networkID=" + wifiConfiguration.networkId
                                  + " [true] status=" + result);

                    result = mWifiManager.reconnect();
                    Log.d(TAG, "[EnableOnboardeeRunnable] reconnect networkID=" + wifiConfiguration.networkId
                            + " status=" + result);
                    mUiHandler.postDelayed(mEnableOnboardeeRunnable, ENABLE_NETWORK_TIMEOUT);
                }
            }
        }
    };

    public WeakReference<CustomDialogFragment> showWifiScanDialog(String title, String message) {
        mProgressDialogRef = showProgressDialog(WIFI_SCAN_FRAGMENT_TAG, title, message);
        return mProgressDialogRef;
    }

    /*
     * Route all network requests through WiFi when connected to a specific network of your choice
     *
     **/
    public void routeWifiTraffic(String ssid) {

        Log.d(TAG, "Routing traffic to the Wi-Fi:" + ssid);
        unregisterNetworkCallback(mNetworkCallback);

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {

            public void onAvailable(Network network) {

                String networkName = ssid.replaceAll("\"", "");
                if (getNetworkSsid().equalsIgnoreCase(networkName)) {
                    clearNetworkRoute();
                    setNetworkRoute(network);
                    mNetwork = network;
                }
                else {
                    clearNetworkRoute();
                    mNetwork = null;

                }
            }
        };

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        NetworkRequest request =
                builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI).removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
        mConnectivityManager.registerNetworkCallback(request, mNetworkCallback);
    }

    protected void unregisterNetworkCallback(ConnectivityManager.NetworkCallback networkCallback) {
        if (networkCallback != null) {
            try {
                mConnectivityManager.unregisterNetworkCallback(networkCallback);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mNetworkCallback = null;
            }
        }
    }

    private boolean clearNetworkRoute() {
        Boolean retVal = true;
        if (Build.VERSION.SDK_INT >= 23) {
            retVal = mConnectivityManager.bindProcessToNetwork(null);
        }
        else if (Build.VERSION.SDK_INT >= 21) {
            retVal = mConnectivityManager.setProcessDefaultNetwork(null);
        }
        return retVal;
    }

    private String getNetworkSsid() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            return wifiInfo.getSSID().replaceAll("\"", "");
        }
        return "";
    }

    private boolean setNetworkRoute(Network network) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= 23) {
            retVal = mConnectivityManager.bindProcessToNetwork(network);
        }
        else if (Build.VERSION.SDK_INT >= 21) {
            retVal = mConnectivityManager.setProcessDefaultNetwork(network);
        }

        return retVal;
    }

    public void dismissWifiScanDialog() {
        dismissDialog(WIFI_SCAN_FRAGMENT_TAG);
    }

    public void disconnectWifiNetwork() {
        if (mCurrentSsid != null) {
            WifiConfiguration wifiConfig = findConfiguration(mCurrentSsid);

            if (wifiConfig != null) {
                mWifiManager.removeNetwork(wifiConfig.networkId);
            }
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }

    abstract void handleScanResult();

    abstract void handleConnectivity(Context context, String action);
}
