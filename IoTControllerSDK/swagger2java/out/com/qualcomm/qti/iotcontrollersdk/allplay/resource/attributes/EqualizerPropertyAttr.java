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

public class EqualizerPropertyAttr implements IResourceAttributes {


    public List<Integer>  mBandLevel;
    public List<Integer>  mCenterFrequency;
    public EffectRangeAttr mLevelRange;
    public int mNumBands;

     public EqualizerPropertyAttr() {
        mBandLevel = new ArrayList<>();
        mCenterFrequency = new ArrayList<>();
        mLevelRange = new EffectRangeAttr();
        mNumBands = 0;
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/equalizer_property_rep_uri");

         rep.setValue(ResourceAttributes.Prop_bandLevel,ResourceAttrUtils.streamFromIntArray(mBandLevel));
         rep.setValue(ResourceAttributes.Prop_centerFrequency,ResourceAttrUtils.streamFromIntArray(mCenterFrequency));
         rep.setValue(ResourceAttributes.Prop_levelRange,mLevelRange.pack());
         rep.setValue(ResourceAttributes.Prop_numBands,mNumBands);
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_bandLevel)) {
            didUnpack = true;
            mBandLevel = ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_bandLevel);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_centerFrequency)) {
            didUnpack = true;
            mCenterFrequency = ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_centerFrequency);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_levelRange)) {
            didUnpack = true;
            EffectRangeAttr obj = new EffectRangeAttr();
            if(obj.unpack((OcRepresentation)rep.getValue(ResourceAttributes.Prop_levelRange)))
                mLevelRange = obj;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_numBands)) {
            didUnpack = true;
            mNumBands = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_numBands);
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    EqualizerPropertyAttr clonedObj = new EqualizerPropertyAttr();
    clonedObj.mBandLevel = this.mBandLevel;
    clonedObj.mCenterFrequency = this.mCenterFrequency;
    clonedObj.mLevelRange = (EffectRangeAttr)mLevelRange.getData();
    clonedObj.mNumBands = this.mNumBands;
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof EqualizerPropertyAttr) {
        EqualizerPropertyAttr obj = (EqualizerPropertyAttr) state;
        this.mBandLevel = obj.mBandLevel;
        this.mCenterFrequency = obj.mCenterFrequency;
        this.mLevelRange.setData(obj.mLevelRange);
        this.mNumBands = obj.mNumBands;
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_bandLevel)) {
            didChanged = (!mBandLevel.equals(ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_bandLevel)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_centerFrequency)) {
            didChanged = (!mCenterFrequency.equals(ResourceAttrUtils.intArrayFromStream(rep, ResourceAttributes.Prop_centerFrequency)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_levelRange)) {
            didChanged = (mLevelRange.checkDifference(rep.getValue(ResourceAttributes.Prop_levelRange)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_numBands)) {
            didChanged = (mNumBands != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_numBands));
            if(didChanged) return true;
        }
        return false;
   }
}