/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import static com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState.kPlaying;
import static com.qualcomm.qti.iotcontrollersdk.controller.IoTService.getInstance;

import android.support.annotation.NonNull;
import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTConstants;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import com.qualcomm.qti.iotcontrollersdk.utils.PlayerNullPointerException;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTAllPlayClient;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupMember;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupInfoHelperAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayItemAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayItemInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.SetPositionInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VolumeControlAttr;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.utils.VoiceUINameHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.iotivity.base.OcException;

/**
 * The class provides APIs for the app to access to device group information
 */
public class IoTGroup extends IoTRepository {

  private final static String TAG = "IoTGroup";

  private final IoTGroupState mState = new IoTGroupState();

  private IoTPlaylist mPlaylist;

  private class IoTGroupState {

    private String mGroupId = "";
    private String mGroupName = "";
    private boolean isSinglePlayer;
    private final List<String> mDeviceIds;
    private final List<PlayerGroupMember> mMembers;

    private IoTGroupState() {
      mDeviceIds = new ArrayList<>();
      mMembers = new ArrayList<>();
    }

    synchronized String getGroupId() {
        return mGroupId;
    }

    synchronized void setGroupId(String id) {
      mGroupId = id;
    }

    synchronized String getGroupName() {
        return VoiceUINameHelper.convert2GroupUIName(mGroupName);
    }

    synchronized void setGroupName(String name) {
       mGroupName = name;
    }

    synchronized List<PlayerGroupMember> getMembers() {
      List<PlayerGroupMember> retList = new ArrayList<>();
      for(PlayerGroupMember member : mMembers) {
        retList.add(new PlayerGroupMember(member));
      }
      return retList;
    }

    synchronized void setMembers(List<PlayerGroupMember> members) {
      for (PlayerGroupMember m : members) {
        PlayerGroupMember member = new PlayerGroupMember(m);
        if(!mMembers.contains(member))
          mMembers.add(member);
      }
    }

    synchronized void addMembers(PlayerGroupMember member) {
      if(!mMembers.contains(member))
        mMembers.add(member);
    }

    synchronized void clearDeviceIds() {
      mDeviceIds.clear();
    }

    synchronized void clearMemberList() {
      mMembers.clear();
    }

    synchronized boolean isSinglePlayer() {
      return isSinglePlayer;
    }

    synchronized void setSinglePlayer(boolean singlePlayer) {
      isSinglePlayer = singlePlayer;
    }

    synchronized List<String> getDeviceIds() {
      return mDeviceIds.stream().map(id -> new String(id)).collect(Collectors.toList());
    }

    synchronized void addDeviceId(String id) {
      if(!mDeviceIds.contains(id))
        mDeviceIds.add(id);
    }

    synchronized void removeDeviceId(String id) {
      mDeviceIds.removeIf(devId->devId.equalsIgnoreCase(id));
    }

  }

  public IoTGroup() {
    super(IoTType.GROUP);
    mState.setSinglePlayer(true);
    mPlaylist = new IoTPlaylist(this);
  }

  public IoTGroup(String id) {
    super(IoTType.GROUP);
    mState.setSinglePlayer(false);
    mState.setGroupId(id);
    mPlaylist = new IoTPlaylist(this);
  }

  @Override
  public synchronized String getName() {
    return mState.getGroupName();
  }

  @Override
  public synchronized String getId() {
    return mState.getGroupId();
  }

  @Override
  public IoTType getType() {
    return IoTType.GROUP;
  }

  @Override
  public boolean dispose() {
    return true;
  }

  @Override
  public List<IoTPlayer> getList() {
    return getPlayers();
  }

  @Override
  public int compareTo(@NonNull IoTRepository o) {
    if (o instanceof IoTGroup) {
      if (isSinglePlayer() && ((IoTGroup) o).isSinglePlayer()) {
        getDisplayName().compareTo(((IoTGroup) o).getDisplayName());
      } else if (isSinglePlayer()) {
        return 1;
      } else {
        return -1;
      }
    }
    return -1;
  }

