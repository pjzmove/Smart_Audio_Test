/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

public abstract class ResourceState {

  protected boolean isAvailable = false;

  public synchronized void setAvailable(boolean available) {
    isAvailable = available;
  }

  public synchronized boolean isAvailable() {
    return isAvailable;
  }

}
