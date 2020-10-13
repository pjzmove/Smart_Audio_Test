/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.qualcomm.qti.smartaudio.manager.onboarding.models.ConnectingStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.models.ConnectionStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.models.WpsStatus;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestListener;
import com.qualcomm.qti.smartaudio.manager.onboarding.requests.RequestType;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.NetworksListError;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.OnBoardingError;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.OnBoardingStep;
import com.qualcomm.qti.smartaudio.manager.onboarding.states.RequestResult;
import com.qualcomm.qti.smartaudio.model.WifiNetwork;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.RequestStatus.SUCCESS;

/**
 * <p>This class manages the boarding of a device by maintaining a WiFi connection with it and using a HTTP client
 * to communicate with it.</p>
 * <p>In order to get an instance of this manager, first call {@link #prepare(Context)} and then
 * {@link #getInstance()}.</p>
 * <p>Once this manager is not necessary anymore, it is required to call {@link #release(Context)}.</p>
 * <p>To connect and manage a connection to a WiFi network, call {@link #connectToWifiNetwork(WifiNetwork)}
 * and subscribe to connection states and errors by calling
 * {@link #subscribeWifiConnectorListener(WifiConnector.WifiConnectorListener)}. If it is not necessary anymore to get
 * those updates, they can be unsubscribed by calling
 * {@link #unsubscribeWifiConnectorListener(WifiConnector.WifiConnectorListener)}.</p>
 * <p>To board a device call {@link #requestOnBoarding(String, OnBoardingListener, boolean, String, String)}. A
 * device which can be on boarded is a speaker which can provide a WiFi network of which it is the gateway. This
 * manager uses the SSID of the WiFi network to connect to the device/speaker and uses the gateway address to send
 * HTTP requests to it.</p>
 */
public class OnBoardingManager {

    // ========================================================================
    // CONSTANTS

    /**
     * The tag to use to display logs.
     */
    private static final String TAG = "OnBoardingManager";
    /**
     * The time out for which the verify_connecting request should be attempted to get SUCCESS as the response.
     */
    private static final long VERIFY_CONNECTING_TIME_OUT_MS = 60000;


    // ========================================================================
    // STATIC FIELDS

    /**
     * A static instance instantiated by the application to get continuous access to the manager.
     * This is initiated by calling {@link #prepare(Context)}.
     */
    private static OnBoardingManager sInstance;


    // ========================================================================
    // PRIVATE FIELDS

    /**
     * The connector to maintain a connection to a device network's to board the device.
     */
    private final WifiConnector mWifiConnector;
    /**
     * The object to send HTTP requests to board a device.
     */
    private final OnBoardingHttpClient mHttpClient;
    /**
     * The list of listeners which subscribes to notifications from the WifiConnector. They are kept in a list in
     * order to unsubscribe them when this manager is released.
     */
    private final List<WifiConnector.WifiConnectorListener> mWifiSubscribers = new CopyOnWriteArrayList<>();
    /**
     * To store the time when the first "verify_connecting" request starts.
     */
    private long mVerifyConnectingTimeStamp = 0;


    // ========================================================================
    // STATIC METHODS

    /**
     * <p>The method to prepare an instance of this manager.</p>
     * <p>This method can be called after {@link #release(Context)}. However it resets the manager to its initial
     * state.</p>
     *
     * @param appContext
     *         The context of the application in order to access system services such as the WifiManager for
     *         instance.
     */
    public static void prepare(Context appContext) {
        synchronized (OnBoardingManager.class) {
            if (sInstance == null) {
                sInstance = new OnBoardingManager(appContext);
            }
        }
    }

    /**
     * <p>This method provides the singleton object of the on boarding manager.</p>
     *
     * @return the instance if the on boarding manager or null if {@link #prepare(Context)} has not been called before
     * or if {@link #release(Context)} has been called.
     */
    public static OnBoardingManager getInstance() {
        return sInstance;
    }

    /**
     * <p>To release the manager and all resources linked to it.</p>
     *
     * @param appContext
     *         The context of the application in order to release and unregister services and receivers from the
     *         system.
     */
    public static void release(Context appContext) {
        synchronized (OnBoardingManager.class) {
            if (sInstance != null) {
                sInstance.stopAll(appContext);
                sInstance = null;
            }
        }
    }


    // ========================================================================
    // CONSTRUCTOR

    /**
     * <p>A private constructor to build a new instance of this manager.</p>
     *
     * @param appContext
     *         The context of the application in order to access system services such as the WifiManager for
     *         instance.
     */
    private OnBoardingManager(Context appContext) {
        this.mHttpClient = new OnBoardingHttpClient();
        this.mWifiConnector = new WifiConnector(appContext);
    }


