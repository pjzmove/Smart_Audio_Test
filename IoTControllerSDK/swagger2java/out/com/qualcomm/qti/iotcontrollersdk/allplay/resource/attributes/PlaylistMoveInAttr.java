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

public class PlaylistMoveInAttr implements IResourceAttributes {


    public int mCount;
    public int mPosition;
    public String mSnapshotId;
    public int mStart;

     public PlaylistMoveInAttr() {
        mCount = 0;
        mPosition = 0;
        mSnapshotId = "";
        mStart = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_move_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_count,mCount);
         rep.setValue(ResourceAttributes.Prop_position,mPosition);
         rep.setValue(ResourceAttributes.Prop_snapshotId,mSnapshotId);
         rep.setValue(ResourceAttributes.Prop_start,mStart);
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
        if (rep.hasAttribute(ResourceAttributes.Prop_position)) {
            didUnpack = true;
            mPosition = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_position);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_snapshotId)) {
            didUnpack = true;
            mSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_snapshotId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_start)) {
            didUnpack = true;
            mStart = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_start);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistMoveInAttr clonedObj = new PlaylistMoveInAttr();
    clonedObj.mCount = this.mCount;
    clonedObj.mPosition = this.mPosition;
    clonedObj.mSnapshotId = this.mSnapshotId;
    clonedObj.mStart = this.mStart;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistMoveInAttr) {
        PlaylistMoveInAttr obj = (PlaylistMoveInAttr) state;
        this.mCount = obj.mCount;
        this.mPosition = obj.mPosition;
        this.mSnapshotId = obj.mSnapshotId;
        this.mStart = obj.mStart;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_position)) {
            didChanged = (mPosition != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_position));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_snapshotId)) {
            didChanged = (!mSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_snapshotId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_start)) {
            didChanged = (mStart != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_start));
            if(didChanged) return true;
        }
        return false;
   }
}