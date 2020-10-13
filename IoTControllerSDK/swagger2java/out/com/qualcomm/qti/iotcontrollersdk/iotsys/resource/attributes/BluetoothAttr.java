/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcException;
import com.qualcomm.qti.iotcontrollersdk.controller.ResourceAttrUtils;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IResourceAttributes;

public class BluetoothAttr implements IResourceAttributes {

    public enum AdapterState {
        kEnabled,
        kDisabled,
        kUnknown,
    }

    public enum Discoverable {
        kDiscoverable,
        kNonDiscoverable,
        kUnknown,
    }


    public AdapterState mAdapterState;
    public BtDeviceStatusAttr mConnectedState;
    public Discoverable mDiscoverable;
    public BtErrorAttr mError;
    public List<BtDeviceAttr>  mPairedDevices;
    public BtDeviceStatusAttr mPairedState;
    public BtDeviceAttr mScanResult;
    public BtSignalAttr mSignal;

     public BluetoothAttr() {
        mAdapterState = AdapterState.kEnabled;
        mConnectedState = new BtDeviceStatusAttr();
        mDiscoverable = Discoverable.kDiscoverable;
        mError = new BtErrorAttr();
        mPairedDevices = new ArrayList<>();
        mPairedState = new BtDeviceStatusAttr();
        mScanResult = new BtDeviceAttr();
        mSignal = new BtSignalAttr();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/bluetooth_rep_uri");

         rep.setValue(ResourceAttributes.Prop_scanResult,mScanResult.pack());
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_adapterState)) {
            didUnpack = true;
            mAdapterState = adapterStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_adapterState));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_connectedState)) {
            didUnpack = true;
            BtDeviceStatusAttr obj = new BtDeviceStatusAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_connectedState)))
                mConnectedState = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_discoverable)) {
            didUnpack = true;
            mDiscoverable = discoverableFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_discoverable));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
            didUnpack = true;
            BtErrorAttr obj = new BtErrorAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_error)))
                mError = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_pairedDevices)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_pairedDevices);
            if(reps != null) {
                mPairedDevices.clear();
                for (OcRepresentation elem_rep: reps) {
                    BtDeviceAttr obj = new BtDeviceAttr();
                    if(obj.unpack(elem_rep))
                        mPairedDevices.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_pairedState)) {
            didUnpack = true;
            BtDeviceStatusAttr obj = new BtDeviceStatusAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_pairedState)))
                mPairedState = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_scanResult)) {
            didUnpack = true;
            BtDeviceAttr obj = new BtDeviceAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_scanResult)))
                mScanResult = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_signal)) {
            didUnpack = true;
            BtSignalAttr obj = new BtSignalAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_signal)))
                mSignal = obj;
        }
        return didUnpack;
    }

    public AdapterState adapterStateFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("enabled")) {
                return AdapterState.kEnabled;
            }
            if (value.equalsIgnoreCase("disabled")) {
                return AdapterState.kDisabled;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return AdapterState.kUnknown;
            }
        }
        return AdapterState.kEnabled;
    }

    public String adapterStateToString(AdapterState value) {
        switch(value) {
            case kEnabled:
                return "enabled";
            case kDisabled:
                return "disabled";
            case kUnknown:
                return "unknown";
        }

        return "enabled";
    }

    public Discoverable discoverableFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("discoverable")) {
                return Discoverable.kDiscoverable;
            }
            if (value.equalsIgnoreCase("non-discoverable")) {
                return Discoverable.kNonDiscoverable;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return Discoverable.kUnknown;
            }
        }
        return Discoverable.kDiscoverable;
    }

    public String discoverableToString(Discoverable value) {
        switch(value) {
            case kDiscoverable:
                return "discoverable";
            case kNonDiscoverable:
                return "non-discoverable";
            case kUnknown:
                return "unknown";
        }

        return "discoverable";
    }


   @Override
   public Object getData() {
    BluetoothAttr clonedObj = new BluetoothAttr();
    clonedObj.mAdapterState = this.mAdapterState;
    clonedObj.mConnectedState = (BtDeviceStatusAttr)mConnectedState.getData();
    clonedObj.mDiscoverable = this.mDiscoverable;
    clonedObj.mError = (BtErrorAttr)mError.getData();
    for(BtDeviceAttr item:mPairedDevices) {
        clonedObj.mPairedDevices.add((BtDeviceAttr)item.getData());
    }
    clonedObj.mPairedState = (BtDeviceStatusAttr)mPairedState.getData();
    clonedObj.mScanResult = (BtDeviceAttr)mScanResult.getData();
    clonedObj.mSignal = (BtSignalAttr)mSignal.getData();
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof BluetoothAttr) {
        BluetoothAttr obj = (BluetoothAttr) state;
        this.mAdapterState = obj.mAdapterState;
        this.mConnectedState.setData(obj.mConnectedState);
        this.mDiscoverable = obj.mDiscoverable;
        this.mError.setData(obj.mError);
        this.mPairedDevices.clear();
        for(BtDeviceAttr item:obj.mPairedDevices) {
            this.mPairedDevices.add((BtDeviceAttr)item.getData());
        }
        this.mPairedState.setData(obj.mPairedState);
        this.mScanResult.setData(obj.mScanResult);
        this.mSignal.setData(obj.mSignal);
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_adapterState)) {
            didChanged = (mAdapterState != adapterStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_adapterState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_connectedState)) {
            didChanged = (mConnectedState.checkDifference(rep.getValue(ResourceAttributes.Prop_connectedState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_discoverable)) {
            didChanged = (mDiscoverable != discoverableFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_discoverable)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
            didChanged = (mError.checkDifference(rep.getValue(ResourceAttributes.Prop_error)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_pairedDevices)) {
            reps = rep.getValue(ResourceAttributes.Prop_pairedDevices);
            if(reps != null) {
                if(mPairedDevices.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mPairedDevices.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_pairedState)) {
            didChanged = (mPairedState.checkDifference(rep.getValue(ResourceAttributes.Prop_pairedState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_scanResult)) {
            didChanged = (mScanResult.checkDifference(rep.getValue(ResourceAttributes.Prop_scanResult)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_signal)) {
            didChanged = (mSignal.checkDifference(rep.getValue(ResourceAttributes.Prop_signal)));
            if(didChanged) return true;
        }
        return false;
   }
}