/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes;



import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class GroupInfoHelperAttr implements IResourceAttributes {


    public String mGroupId;
    public List<GroupMembersHelperAttr>  mGroupMembersList;
    public String mGroupName;

     public GroupInfoHelperAttr() {
        mGroupId = "";
        mGroupMembersList = new ArrayList<>();
        mGroupName = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_info_helper_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();
         rep.setValue(ResourceAttributes.Prop_groupId,mGroupId);

         for (GroupMembersHelperAttr elem : mGroupMembersList) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_groupMembersList,repArray);
         }
         rep.setValue(ResourceAttributes.Prop_groupName,mGroupName);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_groupId)) {
            didUnpack = true;
            mGroupId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupMembersList)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_groupMembersList);
            if(reps != null) {
                mGroupMembersList.clear();
                for (OcRepresentation elem_rep: reps) {
                    GroupMembersHelperAttr obj = new GroupMembersHelperAttr();
                    if(obj.unpack(elem_rep))
                        mGroupMembersList.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupName)) {
            didUnpack = true;
            mGroupName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupName);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupInfoHelperAttr clonedObj = new GroupInfoHelperAttr();
    clonedObj.mGroupId = this.mGroupId;
    for(GroupMembersHelperAttr item:mGroupMembersList) {
        clonedObj.mGroupMembersList.add((GroupMembersHelperAttr)item.getData());
    }
    clonedObj.mGroupName = this.mGroupName;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupInfoHelperAttr) {
        GroupInfoHelperAttr obj = (GroupInfoHelperAttr) state;
        this.mGroupId = obj.mGroupId;
        this.mGroupMembersList.clear();
        for(GroupMembersHelperAttr item:obj.mGroupMembersList) {
            this.mGroupMembersList.add((GroupMembersHelperAttr)item.getData());
        }
        this.mGroupName = obj.mGroupName;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_groupId)) {
            didChanged = (!mGroupId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupMembersList)) {
            reps = rep.getValue(ResourceAttributes.Prop_groupMembersList);
            if(reps != null) {
                if(mGroupMembersList.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mGroupMembersList.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupName)) {
            didChanged = (!mGroupName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupName)));
            if(didChanged) return true;
        }
        return false;
   }
}