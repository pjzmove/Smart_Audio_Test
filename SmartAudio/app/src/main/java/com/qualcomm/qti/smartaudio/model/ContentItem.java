/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.os.Parcelable;

import java.io.Serializable;

public abstract class ContentItem implements Serializable, Parcelable {

	private static final long serialVersionUID = -5043204745893619437L;

	public enum ContentType {
		MEDIA,
		INPUT,
		GROUP,
		SECTION,
		ONLINE
	}

	public ContentItem() {}

	public abstract String getTitle();

	public abstract void setTitle(final String title);

	public abstract String getSubtitle();

	public abstract void setSubtitle(final String subtitle);

	public abstract String getThumbnailUrl();

	public abstract void setThumbnailUrl(final String thumbnailUrl);

	public abstract ContentType getContentType();
}
