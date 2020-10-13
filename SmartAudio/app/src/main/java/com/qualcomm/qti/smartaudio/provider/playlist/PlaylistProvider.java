/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.playlist;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;


import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentGroup;
import com.qualcomm.qti.smartaudio.model.ContentItem;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.MediaContentList;
import com.qualcomm.qti.smartaudio.model.playlist.PlaylistContentList;
import com.qualcomm.qti.smartaudio.model.playlist.PlaylistNameContentList;
import com.qualcomm.qti.smartaudio.provider.ContentProviderException;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;
import com.qualcomm.qti.smartaudio.provider.local.LocalSearchRequest;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class PlaylistProvider implements Provider {
	private static final String TAG = PlaylistProvider.class.getSimpleName();

	private static PlaylistProvider sInstance = null;

	public static final String PLAYLIST = "playlist";
	public static final String PLAYLIST_SCHEME = "playlist://";
	public static final String PLAYLIST_PATH = PLAYLIST_SCHEME + "audio/playlist";

	public static PlaylistProvider getInstance() {
		PlaylistProvider playlistProvider = sInstance;
		synchronized (PlaylistProvider.class) {
			if (playlistProvider == null) {
				sInstance = playlistProvider = new PlaylistProvider();
			}
		}
		return playlistProvider;
	}

	@Override
	public ContentList getContent(Context context, ContentSearchRequest contentSearchRequest, ContentList appendContentList){
		String query = contentSearchRequest.getQuery();
		Uri queryUri = Uri.parse(query);
		String lastPathSegment = queryUri.getLastPathSegment();
		String playlistName = queryUri.getQueryParameter(PlaylistSearchRequest.PLAYLIST_ID);

		if (appendContentList == null) {
			if (lastPathSegment.equals(PLAYLIST) && (playlistName == null)) {
				appendContentList = new PlaylistNameContentList();
				appendContentList.setEmptyString(context.getString(R.string.no_playlist));
			} else {
				appendContentList = new PlaylistContentList();
				appendContentList.setEmptyString(context.getString(R.string.no_music_in_playlist));
			}
		}

		return appendContentList;
	}

	@Override
	public ContentList searchContent(Context context, ContentSearchRequest contentSearchRequest, String searchText)
			throws ContentProviderException {
		if (Utils.isStringEmpty(searchText)) {
			return new MediaContentList();
		}

		ContentList contentList = getContent(context, contentSearchRequest, null);
		DataList dataList = contentList.getDataList();
		List<ContentItem> items = dataList.getItems();
		Iterator<ContentItem> iterator = items.iterator();
		Pattern pattern = Pattern.compile("(?i)[" + searchText + "]?", Pattern.CASE_INSENSITIVE);

		while (iterator.hasNext()) {
			ContentItem contentItem = iterator.next();
			String title = contentItem.getTitle();
			if (Utils.isStringEmpty(title) || !title.matches(pattern.toString())) {
				iterator.remove();
			}
		}
		return contentList;
	}

	private ContentList getOtherPlaylists(final Context context) {
		ContentList contentList = new MediaContentList();
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				return contentList;
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		final String[] playlistProjections = { BaseColumns._ID,
				MediaStore.Audio.PlaylistsColumns.NAME };

		Cursor cursor = contentResolver.query(android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
				playlistProjections, null, null, MediaStore.Audio.PlaylistsColumns.NAME);
		if (cursor == null) {
			return contentList;
		}

		if (!cursor.moveToFirst()) {
			return contentList;
		}

		List<OtherPlaylistInfo> otherPlaylistInfos = new ArrayList<>();
		try {
			int idColumn = cursor.getColumnIndex(BaseColumns._ID);
			int nameColumn = cursor.getColumnIndex(MediaStore.Audio.PlaylistsColumns.NAME);

			do {
				String name = cursor.getString(nameColumn);
				if (Utils.isStringEmpty(name)) {
					continue;
				}

				LocalSearchRequest localSearchRequest = new LocalSearchRequest();
				localSearchRequest.setQuery(Uri.withAppendedPath(ContentUris
								.withAppendedId(android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
										cursor.getLong(idColumn)),
						"media").toString());
				otherPlaylistInfos.add(new OtherPlaylistInfo(name, localSearchRequest));
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		DataList listResult = contentList.getDataList();
		for (OtherPlaylistInfo otherPlaylistInfo : otherPlaylistInfos) {
			ContentSearchRequest contentSearchRequest = otherPlaylistInfo.contentSearchRequest;
			ContentList otherPlaylistContentList = null;
			try {
				otherPlaylistContentList = contentSearchRequest.getContent(context, null);
			} catch (ContentProviderException e) {
				e.printStackTrace();
			}
			if (otherPlaylistContentList == null) {
				continue;
			}
			ContentGroup contentGroup = new ContentGroup();
			contentGroup.setTitle(otherPlaylistInfo.name);
			contentGroup.setSubtitle(context.getString(R.string.number_of_songs, otherPlaylistContentList.getSize()));
			contentGroup.setRequest(contentSearchRequest);
			DataList otherPlaylistDataList = otherPlaylistContentList.getDataList();
			if (!otherPlaylistDataList.isEmpty()) {
				List<ContentItem> contentItems = otherPlaylistDataList.getItems();
				contentGroup.setThumbnailUrl(contentItems.get(0).getThumbnailUrl());
			}

			listResult.addItem(contentGroup);
		}
		return contentList;
	}

	private class OtherPlaylistInfo {
		String name;
		ContentSearchRequest contentSearchRequest;

		public OtherPlaylistInfo(final String name, final ContentSearchRequest contentSearchRequest) {
			this.name = name;
			this.contentSearchRequest = contentSearchRequest;
		}
	}
}
