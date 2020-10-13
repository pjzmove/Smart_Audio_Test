/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.interfaces;

import com.qualcomm.qti.smartaudio.model.ContentGroup;
import java.util.ArrayList;
import java.util.List;

public interface IFragmentObserver {

    List<IFragmentControl> mObserver = new ArrayList<>();

    default void register(IFragmentControl fragment) {
      if(!mObserver.contains(fragment))
        mObserver.add(fragment);
    }

    default void unRegister(IFragmentControl fragment) {
      if(mObserver.contains(fragment))
        mObserver.remove(fragment);
    }

    default void clickBrowseListGroup(ContentGroup contentGroup) {
      for(IFragmentControl control : mObserver) {
        control.onBrowseListGroupClicked(contentGroup);
      }
    }

    default void obsoleteChildBrowseFragment(){
      for(IFragmentControl control : mObserver) {
        control.onChildBrowseFragmentObsolete();
      }
    }

    default void modifyChildBrowseFragment(){
      for(IFragmentControl control : mObserver) {
          control.onChildBrowseFragmentModified();
       }
    }

    default void dismissGroupSpeakerFragment(){
      for(IFragmentControl control : mObserver) {
          control.onGroupSpeakerFragmentDismiss();
       }
    }

}
