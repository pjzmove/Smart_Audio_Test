/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;

public class SearchFragment extends BrowseFragment {
	public static final String TAG = SearchFragment.class.getSimpleName();

	private static final int RELOAD_DELAY = 500;

	private Handler mSearchHandler = new Handler(Looper.getMainLooper());

	private Runnable mReloadRunnable = new Runnable() {
		@Override
		public void run() {
			reload();
		}
	};

	public static SearchFragment newInstance() {
		return new SearchFragment();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		if (mBackView != null) {
			mBackView.setVisibility(View.GONE);
		}

		return view;
	}

	@Override
	public BrowseFragmentType getBrowseFragmentType() {
		return BrowseFragmentType.SEARCH;
	}

	public synchronized void updateSearchRequest(final ContentSearchRequest contentSearchRequest) {
		mRequest = contentSearchRequest;

		mSearchHandler.removeCallbacks(mReloadRunnable);
		mSearchHandler.postDelayed(mReloadRunnable, RELOAD_DELAY);
	}
}
