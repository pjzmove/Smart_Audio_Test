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

public class PlayItemAttr implements IResourceAttributes {


    public String mAlbum;
    public String mArtist;
    public int mDurationMsecs;
    public String mGenre;
    public String mThumbnailUrl;
    public String mTitle;
    public String mUrl;
    public List<VariantHelperAttr>  mUserData;

     public PlayItemAttr() {
        mAlbum = "";
        mArtist = "";
        mDurationMsecs = 0;
        mGenre = "";
        mThumbnailUrl = "";
        mTitle = "";
        mUrl = "";
        mUserData = new ArrayList<>();
     }

     @Override
     public OcRepresentation pack() throws OcException {
         OcRepresentation rep = new OcRepresentation();
         rep.setUri("/play_item_rep_uri");

         List<OcRepresentation> reps = new ArrayList<>();
         rep.setValue(ResourceAttributes.Prop_album,mAlbum);
         rep.setValue(ResourceAttributes.Prop_artist,mArtist);
         rep.setValue(ResourceAttributes.Prop_durationMsecs,mDurationMsecs);
         rep.setValue(ResourceAttributes.Prop_genre,mGenre);
         rep.setValue(ResourceAttributes.Prop_thumbnailUrl,mThumbnailUrl);
         rep.setValue(ResourceAttributes.Prop_title,mTitle);
         rep.setValue(ResourceAttributes.Prop_url,mUrl);

         for (VariantHelperAttr elem : mUserData) {
           reps.add(elem.pack());
         }

         if (reps.size() > 0) {
            Stream<OcRepresentation> repStream = reps.stream();
            OcRepresentation[] repArray = repStream.toArray(size -> new OcRepresentation[size]);
            rep.setValue(ResourceAttributes.Prop_userData,repArray);
         }
         return rep;
     }

     @Override
     public boolean unpack(OcRepresentation rep) throws OcException, NullPointerException  {
        OcRepresentation[] reps;
        boolean didUnpack = false;
        if(rep == null) return didUnpack;
        
        if (rep.hasAttribute(ResourceAttributes.Prop_album)) {
            didUnpack = true;
            mAlbum = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_album);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_artist)) {
            didUnpack = true;
            mArtist = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_artist);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_durationMsecs)) {
            didUnpack = true;
            mDurationMsecs = ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_durationMsecs);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_genre)) {
            didUnpack = true;
            mGenre = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_genre);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_thumbnailUrl)) {
            didUnpack = true;
            mThumbnailUrl = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_thumbnailUrl);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_title)) {
            didUnpack = true;
            mTitle = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_title);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_url)) {
            didUnpack = true;
            mUrl = ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_url);
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_userData)) {
            didUnpack = true;
            reps = rep.getValue(ResourceAttributes.Prop_userData);
            if(reps != null) {
                mUserData.clear();
                for (OcRepresentation elem_rep: reps) {
                    VariantHelperAttr obj = new VariantHelperAttr();
                    if(obj.unpack(elem_rep))
                        mUserData.add(obj);
                }
            }
        }
        return didUnpack;
    }


   @Override
   public Object getData() {
    PlayItemAttr clonedObj = new PlayItemAttr();
    clonedObj.mAlbum = this.mAlbum;
    clonedObj.mArtist = this.mArtist;
    clonedObj.mDurationMsecs = this.mDurationMsecs;
    clonedObj.mGenre = this.mGenre;
    clonedObj.mThumbnailUrl = this.mThumbnailUrl;
    clonedObj.mTitle = this.mTitle;
    clonedObj.mUrl = this.mUrl;
    for(VariantHelperAttr item:mUserData) {
        clonedObj.mUserData.add((VariantHelperAttr)item.getData());
    }
    return clonedObj;
   }

   @Override
   public void setData(Object state) {
     if(state instanceof PlayItemAttr) {
        PlayItemAttr obj = (PlayItemAttr) state;
        this.mAlbum = obj.mAlbum;
        this.mArtist = obj.mArtist;
        this.mDurationMsecs = obj.mDurationMsecs;
        this.mGenre = obj.mGenre;
        this.mThumbnailUrl = obj.mThumbnailUrl;
        this.mTitle = obj.mTitle;
        this.mUrl = obj.mUrl;
        this.mUserData.clear();
        for(VariantHelperAttr item:obj.mUserData) {
            this.mUserData.add((VariantHelperAttr)item.getData());
        }
    }
   }

   @Override
   public boolean checkDifference(OcRepresentation rep) throws OcException {
        OcRepresentation[] reps;
        if(rep == null) return false;
        boolean didChanged;
        if (rep.hasAttribute(ResourceAttributes.Prop_album)) {
            didChanged = (!mAlbum.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_album)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_artist)) {
            didChanged = (!mArtist.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_artist)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_durationMsecs)) {
            didChanged = (mDurationMsecs != ResourceAttrUtils.intValueFromRepresentation(rep, ResourceAttributes.Prop_durationMsecs));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_genre)) {
            didChanged = (!mGenre.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_genre)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_thumbnailUrl)) {
            didChanged = (!mThumbnailUrl.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_thumbnailUrl)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_title)) {
            didChanged = (!mTitle.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_title)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_url)) {
            didChanged = (!mUrl.equalsIgnoreCase(ResourceAttrUtils.stringValueFromRepresentation(rep, ResourceAttributes.Prop_url)));
            if(didChanged) return true;
        }
        if (rep.hasAttribute(ResourceAttributes.Prop_userData)) {
            reps = rep.getValue(ResourceAttributes.Prop_userData);
            if(reps != null) {
                if(mUserData.size() != reps.length)
                    return true;
                else {
                    for (int i=0; i < reps.length; i++) {
                        if(mUserData.get(i).checkDifference(reps[i]))
                            return true;
                    }
                }
            }
        }
        return false;
   }
}