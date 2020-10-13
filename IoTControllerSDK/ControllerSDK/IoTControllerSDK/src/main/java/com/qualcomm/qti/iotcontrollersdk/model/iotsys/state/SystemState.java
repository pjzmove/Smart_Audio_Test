/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys.state;

import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BatteryStatusAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.SystemAttr;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.ResourceState;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class SystemState extends ResourceState {

  private final SystemAttr mSystemAttr = new SystemAttr();

  public SystemState() {
  }

  public synchronized void update(SystemAttr attr) {
     GenericStateApi.setState(mSystemAttr, attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
     return GenericStateApi.updateState(mSystemAttr, rep);
  }

  public synchronized SystemAttr getData() {
     return GenericStateApi.getState(mSystemAttr);
  }

  public synchronized BatteryStatusAttr getBatteryStatus() {
     return GenericStateApi.getState(mSystemAttr.mBatteryStatus);
  }

  public synchronized void setBatteryStatus( BatteryStatusAttr batteryStatus) {
    GenericStateApi.setState(mSystemAttr.mBatteryStatus, batteryStatus);
  }

  public synchronized boolean isBatterySupported() {
      return mSystemAttr.mBatterySupported;
  }

  public synchronized void setBatterySupported(boolean batterySupported) {
    mSystemAttr.mBatterySupported = batterySupported;
  }

  public synchronized String getDeviceFriendlyName() {
    return mSystemAttr.mDeviceFriendlyName;
  }

  public synchronized void setDeviceFriendlyName(String deviceFriendlyName) {
    mSystemAttr.mDeviceFriendlyName = deviceFriendlyName;
  }

  public synchronized String getFirmwareVersion() {
    return mSystemAttr.mFirmwareVersion;
  }

  public synchronized void setFirmwareVersion(String firmwareVersion) {
    mSystemAttr.mFirmwareVersion = firmwareVersion;
  }

  public synchronized String getManufacturer() {
    return mSystemAttr.mManufacturer;
  }

  public synchronized void setManufacturer(String manufacturer) {
    mSystemAttr.mManufacturer = manufacturer;
  }

  public synchronized String getModel() {
     return mSystemAttr.mModel;
  }

  public synchronized void setModel(String model) {
    mSystemAttr.mModel = model;
  }
}
