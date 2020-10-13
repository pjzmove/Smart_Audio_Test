/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistGetRangeOutAttr;

public interface PlaylistGetRangeListener {
    void OnPlaylistGetRangeOutCompleted(PlaylistGetRangeOutAttr attribute, boolean status);
}









