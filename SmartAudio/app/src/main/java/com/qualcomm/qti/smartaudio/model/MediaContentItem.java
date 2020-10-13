/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;

public class MediaContentItem extends ContentItem {
	private static final long serialVersionUID = -2208045556134732532L;

	protected MediaItem mMediaItem = null;
	protected boolean mSupportedFormat = false;

	public MediaContentItem() {
		mMediaItem = new MediaItem();
	}

	public MediaContentItem(final MediaItem mediaItem) {
		mMediaItem = mediaItem;
	}

	public MediaItem getMediaItem() {
		return mMediaItem;
	}

	@Override
	public String getTitle() {
		return mMediaItem.getTitle();
	}

	@Override
	public void setTitle(String title) {
		mMediaItem.setTitle(title);
	}

	@Override
	public ContentItem.ContentType getContentType() {
		return ContentType.MEDIA;
	}

	public String getStreamUrl() {
		return mMediaItem.getStreamUrl();
	}

	public void setStreamUrl(final String streamUrl) {
		mMediaItem.setStreamUrl(streamUrl);
	}

	@Override
	public String getThumbnailUrl() {
		return mMediaItem.getThumbnailUrl();
	}

	@Override
	public void setThumbnailUrl(final String thumbnailUrl) {
		mMediaItem.setThumbnailUrl(thumbnailUrl);
	}

	public String getAlbum() {
		return mMediaItem.getAlbum();
	}

	public void setAlbum(final String album) {
		mMediaItem.setAlbum(album);
	}

	public String getArtist() {
		return mMediaItem.getArtist();
	}

	public void setArtist(final String artist) {
		mMediaItem.setArtist(artist);
	}

	@Override
	public String getSubtitle() {
		return mMediaItem.getSubTitle();
	}

	@Override
	public void setSubtitle(final String subtitle) {
		mMediaItem.setSubTitle(subtitle);
	}

	public int getDuration() {
		return mMediaItem.getDuration();
	}

	public void setDuration(final int duration) {
		mMediaItem.setDuration(duration);
	}

	public boolean isSupportedFormat() {
		return mSupportedFormat;
	}

	public void setSupportedFormat(final boolean isSupported) {
		mSupportedFormat = isSupported;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel destination, final int flag) {
		destination.writeParcelable(mMediaItem, 0);
	}

	public static final Creator<MediaContentItem> CREATOR = new Parcelable.Creator<MediaContentItem>() {
		@Override
		public MediaContentItem createFromParcel(final Parcel in) {
			return new MediaContentItem(in);
		}

		@Override
		public MediaContentItem[] newArray(final int size) {
			return new MediaContentItem[size];
		}
	};

	protected MediaContentItem(final Parcel in) {
		mMediaItem = in.readParcelable(MediaItem.class.getClassLoader());
	}
}
