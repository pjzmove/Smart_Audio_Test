/*
 * *************************************************************************************************
 *  * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;

public class IoTSysInfo {
    public String name;
    public String id;
    public String mHost;
    private String mFirmwareVersion;
    private String mModel;
    private String mManufacturer;

    public boolean isVoicdUIEnabled = false;
    public boolean isBluetoothOnBoarded;
    public boolean isZigBeeOnBoarded;
    public boolean isAvsOnBoarded;
    public boolean isCortanaOnboarded = false;
    public boolean isModularOnBoarded =false;
    public boolean isGoogleOnboarded = false;
    public boolean isAVSWakeWord;
    public String mAVSLanguage;


  public class VoiceClient {
    String name;
    String version;

    public VoiceClient(String name, String version) {
      this.name = name;
      this.version = version;
    }
  }

  public IoTSysInfo(String name, String id, String host,boolean voiceUIenabled, boolean avsOnboarded) {
      this.name = name;
      this.id = id;
      this.mHost = host;
      isVoicdUIEnabled = voiceUIenabled;
      isAvsOnBoarded = avsOnboarded;
      isBluetoothOnBoarded = false;
      isZigBeeOnBoarded = false;
    }

    public String getFirmwareVersion() {
       return mFirmwareVersion;
    }

    public String getModel() {
      return mModel;
    }

    public String getManufacturer() {
      return mManufacturer;
    }

    public void updateSystemInfo(IoTDevice device) {
      this.mFirmwareVersion = device.getFirmwareVersion();
      this.mModel = device.getModel();
      this.mManufacturer = device.getManufacturer();
    }

}
