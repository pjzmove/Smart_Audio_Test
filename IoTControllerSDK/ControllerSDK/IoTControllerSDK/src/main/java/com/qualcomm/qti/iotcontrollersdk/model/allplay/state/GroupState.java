/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay.state;

import com.qualcomm.qti.iotcontrollersdk.utils.GenericStateApi;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupInfoHelperAttr;
import java.util.List;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;

public class GroupState extends ResourceState {

    private final GroupAttr groupAttr = new GroupAttr();

    public GroupState() {

    }

    public synchronized void update(GroupAttr attr) {
      GenericStateApi.setState(groupAttr,attr);
    }

    public synchronized boolean update(OcRepresentation rep) throws OcException {
        return GenericStateApi.updateState(groupAttr, rep);
    }

    public synchronized GroupAttr getAttribute(){
      GroupAttr ret = (GroupAttr)groupAttr.getData();
      return ret;
    }

    public synchronized String getCurrentGroupId() {
        return GenericStateApi.getState(groupAttr.mCurrentGroupId);
    }

    public synchronized List<GroupInfoHelperAttr> getGroupInfo() {
       return GenericStateApi.getState(groupAttr.mGroupInfo);
    }

    public synchronized int getVersion() {
        return GenericStateApi.getState(groupAttr.mVersion);
    }

    public synchronized void setCurrentGroupId(String currentGroupId) {
      groupAttr.mCurrentGroupId = currentGroupId;
    }

    public synchronized void setGroupInfo(List<GroupInfoHelperAttr> groupInfo) {
      groupAttr.mGroupInfo = groupInfo;
    }

    public synchronized void setVersion(int version) {
      groupAttr.mVersion = version;
    }
  }