    // ========================================================================
    // PUBLIC METHODS

    /**
     * <p>To connect to the given network using Wifi.</p>
     * <p>If this method returns
     * {@link WifiConnector.RequestStatus#SUCCESS SUCCESS}, The result of the
     * connection is asynchronous and will be sent to any
     * {@link WifiConnector.WifiConnectorListener WifiConnectorListener}
     * registered with {@link #subscribeWifiConnectorListener(WifiConnector.WifiConnectorListener)}.</p>
     *
     * @param device
     *         The wifi network to connect with.
     *
     * @return {@link WifiConnector.RequestStatus#SUCCESS SUCCESS} if the request
     * could successfully initiated. Otherwise it returns the reason of the failure.
     */
    public @WifiConnector.RequestStatus
    int connectToWifiNetwork(WifiNetwork device) {
        return mWifiConnector.connect(device);
    }

    /**
     * <p>To subscribe to connection events.</p>
     *
     * @param listener
     *         The listener to be notified of any connection state and error.
     */
    public void subscribeWifiConnectorListener(WifiConnector.WifiConnectorListener listener) {
        mWifiSubscribers.add(listener);
        mWifiConnector.subscribe(listener);
    }

    /**
     * <p>To unsubscribe from connection updates.</p>
     *
     * @param listener
     *         The listener to unsubscribe.
     */
    public void unsubscribeWifiConnectorListener(WifiConnector.WifiConnectorListener listener) {
        mWifiConnector.unsubscribe(listener);
        mWifiSubscribers.remove(listener);
    }

    /**
     * <p>This method checks if the given ssid corresponds to a configured device.</p>
     *
     * @param ssid
     *         the ssid to check without quotes.
     *
     * @return True if an existing configuration has the given ssid, false otherwise.
     */
    public boolean isConfiguredNetwork(String ssid) {
        return mWifiConnector.isConfiguredNetwork(ssid);
    }

    /**
     * <p>To request the list of networks a device can on board with.</p>
     * <p>If this method returns True, the result of the operation will be sent asynchronously using the given
     * listener.</p>
     * <p>Through its called to
     * {@link #request(String, RequestType, RequestListener, WifiConnector.WifiConnectorListener, Object...)} this
     * method will connect to the given SSID as a WiFi network if it is not already connected to it.</p>
     *
     * @param expectedConnectedSsid
     *         The SSID of the device to get the network list from.
     * @param listener
     *         The listener to get the result of the operation.
     *
     * @return True if the operation could be initiated, false otherwise.
     */
    public boolean requestNetworksList(String expectedConnectedSsid, NetworksListener listener) {
        RequestType type = RequestType.REFRESH_SCAN_LIST;
        RequestListener requestListener = buildRequestListener(listener);
        WifiConnector.WifiConnectorListener wifiListener = buildWifiConnectorListener(expectedConnectedSsid, listener);
        return request(expectedConnectedSsid, type, requestListener, wifiListener);
    }

    /**
     * <p>To request a device - represented by the <code>expectedConnectedSsid</code> parameter - to be on boarded
     * with the given network - represented by the <code>ssid</code> parameter.</p>
     * <p>If this method returns True, the result of the operation will be sent asynchronously using the given
     * listener.</p>
     * <p>Through its called to
     * {@link #request(String, OnBoardingListener, RequestType, Object...)} this method will connect to the given
     * SSID as a WiFi network if it is not already connected to it.</p>
     *
     * @param expectedConnectedSsid
     *         the ssid of the device to on board.
     * @param listener
     *         the listener to get the result of the operation.
     * @param isWps
     *         True of the device should be on boarded to the network using WPS, false if the password should be
     *         used.
     * @param ssid
     *         The ssid of the network the device should connect to during its on boarding process.
     * @param password
     *         The password of the network the device should connect to.
     *
     * @return True if the operation could be initiated, false otherwise.
     */
    public boolean requestOnBoarding(String expectedConnectedSsid, OnBoardingListener listener,
                                     boolean isWps, String ssid, String password) {
        RequestType type = isWps ? RequestType.WPS : RequestType.VERIFY_CONNECT;
        return request(expectedConnectedSsid, listener, type, ssid, password);
    }

