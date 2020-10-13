/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import org.jetbrains.annotations.NotNull;

public class PlayerGroupMember {
    public String host;
    public String deviceId;
    public String displayName;
    public boolean available;
    public boolean isLicensed;

    public PlayerGroupMember(PlayerGroupMember member) {
      /*host = member.host;*/
      deviceId = member.deviceId;
      displayName = member.displayName;
      available = member.available;
      isLicensed = member.isLicensed;
    }

    public PlayerGroupMember(/*String host,*/ String id, String name, boolean isAvailable, boolean isLicensed) {
      /*this.host = host;*/
      deviceId = id;
      displayName = name;
      available = isAvailable;
      this.isLicensed = isLicensed;
    }

  @Override
  public boolean equals(@NotNull Object obj) {
    if (obj == null) return false;
    PlayerGroupMember that = (PlayerGroupMember) obj;
    return deviceId != null && deviceId.equalsIgnoreCase(that.deviceId);
  }

}