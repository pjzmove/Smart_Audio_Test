/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EffectRangeAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VirtualXSoundXAttr;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class DtsVirtaulXState extends ResourceState {

  private final VirtualXSoundXAttr dtsVirtualX = new VirtualXSoundXAttr();

  public DtsVirtaulXState() {
   }

   public synchronized void update(VirtualXSoundXAttr attr) {
     GenericStateApi.setState(dtsVirtualX,attr);
   }

   public synchronized boolean update(OcRepresentation rep) throws OcException {

      if(rep == null) return false;
      return dtsVirtualX.checkDifference(rep) && GenericStateApi.updateState(dtsVirtualX, rep);
  }

   public synchronized VirtualXSoundXAttr getAttribute() {
       return GenericStateApi.getState(dtsVirtualX);
   }

  public synchronized boolean isDialogClarityEnabled() {
      return dtsVirtualX.mDialogClarityEnabled;
  }

  public synchronized boolean isEnabled() {
      return dtsVirtualX.mEnabled;
  }

  public synchronized int getOutMode() {
      return dtsVirtualX.mOutMode;
  }

  public synchronized EffectRangeAttr getOutModeRange() {
      return GenericStateApi.getState(dtsVirtualX.mOutModeRange);
  }
}
