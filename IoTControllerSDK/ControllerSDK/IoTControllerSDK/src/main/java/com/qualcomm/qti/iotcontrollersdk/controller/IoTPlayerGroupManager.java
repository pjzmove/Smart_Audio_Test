/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupMember;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.controller.listeners.IoTAppListener;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manage group information for all types of devices in home network.
 *
 */
/*package*/ class IoTPlayerGroupManager {

  private static final String TAG = "PlayerGrpManager";

  private Map<String,IoTGroup> mGroupsMapping;
  private static IoTPlayerGroupManager mInstance = new IoTPlayerGroupManager();

  /**
   * Get singleton instance
   *
   * @return IoTPlayerGroupManager instance
   */
  protected static IoTPlayerGroupManager getInstance() {
    return mInstance;
  }

  /**
   * Get IoTPlayerGroupManager singleton instance.
   */
  private IoTPlayerGroupManager() {
    mGroupsMapping = new HashMap<>();
  }

  /**
   * Update group info when a new player is found or group info changed
   *
   * @param player is a new found player or from which group info is updating
   * @param client is IoTService instance
   * @param listener is an object who is interested in the event of group info changed
   */

  /*package*/ synchronized void processPlayerUpdate(IoTPlayer player, IoTService client, final IoTAppListener listener) {

      Log.d(TAG,"*** Begin to process a new player *** ");

      Map<String,IoTGroup> newPlayerGroups = new HashMap<>();
      Set<String> groupedPlayerIds = new HashSet<>();
      Set<String> modifiedPlayerGroupIds = new HashSet<>();
      Set<String> existingPlayerGroupIds = new HashSet<>();
      Set<String> membersChangedPlayerGroupIDs = new HashSet<>();

      Map<String,IoTGroup> oldPlayerGroups = new HashMap<>();

      existingPlayerGroupIds.addAll(mGroupsMapping.keySet());
      oldPlayerGroups.putAll(mGroupsMapping);

      List<PlayerGroupInfo> groupInfoList = player.getGroupsInfo().getList();

      for(PlayerGroupInfo groupInfo : groupInfoList) {
          groupedPlayerIds.clear();
          IoTGroup playerGroup = oldPlayerGroups.get(groupInfo.mGroupId);
          boolean groupExists = (playerGroup != null);
          if(!groupExists) {
            playerGroup = new IoTGroup(groupInfo.mGroupId);
          } else {
            playerGroup.setSinglePlayer(false);
          }

          playerGroup.setGroupName(groupInfo.mGroupName);

          // Update device Ids and member list with latest value
          playerGroup.clearDeviceIds();
          playerGroup.clearMemberList();

          List<PlayerGroupMember> newMembers = new ArrayList<>();
          for (PlayerGroupMember memberInfo :groupInfo.mMembers) {
            IoTPlayer newPlayer = client.getPlayerById(memberInfo.deviceId);
            boolean availability = (newPlayer != null);
            boolean licensed = availability && newPlayer.isLicensed();
            PlayerGroupMember member = new PlayerGroupMember(/*memberInfo.host,*/memberInfo.deviceId, memberInfo.displayName,availability,licensed);

            if(player.isLicensed() && availability) {
                newMembers.add(member);
                groupedPlayerIds.add(memberInfo.deviceId);
            }
          }

          if(groupExists && !newMembers.containsAll(playerGroup.getMemberList())) {
            membersChangedPlayerGroupIDs.add(playerGroup.getGroupId());
          }

          // Add new player Ids into the group
          for(String id: groupedPlayerIds) {
            IoTPlayer p = client.getPlayerById(id);
            if( p != null) {
              playerGroup.addPlayer(p);
            }
          }

          playerGroup.setMembers(newMembers);
          newPlayerGroups.put(groupInfo.mGroupId, playerGroup);
      }

      List<IoTPlayer> playersList = client.getAllPlayers();

      Log.d(TAG,"How many IoT players in the list :"+ playersList.size());

      playersList.forEach(iotPlayer -> {

        IoTGroup playerGroup = oldPlayerGroups.get(iotPlayer.getDeviceId());
        boolean playerExisted = (playerGroup != null);
        if(!playerExisted)
          playerGroup = new IoTGroup();

        String playerId = iotPlayer.getPlayerId();
        boolean isInGroup = false;
        for(Map.Entry<String, IoTGroup> entry : newPlayerGroups.entrySet()) {
          List<IoTPlayer> players = entry.getValue().getPlayers();
          for(IoTPlayer ply : players ) {
            if(ply.getPlayerId().equalsIgnoreCase(playerId)) {
              isInGroup = true;
              break;
            }
          }
        }

        if(!isInGroup && !groupedPlayerIds.contains(iotPlayer.getDeviceId())) {
          Log.d(TAG,"Handle a single player in a group:"+ ControllerSdkUtils.stripHostName(iotPlayer.getHostName()) + "," + iotPlayer.getDeviceId() + ", license:" + iotPlayer.isLicensed());
          playerGroup.setGroupId(iotPlayer.getPlayerId());
          PlayerGroupMember member = new PlayerGroupMember(iotPlayer.getDeviceId(),iotPlayer.getName(), true,iotPlayer.isLicensed());
          playerGroup.addMember(member);
          newPlayerGroups.put(iotPlayer.getDeviceId(), playerGroup);
        }

      });

      mGroupsMapping.clear();
      mGroupsMapping = newPlayerGroups;

      Set<String> deletedPlayerGroupIds = new HashSet<>();
      deletedPlayerGroupIds.addAll(existingPlayerGroupIds);
      boolean anyGroupsDeleted = deletedPlayerGroupIds.removeAll(newPlayerGroups.keySet());
      if(anyGroupsDeleted) {
        if(deletedPlayerGroupIds.size() > 0) {
          String playerId = player.getDeviceId();
          playersList.forEach(iter -> {
            Log.d(TAG, "Notify other player to fetch group info!");
            if (!playerId.equalsIgnoreCase(iter.getDeviceId())) {
              iter.updateGroupInfo(1000, false);
            }
          });
        }
        for(String id:deletedPlayerGroupIds) {
           Log.d(TAG,"onPlayerGroupRemoved");
          listener.onPlayerGroupRemoved(oldPlayerGroups.get(id));
        }
      }

      HashSet<String> addedPlayerGroupIDs = new HashSet<>();
      addedPlayerGroupIDs.addAll(newPlayerGroups.keySet());
      addedPlayerGroupIDs.removeAll(existingPlayerGroupIds);

      if(addedPlayerGroupIDs.size() > 0) {
        for(String id:addedPlayerGroupIDs) {
          IoTGroup group = mGroupsMapping.get(id);
          listener.onPlayerGroupAdd(group);
        }
      }

      if(modifiedPlayerGroupIds.size() > 0) {
        for (String id : modifiedPlayerGroupIds) {
          IoTGroup group = mGroupsMapping.get(id);
          listener.onPlayerGroupChanged(group);
        }
      }

      if(membersChangedPlayerGroupIDs.size() > 0) {
        for (String id : membersChangedPlayerGroupIDs) {
          IoTGroup group = mGroupsMapping.get(id);
          listener.onPlayerGroupChanged(group);
        }
      }

      Log.d(TAG,"*** Process a new player end*** ");
  }

  /*package*/ synchronized void processDeadPlayer(IoTPlayer player, IoTAppListener appListener) {
    String id = player.getPlayerId();
    IoTGroup group = mGroupsMapping.get(id);
    if (group != null) {
      Log.d(TAG, "Remove missed player:" + id);
      if(group.isSinglePlayer()) {
        mGroupsMapping.remove(id);
        appListener.onPlayerGroupRemoved(group);
      } else {
        List<PlayerGroupMember> memberList = group.getMemberList();
        for(PlayerGroupMember member : memberList) {
          if(member.deviceId.equalsIgnoreCase(player.getPlayerId()))
            member.available = false;
        }
        group.setMembers(memberList);
        appListener.onPlayerGroupChanged(group);
      }
    }
  }

  /**
   * Get a IoTGroup object by a given device ID
   *
   * @param id is the device ID
   * @return IoTGroup object
   */
  /*package*/ synchronized IoTGroup getGroupById(String id) {
      return mGroupsMapping.get(id);
  }

  /**
   * Remove a IoTGroup object from hash map by a given device ID
   *
   * @param id is the device ID
   */
  public synchronized void delete(String id) {
    if (mGroupsMapping.containsKey(id))
      mGroupsMapping.remove(id);
  }

  /**
   * Return a whole list of available IoTGroup objects
   * @return List<IoTGroup>
   */
  /*package*/ synchronized List<IoTGroup> getAvailableGroups() {
    List<IoTGroup> retList = new ArrayList();
      for(IoTGroup grp : mGroupsMapping.values())
        retList.add(grp);

      return retList;
  }

  /**
   * Search IoTGroup object by a given device ID
   * @return List<IoTGroup>
   */
  /*package*/ synchronized IoTGroup getGroupByPlayerId(String id) {
    IoTGroup group = mGroupsMapping.get(id);
    if (group != null) {
      // single players
      return group;
    } else {
      for (IoTGroup grp : mGroupsMapping.values()) {
        List<IoTPlayer> players = grp.getPlayers();
        for (IoTPlayer player : players) {
          if (player.getPlayerId().equalsIgnoreCase(id))
            return grp;
        }
      }
    }
    return null;
  }

  /**
   * Package group info into PlayerGroupInfo object list
   * @see Class com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupInfo
   * @return List<PlayerGroupInfo>
   *
  /*package*/ synchronized List<PlayerGroupInfo> getPlayersGroupInfo() {

    List<PlayerGroupInfo> retList = new ArrayList<>();
    for(IoTGroup group : mGroupsMapping.values()) {
      PlayerGroupInfo groupInfo = new PlayerGroupInfo(group.getDisplayName(),group.getGroupId(),group.getMemberList(),group.isSinglePlayer());
      retList.add(groupInfo);
    }
    return retList;
  }

  /**
   * Clear the whole list of group info
   */
  public synchronized void clear() {
    mGroupsMapping.clear();
  }

  /**
   * Dispose the whole list of group info, especially use when exiting apps
   */
  protected synchronized void dispose() {
    List<IoTGroup> playerGroupList = getAvailableGroups();
    for(IoTGroup group:playerGroupList) {
      group.dispose();
    }
    mGroupsMapping.clear();
  }


}
