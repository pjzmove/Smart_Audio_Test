/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller.listeners;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;

/**
 * A listener interface defines the methods used by Controller SDK to dispatch events.
 */
public interface IoTAppListener {

  /**
   * a new device found event dispatch
   * @param device is a new device instance
   */
  void onDeviceAdded(IoTDevice device);

  /**
   * A group created event dispatch
   * @param group is a new {@link IoTGroup} instance
   */
  void onPlayerGroupAdd(IoTGroup group);

  /**
   * A device removed event dispatch
   * @param group is a {@link IoTGroup} instance
   */
  void onPlayerGroupRemoved(IoTGroup group);

  /**
   * Group info changed event dispatch
   * @param group whose info is changed
   */
  void onPlayerGroupChanged(IoTGroup group);

  /**
   * Device re-discovery event dispatch
   */
  void onPlayerReDiscovered();

  /**
   * A surround speaker found event dispatch
   * @param host is host URI
   * @param deviceId is the device ID
   */
  void onSurroundPlayerDiscovered(String host, String deviceId);

}
