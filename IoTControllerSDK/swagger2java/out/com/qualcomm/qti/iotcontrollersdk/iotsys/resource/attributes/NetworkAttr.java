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

public class NetworkAttr implements IResourceAttributes {


    public AccessPointAttr mAccessPoint;
    public NetworkInterfaceAttr mEthernetAdapter;
    public NetworkInterfaceAttr mWifiAdapter;

     public NetworkAttr() {
        mAccessPoint = new AccessPointAttr();
        mEthernetAdapter = new NetworkInterfaceAttr();
        mWifiAdapter = new NetworkInterfaceAttr();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/network_rep_uri");

         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_accessPoint)) {
            didUnpack = true;
            AccessPointAttr obj = new AccessPointAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_accessPoint)))
                mAccessPoint = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_ethernetAdapter)) {
            didUnpack = true;
            NetworkInterfaceAttr obj = new NetworkInterfaceAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_ethernetAdapter)))
                mEthernetAdapter = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_wifiAdapter)) {
            didUnpack = true;
            NetworkInterfaceAttr obj = new NetworkInterfaceAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_wifiAdapter)))
                mWifiAdapter = obj;
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    NetworkAttr clonedObj = new NetworkAttr();
    clonedObj.mAccessPoint = (AccessPointAttr)mAccessPoint.getData();
    clonedObj.mEthernetAdapter = (NetworkInterfaceAttr)mEthernetAdapter.getData();
    clonedObj.mWifiAdapter = (NetworkInterfaceAttr)mWifiAdapter.getData();
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof NetworkAttr) {
        NetworkAttr obj = (NetworkAttr) state;
        this.mAccessPoint.setData(obj.mAccessPoint);
        this.mEthernetAdapter.setData(obj.mEthernetAdapter);
        this.mWifiAdapter.setData(obj.mWifiAdapter);
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_accessPoint)) {
            didChanged = (mAccessPoint.checkDifference(rep.getValue(ResourceAttributes.Prop_accessPoint)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_ethernetAdapter)) {
            didChanged = (mEthernetAdapter.checkDifference(rep.getValue(ResourceAttributes.Prop_ethernetAdapter)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_wifiAdapter)) {
            didChanged = (mWifiAdapter.checkDifference(rep.getValue(ResourceAttributes.Prop_wifiAdapter)));
            if(didChanged) return true;
        }
        return false;
   }
}