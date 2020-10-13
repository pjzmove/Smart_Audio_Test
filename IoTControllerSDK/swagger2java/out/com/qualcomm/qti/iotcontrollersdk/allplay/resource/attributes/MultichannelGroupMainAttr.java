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

public class MultichannelGroupMainAttr implements IResourceAttributes {


    public List<GroupPlayersAttr>  mGroup;
    public int mVersion;

     public MultichannelGroupMainAttr() {
        mGroup = new ArrayList<>();
        mVersion = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/multichannel_group_main_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_group)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_group);
            if(reps != null) {
                mGroup.clear();
                for (OcRepresentation elem_rep: reps) {
                    GroupPlayersAttr obj = new GroupPlayersAttr();
                    if(obj.unpack(elem_rep))
                        mGroup.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didUnpack = true;
            mVersion = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    MultichannelGroupMainAttr clonedObj = new MultichannelGroupMainAttr();
    for(GroupPlayersAttr item:mGroup) {
        clonedObj.mGroup.add((GroupPlayersAttr)item.getData());
    }
    clonedObj.mVersion = this.mVersion;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof MultichannelGroupMainAttr) {
        MultichannelGroupMainAttr obj = (MultichannelGroupMainAttr) state;
        this.mGroup.clear();
        for(GroupPlayersAttr item:obj.mGroup) {
            this.mGroup.add((GroupPlayersAttr)item.getData());
        }
        this.mVersion = obj.mVersion;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_group)) {
            reps = rep.getValue(ResourceAttributes.Prop_group);
            if(reps != null) {
                if(mGroup.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mGroup.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didChanged = (mVersion != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_version));
            if(didChanged) return true;
        }
        return false;
   }
}