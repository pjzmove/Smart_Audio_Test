/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.constants;

/**
 * This class provides APIs for converting home channel to channel number defined by surround system and vice-versa
 */

public class MultiChannelMapping {

  /**
   * Enumeration of Home Theater Channel.
   */
  public enum HomeTheaterChannel {

    NONE,
    /**
     * Subwoofer
     */
    SUBWOOFER,
    LEFT_CHANNEL,
    RIGHT_CHANNEL,
    CENTER_CHANNEL,

    /**
     * Left surround
     */
    LEFT_SURROUND,
    /**
     * Right surround
     */
    RIGHT_SURROUND,
    LEFT_REAR_SURROUND,
    RIGHT_REAR_SURROUND,
    LEFT_UPFIRING_SURROUND,
    RIGHT_UPFIRING_SURROUND,
    LEFT_REARUPFIRING_SURROUND,
    RIGHT_REARUPFIRING_SURROUND,
  }

  /**
   * Enumeration of underlying .
   */
  public enum IoTChannelMap {
    IoTChannelMapNumberNone,
    IoTChannelMapNumberFrontLeft,
    IoTChannelMapNumberFrontRight,
    IoTChannelMapNumberFrontCenter,
    IoTChannelMapNumberSubwoofer,
    IoTChannelMapNumberSideLeft,
    IoTChannelMapNumberSideRight,
    IoTChannelMapNumberBackLeft,
    IoTChannelMapNumberBackRight,
    IoTChannelMapNumberTopFrontLeft,
    IoTChannelMapNumberTopFrontRight,
    IoTChannelMapNumberTopBackLeft,
    IoTChannelMapNumberTopBackRight,
  }

  /**
   * Convert underlying channel number to surround system channel number.
   * @param ch The value of a given home theater channel.
   * @return HomeTheaterChannel The Home theater channel number
   */
  public static HomeTheaterChannel getHomeTheaterChannel(IoTChannelMap ch) {
    HomeTheaterChannel channel = HomeTheaterChannel.NONE;
    switch (ch) {
        case IoTChannelMapNumberNone:
            channel = HomeTheaterChannel.NONE;
            break;
        case IoTChannelMapNumberFrontLeft:
            channel = HomeTheaterChannel.LEFT_CHANNEL;
            break;
        case IoTChannelMapNumberFrontRight:
            channel = HomeTheaterChannel.RIGHT_CHANNEL;
            break;
        case IoTChannelMapNumberFrontCenter:
            channel = HomeTheaterChannel.CENTER_CHANNEL;
            break;
        case IoTChannelMapNumberSubwoofer:
            channel = HomeTheaterChannel.SUBWOOFER;
            break;
        case IoTChannelMapNumberSideLeft:
            channel = HomeTheaterChannel.LEFT_SURROUND;
            break;
        case IoTChannelMapNumberSideRight:
            channel = HomeTheaterChannel.RIGHT_SURROUND;
            break;
        case IoTChannelMapNumberBackLeft:
            channel = HomeTheaterChannel.LEFT_REAR_SURROUND;
            break;
        case IoTChannelMapNumberBackRight:
            channel = HomeTheaterChannel.RIGHT_REAR_SURROUND;
            break;
        case IoTChannelMapNumberTopFrontLeft:
            channel = HomeTheaterChannel.LEFT_UPFIRING_SURROUND;
            break;
        case IoTChannelMapNumberTopFrontRight:
            channel = HomeTheaterChannel.RIGHT_UPFIRING_SURROUND;
            break;
        case IoTChannelMapNumberTopBackLeft:
            channel = HomeTheaterChannel.LEFT_UPFIRING_SURROUND;
            break;
        case IoTChannelMapNumberTopBackRight:
            channel = HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND;
            break;
        default:
           channel = HomeTheaterChannel.NONE;
            break;
      }
      return channel;
    }

    /**
     * Convert a surround system channel number to a channel number defined by surround system
     * @param ch The Home theater channel number
     * @return IoTChannelMap The value of a given home theater channel.
     */
    public static IoTChannelMap getIoTChannelMap(HomeTheaterChannel ch) {

        IoTChannelMap channel = IoTChannelMap.IoTChannelMapNumberNone;
        switch (ch) {
          case NONE:
              channel = IoTChannelMap.IoTChannelMapNumberNone;
              break;
          case SUBWOOFER:
              channel = IoTChannelMap.IoTChannelMapNumberSubwoofer;
              break;
          case LEFT_CHANNEL:
              channel = IoTChannelMap.IoTChannelMapNumberFrontLeft;
              break;
          case RIGHT_CHANNEL:
                channel = IoTChannelMap.IoTChannelMapNumberFrontRight;
                break;
          case CENTER_CHANNEL:
              channel = IoTChannelMap.IoTChannelMapNumberFrontCenter;
              break;
          case LEFT_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberSideLeft;
              break;
          case RIGHT_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberSideRight;
              break;
          case LEFT_REAR_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberBackLeft;
              break;
          case RIGHT_REAR_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberBackRight;
              break;
          case LEFT_UPFIRING_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberTopFrontLeft;
              break;
          case RIGHT_UPFIRING_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberTopFrontRight;
              break;
          case LEFT_REARUPFIRING_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberTopBackLeft;
              break;
          case RIGHT_REARUPFIRING_SURROUND:
              channel = IoTChannelMap.IoTChannelMapNumberTopBackRight;
              break;

        }
      return channel;
    }

    /**
     * Convert an integer value to a channel number defined by surround system
     * @param channelNumber The integer value for a given surround system channel
     * @return IoTChannelMap The value of a given home theater channel
     */
    public static IoTChannelMap getIoTChannelMap(int channelNumber) {

        IoTChannelMap channel = IoTChannelMap.IoTChannelMapNumberNone;
        switch (channelNumber) {
          case 0:
              channel = IoTChannelMap.IoTChannelMapNumberNone;
              break;
          case 1:
              channel = IoTChannelMap.IoTChannelMapNumberFrontLeft;
              break;
          case 2:
              channel = IoTChannelMap.IoTChannelMapNumberFrontRight;
              break;
          case 3:
                channel = IoTChannelMap.IoTChannelMapNumberFrontCenter;
                break;
          case 4:
              channel = IoTChannelMap.IoTChannelMapNumberSubwoofer;
              break;
          case 5:
              channel = IoTChannelMap.IoTChannelMapNumberSideLeft;
              break;
          case 6:
              channel = IoTChannelMap.IoTChannelMapNumberSideRight;
              break;
          case 7:
              channel = IoTChannelMap.IoTChannelMapNumberBackLeft;
              break;
          case 8:
              channel = IoTChannelMap.IoTChannelMapNumberBackRight;
              break;
          case 9:
              channel = IoTChannelMap.IoTChannelMapNumberTopFrontLeft;
              break;
          case 10:
              channel = IoTChannelMap.IoTChannelMapNumberTopFrontRight;
              break;
          case 11:
              channel = IoTChannelMap.IoTChannelMapNumberTopBackLeft;
              break;
          case 12:
              channel = IoTChannelMap.IoTChannelMapNumberTopBackRight;
              break;
          default:
              channel = IoTChannelMap.IoTChannelMapNumberNone;
              break;

        }
      return channel;
    }

}

