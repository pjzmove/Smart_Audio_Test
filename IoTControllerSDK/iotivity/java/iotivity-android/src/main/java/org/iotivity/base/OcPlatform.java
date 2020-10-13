/*
 *******************************************************************
 *
 * Copyright 2015 Intel Corporation.
 *
 *-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
 */

package org.iotivity.base;

import org.iotivity.ca.CaInterface;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains the main entrance/functionality of the product. To set a custom
 * configuration, the implementer must make a call to OcPlatform.Configure before the first usage
 * of a method in this class.
 */
public final class OcPlatform {

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("connectivity_abstraction");
        System.loadLibrary("oc_logger");
        System.loadLibrary("octbstack");
        System.loadLibrary("oc");
        if (0 != BuildConfig.SECURED)
        {
            System.loadLibrary("ocprovision");
        }
        System.loadLibrary("ocstack-jni");
    }

    /**
     * Default interface
     */
    public static final String DEFAULT_INTERFACE = "oic.if.baseline";

    /**
     * Used in discovering (GET) links to other resources of a collection
     */
    public static final String LINK_INTERFACE = "oic.if.ll";

    /**
     * Used in GET, PUT, POST, DELETE methods on links to other resources of a collection
     */
    public static final String BATCH_INTERFACE = "oic.if.b";

    /**
     * Used in GET, PUT, POST methods on links to other remote resources of a group
     */
    public static final String GROUP_INTERFACE = "oic.mi.grp";

    public static final String WELL_KNOWN_QUERY = "/oic/res";
    public static final String WELL_KNOWN_DEVICE_QUERY = "/oic/d";
    public static final String WELL_KNOWN_PLATFORM_QUERY = "/oic/p";
    public static final int DEFAULT_PRESENCE_TTL = 60;
    public static final String PRESENCE_URI = "/oic/ad";

    private static volatile boolean sIsPlatformInitialized = false;
    private static QualityOfService sPlatformQualityOfService = QualityOfService.NA;

    private static volatile boolean sIsStopPlatform = true;

    private OcPlatform() {
    }

    /**
     * API for setting the configuration of the OcPlatform.
     * <p>
     * Note: Any calls made to this AFTER the first call to OcPlatform.Configure will have no affect
     * </p>
     *
     * @param platformConfig platform configuration
     */
    public synchronized static void Configure(PlatformConfig platformConfig) {
        if (!sIsPlatformInitialized) {
            CaInterface.initialize(platformConfig.getActivity(), platformConfig.getContext());

            sPlatformQualityOfService = platformConfig.getQualityOfService();

            OcPlatform.configure(
                    platformConfig.getServiceType().getValue(),
                    platformConfig.getModeType().getValue(),
                    platformConfig.getIpAddress(),
                    platformConfig.getPort(),
                    platformConfig.getQualityOfService().getValue(),
                    platformConfig.getSvrDbPath(),
                    platformConfig.getIntrospectionPath(),
                    platformConfig.getAvailableTransportType()
            );

            sIsPlatformInitialized = true;
        }

        if (sIsStopPlatform)
        {
            OcPlatform.start();
            sIsStopPlatform = false;
        }
    }

    private static native void configure(int serviceType,
                                         int modeType,
                                         String ipAddress,
                                         int port,
                                         int qualityOfService,
                                         String dbPath,
                                         String introspectionPath,
                                         int transport);

    /**
     * API for stop all process of the OcPlatform.
     * All of threads and memory will be terminated by this API.
     * Iotivity Core can be started again through Configure(PlatformConfig platformConfig) API.
     * Both Configure and Shutdown API is filtering for duplicated calling even while processing.
     * <p>
     * Note: This API is for both server and client side.
     * </p>
     */
    public synchronized static void Shutdown() {
        if (!sIsStopPlatform)
        {
            OcPlatform.stop();
            sIsStopPlatform = true;
            sIsPlatformInitialized = false;
        }
    }

    private static native void stop();
    private static native void start();

    /**
     * API for notifying base that resource's attributes have changed.
     * <p>
     * Note: This API is for server side only.
     * </p>
     *
     * @param ocResourceHandle resource handle of the resource
     * @throws OcException if failure
     */
    public static void notifyAllObservers(
            OcResourceHandle ocResourceHandle) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.notifyAllObservers0(ocResourceHandle);
    }

    private static native void notifyAllObservers0(
            OcResourceHandle ocResourceHandle) throws OcException;

    /**
     * API for notifying base that resource's attributes have changed.
     * <p>
     * Note: This API is for server side only.
     * </p>
     *
     * @param ocResourceHandle resource handle of the resource
     * @param qualityOfService the quality of communication
     * @throws OcException if failure
     */
    public static void notifyAllObservers(
            OcResourceHandle ocResourceHandle,
            QualityOfService qualityOfService) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.notifyAllObservers1(ocResourceHandle, qualityOfService.getValue());
    }

    private static native void notifyAllObservers1(
            OcResourceHandle ocResourceHandle,
            int qualityOfService) throws OcException;

    /**
     * API for notifying only specific clients that resource's attributes have changed.
     * <p>
     * Note: This API is for server side only.
     * </p>
     *
     * @param ocResourceHandle    resource handle of the resource
     * @param ocObservationIdList These set of ids are ones which which will be notified upon
     *                            resource change.
     * @param ocResourceResponse  OcResourceResponse object used by app to fill the response for
     *                            this resource change
     * @throws OcException if failure
     */
    public static void notifyListOfObservers(
            OcResourceHandle ocResourceHandle,
            List<Byte> ocObservationIdList,
            OcResourceResponse ocResourceResponse) throws OcException {
        OcPlatform.initCheck();

        if (ocObservationIdList == null) {
            throw new OcException(ErrorCode.INVALID_PARAM, "ocObservationIdList cannot be null");
        }

        byte[] idArr = new byte[ocObservationIdList.size()];
        Iterator<Byte> it = ocObservationIdList.iterator();
        int i = 0;
        while (it.hasNext()) {
            idArr[i++] = (byte) it.next();
        }

        OcPlatform.notifyListOfObservers2(
                ocResourceHandle,
                idArr,
                ocResourceResponse);
    }

    private static native void notifyListOfObservers2(
            OcResourceHandle ocResourceHandle,
            byte[] ocObservationIdArray,
            OcResourceResponse ocResourceResponse) throws OcException;

    /**
     * API for notifying only specific clients that resource's attributes have changed.
     * <p>
     * Note: This API is for server side only.
     * </p>
     *
     * @param ocResourceHandle    resource handle of the resource
     * @param ocObservationIdList These set of ids are ones which which will be notified upon
     *                            resource change.
     * @param ocResourceResponse  OcResourceResponse object used by app to fill the response for
     *                            this resource change
     * @param qualityOfService    the quality of communication
     * @throws OcException if failure
     */
    public static void notifyListOfObservers(
            OcResourceHandle ocResourceHandle,
            List<Byte> ocObservationIdList,
            OcResourceResponse ocResourceResponse,
            QualityOfService qualityOfService) throws OcException {
        OcPlatform.initCheck();

        if (ocObservationIdList == null) {
            throw new OcException(ErrorCode.INVALID_PARAM, "ocObservationIdList cannot be null");
        }

        byte[] idArr = new byte[ocObservationIdList.size()];
        Iterator<Byte> it = ocObservationIdList.iterator();
        int i = 0;
        while (it.hasNext()) {
            idArr[i++] = (byte) it.next();
        }

        OcPlatform.notifyListOfObservers3(
                ocResourceHandle,
                idArr,
                ocResourceResponse,
                qualityOfService.getValue()
        );
    }

    private static native void notifyListOfObservers3(
            OcResourceHandle ocResourceHandle,
            byte[] ocObservationIdArray,
            OcResourceResponse ocResourceResponse,
            int qualityOfService) throws OcException;

    /**
     * API for Service and Resource Discovery
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
    public static void findResource(
            String host,
            String resourceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnResourceFoundListener onResourceFoundListener) throws OcException {
        OcPlatform.initCheck();

        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }

        OcPlatform.findResource0(
                host,
                resourceUri,
                connTypeInt,
                onResourceFoundListener
        );
    }

    private static native void findResource0(
            String host,
            String resourceUri,
            int connectivityType,
            OnResourceFoundListener onResourceFoundListener) throws OcException;

    /**
     * API for Service and Resource Discovery.
     * <p>
     * Note: This API is for client side only.
     * </p>
     *
     * @param host                    Host IP Address of a service to direct resource discovery query.
     *                                If empty, performs multicast resource discovery query
     * @param resourceUri             name of the resource. If null or empty, performs search for all
     *                                resource names
     * @param connectivityTypeSet     Set of types of connectivity. Example: IP
     * @param onResourceFoundListener Handles events, success states and failure states.
     * @param qualityOfService        the quality of communication
     * @throws OcException if failure
     */
    public static void findResource(
            String host,
            String resourceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnResourceFoundListener onResourceFoundListener,
            QualityOfService qualityOfService) throws OcException {
        OcPlatform.initCheck();

        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }

        OcPlatform.findResource1(host,
                resourceUri,
                connTypeInt,
                onResourceFoundListener,
                qualityOfService.getValue()
        );
    }

    private static native void findResource1(
            String host,
            String resourceUri,
            int connectivityType,
            OnResourceFoundListener onResourceFoundListener,
            int qualityOfService) throws OcException;

    /**
     * API for Service and Resource Discovery
     * <p>
     * Note: This API is for client side only.
     * </p>
     *
     * @param host                     Host Address of a service to direct resource discovery query.
     *                                 If empty, performs multicast resource discovery query
     * @param resourceUri              name of the resource. If null or empty, performs search for all
     *                                 resource names
     * @param connectivityTypeSet      Set of types of connectivity. Example: IP
     * @param onResourcesFoundListener Handles events, success states and failure states.
     * @throws OcException if failure
     */
    public static void findResources(
            String host,
            String resourceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnResourcesFoundListener onResourcesFoundListener) throws OcException {
        OcPlatform.initCheck();

        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }

        OcPlatform.findResources0(
                host,
                resourceUri,
                connTypeInt,
                onResourcesFoundListener
        );
    }

    private static native void findResources0(
            String host,
            String resourceUri,
            int connectivityType,
            OnResourcesFoundListener onResourcesFoundListener) throws OcException;

    /**
     * API for Service and Resource Discovery.
     * <p>
     * Note: This API is for client side only.
     * </p>
     *
     * @param host                     Host IP Address of a service to direct resource discovery query.
     *                                 If empty, performs multicast resource discovery query
     * @param resourceUri              name of the resource. If null or empty, performs search for all
     *                                 resource names
     * @param connectivityTypeSet      Set of types of connectivity. Example: IP
     * @param onResourcesFoundListener Handles events, success states and failure states.
     * @param qualityOfService         the quality of communication
     * @throws OcException if failure
     */
    public static void findResources(
            String host,
            String resourceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnResourcesFoundListener onResourcesFoundListener,
            QualityOfService qualityOfService) throws OcException {
        OcPlatform.initCheck();

        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }

        OcPlatform.findResources1(host,
                resourceUri,
                connTypeInt,
                onResourcesFoundListener,
                qualityOfService.getValue()
        );
    }

    private static native void findResources1(
            String host,
            String resourceUri,
            int connectivityType,
            OnResourcesFoundListener onResourcesFoundListener,
            int qualityOfService) throws OcException;

    /**
     * API for Device Discovery
     *
     * @param host                  Host IP Address. If null or empty, Multicast is performed.
     * @param deviceUri             Uri containing address to the virtual device
     * @param connectivityTypeSet   Set of types of connectivity. Example: IP
     * @param onDeviceFoundListener Handles events, success states and failure states.
     * @throws OcException if failure
     */
    public static void getDeviceInfo(
            String host,
            String deviceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnDeviceFoundListener onDeviceFoundListener) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        OcPlatform.getDeviceInfo0(
                host,
                deviceUri,
                connTypeInt,
                onDeviceFoundListener
        );
    }

    private static native void getDeviceInfo0(
            String host,
            String deviceUri,
            int connectivityType,
            OnDeviceFoundListener onDeviceFoundListener) throws OcException;

    /**
     * API for Device Discovery
     *
     * @param host                  Host IP Address. If null or empty, Multicast is performed.
     * @param deviceUri             Uri containing address to the virtual device
     * @param connectivityTypeSet   Set of types of connectivity. Example: IP
     * @param onDeviceFoundListener Handles events, success states and failure states.
     * @param qualityOfService      the quality of communication
     * @throws OcException if failure
     */
    public static void getDeviceInfo(
            String host,
            String deviceUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnDeviceFoundListener onDeviceFoundListener,
            QualityOfService qualityOfService) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        OcPlatform.getDeviceInfo1(
                host,
                deviceUri,
                connTypeInt,
                onDeviceFoundListener,
                qualityOfService.getValue()
        );
    }

    private static native void getDeviceInfo1(
            String host,
            String deviceUri,
            int connectivityType,
            OnDeviceFoundListener onDeviceFoundListener,
            int qualityOfService) throws OcException;

    /**
     * API for Platform Discovery
     *
     * @param host                    Host IP Address. If null or empty, Multicast is performed.
     * @param platformUri             Uri containing address to the platform
     * @param connectivityTypeSet     Set of types of connectivity. Example: IP
     * @param onPlatformFoundListener Handles events, success states and failure states.
     * @throws OcException if failure
     */

    public static void getPlatformInfo(
            String host,
            String platformUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnPlatformFoundListener onPlatformFoundListener) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        OcPlatform.getPlatformInfo0(
                host,
                platformUri,
                connTypeInt,
                onPlatformFoundListener
        );
    }

    private static native void getPlatformInfo0(
            String host,
            String platformUri,
            int connectivityType,
            OnPlatformFoundListener onPlatformInfoFoundListener) throws OcException;

    /**
     * API for Platform Discovery
     *
     * @param host                    Host IP Address. If null or empty, Multicast is performed.
     * @param platformUri             Uri containing address to the platform
     * @param connectivityTypeSet     Set of types of connectivity. Example: IP
     * @param onPlatformFoundListener Handles events, success states and failure states.
     * @param qualityOfService        the quality of communication
     * @throws OcException if failure
     */

    public static void getPlatformInfo(
            String host,
            String platformUri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnPlatformFoundListener onPlatformFoundListener,
            QualityOfService qualityOfService) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        OcPlatform.getPlatformInfo1(
                host,
                platformUri,
                connTypeInt,
                onPlatformFoundListener,
                qualityOfService.getValue()
        );
    }

    private static native void getPlatformInfo1(
            String host,
            String platformUri,
            int connectivityType,
            OnPlatformFoundListener onPlatformFoundListener,
            int qualityOfService) throws OcException;

    /**
     * This API registers a resource with the server
     * <p>
     * Note: This API applies to server and client side.
     * </P>
     *
     * @param ocResource The instance of OcResource with all data filled
     * @return resource handle
     * @throws OcException if failure
     */
    public static OcResourceHandle registerResource(
            OcResource ocResource) throws OcException {
        OcPlatform.initCheck();
        return OcPlatform.registerResource0(ocResource);
    }

    private static native OcResourceHandle registerResource0(
            OcResource ocResource) throws OcException;

    /**
     * This API registers a resource with the server NOTE: This API applies to server side only.
     * <p>
     * Note: This API applies to server side only.
     * </P>
     *
     * @param resourceUri         The URI of the resource. Example: "a/light"
     * @param resourceTypeName    The resource type. Example: "light"
     * @param resourceInterface   The resource interface (whether it is collection etc).
     * @param entityHandler       entity handler.
     * @param resourcePropertySet indicates the property of the resource
     * @return resource handle
     * @throws OcException if failure
     */
    public static OcResourceHandle registerResource(
            String resourceUri,
            String resourceTypeName,
            String resourceInterface,
            EntityHandler entityHandler,
            EnumSet<ResourceProperty> resourcePropertySet) throws OcException {
        OcPlatform.initCheck();

        int resProperty = 0;

        for (ResourceProperty prop : ResourceProperty.values()) {
            if (resourcePropertySet.contains(prop))
                resProperty |= prop.getValue();
        }

        return OcPlatform.registerResource1(resourceUri,
                resourceTypeName,
                resourceInterface,
                entityHandler,
                resProperty);
    }

    private static native OcResourceHandle registerResource1(
            String resourceUri,
            String resourceTypeName,
            String resourceInterface,
            EntityHandler entityHandler,
            int resourceProperty) throws OcException;

    /**
     * Register Device Info
     *
     * @deprecated use setPropertyValue instead
     *
     * @param ocDeviceInfo object containing all the device specific information
     * @throws OcException if failure
     */
    @Deprecated
    public static void registerDeviceInfo(
            OcDeviceInfo ocDeviceInfo) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.registerDeviceInfo0(
                ocDeviceInfo.getDeviceName(),
                ocDeviceInfo.getDeviceTypes().toArray(
                        new String[ocDeviceInfo.getDeviceTypes().size()]
                )
        );
    }

    private static native void registerDeviceInfo0(
            String deviceName,
            String[] deviceTypes
    ) throws OcException;

    /**
    * This function gets a resource handle by resource uri.
    *
    * @param  resourceUri   Uri of Resource to get Resource handle.
    *
    * @return Found  resource handle or NULL if not found.
    * @throws OcException if failure
    */
    public static OcResourceHandle getResourceHandleAtUri(String resourceUri) throws OcException {
        OcPlatform.initCheck();
        return getResourceHandleAtUri0(resourceUri);
    }

    private static native OcResourceHandle getResourceHandleAtUri0(String resourceUri);

    /**
     * Set Property Value (to a single value)
     *
     * @param path value from PayloadType
     * @param propName property name
     * @param propValue new property value
     * @throws OcException if failure
     */
    public static void setPropertyValue(
            int path, String propName, String propValue) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.setPropertyValue1(path, propName, propValue);
    }

    /**
     * Set Property Value (to a list of values)
     *
     * @param path value from PayloadType
     * @param propName property name
     * @param propValue new property value
     * @throws OcException if failure
     */
    public static void setPropertyValue(
            int path, String propName, List<String> propValue) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.setPropertyValue0(path, propName, propValue.toArray(new String[propValue.size()]));
    }

    /**
     * Get Property Value
     *
     * @param path value from PayloadType
     * @param propName property name
     * @return the property value, or null if property name is not found
     * @throws OcException if failure
     */
    public static String getPropertyValue(int path, String propName) throws OcException {
        OcPlatform.initCheck();
        return OcPlatform.getPropertyValue0(path, propName);
    }

    private static native void setPropertyValue1(
            int path,
            String propName,
            String propValue
    ) throws OcException;


    private static native void setPropertyValue0(
            int path,
            String propName,
            String[] propValue
    ) throws OcException;

    private static native String getPropertyValue0(
            int path,
            String propName
    ) throws OcException;

    /**
     * Register Platform Info
     *
     * @param ocPlatformInfo object containing all the platform specific information
     * @throws OcException if failure
     */
    public static void registerPlatformInfo(
            OcPlatformInfo ocPlatformInfo) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.registerPlatformInfo0(
                ocPlatformInfo.getPlatformId(),
                ocPlatformInfo.getManufacturerName(),
                ocPlatformInfo.getManufacturerUrl(),
                ocPlatformInfo.getModelNumber(),
                ocPlatformInfo.getDateOfManufacture(),
                ocPlatformInfo.getPlatformVersion(),
                ocPlatformInfo.getOperatingSystemVersion(),
                ocPlatformInfo.getHardwareVersion(),
                ocPlatformInfo.getFirmwareVersion(),
                ocPlatformInfo.getSupportUrl(),
                ocPlatformInfo.getSystemTime()
        );
    }

    private static native void registerPlatformInfo0(
            String platformId, String manufacturerName, String manufacturerUrl,
            String modelNumber, String dateOfManufacture, String platformVersion,
            String operatingSystemVersion, String hardwareVersion, String firmwareVersion,
            String supportUrl, String systemTime
    ) throws OcException;

    /**
     * This API unregisters a resource with the server NOTE: This API applies to server side only.
     *
     * @param ocResourceHandle This is the resource handle which we which to unregister from the
     *                         server
     * @throws OcException if failure
     */
    public static void unregisterResource(
            OcResourceHandle ocResourceHandle) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.unregisterResource0(ocResourceHandle);
    }

    private static native void unregisterResource0(
            OcResourceHandle ocResourceHandle) throws OcException;


    /**
     * Add a resource to a collection resource
     *
     * @param ocResourceCollectionHandle handle to the collection resource
     * @param ocResourceHandle           handle to resource to be added to the collection resource
     * @throws OcException if failure
     */
    public static void bindResource(
            OcResourceHandle ocResourceCollectionHandle,
            OcResourceHandle ocResourceHandle) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.bindResource0(ocResourceCollectionHandle, ocResourceHandle);
    }

    private static native void bindResource0(
            OcResourceHandle ocResourceCollectionHandle,
            OcResourceHandle ocResourceHandle) throws OcException;

    /**
     * Add multiple resources to a collection resource.
     *
     * @param ocResourceCollectionHandle handle to the collection resource
     * @param ocResourceHandleList       reference to list of resource handles to be added to the
     *                                   collection resource
     * @throws OcException if failure
     */
    public static void bindResources(
            OcResourceHandle ocResourceCollectionHandle,
            List<OcResourceHandle> ocResourceHandleList) throws OcException {
        OcPlatform.initCheck();

        if (ocResourceHandleList == null) {
            throw new OcException(ErrorCode.INVALID_PARAM, "ocResourceHandleList cannot be null");
        }

        OcPlatform.bindResources0(
                ocResourceCollectionHandle,
                ocResourceHandleList.toArray(
                        new OcResourceHandle[ocResourceHandleList.size()])
        );
    }

    private static native void bindResources0(
            OcResourceHandle ocResourceCollectionHandle,
            OcResourceHandle[] ocResourceHandleArray) throws OcException;

    /**
     * Unbind a resource from a collection resource.
     *
     * @param ocResourceCollectionHandle handle to the collection resource
     * @param ocResourceHandle           resource handle to be unbound from the collection resource
     * @throws OcException if failure
     */
    public static void unbindResource(
            OcResourceHandle ocResourceCollectionHandle,
            OcResourceHandle ocResourceHandle) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.unbindResource0(ocResourceCollectionHandle, ocResourceHandle);
    }

    private static native void unbindResource0(
            OcResourceHandle ocResourceCollectionHandle,
            OcResourceHandle ocResourceHandle) throws OcException;

    /**
     * Unbind resources from a collection resource.
     *
     * @param ocResourceCollectionHandle Handle to the collection resource
     * @param ocResourceHandleList       List of resource handles to be unbound from the collection
     *                                   resource
     * @throws OcException if failure
     */
    public static void unbindResources(
            OcResourceHandle ocResourceCollectionHandle,
            List<OcResourceHandle> ocResourceHandleList) throws OcException {
        OcPlatform.initCheck();

        if (ocResourceHandleList == null) {
            throw new OcException(ErrorCode.INVALID_PARAM, "ocResourceHandleList cannot be null");
        }

        OcPlatform.unbindResources0(
                ocResourceCollectionHandle,
                ocResourceHandleList.toArray(
                        new OcResourceHandle[ocResourceHandleList.size()])
        );
    }

    private static native void unbindResources0(
            OcResourceHandle ocResourceCollectionHandle,
            OcResourceHandle[] ocResourceHandleArray) throws OcException;

    /**
     * Binds a type to a particular resource
     *
     * @param ocResourceHandle handle to the resource
     * @param resourceTypeName new typename to bind to the resource
     * @throws OcException if failure
     */
    public static void bindTypeToResource(
            OcResourceHandle ocResourceHandle,
            String resourceTypeName) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.bindTypeToResource0(ocResourceHandle, resourceTypeName);
    }

    private static native void bindTypeToResource0(
            OcResourceHandle ocResourceHandle,
            String resourceTypeName) throws OcException;

    /**
     * Binds an interface to a particular resource
     *
     * @param ocResourceHandle      handle to the resource
     * @param resourceInterfaceName new interface to bind to the resource
     * @throws OcException if failure
     */
    public static void bindInterfaceToResource(
            OcResourceHandle ocResourceHandle,
            String resourceInterfaceName) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.bindInterfaceToResource0(ocResourceHandle, resourceInterfaceName);
    }

    private static native void bindInterfaceToResource0(
            OcResourceHandle ocResourceHandle,
            String resourceInterfaceName) throws OcException;

    /**
     * Start Presence announcements.
     *
     * @param ttl time to live in seconds
     * @throws OcException if failure
     */
    public static void startPresence(int ttl) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.startPresence0(ttl);
    }

    private static native void startPresence0(int ttl) throws OcException;

    /**
     * Stop Presence announcements.
     *
     * @throws OcException if failure
     */
    public static void stopPresence() throws OcException {
        OcPlatform.initCheck();
        OcPlatform.stopPresence0();
    }

    private static native void stopPresence0() throws OcException;

    /**
     * Subscribes to a server's presence change events. By making this subscription, every time a
     * server adds/removes/alters a resource, starts or is intentionally stopped
     *
     * @param host                The IP address/addressable name of the server to subscribe to
     * @param connectivityTypeSet Set of types of connectivity. Example: IP
     * @param onPresenceListener  listener that will receive notifications/subscription events
     * @return a handle object that can be used to identify this subscription request. It can be
     * used to unsubscribe from these events in the future
     * @throws OcException if failure
     */
    public static OcPresenceHandle subscribePresence(
            String host,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnPresenceListener onPresenceListener) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        return OcPlatform.subscribePresence0(
                host,
                connTypeInt,
                onPresenceListener
        );
    }

    private static native OcPresenceHandle subscribePresence0(
            String host,
            int connectivityType,
            OnPresenceListener onPresenceListener) throws OcException;

    /**
     * Subscribes to a server's presence change events. By making this subscription, every time a
     * server adds/removes/alters a resource, starts or is intentionally stopped
     *
     * @param host                The IP address/addressable name of the server to subscribe to
     * @param resourceType        a resource type specified as a filter for subscription events.
     * @param connectivityTypeSet Set of types of connectivity. Example: IP
     * @param onPresenceListener  listener that will receive notifications/subscription events
     * @return a handle object that can be used to identify this subscription request. It can be
     * used to unsubscribe from these events in the future
     * @throws OcException if failure
     */
    public static OcPresenceHandle subscribePresence(
            String host,
            String resourceType,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OnPresenceListener onPresenceListener) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        return OcPlatform.subscribePresence1(
                host,
                resourceType,
                connTypeInt,
                onPresenceListener);
    }

    private static native OcPresenceHandle subscribePresence1(
            String host,
            String resourceType,
            int connectivityType,
            OnPresenceListener onPresenceListener) throws OcException;

    /**
     * Unsubscribes from a previously subscribed server's presence events. Note that you may for
     * a short time still receive events from the server since it may take time for the
     * unsubscribe to take effect.
     *
     * @param ocPresenceHandle the handle object provided by the subscribePresence call that
     *                         identifies this subscription
     * @throws OcException if failure
     */
    public static void unsubscribePresence(
            OcPresenceHandle ocPresenceHandle) throws OcException {
        OcPlatform.initCheck();
        OcPlatform.unsubscribePresence0(ocPresenceHandle);
    }

    private static native void unsubscribePresence0(
            OcPresenceHandle ocPresenceHandle) throws OcException;

    /**
     * Subscribes to a server's device presence change events.
     *
     * @param host                The IP address/addressable name of the server to subscribe to.
     * @param di                  Vector which can have the devices id.
     * @param connectivityTypeSet Set of connectivity types, e.g. IP.
     * @param onObserveListener   The handler method will be invoked with a map
     *                            of attribute name and values.
     * @return a handle object that can be used to identify this subscription request.
     *         It can be used to unsubscribe from these events in the future.
     * @throws OcException if failure.
     */
    public static OcPresenceHandle subscribeDevicePresence(
            String host,
            List<String> di,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            OcResource.OnObserveListener onObserveListener) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        return OcPlatform.subscribeDevicePresence0(
                host,
                di.toArray(new String[di.size()]),
                connTypeInt,
                onObserveListener);
    }

    private static native OcPresenceHandle subscribeDevicePresence0(
            String host,
            String[] di,
            int connectivityType,
            OcResource.OnObserveListener onObserveListener) throws OcException;

    /**
     * Creates a resource proxy object so that get/put/observe functionality can be used without
     * discovering the object in advance. Note that the consumer of this method needs to provide
     * all of the details required to correctly contact and observe the object. If the consumer
     * lacks any of this information, they should discover the resource object normally.
     * Additionally, you can only create this object if OcPlatform was initialized to be a Client
     * or Client/Server.
     *
     * @param host                a string containing a resolvable host address of the server
     *                            holding the resource. Currently this should be in the format of
     *                            coap://address:port for IPv4  and in the format of
     *                            coap://[address%25ZoneID]:port for IPv6. In the future,
     *                            we expect "coap:" section is removed from this format.
     * @param uri                 the rest of the resource's URI that will permit messages to be
     *                            properly routed.
     *                            Example: /a/light
     * @param connectivityTypeSet Set of types of connectivity. Example: IP
     * @param isObservable        a boolean containing whether the resource supports observation
     * @param resourceTypeList    a collection of resource types implemented by the resource
     * @param interfaceList       a collection of interfaces that the resource supports/implements
     * @return new resource object
     * @throws OcException if failure
     */
    public static OcResource constructResourceObject(
            String host,
            String uri,
            EnumSet<OcConnectivityType> connectivityTypeSet,
            boolean isObservable,
            List<String> resourceTypeList,
            List<String> interfaceList) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
                connTypeInt |= connType.getValue();
        }
        return OcPlatform.constructResourceObject0(
                host,
                uri,
                connTypeInt,
                isObservable,
                resourceTypeList.toArray(new String[resourceTypeList.size()]),
                interfaceList.toArray(new String[interfaceList.size()])
        );
    }

    private static native OcResource constructResourceObject0(
            String host,
            String uri,
            int connectivityType,
            boolean isObservable,
            String[] resourceTypes,
            String[] interfaces) throws OcException;

    /**
     * Allows application entity handler to send response to an incoming request.
     *
     * @param ocResourceResponse resource response
     * @throws OcException if failure
     */
    public static void sendResponse(OcResourceResponse ocResourceResponse)
            throws OcException {
        OcPlatform.initCheck();
        OcPlatform.sendResponse0(ocResourceResponse);
    }

    private static native void sendResponse0(OcResourceResponse ocResourceResponse)
            throws OcException;

    /**
     * An OnResourceFoundListener can be registered via the OcPlatform.findResource call.
     * Event listeners are notified asynchronously
     */
    public interface OnResourceFoundListener {
        public void onResourceFound(OcResource resource);
        public void onFindResourceFailed(Throwable ex, String uri);
    }

    /**
     * An OnResourcesFoundListener can be registered via the OcPlatform.findResources call.
     * Event listeners are notified asynchronously
     */
    public interface OnResourcesFoundListener {
        public void onResourcesFound(OcResource[] resources);
        public void onFindResourcesFailed(Throwable ex, String uri);
    }

    /**
     * An OnDeviceFoundListener can be registered via the OcPlatform.getDeviceInfo call.
     * Event listeners are notified asynchronously
     */
    public interface OnDeviceFoundListener {
        public void onDeviceFound(OcRepresentation ocRepresentation);
    }

    /**
     * An OnPlatformFoundListener can be registered via the OcPlatform.getPlatformInfo call.
     * Event listeners are notified asynchronously
     */
    public interface OnPlatformFoundListener {
        public void onPlatformFound(OcRepresentation ocRepresentation);
    }

    /**
     * An OnPresenceListener can be registered via the OcPlatform.subscribePresence call.
     * Event listeners are notified asynchronously
     */
    public interface OnPresenceListener {
        public void onPresence(OcPresenceStatus ocPresenceStatus, int nonce, String hostAddress);
    }

    /**
     * An EntityHandler can be registered via the OcPlatform.registerResource call.
     * Event listeners are notified asynchronously
     *
     * Note: entityhandler callback :
     * When you set specific return value like EntityHandlerResult.OK, SLOW
     * and etc in entity handler callback,
     * ocstack will be not send response automatically to client
     * except for error return value like EntityHandlerResult.ERROR
     * If you want to send response to client with specific result,
     * sendResponse API should be called with the result value.
     */
    public interface EntityHandler {
        public EntityHandlerResult handleEntity(OcResourceRequest ocResourceRequest);
    }

    private static void initCheck() {
        if (!sIsPlatformInitialized) {
            throw new IllegalStateException("OcPlatform must be configured by making a call to " +
                    "OcPlatform.Configure before any other API calls are permitted");
        }
    }

    /**
     * Gets platform quality of service
     *
     * @return quality of service
     */
    public static QualityOfService getPlatformQualityOfService() {
        OcPlatform.initCheck();
        return sPlatformQualityOfService;
    }

    /**
     * Create an account manager object that can be used for doing request to account server.
     * You can only create this object if OCPlatform was initialized to be a Client or
     * Client/Server. Otherwise, this will return an empty shared ptr.
     *
     * Note: For now, OCPlatform SHOULD be initialized to be a Client/Server(Both) for the
     *       methods of this object to work since device id is not generated on Client mode.
     *
     * @param host Host IP Address of a account server.
     * @param connectivityTypeSet Set of types of connectivity. Example: CT_ADAPTER_IP
     * @return new AccountManager object
     * @throws OcException if failure
     */
    public static OcAccountManager constructAccountManagerObject(
            String host,
            EnumSet<OcConnectivityType> connectivityTypeSet) throws OcException {
        OcPlatform.initCheck();
        int connTypeInt = 0;

        for (OcConnectivityType connType : OcConnectivityType.values()) {
            if (connectivityTypeSet.contains(connType))
            {
                connTypeInt |= connType.getValue();
            }
        }
        return OcPlatform.constructAccountManagerObject0(
                host,
                connTypeInt
                );
    }

    private static native OcAccountManager constructAccountManagerObject0(
            String host,
            int connectivityType) throws OcException;
    /**
     * Method to get device Id in byte array.
     * @return My DeviceId.
     */
    public static native byte[] getDeviceId();

    /**
     * Method to set DeviceId.
     * @param deviceId DeviceId of the client
     * @throws OcException if failure
     */
    public static native void setDeviceId(byte[] deviceId) throws OcException;

    /**
     * Method to get the version of IoTivity.
     * @return the version of IoTivity
     */
    public static native String getIoTivityVersion();
}