  public String getGroupId() {
    return mState.getGroupId();
  }

  public void setGroupId(String id) {
     mState.setGroupId(id);
  }

  public void setGroupName(String name) {
    mState.setGroupName(name);
  }

  public List<PlayerGroupMember> getMemberList() {
     return mState.getMembers();
  }

  public void addMember(PlayerGroupMember member) {
    mState.addMembers(member);
  }

  public void clearDeviceIds() {
    mState.clearDeviceIds();
  }

  public void clearMemberList() {
    mState.clearMemberList();
  }

  public String getDisplayName() {
     String name = "";
     try {
       if (mState.isSinglePlayer()) {
         String id = mState.getMembers().get(0).deviceId;
         IoTPlayer player= getInstance().getPlayerById(id);
         if(player!= null)
            name = player.getMediaPlayState().getDisplayName();
       } else {
         IoTPlayer player = getLeadPlayer();
         String id = mState.getGroupId();
         if(player == null) {
            List<IoTPlayer> players = IoTService.getInstance().getAllPlayers();
            for(IoTPlayer p : players) {
                GroupInfoHelperAttr attr = p.getGroupInfo().stream().filter(info -> info.mGroupId.equalsIgnoreCase(id))
                .findAny().orElse(null);
                if(attr != null) {
                  name = VoiceUINameHelper.convert2GroupUIName(attr.mGroupName);
                  break;
                }
            }
         } else {
           List<GroupInfoHelperAttr> p = player.getGroupInfo();
           GroupInfoHelperAttr attr = p.stream().filter(info -> info.mGroupId.equalsIgnoreCase(id))
                .findAny().orElse(null);
           if(attr != null) {
              name = VoiceUINameHelper.convert2GroupUIName(attr.mGroupName);
           }
         }
         Log.d(TAG,"Group id:" + id + ", Group name:" +  name);
       }
     } catch (PlayerNullPointerException e) {
      e.printStackTrace();
     }

     return name;
  }

  public void setDisplayName(String name) throws PlayerNullPointerException {
     if(mState.isSinglePlayer()) {
       IoTPlayer player = getLeadPlayer();
       ControllerSdkUtils.checkNotNull(player,"Player == null");
       player.setDisplayName(name);
     } else {
       String id = mState.getGroupId();
       List<IoTPlayer> players = IoTService.getInstance().getAllPlayers();
       for(IoTPlayer player :players) {
         List<GroupInfoHelperAttr> attrs = player.getGroupState().getGroupInfo();
         for (GroupInfoHelperAttr attr : attrs) {
           if (attr.mGroupId.equalsIgnoreCase(id)) {
             attr.mGroupName = name;
             player.setDisplayName(name);
           }
         }
       }
     }
  }

  public boolean isSinglePlayer() {
    return mState.isSinglePlayer();
  }

  public void setSinglePlayer(boolean single) {
     mState.setSinglePlayer(single);
  }

  public int getMaxVolume() {

    if(isSinglePlayer()) {
      IoTPlayer player = getLeadPlayer();
      if(player == null) return 0;
      return player.getMaxVolume();
    } else {
      List<IoTPlayer> players = getPlayers();
      int maxVolume = 0;
      int availablePlayers = 0 ;
      for(IoTPlayer player: players) {

        if(player.getVolumeControlState().isVolumeEnabled()) {
          availablePlayers++;
          maxVolume+= player.getMaxVolume();
        }

        if(availablePlayers > 0)
          maxVolume = maxVolume/availablePlayers;
      }
      return maxVolume;
    }
  }

  private int getMasterVolume() {
    IoTPlayer player = getLeadPlayer();
    if(player != null) {
      return (int)(player.getGroupVolumeState().getVolume() * 100);
    }
    return IoTConstants.INVALID_VALUE;
  }

  public int getMaxMasterVolume() {
    IoTPlayer player = getLeadPlayer();
    if(player != null) {
      return (int)(player.getGroupVolumeState().getMaxVolume() * 100);
    }
    return 0;
  }

