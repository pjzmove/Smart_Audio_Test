/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EqualizerAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EqualizerPropertyAttr;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class EqualizerState extends ResourceState {

  private final EqualizerAttr equalizerAttr = new EqualizerAttr();

  public EqualizerState() {
  }

  public synchronized void update(EqualizerAttr attr) {
    GenericStateApi.setState(equalizerAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
      return GenericStateApi.updateState(equalizerAttr, rep);
  }

  public synchronized EqualizerAttr getAttribute() {
      return GenericStateApi.getState(equalizerAttr);
  }

  public synchronized String getCurrentPreset() {
      return equalizerAttr.mCurrentPreset;
  }

  public synchronized boolean isEnabled() {
      return equalizerAttr.mEnabled;
  }

  public synchronized EqualizerPropertyAttr getEqualizerProperty() {
      return GenericStateApi.getState(equalizerAttr.mEqualizerProperty);
  }

  public synchronized List<String> getPresets() {
      return GenericStateApi.getPrimitiveTypeList(equalizerAttr.mPresets);
  }

}
