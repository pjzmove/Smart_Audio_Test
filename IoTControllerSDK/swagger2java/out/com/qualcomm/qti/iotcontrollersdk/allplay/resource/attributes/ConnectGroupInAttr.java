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

public class ConnectGroupInAttr implements IResourceAttributes {


    public String mDeviceId;

     public ConnectGroupInAttr() {
        mDeviceId = "";
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/connect_group_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_deviceId,mDeviceId);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didUnpack = true;
            mDeviceId = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    ConnectGroupInAttr clonedObj = new ConnectGroupInAttr();
    clonedObj.mDeviceId = this.mDeviceId;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof ConnectGroupInAttr) {
        ConnectGroupInAttr obj = (ConnectGroupInAttr) state;
        this.mDeviceId = obj.mDeviceId;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceId)) {
            didChanged = (!mDeviceId.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_deviceId)));
            if(didChanged) return true;
        }
        return false;
   }
}