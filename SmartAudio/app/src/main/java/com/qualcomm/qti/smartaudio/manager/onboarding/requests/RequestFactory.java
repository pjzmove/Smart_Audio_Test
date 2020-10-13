/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

public class RequestFactory {

    public static HttpRequest buildRequest(RequestType requestType, String url, RequestListener listener,
                                           Object[] args) {
        String ssid, key;
        switch (requestType) {
            case REFRESH_SCAN_LIST: // get the list of networks scanned by the device
                return new NetworksListRequest(url, listener);
            case VERIFY_CONNECT: // use to connect to a network
                ssid = args.length > 0 ? (String) args[0] : "";
                key = args.length > 1 ? (String) args[1] : "";
                return new ConnectRequest(url, ssid, key, listener);
            case VERIFY_CONNECTING: // use until connection is successful, error or timeout
                return new VerifyConnectingRequest(url, listener);
            case CHECK_CONNECT_STATUS: // check the connection state
                return new CheckConnectStatusRequest(url, listener);
            case JOIN: // use to join network, old client was using post
                return new DefaultGetRequest(requestType, url, listener);
            case WPS: // post with ssid as a parameter
                ssid = args.length > 0 ? (String) args[0] : "";
                return new WpsRequest(url, ssid, listener);
        }

        return null;
    }

}
