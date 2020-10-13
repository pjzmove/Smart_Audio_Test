/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

public abstract class SetupFragment extends BaseFragment implements View.OnClickListener {
	public static final String SETUP_TAG_KEY = "SETUP_TAG_KEY";
	private static final String TAG = SetupFragment.class.getSimpleName();

	protected TextView mActionBarTitleTextView = null;

	protected FrameLayout mSetupBarFrameLayout = null;
	protected FrameLayout mFrameLayout = null;

	protected Button mTopButton = null;
	protected View mDivider1 = null;
	protected Button mMiddleButton = null;
	protected View mDivider2 = null;
	protected Button mBottomButton = null;

	protected String mTag = null;

	protected SetupFragmentListener mSetupFragmentListener = null;

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mTag = getArguments().getString(SETUP_TAG_KEY);
		mSetupFragmentListener = (SetupFragmentListener)context;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_setup, container, false);

		mSetupBarFrameLayout = (FrameLayout) view.findViewById(R.id.setup_bar_frame);

		View frameView = inflater.inflate(R.layout.app_bar_text, mSetupBarFrameLayout, true);
		mActionBarTitleTextView = (TextView) frameView.findViewById(R.id.setup_action_bar_title);

		mFrameLayout = (FrameLayout) view.findViewById(R.id.setup_frame);

		mTopButton = (Button) view.findViewById(R.id.setup_top_button);
		mTopButton.setOnClickListener(this);

		mDivider1 = view.findViewById(R.id.setup_divider1);

		mMiddleButton = (Button) view.findViewById(R.id.setup_middle_button);
		mMiddleButton.setOnClickListener(this);

		mDivider2 = view.findViewById(R.id.setup_divider2);

		mBottomButton = (Button) view.findViewById(R.id.setup_bottom_button);
		mBottomButton.setOnClickListener(this);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.setup_top_button:
				onTopButtonClicked();
				break;
			case R.id.setup_middle_button:
				onMiddleButtonClicked();
				break;
			case R.id.setup_bottom_button:
				onBottomButtonClicked();
				break;
			default:
				break;
		}
	}

	public void onTopButtonClicked() {
		if (mSetupFragmentListener != null) {
			mSetupFragmentListener.onTopButtonClicked(mTag);
		}
	}

	public void onMiddleButtonClicked() {
		if (mSetupFragmentListener != null) {
			mSetupFragmentListener.onMiddleButtonClicked(mTag);
		}
	}

	public void onBottomButtonClicked() {
		if (mSetupFragmentListener != null) {
			mSetupFragmentListener.onBottomButtonClicked(mTag);
		}
	}

	protected void setNoButtonSetup() {
		mTopButton.setVisibility(View.GONE);
		mDivider1.setVisibility(View.GONE);
		mMiddleButton.setVisibility(View.GONE);
		mDivider2.setVisibility(View.GONE);
		mBottomButton.setVisibility(View.GONE);
	}

	protected void setOneButtonSetup() {
		mTopButton.setVisibility(View.GONE);
		mDivider1.setVisibility(View.GONE);
		mMiddleButton.setVisibility(View.GONE);
		mDivider2.setVisibility(View.GONE);
		mBottomButton.setVisibility(View.VISIBLE);
	}

	protected void setTwoButtonsSetup() {
		mTopButton.setVisibility(View.GONE);
		mDivider1.setVisibility(View.GONE);
		mMiddleButton.setVisibility(View.VISIBLE);
		mDivider2.setVisibility(View.VISIBLE);
		mBottomButton.setVisibility(View.VISIBLE);
	}

	public interface SetupFragmentListener {
		void onTopButtonClicked(final String tag);
		void onMiddleButtonClicked(final String tag);
		void onBottomButtonClicked(final String tag);
	}
}