  private void setMasterVolume(int volume){
    IoTPlayer player = getLeadPlayer();
    if(player != null) {
      double volumeSet = (double)volume/100.0f;
      player.setGroupVolume(volumeSet,success -> Log.d(TAG,"Set group volume succeed!"));
    }
  }

  public int getVolume() throws PlayerNullPointerException {
    if(isMasterVolumeEnabled()) {
      return getMasterVolume();
    } else if(isSinglePlayer()) {
      IoTPlayer player = getLeadPlayer();
      if(player == null) return 0;
      return player.getVolume();
    } else {
      List<IoTPlayer> players = getPlayers();
      int volume = 0;
      int availablePlayers = 0 ;
      for(IoTPlayer player: players) {

        if(player.getVolumeControlState().isVolumeEnabled()) {
          availablePlayers++;
          volume+= player.getVolume();
        }

        if(availablePlayers > 0)
          volume = volume/availablePlayers;
      }
      return volume;
    }
  }

  public void setVolume(int volume) throws PlayerNullPointerException {
    if(isMasterVolumeEnabled()) {
      Log.d(TAG,"Set group volume!");
      setMasterVolume(volume);
    } else if(isSinglePlayer()) {
      IoTPlayer player = getLeadPlayer();
      if(player == null) return;
      player.setVolume((double)volume/100.0f, success -> {});
    } else {
      Log.d(TAG,"Set volume for each player in a group!");
      List<IoTPlayer> players = getPlayers();
      for(IoTPlayer player: players) {
          int targetVolume = getTargetVolume(player,volume,player.getVolume());
          Log.d(TAG,"Target volume:"+targetVolume);
          if(player != null)
            player.setVolume((double)targetVolume/100.0f, success -> {});
      }
    }
  }

  private int getTargetVolume(IoTPlayer player, int newVolume, int oldVolume) {
    if(newVolume < 1) return 0;

    float ratio;
    int grpMaxVolume = getMaxVolume();
    if(newVolume > oldVolume) {
      ratio = (float)(newVolume - oldVolume)/(float)(grpMaxVolume - oldVolume);
    } else {
      ratio = (float)(newVolume - oldVolume)/(float)(oldVolume);
    }

    if(ratio < 0.01f && ratio > -0.01f) {
      return player.getVolume();
    } else if(ratio <= -1.0f) {
      return 0;
    } else if(ratio >= 1.0f) {
      return player.getMaxVolume();
    } else {
      int playerVolume = player.getVolume();
      int playerMaxVolume = player.getMaxVolume();

      if(ratio > 0.0f) {
        playerVolume += ((player.getMaxVolume() - (int)((float)playerVolume * ratio)));
      } else {
        playerVolume += (int)((float)playerVolume * ratio);
      }
      return (playerVolume < 0) ? 0 : ((playerVolume > playerMaxVolume) ? playerMaxVolume : playerVolume);
    }
  }

  public void setMuted(boolean mute) {

    if(isSinglePlayer()) {
      IoTPlayer player = getLeadPlayer();
      if(player == null) return;
      player.setMuted(mute);
    } else {
      List<IoTPlayer> players = getPlayers();
      for(IoTPlayer player: players) {
          player.setMuted(mute);
      }
    }
  }

  public void addPlayer(IoTPlayer player) {
    String playerId = player.getPlayerId();
    List<String> ids= mState.getDeviceIds();
    if(!ids.contains(playerId))
      mState.addDeviceId(playerId);
  }

  public void removePlayerFromGroup(String id) {
    mState.removeDeviceId(id);
  }

  public List<IoTPlayer> getPlayers() {
    if(mState.isSinglePlayer()) {
      IoTPlayer player = getLeadPlayer();
      List<IoTPlayer> retList = new ArrayList<>();
      if(player != null)
         retList.add(player);
      return retList;
    } else {
      List<IoTPlayer> retList = new ArrayList<>();
      for (String id : mState.getDeviceIds()) {
        List<IoTPlayer> playerList = IoTService.getInstance().getAllPlayers();
        for (IoTPlayer player : playerList) {
          if (player.getPlayerId().equalsIgnoreCase(id))
            retList.add(player);
        }
      }
      return retList;
    }
  }

