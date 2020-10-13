/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr;

public interface MediaPlayerListener {

    void OnGetMediaPlayerCompleted(MediaPlayerAttr attribute, boolean status);
    default void OnMediaPlayerCompleted(boolean status){}
}