    /**
     * <p>To stop all ongoing HTTP requests for {@link #requestNetworksList(String, NetworksListener)
     * requestNetworksList} and {@link #requestOnBoarding(String, OnBoardingListener, boolean, String, String)}
     * requestOnBoarding}.</p>
     */
    public void cancel() {
        mHttpClient.reset();
        for (WifiConnector.WifiConnectorListener listener : mWifiSubscribers) {
            unsubscribeWifiConnectorListener(listener);
        }
    }


    // ========================================================================
    // PRIVATE METHODS

    /**
     * <p>To stop all ongoing actions and release all system resources.</p>
     *
     * @param appContext
     *         The application context to release any system resource.
     */
    private void stopAll(Context appContext) {
        mWifiConnector.release(appContext);
        cancel();
        mHttpClient.release();
    }

    /**
     * <p>This method checks if the given network is the connected wifi network.</p>
     *
     * @param ssid
     *         The ssid of network to check the connection state of. The SSID is expected without quotes.
     *
     * @return True if it is the connected network, false otherwise.
     */
    private boolean isConnected(String ssid) {
        return mWifiConnector.isConnected(ssid);
    }

    /**
     * <p>This method initialises the time stamp for this request if <code>initTimeStamp</code> is set to
     * <code>true</code>. Then it calls {@link #request(String, OnBoardingListener, RequestType, Object...)} to
     * start the process for the request {@link RequestType#VERIFY_CONNECTING VERIFY_CONNECTING}.</p>
     *
     * @param listener
     *         The listener to get the result of the request operation.
     * @param expectedConnectedSsid
     *         The ssid of the network the device should be the gateway of.
     * @param initTimeStamp
     *         True to start the time stamp for {@link RequestType#VERIFY_CONNECTING VERIFY_CONNECTING},
     *         false to only do the request.
     */
    private void requestVerifyConnecting(OnBoardingListener listener, String expectedConnectedSsid,
                                         boolean initTimeStamp) {
        if (initTimeStamp) {
            if (listener != null) {
                // this step starts only when the timer is initialised
                listener.onStepStart(OnBoardingStep.CONNECTING);
            }
            mVerifyConnectingTimeStamp = System.currentTimeMillis();
        }
        request(expectedConnectedSsid, listener, RequestType.VERIFY_CONNECTING);
    }

    /**
     * <p>This method initialises the HTTP request and wifi connector listeners in order to connect to a wifi
     * network to send a HTTP request to its gateway - the expected device/speaker.</p>
     * <p>This method uses the given OnBoardingListener to build the other listeners.</p>
     * <p>This method informs the given listener that a step is starting by calling
     * {@link OnBoardingListener#onStepStart(OnBoardingStep) onStepStart(OnBoardingStep)} except when the given
     * type is {@link RequestType#VERIFY_CONNECTING VERIFY_CONNECTING} as the process loops on that request leading
     * the step to have already started.</p>
     *
     * @param expectedConnectedSsid
     *         The ssid of the network the device should be the gateway of.
     * @param listener
     *         The listener to get the result of the operation.
     * @param type
     *         The type of request ({@link RequestType RequestType}) which should be sent to the device.
     * @param args
     *         The list of arguments to send in the request.
     *
     * @return True if the operation could be initiated, false otherwise.
     */
    private boolean request(String expectedConnectedSsid, OnBoardingListener listener, RequestType type,
                            Object... args) {
        if (type != RequestType.VERIFY_CONNECTING && listener != null) {
            // the process loops on this request => the step might have already started when this is called
            listener.onStepStart(getStepFromRequestType(type));
        }
        RequestListener requestListener = buildRequestListener(expectedConnectedSsid, listener);
        WifiConnector.WifiConnectorListener wifiListener = buildWifiConnectorListener(type,
                                                                                      expectedConnectedSsid,
                                                                                      requestListener, listener, args);
        return request(expectedConnectedSsid, type, requestListener, wifiListener, args);
    }

    /**
     * <p>This method initialises the HTTP request and wifi connector listeners in order to connect to a wifi
     * network to send a HTTP request to its gateway - the expected device/speaker.</p>
     * <p>This method uses the given OnBoardingListener to build the other listeners.</p>
     *
     * @param expectedConnectedSsid
     *         The ssid of the device to on board.
     * @param type
     *         The type of request ({@link RequestType RequestType}) which should be sent to the device.
     * @param requestListener
     *         the listener to get the result of the HTTP request.
     * @param wifiListener
     *         the listener to get the results of connection to the device/speaker network if it wasn't
     *         already connected to it.
     * @param args
     *         The list of arguments to send in the request.
     *
     * @return True if the operation could be initiated, false otherwise.
     */
    private boolean request(String expectedConnectedSsid, RequestType type, RequestListener requestListener,
                            WifiConnector.WifiConnectorListener wifiListener, Object... args) {
        if (isConnected(expectedConnectedSsid)) {
            return request(type, requestListener, args);
        }
        else {
            subscribeWifiConnectorListener(wifiListener);
            WifiNetwork networkToConnect = new WifiNetwork(expectedConnectedSsid, 0, true);
            return connectToWifiNetwork(networkToConnect) == SUCCESS;
        }
    }

