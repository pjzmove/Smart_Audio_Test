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

public class GroupVolumeSyncInAttr implements IResourceAttributes {


    public String mDeviceId;
    public List<VersionVectorHelperAttr>  mVVector;
    public List<VolumeInfoHelperAttr>  mVolumeInfo;

     public GroupVolumeSyncInAttr() {
        mDeviceId = "";
        mVVector = new ArrayList<>();
        mVolumeInfo = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/group_volume_sync_in_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();
         rep.setValue(ResourceAttributes.Prop_deviceId,mDeviceId);

         for (VersionVectorHelperAttr elem : mVVector) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_vVector,repArray);
         }

         for (VolumeInfoHelperAttr elem : mVolumeInfo) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_volumeInfo,repArray);
         }
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didUnpack = true;
            mDeviceId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_vVector)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_vVector);
            if(reps != null) {
                mVVector.clear();
                for (OcRepresentation elem_rep: reps) {
                    VersionVectorHelperAttr obj = new VersionVectorHelperAttr();
                    if(obj.unpack(elem_rep))
                        mVVector.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volumeInfo)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_volumeInfo);
            if(reps != null) {
                mVolumeInfo.clear();
                for (OcRepresentation elem_rep: reps) {
                    VolumeInfoHelperAttr obj = new VolumeInfoHelperAttr();
                    if(obj.unpack(elem_rep))
                        mVolumeInfo.add(obj);
                }
            }
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    GroupVolumeSyncInAttr clonedObj = new GroupVolumeSyncInAttr();
    clonedObj.mDeviceId = this.mDeviceId;
    for(VersionVectorHelperAttr item:mVVector) {
        clonedObj.mVVector.add((VersionVectorHelperAttr)item.getData());
    }
    for(VolumeInfoHelperAttr item:mVolumeInfo) {
        clonedObj.mVolumeInfo.add((VolumeInfoHelperAttr)item.getData());
    }
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof GroupVolumeSyncInAttr) {
        GroupVolumeSyncInAttr obj = (GroupVolumeSyncInAttr) state;
        this.mDeviceId = obj.mDeviceId;
        this.mVVector.clear();
        for(VersionVectorHelperAttr item:obj.mVVector) {
            this.mVVector.add((VersionVectorHelperAttr)item.getData());
        }
        this.mVolumeInfo.clear();
        for(VolumeInfoHelperAttr item:obj.mVolumeInfo) {
            this.mVolumeInfo.add((VolumeInfoHelperAttr)item.getData());
        }
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didChanged = (!mDeviceId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_vVector)) {
            reps = rep.getValue(ResourceAttributes.Prop_vVector);
            if(reps != null) {
                if(mVVector.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mVVector.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volumeInfo)) {
            reps = rep.getValue(ResourceAttributes.Prop_volumeInfo);
            if(reps != null) {
                if(mVolumeInfo.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mVolumeInfo.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        return false;
   }
}