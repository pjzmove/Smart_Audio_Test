/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;
import java.util.ArrayList;
import java.util.List;

/**
 * The plain object represents a group info item
 */
public class PlayerGroupInfo {

    public String mGroupName;
    public String mGroupId;
    public List<PlayerGroupMember> mMembers;
    public boolean isSinglePlayer;

  /**
   * Class constructor
   *
   * @param name is the group name
   * @param id is the grouop id
   * @param items is the list of members which belong to the group
   */
  public PlayerGroupInfo(String name, String id, List<PlayerGroupMember> items, boolean isSinglePlayer) {
      this.mGroupName = name;
      this.mGroupId = id;
      this.mMembers = items;
      this.isSinglePlayer = isSinglePlayer;
    }

    /**
     * Class copy constructor
     *
     * @param groupInfo is original PlayerGroupInfo object
     */
    public PlayerGroupInfo(PlayerGroupInfo groupInfo) {
      mGroupName = groupInfo.mGroupName;
      mGroupId = groupInfo.mGroupId;
      isSinglePlayer = groupInfo.isSinglePlayer;
      mMembers = new ArrayList<>();
      for(PlayerGroupMember info : groupInfo.mMembers)
        mMembers.add(new PlayerGroupMember(info.deviceId, info.displayName, info.available,info.isLicensed));
    }

    @Override
    public boolean equals(Object otherItem) {
      if ((otherItem == null) || !(otherItem instanceof PlayerGroupInfo)) {
        return false;
      }
      return mGroupId.equalsIgnoreCase(((PlayerGroupInfo) otherItem).mGroupId);
    }
}