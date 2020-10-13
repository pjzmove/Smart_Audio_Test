/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistInsertOutAttr;

public interface PlaylistInsertListener {
    void OnPlaylistInsertOutCompleted(PlaylistInsertOutAttr attribute, boolean status);
}









