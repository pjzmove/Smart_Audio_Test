/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import static org.iotivity.base.ObserveType.OBSERVE;
import static org.iotivity.base.ObserveType.OBSERVE_ALL;

import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.ResourceInterface;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResource.OnGetListener;
import org.iotivity.base.OcResource.OnObserveListener;
import org.iotivity.base.OcResource.OnPostListener;
import org.iotivity.base.QualityOfService;

/**
 *  Based class for resource client
 *
 */
public class IoTBaseResourceClient implements ResourceInterface {

  public String mHost;
  /*package*/ OcResource mResource;
  private boolean isResourceAvailable = false;

  /**
   * default class Constructor
   */
  public IoTBaseResourceClient() {

  }

  /**
   * default class Constructor
   *
   * @param host CoAP host uri
   */
  public IoTBaseResourceClient(String host) {
    isResourceAvailable = false;
    mHost = host;
  }

  @Override
  public OcResource construct(String host, String uri, List<String> resourceTypeList,
      List<String> interfaceList, boolean observable) throws OcException {

    mResource = OcPlatform.constructResourceObject(host,
                    uri,
                    EnumSet.of(OcConnectivityType.CT_ADAPTER_IP),
                    observable,
                    resourceTypeList,
                    interfaceList);
    return mResource;
  }

  @Override
  public void get(Map<String, String> queryParamsMap, OnGetListener onGetListener,
      QualityOfService qualityOfService) throws OcException {

     if(mResource != null)
      mResource.get(queryParamsMap, onGetListener,qualityOfService);
  }

  @Override
  public void post(OcRepresentation ocRepresentation, Map<String, String> queryParamsMap,
      OnPostListener onPostListener, QualityOfService qualityOfService) throws OcException {

    if(mResource != null)
      mResource.post(ocRepresentation,queryParamsMap,onPostListener,qualityOfService);
  }

  @Override
  public void observe(int observeType, Map<String, String> queryParamsMap,
      OnObserveListener onObserveListener) throws OcException {

    if(mResource != null)
      mResource.observe(observeType==0?OBSERVE:OBSERVE_ALL,queryParamsMap,onObserveListener);
  }

  @Override
  public boolean stopObserving() throws OcException {
    if(mResource != null && isResourceAvailable()) {
      mResource.cancelObserve(QualityOfService.HIGH);
      return true;
    }
    return false;
  }

  @Override
  public boolean isGetSupported () {
    return false;
  }

  @Override
  public boolean isPostSupported() {
    return false;
  }

  @Override
  public boolean isObserveSupported() {
    return false;
  }

  @Override
  public boolean isResourceAvailable() {
    return isObserveSupported() && isResourceAvailable;
  }

  @Override
  public void setResourceAvailable(boolean available) {
     isResourceAvailable = available;
  }
}
