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

public class GroupCreateInAttr implements IResourceAttributes {


    public String mGroupName;

     public GroupCreateInAttr() {
        mGroupName = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_create_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_groupName,mGroupName);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_groupName)) {
            didUnpack = true;
            mGroupName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupName);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupCreateInAttr clonedObj = new GroupCreateInAttr();
    clonedObj.mGroupName = this.mGroupName;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupCreateInAttr) {
        GroupCreateInAttr obj = (GroupCreateInAttr) state;
        this.mGroupName = obj.mGroupName;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_groupName)) {
            didChanged = (!mGroupName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_groupName)));
            if(didChanged) return true;
        }
        return false;
   }
}