  public MediaItem getCurrentItem() {
     return mPlaylist.getCurrentItem();
  }

  public int getPlaylistSize() {
     return mPlaylist.size();
  }

  public int getIndexPlaying() {
    return mPlaylist.getIndexPlaying();
  }

  public List<MediaItem>  getPlayItem() {
    return mPlaylist.getPlayItem();
  }

  public IoTError moveMediaItems(int start, int count, int position, final IoTCompletionCallback callback) {
      return mPlaylist.moveMediaItems(start, count, position, callback);
  }

  public IoTError playAtIndex(int index) {
    return mPlaylist.playAtIndex(index);
  }

  public LoopMode getLoopMode() throws PlayerNullPointerException {
    IoTPlayer player = getLeadPlayer();
    if(player == null) return LoopMode.kNone;
    return player.getMediaPlayState().getLoopMode();
  }

  public IoTError removeMediaItems(int start, int count, IoTCompletionCallback callback) {
	  return mPlaylist.removeMediaItems(start, count, success -> {
	      mPlaylist.getMediaItems(callback);
	  });
	}

  public long getPlayPosition() {
    IoTPlayer player = getLeadPlayer();
    if(player == null) return -1L;
    return player.getPlayingPosition();
  }

  public IoTError clearPlaylist(IoTCompletionCallback callback) {
     return mPlaylist.clear(callback);
  }

  /**
	 * Get the zone's current shuffle mode.
	 * @return the shuffle mode
	 */
	public ShuffleMode getShuffleMode() {
	  IoTPlayer player = getLeadPlayer();
	  if(player == null) return ShuffleMode.kLinear;
	  return player.getMediaPlayState().getShuffleMode();
	}

  public IoTError addMediaItemList(final int index, final List<MediaItem> items, final boolean play, final String userData) {
    return mPlaylist.addMediaItemList(index, items, play, userData);
	}

  public void setMembers(List<PlayerGroupMember> members) {
    mState.setMembers(members);
  }

  /**
	 * Check to see if the zone's shuffle control is enabled
	 * @return true if the zone's shuffle control is enabled
	 */
	public boolean isShuffleModeEnabled() {
	  IoTPlayer player = getLeadPlayer();
	  if(player != null)
	    return player.getMediaPlayState().getEnabledControls().mShuffleMode;

	  return false;
	}

	/**
	 * Check to see if the zone's loop control is enabled
	 * @return true if the zone's loop control is enabled
	 */
	public boolean isLoopModeEnabled() {
	  IoTPlayer player = getLeadPlayer();
	  if(player != null)
	    return player.getMediaPlayState().getEnabledControls().mLoopMode;
	  return false;
	}

  public boolean isMasterVolumeMuted() {
    IoTPlayer player = getLeadPlayer();
	  if(player != null)
	    return player.getGroupVolumeState().isMute();
	  return false;
  }

  private boolean isMasterVolumeSupported() {
    IoTPlayer player = getLeadPlayer();
	  if(player != null)
	    return player.getGroupVolumeState().isAvailable();
	  return false;
  }

  public boolean isMasterVolumeEnabled() {
    return !isSinglePlayer() && isMasterVolumeSupported();
  }

  public double masterMaxVolume() {
    return 1.0f;
  }

  public PlayState getPlayerState() {
    IoTPlayer player = getLeadPlayer();
    if(player != null)
      return player.getPlayState();
    else
      return PlayState.kStopped;
  }

  public void updatePlayerState(IoTCompletionCallback callback) {
    IoTPlayer player = getLeadPlayer();
    if(player != null)
      player.updatePlayerState(success -> callback.onCompletion(success));
  }

  public boolean isInterruptible() {
    return true;
  }

  public boolean isPauseEnabled() {
    IoTPlayer player = getLeadPlayer();
    if(player == null || player.getMediaPlayState() == null) return false;
    return player.getMediaPlayState().getEnabledControls().mPause;
  }

