/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.qualcomm.qti.smartaudio.util.CustomFont;

/**
 * This edit text view class allows you to have custom font
 */
public class CustomFontEditText extends EditText {
	/**
	 * Default constructor
	 * @param context the Android context object
	 */
	public CustomFontEditText(Context context) {
		super(context);
	}

	/**
	 * Constructor with attribute set
	 * @param context the Android context object
	 * @param attrs the attribute set
	 */
	public CustomFontEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		CustomFont.setCustomFont(context, this, attrs);
	}

	/**
	 * Constructor with attribute set and style
	 * @param context the Android context object
	 * @param attrs the attribute set
	 * @param defStyle the style def id
	 */
	public CustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CustomFont.setCustomFont(context, this, attrs);
	}
}
