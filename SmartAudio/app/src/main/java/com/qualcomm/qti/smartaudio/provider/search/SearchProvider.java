/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.search;

import android.content.Context;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.provider.ContentProviderException;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.model.ContentSection;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.MediaContentList;
import com.qualcomm.qti.smartaudio.provider.Provider;
import com.qualcomm.qti.smartaudio.provider.local.LocalProvider;
import com.qualcomm.qti.smartaudio.provider.local.LocalSearchRequest;
import com.qualcomm.qti.smartaudio.provider.playlist.PlaylistProvider;
import com.qualcomm.qti.smartaudio.provider.playlist.PlaylistSearchRequest;

public class SearchProvider implements Provider {
	private static final String TAG = SearchProvider.class.getSimpleName();

	private static SearchProvider sInstance = null;

	public static SearchProvider getInstance() {
		SearchProvider searchProvider = sInstance;
		synchronized (SearchProvider.class) {
			if (searchProvider == null) {
				sInstance = searchProvider = new SearchProvider();
			}
		}
		return searchProvider;
	}

	@Override
	public ContentList getContent(Context context, ContentSearchRequest contentSearchRequest, ContentList appendContentList)
			throws ContentProviderException {
		ContentList contentList = appendContentList;
		if (contentList == null) {
			contentList = new MediaContentList();
		}
		contentList.setEmptyString(context.getString(R.string.no_search_results));

		DataList dataList = contentList.getDataList();

		String query = contentSearchRequest.getQuery();

		ContentSearchRequest searchRequest = new LocalSearchRequest();
		searchRequest.setQuery(LocalProvider.ARTIST_PATH);
		search(context, searchRequest, query, context.getString(R.string.artists), dataList);

		searchRequest = new LocalSearchRequest();
		searchRequest.setQuery(LocalProvider.ALBUM_PATH);
		search(context, searchRequest, query, context.getString(R.string.albums), dataList);

		searchRequest = new LocalSearchRequest();
		searchRequest.setQuery(LocalProvider.GENRE_PATH);
		search(context, searchRequest, query, context.getString(R.string.genres), dataList);

		searchRequest = new PlaylistSearchRequest();
		searchRequest.setQuery(PlaylistProvider.PLAYLIST_PATH);
		search(context, searchRequest, query, context.getString(R.string.playlists), dataList);

		searchRequest = new LocalSearchRequest();
		searchRequest.setQuery(LocalProvider.SONG_PATH);
		search(context, searchRequest, query, context.getString(R.string.songs), dataList);

		return contentList;
	}

	private void search(final Context context, final ContentSearchRequest contentSearchRequest, final String searchText,
						   final String sectionTitle, final DataList dataList) {
		try {
			ContentList contentList = contentSearchRequest.searchContent(context, searchText);
			if (!contentList.isEmpty()) {
				ContentSection contentSection = new ContentSection();
				contentSection.setTitle(sectionTitle);
				dataList.addItem(contentSection);
				dataList.append(contentList.getDataList());
			}
		} catch (ContentProviderException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ContentList searchContent(Context context, ContentSearchRequest contentSearchRequest, String searchText)
			throws ContentProviderException {
		return null;
	}
}
