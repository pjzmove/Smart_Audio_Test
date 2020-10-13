/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import java.util.List;
import java.util.Objects;

/**
 * <p>This class describes a Bluetooth device which can be paired or connected to the IoT
 * device.</p>
 */
public class IoTBluetoothDevice extends IoTRepository {

  /**
   * The readable name of the device - either given by the device or the system.
   */
  private final String mmName;
  /**
   * The Bluetooth address of the device.
   */
  private final String mmAddress;
  /**
   * The connection state of the Bluetooth device with the IoT Device.
   */
  private boolean mmConnected;

  /**
   * To build a new instance of IoTBluetoothDevice.
   *
   * @param name The name of the device.
   * @param address The Bluetooth address of the device.
   */
  IoTBluetoothDevice(String name, String address) {
    this(name, address, false);
  }

  /**
   * To build a new instance of IoTBluetoothDevice.
   *
   * @param name The name of the device.
   * @param address The Bluetooth address of the device.
   * @param connected The connection state of the Bluetooth device with the IoT Device.
   */
  IoTBluetoothDevice(String name, String address, boolean connected) {
    super(IoTType.BLUETOOTH_DEVICE);
    this.mmName = name;
    this.mmAddress = address;
    this.mmConnected = connected;
  }

  /**
   * <p>To get the name of the device.</p>
   *
   * @return The name as given when creating the object.
   */
  public String getName() {
    return mmName;
  }

  /**
   * <p>To get the Bluetooth address of the device.</p>
   *
   * @return The Bluetooth address as given when creating the object.
   */
  public String getAddress() {
    return mmAddress;
  }

  /**
   * <p>To get the connection state of this Bluetooth device.</p>
   *
   * @return the connection state of the Bluetooth device with the IoT device.
   */
  public boolean isConnected() {
    return mmConnected;
  }

  /**
   * <p>To set the connection state of this Bluetooth device.</p>
   *
   * @param connected True if hte device is connected with the IoT Device, false otherwise.
   */
  public void setConnected(boolean connected) {
    this.mmConnected = connected;
  }

  @Override // Object
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IoTBluetoothDevice that = (IoTBluetoothDevice) o;
    return Objects.equals(mmAddress, that.mmAddress);
  }

  @Override // Object
  public int hashCode() {
    return Objects.hash(mmAddress);
  }

  @Override // IotRepository
  public String getId() {
    return mmAddress;
  }

  @Override // IotRepository
  public IoTType getType() {
    return IoTType.BLUETOOTH_DEVICE;
  }

  @Override // IotRepository
  public List<? extends IoTRepository> getList() {
    // no linked devices
    return null;
  }

  @Override // IotRepository
  public boolean dispose() {
    return true;
  }
}
