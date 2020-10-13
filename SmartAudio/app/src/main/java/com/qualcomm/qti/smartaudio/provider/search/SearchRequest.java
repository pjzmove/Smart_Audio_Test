/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.search;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class SearchRequest extends ContentSearchRequest {
	private static final long serialVersionUID = -2815652899718053059L;

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.SEARCH;
	}

	@Override
	protected Provider getProvider() {
		return SearchProvider.getInstance();
	}
}
