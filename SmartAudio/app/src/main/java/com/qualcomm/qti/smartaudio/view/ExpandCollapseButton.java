/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import com.qualcomm.qti.smartaudio.R;

public class ExpandCollapseButton extends AppCompatImageButton {

	public enum ExpandCollapseState {
		EXPAND,
		COLLAPSE
	}

	private ExpandCollapseState mState = ExpandCollapseState.EXPAND;

	public ExpandCollapseButton(Context context) {
		super(context);
	}

	public ExpandCollapseButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExpandCollapseButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setState(final ExpandCollapseState state) {
		mState = state;
		update();
	}

	public ExpandCollapseState getState() {
		return mState;
	}

	private void update() {
		switch (mState) {
			case COLLAPSE:
				setImageResource(R.drawable.btn_close);
				break;
			case EXPAND:
			default:
				setImageResource(R.drawable.btn_expand);
				break;
		}
	}
}
