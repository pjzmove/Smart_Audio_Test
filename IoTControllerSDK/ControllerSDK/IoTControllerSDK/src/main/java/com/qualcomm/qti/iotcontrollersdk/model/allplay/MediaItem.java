/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.model.allplay;

import android.os.Parcel;
import android.os.Parcelable;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayItemAttr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a media item, including its associated metadata.
 */
public class MediaItem implements Parcelable {
	private String mStreamUrl = null;
	private String mMediaType = "audio";
	private String mThumbnailUrl = null;
	private String mTitle = null;
	private String mSubTitle = null;
	private String mArtist = null;
	private String mAlbum = null;
	private String mGenre = null;
	private String mCountry = null;
	private String mChannel = null;
	private String mDescription = null;
	private String mUserData = null;
	private String mContentSource = null;
	private List<String> mCustomHttpHeader = new ArrayList<>();
	
	private int mDuration = 0;

	public static final String REAL_TIME = "realtime";
	public static final String MIME_TYPE = "mimetype";
	final private String SET = "1";
	public static final String ALL = "all";
	
	final private String MIME_TYPE_FORMAT = "audio/l16;rate=%d;channels=%d";
	
	private Map<String, String> mMediumDescriptionMap = new HashMap<>();
	
	public MediaItem() {}

	/**
	 * Initialize a new media item.
	 * 
	 * @param title
	 *            The title of the item
	 * @param streamUrl
	 *            The stream url of the item
	 */
	public MediaItem(final String title, final String streamUrl) {
		setTitle(title);
		setStreamUrl(streamUrl);
	}

  public MediaItem(PlayItemAttr attr) {
        mStreamUrl = attr.mUrl;
        mThumbnailUrl = attr.mThumbnailUrl;
        mArtist = attr.mArtist;
        mAlbum = attr.mAlbum;
        mGenre = attr.mGenre;
        mDuration = attr.mDurationMsecs;
  }

	/**
	 * Copy constructor
	 *
	 * @param mediaItem
	 * 			 The MediaItem to copy
	 */
	public MediaItem(MediaItem mediaItem) {
		mTitle = mediaItem.mTitle;
		mStreamUrl = mediaItem.mStreamUrl;
		mThumbnailUrl = mediaItem.mThumbnailUrl;
		mSubTitle = mediaItem.mSubTitle;
		mArtist = mediaItem.mArtist;
		mAlbum = mediaItem.mAlbum;
		mGenre = mediaItem.mGenre;
		mCountry = mediaItem.mCountry;
		mChannel = mediaItem.mChannel;
		mDescription = mediaItem.mDescription;
		mUserData = mediaItem.mUserData;
		mContentSource = mediaItem.mContentSource;
		mDuration = mediaItem.mDuration;
		mCustomHttpHeader = new ArrayList<>(mediaItem.mCustomHttpHeader);
		mMediumDescriptionMap = new HashMap<>(mediaItem.mMediumDescriptionMap);
	}

	/**
	 * Get the stream URL of the media item.
	 * 
	 * @return the stream URL
	 */
	public String getStreamUrl() {
		return mStreamUrl;
	}

	/**
	 * Set the stream URL of the media item.
	 * @param streamUrl
	 * 				The stream URL to set
	 * @return current media item
	 */
	public MediaItem setStreamUrl(final String streamUrl) {
		mStreamUrl = streamUrl;
		return this;
	}

	/**
	 * Get the title of the media item.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Set the title of the media item.
	 * @param title
	 * 				The title to set
	 * @return current media item
	 */
	public MediaItem setTitle(final String title) {
		mTitle = title;
		return this;
	}

	/**
	 * Get the subtitle of the media item.
	 * 
	 * @return the subtitle
	 */
	public String getSubTitle() {
		return mSubTitle;
	}

	/**
	 * Set the subtitle of the media item.
	 * @param subTitle
	 * 				The subtitle to set
	 * @return current media item
	 */
	public MediaItem setSubTitle(final String subTitle) {
		mSubTitle = subTitle;
		return this;
	}

