/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.requests;

/**
 * <p>This enumeration lists all requests which are used in the on boarding process of a device.</p>
 * <p>Each item of this enumeration is built with the corresponding CGI command to use in a HTTP command.</p>
 */
public enum RequestType {
    /**
     * <p>This is used to get the list of networks the device can found/connect to.</p>
     * <p>This request expects no argument.</p>
     */
    REFRESH_SCAN_LIST("refresh_scan_list"),
    /**
     * <p>This is used to send the credentials of the network the device should connect to.</p>
     * <p>This method expects the following arguments:
     * <ul>
     *     <li>the SSID of the network to connect to.</li>
     *     <li>The password to join the network if the network is secure.</li>
     * </ul></p>
     */
    VERIFY_CONNECT("verify_connect"),
    /**
     * <p>This is used to know if the connection process is still ongoing.</p>
     * <p>This request expects no argument.</p>
     */
    VERIFY_CONNECTING("verify_connecting"),
    /**
     * This is used to know the result of the connection.
     * <p>This request expects no argument.</p>
     */
    CHECK_CONNECT_STATUS("check_connect_status"),
    /**
     * This is used for the device to join the network in order to devices on the network to use it.
     * <p>This request expects no argument.</p>
     */
    JOIN("join"),
    /**
     * This is used to connect to a network using WPS.
     * <p>This method expects the following arguments:
     * <ul>
     *     <li>the SSID of the network to connect to.</li>
     * </ul></p>
     */
    WPS("wps");

    /**
     * To store the CGI command which corresponds to the item.
     */
    private final String command;

    /**
     * To build a new item of the enumeration with its corresponding CGI command.
     *
     * @param command the command to store for the item.
     */
    RequestType(String command) {
        this.command = command;
    }

    /**
     * To get the CGI which corresponds to the item - this is to build the request URL.
     *
     * @return the CGI command.
     */
    public String getCommand() {
        return command;
    }
}
