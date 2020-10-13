/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider;

public class ContentProviderException extends Exception {
	private static final long serialVersionUID = 6730638713425009006L;

	public static final int ERROR_UNKNOWN = 0;
	public static final int ERROR_SD_CARD_UNAVAILABLE = 1;
	public static final int ERROR_STORAGE_PERMISSION = 2;

	private int mCode = -1;

	public ContentProviderException(final String detailMessage) {
		super(detailMessage);
	}

	public ContentProviderException(final Throwable throwable) {
		super(throwable);
	}

	public ContentProviderException(final String detailMessage, final Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ContentProviderException(final int code, final String detailMessage) {
		super(detailMessage);
		mCode = code;
	}

	public ContentProviderException(final int code, final Throwable throwable) {
		super(throwable);
		mCode = code;
	}

	public ContentProviderException(final int code, final String detailMessage, final Throwable throwable) {
		super(detailMessage, throwable);
		mCode = code;
	}

	public int getCode() {
		return mCode;
	}
}