  public boolean isNextEnabled() {
    IoTPlayer player = getLeadPlayer();
    if(player != null)
      return player.getMediaPlayState().getEnabledControls().mNext;
    else
      return false;
  }

  public boolean isPreviousEnabled() {
    IoTPlayer player = getLeadPlayer();
    if(player != null)
      return player.getMediaPlayState().getEnabledControls().mPrevious;
    else
      return false;
  }

  public boolean isSeekEnabled() {
    IoTPlayer player = getLeadPlayer();
    if(player != null)
      return player.getMediaPlayState().getEnabledControls().mSeek;
    else
      return false;
  }

  public boolean isPlayItemSupported() {
    return true;
  }

  public boolean isInputSelectorModeSupported() {
      IoTPlayer player = getLeadPlayer();
      if(player != null)
       return player.getInputSelectorNameList().size() > 0;
      else
       return false;
  }

  public void playItem(MediaItem item, IoTCompletionCallback callback) {
    IoTPlayer player = getLeadPlayer();

    if (player != null && item != null) {

      try {
        player.getAllPlayController().postPlayWithCompletion(status -> {
          if (status)
            Log.d(TAG, "Playing success");
          callback.onCompletion(status);
        });
      } catch (OcException e) {
        e.printStackTrace();
      }
      return;
    }
  }

  public void updatePlaylistState(IoTCompletionCallback callback) {
    IoTPlayer player = getLeadPlayer();
    if(player != null) {
      player.updatePlaylistState(callback);
    }
  }

  public IoTPlayer getLeadPlayer() {
    IoTService controller = getInstance();
    if(mState.isSinglePlayer()) {
      String id = mState.getMembers().get(0).deviceId;
      return controller.getPlayerById(id);
    } else {

      for (PlayerGroupMember member : getMemberList()) {
        IoTPlayer player = controller.getPlayerById(member.deviceId);
        if(player != null && player.isSoundBar()) {
          if(player.getPlaylistState().getSize() > 0) {
            return player;
          }
        }
      }

      for (PlayerGroupMember member : getMemberList()) {
        IoTPlayer player = controller.getPlayerById(member.deviceId);
        if(player != null) {
          if(player.getPlaylistState().getSize() > 0) {
            return player;
          }
        }
      }

      for (PlayerGroupMember member : getMemberList()) {
        IoTPlayer player = controller.getPlayerById(member.deviceId);
        if(player != null) {
          if(player.getMediaPlayState().getPlayState().mPlayState == kPlaying) {
            return player;
          }
        }
      }

      for (PlayerGroupMember member : getMemberList()) {
        IoTPlayer player = controller.getPlayerById(member.deviceId);
        if(player != null) {
            return player;
        }
      }
      return null;
    }
  }

  /**
	 * Set the zone's loop mode.
	 * @param loopMode
	 * 				The loop mode to set
	 * @return the error enum
	 */
	public IoTError setLoopMode(LoopMode loopMode) {
    IoTPlayer player = getLeadPlayer();
    if(player == null) return IoTError.INVALID_OBJECT;
    MediaPlayerAttr attr = player.getMediaPlayState().getAttribute();
    attr.mLoopMode = loopMode;
    try {
      boolean result  = player.getAllPlayController().postMediaPlayer(attr, (attribute, status) -> {
        if(status)
          Log.e(TAG, "Set loop mode status:" +status);
      });

      if(!result)
        return IoTError.REQUEST;

    } catch (OcException e) {
      e.printStackTrace();
    }

	  return IoTError.NONE;
	}

  /**
	 * Set the shuffle mode.
	 * @param shuffleMode
	 * 				The shuffle mode to set
	 * @return the error enum
	 */
	public IoTError setShuffleMode(ShuffleMode shuffleMode, IoTCompletionCallback callback) {
	  IoTPlayer player = getLeadPlayer();
    if(player == null) return IoTError.INVALID_OBJECT;

    MediaPlayerAttr attr = player.getMediaPlayState().getAttribute();
    attr.mShuffleMode = shuffleMode;
    try {
      boolean result  = player.getAllPlayController().postMediaPlayer(attr, (attribute, status) -> {
        Log.e(TAG, "Set shuffle mode status:"+status);
        callback.onCompletion(status);
      });

      if(!result)
        return IoTError.REQUEST;

    } catch (OcException e) {
      e.printStackTrace();
    }

	  return IoTError.NONE;
	}

