/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

import java.util.Hashtable;

/**
 * This class handles the Qualcomm font used throughout the app.
 */
public class CustomFont {
	private static Hashtable<String, Typeface> sCustomFontCache = new Hashtable<String, Typeface>();
	/**
	 * Sets qualcomm font on a textview based on the custom font attribute
	 * @param context the Android context object
	 * @param textView the text view where the font needs to be set
	 * @param attrs the attribute set
	 */
	public static void setCustomFont(final Context context, final TextView textView, final AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
		setCustomFont(context, textView, typedArray.getString(R.styleable.CustomFont_qfont));
		typedArray.recycle();
	}

	/**
	 * Sets a qualcomm font on a textView
	 * @param context the Android context object
	 * @param textView the text view where the font needs to be set
	 * @param fontName the font name
	 */
	public static void setCustomFont(final Context context, final TextView textView, final String fontName) {
		if (fontName == null) {
			return;
		}
		Typeface typeface = getTypeface(context, fontName);
		if (typeface != null) {
			textView.setTypeface(typeface);
		}
	}

	/**
	 * Get the typeface from the font name
	 * @param context the Android context object
	 * @param fontName the name of the font
	 * @return the typeface.  Returns null if not found.
	 */
	public static Typeface getTypeface(final Context context, final String fontName) {
		Typeface typeface = sCustomFontCache.get(fontName);
		if (typeface == null) {
			if (context != null) {
				try {
					typeface = Typeface.createFromAsset(context.getAssets(), fontName);
				} catch (Exception e) {
					return null;
				}
				sCustomFontCache.put(fontName, typeface);
			}
		}
		return typeface;
	}
}
