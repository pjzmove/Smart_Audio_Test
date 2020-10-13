/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.AvsOnboardingInfo;

public class AvsOnBoardingSetupFragment extends BaseFragment {

    private final static String EXTRA_KEYS_AVS_ONBOARDING = "IOTSYS_AVS_ONBOARDING";

    public static AvsOnBoardingSetupFragment newInstance(AvsOnboardingInfo info) {
        AvsOnBoardingSetupFragment fragment = new AvsOnBoardingSetupFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_KEYS_AVS_ONBOARDING, info);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_avs_on_boarding_setup, container, false);

        String title = getString(R.string.avs_on_boarding_title);
        TextView titleView = view.findViewById(R.id.settings_app_bar_text_view);
        titleView.setText(title);
        titleView.setContentDescription(getString(R.string.cont_desc_screen_avs_on_boarding));

        Button signIn = view.findViewById(R.id.sign_in_button);
        signIn.setEnabled(true);

        // get AVS information
        Bundle bundle = getArguments();
        AvsOnboardingInfo info = bundle != null ? bundle.getParcelable(EXTRA_KEYS_AVS_ONBOARDING) : null;
        String url = info != null ? info.mUrl : "";
        String code = info != null ? info.mCode : "";

        signIn.setOnClickListener(v -> {
            AvsOnBoardingWebViewFragment fragment = AvsOnBoardingWebViewFragment.newInstance(url, code);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.setting_container, fragment)
                    .commitNow();
        });

        return view;
    }
}
