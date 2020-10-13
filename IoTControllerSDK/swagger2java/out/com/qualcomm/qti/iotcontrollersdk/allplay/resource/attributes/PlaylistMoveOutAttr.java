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

public class PlaylistMoveOutAttr implements IResourceAttributes {


    public String mNewSnapshotId;

     public PlaylistMoveOutAttr() {
        mNewSnapshotId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_move_out_rep_uri");

         rep.setValue(ResourceAttributes.Prop_newSnapshotId,mNewSnapshotId);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_newSnapshotId)) {
            didUnpack = true;
            mNewSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_newSnapshotId);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistMoveOutAttr clonedObj = new PlaylistMoveOutAttr();
    clonedObj.mNewSnapshotId = this.mNewSnapshotId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistMoveOutAttr) {
        PlaylistMoveOutAttr obj = (PlaylistMoveOutAttr) state;
        this.mNewSnapshotId = obj.mNewSnapshotId;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_newSnapshotId)) {
            didChanged = (!mNewSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_newSnapshotId)));
            if(didChanged) return true;
        }
        return false;
   }
}