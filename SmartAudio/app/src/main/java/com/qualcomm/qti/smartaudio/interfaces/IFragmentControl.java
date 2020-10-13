/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.interfaces;

import android.support.v4.app.Fragment;

import com.qualcomm.qti.smartaudio.fragment.DeviceDetailsFragment.ActionType;
import com.qualcomm.qti.smartaudio.model.ContentGroup;

public interface IFragmentControl {

    default void onBrowseListGroupClicked(ContentGroup contentGroup) {
    }

    default void onChildBrowseFragmentObsolete() {
    }

    default void onChildBrowseFragmentModified() {
    }

    default void onGroupSpeakerFragmentDismiss() {
    }

    default void onDeviceDetailsItemClick(ActionType type) {
    }

    default void onShowEditGroupNameFragment(Fragment fragment) {
    }

    default void onGroupNameChosen(String name) {
    }
}
