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

public class RemoveDevicesInAttr implements IResourceAttributes {


    public List<String>  mDeviceIdList;

     public RemoveDevicesInAttr() {
        mDeviceIdList = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/remove_devices_in_rep_uri");

         rep.setValue(ResourceAttributes.Prop_deviceIdList,ResourceAttrUtils.streamFromStringArray(mDeviceIdList));
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceIdList)) {
            didUnpack = true;
            mDeviceIdList = ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_deviceIdList);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    RemoveDevicesInAttr clonedObj = new RemoveDevicesInAttr();
    clonedObj.mDeviceIdList = this.mDeviceIdList;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof RemoveDevicesInAttr) {
        RemoveDevicesInAttr obj = (RemoveDevicesInAttr) state;
        this.mDeviceIdList = obj.mDeviceIdList;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_deviceIdList)) {
            didChanged = (!mDeviceIdList.equals(ResourceAttrUtils.stringArrayFromStream(rep, ResourceAttributes.Prop_deviceIdList)));
            if(didChanged) return true;
        }
        return false;
   }
}