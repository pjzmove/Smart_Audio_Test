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
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qualcomm.qti.smartaudio.model.WifiNetwork;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>This class allows to connect to a device temporarily. Once the connection is not required anymore, the
 * {@link #release(Context)} method must be called in order to restore the previous connection and release any system
 * resources. Then the connector can be discarded.</p>
 * <p>In order to keep a connection in a stable way, this manager acts as follows:
 * <ol>
 *     <li>Delete any configured network which corresponds to the network</li>
 *     <li>Creates a new configuration for the network</li>
 *     <li>Connects to the network</li>
 *     <li>Routes the traffic to the network as the system - the network could have no Internet resulting in the
 *     system routing the traffic to the phone network.</li>
 * </ol></p>
 */
public class WifiConnector {

    // ========================================================================
    // CONSTANTS

    /**
     * The tag to use to display logs from this class.
     */
    private static final String TAG = "WifiConnector";
    /**
     * The default value of a null SSID.
     */
    private static final String NULL_SSID = "";
    /**
     * The boolean to use to display logs.
     */
    private static final boolean DEBUG = com.qualcomm.qti.smartaudio.DEBUG.WifiConnector.DEBUG;


    // ========================================================================
    // FIELDS
    /**
     * A Handler in order to inform the listeners about events after this connector has finished its current process.
     */
    private final Handler mHandler = new Handler();
    /**
     * The list of listeners which are listening for connection events.
     */
    private final List<WifiConnectorListener> mListeners = new CopyOnWriteArrayList<>();
    /**
     * The system wifi manager in order to connect to a wifi network.
     */
    private final WifiManager mWifiManager;
    /**
     * The system connectivity manager in order to manage the current connected network.
     */
    private ConnectivityManager mConnectivityManager;
    /**
     * The network this connector should connect/is connected with.
     */
    private WifiNetwork mExpectedNetwork = null;
    /**
     * The initial network which was connected before this connector altered the Wifi connection.
     */
    private final WifiConfiguration mPreviousNetwork;
    /**
     * The current connection state. This is used to avoid to not dispatch the connection state for each network
     * update received from the system.
     */
    private @ConnectionState int mState = ConnectionState.DISCONNECTED;
    /**
     * To get the system to broadcast the network updates to this connector.
     */
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                // get all information from intent and system
                boolean hasNetworkInfo = intent.hasExtra(WifiManager.EXTRA_NETWORK_INFO);
                boolean hasWifiInfo = intent.hasExtra(WifiManager.EXTRA_WIFI_INFO);
                NetworkInfo networkInfoFresh = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                WifiInfo wifiInfoFresh = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                NetworkInfo networkInfoSystem = mConnectivityManager.getActiveNetworkInfo();
                WifiInfo wifiInfoSystem = mWifiManager.getConnectionInfo();

                // set the most accurate information
                NetworkInfo networkInfo = hasNetworkInfo ? networkInfoFresh : networkInfoSystem;
                WifiInfo wifiInfo = hasWifiInfo ? wifiInfoFresh : wifiInfoSystem;

                // send the update with the most accurate information about the state change
                onNetworkStateChanged(networkInfo, wifiInfo);
            }
        }
    };


    // ========================================================================
    // ENUMERATIONS

    /**
     * <p>The values for the connection state.</p>
     */
    @IntDef({ ConnectionState.CONNECTED, ConnectionState.DISCONNECTED, ConnectionState.CONNECTING,
            ConnectionState.DISCONNECTING })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionState {
        int DISCONNECTED = 0;
        int CONNECTING = 1;
        int CONNECTED = 2;
        int DISCONNECTING = 3;
    }

    /**
     * <p>All the reasons for a request to fail.</p>
     */
    @IntDef({ RequestStatus.SUCCESS, RequestStatus.FAIL, RequestStatus.UNEXPECTED_PARAMETER,
            RequestStatus.NOT_AN_OPEN_NETWORK, RequestStatus.CONFIGURATION_NOT_CREATED,
            RequestStatus.CANNOT_REMOVE_NETWORK })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestStatus {
        /**
         * The request was successfully initiated.
         */
        int SUCCESS = 0;
        /**
         * The system has rejected the request.
         */
        int FAIL = 1;
        /**
         * The connector has rejected the request due to undefined parameters.
         */
        int UNEXPECTED_PARAMETER = 2;
        /**
         * The connector has rejected the request because the given network is not an open network.
         */
        int NOT_AN_OPEN_NETWORK = 3;
        /**
         * The system has rejected the app request to create a configuration to connect to the network.
         */
        int CONFIGURATION_NOT_CREATED = 4;
        /**
         * The system has rejected the app request to delete the existing configuration of the network. This happens
         * if the previous configuration had been created by another application or the system.
         */
        int CANNOT_REMOVE_NETWORK = 5;
    }

    /**
     * All the different connection errors which can occur.
     */
    @IntDef(flag = true, value = { ConnectionError.CONNECTION_FAILED, ConnectionError.CONNECTION_BLOCKED,
            ConnectionError.CONNECTION_POOR_LINK })
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // it is more human readable this way
    public @interface ConnectionError {

        /**
         * The connection has failed and the device should be disconnected.
         */
        int CONNECTION_FAILED = 0;
        /**
         * The connection is blocked and no progress can be done, also see
         * {@link android.net.NetworkInfo.DetailedState#BLOCKED BLOCKED}.
         */
        int CONNECTION_BLOCKED = 1;
        /**
         * The connection has poor link, also see
         * {@link android.net.NetworkInfo.DetailedState#VERIFYING_POOR_LINK VERIFYING_POOR_LINK}.
         */
        int CONNECTION_POOR_LINK = 2;
    }


    // ========================================================================
    // CONSTRUCTOR

    /**
     * <p>To build a new instance of WifiConnector.</p>
     * <p>This constructor get the system resources from the given context, it also registers a receiver to receive
     * network updates from the system.</p>
     * <p>When building a new instance, the connector keeps the current connected network in order to restore it
     * when the instance is released.</p>
     * <p>Call {@link #release(Context)} when the instance becomes useless.</p>
     *
     * @param appContext The context of the application in order to get the system resources necessary to connect to
     * a Wifi network.
     */
    WifiConnector(Context appContext) {
        // initialising system managers
        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        // registering for WiFi events
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        appContext.registerReceiver(mWifiReceiver, intentFilter);

        // keep the original network to restore the state on disconnection
        mPreviousNetwork = getConfiguredNetwork(getConnectedSsid());
    }


    // ========================================================================
    // PACKAGE METHODS

    /**
     * <p>To release all the system resources linked to this connector and to restore the connection as before this
     * connector might have changed it.</p>
     * <p>This method also unsubscribes all its listener.</p>
     *
     * @param appContext The context of the application in order to release all the system resources.
     */
    void release(Context appContext) {
        restore();
        appContext.unregisterReceiver(mWifiReceiver);
        mListeners.clear();
    }

    /**
     * <p>This method connects to the given network.</p>
     * <p>This method returns a request status to know if the request could successfully be initiated.</p>
     * <p>To know if the request was successful, an asynchronous response will be made to all listener which
     * subscribed to this connector with {@link #subscribe(WifiConnectorListener)}.</p>
     * <p>If the network was already connected, this method would trigger an asynchronous call to
     * {@link WifiConnectorListener#onConnectionStateUpdated(String, int) onConnectionStateUpdated} with
     * {@link ConnectionState#CONNECTED CONNECTED}.</p>
     *
     * @param network The network to connect with.
     *
     * @return {@link RequestStatus#SUCCESS SUCCESS} if the request was successfully instantiated, the reason of the
     * failure otherwise.
     */
    public @RequestStatus int connect(WifiNetwork network) {
        if (network == null || network.getSSID() == null || network.getSSID().isEmpty()) {
            Log.w(TAG, "[connect] called with null or empty parameters.");
            return RequestStatus.UNEXPECTED_PARAMETER;
        }

        if (!network.isOpenNetwork()) {
            Log.w(TAG, "[connect] network is not opened.");
            return RequestStatus.NOT_AN_OPEN_NETWORK;
        }

        // setting up the network to watch
        mExpectedNetwork = network;

        if (isConnected(network.getSSID())) {
            // network is already connected, forcing an asynchronous response to the request.
            if (DEBUG) {
                Log.i(TAG, "[connect] network is already connected.");
            }
            updateState(network, ConnectionState.CONNECTED, true);
            return RequestStatus.SUCCESS;
        }
        else {
            // going into connecting state
            updateState(network, ConnectionState.CONNECTING, true);
        }

        // step 1: remove the current configured network if it exists
        if (!removeWifiConfiguration(network.getSSID())) {
            Log.w(TAG, "[connect] Network could not be removed by the application");
            return RequestStatus.CANNOT_REMOVE_NETWORK;
        }

        // step 2: create a new configuration
        WifiConfiguration config = createConfiguration(network.getSSID());

        if (config == null) {
            Log.w(TAG, "[connect] Network could not be removed by the application");
            return RequestStatus.CONFIGURATION_NOT_CREATED;
        }

        // step 3: connect to the network
        if (mWifiManager.enableNetwork(config.networkId, true)) {
            return RequestStatus.SUCCESS;
        }
        else {
            Log.w(TAG, "[connect] WifiManager could not enable the given network");
            return RequestStatus.FAIL;
        }
    }

    /**
     * <p>To get the IP address of the gateway this Android device is connected to.</p>
     * <p>This method gets the bytes which represent the gateway IP and process it to get the Ip in a human
     * readable String format as follows: <code>xx.xx.xx.xx</code></p>
     *
     * @return The IPv4 address of the connected gateway as <code>xx.xx.xx.xx</code>.
     */
    String getGatewayIpAddress() {
        DhcpInfo dhcp = mWifiManager.getDhcpInfo();

        // contains the IP address as 4 bytes summed up into an int
        int address = dhcp.gateway;

        // converts little-endian to big-endian if needed to get the numbers in network byte order
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            address = Integer.reverseBytes(address);
        }

        // gets the corresponding 4 bytes
        byte[] byteArray = BigInteger.valueOf(address).toByteArray();

        try {
            // builds the IP address from the 4 bytes
            return InetAddress.getByAddress(byteArray).getHostAddress();
        }
        catch (UnknownHostException ex) {
            Log.w(TAG, "[getGatewayIpAddress] Failed to build the host address");
            return "";
        }
    }

    /**
     * <p>This method checks if the given network is the connected wifi network.</p>
     *
     * @param ssid The ssid of network to check the connection state of. The SSID is expected without quotes.
     *
     * @return True if it is the connected network, false otherwise.
     */
    boolean isConnected(String ssid) {
        return ssid != null && ssid.equals(getConnectedSsid());
    }

    /**
     * <p>This method checks if the given ssid corresponds to a configured device.</p>
     *
     * @param ssid the ssid to check without quotes.
     *
     * @return True if an existing configuration has the given ssid, false otherwise.
     */
    boolean isConfiguredNetwork(String ssid) {
        return getConfiguredNetwork(ssid) != null;
    }

    /**
     * <p>To register a listener to get connection updates.</p>
     *
     * @param listener The listener to register.
     */
    void subscribe(WifiConnectorListener listener) {
        mListeners.add(listener);
    }

    /**
     * <p>To unregister a listener to not receive anymore connection updates.</p>
     *
     * @param listener the listener to unregister.
     */
    void unsubscribe(WifiConnectorListener listener) {
        mListeners.remove(listener);
    }


    // ========================================================================
    // PRIVATE METHOD

    /**
     * <p>To restore the connected network as it was before this connector has altered the connections.</p>
     */
    private void restore() {
        boolean isPreviousNetwork = mPreviousNetwork != null && mExpectedNetwork != null
                && mExpectedNetwork.getSSIDWithQuotes().equals(mPreviousNetwork.SSID);
        boolean isConnected = mExpectedNetwork != null && isConnected(mExpectedNetwork.getSSID());

        if (DEBUG) {
            Log.d(TAG, String.format("[restore] restoring connection to previous network: network=%s, isConnected=%s",
                                     isPreviousNetwork ? mPreviousNetwork.SSID : "null", isConnected));
        }

        if (isConnected && !isPreviousNetwork) {
            // the expected network configuration has to be removed.
            // this will also disconnects the network and the system will reconnect to its preferred nearby one.
            removeWifiConfiguration(mExpectedNetwork.getSSID());
            mExpectedNetwork = null;
        }
        else if (!isConnected && isPreviousNetwork) {
            // the network has to be reconnected
            WifiConfiguration config =
                    mExpectedNetwork != null ? getConfiguredNetwork(mExpectedNetwork.getSSIDWithQuotes()) : null;
            if (config != null) {
                mWifiManager.enableNetwork(config.networkId, true);
            }
        }

        // cancel the traffic routed to the wifi network
        routeWifiTraffic(null);
    }

    /**
     * <p>To get the SSID of the connected network.</p>
     *
     * @return The SSID - without quotes - of the connected network or {@link #NULL_SSID} if none could be found.
     */
    private String getConnectedSsid() {
        // check if wifi is connected
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()
                || networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
            return NULL_SSID;
        }

        // get the SSID of the connected wifi network
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        return wifiInfo == null ? NULL_SSID : Utils.stripSSIDQuotes(wifiInfo.getSSID());
    }

    /**
     * <p>This method creates an open wifi configuration for the given network and registers it with the system.</p>
     *
     * @param ssid The ssid of the network to create a configuration for.
     *
     * @return The new configuration from the system.
     */
    private WifiConfiguration createConfiguration(String ssid) {
        WifiConfiguration config = buildOpenWifiConfiguration(ssid);
        int networkId = mWifiManager.addNetwork(config);
        if (networkId == -1) {
            Log.w(TAG, "[createConfiguration] Configuration could not be added by the application.");
            return null;
        }

        return getConfiguredNetwork(ssid); // getting the final configuration from the system
    }

    /**
     * <p>This method deletes any configuration which corresponds to the given network from the system.</p>
     * <p>If there was no corresponding configuration, this method has no effect and would return true.</p>
     * <p>If there is an existing corresponding configuration and it has been created by another process - another
     * application or the system, this method would fail and would return false.</p>
     *
     * @param ssid The ssid of the network to delete any corresponding configuration.
     *
     * @return True if the configuration could be removed or there wasn't any corresponding configuration.
     */
    private boolean removeWifiConfiguration(String ssid) {
        WifiConfiguration config = getConfiguredNetwork(ssid);

        if (config == null) {
            return true;
        }

        // removing the existing config to avoid disconnections
        if (!mWifiManager.removeNetwork(config.networkId)) {
            Log.w(TAG, "[removeWifiConfiguration] Cannot remove network: user needs to do it through the settings");
            return false;
        }

        return true;
    }

    /**
     * <p>This method looks for a configured network which corresponds to the given ssid.</p>
     *
     * @param ssid The SSID to find a corresponding configuration for.
     *
     * @return the corresponding configuration or null if none could be found
     */
    private WifiConfiguration getConfiguredNetwork(String ssid) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        String extendedSsid = "\"" + ssid + "\"";

        for (WifiConfiguration config : list) {
            if (config.SSID != null && config.SSID.equals(extendedSsid)) {
                return config;
            }
        }

        return null;
    }

    /**
     * <p>This method routes all the traffic to the given network.</p>
     * <p>IF the network is null, the system cancels any routing rules and restores its own.</p>
     *
     * @param network The network to route the traffic to.
     */
    private void routeWifiTraffic(Network network) {
        mConnectivityManager.bindProcessToNetwork(network);
    }

    /**
     * <p>This method builds a wifi configuration of type "open" with the given ssid.</p>
     *
     * @param ssid The ssid to build an open configuration for.
     *
     * @return A new wifi configuration object with the givne parameters.
     */
    private WifiConfiguration buildOpenWifiConfiguration(String ssid) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\""; // known configurations are enclosed in double quotes
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); // open network
        return config;
    }

    /**
     * <p>This method analyzes the given objects to detect the network state change and acts upon it.</p>
     *
     * @param networkInfo The network info to look at.
     * @param wifiInfo The wifi info to look at.
     */
    private void onNetworkStateChanged(NetworkInfo networkInfo, WifiInfo wifiInfo) {
        if(DEBUG) {
            WifiInfo retrievedInfo = mWifiManager.getConnectionInfo();
            Log.d(TAG, String.format("[onNetworkStateChanged] networkInfo=%s, wifiInfo=%s, retrievedInfo=%s",
                                     networkInfo, wifiInfo, retrievedInfo));
        }

        NetworkInfo.DetailedState state = networkInfo.getDetailedState();
        boolean isExpectedNetwork = wifiInfo != null && mExpectedNetwork != null
                && wifiInfo.getSSID().equals(mExpectedNetwork.getSSIDWithQuotes());
        boolean isConnected = state == NetworkInfo.DetailedState.CONNECTED;

        if (!isExpectedNetwork) {
            if (isConnected) {
                // another network is connected, hence the expected one is disconnected
                updateState(mExpectedNetwork, ConnectionState.DISCONNECTED, false);
            }
            // update is not about the network this class is interested in
            return;
        }

        // it is the network we are interested in: updating the state
        onDetailedStateUpdated(mExpectedNetwork, state);

        if (/*isExpectedNetwork && */isConnected) {
            // step 4: route the traffic to the network
            routeWifiTraffic(mConnectivityManager.getActiveNetwork());
        }
    }

    /**
     * <p>This method analyses the received state and acts upon it either by sending a soft state update to any
     * listener or by sending an error to it.</p>
     *
     * @param network The network concerned by the update.
     * @param state The state to analyse.
     */
    private void onDetailedStateUpdated(WifiNetwork network, NetworkInfo.DetailedState state) {
        switch (state) {
            case DISCONNECTED:
                updateState(network, ConnectionState.DISCONNECTED, false);
                break;
            case CONNECTED:
                updateState(network, ConnectionState.CONNECTED, false);
                break;
            case CONNECTING:
                updateState(network, ConnectionState.CONNECTING, false);
                break;
            case DISCONNECTING:
                updateState(network, ConnectionState.DISCONNECTING, false);
                break;

            case IDLE:
            case OBTAINING_IPADDR:
            case SCANNING:
            case AUTHENTICATING:
            case CAPTIVE_PORTAL_CHECK:
                // nothing to do, just wait for the next notification
                return;

            case VERIFYING_POOR_LINK:
                sendError(network, ConnectionError.CONNECTION_POOR_LINK);
                return;
            case BLOCKED:
                sendError(network, ConnectionError.CONNECTION_BLOCKED);
                return;
            case FAILED:
                sendError(network, ConnectionError.CONNECTION_FAILED);
                return;

            case SUSPENDED:
                // traffic is suspended, should it send a disconnected state?
                break;
        }
    }

    /**
     * <p>This method updates the connection state and sends the new to state to the connector's listener.</p>
     * <p>This method only updates the state if the state has changed or if a hard update must be done.</p>
     * 
     * @param network The network for which the state has been updated.
     * @param state The new state.
     * @param hardUpdate True to force the update to happen, false to only update the state if it has changed.
     */
    private void updateState(WifiNetwork network, @ConnectionState int state, boolean hardUpdate) {
        if (DEBUG) {
            Log.d(TAG, String.format("[updateState] Updating state from %s to %s for network %s",
                                     getConnectionStateLabel(mState),
                                     getConnectionStateLabel(state), network == null ? "null" : network.getSSID()));
        }

        if (hardUpdate || mState != state) {
            mState = state;
            sendConnectionStateUpdate(network, state);
        }

    }


    // ========================================================================
    // PRIVATE METHODS - NOTIFY LISTENERS

    /**
     * <p>This method sends the updated state to its listeners. This is done asynchronously to let the calling 
     * process finishes.</p>
     * 
     * @param network The network this update is about.
     * @param state The new state.
     */
    private void sendConnectionStateUpdate(WifiNetwork network, @ConnectionState int state) {
        @NonNull final String ssid = network == null ? "" : network.getSSID();
        mHandler.post(() -> {
            for (WifiConnectorListener listener : mListeners) {
                listener.onConnectionStateUpdated(ssid, state);
            }
        });
    }

    /**
     * <p>This method sends the error to its listeners. This is done asynchronously to let the calling
     * process finishes.</p>
     * 
     * @param network The network this update is about.
     * @param error The error which occurred.
     */
    private void sendError(WifiNetwork network, @ConnectionError int error) {
        mHandler.post(() -> {
            for (WifiConnectorListener listener : mListeners) {
                listener.onError(network, error);
            }
        });
    }


    // ========================================================================
    // PRIVATE STATIC METHODS

    /**
     * <p>To get a label for the given connection state.</p>
     *
     * @param state The state to get a label for.
     *
     * @return The corresponding label or "unknown" if none could be found.
     */
    private static String getConnectionStateLabel(@ConnectionState int state) {
        switch (state) {
            case ConnectionState.CONNECTED:
                return "CONNECTED";
            case ConnectionState.CONNECTING:
                return "CONNECTING";
            case ConnectionState.DISCONNECTED:
                return "DISCONNECTED";
            case ConnectionState.DISCONNECTING:
                return "DISCONNECTING";
            default:
                return String.format("UNKNOWN (%s)", state);
        }
    }


    // ========================================================================
    // INTERFACES

    /**
     * <p>A listener to receive notifications about the connection this connector creates.</p>
     */
    public interface WifiConnectorListener {

        /**
         * <p>This is called when the connection state of the {@link WifiNetwork} sets with
         * {@link #connect(WifiNetwork)} changes.</p>
         * <p>This also provides a successful asynchronous result to {@link #connect(WifiNetwork)}.</p>
         *
         * @param ssid The ssid for which the state has been updated.
         * @param state The updated state.
         */
        void onConnectionStateUpdated(@NonNull String ssid, @ConnectionState int state);

        /**
         * <p>This is called when a connection error occurs with the {@link WifiNetwork} sets with
         * {@link #connect(WifiNetwork)}.</p>
         * <p>This also provides an unsuccessful asynchronous result to {@link #connect(WifiNetwork)}.</p>
         *
         * @param network The network for which the error occurred.
         * @param error The error which occurred.
         */
        void onError(WifiNetwork network, @ConnectionError int error);
    }

}
