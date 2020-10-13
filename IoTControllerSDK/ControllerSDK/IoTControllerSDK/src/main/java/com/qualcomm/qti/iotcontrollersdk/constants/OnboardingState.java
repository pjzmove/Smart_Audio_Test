/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * Enumeration of AllPlay device onboarding states.
 */
public enum OnboardingState {
	/**
	 * Initial state
	 */
	NOT_ONBOARDED,
	/**
	 * Sending the credential
	 */
	SENDING_CREDENTIAL,
	/**
	 * IoTError sending credential
	 */
	SENDING_ERROR,
	/**
	 * Sending credential finished, device configured
	 */
	CONFIGURED,
	/**
	 * Connecting to network
	 */
	CONNECTING_TO_NETWORK,
	/**
	 * IoTError while connecting to network
	 */
	CONNECTING_ERROR,
	/**
	 * Connection timeout
	 */
	CONNECTING_TIMEOUT,
	/**
	 * Wi-Fi turned off
	 */
	WIFI_OFF,
	/**
	 * Waiting for the device to appear
	 */
	WAITING_FOR_DEVICE,
	/**
	 * Waiting timeout
	 */
	WAITING_TIMEOUT,
	/**
	 * Device onboarded
	 */
	ONBOARDED
}