    /**
     * <p>This method looks for the gateway of the current connected network and asks to the
     * {@link OnBoardingHttpClient OnBoardingHttpClient} to send the given request using the gateway IP address.</p>
     *
     * @param type
     *         the type of request to send to the gateway.
     * @param listener
     *         the listener to be notified of the success or failure of the request.
     * @param args
     *         Any arguments to be added to the request, depends on the request type.
     *
     * @return True if the operation could be initiated, false otherwise.
     */
    private boolean request(RequestType type, RequestListener listener, Object... args) {
        String gatewayIpAddress = mWifiConnector.getGatewayIpAddress();

        if (gatewayIpAddress == null || gatewayIpAddress.isEmpty()) {
            Log.w(TAG, "[request] No gateway IP address.");
            return false;
        }

        return mHttpClient.request(type, gatewayIpAddress, listener, args);
    }

    /**
     * <p>This method builds a RequestListener for the HTTP request from a NetworksListener.</p>
     * <p>This method ensures that the responses received matches the expectations for the NetworksListener before
     * to push them into it.</p>
     *
     * @param listener
     *         The NetworksListener to get the responses from the RequestListener.
     *
     * @return A RequestListener which forwards its calls to the given NetworksListener.
     */
    private RequestListener buildRequestListener(NetworksListener listener) {
        return new RequestListener() {
            @Override // RequestListener
            public void onError(RequestType type, RequestResult requestResult, Object[] args) {
                if (type == RequestType.REFRESH_SCAN_LIST) {
                    listener.onError(NetworksListError.REQUEST_ERROR, buildArguments(args, requestResult));
                }
                else {
                    Log.w(TAG, "[buildRequestListener->onError] Received unexpected request type, " +
                            "expected=REFRESH_SCAN_LIST, received=" + type);
                }
            }

            @Override // RequestListener
            public void onComplete(RequestType type, Object response) {
                if (RequestType.REFRESH_SCAN_LIST.equals(type) && response instanceof List) {
                    listener.onComplete((List<WifiNetwork>) response);
                }
                else if (RequestType.REFRESH_SCAN_LIST.equals(type)) {
                    listener.onError(NetworksListError.REQUEST_ERROR,
                                     buildArguments(RequestResult.READING_RESPONSE_FAILED));
                }
                else {
                    Log.w(TAG, "[buildRequestListener->onComplete] Received unexpected request type, " +
                            "expected=REFRESH_SCAN_LIST, received=" + type);
                }
            }
        };
    }

    /**
     * <p>This method builds a RequestListener for the HTTP request from a OnBoardingListener.</p>
     *
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been sent the request to.
     * @param listener
     *         The OnBoardingListener to get the responses from the RequestListener.
     *
     * @return A RequestListener which forwards its calls to the given OnBoardingListener.
     */
    private RequestListener buildRequestListener(String expectedConnectedSsid, OnBoardingListener listener) {
        return new RequestListener() {
            @Override // RequestListener
            public void onError(RequestType type, RequestResult requestResult, Object[] args) {
                OnBoardingStep step = getStepFromRequestType(type);
                if (RequestType.VERIFY_CONNECTING.equals(type) && RequestResult.REQUEST_TIMEOUT.equals(requestResult)) {
                    // VERIFY_CONNECT has timed out after 10 attempts over a time of 60s
                    // we are checking the connection status
                    request(expectedConnectedSsid, listener, RequestType.CHECK_CONNECT_STATUS);
                }
                listener.onError(step, OnBoardingError.REQUEST_ERROR, buildArguments(args, requestResult));
            }

            @Override // RequestListener
            public void onComplete(RequestType requestType, Object response) {
                onBoardingRequestComplete(listener, expectedConnectedSsid, requestType, response);
            }
        };
    }

