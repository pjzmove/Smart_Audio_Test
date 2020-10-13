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

public class OutputSelectorAttr implements IResourceAttributes {


    public List<InputOutputInfoAttr>  mActiveOutputs;
    public List<InputOutputInfoAttr>  mOutputList;
    public int mVersion;

     public OutputSelectorAttr() {
        mActiveOutputs = new ArrayList<>();
        mOutputList = new ArrayList<>();
        mVersion = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/output_selector_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();

         for (InputOutputInfoAttr elem : mActiveOutputs) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_activeOutputs,repArray);
         }
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_activeOutputs)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_activeOutputs);
            if(reps != null) {
                mActiveOutputs.clear();
                for (OcRepresentation elem_rep: reps) {
                    InputOutputInfoAttr obj = new InputOutputInfoAttr();
                    if(obj.unpack(elem_rep))
                        mActiveOutputs.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_outputList)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_outputList);
            if(reps != null) {
                mOutputList.clear();
                for (OcRepresentation elem_rep: reps) {
                    InputOutputInfoAttr obj = new InputOutputInfoAttr();
                    if(obj.unpack(elem_rep))
                        mOutputList.add(obj);
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
    OutputSelectorAttr clonedObj = new OutputSelectorAttr();
    for(InputOutputInfoAttr item:mActiveOutputs) {
        clonedObj.mActiveOutputs.add((InputOutputInfoAttr)item.getData());
    }
    for(InputOutputInfoAttr item:mOutputList) {
        clonedObj.mOutputList.add((InputOutputInfoAttr)item.getData());
    }
    clonedObj.mVersion = this.mVersion;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof OutputSelectorAttr) {
        OutputSelectorAttr obj = (OutputSelectorAttr) state;
        this.mActiveOutputs.clear();
        for(InputOutputInfoAttr item:obj.mActiveOutputs) {
            this.mActiveOutputs.add((InputOutputInfoAttr)item.getData());
        }
        this.mOutputList.clear();
        for(InputOutputInfoAttr item:obj.mOutputList) {
            this.mOutputList.add((InputOutputInfoAttr)item.getData());
        }
        this.mVersion = obj.mVersion;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_activeOutputs)) {
            reps = rep.getValue(ResourceAttributes.Prop_activeOutputs);
            if(reps != null) {
                if(mActiveOutputs.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mActiveOutputs.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_outputList)) {
            reps = rep.getValue(ResourceAttributes.Prop_outputList);
            if(reps != null) {
                if(mOutputList.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mOutputList.get(i).checkDifference(reps[i]))
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