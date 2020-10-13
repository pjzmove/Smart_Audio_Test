/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.DiscoveryInterface;
import java.util.EnumSet;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcPlatform.OnDeviceFoundListener;
import org.iotivity.base.OcPlatform.OnResourceFoundListener;

/**
 * The singleton class provide APIs for device and resource discovery
 */
public class IoTDiscovery implements DiscoveryInterface{

  private static IoTDiscovery mInstance = new IoTDiscovery();

  /**
   * Get the singleton instance for device and resource discovery implementation
   *
   * @return the IoTDiscovery instance
   */
  public static IoTDiscovery getInstance() {
    return mInstance;
  }

  @Override
  public void getDeviceInfo(String host, String deviceUri,
      EnumSet<OcConnectivityType> connectivityTypeSet, OnDeviceFoundListener onDeviceFoundListener)
      throws OcException {

      OcPlatform.getDeviceInfo(host,
                    OcPlatform.WELL_KNOWN_DEVICE_QUERY,
                    EnumSet.of(OcConnectivityType.CT_ADAPTER_IP),
                    onDeviceFoundListener);
  }

  @Override
  public void findResource(String host, String resourceUri,
      EnumSet<OcConnectivityType> connectivityTypeSet,
      OnResourceFoundListener onResourceFoundListener) throws OcException {

      OcPlatform.findResource(host,
                    resourceUri,
                    EnumSet.of(OcConnectivityType.CT_ADAPTER_IP),
                    onResourceFoundListener);
  }
}
