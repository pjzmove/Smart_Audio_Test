/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.output;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class OutputSourceSearchRequest extends ContentSearchRequest {
	private static final long serialVersionUID = 1556684650841806728L;

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.OUTPUT;
	}

	@Override
	protected Provider getProvider() {
		return OutputSourceProvider.getInstance();
	}


}
