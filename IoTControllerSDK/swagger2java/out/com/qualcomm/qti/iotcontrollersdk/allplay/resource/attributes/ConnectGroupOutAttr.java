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

public class ConnectGroupOutAttr implements IResourceAttributes {


    public boolean mMute;
    public String mTimeServerIp;
    public int mTimeServerPort;
    public int mVolume;

     public ConnectGroupOutAttr() {
        mMute = false;
        mTimeServerIp = "";
        mTimeServerPort = 0;
        mVolume = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/connect_group_out_rep_uri");

         rep.setValue(ResourceAttributes.Prop_mute,mMute);
         rep.setValue(ResourceAttributes.Prop_timeServerIp,mTimeServerIp);
         rep.setValue(ResourceAttributes.Prop_timeServerPort,mTimeServerPort);
         rep.setValue(ResourceAttributes.Prop_volume,mVolume);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
            didUnpack = true;
            mMute = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_mute);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeServerIp)) {
            didUnpack = true;
            mTimeServerIp = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_timeServerIp);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeServerPort)) {
            didUnpack = true;
            mTimeServerPort = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeServerPort);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didUnpack = true;
            mVolume = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_volume);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    ConnectGroupOutAttr clonedObj = new ConnectGroupOutAttr();
    clonedObj.mMute = this.mMute;
    clonedObj.mTimeServerIp = this.mTimeServerIp;
    clonedObj.mTimeServerPort = this.mTimeServerPort;
    clonedObj.mVolume = this.mVolume;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof ConnectGroupOutAttr) {
        ConnectGroupOutAttr obj = (ConnectGroupOutAttr) state;
        this.mMute = obj.mMute;
        this.mTimeServerIp = obj.mTimeServerIp;
        this.mTimeServerPort = obj.mTimeServerPort;
        this.mVolume = obj.mVolume;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
            didChanged = (mMute != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_mute));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeServerIp)) {
            didChanged = (!mTimeServerIp.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_timeServerIp)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_timeServerPort)) {
            didChanged = (mTimeServerPort != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_timeServerPort));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
            didChanged = (mVolume != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_volume));
            if(didChanged) return true;
        }
        return false;
   }
}