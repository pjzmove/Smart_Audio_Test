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

public class CapabilitiesAttr implements IResourceAttributes {


    public List<String>  mCapabilitiesList;

     public CapabilitiesAttr() {
        mCapabilitiesList = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/capabilities_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_capabilitiesList)) {
            didUnpack = true;
            mCapabilitiesList = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_capabilitiesList);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    CapabilitiesAttr clonedObj = new CapabilitiesAttr();
    clonedObj.mCapabilitiesList = this.mCapabilitiesList;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof CapabilitiesAttr) {
        CapabilitiesAttr obj = (CapabilitiesAttr) state;
        this.mCapabilitiesList = obj.mCapabilitiesList;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_capabilitiesList)) {
            didChanged = (!mCapabilitiesList.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_capabilitiesList)));
            if(didChanged) return true;
        }
        return false;
   }
}