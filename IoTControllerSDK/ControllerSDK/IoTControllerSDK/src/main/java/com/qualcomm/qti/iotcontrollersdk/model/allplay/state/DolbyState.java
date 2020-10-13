/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.DolbyAttr;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class DolbyState extends ResourceState {

  private final DolbyAttr dolbyAttr = new DolbyAttr();

  public DolbyState() {
  }

  public synchronized void update(DolbyAttr attr) {
    GenericStateApi.setState(dolbyAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
      if(rep == null) return false;
      return (dolbyAttr.checkDifference(rep) && GenericStateApi.updateState(dolbyAttr, rep));
  }

  public synchronized boolean checkDifference(OcRepresentation rep) throws OcException {
    return dolbyAttr.checkDifference(rep);

  }

  public synchronized DolbyAttr getAttribute() {
      return GenericStateApi.getState(dolbyAttr);
  }

  public synchronized boolean isEnabled() {
      return dolbyAttr.mEnabled;
  }

  public synchronized void setEnabled(boolean enabled) {
      dolbyAttr.mEnabled = enabled;
  }

  public synchronized int geMode() {
    return dolbyAttr.mMode;
  }

}
