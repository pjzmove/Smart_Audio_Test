/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.models;

/**
 * Enumeration of network authentication types supported
 */
public enum AuthType {
    /**
     * OPEN authentication
     */
    OPEN,
    /**
     * secure with password: WPA, WPA2, WEP
     */
    SECURE,
    /**
     * WPS authentication
     */
    WPS;
}
