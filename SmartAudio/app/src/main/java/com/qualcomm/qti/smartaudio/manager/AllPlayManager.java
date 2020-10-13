/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager;

import static com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils.stripHostName;

import android.content.Context;
import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.constants.EffectType;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EnabledControlsAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputSelectorAttr;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.iotcontrollersdk.constants.ConnectionState;
import com.qualcomm.qti.iotcontrollersdk.constants.OnboardingState;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.RemoteOnboardingStatus;
import com.qualcomm.qti.iotcontrollersdk.constants.UpdateStatus;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.UserPassword;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.controller.listeners.IoTAppListener;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer.IoTPlayerUpdatesDelegate;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class AllPlayManager implements IoTAppListener,IoTPlayerUpdatesDelegate {
	private static final String TAG = AllPlayManager.class.getSimpleName();

	private static AllPlayManager mInstance = new AllPlayManager();
	private WeakReference<Context> mContextRef;
  private IoTService mIoTService;

  private IoTGroup mCurrentGroup;

	private final List<OnGroupListChangedListener> mOnGroupListChangedListeners;
	private final List<OnGroupInfoStateChangedListener> mOnGroupInfoStateChangedListeners;
	private final List<OnCurrentGroupSelectedListener> mOnCurrentGroupSelectedListeners;
	private final List<OnCurrentGroupVolumeChangedListener> mOnCurrentGroupVolumeChangedListeners;
	private final List<OnCurrentGroupPlaylistChangedListener> mOnCurrentGroupPlaylistChangedListeners;
	private final List<OnCurrentGroupStateChangedListener> mOnCurrentGroupStateChangedListeners;
	private final List<OnPlayerVolumeChangedListener> mOnPlayerVolumeChangedListeners;
	private final List<OnPlayerInputSelectorChangedListener> mOnPlayerInputSelectorChangedListeners;
	private final List<OnPlayerOutputSelectorChangedListener> mOnPlayerOutputSelectorChangedListeners;
	private final List<OnMediaPlayerDisplayNameChangedListener>  mOnMediaPlayerDisplayNameChangedListeners;
	private final List<OnLeadPlayerChangedListener> mOnLeadPlayerChangedListeners;

	private final List<OnDeviceListChangedListener> mOnDeviceListChangedListeners;
	private final List<OnDeviceStateChangedListener> mOnDeviceStateChangedListeners;
	private final List<OnDeviceUpdateListener> mOnDeviceUpdateListeners;
	private final List<OnDevicePasswordRequestedListener> mOnDevicePasswordRequestedListeners;
	private final List<OnDeviceOnboardingStateChangedListener> mOnDeviceOnboardingStateChangedListeners;
	private final List<OnHomeTheaterChannelChangedListener> mOnHomeTheaterChannelChangedListeners;

	private final List<OnCurrentGroupAudioSettingListener> mOnAudioSettingListeners;
	private final List<OnDeviceRediscoveredListener> mOnDeviceRediscoveredListeners;
	private final List<OnSurroundSpeakerDiscoveredListener> mOnSurroundSpeakerDiscoveredListeners;

	private final AtomicBoolean mGroupOperation = new AtomicBoolean(false);
	private boolean isStarted;


  public static void init(Context context) {
    mInstance.mContextRef = new WeakReference<>(context);
    IoTService.getInstance().setPlayerDelegate(mInstance);
  }

	private AllPlayManager() {
		mOnGroupListChangedListeners = new ArrayList<>();
		mOnGroupInfoStateChangedListeners = new ArrayList<>();
		mOnCurrentGroupSelectedListeners = new ArrayList<>();
		mOnCurrentGroupVolumeChangedListeners = new ArrayList<>();
		mOnMediaPlayerDisplayNameChangedListeners = new ArrayList<>();
		mOnCurrentGroupPlaylistChangedListeners = new ArrayList<>();
		mOnPlayerVolumeChangedListeners = new ArrayList<>();
		mOnCurrentGroupStateChangedListeners = new ArrayList<>();
		mOnPlayerInputSelectorChangedListeners = new ArrayList<>();
		mOnPlayerOutputSelectorChangedListeners = new ArrayList<>();

		mOnDeviceListChangedListeners = new ArrayList<>();
		mOnDeviceStateChangedListeners = new ArrayList<>();
		mOnDeviceUpdateListeners = new ArrayList<>();
		mOnDevicePasswordRequestedListeners = new ArrayList<>();
		mOnDeviceOnboardingStateChangedListeners = new ArrayList<>();
		mOnHomeTheaterChannelChangedListeners = new ArrayList<>();

		mOnAudioSettingListeners = new ArrayList<>();

		mOnDeviceRediscoveredListeners = new ArrayList<>();
		mOnSurroundSpeakerDiscoveredListeners = new ArrayList<>();
		mOnLeadPlayerChangedListeners = new ArrayList<>();

		isStarted = false;
	}

	public static AllPlayManager getInstance() {
	  return mInstance;
	}

	public synchronized void start() {
		if (!isStarted()) {
		  mIoTService = IoTService.getInstance();
		  mIoTService.setAppListener(this);
      mIoTService.start(mContextRef.get());
      isStarted = true;
		}
	}

	public synchronized void stop() {
		if (isStarted()) {
			mIoTService.stopStack();
			isStarted = false;
		}
	}

	public void enableDiscoverSurrounds(boolean enabled) {
	  mIoTService.setSurroundDiscovery(enabled);
	}

	public void clearAllPlayers(IoTCompletionCallback callback) {
	  mIoTService.clearPlayerAndDevices(callback);
	}

	public synchronized boolean isStarted() {
		return isStarted;
	}

	public List<IoTGroup> getGroups() {
		return mIoTService.getAvailableGroups();
	}

	public List<IoTPlayer> getPlayers() {
		return mIoTService.getAllPlayers();
	}

	public List<IoTDevice> getDevices() {
		return mIoTService.getAllDevices();
	}

	public IoTPlayer getPlayer(final String playerId) {
		List<IoTPlayer> players = getPlayers();
    return players.stream().filter(player->player.getPlayerId().equals(playerId))
                                      .findAny().orElse(null);
	}

	public IoTPlayer getPlayerByHostName(String host) {
	  List<IoTPlayer> players = getPlayers();
	  host = stripHostName(host);
    for (int i = 0; i < players.size(); i++) {
      IoTPlayer player = players.get(i);
      String hostName = player.getHostName();
      hostName = stripHostName(hostName);
      if ((player != null) && host.equalsIgnoreCase(hostName)) {
        return player;
      }
    }
    return null;
	}

	public IoTDevice getDevice(String deviceId) {

	  String playerId = getPlayerIdByDeviceId(deviceId);
	  if(playerId == null) return null;

    List<IoTDevice> devices = getDevices();
    IoTDevice device = devices.stream().filter(d -> {
      String id = ControllerSdkUtils.stripHostName(d.getHostName());
      return (id != null && id.equalsIgnoreCase(playerId));
    }).findFirst().orElse(null);
		return device;
	}

	private String getPlayerIdByDeviceId(String deviceId) {
	  String playerId = "";
    List<IoTPlayer> players = getPlayers();
    IoTPlayer player = players.stream().filter(p -> p.getDeviceId().equalsIgnoreCase(deviceId))
        .findFirst().orElse(null);
    if (player != null) {
      playerId = player.getPlayerId();
    }
    return playerId;
	}

	public IoTDevice getDeviceById(String id) {
    IoTDevice retDevice = null;
    for (IoTDevice device : getDevices()) {
      String deviceId = device.getId();
      if ((device.getId() != null) && deviceId.equals(id)) {
        retDevice = device;
        break;
      }
    }
    return retDevice;
	}

	public IoTDevice getDeviceByHostName(String host) {
    IoTDevice retDevice = null;
    for (IoTDevice device : getDevices()) {
      String hostName  = ControllerSdkUtils.stripHostName(host);
      String deviceHostName = ControllerSdkUtils.stripHostName(device.getHostName());
      if (deviceHostName != null && hostName != null && hostName.equalsIgnoreCase(deviceHostName)) {
        retDevice = device;
        break;
      }
    }
    return retDevice;
	}

	public IoTGroup getZone(final String id) {
		final List<IoTGroup> zones = getGroups();
    for (IoTGroup zone : zones) {
      if (zone.getId().equals(id)) {
        return zone;
      }
    }
    return null;
	}

	public IoTGroup getGroupById(String id) {
	  return mIoTService.getGroupById(id);
	}

	public IoTGroup getZone(final IoTPlayer player) {
		final List<IoTGroup> zones = getGroups();
    for (IoTGroup zone : zones) {
      if (zone.getPlayers().contains(player)) {
        return zone;
      }
    }
    return null;
	}

	public IoTGroup getZoneFromPlayerID(final String playerID) {
		List<IoTGroup> zones = getGroups();
    for (IoTGroup zone : zones) {
      if (zone != null) {
        for (IoTPlayer player : zone.getPlayers()) {
          if ((player != null) && (player.getPlayerId() != null) && player.getPlayerId()
              .equals(playerID)) {
            return zone;
          }
        }
      }
    }
    return null;
	}

	private boolean deleteZone(final IoTGroup zone) {
		if (zone == null) {
			return false;
		}

		mGroupOperation.set(true);

		IoTError error = mIoTService.deleteGroup(zone.getGroupId(),success -> {
        mGroupOperation.set(false);
    });

		return (error == IoTError.NONE);
	}

	public boolean groupZone(final IoTGroup zone, final Set<IoTPlayer> playerSet) {
		if ((zone == null) || (playerSet == null)) {
			return false;
		}

		if (playerSet.size() == 0) {
			return deleteZone(zone);
		} else {
			List<IoTPlayer> players = new ArrayList<>();
			players.addAll(playerSet);
			//Collections.sort(players);

			mGroupOperation.set(true);

			IoTError error = IoTError.NONE;//mIoTService.editZone(zone, players);

			mGroupOperation.set(false);

			return (error == IoTError.NONE);
		}
	}

	public void startPlayerScan() {
		//mIoTService.startOnboardingScan();
	}

	public void stopPlayerScan() {
		//mIoTService.stopOnboardingScan();
	}

	private void notifyOnZoneListChanged() {
		synchronized (mOnGroupListChangedListeners) {
			for (OnGroupListChangedListener listener : mOnGroupListChangedListeners) {
				listener.onZoneListChanged();
			}
		}
	}

	private void notifySurroundSpeakerDiscovered(String playerId, String deviceId) {
	  synchronized (mOnSurroundSpeakerDiscoveredListeners) {
			for (OnSurroundSpeakerDiscoveredListener listener : mOnSurroundSpeakerDiscoveredListeners) {
				listener.onSurroundSpeakerDiscovered(playerId, deviceId);
			}
		}
	}

	private void notifyOnZoneStateChanged(IoTGroup zone) {
		if (zone == null) {
			return;
		}

		synchronized (mOnGroupInfoStateChangedListeners) {
			for (OnGroupInfoStateChangedListener listener : mOnGroupInfoStateChangedListeners) {
				listener.onGroupInfoStateChanged();
			}
		}
	}

	private void notifyOnDeviceListChanged() {
		synchronized (mOnDeviceListChangedListeners) {
			for (OnDeviceListChangedListener listener : mOnDeviceListChangedListeners) {
				listener.onDeviceListChanged();
			}
		}
	}

	private void notifyOnDeviceStateChanged(IoTDevice device) {
		if (device == null) {
			return;
		}

		synchronized (mOnDeviceStateChangedListeners) {
			for (OnDeviceStateChangedListener listener : mOnDeviceStateChangedListeners) {
				listener.onDeviceStateChanged(device);
			}
		}
	}

	public IoTGroup getCurrentGroup() {
		synchronized (this) {
			return mCurrentGroup;
		}
	}

	public void setCurrentZone(final IoTGroup zone) {
		synchronized (this) {
			mCurrentGroup = zone;
		}

		synchronized (mOnCurrentGroupSelectedListeners) {
			for (OnCurrentGroupSelectedListener listener : mOnCurrentGroupSelectedListeners) {
				if (listener != null) {
					if (zone == null) {
						listener.onCurrentZoneRemoved();
					} else {
						listener.onCurrentZoneSelected(zone);
					}
				}
			}
		}
	}

	public List<IoTPlayer> getHomeTheaterChannelSupportedSpeakers() {
		List<IoTPlayer> satelliteSpeakers = new ArrayList<>();
    List<IoTPlayer> players = getPlayers();
    for (int i = 0; i < players.size(); i++) {
      final IoTPlayer player = players.get(i);
      if (player != null && player.isHomeTheaterChannelSupported()) {
        satelliteSpeakers.add(player);
      }
    }
    return satelliteSpeakers;
	}

	public boolean manipulateZone(final IoTGroup selectedZone, final Set<IoTPlayer> selectedPlayers) {
		if ((selectedZone == null) || (selectedPlayers == null)) {
			return false;
		}

		if (selectedPlayers.size() == 0) {
			return deleteZone(selectedZone);
		} else {
			List<IoTPlayer> groupedPlayers = new ArrayList<>();
			groupedPlayers.addAll(selectedPlayers);
			//Collections.sort(groupedPlayers);

			mGroupOperation.set(true);

			boolean setZone = selectedZone.equals(mCurrentGroup);
			if (!setZone) {
				if ((mCurrentGroup != null) && selectedPlayers.containsAll(mCurrentGroup.getPlayers())) {
					setZone = true;
					setCurrentZone(null);
				}
			}

			IoTError error = IoTError.NONE;//mIoTService.editZone(selectedZone, groupedPlayers);
			if (IoTError.NONE.equals(error)) {
				Log.v(TAG, "manipulateZone() success");
				List<IoTGroup> availableZones = mIoTService.getAvailableGroups();
				for (IoTGroup zone : availableZones) {
					if (groupedPlayers.containsAll(zone.getPlayers())) {
						if (setZone) {
							setCurrentZone(zone);
						}
						break;
					}
				}
			} else {
				Log.e(TAG, "manipulateZone() error: " + error);
			}
			mGroupOperation.set(false);
			return true;
		}
	}

	public String getPlayerName(final IoTPlayer player) {
		String playerName = player.getName();
		if (player.isSoundBar()) {
			String channels = new String();
			if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.LEFT_SURROUND)) {
				channels += mContextRef.get().getString(R.string.ls);
			}
			if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.RIGHT_SURROUND)) {
				if (channels.length() > 0) {
					channels += " + ";
				}
				channels += mContextRef.get().getString(R.string.rs);
			}
			if (player.isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel.SUBWOOFER)) {
				if (channels.length() > 0) {
					channels += " + ";
				}
				channels += mContextRef.get().getString(R.string.sub);
			}
			if (channels.length() > 0) {
				channels = " (" + channels + ")";
				playerName += channels;
			}
		}
		return playerName;
	}

	public void addOnZoneListChangedListener(final OnGroupListChangedListener listener) {
		synchronized (mOnGroupListChangedListeners) {
			if ((listener != null) && !mOnGroupListChangedListeners.contains(listener)) {
				mOnGroupListChangedListeners.add(listener);
			}
		}
	}

	public void removeOnZoneListChangedListener(final OnGroupListChangedListener listener) {
		synchronized (mOnGroupListChangedListeners) {
			if (listener != null) {
				mOnGroupListChangedListeners.remove(listener);
			}
		}
	}

	public void addOnZoneStateChangedListener(final OnGroupInfoStateChangedListener listener) {
		synchronized (mOnGroupInfoStateChangedListeners) {
			if ((listener != null) && !mOnGroupInfoStateChangedListeners.contains(listener)) {
				mOnGroupInfoStateChangedListeners.add(listener);
			}
		}
	}

	public void removeOnZoneStateChangedListener(final OnGroupInfoStateChangedListener listener) {
		synchronized (mOnGroupInfoStateChangedListeners) {
			if (listener != null) {
				mOnGroupInfoStateChangedListeners.remove(listener);
			}
		}
	}

	public void addOnCurrentZoneSelectedListener(final OnCurrentGroupSelectedListener listener) {
		synchronized (mOnCurrentGroupSelectedListeners) {
			if ((listener != null) && !mOnCurrentGroupSelectedListeners.contains(listener)) {
				mOnCurrentGroupSelectedListeners.add(listener);
			}
		}
	}

	public void removeOnCurrentZoneSelectedListener(final OnCurrentGroupSelectedListener listener) {
		synchronized (mOnCurrentGroupSelectedListeners) {
			if (listener != null) {
				mOnCurrentGroupSelectedListeners.remove(listener);
			}
		}
	}

	public void addOnCurrentZoneVolumeChangedListener(final OnCurrentGroupVolumeChangedListener listener) {
		synchronized (mOnCurrentGroupVolumeChangedListeners) {
			if ((listener != null) && !mOnCurrentGroupVolumeChangedListeners.contains(listener)) {
				mOnCurrentGroupVolumeChangedListeners.add(listener);
			}
		}
	}

  public void removeOnCurrentZoneVolumeChangedListener(final OnCurrentGroupVolumeChangedListener listener) {
		synchronized (mOnCurrentGroupVolumeChangedListeners) {
			if (listener != null) {
				mOnCurrentGroupVolumeChangedListeners.remove(listener);
			}
		}
	}

  public void registerDeviceRediscoveredListener(OnDeviceRediscoveredListener listener) {
    synchronized (mOnDeviceRediscoveredListeners) {
      if (listener != null && !mOnDeviceRediscoveredListeners.contains(listener)) {
        mOnDeviceRediscoveredListeners.add(listener);
      }
    }
  }

  public void unRegisterDeviceRediscoveredListener(OnDeviceRediscoveredListener listener) {
    synchronized (mOnDeviceRediscoveredListeners) {
			if (listener != null) {
				mOnDeviceRediscoveredListeners.remove(listener);
			}
		}
  }

  public void registerSurroundDiscoveryListener(OnSurroundSpeakerDiscoveredListener listener) {
    synchronized (mOnSurroundSpeakerDiscoveredListeners) {
      if (listener != null && !mOnSurroundSpeakerDiscoveredListeners.contains(listener)) {
        mOnSurroundSpeakerDiscoveredListeners.add(listener);
      }
    }
  }

  public void unRegisterSurroundDiscoveryListener(OnSurroundSpeakerDiscoveredListener listener) {
    synchronized (mOnSurroundSpeakerDiscoveredListeners) {
			if (listener != null) {
				mOnSurroundSpeakerDiscoveredListeners.remove(listener);
			}
		}
  }


	public void addOnCurrentZonePlaylistChangedListener(final OnCurrentGroupPlaylistChangedListener listener) {
		synchronized (mOnCurrentGroupPlaylistChangedListeners) {
			if ((listener != null) && !mOnCurrentGroupPlaylistChangedListeners.contains(listener)) {
				mOnCurrentGroupPlaylistChangedListeners.add(listener);
			}
		}
	}

	public void removeOnCurrentZonePlaylistChangedListener(final OnCurrentGroupPlaylistChangedListener listener) {
		synchronized (mOnCurrentGroupPlaylistChangedListeners) {
			if (listener != null) {
				mOnCurrentGroupPlaylistChangedListeners.remove(listener);
			}
		}
	}

	public void addOnPlayerVolumeChangedListener(final OnPlayerVolumeChangedListener listener) {
		synchronized (mOnPlayerVolumeChangedListeners) {
			if ((listener != null) && !mOnPlayerVolumeChangedListeners.contains(listener)) {
				mOnPlayerVolumeChangedListeners.add(listener);
			}
		}
	}

	public void removeOnPlayerVolumeChangedListener(final OnPlayerVolumeChangedListener listener) {
		synchronized (mOnPlayerVolumeChangedListeners) {
			if (listener != null) {
				mOnPlayerVolumeChangedListeners.remove(listener);
			}
		}
	}

	public void addOnPlayerInputSelectorChangedListener(final OnPlayerInputSelectorChangedListener listener) {
		synchronized (mOnPlayerInputSelectorChangedListeners) {
			if ((listener != null) && !mOnPlayerInputSelectorChangedListeners.contains(listener)) {
				mOnPlayerInputSelectorChangedListeners.add(listener);
			}
		}
	}

	public void removeOnPlayerInputSelectorChangedListener(final OnPlayerInputSelectorChangedListener listener) {
		synchronized (mOnPlayerInputSelectorChangedListeners) {
			if (listener != null) {
				mOnPlayerInputSelectorChangedListeners.remove(listener);
			}
		}
	}

	public void addOnPlayerOutputSelectorChangedListener(final OnPlayerOutputSelectorChangedListener listener) {
		synchronized (mOnPlayerOutputSelectorChangedListeners) {
			if ((listener != null) && !mOnPlayerOutputSelectorChangedListeners.contains(listener)) {
				mOnPlayerOutputSelectorChangedListeners.add(listener);
			}
		}
	}

	public void removeOnPlayerOutputSelectorChangedListener(final OnPlayerOutputSelectorChangedListener listener) {
		synchronized (mOnPlayerOutputSelectorChangedListeners) {
			if (listener != null) {
				mOnPlayerOutputSelectorChangedListeners.remove(listener);
			}
		}
	}

	public void addOnCurrentZoneStateChangedListener(final OnCurrentGroupStateChangedListener listener) {
		synchronized (mOnCurrentGroupStateChangedListeners) {
			if ((listener != null) && !mOnCurrentGroupStateChangedListeners.contains(listener)) {
				mOnCurrentGroupStateChangedListeners.add(listener);
			}
		}
	}

	public void removeOnCurrentZoneStateChangedListener(final OnCurrentGroupStateChangedListener listener) {
		synchronized (mOnCurrentGroupStateChangedListeners) {
			if (listener != null) {
				mOnCurrentGroupStateChangedListeners.remove(listener);
			}
		}
	}

	public void addOnDeviceListChangedListener(final OnDeviceListChangedListener listener) {
		synchronized (mOnDeviceListChangedListeners) {
			if ((listener != null) && !mOnDeviceListChangedListeners.contains(listener)) {
				mOnDeviceListChangedListeners.add(listener);
			}
		}
	}

	public void removeOnDeviceListChangedListener(final OnDeviceListChangedListener listener) {
		synchronized (mOnDeviceListChangedListeners) {
			if (listener != null) {
				mOnDeviceListChangedListeners.remove(listener);
			}
		}
	}

	public void addOnDeviceStateChangedListener(final OnDeviceStateChangedListener listener) {
		synchronized (mOnDeviceStateChangedListeners) {
			if ((listener != null) && !mOnDeviceStateChangedListeners.contains(listener)) {
				mOnDeviceStateChangedListeners.add(listener);
			}
		}
	}

	public void addOnCurrentGroupAudioSettingListener(OnCurrentGroupAudioSettingListener listener) {
	  synchronized (mOnAudioSettingListeners) {
			if ((listener != null) && !mOnAudioSettingListeners.contains(listener)) {
				mOnAudioSettingListeners.add(listener);
			}
		}
	}

  public void removeOnCurrentGroupAudioSettingListener(OnCurrentGroupAudioSettingListener listener) {
    synchronized (mOnAudioSettingListeners) {
      if (listener != null) {
        mOnAudioSettingListeners.remove(listener);
      }
    }
  }

	public void removeOnDeviceStateChangedListener(final OnDeviceStateChangedListener listener) {
		synchronized (mOnDeviceStateChangedListeners) {
			if (listener != null) {
				mOnDeviceStateChangedListeners.remove(listener);
			}
		}
	}

	public void addOnDeviceUpdateListener(final OnDeviceUpdateListener listener) {
		synchronized (mOnDeviceUpdateListeners) {
			if ((listener != null) && !mOnDeviceUpdateListeners.contains(listener)) {
				mOnDeviceUpdateListeners.add(listener);
			}
		}
	}

	public void removeOnDeviceUpdateListener(final OnDeviceUpdateListener listener) {
		synchronized (mOnDeviceUpdateListeners) {
			if (listener != null) {
				mOnDeviceUpdateListeners.remove(listener);
			}
		}
	}

	public void addOnDevicePasswordRequestedListener(final OnDevicePasswordRequestedListener listener) {
		synchronized (mOnDevicePasswordRequestedListeners) {
			if ((listener != null) && !mOnDevicePasswordRequestedListeners.contains(listener)) {
				mOnDevicePasswordRequestedListeners.add(listener);
			}
		}
	}

	public void removeOnDevicePasswordRequestedListener(final OnDevicePasswordRequestedListener listener) {
		synchronized (mOnDevicePasswordRequestedListeners) {
			if (listener != null) {
				mOnDevicePasswordRequestedListeners.remove(listener);
			}
		}
	}

	public void addOnDeviceOnboardingStateChangedListener(final OnDeviceOnboardingStateChangedListener listener) {
		synchronized (mOnDeviceOnboardingStateChangedListeners) {
			if ((listener != null) && !mOnDeviceOnboardingStateChangedListeners.contains(listener)) {
				mOnDeviceOnboardingStateChangedListeners.add(listener);
			}
		}
	}

	public void removeOnDeviceOnboardingStateChangedListener(final OnDeviceOnboardingStateChangedListener listener) {
		synchronized (mOnDeviceOnboardingStateChangedListeners) {
			if (listener != null) {
				mOnDeviceOnboardingStateChangedListeners.remove(listener);
			}
		}
	}

	public void addOnHomeTheaterChannelChangedListener(final OnHomeTheaterChannelChangedListener listener) {
		synchronized (mOnHomeTheaterChannelChangedListeners) {
			if (listener != null) {
				mOnHomeTheaterChannelChangedListeners.add(listener);
			}
		}
	}

	public void removeOnHomeTheaterChannelChangedListener(final OnHomeTheaterChannelChangedListener listener) {
		synchronized (mOnHomeTheaterChannelChangedListeners) {
			if (listener != null) {
				mOnHomeTheaterChannelChangedListeners.remove(listener);
			}
		}
	}


	public interface OnGroupListChangedListener {
		void onZoneListChanged();
	}

	public interface OnGroupInfoStateChangedListener {
		void onGroupInfoStateChanged();
	}

	public interface OnCurrentGroupSelectedListener {
		void onCurrentZoneSelected(final IoTGroup zone);
		void onCurrentZoneRemoved();
	}

  public interface OnLeadPlayerChangedListener {
    void onLeadPlayerChanged(IoTPlayer player);
  }

	public interface OnCurrentGroupAudioSettingListener {
	  void onCurrentGroupAudioEffectChanged(IoTPlayer player);
	  void onCurrentGroupPresetReverbChanged(IoTPlayer player);
	  void onCurrentGroupBassboostChanged(IoTPlayer player);
	  void onCurrentGroupEqualizerChanged(IoTPlayer player);
	  void onCurrentGroupVirtualXChanged(IoTPlayer player);
	  void onCurrentGroupDolbyChanged(IoTPlayer player);
	  void onCurrentGroupTrumpetChanged(IoTPlayer player);
	}

  public interface OnMediaPlayerDisplayNameChangedListener {
    void onPlayerDisplayerNameChanged(IoTPlayer player, String name);
  }

	public interface OnCurrentGroupVolumeChangedListener {
		void onCurrentGroupVolumeStateChanged(int volume, boolean user);
		void onCurrentZoneVolumeEnabledChanged(boolean enabled);
		void onCurrentZoneMuteStateChanged(boolean muted);
	}

	public interface OnCurrentGroupPlaylistChangedListener {
		void onCurrentZonePlaylistChanged();
		default void onCurrentGroupEnabledControlsChanged(EnabledControlsAttr attr){}
		default void onCurrentGroupLoopModeChanged(LoopMode loopMode){}
		default void onCurrentGroupShuffleModeChanged(ShuffleMode shuffleMode){}

	}

	public interface OnCurrentGroupStateChangedListener {
		void OnCurrentGroupStateChanged();
	}

	public interface OnPlayerVolumeChangedListener {
		void onPlayerVolumeStateChanged(IoTPlayer player, int volume, boolean user);
		void onPlayerVolumeEnabledChanged(IoTPlayer player, boolean enabled);
		void onPlayerMuteStateChanged(IoTPlayer player, boolean muted);
	}

	public interface OnPlayerInputSelectorChangedListener {
		void onPlayerInputSelectorChanged(IoTPlayer player,  InputSelectorAttr input);
	}

	public interface OnPlayerOutputSelectorChangedListener {
		void onPlayerOutputSelectorChanged(IoTPlayer player);
	}

	public interface OnDeviceListChangedListener {
		void onDeviceListChanged();
	}

	public interface OnDeviceStateChangedListener {
		void onDeviceStateChanged(IoTDevice device);
		void onDeviceConnectionStateChanged(IoTDevice device, ConnectionState connectionState);
	}

	public interface OnDeviceUpdateListener {
		void onDeviceAutoUpdateChanged(IoTDevice device, boolean autoUpdate);
		void onDeviceUpdateAvailable(IoTDevice device);
		void onDeviceUpdateStatusChanged(IoTDevice device, UpdateStatus updateStatus);
		void onDeviceUpdateProgressChanged(IoTDevice device, double progress);
		void onDeviceUpdatePhysicalRebootRequired(IoTDevice device);
	}

	public interface OnDevicePasswordRequestedListener {
		UserPassword onDevicePasswordRequested(IoTDevice device);
	}

	public interface OnDeviceOnboardingStateChangedListener {
		void onOnboardingStateChanged(String deviceID, OnboardingState onboardingState);
		void onDeviceRemoteOnboardingStatusChanged(IoTDevice device, RemoteOnboardingStatus remoteOnboardingStatus);
	}

  public interface OnDeviceRediscoveredListener {
    void onDeviceRediscovered();
  }

  public interface OnSurroundSpeakerDiscoveredListener {
    void onSurroundSpeakerDiscovered(String playerId, String deviceId);
  }

	public interface OnHomeTheaterChannelChangedListener {
		void onHomeTheaterChannelUpdate(IoTPlayer player, HomeTheaterChannelMap channelMap);
		default void onHomeTheaterChannelPlayerInfoAvailable(IoTPlayer player, HomeTheaterChannel channel, boolean available){}
		default void onHomeTheaterChannelDeviceInfoAvailable(IoTPlayer player, HomeTheaterChannel channel, boolean available){}
		default void onHomeTheaterChannelVolumeChanged(final IoTPlayer player, final HomeTheaterChannel channel, final int volume, final boolean user){}
		default void onHomeTheaterChannelFirmwareAutoUpdateChanged(final IoTPlayer player, final HomeTheaterChannel channel, final boolean autoUpdate){}
		default void onHomeTheaterChannelFirmwareUpdateAvailable(final IoTPlayer player, final HomeTheaterChannel channel){}
		default void onHomeTheaterChannelFirmwareUpdateStatusChanged(final IoTPlayer player, final HomeTheaterChannel channel, final UpdateStatus updateStatus){}
		default void onHomeTheaterChannelFirmwareUpdateProgressChanged(final IoTPlayer player, final HomeTheaterChannel channel, final double progress){}
	}

  /**
   * The followings implement {@see IoTAppListener} interface
   */

  @Override
  public void onDeviceAdded(IoTDevice device) {

  }

  @Override
  public void onPlayerGroupAdd(IoTGroup player) {
    notifyOnZoneListChanged();
  }

  @Override
  public void onPlayerGroupRemoved(IoTGroup playerGroup) {
      notifyOnZoneListChanged();
  }

  @Override
  public void onPlayerGroupChanged(IoTGroup playerGroup) {
    notifyOnZoneListChanged();
  }

  @Override
  public void onSurroundPlayerDiscovered(String playerId, String deviceId) {
    notifySurroundSpeakerDiscovered(playerId, deviceId);
  }

  /**
   * The followings implement {see @IoTPlayerUpdatesDelegate} interface
   */

  @Override
  public void didHavePlaybackError() {

  }

  @Override
  public void didChangeEnabledControls(EnabledControlsAttr attr, IoTPlayer player) {
    //Check if player is in current group
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnCurrentGroupPlaylistChangedListener listener : mOnCurrentGroupPlaylistChangedListeners) {
        listener.onCurrentGroupEnabledControlsChanged(attr);
      }
    }
  }



  @Override
  public void didChangeLoopMode(LoopMode loopMode, IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      synchronized (mOnCurrentGroupPlaylistChangedListeners) {
        for (OnCurrentGroupPlaylistChangedListener listener : mOnCurrentGroupPlaylistChangedListeners) {
          listener.onCurrentGroupLoopModeChanged(loopMode);
        }
      }
    }
  }

  @Override
  public void didChangeShuffleMode(ShuffleMode shuffleMode, IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      synchronized (mOnCurrentGroupPlaylistChangedListeners) {
        for (OnCurrentGroupPlaylistChangedListener listener : mOnCurrentGroupPlaylistChangedListeners) {
          listener.onCurrentGroupShuffleModeChanged(shuffleMode);
        }
      }
    }
  }

  @Override
  public void didChangePlayState(IoTPlayer player) {

    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if (group == null || mCurrentGroup == null)
      return;

    if (group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      synchronized (mOnCurrentGroupStateChangedListeners) {
        for (OnCurrentGroupStateChangedListener listener : mOnCurrentGroupStateChangedListeners) {
          listener.OnCurrentGroupStateChanged();
        }
      }
    }
  }

  @Override
  public void didChangeDisplayName(String name,IoTPlayer player) {
    synchronized (mOnMediaPlayerDisplayNameChangedListeners) {
      for (OnMediaPlayerDisplayNameChangedListener listener : mOnMediaPlayerDisplayNameChangedListeners) {
        listener.onPlayerDisplayerNameChanged(player, name);
      }
    }
  }

  @Override
  public void didChangeVolume(double volume,IoTPlayer player) {
    synchronized (mOnPlayerVolumeChangedListeners) {
      for (OnPlayerVolumeChangedListener listener : mOnPlayerVolumeChangedListeners)
        listener.onPlayerVolumeStateChanged(player, (int) (volume * 100), false);
    }
  }

  @Override
  public void didChangeVolumeEnabled(boolean enabled, IoTPlayer player) {
    synchronized (mOnPlayerVolumeChangedListeners) {
      for(OnPlayerVolumeChangedListener listener : mOnPlayerVolumeChangedListeners)
				listener.onPlayerVolumeEnabledChanged(player,enabled);
		}
  }

  @Override
  public void didChangeMuteState(boolean enabled,IoTPlayer player) {

    synchronized (mOnPlayerVolumeChangedListeners) {
      for (OnPlayerVolumeChangedListener listener : mOnPlayerVolumeChangedListeners)
        listener.onPlayerMuteStateChanged(player, enabled);
    }
  }

  @Override
  public void didChangeGroupInfo(IoTPlayer player) {
    synchronized (mOnGroupInfoStateChangedListeners) {
      for (OnGroupInfoStateChangedListener listener : mOnGroupInfoStateChangedListeners) {
        listener.onGroupInfoStateChanged();
      }
    }
  }

  @Override
  public void didChangeLeadPlayer(IoTPlayer player) {
    synchronized (mOnLeadPlayerChangedListeners) {
      for (OnLeadPlayerChangedListener listener : mOnLeadPlayerChangedListeners) {
        listener.onLeadPlayerChanged(player);
      }
    }
  }

  @Override
  public void didChangeInputSelector(InputSelectorAttr attr, IoTPlayer player) {
    for(OnPlayerInputSelectorChangedListener listener: mOnPlayerInputSelectorChangedListeners) {
        listener.onPlayerInputSelectorChanged(player, attr);
    }
  }

  @Override
  public void didChangeOutputSelector(IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnPlayerOutputSelectorChangedListener listener : mOnPlayerOutputSelectorChangedListeners) {
        listener.onPlayerOutputSelectorChanged(player);
      }
    }
  }

  @Override
  public void didChangePlaylist(IoTPlayer player) {

    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnCurrentGroupPlaylistChangedListener listener : mOnCurrentGroupPlaylistChangedListeners) {
        listener.onCurrentZonePlaylistChanged();
      }
    }
  }

  @Override
  public void didChangeAudioConfiguration(IoTPlayer player, EffectType type) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {

      for (OnCurrentGroupAudioSettingListener listener : mOnAudioSettingListeners) {
        switch (type) {
          case DOLBY:
            listener.onCurrentGroupDolbyChanged(player);
            break;
          case TRUMPET:
            listener.onCurrentGroupTrumpetChanged(player);
            break;
          case DTS:
            listener.onCurrentGroupVirtualXChanged(player);
            break;
          case AudioEffects:
            listener.onCurrentGroupAudioEffectChanged(player);
            break;
        }
      }
    }
  }

  @Override
  public void didChangeBassboost(IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnCurrentGroupAudioSettingListener listener : mOnAudioSettingListeners) {
        listener.onCurrentGroupBassboostChanged(player);
      }
    }
  }

  @Override
  public void didChangeEqualizer(IoTPlayer player) {
     IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
     if(group == null || mCurrentGroup == null) return;

     if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnCurrentGroupAudioSettingListener listener : mOnAudioSettingListeners) {
        listener.onCurrentGroupEqualizerChanged(player);
      }
    }
  }

  @Override
  public void didChangePresetReverb(IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnCurrentGroupAudioSettingListener listener : mOnAudioSettingListeners) {
        listener.onCurrentGroupPresetReverbChanged(player);
      }
    }
  }

  @Override
  public void didChangeMultiChannelMain(IoTPlayer player, HomeTheaterChannelMap channelMap) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      for (OnHomeTheaterChannelChangedListener listener : mOnHomeTheaterChannelChangedListeners) {
        listener.onHomeTheaterChannelUpdate(player,channelMap);
      }
    }
  }

  @Override
  public void didGroupVolumeChanged(double volume, IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      int groupVolume = (int)(volume * 100);
      synchronized (mOnCurrentGroupVolumeChangedListeners) {
        for (OnCurrentGroupVolumeChangedListener listener : mOnCurrentGroupVolumeChangedListeners)
          listener.onCurrentGroupVolumeStateChanged(groupVolume, false);
      }
    }
  }

  @Override
  public void didGroupMuteChanged(boolean muted, IoTPlayer player) {
    IoTGroup group = IoTService.getInstance().getGroupByPlayerId(player.getPlayerId());
    if(group == null || mCurrentGroup == null) return;

    if(group.getGroupId().equalsIgnoreCase(mCurrentGroup.getGroupId())) {
      synchronized (mOnCurrentGroupVolumeChangedListeners) {
        for (OnCurrentGroupVolumeChangedListener listener : mOnCurrentGroupVolumeChangedListeners)
          listener.onCurrentZoneMuteStateChanged(muted);
      }
    }
  }

  @Override
  public void onPlayerReDiscovered() {
    synchronized (mOnDeviceRediscoveredListeners) {
      if (mOnDeviceRediscoveredListeners != null)
        mOnDeviceRediscoveredListeners.stream()
            .forEach(listener -> listener.onDeviceRediscovered());
    }
  }

}
