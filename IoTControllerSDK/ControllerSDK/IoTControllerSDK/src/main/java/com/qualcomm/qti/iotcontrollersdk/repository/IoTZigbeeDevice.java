/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.constants.ZigbeeDeviceType;
import java.util.List;

/**
 * <p>This class describes a ZigBee Device in the IoT world.</p>
 * <p>ZigBee devices have different types.</p>
 */
public class IoTZigbeeDevice extends IoTRepository {

  /**
   * The device identifier.
   */
  private int mId;
  /**
   * The type of the Zigbee device.
   */
  private ZigbeeDeviceType mType;
  /**
   * The name of the Zigbee device.
   */
  private String mName;

  /**
   * To build a new instance of IoTZigBeeDevice.
   *
   * @param id
   *          The identifier of the device.
   * @param type
   *          The type of the ZigBee device.
   * @param name
   *          The name of the ZigBee device.
   */
  IoTZigbeeDevice(int id, String type, String name) {
    super(IoTType.ZIGBEE_DEVICE);
    this.mId = id;
    this.mType = ZigbeeDeviceType.get(type);
    this.mName = name;
  }

  /**
   * To get the device identifier.
   *
   * @return the ZigBee device identifier.
   */
  public int getDeviceIdentifier() {
    return mId;
  }

  /**
   * <p>To get the type of this ZigBee Device.</p>
   * <p>This method is different to {@link #getType() getType()} which provides the type of IoT
   * element as one of {@link IoTType IoTType}. In the case of a ZigBee device,
   * {@link #getType() getType()} returns {@link IoTType#ZIGBEE_DEVICE ZIGBEE_DEVICE}.</p>
   *
   * @return the ZigBee device type.
   */
  public ZigbeeDeviceType getZigbeeType() {
    return mType;
  }

  @Override // IoTRepository
  public String getName() {
    return mName;
  }

  @Override // IoTRepository
  public String getId() {
    return mId + "";
  }

  @Override // IoTRepository
  public IoTType getType() {
    return IoTType.ZIGBEE_DEVICE;
  }

  @Override // IoTRepository
  public List<? extends IoTRepository> getList() {
    return null;
  }

  @Override // IoTRepository
  public boolean dispose() {
    return true;
  }
}
