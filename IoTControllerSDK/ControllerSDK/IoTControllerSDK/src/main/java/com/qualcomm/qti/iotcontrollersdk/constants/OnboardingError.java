/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * Represents an onboarding error 
 */
public class OnboardingError {
	/**
	 * Enumeration of AllPlay device onboarding error codes.
	 */
	public enum OnboardingErrorCode {
		/**
		 * No onboarding error
		 */
		NONE,
		/**
		 * Name of the network is empty
		 */
		SSID_EMPTY,
		/**
		 * Name of the network is too long
		 */
		SSID_TOO_LONG,
		/**
		 * Passphrase is empty
		 */
		PASSPHRASE_EMPTY,
		/**
		 * Passphrase is too short
		 */
		PASSPHRASE_TOO_SHORT,
		/**
		 * Passphrase is too long
		 */
		PASSPHRASE_TOO_LONG,
		/**
		 * Network not reachable or not found
		 */
		NETWORK_UNREACHABLE,
		/**
		 * Incorrect authentication type
		 */
		UNSUPPORTED_PROTOCOL,
		/**
		 * Passphrase not correct
		 */
		UNAUTHORIZED,
		/**
		 * Request error
		 */
		REQUEST,
		/**
		 * Onboarding timed out
		 */
		TIMED_OUT,
		/**
		 * An error with message
		 */
		MESSAGE
	}
	
	/**
	 * The onboarding error code
	 */
	public OnboardingErrorCode errorCode;
	
	/**
	 * The onboarding error message
	 */
	public String errorMessage = null;
	
	OnboardingError() {
		errorCode = OnboardingErrorCode.NONE;
	}
	
	OnboardingError(final OnboardingErrorCode code, final String message) {
		errorCode = code;
		errorMessage = message;
	}
}
