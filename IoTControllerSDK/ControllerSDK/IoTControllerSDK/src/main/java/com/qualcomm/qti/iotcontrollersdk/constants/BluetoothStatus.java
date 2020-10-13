/*
 * *************************************************************************************************
 * * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                      *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.constants;

import java.util.Arrays;

/**
 * <p>This enumeration keeps a list of the Bluetooth error status, see
 * {@link com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtErrorAttr#mStatus mStatus} from
 * {@link com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BtErrorAttr BtErrorAttr}, which can
 * be sent from an IoTDevice .</p>
 * <p>This enumeration is not generated from the iotsys library and is built based on knowledge.
 * Therefore this
 * enumeration is non comprehensive.</p>
 * <p>As some statuses contain spaces, to get the BluetoothStatus which corresponds to the status,
 * {@link #get(String) get(String)} should be used instead of <code>valueOf(String)</code>.</p>
 */
public enum BluetoothStatus {

  set_discoverable_bt_not_enabled("set_discoverable_bt_not_enabled"),
  exit_discoverable_bt_not_enabled("exit_discoverable_bt_not_enabled"),
  bt_not_enabled("bt_not_enabled"),
  invalid_address("invalid_address"),
  device_not_in_interface_list("device_not_in_interface_list"),
  device_already_paired("device_already_paired"),
  bt_last_connected_peer_device_entry_missing("bt_last_connected_peer_device_entry_missing"),
  bt_last_connected_peer_device_empty("bt_last_connected_peer_device_empty"),
  device_already_connected("device_already_connected"),
  device_object_not_created("device_object_not_created"),
  device_not_connected("device_not_connected"),
  device_not_paired("device_not_paired"),
  command_not_supported("command_not_supported"),
  make_discoverable_invalid_parameter("make_discoverable_invalid parameter");

  /**
   * The String value of the error status.
   */
  final String mmStatus;

  /**
   * <p>To build the enumeration items with a status value.</p>
   *
   * @param status The string value for the enumeration item.
   */
  BluetoothStatus(String status) {
    mmStatus = status;
  }

  /**
   * <p>To get the value of the error status.</p>
   *
   * @return The error status.
   */
  public String getStatus() {
    return mmStatus;
  }

  /**
   * <p>To get the enumeration item depending on its status value.</p>
   *
   * @param status The status to get the item for.
   * @return The corresponding enumeration item.
   * @throws Throwable this method throws {@link IllegalArgumentException IllegalArgumentException}
   * if the given status does not a match an enumeration item.
   */
  public static BluetoothStatus get(String status) throws Throwable {
    return Arrays.stream(values())
        .filter(bluetoothStatus -> bluetoothStatus.getStatus().equals(status))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + status));
  }

}