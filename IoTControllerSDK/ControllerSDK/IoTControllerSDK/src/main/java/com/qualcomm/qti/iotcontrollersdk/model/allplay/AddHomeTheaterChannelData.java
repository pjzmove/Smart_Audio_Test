/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;

/**
 * Represents the data returned from addHomeTheaterChannel in Player.
 */
public class AddHomeTheaterChannelData {
	/**
	 * The error code returned from Player addHomeTheaterChannel.
	 */
	public IoTError error;
	/**
	 * If the channel with speaker was previously known.
	 */
	public boolean previouslyKnown;

	/**
	 * Constructor for AddHomeTheaterChannelData. Initialized to default values.
	 */
	public AddHomeTheaterChannelData() {
		error = IoTError.NONE;
		previouslyKnown = false;
	}

	void setError(final IoTError error) {
		this.error = error;
	}

	void setPreviouslyKnown(final boolean previouslyKnown) {
		this.previouslyKnown = previouslyKnown;
	}
}
