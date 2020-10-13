/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PlayerInfo {
    public String name;
    public String host;
    public String id;
    public List<String> groupIds;
    public List<String> groupNames;
    public boolean isAvailable;
    public boolean isEmptyGroup;

    public PlayerInfo(String host,String name, String id, boolean isAvailable) {
      this.host = host;
      this.name = name;
      this.id = id;
      this.isAvailable = isAvailable;
      isEmptyGroup = false;
    }

    public void addGroupId(String id) {
      if(groupIds == null)
        groupIds = new ArrayList<>();

      if(!groupIds.contains(id))
        groupIds.add(id);
    }

    public void addGroupName(String name) {
      if(groupNames == null)
        groupNames = new ArrayList<>();

      if(!groupNames.contains(name))
        groupNames.add(name);
    }

    @Override
    public boolean equals(@NotNull Object that) {
      if (that == null) return false;
      return this.id.equalsIgnoreCase(((PlayerInfo) that).id);
    }

}
