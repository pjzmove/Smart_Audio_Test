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

public class GroupDeleteInAttr implements IResourceAttributes {


    public String mGroupId;

     public GroupDeleteInAttr() {
        mGroupId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_delete_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_groupId,mGroupId);
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
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupDeleteInAttr clonedObj = new GroupDeleteInAttr();
    clonedObj.mGroupId = this.mGroupId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupDeleteInAttr) {
        GroupDeleteInAttr obj = (GroupDeleteInAttr) state;
        this.mGroupId = obj.mGroupId;
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
        return false;
   }
}