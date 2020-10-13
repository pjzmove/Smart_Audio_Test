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

public class PlaylistAttr implements IResourceAttributes {


    public String mOwnerInfo;
    public String mSnapshotId;
    public String mUserData;
    public int mVersion;

     public PlaylistAttr() {
        mOwnerInfo = "";
        mSnapshotId = "";
        mUserData = "";
        mVersion = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_rep_uri");

         rep.setValue(ResourceAttributes.Prop_ownerInfo,mOwnerInfo);
         rep.setValue(ResourceAttributes.Prop_userData,mUserData);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_ownerInfo)) {
            didUnpack = true;
            mOwnerInfo = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_ownerInfo);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_snapshotId)) {
            didUnpack = true;
            mSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_snapshotId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_userData)) {
            didUnpack = true;
            mUserData = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_userData);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didUnpack = true;
            mVersion = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistAttr clonedObj = new PlaylistAttr();
    clonedObj.mOwnerInfo = this.mOwnerInfo;
    clonedObj.mSnapshotId = this.mSnapshotId;
    clonedObj.mUserData = this.mUserData;
    clonedObj.mVersion = this.mVersion;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistAttr) {
        PlaylistAttr obj = (PlaylistAttr) state;
        this.mOwnerInfo = obj.mOwnerInfo;
        this.mSnapshotId = obj.mSnapshotId;
        this.mUserData = obj.mUserData;
        this.mVersion = obj.mVersion;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_ownerInfo)) {
            didChanged = (!mOwnerInfo.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_ownerInfo)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_snapshotId)) {
            didChanged = (!mSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_snapshotId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_userData)) {
            didChanged = (!mUserData.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_userData)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didChanged = (mVersion != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version));
            if(didChanged) return true;
        }
        return false;
   }
}