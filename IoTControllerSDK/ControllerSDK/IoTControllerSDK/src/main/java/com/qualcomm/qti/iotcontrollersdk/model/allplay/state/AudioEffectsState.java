/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.AudioEffectsAttr;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class AudioEffectsState extends ResourceState {


  private final AudioEffectsAttr audioEffectsAttr = new AudioEffectsAttr();


  public AudioEffectsState() {
  }

  public synchronized void update(AudioEffectsAttr attr) {
    GenericStateApi.setState(audioEffectsAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    return GenericStateApi.updateState(audioEffectsAttr, rep);
  }

}
