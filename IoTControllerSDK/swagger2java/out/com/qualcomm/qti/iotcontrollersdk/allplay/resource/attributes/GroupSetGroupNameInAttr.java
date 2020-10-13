/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes;



import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class GroupSetGroupNameInAttr implements IResourceAttributes {


    public String mGroupId;
    public String mGroupName;

     public GroupSetGroupNameInAttr() {
        mGroupId = "";
        mGroupName = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_set_group_name_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_groupId,mGroupId);
         rep.setValue(ResourceAttributes.Prop_groupName,mGroupName);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_groupId)) {
            didUnpack = true;
            mGroupId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupName)) {
            didUnpack = true;
            mGroupName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupName);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupSetGroupNameInAttr clonedObj = new GroupSetGroupNameInAttr();
    clonedObj.mGroupId = this.mGroupId;
    clonedObj.mGroupName = this.mGroupName;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupSetGroupNameInAttr) {
        GroupSetGroupNameInAttr obj = (GroupSetGroupNameInAttr) state;
        this.mGroupId = obj.mGroupId;
        this.mGroupName = obj.mGroupName;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_groupId)) {
            didChanged = (!mGroupId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_groupName)) {
            didChanged = (!mGroupName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupName)));
            if(didChanged) return true;
        }
        return false;
   }
}