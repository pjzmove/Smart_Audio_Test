/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.BassboostAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EffectRangeAttr;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class BassBoostState extends ResourceState {

  private final BassboostAttr bassBoostAttr = new BassboostAttr();
  private boolean isAvailable;

  public BassBoostState() {
  }

  public synchronized void update(BassboostAttr attr) {
    GenericStateApi.setState(bassBoostAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
     return GenericStateApi.updateState(bassBoostAttr, rep);
  }

  public synchronized BassboostAttr getAttribute() {
     return GenericStateApi.getState(bassBoostAttr);
  }

  public synchronized boolean isEnabled() {
     return bassBoostAttr.mEnabled;
  }

  public synchronized int getStrength() {
     return bassBoostAttr.mStrength;
  }

  public synchronized EffectRangeAttr getStrengthRange() {
     return GenericStateApi.getState(bassBoostAttr.mStrengthRange);
  }

}
