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

public class GroupAttr implements IResourceAttributes {


    public String mCurrentGroupId;
    public List<GroupInfoHelperAttr>  mGroupInfo;
    public int mVersion;

     public GroupAttr() {
        mCurrentGroupId = "";
        mGroupInfo = new ArrayList<>();
        mVersion = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_currentGroupId)) {
            didUnpack = true;
            mCurrentGroupId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_currentGroupId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupInfo)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_groupInfo);
            if(reps != null) {
                mGroupInfo.clear();
                for (OcRepresentation elem_rep: reps) {
                    GroupInfoHelperAttr obj = new GroupInfoHelperAttr();
                    if(obj.unpack(elem_rep))
                        mGroupInfo.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didUnpack = true;
            mVersion = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupAttr clonedObj = new GroupAttr();
    clonedObj.mCurrentGroupId = this.mCurrentGroupId;
    for(GroupInfoHelperAttr item:mGroupInfo) {
        clonedObj.mGroupInfo.add((GroupInfoHelperAttr)item.getData());
    }
    clonedObj.mVersion = this.mVersion;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupAttr) {
        GroupAttr obj = (GroupAttr) state;
        this.mCurrentGroupId = obj.mCurrentGroupId;
        this.mGroupInfo.clear();
        for(GroupInfoHelperAttr item:obj.mGroupInfo) {
            this.mGroupInfo.add((GroupInfoHelperAttr)item.getData());
        }
        this.mVersion = obj.mVersion;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_currentGroupId)) {
            didChanged = (!mCurrentGroupId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_currentGroupId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupInfo)) {
            reps = rep.getValue(ResourceAttributes.Prop_groupInfo);
            if(reps != null) {
                if(mGroupInfo.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mGroupInfo.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didChanged = (mVersion != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version));
            if(didChanged) return true;
        }
        return false;
   }
}