/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * Enumeration of AllPlay player error codes
 */
public enum IoTError {
	/**
	 * No error.
	 */
	NONE(0),
	/**
	 * Unknown
	 */
	UNKNOWN(1),
	/**
	 * Request failed.
	 */
	REQUEST(2),
	/**
	 * Network error.
	 */
	NETWORK(3),
	/**
	 * Format error.
	 */
	FORMAT(4),
	/**
	 * Stream error.
	 */
	STREAM(5),
	/**
	 * Authentication error.
	 */
	AUTHENTICATION(6),
	/**
	 * Media rules engine error.
	 */
	MEDIA_RULES_ENGINE(7),
	/**
	 * Invalid device, player, zone, or playlist
	 */
	INVALID_OBJECT(8),
	/**
	 * Player cannot be interrupted
	 */
	UNINTERRUPTIBLE(9),
	/**
	 * Player volume disabled
	 */
	VOLUME_DISABLED(10),
	/**
	 * User cancelled password request
	 */
	AUTHENTICATION_CANCELLED(11),
	/**
	 * Not support request
	 */
	NOT_SUPPORTED(12),
	/**
	 * Not connected
	 */
	NOT_CONNECTED(13),
	/**
	 * Connection in progress
	 */
	CONNECTION_IN_PROGRESS(14),
	/**
	 * Onboarding in progress
	 */
	ONBOARDING_IN_PROGRESS(15),
	/**
	 * Party mode is disabled
	 */
	PARTYMODE_DISABLED(16);
	
	public final int errorValue;

	IoTError(int value) {
		this.errorValue = value;
	}
}