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

public class InputOutputInfoAttr implements IResourceAttributes {


    public String mFriendlyName;
    public String mId;

     public InputOutputInfoAttr() {
        mFriendlyName = "";
        mId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/input_output_info_rep_uri");

         rep.setValue(ResourceAttributes.Prop_friendlyName,mFriendlyName);
         rep.setValue(ResourceAttributes.Prop_id,mId);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_friendlyName)) {
            didUnpack = true;
            mFriendlyName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_friendlyName);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_id)) {
            didUnpack = true;
            mId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_id);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    InputOutputInfoAttr clonedObj = new InputOutputInfoAttr();
    clonedObj.mFriendlyName = this.mFriendlyName;
    clonedObj.mId = this.mId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof InputOutputInfoAttr) {
        InputOutputInfoAttr obj = (InputOutputInfoAttr) state;
        this.mFriendlyName = obj.mFriendlyName;
        this.mId = obj.mId;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_friendlyName)) {
            didChanged = (!mFriendlyName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_friendlyName)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_id)) {
            didChanged = (!mId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_id)));
            if(didChanged) return true;
        }
        return false;
   }
}