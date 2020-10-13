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

public class AddConfiguredDevicesInAttr implements IResourceAttributes {


    public List<AddConfiguredDevicesPlayersAttr>  mPlayers;

     public AddConfiguredDevicesInAttr() {
        mPlayers = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/add_configured_devices_in_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();

         for (AddConfiguredDevicesPlayersAttr elem : mPlayers) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_players,repArray);
         }
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_players)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_players);
            if(reps != null) {
                mPlayers.clear();
                for (OcRepresentation elem_rep: reps) {
                    AddConfiguredDevicesPlayersAttr obj = new AddConfiguredDevicesPlayersAttr();
                    if(obj.unpack(elem_rep))
                        mPlayers.add(obj);
                }
            }
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    AddConfiguredDevicesInAttr clonedObj = new AddConfiguredDevicesInAttr();
    for(AddConfiguredDevicesPlayersAttr item:mPlayers) {
        clonedObj.mPlayers.add((AddConfiguredDevicesPlayersAttr)item.getData());
    }
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof AddConfiguredDevicesInAttr) {
        AddConfiguredDevicesInAttr obj = (AddConfiguredDevicesInAttr) state;
        this.mPlayers.clear();
        for(AddConfiguredDevicesPlayersAttr item:obj.mPlayers) {
            this.mPlayers.add((AddConfiguredDevicesPlayersAttr)item.getData());
        }
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_players)) {
            reps = rep.getValue(ResourceAttributes.Prop_players);
            if(reps != null) {
                if(mPlayers.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mPlayers.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        return false;
   }
}