  private void setVolume(double volume, IoTCompletionCallback callback) {

    IoTPlayer player = getLeadPlayer();
    if(player == null) return;

    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    VolumeControlAttr attr = player.getVolumeControlState().getAttribute();
    attr.mVolume = volume;

    try {
      client.postVolumeControl(attr, (attribute,status) -> {
        if(status) {
          Log.d(TAG,String.format("Set volume:%d, status:%b",volume,status));
          callback.onCompletion(status);
        }
      });
    } catch (OcException e) {
      e.printStackTrace();
    }

  }

  private void setMuted(boolean mute, IoTCompletionCallback callback) {

    IoTPlayer player = getLeadPlayer();
    ControllerSdkUtils.checkNotNull(player, "Player == null");

    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    VolumeControlAttr attr = player.getVolumeControlState().getAttribute();
    attr.mMute = mute;

    try {
      client.postVolumeControl(attr, (attribute,status) -> {
        if(status) {
          player.getVolumeControlState().setMute(mute);
          callback.onCompletion(status);
        }
      });
    } catch (OcException e) {
      e.printStackTrace();
    }
  }

  public void setPlayerPosition(int position, final IoTCompletionCallback callback) throws OcException, PlayerNullPointerException {
    IoTPlayer player = getLeadPlayer();
    if(player == null) return;

    SetPositionInAttr attr = new SetPositionInAttr();
    attr.mPositionMsecs = position;

    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    client.postSetPosition(attr, status -> {
      if(status) {
        callback.onCompletion(status);
      }
    });

  }

  public void play(IoTCompletionCallback callback) throws OcException, PlayerNullPointerException {
    IoTPlayer player = getLeadPlayer();
    ControllerSdkUtils.checkNotNull(player, "Player == null");

    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    client.postPlayWithCompletion(status -> {
        Log.d(TAG,"Play "+(status?"Succeed":"Failed")+"!");
        callback.onCompletion(status);
    });
  }

  public void playMediaItem(MediaItem mediaItem, final IoTCompletionCallback callback) throws OcException, PlayerNullPointerException {
    PlayItemInAttr attr = new PlayItemInAttr();
    attr.mItem = new PlayItemAttr();
    attr.mItem.mUrl = mediaItem.getStreamUrl();

    IoTPlayer player = getLeadPlayer();
    if(player == null) return;

    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    client.postPlayItem(attr, status -> callback.onCompletion(status));
  }

  public void pause(IoTCompletionCallback callback) throws OcException {
    final IoTCompletionCallback cb = callback;
    IoTPlayer player = getLeadPlayer();
    if(player ==null) return;
    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    client.postPauseWithCompletion(status -> {
      if (status) {
        if (cb != null)
          cb.onCompletion(status);
      }
    });
  }

  public void stop(IoTCompletionCallback callback) throws OcException {
    IoTPlayer player = getLeadPlayer();
    if(player ==null) return;
    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;
    client.postStopWithCompletion(status -> {
      if (status) {
        callback.onCompletion(status);
      }
    });
  }

  public void next(IoTCompletionCallback callback) throws OcException {
    IoTPlayer player = getLeadPlayer();
    if(player ==null) return;
    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    client.postNextWithCompletion(status -> {
      if (!status) {

      }
      callback.onCompletion(status);

    });
  }

  public void previous(IoTCompletionCallback callback) throws OcException {
    IoTPlayer player = getLeadPlayer();
    if(player ==null) return;
    IoTAllPlayClient client = player.getAllPlayController();
    if(client == null) return;

    client.postPreviousWithCompletion(status -> {
      if (!status) {

      }
      callback.onCompletion(status);
    });
  }


}
