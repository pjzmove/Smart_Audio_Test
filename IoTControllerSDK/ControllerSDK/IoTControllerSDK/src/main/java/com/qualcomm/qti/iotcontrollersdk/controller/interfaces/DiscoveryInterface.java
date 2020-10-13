/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller.interfaces;

import java.util.EnumSet;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform.OnDeviceFoundListener;
import org.iotivity.base.OcPlatform.OnResourceFoundListener;

/*
 * The interface for Controller SDK to discover devices and resources through IoTivity Stack
 */
public interface DiscoveryInterface {

  /**
     * A wrapper API for Device Discovery through IoTivity stack
     *
     * @param host                  Host IP Address. If null or empty, Multicast is performed.
     * @param deviceUri             Uri containing address to the virtual device
     * @param connectivityTypeSet   Set of types of connectivity. Example: IP
     * @param onDeviceFoundListener Handles events, success states and failure states.
     * @throws OcException if failure
     */
  void getDeviceInfo(
            String host,
            String deviceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnDeviceFoundListener onDeviceFoundListener) throws OcException ;

  /**
     * A wrapper API for Service and Resource Discovery through IoTivity stack
     * <p>
     * Note: This API is for client side only.
     * </p>
     * <p>
     * Note: To get the tcpPort information, user must call getAllHosts() from the OCResource
     * object in the onResourceFound callback. To use a TCP endpoint that has been received
     * by getAllHosts(), user must use the setHost() method.
     * </p>
     *
     * @param host                    Host Address of a service to direct resource discovery query.
     *                                If empty, performs multicast resource discovery query
     * @param resourceUri             name of the resource. If null or empty, performs search for all
     *                                resource names
     * @param connectivityTypeSet     Set of types of connectivity. Example: IP
     * @param onResourceFoundListener Handles events, success states and failure states.
     * @throws OcException if failure
     */
  void findResource(
            String host,
            String resourceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnResourceFoundListener onResourceFoundListener) throws OcException;

}
