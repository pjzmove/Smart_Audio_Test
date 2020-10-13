/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys;

/**
 * Represents a remote status.  Each status is a remote speaker's AP with its status
 */
public class RemoteOnboardingStatus {
	/**
	 * Enumeration of remote onboarding code from remote onboarding status
	 */
	public enum RemoteOnboardingCode {
		/**
		 * Not configured
		 */
		NOT_CONFIGURED(0),
		/**
		 * Configuring
		 */
		CONFIGURING(1),
		/**
		 * Configured
		 */
		CONFIGURED(2),
		/**
		 * General IoTError, please look into errorMessage for additional message
		 */
		ERROR(0x8000),
		/**
		 * Network unreachable during connection
		 */
		NETWORK_UNREACHABLE(0x8001),
		/**
		 * Unsupported authentication protocol
		 */
		UNSUPPORTED_PROTOCOL(0x8002),
		/**
		 * Not authorized to connect.
		 */
		UNAUTHORIZED(0x8003),
		/**
		 * Time out during proxy onboarding
		 */
		TIMED_OUT(0x8004);

		public final int code;

		RemoteOnboardingCode(int value) {
			this.code = value;
		}
	}

	/**
	 * The speaker's AP
	 */
	public String apName = null;

	/**
	 * The device ID of the speaker.
	 */
	public String deviceID = null;

	/**
	 * The display name of the speaker.
	 */
	public String displayName = null;

	/**
	 * The ProxyCode of current status
	 */
	public RemoteOnboardingCode code = RemoteOnboardingCode.NOT_CONFIGURED;

	/**
	 * IoTError message
	 */
	public String errorMsg = null;

	RemoteOnboardingStatus(final String apName, final String deviceID, final String displayName,
						   final RemoteOnboardingCode code, final String errorMsg) {
		this.apName = apName;
		this.deviceID = deviceID;
		this.displayName = displayName;
		this.code = code;
		this.errorMsg = errorMsg;
	}
}
