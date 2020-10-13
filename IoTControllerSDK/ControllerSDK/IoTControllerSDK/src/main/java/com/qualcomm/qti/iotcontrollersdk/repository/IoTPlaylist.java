/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.PlaylistListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayIndexInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayItemAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistDeleteInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistInsertInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistMoveInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.QueuedItemAttr;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;

import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MediaPlayerState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.PlaylistState;
import java.util.Arrays;
import java.util.List;

import org.iotivity.base.OcException;

/**
 * Represents a playlist of media items in a group (see {@link MediaItem}).
 */
/*package*/ class IoTPlaylist {

  private static final String TAG = "IoTPlaylist";
  private IoTGroup mPlayerGroup;

	public IoTPlaylist(IoTGroup group) {
	  mPlayerGroup = group;
	}

	/**
	 * Get the number of media items in the playlist.
	 * @return
	 * 				The number of media items
	 */
	public int size() {
	  int size = 0;
	  IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if(player != null)
	    size = player.getPlaylistState().getSize();

	  return size;
	}
	
	/**
	 * Sets the user data
	 * 
	 * @param userData
	 * 				The user data
	 */
	public void setUserData(final String userData) {
	  final IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if( player != null) {
      if (userData.equalsIgnoreCase(player.getPlaylistState().getUserData())) {
        try {
          PlaylistAttr attr = player.getPlaylistState().getAttribute();
          attr.mUserData = userData;
          player.getAllPlayController()
              .postPlaylist(attr, new PlaylistListener() {
                @Override
                public void OnGetPlaylistCompleted(PlaylistAttr attribute, boolean status) {
                  if (status)
                    player.getPlaylistState().setUserData(userData);
                }

                @Override
                public void OnPlaylistCompleted(boolean status) {
                  if (status)
                    player.getPlaylistState().setUserData(userData);
                }
              });
        } catch (OcException e) {
          e.printStackTrace();
        }

      }
    }
	}
	
	/**
	 * Gets the user data
	 * 
	 * @return user data
	 */
	public String getUserData() {
	  String userData = "";
	  IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if( player != null) {
	    userData = player.getPlaylistState().getUserData();
    }
	  return userData;
	}

	/**
	 * Test to see if another playlist is the same as this playlist.
	 * 
	 * @param otherPlaylist
	 * 				The other playlist
	 * @return true if its the same playlist
	 */
	@Override
	public boolean equals(final Object otherPlaylist) {
		if ((otherPlaylist == null) || (!(otherPlaylist instanceof IoTPlaylist))) {
			return false;
		}
		return mPlayerGroup.equals(((IoTPlaylist) otherPlaylist).mPlayerGroup);
	}

  public List<MediaItem> getPlayItem() {
      IoTPlayer player = mPlayerGroup.getLeadPlayer();
      if(player == null) return null;

      return player.getPlaylistState().getPlayItem();
  }

	/**
	 * Get the current media item in the playlist.
	 * @return the current media item in the playlist
	 */
	 MediaItem getCurrentItem() {

      IoTPlayer player = mPlayerGroup.getLeadPlayer();
      if(player == null) {
        return null;
      }

      MediaPlayerState mediaState = player.getMediaPlayState();

      PlayStateAttr playStateAttr = mediaState.getPlayState();
      List<QueuedItemAttr> queueItems = playStateAttr.mQueuedItems;

      if(queueItems != null && queueItems.size() > 0) {
          // always has 1 item in queue??

          if(queueItems.get(0).mIndex > 0) {
            player.getPlaylistState().setIndex(queueItems.get(0).mIndex);
          }

          PlayItemAttr itemAttr = queueItems.get(0).mPlayItem;
          if((itemAttr.mUrl != null && !itemAttr.mUrl.isEmpty()) ||
             (itemAttr.mTitle != null && !itemAttr.mTitle.isEmpty()) ||
             (itemAttr.mThumbnailUrl != null && !itemAttr.mThumbnailUrl.isEmpty()) ||
             (itemAttr.mArtist != null && !itemAttr.mArtist.isEmpty()) ||
             (itemAttr.mGenre != null && !itemAttr.mGenre.isEmpty()) ||
             (itemAttr.mAlbum != null && !itemAttr.mAlbum.isEmpty())) {
            MediaItem mediaItem = new MediaItem();
            mediaItem.setStreamUrl(itemAttr.mUrl);
            mediaItem.setTitle(itemAttr.mTitle);
            mediaItem.setThumbnailUrl(itemAttr.mThumbnailUrl);
            mediaItem.setDuration(itemAttr.mDurationMsecs);
            mediaItem.setArtist(itemAttr.mArtist);
            mediaItem.setGenre(itemAttr.mGenre);
            mediaItem.setAlbum(itemAttr.mAlbum);
            return mediaItem;
          }
      }

      PlaylistState plState = player.getPlaylistState();
      int index = plState.getIndex();
      List<MediaItem> items = plState.getPlayItem();
      if(index >=0 && items != null && index < items.size()) {
        return items.get(index);
      } else{
        return null;
      }
   }


  /**
	 * Get the current media item in the playlist.
	 * @return the current media item in the playlist
	 */
  public boolean getMediaItems(IoTCompletionCallback callback) {
    IoTPlayer player = mPlayerGroup.getLeadPlayer();
    if(player != null) {
      player.updatePlaylistState(success -> {
        if (success) {
          Log.d(TAG, "media items size:" + player.getPlaylistState().getPlayItem().size());
        } else {
          Log.d(TAG, "get media items failed");
        }
        callback.onCompletion(success);
      });
      return true;
    }
    return false;
  }

	/**
	 * Get the index of the currently playing media item in the playlist.
	 * @return the index of the currently playing media item
	 */
	/*package*/int getIndexPlaying() {
	  int idx = -1;
	  IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if(player != null)
	    idx = player.getPlaylistState().getIndex();
	  return idx;
	}



	/**
	 * Play a media item in the current playlist.
	 *
	 * @param index
	 * 				The index of the media item to be played
	 * @return the error enum
	 */
	public IoTError playAtIndex(int index) {
	  IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if(player == null) return IoTError.INVALID_OBJECT;

    PlayIndexInAttr attr = new PlayIndexInAttr();
    attr.mItemIndex = index;
    attr.mStartPositionMsecs = 0;
    try {
      boolean result  = player.getAllPlayController().postPlayIndex(attr, success -> {
        if(success) {
          player.getPlaylistState().setIndex(attr.mItemIndex);
        }
        else {
          Log.e(TAG, "Failed in playing at index:%d" + index);
        }
      });

      if(!result)
        return IoTError.REQUEST;

    } catch (OcException e) {
      e.printStackTrace();
    }

    return IoTError.NONE;
	}

	/**
	 * Play a list of MediaItems.  This overwrites the current playlist
	 * @param items
	 * 				The list of MediaItems
	 * @param startIndex
	 * 				The start index from the list
	 * @param startPosition
	 * 				The start play position
	 * @param pause
	 * 				If the stream needs to be paused
	 * @param loopMode
	 * 				The loop mode
	 * @param shuffleMode
	 * 				The shuffle mode
	 * @param playlistUserData
	 * 				The user data
	 * @return the error enum
	 */
	public IoTError playMediaItemList(List<MediaItem> items, final int startIndex, final int startPosition,
								   final boolean pause, final LoopMode loopMode, final ShuffleMode shuffleMode,
								   final String playlistUserData) {
		return playMediaItemArray(convertToMediaItemArray(items), startIndex, startPosition, pause, loopMode, shuffleMode,
									playlistUserData);
	}

	/**
	 * Clear the playlist.
	 * @return the error enum
	 */
	public IoTError clear(IoTCompletionCallback callback) {
	  IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if( player == null ) return IoTError.INVALID_OBJECT;
    PlaylistDeleteInAttr attr = new PlaylistDeleteInAttr();
    attr.mCount = player.getPlaylistState().getSize();
    attr.mStart = 0;
    attr.mSnapshotId = player.getPlaylistState().getSnapShotId();

    try {
      player.getAllPlayController().postPlaylistDelete(attr, (attribute, success) -> {
         if(success) {
            Log.d(TAG,"Clear play list succeed, new snap shot id:" + attribute.mNewSnapshotId);
            player.getPlaylistState().setSnapShotId(attribute.mNewSnapshotId);
            player.getPlaylistState().clearPlayItems();
            player.getPlaylistState().setIndex(0);
         }
         callback.onCompletion(success);
      });
    } catch (OcException e) {
      e.printStackTrace();
    }
    return IoTError.NONE;
  }

	/**
	 * Add a list of MediaItem
	 * @param index
	 * 				The index to add
	 * @param items
	 * 				The list of MediaItem to add
	 * @param play
	 * 				If true, start the playback at index.  It will start early for large playlists for better user experience
	 * @param playlistUserData
	 * 				The playlist user data
	 * @return the error enum
	 */
	public IoTError addMediaItemList(final int index, final List<MediaItem> items, final boolean play, final String playlistUserData) {
		return addMediaItemArray(index, convertToMediaItemArray(items), play, playlistUserData);
	}

	/**
	 * Remove MediaItems from IoTPlaylist
	 * @param start
	 * 				The start index to remove
	 * @param count
	 * 				The number of items to remove
	 * @return the error enum
	 */
	public IoTError removeMediaItems(final int start, final int count, IoTCompletionCallback callback) {
	  final IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if(player == null) return IoTError.INVALID_OBJECT;

    PlaylistDeleteInAttr attr = new PlaylistDeleteInAttr();
    attr.mStart = start;
    attr.mCount = count;
    attr.mSnapshotId = player.getPlaylistState().getSnapShotId();

    try {

      player.getAllPlayController().postPlaylistDelete(attr,
       (attribute, success) -> {
         Log.d(TAG, String.format("Items[%d] start from %d was removed, response:[count:%d, index:%d]", count,
                 start, attr.mCount, attr.mStart));

         callback.onCompletion(success);
         }
       );
    } catch (OcException e) {
      e.printStackTrace();
      return IoTError.REQUEST;
    }

    return IoTError.NONE;
  }

	/**
	 * Move MediaItems to new position
	 * @param start
	 * 				The start index to move
	 * @param count
	 * 				The number of items to move
	 * @param position
	 * 				The new position to move to
	 * @return the error enum
	 */
	public IoTError moveMediaItems(int start, int count, int position, final IoTCompletionCallback callback){
	  final IoTPlayer player = mPlayerGroup.getLeadPlayer();
	  if(player == null) return IoTError.INVALID_OBJECT;

    PlaylistMoveInAttr attr = new PlaylistMoveInAttr();
    attr.mCount = count;
    attr.mPosition = position;
    attr.mStart = start;
    attr.mSnapshotId = player.getPlaylistState().getSnapShotId();

    try {
      player.getAllPlayController().postPlaylistMove(attr, (attribute, success) -> {
         if(success)
          player.getPlaylistState().setSnapShotId(attribute.mNewSnapshotId);

         callback.onCompletion(success);

      });
    } catch (OcException e) {
      e.printStackTrace();
    }
	  return IoTError.NONE;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			destroy();
		} finally {
			super.finalize();
		}
	}

	static MediaItem[] convertToMediaItemArray(final List<MediaItem> items) {
		MediaItem[] mediaItemsArray = new MediaItem[items.size()];
		int i = 0;
		for (MediaItem item : items) {
			mediaItemsArray[i] = item;
			i++;
		}
		return mediaItemsArray;
	}

	private synchronized  void destroy() {}

  private void postInsertPlaylist(IoTPlayer player, int index, final MediaItem[] mediaItems, boolean isPlay) {
    PlaylistInsertInAttr inAttr = new PlaylistInsertInAttr();
    PlayItemAttr[] playItems = new PlayItemAttr[mediaItems.length];
    int i = 0;
    for (MediaItem item : mediaItems) {
      playItems[i] = new PlayItemAttr();
      playItems[i].mUrl = item.getStreamUrl();
      playItems[i].mTitle = item.getTitle();
      playItems[i].mArtist = item.getArtist();
      playItems[i].mAlbum = item.getAlbum();
      playItems[i].mGenre = item.getGenre();
      playItems[i].mThumbnailUrl = item.getThumbnailUrl();
      playItems[i].mDurationMsecs = item.getDuration();
      i++;
    }

    inAttr.mPosition = index;
    inAttr.mPlaylistItems = Arrays.asList(playItems);
    inAttr.mSnapshotId = player.getPlaylistState().getSnapShotId();

    try {

      int playIndex = index;
      player.getAllPlayController().postPlaylistInsert(inAttr, (attribute, success) -> {
        if (success) {
          Log.d(TAG, "Insert play list size succeed:" + attribute.mCount);
          int size = player.getPlaylistState().getSize();
          player.getPlaylistState().setSize(size + attribute.mCount);
          player.getPlaylistState().setSnapShotId(attribute.mNewSnapshotId);

          List<MediaItem> items = Arrays.asList(mediaItems);

          player.getPlaylistState().setIndex(playIndex);
          Log.d(TAG, "Append new tracks, and play it from:" + playIndex);
          List<MediaItem> curItems = player.getPlaylistState().getPlayItem();
          curItems.addAll(items);
          player.getPlaylistState().setPlayItems(curItems);
          if (isPlay) {
            try {
              PlayIndexInAttr attr = new PlayIndexInAttr();
              attr.mItemIndex = playIndex;
              attr.mStartPositionMsecs = 0;
              player.getAllPlayController().postPlayIndex(attr, completion -> {
                if (completion)
                  Log.d(TAG, "Playing from idx:" + playIndex);
                else
                  Log.e(TAG, "Failing at playing inserted play list");

              });
            } catch (OcException e) {
              e.printStackTrace();
            }
          } else {
            Log.e(TAG, "After inserting, playing ?" + isPlay);
          }

        } else {
          Log.e(TAG, "Insert play list got failed!");
        }
      });
    } catch (OcException e) {
      e.printStackTrace();
    }
  }

	private IoTError addMediaItemArray(final int index, final MediaItem[] mediaItems, final boolean isPlay, String userData) {

    Log.d("TEST","addMediaItemArray index:"+index);

    if(mPlayerGroup == null) return IoTError.NONE;

    final IoTPlayer player = mPlayerGroup.getLeadPlayer();
    if(player == null) return IoTError.INVALID_OBJECT;

    PlaylistAttr playlist = player.getPlaylistState().getAttribute();

    boolean isPostNeeded = false;
    if((playlist.mUserData != null && playlist.mUserData.equalsIgnoreCase(userData))
      || (playlist.mOwnerInfo != null && playlist.mOwnerInfo.equalsIgnoreCase(mPlayerGroup.getDisplayName()))) {
      playlist.mUserData = userData;
      playlist.mOwnerInfo = mPlayerGroup.getDisplayName();
      isPostNeeded = true;
    }

    if(isPostNeeded) {

      try {
        player.getAllPlayController().postPlaylist(playlist, new PlaylistListener() {

          @Override
          public void OnGetPlaylistCompleted(PlaylistAttr attribute, boolean status) {
          }

          @Override
          public void OnPlaylistCompleted(boolean status) {
            if (status) {
              postInsertPlaylist(player,index,mediaItems,isPlay);
            } else {
              Log.e(TAG, "Insert play list get failed!");
            }
          }
        });
      } catch (OcException e) {
        return IoTError.REQUEST;
      }
    } else {
      postInsertPlaylist(player,index,mediaItems,isPlay);
    }

    return IoTError.NONE;
  }

	private IoTError playMediaItemArray(final MediaItem[] mediaItems, final int startIndex, final int startPosition,
											final boolean pause, final LoopMode loopMode, final ShuffleMode shuffleMode,
											final String playlistUserData) {

    if(mPlayerGroup == null || mPlayerGroup.getLeadPlayer() == null)
      return IoTError.INVALID_OBJECT;
    else
      return IoTError.NONE;

  }

}