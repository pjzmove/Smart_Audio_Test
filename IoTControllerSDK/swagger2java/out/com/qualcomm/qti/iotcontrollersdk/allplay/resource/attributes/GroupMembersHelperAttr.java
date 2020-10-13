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

public class GroupMembersHelperAttr implements IResourceAttributes {


    public String mMemberId;
    public String mMemberName;

     public GroupMembersHelperAttr() {
        mMemberId = "";
        mMemberName = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_members_helper_rep_uri");

         rep.setValue(ResourceAttributes.Prop_memberId,mMemberId);
         rep.setValue(ResourceAttributes.Prop_memberName,mMemberName);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_memberId)) {
            didUnpack = true;
            mMemberId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_memberId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_memberName)) {
            didUnpack = true;
            mMemberName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_memberName);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupMembersHelperAttr clonedObj = new GroupMembersHelperAttr();
    clonedObj.mMemberId = this.mMemberId;
    clonedObj.mMemberName = this.mMemberName;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupMembersHelperAttr) {
        GroupMembersHelperAttr obj = (GroupMembersHelperAttr) state;
        this.mMemberId = obj.mMemberId;
        this.mMemberName = obj.mMemberName;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_memberId)) {
            didChanged = (!mMemberId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_memberId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_memberName)) {
            didChanged = (!mMemberName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_memberName)));
            if(didChanged) return true;
        }
        return false;
   }
}