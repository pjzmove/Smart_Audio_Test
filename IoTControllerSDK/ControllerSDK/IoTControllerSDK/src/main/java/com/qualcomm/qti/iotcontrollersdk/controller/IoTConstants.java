/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

/**
 * The constants definitions for Controller SDk
 *
 */
public class IoTConstants {

  /**
   * Allplay OCF resource type value
   */
  public static final String OCF_RESOURCE_TYPE_ALLPLAY = "qti.d.allplay";

  /**
   * IoTSys OCF resource type value
   */
  public static final String OCF_RESOURCE_TYPE_IOTSYS = "qti.d.iotsys";

  /**
   * Invalid value
   */
  public static final int INVALID_VALUE = -1;
  public static final int ZIGBEE_FORMATION_REQUEST_TIMEOUT = 10;
  public static final int ZIGBEE_JOINING_REQUEST_TIMEOUT = 11;

  static final int ALLPLAY_RESOURCE_NUMBER = 8;
  static final int IOTSYS_RESOURCE_NUMBER = 4;
  static final String OC_RSRVD_DEVICE_ID = "di";
  static final int START_DISCOVERY = 1;
  static final int DEVICE_DISCOVERY_TIMEOUT = 2;
  static final int FAST_SCAN_TIME_INTERVAL_IN_MS = 3 * 1000;
  static final int SLOW_SCAN_TIME_INTERVAL_IN_MS = 5 * 1000;
  static final long MAX_TIME_DELTA_IN_MS = 5 * SLOW_SCAN_TIME_INTERVAL_IN_MS;

}
