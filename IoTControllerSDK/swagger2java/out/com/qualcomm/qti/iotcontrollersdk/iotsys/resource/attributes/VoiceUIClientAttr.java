/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes;


import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class VoiceUIClientAttr implements IResourceAttributes {


    public String mName;
    public String mVersion;
    public boolean mWakewordStatus;

     public VoiceUIClientAttr() {
        mName = "";
        mVersion = "";
        mWakewordStatus = false;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/voice_ui_client_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_name)) {
            didUnpack = true;
            mName = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_name);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didUnpack = true;
            mVersion = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_version);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_wakewordStatus)) {
            didUnpack = true;
            mWakewordStatus = ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_wakewordStatus);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    VoiceUIClientAttr clonedObj = new VoiceUIClientAttr();
    clonedObj.mName = this.mName;
    clonedObj.mVersion = this.mVersion;
    clonedObj.mWakewordStatus = this.mWakewordStatus;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof VoiceUIClientAttr) {
        VoiceUIClientAttr obj = (VoiceUIClientAttr) state;
        this.mName = obj.mName;
        this.mVersion = obj.mVersion;
        this.mWakewordStatus = obj.mWakewordStatus;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_name)) {
            didChanged = (!mName.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_name)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_version)) {
            didChanged = (!mVersion.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_version)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_wakewordStatus)) {
            didChanged = (mWakewordStatus != ResourceAttrUtils.boolValueFromRepresentation(rep, ResourceAttributes.Prop_wakewordStatus));
            if(didChanged) return true;
        }
        return false;
   }
}