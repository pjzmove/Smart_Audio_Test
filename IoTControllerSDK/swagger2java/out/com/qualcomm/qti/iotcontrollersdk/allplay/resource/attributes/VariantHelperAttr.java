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

public class VariantHelperAttr implements IResourceAttributes {


    public String mKey;
    public String mValue;

     public VariantHelperAttr() {
        mKey = "";
        mValue = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/variant_helper_rep_uri");

         rep.setValue(ResourceAttributes.Prop_key,mKey);
         rep.setValue(ResourceAttributes.Prop_value,mValue);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_key)) {
            didUnpack = true;
            mKey = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_key);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_value)) {
            didUnpack = true;
            mValue = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_value);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    VariantHelperAttr clonedObj = new VariantHelperAttr();
    clonedObj.mKey = this.mKey;
    clonedObj.mValue = this.mValue;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof VariantHelperAttr) {
        VariantHelperAttr obj = (VariantHelperAttr) state;
        this.mKey = obj.mKey;
        this.mValue = obj.mValue;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_key)) {
            didChanged = (!mKey.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_key)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_value)) {
            didChanged = (!mValue.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_value)));
            if(didChanged) return true;
        }
        return false;
   }
}