/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller.interfaces;


import org.iotivity.base.OcException;

/**
 * The interface defines the callback methods for calling services asynchronously.
 * {@link com.qualcomm.qti.iotcontrollersdk.controller.IoTAllPlayClient}
 * {@link com.qualcomm.qti.iotcontrollersdk.controller.IoTSysClient}
 */
public interface IoTCompletionCallback {

  /**
   * The callback method for calling methods defined in the following services asynchronously
   * {@link com.qualcomm.qti.iotcontrollersdk.controller.IoTAllPlayClient}
   * {@link com.qualcomm.qti.iotcontrollersdk.controller.IoTSysClient}
   */
  void onCompletion(boolean success);

  /**
   * The callback methods when OcException is raised
   */
  default void onException(OcException ex) {
    ex.printStackTrace();
  }

}

