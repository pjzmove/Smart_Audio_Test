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

public class PlaylistInsertInAttr implements IResourceAttributes {


    public List<PlayItemAttr>  mPlaylistItems;
    public int mPosition;
    public String mSnapshotId;

     public PlaylistInsertInAttr() {
        mPlaylistItems = new ArrayList<>();
        mPosition = 0;
        mSnapshotId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_insert_in_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();

         for (PlayItemAttr elem : mPlaylistItems) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_playlistItems,repArray);
         }
         rep.setValue(ResourceAttributes.Prop_position,mPosition);
         rep.setValue(ResourceAttributes.Prop_snapshotId,mSnapshotId);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_playlistItems)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_playlistItems);
            if(reps != null) {
                mPlaylistItems.clear();
                for (OcRepresentation elem_rep: reps) {
                    PlayItemAttr obj = new PlayItemAttr();
                    if(obj.unpack(elem_rep))
                        mPlaylistItems.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_position)) {
            didUnpack = true;
            mPosition = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_position);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_snapshotId)) {
            didUnpack = true;
            mSnapshotId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_snapshotId);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlaylistInsertInAttr clonedObj = new PlaylistInsertInAttr();
    for(PlayItemAttr item:mPlaylistItems) {
        clonedObj.mPlaylistItems.add((PlayItemAttr)item.getData());
    }
    clonedObj.mPosition = this.mPosition;
    clonedObj.mSnapshotId = this.mSnapshotId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistInsertInAttr) {
        PlaylistInsertInAttr obj = (PlaylistInsertInAttr) state;
        this.mPlaylistItems.clear();
        for(PlayItemAttr item:obj.mPlaylistItems) {
            this.mPlaylistItems.add((PlayItemAttr)item.getData());
        }
        this.mPosition = obj.mPosition;
        this.mSnapshotId = obj.mSnapshotId;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_playlistItems)) {
            reps = rep.getValue(ResourceAttributes.Prop_playlistItems);
            if(reps != null) {
                if(mPlaylistItems.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mPlaylistItems.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_position)) {
            didChanged = (mPosition != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_position));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_snapshotId)) {
            didChanged = (!mSnapshotId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_snapshotId)));
            if(didChanged) return true;
        }
        return false;
   }
}