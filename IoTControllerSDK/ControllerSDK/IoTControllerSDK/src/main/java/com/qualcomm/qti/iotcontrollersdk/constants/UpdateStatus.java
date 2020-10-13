/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * Enumeration of AllPlay device update status.
 */
public enum UpdateStatus {
	/**
	 * Default state/not needed
	 */
	NONE,
	/**
	 * Updating
	 */
	UPDATING,
	/**
	 *Update was triggered but not needed
	 */
	UPDATE_NOT_NEEDED,
	/**
	 * Update failed
	 */
	UPDATE_FAILED,
	/**
	 * Low battery
	 */
	LOW_BATTERY,
	/**
	 * Update successful
	 */
	SUCCESSFUL
}