	/**
	 * Get the artist name of the media item.
	 * 
	 * @return the artist name
	 */
	public String getArtist() {
		return mArtist;
	}

	/**
	 * Set the artist name of the media item.
	 * @param artist
	 * 				The artist name to set
	 * @return current media item
	 */
	public MediaItem setArtist(final String artist) {
		mArtist = artist;
		return this;
	}

	/**
	 * Get the album name of the media item.
	 * 
	 * @return the album name
	 */
	public String getAlbum() {
		return mAlbum;
	}

	/**
	 * Set the album name of the media item.
	 * @param album
	 * 				The album name to set
	 * @return current media item
	 */
	public MediaItem setAlbum(final String album) {
		mAlbum = album;
		return this;
	}

	/**
	 * Get the genre of the media item.
	 * 
	 * @return the genre
	 */
	public String getGenre() {
		return mGenre;
	}

	/**
	 * Set the genre of the media item.
	 * @param genre
	 * 				The genre to set
	 * @return current media item
	 */
	public MediaItem setGenre(final String genre) {
		mGenre = genre;
		return this;
	}

	/**
	 * Get the country of the media item.
	 * 
	 * @return the country
	 */
	public String getCountry() {
		return mCountry;
	}

	/**
	 * Set the country of the media item.
	 * @param country
	 * 				The country to set
	 * @return current media item
	 */
	public MediaItem setCountry(final String country) {
		mCountry = country;
		return this;
	}

	/**
	 * Get the channel of the media item.
	 * 
	 * @return the channel
	 */
	public String getChannel() {
		return mChannel;
	}

	/**
	 * Set the channel of the media item.
	 * @param channel
	 * 				The channel to set
	 * @return current media item
	 */
	public MediaItem setChannel(final String channel) {
		mChannel = channel;
		return this;
	}

	/**
	 * Get the description of the media item.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return mDescription;
	}

	/**
	 * Set the description of the media item.
	 * @param description
	 * 				The description to set
	 * @return current media item
	 */
	public MediaItem setDescription(final String description) {
		mDescription = description;
		return this;
	}

	/**
	 * Get the duration in milliseconds of the media item.
	 * 
	 * @return the duration in milliseconds
	 */
	public int getDuration() {
		return mDuration;
	}

	/**
	 * Set the duration in milliseconds of the media item.
	 * @param duration
	 * 				The duration to set
	 * @return current media item
	 */
	public MediaItem setDuration(final int duration) {
		mDuration = duration;
		return this;
	}

	/**
	 * Get the user data of the media item.
	 * 
	 * @return the user data
	 */
	public String getUserData() {
		return mUserData;
	}

	/**
	 * Set the user data of the media item.
	 * @param userData
	 * 				The user data to set
	 * @return current media item
	 */
	public MediaItem setUserData(final String userData) {
		mUserData = userData;
		return this;
	}

	/**
	 * Get the thumbnail URL of the media item.
	 * 
	 * @return the thumbnail URL
	 */
	public String getThumbnailUrl() {
		return mThumbnailUrl;
	}

	/**
	 * Set the thumbnail URL of the media item.
	 * @param thumbnailUrl
	 * 				The thumbnail URL to set
	 * @return current media item
	 */
	public MediaItem setThumbnailUrl(final String thumbnailUrl) {
		mThumbnailUrl = thumbnailUrl;
		return this;
	}
	
	/**
	 * Get the content source of the media item.
	 * 
	 * @return the content source
	 */
	public String getContentSource() {
		return mContentSource;
	}
	
	/**
	 * Set the content source of the media item.
	 * 
	 * @param contentSource
	 * 				The content source to set
	 * @return current media item
	 */
	public MediaItem setContentSource(final String contentSource) {
		mContentSource = contentSource;
		return this;
	}
	
