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

public class PlaylistGetHistoryInAttr implements IResourceAttributes {


    public String mFromSnapshotId;

     public PlaylistGetHistoryInAttr() {
        mFromSnapshotId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_get_history_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_fromSnapshotId,mFromSnapshotId);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_fromSnapshotId)) {
            didUnpack = true;
            mFromSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_fromSnapshotId);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistGetHistoryInAttr clonedObj = new PlaylistGetHistoryInAttr();
    clonedObj.mFromSnapshotId = this.mFromSnapshotId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistGetHistoryInAttr) {
        PlaylistGetHistoryInAttr obj = (PlaylistGetHistoryInAttr) state;
        this.mFromSnapshotId = obj.mFromSnapshotId;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_fromSnapshotId)) {
            didChanged = (!mFromSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_fromSnapshotId)));
            if(didChanged) return true;
        }
        return false;
   }
}