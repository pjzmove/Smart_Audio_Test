/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TextAppearanceSpan;

import com.qualcomm.qti.smartaudio.R;

public class CustomTextAppearanceSpan extends TextAppearanceSpan {
	final Context mContext;
	final int mAppearance;

	public CustomTextAppearanceSpan(Context context, int appearance) {
		super(context, appearance);
		mContext = context;
		mAppearance = appearance;
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		super.updateDrawState(ds);
		applyFont(ds);
	}

	@Override
	public void updateMeasureState(TextPaint paint) {
		super.updateMeasureState(paint);
		applyFont(paint);
	}

	private void applyFont(Paint paint) {
		TypedArray typedArray = mContext.obtainStyledAttributes(mAppearance, R.styleable.CustomFont);
		if (typedArray == null) {
			return;
		}
		String font = typedArray.getString(R.styleable.CustomFont_qfont);
		if (font == null) {
			return;
		}
		Typeface tf = CustomFont.getTypeface(mContext, font);
		paint.setTypeface(tf);
	}
}
