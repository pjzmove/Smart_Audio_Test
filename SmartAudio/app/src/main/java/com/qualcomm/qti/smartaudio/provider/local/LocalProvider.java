/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.provider.local;

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

import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.ContentGroup;
import com.qualcomm.qti.smartaudio.model.ContentList;
import com.qualcomm.qti.smartaudio.model.DataList;
import com.qualcomm.qti.smartaudio.model.MediaContentItem;
import com.qualcomm.qti.smartaudio.model.MediaContentList;
import com.qualcomm.qti.smartaudio.provider.ContentProviderException;
import com.qualcomm.qti.smartaudio.provider.ContentSearchRequest;
import com.qualcomm.qti.smartaudio.provider.Provider;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalProvider implements Provider {
	private static final String TAG = LocalProvider.class.getSimpleName();

	private static LocalProvider sInstance = null;

	public static final String ARTIST_PATH = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI.toString();
	public static final String ALBUM_PATH = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.toString();
	public static final String GENRE_PATH = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI.toString();
	public static final String SONG_PATH = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();

	public static final String LOCAL_SCHEME = "content://";

	public static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");
	public static final Uri SONG_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

	private static String ARTISTS = "artists";
	private static String ALBUMS = "albums";
	private static String MEDIA = "media";
	private static String GENRES = "genres";
	private static String MEMBERS = "members";

	public static LocalProvider getInstance() {
		LocalProvider localProvider = sInstance;
		synchronized (LocalProvider.class) {
			if (localProvider == null) {
				sInstance = localProvider = new LocalProvider();
			}
		}
		return localProvider;
	}

	@Override
	public ContentList getContent(Context context, ContentSearchRequest contentSearchRequest, ContentList appendContentList)
			throws ContentProviderException {

		String query = contentSearchRequest.getQuery();
		Uri queryUri = Uri.parse(query);
		String lastPathSegment = queryUri.getLastPathSegment();
		if (lastPathSegment.contains(ARTISTS)) {
			return queryArtists(context, queryUri, null, appendContentList);
		} else if (lastPathSegment.contains(ALBUMS)) {
			return queryAlbums(context, queryUri, null, appendContentList);
		} else if (lastPathSegment.contains(MEDIA)) {
			return querySongs(context, queryUri, null, appendContentList);
		} else if (lastPathSegment.contains(GENRES)) {
			return queryGenres(context, queryUri, null, appendContentList);
		} else if (lastPathSegment.contains(MEMBERS)) {
			return queryMembers(context, queryUri, appendContentList);
		}

		return null;
	}

	@Override
	public ContentList searchContent(Context context, ContentSearchRequest contentSearchRequest, String searchText)
			throws ContentProviderException {
		if (Utils.isStringEmpty(searchText)) {
			return new MediaContentList();
		}
		String query = contentSearchRequest.getQuery();
		Uri queryUri = Uri.parse(query);
		String lastPathSegment = queryUri.getLastPathSegment();
		if (lastPathSegment.contains(ARTISTS)) {
			return queryArtists(context, queryUri, searchText, null);
		} else if (lastPathSegment.contains(ALBUMS)) {
			return queryAlbums(context, queryUri, searchText, null);
		} else if (lastPathSegment.contains(MEDIA)) {
			return querySongs(context, queryUri, searchText, null);
		} else if (lastPathSegment.contains(GENRES)) {
			return queryGenres(context, queryUri, searchText, null);
		}
		return null;
	}

	private ContentList queryMembers(final Context context, final Uri queryUri, ContentList contentList)
			throws ContentProviderException {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				throw new ContentProviderException(ContentProviderException.ERROR_STORAGE_PERMISSION,
						"Query for Android Media failed!! READ_EXTERNAL_STORAGE not granted!");
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		if (contentList == null) {
			contentList = new MediaContentList();
			contentList.setEmptyString(context.getString(R.string.no_music_in_device));
		}

		final String[] memberProjections = {
				MediaStore.Audio.AudioColumns.ALBUM_ID
		};

		final String[] albumProjections = {
				MediaStore.Audio.Albums._ID,
				MediaStore.Audio.AlbumColumns.ALBUM,
				MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
		};

		Cursor cursor = contentResolver.query(queryUri,
				memberProjections, null, null, MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER);

		if (cursor == null) {
			throw new ContentProviderException(ContentProviderException.ERROR_SD_CARD_UNAVAILABLE,
					"Query for Android Media failed!! Cursor is null");
		}

		DataList dataList = contentList.getDataList();
		Set<Long> albumIDs = new HashSet<>();
		List<Long> orderedAlbumIDs = new ArrayList<>();
		try {
			if (!cursor.moveToFirst()) {
				return contentList;
			}

			int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);
			do {
				long albumID = cursor.getLong(albumIDColumn);
				if (albumID < 0) {
					continue;
				}

				if (!albumIDs.contains(albumID)) {
					albumIDs.add(albumID);
					orderedAlbumIDs.add(albumID);
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		for (Long albumID : orderedAlbumIDs) {
			StringBuilder where = new StringBuilder();
			where.append(MediaStore.Audio.Albums._ID).append("=").append(albumID);

			cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
					albumProjections, where.toString(), null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

			try {
				if (!cursor.moveToFirst()) {
					continue;
				}

				int albumColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
				int numberOfSongsColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);

				do {
					String album = cursor.getString(albumColumn);

					if (Utils.isStringEmpty(album)) {
						continue;
					}

					int numberOfSongs = cursor.getInt(numberOfSongsColumn);

					ContentGroup contentGroup = new ContentGroup();
					contentGroup.setTitle(album.toUpperCase());

					String subtitle = (numberOfSongs == 1) ? context.getString(R.string.one_song) :
							context.getString(R.string.number_of_songs, numberOfSongs);

					contentGroup.setSubtitle(subtitle);
					contentGroup.setThumbnailUrl(ContentUris.withAppendedId(ALBUM_ART_URI, albumID).toString());
					contentGroup.setPlayableCount(numberOfSongs);

					LocalSearchRequest localSearchRequest = new LocalSearchRequest();
					localSearchRequest.setQuery(Uri.withAppendedPath(ContentUris.withAppendedId(queryUri, albumID),
							"media").toString());
					contentGroup.setRequest(localSearchRequest);

					dataList.addItem(contentGroup);

				} while (cursor.moveToNext());
			} finally {
				cursor.close();
			}
		}

		return contentList;
	}

	private ContentList queryGenres(final Context context, final Uri queryUri, final String searchText,
									ContentList contentList)
			throws ContentProviderException {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				throw new ContentProviderException(ContentProviderException.ERROR_STORAGE_PERMISSION,
						"Query for Android Media failed!! READ_EXTERNAL_STORAGE not granted!");
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		if (contentList == null) {
			contentList = new MediaContentList();
			contentList.setEmptyString(context.getString(R.string.no_music_in_device));
		}

		final String[] genreProjections = {
				BaseColumns._ID,
				MediaStore.Audio.GenresColumns.NAME
		};

		StringBuffer where = new StringBuffer();
		String[] selectionArgs = {};
		if (searchText != null) {
			where.append(MediaStore.Audio.GenresColumns.NAME).append(" like ?");
			selectionArgs = new String[] { "%" + searchText + "%" };
		}

		Cursor cursor = contentResolver.query(queryUri, genreProjections, where.toString(), selectionArgs,
				MediaStore.Audio.Genres.DEFAULT_SORT_ORDER);
		if (cursor == null) {
			throw new ContentProviderException(ContentProviderException.ERROR_SD_CARD_UNAVAILABLE,
					"Query for Android Media failed!! Cursor is null");
		}

		List<AudioInfo> genres = new ArrayList<>();
		try {
			if (!cursor.moveToFirst()) {
				return contentList;
			}

			int idColumn = cursor.getColumnIndex(BaseColumns._ID);
			int nameColumn = cursor.getColumnIndex(MediaStore.Audio.GenresColumns.NAME);

			do {
				String name = cursor.getString(nameColumn);
				if (Utils.isStringEmpty(name)) {
					continue;
				}

				long genreID = cursor.getLong(idColumn);
				AudioInfo audioInfo = new AudioInfo();
				audioInfo.genreID = genreID;
				audioInfo.genre = name;
				genres.add(audioInfo);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		if (genres.isEmpty()) {
			return contentList;
		}

		final String[] memberProjections = {
				MediaStore.Audio.AudioColumns.ALBUM_ID
		};

		final String[] albumProjections = {
				MediaStore.Audio.Albums._ID,
				MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
		};

		for (AudioInfo audioInfo : genres) {
			cursor = contentResolver.query(MediaStore.Audio.Genres.Members.getContentUri("external", audioInfo.genreID),
					memberProjections, null, null, MediaStore.Audio.Genres.Members.DEFAULT_SORT_ORDER);

			Set<Long> albumIDs = new HashSet<>();
			try {
				if (!cursor.moveToFirst()) {
					continue;
				}

				int albumIDColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);

				do {
					long albumID = cursor.getLong(albumIDColumn);

					if (albumID < 0) {
						continue;
					}

					if (!albumIDs.contains(albumID)) {
						albumIDs.add(albumID);
					}
				} while (cursor.moveToNext());

			} finally {
				cursor.close();
			}
			audioInfo.numberOfAlbums = albumIDs.size();
			if (!albumIDs.isEmpty()) {
				audioInfo.albumID = albumIDs.iterator().next();
			}

			for (Long albumID : albumIDs) {
				StringBuilder whereString = new StringBuilder();
				whereString.append(MediaStore.Audio.Albums._ID).append("=").append(albumID);

				cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
						albumProjections, whereString.toString(), null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

				try {
					if (!cursor.moveToFirst()) {
						continue;
					}

					int numberOfSongsColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);

					do {
						int numberOfSongs = cursor.getInt(numberOfSongsColumn);
						audioInfo.numberOfTracks += numberOfSongs;
					} while (cursor.moveToNext());
				} finally {
					cursor.close();
				}
			}
		}

		DataList dataList = contentList.getDataList();
		for (AudioInfo audioInfo : genres) {
			String genre = audioInfo.genre;

			ContentGroup contentGroup = new ContentGroup();
			contentGroup.setTitle(genre.toUpperCase());

			String subtitle = (audioInfo.numberOfAlbums == 1) ? context.getString(R.string.one_album) :
					context.getString(R.string.number_of_albums, audioInfo.numberOfAlbums);
			subtitle += ", ";
			subtitle += (audioInfo.numberOfTracks == 1) ? context.getString(R.string.one_song) :
					context.getString(R.string.number_of_songs, audioInfo.numberOfTracks);
			contentGroup.setSubtitle(subtitle);
			contentGroup.setThumbnailUrl(ContentUris.withAppendedId(ALBUM_ART_URI, audioInfo.albumID).toString());
			contentGroup.setPlayableCount(audioInfo.numberOfTracks);

			LocalSearchRequest localSearchRequest = new LocalSearchRequest();
			localSearchRequest.setQuery(MediaStore.Audio.Genres.Members.getContentUri("external", audioInfo.genreID).toString());
			contentGroup.setRequest(localSearchRequest);

			dataList.addItem(contentGroup);
		}

		return contentList;
	}

	private ContentList querySongs(final Context context, final Uri queryUri, final String searchText, ContentList contentList)
			throws ContentProviderException {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				throw new ContentProviderException(ContentProviderException.ERROR_STORAGE_PERMISSION,
						"Query for Android Media failed!! READ_EXTERNAL_STORAGE not granted!");
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		if (contentList == null) {
			contentList = new MediaContentList();
			contentList.setEmptyString(context.getString(R.string.no_music_in_device));
		}

		String[] projections = {
				BaseColumns._ID,
				MediaStore.MediaColumns.DISPLAY_NAME,
				MediaStore.MediaColumns.TITLE,
				MediaStore.Audio.AudioColumns.ARTIST,
				MediaStore.Audio.AudioColumns.ALBUM,
				MediaStore.Audio.AudioColumns.ALBUM_ID,
				MediaStore.Audio.AudioColumns.DURATION
		};

		Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

		StringBuilder where = new StringBuilder();
		where.append(MediaStore.Audio.AudioColumns.IS_MUSIC).append("=1 ");

		StringBuilder sortBy = new StringBuilder();
		sortBy.append(MediaStore.Audio.Media.TITLE).append(", ")
				.append(MediaStore.Audio.AudioColumns.ARTIST).append(", ")
				.append(MediaStore.Audio.AudioColumns.ALBUM).append(", ")
				.append(MediaStore.Audio.AudioColumns.TRACK);

		List<String> pathSegments = queryUri.getPathSegments();
		Iterator<String> iterator = pathSegments.iterator();
		while (iterator.hasNext()) {
			String path = iterator.next().toString();
			if (path.equals(ARTISTS)) {
				String artistID = iterator.next().toString();
				where.append(" AND ").append(MediaStore.Audio.AudioColumns.ARTIST_ID).append("=").append(Long.parseLong(artistID));
			} else if (path.equals(ALBUMS)) {
				String albumID = iterator.next().toString();
				where.append(" AND ").append(MediaStore.Audio.AudioColumns.ALBUM_ID).append("=").append(Long.parseLong(albumID));
			} else if (path.equals(GENRES)) {
				String genreID = iterator.next().toString();
				uri = MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(genreID));
			} else if (path.equals(MEMBERS)) {
				String albumID = iterator.next().toString();
				where.append(" AND ").append(MediaStore.Audio.AudioColumns.ALBUM_ID).append("=").append(Long.parseLong(albumID));
			}
		}

		String[] selectionArgs = {};
		if (searchText != null) {
			where.append(" AND ").append(MediaStore.MediaColumns.TITLE).append(" like ?");
			selectionArgs = new String[] { "%" + searchText + "%" };
		}

		Cursor cursor = contentResolver.query(uri, projections,
				where.toString(), selectionArgs, sortBy.toString());
		if (cursor == null) {
			throw new ContentProviderException(ContentProviderException.ERROR_SD_CARD_UNAVAILABLE,
					"Query for Android Media failed!! Cursor is null");
		}

		DataList dataList = contentList.getDataList();
		try {
			if (!cursor.moveToFirst()) {
				return contentList;
			}

			int idColumn = cursor.getColumnIndex(BaseColumns._ID);
			int displayNameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
			int titleColumn = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
			int artistColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
			int albumColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM);
			int albumArtColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID);
			int durationColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION);

			do {
				String displayName = cursor.getString(displayNameColumn);
				String title = (titleColumn == -1) ? null : cursor.getString(titleColumn);
				if (title == null) {
					title = displayName;
				}

				MediaContentItem mediaContentItem = new MediaContentItem();
				mediaContentItem.setStreamUrl(ContentUris.withAppendedId(SONG_URI, cursor.getLong(idColumn)).toString());
				mediaContentItem.setTitle(title);

				String artist = cursor.getString(artistColumn);
				String album = cursor.getString(albumColumn);
				mediaContentItem.setAlbum(album);
				mediaContentItem.setArtist(artist);

				String subtitle = artist;
				if (!Utils.isStringEmpty(subtitle) && !Utils.isStringEmpty(album)) {
					subtitle += " - ";
				}
				subtitle += album;
				mediaContentItem.setSubtitle(subtitle);

				mediaContentItem.setDuration(Integer.parseInt(String.valueOf(cursor.getLong(durationColumn))));
				mediaContentItem.setThumbnailUrl(ContentUris.withAppendedId(ALBUM_ART_URI, cursor.getLong(albumArtColumn)).toString());

				dataList.addItem(mediaContentItem);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		return contentList;
	}

	private ContentList queryAlbums(final Context context, final Uri queryUri, final String searchText, ContentList contentList)
			throws ContentProviderException {

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				throw new ContentProviderException(ContentProviderException.ERROR_STORAGE_PERMISSION,
						"Query for Android Media failed!! READ_EXTERNAL_STORAGE not granted!");
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		if (contentList == null) {
			contentList = new MediaContentList();
			contentList.setEmptyString(context.getString(R.string.no_music_in_device));
		}

		final String[] projections = {
				BaseColumns._ID,
				MediaStore.Audio.AlbumColumns.ALBUM,
				MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
		};

		StringBuffer where = new StringBuffer();
		String[] selectionArgs = {};
		if (searchText != null) {
			where.append(MediaStore.Audio.AlbumColumns.ALBUM).append(" like ?");
			selectionArgs = new String[] { "%" + searchText + "%" };
		}

		Cursor cursor = contentResolver.query(queryUri, projections,
				where.toString(), selectionArgs, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
		if (cursor == null) {
			throw new ContentProviderException(ContentProviderException.ERROR_SD_CARD_UNAVAILABLE,
					"Query for Android Media failed!! Cursor is null");
		}

		DataList dataList = contentList.getDataList();
		try {
			if (!cursor.moveToFirst()) {
				return contentList;
			}

			int idColumn = cursor.getColumnIndex(BaseColumns._ID);
			int albumColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
			int numberOfSongsColumn = cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS);

			do {
				long albumID = cursor.getLong(idColumn);
				String album = cursor.getString(albumColumn);
				if (Utils.isStringEmpty(album)) {
					continue;
				}
				int numberOfSongs = cursor.getInt(numberOfSongsColumn);

				ContentGroup contentGroup = new ContentGroup();
				contentGroup.setTitle(album.toUpperCase());

				String subtitle = (numberOfSongs == 1) ? context.getString(R.string.one_song) :
						context.getString(R.string.number_of_songs, numberOfSongs);

				contentGroup.setSubtitle(subtitle);
				contentGroup.setThumbnailUrl(ContentUris.withAppendedId(ALBUM_ART_URI, albumID).toString());
				contentGroup.setPlayableCount(numberOfSongs);

				LocalSearchRequest localSearchRequest = new LocalSearchRequest();
				localSearchRequest.setQuery(Uri.withAppendedPath(ContentUris.withAppendedId(queryUri,
						cursor.getLong(idColumn)), "media").toString());
				contentGroup.setRequest(localSearchRequest);

				dataList.addItem(contentGroup);
			} while (cursor.moveToNext());
		}
		finally {
			cursor.close();
		}

		return contentList;
	}

	private ContentList queryArtists(final Context context, final Uri queryUri, final String searchText, ContentList contentList)
			throws ContentProviderException {

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				throw new ContentProviderException(ContentProviderException.ERROR_STORAGE_PERMISSION,
						"Query for Android Media failed!! READ_EXTERNAL_STORAGE not granted!");
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		if (contentList == null) {
			contentList = new MediaContentList();
			contentList.setEmptyString(context.getString(R.string.no_music_in_device));
		}

		final String[] artistProjections = {
				BaseColumns._ID,
				MediaStore.Audio.ArtistColumns.ARTIST,
				MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
				MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
		};

		StringBuffer where = new StringBuffer();
		String[] selectionArgs = {};
		if (searchText != null) {
			where.append(MediaStore.Audio.ArtistColumns.ARTIST).append(" like ?");
			selectionArgs = new String[] { "%" + searchText + "%" };
		}

		Cursor cursor = contentResolver.query(queryUri, artistProjections,
				where.toString(), selectionArgs, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
		if (cursor == null) {
			throw new ContentProviderException(ContentProviderException.ERROR_SD_CARD_UNAVAILABLE,
					"Query for Android Media failed!! Cursor is null");
		}

		List<AudioInfo> artists = new ArrayList<>();
		try {
			if (!cursor.moveToFirst()) {
				return contentList;
			}

			int idColumn = cursor.getColumnIndex(BaseColumns._ID);
			int artistColumn = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST);
			int numberOfAlbumsColumn = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS);
			int numberOfTracksColumn = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS);

			do {
				long artistID = cursor.getLong(idColumn);
				String artist = cursor.getString(artistColumn);
				if (Utils.isStringEmpty(artist)) {
					continue;
				}
				String numberOfAlbums = cursor.getString(numberOfAlbumsColumn);
				String numberOfTracks = cursor.getString(numberOfTracksColumn);

				AudioInfo audioInfo = new AudioInfo();
				audioInfo.artistID = artistID;
				audioInfo.numberOfAlbums = Integer.parseInt(numberOfAlbums);
				audioInfo.numberOfTracks = Integer.valueOf(numberOfTracks);
				audioInfo.artist = artist;
				artists.add(audioInfo);
			} while (cursor.moveToNext());
		}
		finally {
			cursor.close();
		}

		if (artists.isEmpty()) {
			return contentList;
		}

		final String[] albumProjections = {
				BaseColumns._ID,
				MediaStore.Audio.AlbumColumns.ARTIST
		};

		for (AudioInfo audioInfo : artists) {
			String artist = audioInfo.artist;

			StringBuffer albumWhere = new StringBuffer();

			albumWhere.append(MediaStore.Audio.AlbumColumns.ARTIST).append(" like ?");
			String[] albumSelectionArgs = new String[] { "%" + artist + "%" };

			cursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumProjections,
					albumWhere.toString(), albumSelectionArgs, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);

			try {
				if (!cursor.moveToFirst()) {
					continue;
				}

				int albumIDColumn = cursor.getColumnIndex(BaseColumns._ID);

				do {
					int albumID = cursor.getInt(albumIDColumn);

					if (albumID < 0) {
						continue;
					}

					audioInfo.albumID = albumID;

				} while ((audioInfo.albumID < 0) && cursor.moveToNext());

			} finally {
				cursor.close();
			}
		}

		DataList dataList = contentList.getDataList();
		for (AudioInfo audioInfo : artists) {
			String artist = audioInfo.artist;

			ContentGroup contentGroup = new ContentGroup();
			contentGroup.setTitle(artist.toUpperCase());

			String subtitle = (audioInfo.numberOfAlbums == 1) ? context.getString(R.string.one_album) :
					context.getString(R.string.number_of_albums, audioInfo.numberOfAlbums);
			subtitle += ", ";
			subtitle += (audioInfo.numberOfTracks == 1) ? context.getString(R.string.one_song) :
					context.getString(R.string.number_of_songs, audioInfo.numberOfTracks);
			contentGroup.setSubtitle(subtitle);
			contentGroup.setThumbnailUrl(ContentUris.withAppendedId(ALBUM_ART_URI, audioInfo.albumID).toString());
			contentGroup.setPlayableCount(audioInfo.numberOfTracks);

			LocalSearchRequest localSearchRequest = new LocalSearchRequest();
			localSearchRequest.setQuery(MediaStore.Audio.Artists.Albums.getContentUri("external", audioInfo.artistID).toString());
			contentGroup.setRequest(localSearchRequest);

			dataList.addItem(contentGroup);
		}
		return contentList;
	}

	private Map<String, AudioItem> getAllAudioItems(final Context context) {
		Map<String, AudioItem> audioItemMap = new HashMap<>();

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) !=
					PackageManager.PERMISSION_GRANTED) {
				return audioItemMap;
			}
		}

		ContentResolver contentResolver = context.getContentResolver();

		String[] projections = {
				BaseColumns._ID,
				MediaStore.MediaColumns.DISPLAY_NAME,
				MediaStore.MediaColumns.TITLE,
				MediaStore.Audio.AudioColumns.ARTIST,
				MediaStore.Audio.AudioColumns.ALBUM,
		};

		Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projections, null, null, null);
		if (cursor == null) {
			return audioItemMap;
		}

		if (!cursor.moveToFirst()) {
			cursor.close();
			return audioItemMap;
		}

		try {

			int idColumn = cursor.getColumnIndex(BaseColumns._ID);
			int displayNameColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
			int titleColumn = cursor.getColumnIndex(MediaStore.MediaColumns.TITLE);
			int artistColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
			int albumColumn = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM);
			do {
				String displayName = cursor.getString(displayNameColumn);
				String title = (titleColumn == -1) ? null : cursor.getString(titleColumn);
				if (title == null) {
					title = displayName;
				}
				String artist = cursor.getString(artistColumn);
				String album = cursor.getString(albumColumn);
				audioItemMap.put(String.valueOf(cursor.getLong(idColumn)),
						new AudioItem(title, artist, album));
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		return audioItemMap;
	}

	public void updateLocalContent(final Context context, final List<MediaItem> mediaItems) {
		Map<String, AudioItem> audioItemMap = getAllAudioItems(context);
		if (audioItemMap.size() == 0) {
			//this device does not have any songs, nothing to updateState.
			return;
		}

		for (MediaItem mediaItem : mediaItems) {
			String url = mediaItem.getStreamUrl();
			if (url != null) {
				Uri streamUri = Uri.parse(url);
				String streamID = streamUri.getQueryParameter("id");
				if ((streamID != null) && audioItemMap.containsKey(streamID)) {
					AudioItem audioItem = audioItemMap.get(streamID);
					AudioItem mediaAudioItem = new AudioItem(mediaItem.getTitle(), mediaItem.getArtist(),
							mediaItem.getAlbum());

					if (audioItem.equals(mediaAudioItem)) {
						mediaItem.setStreamUrl(ContentUris.withAppendedId(SONG_URI, Long.parseLong(streamID)).toString());

						String thumbnailUrl = mediaItem.getThumbnailUrl();
						Uri thumbnailUri = Uri.parse(thumbnailUrl);
						String thumbnailID = thumbnailUri.getQueryParameter("id");
						if (thumbnailID != null) {
							mediaItem.setThumbnailUrl(ContentUris.withAppendedId(ALBUM_ART_URI,
									Long.parseLong(thumbnailID)).toString());
						}
					}
				}
			}
		}
	}

	public boolean haveNonLocalContents(final Context context, final List<MediaItem> mediaItems) {
		if ((mediaItems == null) || (mediaItems.size() == 0)) {
			return false;
		}
		Map<String, AudioItem> audioItemMap = getAllAudioItems(context);
		if (audioItemMap.size() == 0) {
			//this device does not have any songs.
			return false;
		}

		for (MediaItem mediaItem : mediaItems) {
			String url = mediaItem.getStreamUrl();
			if (url != null) {
				Uri streamUri = Uri.parse(url);
				String streamID = streamUri.getQueryParameter("id");
				if ((streamID != null) && audioItemMap.containsKey(streamID)) {
					AudioItem audioItem = audioItemMap.get(streamID);
					AudioItem mediaAudioItem = new AudioItem(mediaItem.getTitle(), mediaItem.getArtist(),
							mediaItem.getAlbum());

					if (audioItem.equals(mediaAudioItem)) {
						continue;
					}
				}
			}
			return true;
		}
		return false;
	}

	private class AudioInfo {
		long artistID = -1;
		long albumID = -1;
		long genreID = -1;
		int numberOfTracks = 0;
		int numberOfAlbums = 0;

		String artist = null;
		String genre = null;
	}

	private class AudioItem {
		String title;
		String artist;
		String album;

		public AudioItem(final String title, final String artist, final String album) {
			this.title = title;
			this.artist = artist;
			this.album = album;
		}

		@Override
		public boolean equals(Object otherItem) {
			if ((otherItem == null) || !(otherItem instanceof AudioItem)) {
				return false;
			}

			AudioItem otherAudioItem = (AudioItem)otherItem;

			return ((title == null) ?
							(otherAudioItem.title == null) : title.equals(otherAudioItem.title)) &&
					((artist == null) ?
							(otherAudioItem.artist == null) : artist.equals(otherAudioItem.artist)) &&
					((album == null) ?
							(otherAudioItem.album == null) : album.equals(otherAudioItem.album));
		}
	}
}
