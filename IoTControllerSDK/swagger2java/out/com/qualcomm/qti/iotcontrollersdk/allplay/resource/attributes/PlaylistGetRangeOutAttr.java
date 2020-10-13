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

public class PlaylistGetRangeOutAttr implements IResourceAttributes {


    public List<PlayItemAttr>  mItemsInRange;
    public String mLatestSnapshotId;
    public int mTotalSize;

     public PlaylistGetRangeOutAttr() {
        mItemsInRange = new ArrayList<>();
        mLatestSnapshotId = "";
        mTotalSize = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_get_range_out_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();

         for (PlayItemAttr elem : mItemsInRange) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_itemsInRange,repArray);
         }
         rep.setValue(ResourceAttributes.Prop_latestSnapshotId,mLatestSnapshotId);
         rep.setValue(ResourceAttributes.Prop_totalSize,mTotalSize);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_itemsInRange)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_itemsInRange);
            if(reps != null) {
                mItemsInRange.clear();
                for (OcRepresentation elem_rep: reps) {
                    PlayItemAttr obj = new PlayItemAttr();
                    if(obj.unpack(elem_rep))
                        mItemsInRange.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_latestSnapshotId)) {
            didUnpack = true;
            mLatestSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_latestSnapshotId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_totalSize)) {
            didUnpack = true;
            mTotalSize = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_totalSize);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistGetRangeOutAttr clonedObj = new PlaylistGetRangeOutAttr();
    for(PlayItemAttr item:mItemsInRange) {
        clonedObj.mItemsInRange.add((PlayItemAttr)item.getData());
    }
    clonedObj.mLatestSnapshotId = this.mLatestSnapshotId;
    clonedObj.mTotalSize = this.mTotalSize;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistGetRangeOutAttr) {
        PlaylistGetRangeOutAttr obj = (PlaylistGetRangeOutAttr) state;
        this.mItemsInRange.clear();
        for(PlayItemAttr item:obj.mItemsInRange) {
            this.mItemsInRange.add((PlayItemAttr)item.getData());
        }
        this.mLatestSnapshotId = obj.mLatestSnapshotId;
        this.mTotalSize = obj.mTotalSize;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_itemsInRange)) {
            reps = rep.getValue(ResourceAttributes.Prop_itemsInRange);
            if(reps != null) {
                if(mItemsInRange.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mItemsInRange.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_latestSnapshotId)) {
            didChanged = (!mLatestSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_latestSnapshotId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_totalSize)) {
            didChanged = (mTotalSize != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_totalSize));
            if(didChanged) return true;
        }
        return false;
   }
}