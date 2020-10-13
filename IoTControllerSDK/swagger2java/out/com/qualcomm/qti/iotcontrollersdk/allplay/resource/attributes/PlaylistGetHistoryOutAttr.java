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

public class PlaylistGetHistoryOutAttr implements IResourceAttributes {


    public List<PlaylistHistoryPointAttr>  mHistoryItems;
    public String mLatestSnapshotId;

     public PlaylistGetHistoryOutAttr() {
        mHistoryItems = new ArrayList<>();
        mLatestSnapshotId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_get_history_out_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();

         for (PlaylistHistoryPointAttr elem : mHistoryItems) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_historyItems,repArray);
         }
         rep.setValue(ResourceAttributes.Prop_latestSnapshotId,mLatestSnapshotId);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_historyItems)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_historyItems);
            if(reps != null) {
                mHistoryItems.clear();
                for (OcRepresentation elem_rep: reps) {
                    PlaylistHistoryPointAttr obj = new PlaylistHistoryPointAttr();
                    if(obj.unpack(elem_rep))
                        mHistoryItems.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_latestSnapshotId)) {
            didUnpack = true;
            mLatestSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_latestSnapshotId);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistGetHistoryOutAttr clonedObj = new PlaylistGetHistoryOutAttr();
    for(PlaylistHistoryPointAttr item:mHistoryItems) {
        clonedObj.mHistoryItems.add((PlaylistHistoryPointAttr)item.getData());
    }
    clonedObj.mLatestSnapshotId = this.mLatestSnapshotId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistGetHistoryOutAttr) {
        PlaylistGetHistoryOutAttr obj = (PlaylistGetHistoryOutAttr) state;
        this.mHistoryItems.clear();
        for(PlaylistHistoryPointAttr item:obj.mHistoryItems) {
            this.mHistoryItems.add((PlaylistHistoryPointAttr)item.getData());
        }
        this.mLatestSnapshotId = obj.mLatestSnapshotId;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_historyItems)) {
            reps = rep.getValue(ResourceAttributes.Prop_historyItems);
            if(reps != null) {
                if(mHistoryItems.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mHistoryItems.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_latestSnapshotId)) {
            didChanged = (!mLatestSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_latestSnapshotId)));
            if(didChanged) return true;
        }
        return false;
   }
}