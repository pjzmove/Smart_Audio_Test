/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.upnp;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class UpnpSearchRequest extends ContentSearchRequest {
	private static final long serialVersionUID = 3316957165535502806L;

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.UPNP;
	}

	@Override
	protected Provider getProvider() {
		return UpnpProvider.getInstance();
	}
}
