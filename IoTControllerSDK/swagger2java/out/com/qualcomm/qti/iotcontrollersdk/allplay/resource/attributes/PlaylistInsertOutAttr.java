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

public class PlaylistInsertOutAttr implements IResourceAttributes {


    public int mCount;
    public String mNewSnapshotId;
    public boolean mTruncated;

     public PlaylistInsertOutAttr() {
        mCount = 0;
        mNewSnapshotId = "";
        mTruncated = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_insert_out_rep_uri");

         rep.setValue(ResourceAttributes.Prop_count,mCount);
         rep.setValue(ResourceAttributes.Prop_newSnapshotId,mNewSnapshotId);
         rep.setValue(ResourceAttributes.Prop_truncated,mTruncated);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_count)) {
            didUnpack = true;
            mCount = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_count);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_newSnapshotId)) {
            didUnpack = true;
            mNewSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_newSnapshotId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_truncated)) {
            didUnpack = true;
            mTruncated = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_truncated);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistInsertOutAttr clonedObj = new PlaylistInsertOutAttr();
    clonedObj.mCount = this.mCount;
    clonedObj.mNewSnapshotId = this.mNewSnapshotId;
    clonedObj.mTruncated = this.mTruncated;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistInsertOutAttr) {
        PlaylistInsertOutAttr obj = (PlaylistInsertOutAttr) state;
        this.mCount = obj.mCount;
        this.mNewSnapshotId = obj.mNewSnapshotId;
        this.mTruncated = obj.mTruncated;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_count)) {
            didChanged = (mCount != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_count));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_newSnapshotId)) {
            didChanged = (!mNewSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_newSnapshotId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_truncated)) {
            didChanged = (mTruncated != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_truncated));
            if(didChanged) return true;
        }
        return false;
   }
}