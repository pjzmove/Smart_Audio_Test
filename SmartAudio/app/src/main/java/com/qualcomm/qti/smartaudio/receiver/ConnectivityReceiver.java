/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import com.qualcomm.qti.iotcontrollersdk.model.allplay.ScanInfo;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class receivers network change broadcasts.  It will send out an interface callback for all objects that implements its interface.
 */
public class ConnectivityReceiver extends BroadcastReceiver {
	private static String TAG = ConnectivityReceiver.class.getSimpleName();

	private static final int SECURITY_NONE = 0;
	private static final int SECURITY_WEP = 1;
	private static final int SECURITY_PSK = 2;
	private static final int SECURITY_EAP = 3;
	private static final String NEW_SPEAKER_SOFT_AP_ID = "_AJ";

	private List<ConnectivityChangedListener> mConnectivityChangedListeners = new ArrayList<>();

	private boolean mConnected = false;

	private String mNetworkName = null;
	private String mIPAddress = null;

	/**
	 * Constructor.  Takes in a context to register receiver
	 * @param context the Android context
	 */
	public ConnectivityReceiver(final Context context) {
		parseConnection(context);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		context.registerReceiver(this, filter);
	}

	/**
	 * Add a ConnectivityChangedListener object.  Notify if possible
	 * @param listener the ConnectivityChangedListener object
	 */
	public void addConnectivityChangedListener(final ConnectivityChangedListener listener) {
		if (listener != null) {
			synchronized (mConnectivityChangedListeners) {
				// Check if we had added this before, so we don't add it again
				if (!mConnectivityChangedListeners.contains(listener)) {
					mConnectivityChangedListeners.add(listener);
					notifyListener(listener);
				}
			}
		}
	}

	/**
	 * Remove a ConnectivityChangedListener object
	 * @param listener the ConnectivityChangedListener object
	 */
	public void removeConnectivityChangedListener(final ConnectivityChangedListener listener) {
		if (listener != null) {
			synchronized (mConnectivityChangedListeners) {
				mConnectivityChangedListeners.remove(listener);
			}
		}
	}

	/**
	 * Check to see if device is connected to network
	 * @return true if device is connected to network
	 */
	public boolean isConnected() {
		synchronized (this) {
			return mConnected;
		}
	}

	/**
	 * Get the network name if on NetworkType.WIFI, null if not
	 * @return the network name
	 */
	public String getNetworkName() {
		synchronized (this) {
			return mNetworkName;
		}
	}

	/**
	 * Get the ip address
	 * @return the ip address
	 */
	public String getIPAddress() {
		synchronized (this) {
			return mIPAddress;
		}
	}

	public String getNewSpeakerSoftApId() {
		return NEW_SPEAKER_SOFT_AP_ID;
	}

	public ScanInfo getCurrentScanInfo(Context context) {
		WifiConfiguration activeConfig = getActiveWifiConfiguration(context);

		ScanInfo currentScanInfo = null;
		if (activeConfig != null) {
			currentScanInfo = new ScanInfo();
			currentScanInfo.SSID = activeConfig.SSID.replace("\"", "");

			// handset is connected to soft AP
			if (currentScanInfo.SSID.endsWith(NEW_SPEAKER_SOFT_AP_ID)) {
				currentScanInfo = null;
			} else {
				currentScanInfo.authType = getSecurityType(activeConfig);
				currentScanInfo.wifiQuality = getWifiSignalLevel(context);
			}
		}
		return currentScanInfo;
	}

	public WifiConfiguration getActiveWifiConfiguration(Context context) {
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiConfiguration activeConfig = null;

		if (wifiManager != null) {
			for (WifiConfiguration configuration : wifiManager.getConfiguredNetworks()) {
				if (configuration.status == WifiConfiguration.Status.CURRENT) {
					activeConfig = configuration;
					break;
				}
			}
		}
		return activeConfig;
	}

	public int getWifiSignalLevel(Context context) {
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 5);
	}

	private ScanInfo.AuthType getSecurityType(WifiConfiguration config) {
		switch (getSecurity(config)) {
			case SECURITY_WEP:
				return ScanInfo.AuthType.WEP;
			case SECURITY_PSK:
				if (config.allowedProtocols.get(WifiConfiguration.Protocol.RSN)) {
					return ScanInfo.AuthType.WPA2;
				} else {
					return ScanInfo.AuthType.WPA;
				}
			default:
				return ScanInfo.AuthType.OPEN;
		}
	}

	private int getSecurity(WifiConfiguration config) {
		if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK))
			return SECURITY_PSK;

		if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X))
			return SECURITY_EAP;

		return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		// No need to continue if it is anything else
		if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION) &&
			!action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) &&
			!action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			return;
		}

		// Parse the network info
		parseConnection(context);
	}

	/**
	 * This fucntion obtain the network and get the information needed.
	 * @param context the Android context object
	 */
	private void parseConnection(final Context context) {
		// Defaults to no connection
		boolean connected = false;
		String networkName = null;
		String ipAddress = null;

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// We get the default active network info.  Network info can be null if device is not connected to anything
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if ((info != null) &&
			info.isConnected() &&
			(info.getType() == ConnectivityManager.TYPE_WIFI)) {
			// We are connected to either wifi or ethernet
			connected = true;

			// Get network name
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			networkName = wifiInfo.getSSID();

			// Get ip address with current network interface
			ipAddress = getLocalIPAddress(context);
		}

		final boolean notify;
		synchronized (this) {
			// We will need to notify in these condition
			notify = ((connected != mConnected) ||
					(connected && (networkName != null) && !networkName.equals(mNetworkName)) ||
					(connected && (ipAddress != null) && !ipAddress.equals(mIPAddress)));

			// Set its internal variable for later access
			mConnected = connected;
			mNetworkName = networkName;
			mIPAddress = ipAddress;
		}
		if (notify) {
			// Notify
			notifyListeners();
		}
	}

	/**
	 * This function goes through all listeners in the list
	 */
	private void notifyListeners() {
		synchronized (mConnectivityChangedListeners) {
			for (int i = 0; i < mConnectivityChangedListeners.size(); i++) {
				notifyListener(mConnectivityChangedListeners.get(i));
			}
		}
	}

	/**
	 * This is a helper function that calls on ConnectivityChangedListener interface callback
	 * @param listener the ConnectivityChangedListener listener
	 */
	private void notifyListener(final ConnectivityChangedListener listener) {
		if (listener == null) {
			return;
		}
		listener.onConnectivityChanged(isConnected());
	}

	/**
	 * This gets the local IP address of the network.  Only called on wifi network
	 * @return the String of local IP address.
	 */
	public String getLocalIPAddress(final Context context) {
		if (context == null) {
			return null;
		}
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		int address = wifiManager.getConnectionInfo().getIpAddress();

		// Convert little-endian to big-endian if needed
		if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
			address = Integer.reverseBytes(address);
		}

		byte[] byteArray = BigInteger.valueOf(address).toByteArray();

		String ipAddress = null;
		try {
			ipAddress = InetAddress.getByAddress(byteArray).getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		return ipAddress;
	}

	/**
	 * Interface listener for when connectivity changed
	 */
	public interface ConnectivityChangedListener {
		/**
		 * This will be called when connectivity changed
		 * @param connected
		 */
		void onConnectivityChanged(final boolean connected);
	}
}