    /**
     * <p>This method redirects the request's response to the right processing method depending on the type of
     * request.</p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the RequestListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been sent the request to.
     * @param requestType
     *         the type of request a response has been received for.
     * @param response
     *         the response sent over HTTP.
     */
    private void onBoardingRequestComplete(OnBoardingListener listener, String expectedConnectedSsid,
                                           RequestType requestType, Object response) {
        switch (requestType) {
            case VERIFY_CONNECT:
                processConnectComplete(listener, expectedConnectedSsid);
                return;

            case VERIFY_CONNECTING:
                processVerifyConnectComplete(listener, expectedConnectedSsid, response);
                return;

            case CHECK_CONNECT_STATUS:
                processCheckConnectStatus(listener, expectedConnectedSsid, response);
                return;

            case JOIN:
                processJoinComplete(listener);
                return;

            case WPS:
                processWpsComplete(listener, expectedConnectedSsid, response);
                return;

            case REFRESH_SCAN_LIST:
                // unexpected: the REFRESH_SCAN_LIST request is not part of the on boarding process
                break;
        }

        Log.w(TAG, String.format("[onBoardingRequestComplete] unable to process the request, expectedSsid=%s, " +
                                         "requestType=%s, response=%s", expectedConnectedSsid, requestType,
                                 response));
        listener.onError(getStepFromRequestType(requestType), OnBoardingError.PROCESS_FAILED, null);

    }

    /**
     * <p>This method processes a successful response (empty) for the {@link RequestType#VERIFY_CONNECT VERIFY_CONNECT}
     * request.</p>
     * <p>As the response is empty, it moves the on boarding to its next step:
     * {@link OnBoardingStep#CONNECTING CONNECTING}.</p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the RequestListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been sent the
     *         request to.
     */
    private void processConnectComplete(OnBoardingListener listener, String expectedConnectedSsid) {
        listener.onStepComplete(OnBoardingStep.SENDING_CREDENTIALS);
        requestVerifyConnecting(listener, expectedConnectedSsid, true);
    }

    /**
     * <p>This method processes a successful response (empty) for the {@link RequestType#JOIN JOIN} request.</p>
     * <p>As the response is empty, it moves the on boarding to its next step:
     * {@link OnBoardingStep#DISCONNECTION DISCONNECTION}.</p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the RequestListener.
     */
    private void processJoinComplete(OnBoardingListener listener) {
        listener.onStepComplete(OnBoardingStep.JOIN);
        listener.onStepStart(OnBoardingStep.DISCONNECTION);
        // no more HTTP requests: next step is to wait for the wifi to disconnect
    }

    /**
     * <p>This method processes a successful response for the {@link RequestType#VERIFY_CONNECTING VERIFY_CONNECTING}
     * request.</p>
     * <p>Depending on the response, this method acts as follows:
     * <ul>
     * <li>{@link ConnectingStatus#SUCCESS SUCCESS}: the connection process has completed on the device, the on
     * boarding process can be moved to the next step:
     * {@link OnBoardingStep#CHECK_CONNECTION_STATUS CHECK_CONNECTION_STATUS}.</li>
     * <li>{@link ConnectingStatus#CONNECTING CONNECTING}: the connection process is still in progress, this
     * method continues to use this request until either {@link ConnectingStatus#SUCCESS SUCCESS} is received or
     * marks the on boarding process has failed if the time defined with
     * {@link #VERIFY_CONNECTING_TIME_OUT_MS VERIFY_CONNECTING_TIME_OUT_MS} has happened since the first
     * {@link RequestType#VERIFY_CONNECTING VERIFY_CONNECTING} was sent.</li>
     * <li>Any other response is unexpected and will mark the on boarding process as a failure.</li>
     * </ul>
     * </p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the RequestListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been sent the
     *         request to.
     * @param response
     *         the response sent over HTTP, it is expected to be of type {@link ConnectingStatus
     *         ConnectingStatus}.
     */
    private void processVerifyConnectComplete(OnBoardingListener listener, String expectedConnectedSsid,
                                              Object response) {
        if (!(response instanceof ConnectingStatus)) {
            Log.w(TAG, "[processVerifyConnectComplete] Unexpected response: " + response);
            return;
        }

        ConnectingStatus connectingStatus = (ConnectingStatus) response;

        switch (connectingStatus) {
            case SUCCESS:
                listener.onStepComplete(OnBoardingStep.CONNECTING);
                break;
            case CONNECTING:
                if (VERIFY_CONNECTING_TIME_OUT_MS > System.currentTimeMillis() - mVerifyConnectingTimeStamp) {
                    // verify connecting needs to be tried until SUCCESS is returned or it times out
                    requestVerifyConnecting(listener, expectedConnectedSsid, false);
                    return;
                }
                // otherwise: it has failed for process time out.
            case UNKNOWN:
            default:
                Object[] args = {connectingStatus};
                listener.onError(getStepFromRequestType(RequestType.VERIFY_CONNECTING),
                                 OnBoardingError.ON_VERIFY_CONNECTING_FAILURE, args);
                // process to continue to know the reason of the error.
                break;
        }

        request(expectedConnectedSsid, listener, RequestType.CHECK_CONNECT_STATUS);
    }

