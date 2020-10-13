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

public class InputSelectorAttr implements IResourceAttributes {


    public InputOutputInfoAttr mActiveInput;
    public List<InputOutputInfoAttr>  mInputList;
    public int mVersion;

     public InputSelectorAttr() {
        mActiveInput = new InputOutputInfoAttr();
        mInputList = new ArrayList<>();
        mVersion = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/input_selector_rep_uri");

         rep.setValue(ResourceAttributes.Prop_activeInput,mActiveInput.pack());
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_activeInput)) {
            didUnpack = true;
            InputOutputInfoAttr obj = new InputOutputInfoAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_activeInput)))
                mActiveInput = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_inputList)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_inputList);
            if(reps != null) {
                mInputList.clear();
                for (OcRepresentation elem_rep: reps) {
                    InputOutputInfoAttr obj = new InputOutputInfoAttr();
                    if(obj.unpack(elem_rep))
                        mInputList.add(obj);
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
    InputSelectorAttr clonedObj = new InputSelectorAttr();
    clonedObj.mActiveInput = (InputOutputInfoAttr)mActiveInput.getData();
    for(InputOutputInfoAttr item:mInputList) {
        clonedObj.mInputList.add((InputOutputInfoAttr)item.getData());
    }
    clonedObj.mVersion = this.mVersion;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof InputSelectorAttr) {
        InputSelectorAttr obj = (InputSelectorAttr) state;
        this.mActiveInput.setData(obj.mActiveInput);
        this.mInputList.clear();
        for(InputOutputInfoAttr item:obj.mInputList) {
            this.mInputList.add((InputOutputInfoAttr)item.getData());
        }
        this.mVersion = obj.mVersion;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_activeInput)) {
            didChanged = (mActiveInput.checkDifference(rep.getValue(ResourceAttributes.Prop_activeInput)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_inputList)) {
            reps = rep.getValue(ResourceAttributes.Prop_inputList);
            if(reps != null) {
                if(mInputList.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mInputList.get(i).checkDifference(reps[i]))
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