/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistGetHistoryOutAttr;

public interface PlaylistGetHistoryListener {
    void OnPlaylistGetHistoryOutCompleted(PlaylistGetHistoryOutAttr attribute, boolean status);
}









