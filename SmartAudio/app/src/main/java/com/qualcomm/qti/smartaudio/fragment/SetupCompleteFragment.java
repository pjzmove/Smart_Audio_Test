/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;

public class SetupCompleteFragment extends SetupAddedFragment {

    public static SetupCompleteFragment newInstance(String tag) {
        SetupCompleteFragment fragment = new SetupCompleteFragment();
        Bundle args = new Bundle();
        args.putString(SETUP_TAG_KEY, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mActionBarTitleTextView.setText(R.string.speaker_added);
        mActionBarTitleTextView.setContentDescription(getString(R.string.cont_desc_screen_on_boarding_complete));

        mImageView.setImageResource(R.drawable.ic_success);
        mGreatText.setText(R.string.success);

        setTwoButtonsSetup();

        mMiddleButton.setText(R.string.add_another_speaker);
        mMiddleButton.setContentDescription(getString(R.string.cont_desc_button_add_another_speaker));

        mBottomButton.setText(R.string.exit_setup);
        mBottomButton.setContentDescription(getString(R.string.cont_desc_button_exit_setup));

        mAddedText.setText(R.string.added_speaker_message);
        mAddedSubText.setVisibility(View.VISIBLE);
        mAddedSubText.setText(R.string.name_not_set_message);

        return view;
    }
}
