/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.AboutFragment;
import com.qualcomm.qti.smartaudio.fragment.LegalFragment;
import com.qualcomm.qti.smartaudio.fragment.ToSFragment;
import com.qualcomm.qti.smartaudio.util.FragmentController;

public class AboutActivity extends BaseActivity implements AboutFragment.OnAboutClickedListener, View.OnClickListener {

    private TextView mActionBarText;
    private ImageButton mActionBarBackButton;
    private ImageButton mActionBarCloseButton;

    private static final String ABOUT_MAIN = "ABOUT_MAIN";
    private static final String ABOUT_LEGAL = "ABOUT_LEGAL";
    private static final String ABOUT_TERMS = "ABOUT_TERMS";

    private FragmentController mFragmentController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

		mActionBarText = (TextView) findViewById(R.id.default_app_bar_text_view);
		mActionBarBackButton = (ImageButton) findViewById(R.id.default_app_bar_back_button);
		mActionBarCloseButton = (ImageButton) findViewById(R.id.default_app_bar_close_button);
        mFragmentController = new FragmentController(getSupportFragmentManager(), R.id.about_frame);
        showAboutFragment(false);

        mActionBarCloseButton.setOnClickListener(this);
        mActionBarBackButton.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        String tag = mFragmentController.getCurrentFragmentTag();
        switch (tag) {
            case ABOUT_MAIN:
                finish();
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
                break;
            default:
                showAboutFragment(true);
                break;
        }
    }

    private void showAboutFragment(final boolean pop) {
        if (!pop) {
            mFragmentController.startFragment(AboutFragment.newInstance(), ABOUT_MAIN, false);
        }
        else {
            mFragmentController.pop();
        }
        mActionBarCloseButton.setVisibility(View.VISIBLE);
        mActionBarBackButton.setVisibility(View.GONE);
        mActionBarText.setText(getString(R.string.about));
        mActionBarText.setContentDescription(getString(R.string.cont_desc_screen, getString(R.string.cont_desc_about)));
    }

    private void showLegalFragment() {
        mFragmentController.push(LegalFragment.newInstance(), ABOUT_LEGAL);
        mActionBarCloseButton.setVisibility(View.GONE);
        mActionBarBackButton.setVisibility(View.VISIBLE);
        mActionBarText.setText(getString(R.string.legal_notices_upper));
        mActionBarText.setContentDescription(getString(R.string.cont_desc_screen,
													   getString(R.string.cont_desc_legal_notices)));
    }

    private void showToSFragment() {
        mFragmentController.push(ToSFragment.newInstance(), ABOUT_TERMS);
        mActionBarCloseButton.setVisibility(View.GONE);
        mActionBarBackButton.setVisibility(View.VISIBLE);
        mActionBarText.setText(getString(R.string.terms_of_service_upper));
        mActionBarText.setContentDescription(getString(R.string.cont_desc_screen, getString(R.string.cont_desc_terms_of_service)));
    }

    @Override
    public void onLegalClicked() {
        showLegalFragment();
    }

    @Override
    public void onToSClicked() {
        showToSFragment();
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }

    @Override
    public void onConnectivityChanged(boolean connected) {
    }
}
