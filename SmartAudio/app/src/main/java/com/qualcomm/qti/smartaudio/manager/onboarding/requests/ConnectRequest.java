/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

/**
 * <p>This class manages the building of a request to connect to a wifi network for the WiFi on Boarding process.</p>
 * <p>This request is a POST HTTP request which contains two parameters: the SSID of the network to connect with and
 * the password of the network.</p>
 */
public class ConnectRequest extends DefaultPostRequest {

    /**
     * The format to set the parameters in the request.
     */
    private static final String PARAMETERS_FORMAT = "ssid=%s&key=%s";
    /**
     * An identifier for this request.
     */
    private static final RequestType REQUEST_TYPE = RequestType.VERIFY_CONNECT;

    /**
     * <p>To build a new request of type connect.</p>
     * <p>This method initialises its super class with the type {@link RequestType#VERIFY_CONNECT VERIFY_CONNECT} and the givne
     * <code>url</code> and <code>listener</code>.</p>
     *
     * @param url The url to use to send a connection instruction.
     * @param ssid The SSID of the network to add to the request.
     * @param key The Key of the network to add to the request.
     * @param listener The listener to be notified of the success or failure for this request.
     */
    ConnectRequest(String url, String ssid, String key, RequestListener listener) {
        super(REQUEST_TYPE, url, buildRequestBody(ssid, key), listener);
    }

    /**
     * <p>To build the body of the request with the given parameters using the format
     * {@link #PARAMETERS_FORMAT PARAMETERS_FORMAT}.</p>
     *
     * @param ssid the ssid parameter as the SSID of the network to connect with.
     * @param key the key parameter as the password of the network to connect with.
     *
     * @return A String value which contains the parameters.
     */
    private static String buildRequestBody(String ssid, String key) {
        ssid = ssid == null ? "" : ssid;
        key = key == null ? "" : key;
        return String.format(PARAMETERS_FORMAT, ssid, key);
    }

    @Override // HttpRequest
    public void onResponse(String response) {
        // no content is expected in the response
        onComplete(response);
    }
}
