/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

public class InputOutputSourceInfo {

    public String name;
    public String id;
    public boolean isActivated;

    public InputOutputSourceInfo(String name, String id, boolean activated) {
      this.name = name;
      this.id = id;
      this.isActivated = activated;
    }
}
