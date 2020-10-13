/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller.interfaces;


/**
 * The interface defines the callback method when a group is created.
 */
public interface IoTGroupCallback {

  /**
 * Callback method when a group is created.
 * @param groupId a new group ID
 * @param status indicates whether the group is created successfully or failed
 */
  void OnGroupCreated(String groupId, boolean status);
}
