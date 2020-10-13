/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller.interfaces;

import java.util.List;
import java.util.Map;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResource.OnGetListener;
import org.iotivity.base.OcResource.OnObserveListener;
import org.iotivity.base.OcResource.OnPostListener;
import org.iotivity.base.QualityOfService;

/**
 * Interface definition for Allplay and IoTSys service to access to property attributes
 */
public interface ResourceInterface {

    /**
     * Wrapper Creates a resource proxy object so that get/put/observe functionality can be used without
     * discovering the object in advance.
     *
     * @param host                a string containing a resolvable host address of the server
     *                            holding the resource. Currently this should be in the format of
     *                            coap://address:port for IPv4  and in the format of
     *                            coap://[address%25ZoneID]:port for IPv6. In the future,
     *                            we expect "coap:" section is removed from this format.
     * @param uri                 the rest of the resource's URI that will permit messages to be
     *                            properly routed.
     * @param resourceTypeList    a collection of resource types implemented by the resource
     * @param interfaceList       a collection of interfaces that the resource supports/implements
     * @param observable          a boolean containing whether the resource supports observation
     * @return new resource object
     * @throws OcException if failure
     */
    OcResource construct( String host, String uri,
                              List<String> resourceTypeList,
                              List<String> interfaceList,
                              boolean observable) throws OcException;

    /**
     * Method to get the attributes of a resource.
     *
     * @param queryParamsMap    map which can have the query parameter name and value
     * @param onGetListener     The event handler will be invoked with a map of attribute name and
     *                          values. The event handler will also have the result from this Get
     *                          operation This will have error codes
     * @param qualityOfService  the quality of communication
     * @throws OcException if failure
     */
    void get(Map<String, String> queryParamsMap,
                OnGetListener onGetListener,
                QualityOfService qualityOfService) throws OcException;


    /**
     * Method to POST on a resource
     *
     * @param ocRepresentation representation of the resource
     * @param queryParamsMap   Map which can have the query parameter name and value
     * @param onPostListener   event handler The event handler will be invoked with a map of
     *                         attribute name and values.
     * @param qualityOfService  the quality of communication
     * @throws OcException if failure
     */
    void post(OcRepresentation ocRepresentation,
                 Map<String, String> queryParamsMap,
                 OnPostListener onPostListener,
                 QualityOfService qualityOfService) throws OcException;

    /**
     * Method to set observation on the resource
     *
     * @param observeType       allows the client to specify how it wants to observe
     * @param queryParamsMap    map which can have the query parameter name and value
     * @param onObserveListener event handler The handler method will be invoked with a map
     *                          of attribute name and values.
     * @throws OcException Failed to set observation.
     *                     Use OcException.GetErrorCode() for more details.
     */
    void observe(int observeType,
                    Map<String, String> queryParamsMap,
                    OnObserveListener onObserveListener) throws OcException;


    /**
     * Method to cancel the observation on the resource
     *
     * @throws OcException Failed to cancel observation.
     *                     Use OcException.GetErrorCode() for more details.
     */
    boolean stopObserving() throws OcException;

    /**
     * Check if remote device supports GET method.
     *
     * @return true if support
     */
    boolean isGetSupported () ;

    /**
     * Check if resource attribute supports POST method.
     *
     * @return true if support
     */
    boolean isPostSupported();

    /**
     * Check if resource is observable.
     *
     * @return true if support
     */
    boolean isObserveSupported();

    /**
     * Check if resource is available.
     *
     * @return true if support
     */
    boolean isResourceAvailable();

    /**
     * set resource availability
     *
     * @param available
     */
    void setResourceAvailable(boolean available);
}