	/**
	 * Check if the media item is real time streaming
	 * 
	 * @return true if it is real time streaming
	 */
	public boolean isRealTime() {
		ParamStatus status = checkParamStatus(REAL_TIME);
		return (status.exists) ? status.value.equals(SET) : false;
	}

	/**
	 * Add a custom http header
	 *
	 * @param customHttpHeader
	 *           The custom http header
	 * @return current media item
	 */

	public MediaItem addCustomHttpHeader(final String customHttpHeader) {
		if (mCustomHttpHeader != null) {
			mCustomHttpHeader.add(customHttpHeader);
		}
		return this;
	}

	String[] getCustomHttpHeader() {
		String[] customHeaderArray = null;
		if (mCustomHttpHeader != null) {
			customHeaderArray = new String[mCustomHttpHeader.size()];

			int i = 0;
			for (String header : mCustomHttpHeader) {
				customHeaderArray[i] = header;
				i++;
			}
		}
		return customHeaderArray;
	}
	
	/**
	 * Set media item to be real time streaming
	 * 
	 * @param realtime real time streaming
	 * 
	 * @return current media item
	 */
	public MediaItem setRealTime(final boolean realtime) {
		removeMediumDescriptionParam(ALL);
		removeMediumDescriptionParam(REAL_TIME);
		if (realtime) {
			setMediumDescription(REAL_TIME, SET);
		}
		return setAllMediumDescription();
	}
	
	public MediaItem setAudio16MimeType(final int sampleRate, final int numberOfChannels) {
		removeMediumDescriptionParam(ALL);
		removeMediumDescriptionParam(MIME_TYPE);
		if (sampleRate < 0) {
			return this;
		}
		if (numberOfChannels < 0) {
			return this;
		}
		setMediumDescription(MIME_TYPE, String.format(MIME_TYPE_FORMAT, sampleRate, numberOfChannels));
		return setAllMediumDescription();
	}
	
	private void removeMediumDescriptionParam(final String param) {
		mMediumDescriptionMap.remove(param);
	}
	
	private ParamStatus checkParamStatus(final String param) {
		boolean exists = false;
		String value = null;
		if (mMediumDescriptionMap.containsKey(param)) {
			exists = true;
			value = mMediumDescriptionMap.get(param);
		}
		
		return new ParamStatus(exists, value);
	}
	
	private class ParamStatus {
		public boolean exists = false;
		public String value = null;
		
		public ParamStatus(final boolean exists, final String value) {
			this.exists = exists;
			this.value = value;
		}
	}

	private MediaItem setAllMediumDescription() {
		String all = new String();
		if (mMediumDescriptionMap.containsKey(MIME_TYPE)) {
			all = "format=" + mMediumDescriptionMap.get(MIME_TYPE);
		}
		if (mMediumDescriptionMap.containsKey(REAL_TIME)) {
			if (!all.isEmpty()) {
				all += ";";
			}
			all += REAL_TIME + "=" + SET;
		}
		if (!all.isEmpty()) {
			setMediumDescription(ALL, all);
		}
		return this;
	}

	/**
	 * Get media item medium description
	 *
	 * @param key
	 * 			Can be realtime, mimetype and all.  Obsolete key is all, but for backward compatibility.
	 *
	 * @return value of the medium description
	 */
	public String getMediumDescription(final String key) {
		if (mMediumDescriptionMap.containsKey(key)) {
			return mMediumDescriptionMap.get(key);
		}
		return null;
	}

	/**
	 * Set media item medium description
	 *
	 * @param key
	 * 			Can be realtime, mimetype and all.  Obsolete key is all, but for backward compatibility.
	 * @param value
	 * 			value to the medium description
	 *
	 * @return current media item
	 */
	public MediaItem setMediumDescription(final String key, final String value) {
		if ((key == null) || key.isEmpty() || (value == null) || value.isEmpty()) {
			return this;
		}
		mMediumDescriptionMap.put(key, value);
		return this;
	}
	
