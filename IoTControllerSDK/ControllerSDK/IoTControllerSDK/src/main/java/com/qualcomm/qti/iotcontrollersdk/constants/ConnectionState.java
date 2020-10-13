/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * Enumeration of Android's state when connecting to device's AP.
 */
public enum ConnectionState {
	/**
	 * Not connected to network
	 */
	DISCONNECTED,
	/**
	 * Connecting to network
	 */
	CONNECTING,
	/**
	 * Connected to network
	 */
	CONNECTED,
	/**
	 * Connecting error
	 */
	CONNECTING_ERROR,
	/**
	 * Connecting to network timed out
	 */
	CONNECTING_TIMEOUT,
	/**
	 * Wi-Fi is turned off
	 */
	WIFI_OFF,
	/**
	 * Connected, but discovery the device timed out
	 */
	DISCOVERING_DEVICE_TIMEOUT;
}
