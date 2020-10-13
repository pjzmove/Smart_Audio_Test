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

public class PlaylistHistoryPointAttr implements IResourceAttributes {

    public enum HistoryPointType {
        kInsert,
        kDelete,
        kMove,
    }


    public int mCount;
    public HistoryPointType mHistoryPointType;
    public int mPosition;
    public int mStart;

     public PlaylistHistoryPointAttr() {
        mCount = 0;
        mHistoryPointType = HistoryPointType.kInsert;
        mPosition = 0;
        mStart = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/playlist_history_point_rep_uri");

         rep.setValue(ResourceAttributes.Prop_count,mCount);
         rep.setValue(ResourceAttributes.Prop_historyPointType,historyPointTypeToString(mHistoryPointType));
         rep.setValue(ResourceAttributes.Prop_position,mPosition);
         rep.setValue(ResourceAttributes.Prop_start,mStart);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_count)) {
            didUnpack = true;
            mCount = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_count);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_historyPointType)) {
            didUnpack = true;
            mHistoryPointType = historyPointTypeFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_historyPointType));
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_position)) {
            didUnpack = true;
            mPosition = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_position);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_start)) {
            didUnpack = true;
            mStart = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_start);
        }
        return didUnpack;
    }

    public HistoryPointType historyPointTypeFromString(String value) {
        if(value != null) {
            if (value.equalsIgnoreCase("insert")) {
                return HistoryPointType.kInsert;
            }
            if (value.equalsIgnoreCase("delete")) {
                return HistoryPointType.kDelete;
            }
            if (value.equalsIgnoreCase("move")) {
                return HistoryPointType.kMove;
            }
        }
        return HistoryPointType.kInsert;
    }

    public String historyPointTypeToString(HistoryPointType value) {
        switch(value) {
            case kInsert:
                return "insert";
            case kDelete:
                return "delete";
            case kMove:
                return "move";
        }

        return "insert";
    }


   @Override
   public Object getData() {
    PlaylistHistoryPointAttr clonedObj = new PlaylistHistoryPointAttr();
    clonedObj.mCount = this.mCount;
    clonedObj.mHistoryPointType = this.mHistoryPointType;
    clonedObj.mPosition = this.mPosition;
    clonedObj.mStart = this.mStart;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlaylistHistoryPointAttr) {
        PlaylistHistoryPointAttr obj = (PlaylistHistoryPointAttr) state;
        this.mCount = obj.mCount;
        this.mHistoryPointType = obj.mHistoryPointType;
        this.mPosition = obj.mPosition;
        this.mStart = obj.mStart;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_count)) {
            didChanged = (mCount != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_count));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_historyPointType)) {
            didChanged = (mHistoryPointType != historyPointTypeFromString(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_historyPointType)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_position)) {
            didChanged = (mPosition != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_position));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_start)) {
            didChanged = (mStart != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_start));
            if(didChanged) return true;
        }
        return false;
   }
}