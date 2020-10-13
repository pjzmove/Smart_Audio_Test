/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VolumeControlAttr;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class VolumeControlState extends ResourceState {

  private final VolumeControlAttr volumeControllerAttr = new VolumeControlAttr();

  public VolumeControlState() {
  }

  public synchronized VolumeControlAttr getAttribute() {
      VolumeControlAttr ret = (VolumeControlAttr) volumeControllerAttr.getData();
      return ret;
  }

  public synchronized void update(VolumeControlAttr attr) {
    isAvailable = true;
    GenericStateApi.setState(volumeControllerAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    isAvailable = true;
    return GenericStateApi.updateState(volumeControllerAttr, rep);
  }

  public synchronized boolean isVolumeEnabled() {
    return isAvailable;
  }

  public synchronized boolean isMute() {
      return GenericStateApi.getState(volumeControllerAttr.mMute);
  }

  public synchronized void setMute(boolean mute) {
    volumeControllerAttr.mMute =  mute;
  }

  public synchronized double getStep() {
      return volumeControllerAttr.mStep;
  }

  public synchronized void setStep(double step) {
    volumeControllerAttr.mStep = step;
  }

  public synchronized int getVersion() {
      return volumeControllerAttr.mVersion;
  }

  public synchronized void setVersion(int version) {
    volumeControllerAttr.mVersion = version;
  }

  public synchronized double getVolume() {
      return volumeControllerAttr.mVolume;
  }

  public synchronized boolean getMute() {
      return volumeControllerAttr.mMute;
  }


  public synchronized double getMaxVolume() {
    return 1.0f;
  }

  public synchronized double getMinVolume() {
    return 0.0f;
  }

  public synchronized void setVolume(double volume) {
    volumeControllerAttr.mVolume = volume;
  }

}
