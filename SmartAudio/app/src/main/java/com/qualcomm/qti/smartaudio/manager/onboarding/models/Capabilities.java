/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager.onboarding.models;

import android.annotation.SuppressLint;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * All the security capabilities a WiFi Network can have known by this application.
 */
@StringDef(value = { Capabilities.EAP, Capabilities.PSK, Capabilities.WPA, Capabilities.WEP,
        Capabilities.WPS, Capabilities.ESS, Capabilities.IEEE8021X })
@Retention(RetentionPolicy.SOURCE)
@SuppressLint("ShiftFlags") // it is more human readable this way
public @interface Capabilities {
    String WPA = "WPA";
    String WEP = "WEP";
    String WPS = "WPS";
    String ESS = "ESS";
    String PSK = "PSK";
    String EAP = "EAP";
    String IEEE8021X = "IEEE8021X";

    String[] SECURITY_TYPES = new String[] { WEP, PSK, EAP };
}
