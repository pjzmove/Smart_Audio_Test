/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistMoveOutAttr;

public interface PlaylistMoveListener {
    void OnPlaylistMoveOutCompleted(PlaylistMoveOutAttr attribute, boolean status);
}









