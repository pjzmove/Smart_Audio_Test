/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupInfoHelperAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupMembersHelperAttr;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import java.util.ArrayList;
import java.util.List;

public class PlayerGroupsInfo {

    private final String TAG = "GroupsInfo";

    private final List<PlayerGroupInfo> mInfoList = new ArrayList<>();

  /**
   * Use given player's Group Resource attribute to produce group information list
   * Note: The player may not be added in any groups
   * @param grpAttr Group Resource Attribute
   * @param host Player's IP address
   */

  public synchronized void processAttribute(GroupAttr grpAttr/*, String host*/) {
      mInfoList.clear();
      for(GroupInfoHelperAttr attr:grpAttr.mGroupInfo) {
        List<PlayerGroupMember> mMemberList = new ArrayList<>(attr.mGroupMembersList.size());
        for(GroupMembersHelperAttr member:attr.mGroupMembersList) {
          IoTPlayer player = IoTService.getInstance().getPlayerByDeviceId(member.mMemberId);
          boolean isLicensed = (player != null && player.isLicensed());
          PlayerGroupMember info = new PlayerGroupMember(/*host,*/member.mMemberId,member.mMemberName,true,isLicensed);
          mMemberList.add(info);
        }

        Log.d(TAG,String.format("Group Name:%s, Group Id:%s, members size:%d",attr.mGroupName, attr.mGroupId,attr.mGroupMembersList.size()/*, host*/));
        PlayerGroupInfo groupInfo = new PlayerGroupInfo(attr.mGroupName, attr.mGroupId, mMemberList,false);
        mInfoList.add(groupInfo);
      }
    }

    /**
     * Copy current Group information list and return to the caller
     * @return Group info list
     */
    public synchronized List<PlayerGroupInfo> getList() {
      List<PlayerGroupInfo> retList = new ArrayList<>();
      mInfoList.forEach(info -> retList.add(new PlayerGroupInfo(info)));
      return retList;
    }
}