	/**
	 * Test to see if another media item is the same as this media item.
	 * 
	 * @param otherItem
	 * 				The other media item
	 * @return true if all of the fields are the same
	 */
	@Override
	public boolean equals(Object otherItem) {
		if ((otherItem == null) || !(otherItem instanceof MediaItem)) {
			return false;
		}
		
		MediaItem otherMediaItem = (MediaItem)otherItem;
		
		return ((mStreamUrl == null) ? 
						(otherMediaItem.mStreamUrl == null) : mStreamUrl.equals(otherMediaItem.mStreamUrl)) &&
				((mThumbnailUrl == null) ? 
						(otherMediaItem.mThumbnailUrl == null) : mThumbnailUrl.equals(otherMediaItem.mThumbnailUrl)) &&
				((mTitle == null) ? 
						(otherMediaItem.mTitle == null) : mTitle.equals(otherMediaItem.mTitle)) &&
				((mSubTitle == null) ?
						(otherMediaItem.mSubTitle == null) : mSubTitle.equals(otherMediaItem.mSubTitle)) &&
				((mArtist == null) ?
						(otherMediaItem.mArtist == null) : mArtist.equals(otherMediaItem.mArtist)) &&
				((mAlbum == null) ?
						(otherMediaItem.mAlbum == null) : mAlbum.equals(otherMediaItem.mAlbum)) &&
				((mGenre == null) ?
						(otherMediaItem.mGenre == null) : mGenre.equals(otherMediaItem.mGenre)) &&
				((mCountry == null) ?
						(otherMediaItem.mCountry == null) : mCountry.equals(otherMediaItem.mCountry)) &&
				((mCustomHttpHeader == null) ?
						(otherMediaItem.mCustomHttpHeader == null) : mCustomHttpHeader.equals(otherMediaItem.mCustomHttpHeader)) &&
				((mDescription == null) ?
						(otherMediaItem.mDescription == null) : mDescription.equals(otherMediaItem.mDescription)) &&
				(mDuration == otherMediaItem.mDuration) &&
				((mUserData == null) ?
						(otherMediaItem.mUserData == null) : mUserData.equals(otherMediaItem.mUserData)) &&
				((mContentSource == null) ?
						(otherMediaItem.mContentSource == null) : mContentSource.equals(otherMediaItem.mContentSource)) &&
				(mMediumDescriptionMap.equals(otherMediaItem.mMediumDescriptionMap));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mStreamUrl);
		dest.writeString(mThumbnailUrl);
		dest.writeString(mTitle);
		dest.writeString(mSubTitle);
		dest.writeString(mArtist);
		dest.writeString(mAlbum);
		dest.writeString(mGenre);
		dest.writeString(mCountry);
		dest.writeString(mChannel);
		dest.writeString(mDescription);
		dest.writeString(mUserData);
		dest.writeString(mContentSource);
		dest.writeInt(mDuration);
		dest.writeStringList(mCustomHttpHeader);
		dest.writeInt(mMediumDescriptionMap.size());
		for (final Map.Entry<String, String> entry : mMediumDescriptionMap.entrySet()) {
			dest.writeString(entry.getKey());
			dest.writeString(entry.getValue());
		}
	}
	
	public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() {
		public MediaItem createFromParcel(Parcel in) {
			return new MediaItem(in);
		}
		
		public MediaItem[] newArray(int size) {
			return new MediaItem[size];
		}
	};
	
	protected MediaItem(Parcel in) {
		mStreamUrl = in.readString();
		mThumbnailUrl = in.readString();
		mTitle = in.readString();
		mSubTitle = in.readString();
		mArtist = in.readString();
		mAlbum = in.readString();
		mGenre = in.readString();
		mCountry = in.readString();
		mChannel = in.readString();
		mDescription = in.readString();
		mUserData = in.readString();
		mContentSource = in.readString();
		mDuration = in.readInt();
		in.readStringList(mCustomHttpHeader);
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			mMediumDescriptionMap.put(in.readString(), in.readString());
		}
	}
}