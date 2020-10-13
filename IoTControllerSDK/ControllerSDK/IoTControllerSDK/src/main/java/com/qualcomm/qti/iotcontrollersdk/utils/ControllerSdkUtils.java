/* *************************************************************************************************
 *  * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                *
 *  ************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Controller SDK shared utility class
 */

public class ControllerSdkUtils {

  private final static int eCode = 0;
  private final static String PREFIX_COAP_HOST = "coap://";
  private final static String PREFIX_COAP_HOST_CAP = "COAP://";

  private ControllerSdkUtils() {
  }

  /**
   * Null check method
   *
   * @param T is the object references
   * @param message is a detail message that describes this particular exception
   *
   */
  public static <T> T checkNotNull(@Nullable T obj, String message) {
    if(obj == null) {
      throw new PlayerNullPointerException(eCode,message);
    }
    return obj;
  }

  /**
   * Extract IP address from CoAP Host
   *
   * <p>Coaps URI host scheme is the following:
   * <p>coap-host = "coap:" "//" host [ ":" port ]
   *
   * @param host CoAP Host
   * @return Device IP address
   */
  public static String stripHostName(String host) {
     host = host.replace(PREFIX_COAP_HOST,"").replace(PREFIX_COAP_HOST_CAP,"");
     int idx = host.lastIndexOf(":");
     if(idx < 0) return host;
     return  host.substring(0,idx);
	}

}
