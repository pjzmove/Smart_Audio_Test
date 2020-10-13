/*
 * *************************************************************************************************
 * * © 2018-2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.constants;

import java.util.Arrays;

/**
 * <p>This enumeration keeps a list of the different types of a Zigbee device.</p>
 * <p>This enumeration is not generated from the iotsys library and is built based on knowledge.
 * Therefore this enumeration is non comprehensive.</p>
 */
public enum ZigbeeDeviceType {

  light("light"),
  thermostat("thermostat"),
  unknown("unknown");

  /**
   * The String value of the Zigbee type.
   */
  final String mType;

  /**
   * <p>To build the enumeration items with their String value.</p>
   *
   * @param type The string value of the type.
   */
  ZigbeeDeviceType(String type) {
    mType = type;
  }

  /**
   * <p>To get the String value of the type.</p>
   *
   * @return The value of the type.
   */
  public String getType() {
    return mType;
  }

  /**
   * <p>To get the enumeration item depending on its type value.</p>
   *
   * @param type The type to get the item for.
   * @return The corresponding enumeration item or {@link ZigbeeDeviceType#unknown unknown} type if
   * the item could not be found.
   */
  public static ZigbeeDeviceType get(String type) {
    return Arrays.stream(values())
        .filter(zigBeeType -> zigBeeType.getType().equals(type))
        .findFirst()
        .orElse(ZigbeeDeviceType.unknown);
  }

}