/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import android.support.annotation.NonNull;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import java.util.List;


/**
 * Abstract class defines generic APIs so that the rest of the app can retrieve this data easily.
 */
public abstract class IoTRepository implements Comparable<IoTRepository> {

  public String name;
  public String id;
  public IoTType type;

  public IoTRepository(IoTType type) {
    this.type = type;
  }

  /**
   * Get the device name
   *
   * @return the device name
   */
  abstract public String getName();

  /**
   * Get the device ID
   *
   * @return the device ID
   */
  abstract public String getId();

  /**
   * Get the device type
   *
   * @return {@link IoTType}
   */
  abstract public IoTType getType();

  /**
   * Get the device list
   *
   * @return the list of devices
   */
  abstract public List<? extends IoTRepository> getList();

  /**
   * Cancel observing resource notification
   *
   * @return true if success
   */
  abstract public boolean dispose();

  @Override
  public int compareTo(@NonNull IoTRepository o) {
    if(type.getValue() > o.type.getValue()) {
      return 1;
    } else if(type.getValue() < o.type.getValue()) {
      return -1;
    } else {
      return getName().compareTo(o.getName());
    }
  }
}
