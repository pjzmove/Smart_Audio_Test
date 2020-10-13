/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MultichannelGroupSatelliteAttr;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class MultichannelGroupSatellite extends ResourceState {

  private final MultichannelGroupSatelliteAttr multiChannelGroupSatelliteAttr = new MultichannelGroupSatelliteAttr();

  public MultichannelGroupSatellite() {
  }

  public synchronized void update(MultichannelGroupSatelliteAttr attr) {
    isAvailable = true;
    GenericStateApi.setState(multiChannelGroupSatelliteAttr,attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    isAvailable = true;
    return GenericStateApi.updateState(multiChannelGroupSatelliteAttr, rep);
  }

  public synchronized int getVersion() {
     return multiChannelGroupSatelliteAttr.mVersion;
  }
}
