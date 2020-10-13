/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import static com.qualcomm.qti.iotcontrollersdk.constants.IoTType.SATELLITE_SPEAKER;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import java.util.List;

/**
 * The class provides APIs for the app to access to Surround speaker information
 */
public class IoTSurround extends IoTRepository {

  /**
   * Class constructor
   *
   * @param displayName is the name defined by remote devices
   * @param displayName is the ID of remote devices ID
   */
  /*package*/ IoTSurround(String displayName, String deviceId)  {
    super(SATELLITE_SPEAKER);
    this.name = displayName;
    this.id = deviceId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public IoTType getType() {
    return type;
  }

  @Override
  public List<? extends IoTRepository> getList() {
    return null;
  }

  @Override
  public boolean dispose() {
    return true;
  }
}
