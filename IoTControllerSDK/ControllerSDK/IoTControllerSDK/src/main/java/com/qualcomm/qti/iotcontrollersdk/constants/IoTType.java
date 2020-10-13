/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * <p>This enumeration represents all the types of the different IoT elements recognised by the
 * IoT controller.</p>
 */
public enum IoTType {
  /**
   * Used to represent a group of players or used to represent a "convenience" group for a single
   * player.
   */
  GROUP(0),
  /**
   * An IoT Player allows the media functions on an element.
   */
  PLAYER(1),
  /**
   * A speaker is a physical device which can produce sound.
   */
  SPEAKER(2),
  /**
   * A soundbar is a speaker of type sound bar.
   */
  SOUND_BAR(3),
  /**
   * A satellite speaker is a speaker which is associated with a "main" speaker or soundbar.
   */
  SATELLITE_SPEAKER(4),
  /**
   * An IoT device is a device which implements the IoT system.
   */
  IOT_DEVICE(5),
  /**
   * A light is a physical device which produces light.
   */
  LIGHT(6),
  /**
   * This represents a Bluetooth device associated with an IoT device.
   */
  BLUETOOTH_DEVICE(7),
  /**
   * This represents a ZigBee device associated with an IoT device.
   */
  ZIGBEE_DEVICE(8),
  /**
   * To represent the elements which are undefined in this controller.
   */
  UNKNOWN(9);

  /**
   * The int value of the IoT Type.
   */
  private int value;
  IoTType(int value) {
    this.value = value;
  }

  /**
   * <p>To get the IoT Type from its integer value.</p>
   *
   * @param value
   *          The value to get the corresponding IoT Type.
   *
   * @return the IoT type which corresponds to the given value.
   */
  public static IoTType fromValue(int value) {
    switch(value) {
      case 0:
        return GROUP;
      case 1:
        return PLAYER;
      case 2:
        return SPEAKER;
      case 3:
        return SOUND_BAR;
      case 4:
        return SATELLITE_SPEAKER;
      case 5:
        return IOT_DEVICE;
      case 6:
        return LIGHT;
      case 7:
        return BLUETOOTH_DEVICE;
      case 8:
        return ZIGBEE_DEVICE;
      default:
        return UNKNOWN;
    }
  }

  /**
   * To get the Integer value of the IoT Type.
   *
   * @return the integer which corresponds to the IoT Type in the enumeration.
   */
  public int getValue() {
    return value;
  }

  @Override // object
  public String toString() {
    return getLabel(this);
  }

  /**
   * <p>To get a human readable label which corresponds to the given IoT Type.</p>
   *
   * @param type
   *        The type to get a label for.
   *
   * @return A human readable label for the given IoT Type.
   */
  private static String getLabel(IoTType type) {
    switch (type) {
      case GROUP:
        return "GROUP";
      case PLAYER:
        return "PLAYER";
      case SPEAKER:
        return "SPEAKER";
      case SOUND_BAR:
        return "SOUND_BAR";
      case SATELLITE_SPEAKER:
        return "SATELLITE_SPEAKER";
      case IOT_DEVICE:
        return "IOT_DEVICE";
      case LIGHT:
        return "LIGHT";
      case BLUETOOTH_DEVICE:
        return "BLUETOOTH_DEVICE";
      case ZIGBEE_DEVICE:
        return "ZIGBEE_DEVICE";
      case UNKNOWN:
        default:
          return "UNKNOWN";
    }
  }
}