    /**
     * <p>This method processes a successful response for the {@link RequestType#WPS WPS} request.</p>
     * <p>Depending on the response, this method acts as follows:
     * <ul>
     * <li>{@link WpsStatus#SUCCESS SUCCESS}: the on boarding process can be moved to the next step:
     * {@link RequestType#VERIFY_CONNECTING VERIFY_CONNECTING}.</li>
     * <li>Any other response is unexpected and will mark the on boarding process as a failure.</li>
     * </ul>
     * </p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the RequestListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been sent the
     *         request to.
     * @param response
     *         the response sent over HTTP, it is expected to be of type {@link WpsStatus WpsStatus}.
     */
    private void processWpsComplete(OnBoardingListener listener, String expectedConnectedSsid,
                                    Object response) {
        if (!(response instanceof WpsStatus)) {
            Log.w(TAG, "[processWpsComplete] Unexpected response: " + response);
            return;
        }

        WpsStatus wpsStatus = (WpsStatus) response;

        if (wpsStatus.equals(WpsStatus.SUCCESS)) {
            listener.onStepComplete(OnBoardingStep.SENDING_CREDENTIALS);
            requestVerifyConnecting(listener, expectedConnectedSsid, true);
        }
        else {
            listener.onError(getStepFromRequestType(RequestType.WPS), OnBoardingError.WPS_FAILED, null);
        }
    }

    /**
     * <p>This method processes a successful response for the
     * {@link RequestType#CHECK_CONNECT_STATUS CHECK_CONNECT_STATUS} request.</p>
     * <p>Depending on the response, this method acts as follows:
     * <ul>
     * <li>{@link ConnectionStatus#SUCCESS SUCCESS}: the on boarding process can be moved to the next step:
     * {@link OnBoardingStep#JOIN JOIN}. In prevention of the following step, this method will also register for
     * WIFI connection status for {@link OnBoardingStep#DISCONNECTION DISCONNECTION}.</li>
     * <li>Any other response means the speaker has failed to connect to the given WIFI network and marks the on
     * boarding process as failed.</li>
     * </ul>
     * </p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the RequestListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been sent the
     *         request to.
     * @param response
     *         the response sent over HTTP, it is expected to be of type
     *         {@link ConnectionStatus ConnectionStatus}.
     */
    private void processCheckConnectStatus(OnBoardingListener listener,
                                           String expectedConnectedSsid,
                                           Object response) {
        if (!(response instanceof ConnectionStatus)) {
            Log.w(TAG, "[processCheckConnectStatus] Unexpected response: " + response);
            return;
        }

        ConnectionStatus connectionStatus = (ConnectionStatus) response;

        if (connectionStatus.equals(ConnectionStatus.SUCCESS)) {
            listener.onStepComplete(OnBoardingStep.CHECK_CONNECTION_STATUS);
            subscribeWifiConnectorListener(buildDisconnectionListener(expectedConnectedSsid, listener));
            RequestType requestType = RequestType.JOIN;
            request(expectedConnectedSsid, listener, requestType);
        }
        else {
            Log.w(TAG, "[processCheckConnectStatus] Connecting the speaker to the network failed, status: "
                    + connectionStatus);
            listener.onError(getStepFromRequestType(RequestType.CHECK_CONNECT_STATUS),
                             OnBoardingError.CONNECTION_FAILED_REASON, buildArguments(connectionStatus));
        }
    }

    /**
     * <p>This method analyses the given {@link RequestType RequestType} and provides the corresponding
     * {@link OnBoardingStep OnBoardingStep}.</p>
     *
     * @param type
     *         the {@link RequestType RequestType} to get the corresponding {@link OnBoardingStep
     *         OnBoardingStep} from.
     *
     * @return The corresponding {@link OnBoardingStep OnBoardingStep}.
     */
    private static OnBoardingStep getStepFromRequestType(RequestType type) {
        switch (type) {
            case VERIFY_CONNECT:
            case WPS:
                return OnBoardingStep.SENDING_CREDENTIALS;
            case VERIFY_CONNECTING:
                return OnBoardingStep.CONNECTING;
            case CHECK_CONNECT_STATUS:
                return OnBoardingStep.CHECK_CONNECTION_STATUS;
            case JOIN:
                return OnBoardingStep.JOIN;
            case REFRESH_SCAN_LIST:
            default:
                return null;
        }
    }

