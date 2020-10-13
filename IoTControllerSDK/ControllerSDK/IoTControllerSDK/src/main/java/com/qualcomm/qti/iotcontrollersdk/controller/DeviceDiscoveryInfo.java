/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;


import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;

public class DeviceDiscoveryInfo {

  enum ScanType {
    ALLPLAY,
    IOTSYS
  }

  public String mHost;
  public String mIdentifier;
  String mName;
  private boolean isAllPlayFound;
  private boolean isIoTsysFound;
  ScanType mType;
  long mLastUpdated;

  DeviceDiscoveryInfo(String host, String id, ScanType type) {
    mName = "";
    mHost = host;
    mIdentifier = id;
    mLastUpdated = System.currentTimeMillis();
    isAllPlayFound = false;
    isIoTsysFound = false;
    mType = type;
  }

  DeviceDiscoveryInfo(DeviceDiscoveryInfo that) {
    this.mName = that.mName;
    this.mHost = that.mHost;
    this.mIdentifier = that.mIdentifier;
    this.isAllPlayFound = that.isAllPlayFound;
    this.isIoTsysFound = that.isIoTsysFound;
    this.mType = that.mType;
    this.mLastUpdated = that.mLastUpdated;
  }

  synchronized long getLastUpdate() {
    return mLastUpdated;
  }

  synchronized void updateTime() {
    mLastUpdated = System.currentTimeMillis();
  }

  synchronized void setAllPlayFoundFlag(boolean flag) {
     isAllPlayFound = flag;
  }

  synchronized boolean isAllPlayFound() {
    return isAllPlayFound;
  }

  synchronized void setIoTSysFoundFlag(boolean flag) {
     isIoTsysFound = flag;
  }

  synchronized boolean isIoTSysFound() {
    return isIoTsysFound;
  }

  @Override
  public boolean equals(Object o) {
      DeviceDiscoveryInfo that = (DeviceDiscoveryInfo) o;
      String thisHost = ControllerSdkUtils.stripHostName(mHost);
      String thatHost = ControllerSdkUtils.stripHostName(that.mHost);
      return (that != null && thisHost.equalsIgnoreCase(thatHost) && mType == that.mType);
  }

}
