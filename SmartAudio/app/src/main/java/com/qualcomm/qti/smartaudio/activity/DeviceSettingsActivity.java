/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.fragment.DeviceSettingsFragment;

public class DeviceSettingsActivity extends BaseActivity implements View.OnClickListener {
	public static final String EXTRA_ID = "ID";
	public static final String EXTRA_HOST = "HOST";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device_settings);

		Bundle extras = getIntent().getExtras();
		String id = extras.getString(EXTRA_ID);
		String host = extras.getString(EXTRA_HOST);

		if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
         .replace(R.id.device_settings_frame, DeviceSettingsFragment.newInstance(id,host)).commitNow();
    }

    setupSettingsBar();
	}

	protected void setupSettingsBar() {
		ImageButton actionBarBackButton = findViewById(R.id.settings_app_bar_back_button);
		actionBarBackButton.setVisibility((isTablet()) ? View.INVISIBLE : View.VISIBLE);
		if (!isTablet()) {
			actionBarBackButton.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.settings_app_bar_back_button:
				onBackPressed();
				overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
				break;
		}
	}
}