    /**
     * <p>This method builds an array of arguments by creating a new array where the <code>newElements</code> will
     * be added to the <code>source</code> elements. The new elements are added at the beginning of the array in the
     * order they are provided.</p>
     *
     * @param source
     *         An existing array of arguments.
     * @param newElements
     *         a list of new elements to add at the beginning of the existing arguments.
     *
     * @return the array generated by juxtaposing the new elements to the existing source.
     */
    private static Object[] buildArguments(Object[] source, Object... newElements) {
        Object[] newArgs = new Object[source.length + newElements.length];
        // add new elements at the beginning
        System.arraycopy(newElements, 0, newArgs, 0, newElements.length);
        // add source elements at the end
        System.arraycopy(source, 0, newArgs, newElements.length, source.length);
        return newArgs;
    }

    /**
     * <p>This method builds an array of objects from the given list of objects.</p>
     *
     * @param newElements
     *         the objects to add to an array in the order they should be in the array.
     *
     * @return An array which contains the given parameters.
     */
    private static Object[] buildArguments(Object... newElements) {
        return newElements;
    }

    /**
     * <p>To build a {@link com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.WifiConnectorListener
     * WifiConnectorListener} from a {@link com.qualcomm.qti.iotcontrollersdk.iotsys.listeners.NetworkListener
     * NetworkListener}.</p>
     * <p>The built WifiConnectorListener analyses the updates it receives before to transfer them to the
     * NetworksListener: in case of a state update it only transfers if the event is a
     * {@link com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.ConnectionState#CONNECTED CONNECTED} for
     * the given <code>expectedConnectedSsid</code>. In case of an error it only transfers it if it is about the
     * given <code>expectedConnectedSsid</code>.</p>
     *
     * @param listener
     *         the NetworksListener to get the responses from the WifiConnectorListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been connected to or
     *         disconnected from.
     *
     * @return A WifiConnectorListener which implementation transfers the call to the given NetworkListener.
     */
    private WifiConnector.WifiConnectorListener buildWifiConnectorListener(String expectedConnectedSsid,
                                                                           NetworksListener listener) {
        return new WifiConnector.WifiConnectorListener() {
            @Override // WifiConnector.WifiConnectorListener
            public void onConnectionStateUpdated(@NonNull String ssid, @WifiConnector.ConnectionState int state) {
                if (ssid.equals(expectedConnectedSsid) && state == WifiConnector.ConnectionState.CONNECTED) {
                    request(RequestType.REFRESH_SCAN_LIST, buildRequestListener(listener));
                }
                unsubscribeWifiConnectorListener(this);
            }

            @Override // WifiConnector.WifiConnectorListener
            public void onError(WifiNetwork network, @WifiConnector.ConnectionError int error) {
                if (network.getSSID().equals(expectedConnectedSsid)) {
                    listener.onError(NetworksListError.WIFI_NETWORK_ERROR, buildArguments(expectedConnectedSsid,
                                                                                          error));
                    unsubscribeWifiConnectorListener(this);
                }
            }
        };
    }

    /**
     * <p>To build a {@link com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.WifiConnectorListener
     * WifiConnectorListener} from a {@link OnBoardingListener OnBoardingListener}.</p>
     * <p>The built WifiConnectorListener analyses the updates it receives before to transfer them to the
     * OnBoardingListener: in case of a state update it only transfers if the event is a
     * {@link com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.ConnectionState#DISCONNECTED DISCONNECTED}
     * for the given <code>expectedConnectedSsid</code>. In case of an error it only transfers it if it is about the
     * given <code>expectedConnectedSsid</code>.</p>
     * <p>The built WifiConnectorListener is used to detect the end of the last step of the Wifi on boarding
     * process: {@link OnBoardingStep#DISCONNECTION DISCONNECTION}.</p>
     *
     * @param listener
     *         the OnBoardingListener to get the responses from the WifiConnectorListener.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been connected to or
     *         disconnected from.
     *
     * @return A WifiConnectorListener which implementation transfers the call to the given OnBoardingListener.
     */
    private WifiConnector.WifiConnectorListener buildDisconnectionListener(String expectedConnectedSsid,
                                                                           OnBoardingListener listener) {
        return new WifiConnector.WifiConnectorListener() {
            @Override // WifiConnector.WifiConnectorListener
            public void onConnectionStateUpdated(@NonNull String ssid, @WifiConnector.ConnectionState int state) {
                if (ssid.equals(expectedConnectedSsid) && state == WifiConnector.ConnectionState.DISCONNECTED) {
                    listener.onStepComplete(OnBoardingStep.DISCONNECTION);
                    listener.onComplete();
                    unsubscribeWifiConnectorListener(this);
                }
            }

            @Override // WifiConnector.WifiConnectorListener
            public void onError(WifiNetwork network, @WifiConnector.ConnectionError int error) {
                if (network.getSSID().equals(expectedConnectedSsid)) {
                    listener.onError(OnBoardingStep.DISCONNECTION, OnBoardingError.WIFI_NETWORK_ERROR,
                                     buildArguments(expectedConnectedSsid, error));
                    unsubscribeWifiConnectorListener(this);
                }
            }
        };
    }

