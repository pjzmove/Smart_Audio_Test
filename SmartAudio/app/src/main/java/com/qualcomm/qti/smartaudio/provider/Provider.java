/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider;

import android.content.Context;

import com.qualcomm.qti.smartaudio.model.ContentList;

public interface Provider {

	ContentList getContent(final Context context, final ContentSearchRequest contentSearchRequest, ContentList appendContentList)
			throws ContentProviderException;

	ContentList searchContent(final Context context, final ContentSearchRequest contentSearchRequest, final String searchText)
			throws ContentProviderException;
}
