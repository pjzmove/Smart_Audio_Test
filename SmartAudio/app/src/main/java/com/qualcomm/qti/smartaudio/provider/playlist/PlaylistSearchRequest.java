/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.playlist;

import android.net.Uri;

import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;

public class PlaylistSearchRequest extends ContentSearchRequest {

	private static final long serialVersionUID = 7445226951094017314L;

	public static final String PLAYLIST_ID = "id";

	@Override
	public ContentSearchType getSearchType() {
		return ContentSearchType.PLAYLIST;
	}

	@Override
	protected Provider getProvider() {
		return PlaylistProvider.getInstance();
	}

	public String queryPlaylistName() {
		String query = getQuery();
		Uri queryUri = Uri.parse(query);
		return queryUri.getQueryParameter(PlaylistSearchRequest.PLAYLIST_ID);
	}
}
