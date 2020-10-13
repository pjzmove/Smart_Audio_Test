/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PresetReverbAttr;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class PresetRevertState extends ResourceState {

  private final PresetReverbAttr presetReverbAttr = new PresetReverbAttr();

  public PresetRevertState() {
  }

  public synchronized void update(PresetReverbAttr attr) {
    GenericStateApi.setState(presetReverbAttr, attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
      return GenericStateApi.updateState(presetReverbAttr, rep);
  }

  public synchronized PresetReverbAttr getAttribute() {
      return GenericStateApi.getState(presetReverbAttr);
  }

  public synchronized String getCurrentPreset() {
      return presetReverbAttr.mCurrentPreset;
  }

  public synchronized boolean isEnabled() {
      return presetReverbAttr.mEnabled;
  }

  public synchronized List<String> getPresets() {
      return GenericStateApi.getPrimitiveTypeList(presetReverbAttr.mPresets);
  }
}
