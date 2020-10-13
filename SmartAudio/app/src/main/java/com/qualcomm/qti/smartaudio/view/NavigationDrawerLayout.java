/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;

import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.manager.BlurManager;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.ref.WeakReference;

/**
 * This class does the blur first when open drawer, and unblur after dispose drawer
 */
public class NavigationDrawerLayout extends DrawerLayout implements BlurManager.BlurListener {
	private static final String TAG = NavigationDrawerLayout.class.getSimpleName();

	private WeakReference<BaseActivity> mActivityRef = null;

	public NavigationDrawerLayout(Context context) {
		super(context);
		mActivityRef = new WeakReference<>((BaseActivity)context);
	}

	public NavigationDrawerLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mActivityRef = new WeakReference<>((BaseActivity)context);
	}

	public NavigationDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mActivityRef = new WeakReference<>((BaseActivity)context);
	}

	@Override
	public void openDrawer(int gravity) {
		openDrawer();
	}

	private void openDrawer() {
		final BaseActivity baseActivity = mActivityRef.get();
		if (Utils.isActivityActive(baseActivity)) {
			baseActivity.showNavigationDrawer(this);
		}
	}

	@Override
	public void blurStarted() {}

	@Override
	public void blurFinished() {
		super.openDrawer(GravityCompat.START);
	}

	@Override
	public void unblurStarted() {
		super.closeDrawer(GravityCompat.START);
	}

	@Override
	public void unblurFinished() {}
}
