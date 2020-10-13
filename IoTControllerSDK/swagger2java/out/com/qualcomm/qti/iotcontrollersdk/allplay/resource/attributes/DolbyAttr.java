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

public class DolbyAttr implements IResourceAttributes {


    public boolean mEnabled;
    public int mMode;

     public DolbyAttr() {
        mEnabled = false;
        mMode = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/dolby_rep_uri");

         rep.setValue(ResourceAttributes.Prop_enabled,mEnabled);
         rep.setValue(ResourceAttributes.Prop_mode,mMode);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didUnpack = true;
            mEnabled = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mode)) {
            didUnpack = true;
            mMode = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_mode);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    DolbyAttr clonedObj = new DolbyAttr();
    clonedObj.mEnabled = this.mEnabled;
    clonedObj.mMode = this.mMode;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof DolbyAttr) {
        DolbyAttr obj = (DolbyAttr) state;
        this.mEnabled = obj.mEnabled;
        this.mMode = obj.mMode;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_enabled)) {
            didChanged = (mEnabled != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_enabled));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_mode)) {
            didChanged = (mMode != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_mode));
            if(didChanged) return true;
        }
        return false;
   }
}