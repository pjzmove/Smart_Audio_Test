/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.qualcomm.qti.smartaudio.util.CustomFont;

/**
 * This button class allows you to have custom font
 */
public class CustomFontButton extends Button {
	/**
	 * Default constructor
	 * @param context the Android context object
	 */
	public CustomFontButton(Context context) {
		super(context);
	}

	/**
	 * Constructor with attribute set
	 * @param context the Android context object
	 * @param attrs the attribute set
	 */
	public CustomFontButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		CustomFont.setCustomFont(context, this, attrs);
	}

	/**
	 * Constructor with attribute set and style
	 * @param context the Android context object
	 * @param attrs the attribute set
	 * @param defStyle the style def id
	 */
	public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CustomFont.setCustomFont(context, this, attrs);
	}
}
