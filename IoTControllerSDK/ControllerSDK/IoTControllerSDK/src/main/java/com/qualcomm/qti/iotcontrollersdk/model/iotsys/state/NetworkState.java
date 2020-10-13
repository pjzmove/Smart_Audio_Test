/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.iotsys.state;

import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.NetworkAttr;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.NetworkInterfaceAttr;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.ResourceState;
import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;

import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class NetworkState extends ResourceState {

  private final NetworkAttr mNetworkAttr = new NetworkAttr();

  public NetworkState() {
    isAvailable = false;
  }

  public synchronized void update(NetworkAttr attr) {
      isAvailable = true;
      GenericStateApi.setState(mNetworkAttr, attr);
  }

  public synchronized boolean update(OcRepresentation rep) throws OcException {
    isAvailable = true;
    return GenericStateApi.updateState(mNetworkAttr, rep);
  }

  public synchronized boolean isAvailable() {
    return isAvailable;
  }

  public synchronized NetworkInterfaceAttr getWifiAdapter() {
    return GenericStateApi.getState(mNetworkAttr.mWifiAdapter);
  }

  public synchronized NetworkInterfaceAttr getEthrnerAdapter() {
    return GenericStateApi.getState(mNetworkAttr.mEthernetAdapter);
  }

}
