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

public class ZigbeeAttr implements IResourceAttributes {

    public enum AdapterState {
        kEnabled,
        kDisabled,
        kUnknown,
    }

    public enum CoordinatorState {
        kNominated,
        kNotNominated,
        kUnknown,
    }

    public enum JoiningState {
        kAllowed,
        kDisallowed,
        kUnknown,
    }


    public AdapterState mAdapterState;
    public CoordinatorState mCoordinatorState;
    public String mError;
    public List<ZbDeviceAttr>  mJoinedDevices;
    public JoiningState mJoiningState;
    public ZbSignalAttr mSignal;

     public ZigbeeAttr() {
        mAdapterState = AdapterState.kEnabled;
        mCoordinatorState = CoordinatorState.kNominated;
        mError = "";
        mJoinedDevices = new ArrayList<>();
        mJoiningState = JoiningState.kAllowed;
        mSignal = new ZbSignalAttr();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/zigbee_rep_uri");

         rep.setValue(ResourceAttributes.Prop_error,mError);
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
        if (rep.hasAttribute(ResourceAttributes.Prop_coordinatorState)) {
            didUnpack = true;
            mCoordinatorState = coordinatorStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_coordinatorState));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
            didUnpack = true;
            mError = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_error);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_joinedDevices)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_joinedDevices);
            if(reps != null) {
                mJoinedDevices.clear();
                for (OcRepresentation elem_rep: reps) {
                    ZbDeviceAttr obj = new ZbDeviceAttr();
                    if(obj.unpack(elem_rep))
                        mJoinedDevices.add(obj);
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_joiningState)) {
            didUnpack = true;
            mJoiningState = joiningStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_joiningState));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_signal)) {
            didUnpack = true;
            ZbSignalAttr obj = new ZbSignalAttr();
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

    public CoordinatorState coordinatorStateFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("nominated")) {
                return CoordinatorState.kNominated;
            }
            if (value.equalsIgnoreCase("notNominated")) {
                return CoordinatorState.kNotNominated;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return CoordinatorState.kUnknown;
            }
        }
        return CoordinatorState.kNominated;
    }

    public String coordinatorStateToString(CoordinatorState value) {
        switch(value) {
            case kNominated:
                return "nominated";
            case kNotNominated:
                return "notNominated";
            case kUnknown:
                return "unknown";
        }

        return "nominated";
    }

    public JoiningState joiningStateFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("allowed")) {
                return JoiningState.kAllowed;
            }
            if (value.equalsIgnoreCase("disallowed")) {
                return JoiningState.kDisallowed;
            }
            if (value.equalsIgnoreCase("unknown")) {
                return JoiningState.kUnknown;
            }
        }
        return JoiningState.kAllowed;
    }

    public String joiningStateToString(JoiningState value) {
        switch(value) {
            case kAllowed:
                return "allowed";
            case kDisallowed:
                return "disallowed";
            case kUnknown:
                return "unknown";
        }

        return "allowed";
    }


   @Override
   public Object getData() {
    ZigbeeAttr clonedObj = new ZigbeeAttr();
    clonedObj.mAdapterState = this.mAdapterState;
    clonedObj.mCoordinatorState = this.mCoordinatorState;
    clonedObj.mError = this.mError;
    for(ZbDeviceAttr item:mJoinedDevices) {
        clonedObj.mJoinedDevices.add((ZbDeviceAttr)item.getData());
    }
    clonedObj.mJoiningState = this.mJoiningState;
    clonedObj.mSignal = (ZbSignalAttr)mSignal.getData();
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof ZigbeeAttr) {
        ZigbeeAttr obj = (ZigbeeAttr) state;
        this.mAdapterState = obj.mAdapterState;
        this.mCoordinatorState = obj.mCoordinatorState;
        this.mError = obj.mError;
        this.mJoinedDevices.clear();
        for(ZbDeviceAttr item:obj.mJoinedDevices) {
            this.mJoinedDevices.add((ZbDeviceAttr)item.getData());
        }
        this.mJoiningState = obj.mJoiningState;
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
        if (rep.hasAttribute(ResourceAttributes.Prop_coordinatorState)) {
            didChanged = (mCoordinatorState != coordinatorStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_coordinatorState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_error)) {
            didChanged = (!mError.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_error)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_joinedDevices)) {
            reps = rep.getValue(ResourceAttributes.Prop_joinedDevices);
            if(reps != null) {
                if(mJoinedDevices.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mJoinedDevices.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_joiningState)) {
            didChanged = (mJoiningState != joiningStateFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_joiningState)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_signal)) {
            didChanged = (mSignal.checkDifference(rep.getValue(ResourceAttributes.Prop_signal)));
            if(didChanged) return true;
        }
        return false;
   }
}