    /**
     * <p>To build a {@link com.qualcomm.qti.smartaudio.manager.onboarding.WifiConnector.WifiConnectorListener
     * WifiConnectorListener} based on a {@link RequestListener RequestListener} and a {@link OnBoardingListener
     * OnBoardingListener}.</p>
     * <p>This WifiConnectorListener is used to detect the connection to the given <code>expectedConnectedSsid</code>
     * if the system was not connected to it when a HTTP request is initiated.</p>
     *
     * @param type
     *         the type of request waiting for the network to be connected.
     * @param expectedConnectedSsid
     *         the SSID of the device/speaker which is expected to have been connected to or
     *         disconnected from.
     * @param requestListener
     *         A listener for the request to send in case of a successful connection.
     * @param boardingListener
     *         The OnBoardingListener to be informed of failures: request not sent, disconnections, etc.
     * @param args
     *         Any complementary arguments to send with the request.
     *
     * @return A WifiConnectorListener which implementation transfers the call to the given NetworkListener.
     */
    private WifiConnector.WifiConnectorListener buildWifiConnectorListener(RequestType type,
                                                                           String expectedConnectedSsid,
                                                                           RequestListener requestListener,
                                                                           OnBoardingListener boardingListener,
                                                                           Object... args) {
        return new WifiConnector.WifiConnectorListener() {
            @Override // WifiConnector.WifiConnectorListener
            public void onConnectionStateUpdated(@NonNull String ssid,
                                                 @WifiConnector.ConnectionState int connectionState) {
                if (ssid.equals(expectedConnectedSsid) && connectionState == WifiConnector.ConnectionState.CONNECTED) {
                    if (!request(type, requestListener, args)) {
                        boardingListener.onError(getStepFromRequestType(type), OnBoardingError.INIT_REQUEST_FAILED,
                                                 null);
                    }
                    unsubscribeWifiConnectorListener(this);
                }
            }

            @Override // WifiConnector.WifiConnectorListener
            public void onError(WifiNetwork network, @WifiConnector.ConnectionError int error) {
                if (network.getSSID().equals(expectedConnectedSsid)) {
                    boardingListener.onError(getStepFromRequestType(type), OnBoardingError.WIFI_NETWORK_ERROR,
                                             buildArguments(expectedConnectedSsid, error));
                    unsubscribeWifiConnectorListener(this);
                }
            }
        };
    }


    // ========================================================================
    // INTERFACES

    /**
     * A listener to be informed of the progress during the on boarding.
     */
    public interface OnBoardingListener {

        /**
         * <p>This is called when one a step has successfully completed.</p>
         *
         * @param step the step which has been completed.
         */
        void onStepComplete(OnBoardingStep step);

        /**
         * <p>This is called when a step is starting.</p>
         *
         * @param step The step which starts.
         */
        void onStepStart(OnBoardingStep step);

        /**
         * <p>This is a called when a step has failed and provides the error which occurs.</p>
         *
         * @param step the step which has failed.
         * @param error the reason of the failure.
         * @param args any complementary arguments to the failure.
         */
        void onError(OnBoardingStep step, OnBoardingError error, Object[] args);

        /**
         * This is called when the on boarding has successfully completed.
         */
        void onComplete();
    }

    /**
     * A listener to receive the list of networks a speaker can connect to.
     */
    public interface NetworksListener {

        /**
         * <p>This method is called when the request to get the list of networks fails.</p>
         *
         * @param error the reason of the failure.
         * @param args any complementary arguments to the failure.
         */
        void onError(NetworksListError error, Object[] args);

        /**
         * <p>This method is called when the request has successfully completed and has provided a response.</p>
         *
         * @param networks the list of networks a speaker can connect with to be on boarded with it.
         */
        void onComplete(List<WifiNetwork> networks);
    }
}
