/*
 * *************************************************************************************************
 * * Copyright 2018 Qualcomm Technologies International, Ltd.                                      *
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.repository;

import static com.qualcomm.qti.iotcontrollersdk.constants.IoTError.NONE;
import static com.qualcomm.qti.iotcontrollersdk.constants.IoTError.NOT_SUPPORTED;
import static com.qualcomm.qti.iotcontrollersdk.constants.IoTError.UNKNOWN;

import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.DolbyListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.EffectsTrumpetListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.GroupVolumeControlListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.VirtualXSoundXListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.AddConfiguredDevicesInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.AddConfiguredDevicesPlayersAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.AddUnconfiguredDeviceInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.BassboostAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.DolbyAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EffectsTrumpetAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EqualizerAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EqualizerPropertyAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupAddToGroupInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupCreateInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupDeleteInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupRemoveFromGroupInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupSetGroupNameInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputOutputInfoAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.OutputSelectorAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PresetReverbAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.RemoveDevicesInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.TrumpetEqualizerAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VirtualXSoundXAttr;
import com.qualcomm.qti.iotcontrollersdk.constants.EffectType;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.IoTChannelMap;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTGroupCallback;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.AddHomeTheaterChannelData;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.EqualizerBandSettings;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap.MultiChannelInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.InputOutputSourceInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.TrumpetEqualizerBand;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.DolbyState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.InputSourceSelectionState.InputOutputInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.TrumpetEffectState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.TrumpetEffectState.TrumpetPreset;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import com.qualcomm.qti.iotcontrollersdk.ResourceAttributes;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTAllPlayClient;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTAllPlayClient.IoTAllPlayClientObserver;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTAllPlayClient.IoTAllPlayObserveOrigin;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.MultichannelGroupMainListener;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.DtsVirtaulXState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.GroupState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.InputSourceSelectionState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MediaPlayerState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MultiChannelGroupMainState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.MultichannelGroupSatellite;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.OutputSourceSelectionState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.MediaItem;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupsInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.AllPlayStates;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.AudioEffectsState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.BassBoostState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.EqualizerState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.PlaylistState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.PresetRevertState;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.state.VolumeControlState;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.AudioEffectsListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.BassboostListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.EqualizerListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.PresetReverbListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EnabledControlsAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupInfoHelperAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupPlayersAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputSelectorAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayItemAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.GroupListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.MediaPlayerListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.PlaylistGetRangeListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.PlaylistListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.VolumeControlListener;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.GroupAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.LoopMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.MediaPlayerAttr.ShuffleMode;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlayStateAttr.PlayState;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistGetRangeInAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.PlaylistGetRangeOutAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.QueuedItemAttr;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VolumeControlAttr;
import com.qualcomm.qti.iotcontrollersdk.controller.IoTService;
import com.qualcomm.qti.iotcontrollersdk.controller.TaskExecutors;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;


/**
 * The class provides APIs for the app to access to speakers
 */

public class IoTPlayer extends IoTRepository {

    private final static String TAG = "IoTPlayer";

    private final AllPlayStates mState = new AllPlayStates();

    private IoTAllPlayClient mClient;

    private final AtomicBoolean isFirstTimeFetchComplete;

    private TaskExecutors mExecutor;

    private IoTPlayerUpdatesDelegate mUpdatesDelegate;
    private CountDownLatch mFetchAllLatch;

    private IoTAllPlayObserver mObserver;

    private Long mPlaybackTime = -1L;

    private AtomicBoolean mIsGrouping = new AtomicBoolean(false);
    private CyclicBarrier mBarrier;

    private final static Object mLock = new Object();
    private String mGroupIdCreated;
    private boolean mIsGroupCreated;

    private class IoTAllPlayObserver implements IoTAllPlayClientObserver {

      private WeakReference<IoTPlayer> mPlayer;

      IoTAllPlayObserver(IoTPlayer player) {
        mPlayer = new WeakReference<>(player);
      }

      @Override
      public void onUpdate(IoTAllPlayObserveOrigin type, OcRepresentation rep) {
        Log.d(TAG,"***** Notification:"+type + " Begin *****");
        boolean canUpdate = isFirstTimeFetchComplete.get();
        switch(type) {

          case IoTAllPlayObserveOriginMediaPlayer: {
               try {

                   MediaPlayerState state = getMediaPlayState();
                   String oldLeadPlayerId = state.getPlayState().mGroupLeadId;
                   if(state.update(rep)) {

                     MediaPlayerAttr attr = new MediaPlayerAttr();
                     attr.unpack(rep);
                     if (rep.hasAttribute(ResourceAttributes.Prop_playState)) {

                           OcRepresentation playStateRep = rep.getValue(ResourceAttributes.Prop_playState);

                           Log.d(TAG, "play state position:" + attr.mPlayState.mPositionMsecs + ",play state:" + attr.mPlayState.mPlayState);

                           boolean isPlayPositionUpdated = false;
                           if(playStateRep != null && playStateRep.hasAttribute(ResourceAttributes.Prop_queuedItems)) {
                              if(attr.mPlayState.mQueuedItems.size() > 0) {
                                isPlayPositionUpdated = true;
                                Log.d(TAG,"Play item in queue items size:" + attr.mPlayState.mQueuedItems.size());
                                if(attr.mPlayState.mQueuedItems.size() > 0 ) {
                                  Log.d(TAG,"Play item index:" + attr.mPlayState.mQueuedItems.get(0).mIndex);
                                }

                                for(QueuedItemAttr item: attr.mPlayState.mQueuedItems) {
                                  Log.d(TAG,"[Notify]:Play item URL:" + item.mPlayItem.mUrl);
                                }
                              }
                           }
                           updatePlayStatePosition(isPlayPositionUpdated);

                           if (canUpdate && mUpdatesDelegate != null) {
                               mUpdatesDelegate.didChangePlayState(mPlayer.get());
                               if((oldLeadPlayerId != null && !oldLeadPlayerId.equalsIgnoreCase(attr.mPlayState.mGroupLeadId)) ||
                                oldLeadPlayerId == null && attr.mPlayState.mGroupLeadId != null) {
                                  mUpdatesDelegate.didChangeLeadPlayer(mPlayer.get());
                               }
                           }
                     }

                     if (rep.hasAttribute(ResourceAttributes.Prop_shuffleMode)) {
                        Log.d(TAG, "Shuffle mode:" + attr.mShuffleMode);
                        if (canUpdate && mUpdatesDelegate != null) {
                            mUpdatesDelegate.didChangeShuffleMode(attr.mShuffleMode, mPlayer.get());
                        }
                     }

                     if (rep.hasAttribute(ResourceAttributes.Prop_loopMode)) {
                        Log.d(TAG, "Loop mode:" + attr.mLoopMode);
                       if (canUpdate && mUpdatesDelegate != null) {
                           mUpdatesDelegate.didChangeLoopMode(attr.mLoopMode, mPlayer.get());
                       }
                     }
                     if (rep.hasAttribute(ResourceAttributes.Prop_enabledControls)) {
                        if (canUpdate && mUpdatesDelegate != null) {
                            mUpdatesDelegate.didChangeEnabledControls(attr.mEnabledControls, mPlayer.get());
                        }
                     }
                     if (rep.hasAttribute(ResourceAttributes.Prop_capabilities)) {
                        String logMessage = "Media player Capabilities";
                        for(String cap : attr.mCapabilities) {
                          logMessage+=cap + " ";
                        }
                        Log.d(TAG,logMessage);
                     }
                     if(rep.hasAttribute(ResourceAttributes.Prop_displayName)) {
                        if (canUpdate && mUpdatesDelegate != null) {
                            mUpdatesDelegate.didChangeDisplayName(attr.mDisplayName, mPlayer.get());
                        }
                     }
                   }
                 } catch (OcException e) {
                   e.printStackTrace();
                 }
               }
          break;

          case IoTAllPlayObserveOriginVolumeControl: {
               try {
                   if (getVolumeControlState().update(rep)) {
                     if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
                       Log.d(TAG, String.format("IoTAllPlayObserveOriginVolumeControl volume:%f",
                           getVolumeControlState().getVolume()));
                       if (canUpdate && mUpdatesDelegate != null) {
                         mUpdatesDelegate
                             .didChangeVolume(getVolumeControlState().getVolume(), mPlayer.get());
                       }
                     }
                     if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
                       boolean mute = getVolumeControlState().getMute();
                       Log.d(TAG,
                           String.format("IoTAllPlayObserveOriginVolumeControl mute:%b", mute));
                       if (canUpdate && mUpdatesDelegate != null) {
                         mUpdatesDelegate.didChangeMuteState(mute, mPlayer.get());
                       }
                     }
                   }
                 } catch(OcException e){
                   e.printStackTrace();
                 }
          }
          break;
          case IoTAllPlayObserveOriginPlaylist: {
            try {
              if(getPlaylistState().update(rep)) {
                updatePlaylistState(success -> {
                  if (canUpdate && success && mUpdatesDelegate != null) {
                      mUpdatesDelegate.didChangePlaylist(mPlayer.get());
                  }
                });
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
          }
          break;
          case IoTAllPlayObserveOriginGroup:
            try{
              GroupState state = getGroupState();
              if(state.isAvailable() && state.update(rep)) {
                if(canUpdate) {

                  if(mIsGrouping.get()) {
                    try {
                      mBarrier.await(20,TimeUnit.SECONDS);
                    } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
                      e.printStackTrace();
                    }
                  } else {
                    Log.d(TAG,"Handle Group notification to get Group Info updated ...");
                    mPlayer.get().updateGroupInfo(1000, true);
                    mPlayer.get().updateGroupVolumeControl();
                  }
                }
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
          break;
          case IoTAllPlayObserveOriginDolby:
              try{
                if(getDolbyState().update(rep) && canUpdate && mUpdatesDelegate != null) {
                  Log.d(TAG,"Dolby mode:" + getDolbyState().geMode());
                  mUpdatesDelegate.didChangeAudioConfiguration(mPlayer.get(), EffectType.DOLBY);
                }
              } catch (OcException e) {
                e.printStackTrace();
              }
          break;
          case IoTAllPlayObserveOriginVirtualXSoundX:
              try{
                DtsVirtaulXState state = getVirtualXState();
                if(state.update(rep) && canUpdate && mUpdatesDelegate != null) {
                    Log.d(TAG,"DC enable:"+state.isDialogClarityEnabled() + ",mode:"+state.getOutMode() + String.format(" from %d to %d",state.getOutModeRange().mMin, state.getOutModeRange().mMax) );
                    mUpdatesDelegate.didChangeAudioConfiguration(mPlayer.get(), EffectType.DTS);
                }
              } catch (OcException e) {
                e.printStackTrace();
              }
          break;
          case IoTAllPlayObserveOriginAudioEffects:

              try{
                if(getAudioEffectState().update(rep) && canUpdate && mUpdatesDelegate != null) {
                   mUpdatesDelegate.didChangeAudioConfiguration(mPlayer.get(),EffectType.AudioEffects);
                }
              } catch (OcException e) {
                e.printStackTrace();
              }

          break;
          case IoTAllPlayObserveOriginBassboost:

              try{
                if(getBassBoostState().update(rep) && canUpdate) {
                    getBassBoostState().setAvailable(true);
                    mUpdatesDelegate.didChangeBassboost(mPlayer.get());
                }
              } catch (OcException e) {
                e.printStackTrace();
              }

          break;
          case IoTAllPlayObserveOriginEqualizer:

              try{
                if(getEqualizerState().update(rep) && canUpdate) {
                    getEqualizerState().setAvailable(true);
                    mUpdatesDelegate.didChangeEqualizer(mPlayer.get());
                }
              } catch (OcException e) {
                e.printStackTrace();
              }
          break;
          case IoTAllPlayObserveOriginInputSelector:

            try{
              if(getInputSelection().update(rep) && canUpdate) {
                  mState.getInputSelectorState().setAvailable(true);
                  mUpdatesDelegate.didChangeInputSelector(getInputSelection().getInputSourceSelection(),
                          mPlayer.get());
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
          break;
          case IoTAllPlayObserveOriginOutputSelector:
            try{
              if(getOutputSelection().update(rep) && canUpdate) {
                  mUpdatesDelegate.didChangeOutputSelector(mPlayer.get());
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
            break;
          case IoTAllPlayObserveOriginPresetReverb:

            try{
              if(getPresetRevertState().update(rep) && canUpdate) {
                 mUpdatesDelegate.didChangePresetReverb(mPlayer.get());
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
            break;
          case IoTAllPlayObserveOriginMultichannelGroupMain: {
            try{
              MultiChannelGroupMainState state = getMultichannelGroupMainState();
              if(state.update(rep) && canUpdate) {
                  state.setAvailable(true);
                  Log.d(TAG,"Update multi channel main attribute");
                  mUpdatesDelegate.didChangeMultiChannelMain(mPlayer.get(),state.getChannelInfo());
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
          }
          break;
          case IoTAllPlayObserveOriginMultichannelGroupSatellite: {
            try {
              getMultiChannelGroupSatelliteState().update(rep);
            } catch (OcException e) {
              e.printStackTrace();
            }
          }
          break;
          case IoTAllPlayObserveOriginGroupVolumeControl:
            try {
              VolumeControlState state = getGroupVolumeState();
              if(state.update(rep) && canUpdate) {
                Log.d(TAG,"Group volume update:" + state.getVolume());
                if (rep.hasAttribute(ResourceAttributes.Prop_volume)) {
                  mUpdatesDelegate.didGroupVolumeChanged(state.getVolume(), mPlayer.get());
                }

                if (rep.hasAttribute(ResourceAttributes.Prop_mute)) {
                  mUpdatesDelegate.didGroupMuteChanged(state.getMute(), mPlayer.get());
                }
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
          break;
          case IoTAllPlayObserveOriginEffectsTrumpet:
            try {
              TrumpetEffectState state = getTrumpetEffectState();
              if(state.update(rep) && canUpdate) {
                mUpdatesDelegate.didChangeAudioConfiguration(mPlayer.get(),EffectType.TRUMPET);
              }
            } catch (OcException e) {
              e.printStackTrace();
            }
            break;
          case IoTAllPlayObserveOriginCapabilities:
            break;
          default:
          break;
        }
        Log.d(TAG,"***** Notification for "+type + " End *****");

    }

      @Override
      public void onFailed(IoTAllPlayObserveOrigin type, String exception) {
        Log.d(TAG,String.format("***** AllPlay Observer: " + type + " update failed! ***** \n%s",exception));
      }

      @Override
      public void onRegistration(IoTAllPlayObserveOrigin type) {
        Log.d(TAG,"***** Observer:"+type + " registered *****");
      }

      @Override
      public void deRegistration(IoTAllPlayObserveOrigin type) {
        Log.d(TAG,"***** Observer:"+type + " deregistered *****");
        if(mPlayer.get().mClient.cancelNextObserver()) {

        }
      }
    }

    public interface IoTPlayerUpdatesDelegate {
      void didHavePlaybackError();
      void didChangeEnabledControls(EnabledControlsAttr attr, IoTPlayer player);
      void didChangePlaylist(IoTPlayer player);
      void didChangeAudioConfiguration(IoTPlayer player, EffectType type);
      void didChangeLoopMode(LoopMode loopMode,IoTPlayer player);
      void didChangeShuffleMode(ShuffleMode shuffleMode, IoTPlayer player);
      void didChangePlayState(IoTPlayer player);
      void didChangeDisplayName(String name, IoTPlayer player);
      void didChangeVolume(double volume, IoTPlayer player);
      void didChangeVolumeEnabled(boolean enabled, IoTPlayer player);
      void didChangeMuteState(boolean muted, IoTPlayer player);
      void didChangeGroupInfo(IoTPlayer player);
      void didChangeLeadPlayer(IoTPlayer player);
      void didChangeInputSelector(InputSelectorAttr attr, IoTPlayer player);
      void didChangeOutputSelector(IoTPlayer player);
      void didChangeBassboost(IoTPlayer player);
      void didChangeEqualizer(IoTPlayer player);
      void didChangePresetReverb(IoTPlayer player);
      void didChangeMultiChannelMain(IoTPlayer player, HomeTheaterChannelMap channelMap);
      void didGroupVolumeChanged(double volume, IoTPlayer player);
      void didGroupMuteChanged(boolean muted, IoTPlayer player);
    }

    public IoTPlayer (IoTAllPlayClient client) {
      super(IoTType.SPEAKER);
      this.mClient = client;
      mExecutor = TaskExecutors.getExecutor();
      mObserver = new IoTAllPlayObserver(this);
      client.registerObserver(mObserver);
      isFirstTimeFetchComplete = new AtomicBoolean(false);
    }

    public long getDeviceDiscoveryTime() {
      return mClient.getDeviceFoundTime();
    }

    public boolean dispose() {
      return false;
    }

    public boolean dispose(IoTCompletionCallback callback) {
      if(mState.getMediaPlayState().isAvailable())
        mClient.stopObserving(callback);
      return true;
    }

    @Override
    public String getName() {
      return getMediaPlayState().getDisplayName();
    }

    @Override
    public String getId() {
      return getPlayerId();
    }

    @Override
    public IoTType getType() {
      //TODO check the type from the attributes
      return IoTType.SPEAKER;
    }

    public String getHostName() {
      return mClient.getHostName();
    }

    public List<IoTRepository> getList() {
      List<GroupPlayersAttr> deviceList = getMultichannelGroupMainState().getPlayerAttrs();
      List<IoTRepository> retList = new ArrayList<>();
      if(deviceList.size() > 0 ) {
        for(GroupPlayersAttr attr: deviceList) {
          Log.d(TAG, "Surrounds name:" + attr.mDisplayName + ", device id:"+attr.mDeviceId);
          if(attr != null && attr.mDeviceId != null && !attr.mDeviceId.equalsIgnoreCase(getDeviceId()))
            retList.add(new IoTSurround(attr.mDisplayName, attr.mDeviceId));
        }
      }

      return retList;
    }

    /*package*/ IoTAllPlayClient getAllPlayController() {
      return mClient;
    }


    public synchronized void setDelegate(IoTPlayerUpdatesDelegate delegate) {
      mUpdatesDelegate = delegate;
    }

    public PlayerGroupsInfo getGroupsInfo() {
      if(getGroupState() != null ) {
        PlayerGroupsInfo groupsInfo = new PlayerGroupsInfo();
        groupsInfo.processAttribute(getGroupState().getAttribute()/*,getPlayerHost()*/);
        return groupsInfo;
      }
      return null;
    }

    public String getDeviceId() {
       return mClient.getDeviceId();
    }

    public String getPlayerId() {
      return mClient.getDeviceId();
    }

    public String getPlayerHost() {
      if(mClient != null) {
        String  host = mClient.getHostName();
        return ControllerSdkUtils.stripHostName(host);
      }
      return null;
    }

    public String getLeadPlayerId() {
      return mState.getMediaPlayState().getPlayState().mGroupLeadId;
    }

    public int volume() {
      return (int)(getVolumeControlState().getVolume() * 100);
    }

    public double maxVolume() {
      return 1.00f;
    }

    public double minVolume() {
      return 0.000001f;
    }

    public boolean isPartyModeEnabled() {
      return getMediaPlayState().getCapabilities().stream().anyMatch(str->("supportsPartyMode".equalsIgnoreCase(str)));
    }

    public boolean isMuted() {
      return getVolumeControlState().isMute();
    }

    public boolean isInputSelectorModeSupported() {
      return mState.getInputSelectorState().getInputListName().size() > 0;
    }

    public boolean isOutSelectorModeSupported() {
      return mState.getOutputSelectorState().getOutputList().size() > 0;
    }

    @Override
    public boolean equals(Object other) {
      if ((other == null) || !(other instanceof IoTPlayer)) {
        return false;
      }
      return getPlayerId().equalsIgnoreCase(((IoTPlayer) other).getPlayerId());
    }


    private void updatePlayStatePosition(boolean force) {
       synchronized (mPlaybackTime) {
          PlayState state = mState.getMediaPlayState().getPlayState().mPlayState;
         if (state == PlayState.kPlaying
             && (force || mPlaybackTime < 0)) {
           mPlaybackTime = System.currentTimeMillis();
         } else if(state != PlayState.kPlaying){
           mPlaybackTime = -1L;
         }
       }
    }

    public long getPlayingPosition() {

       synchronized (mPlaybackTime) {
        long diff = 0;
        if(mPlaybackTime >= 0) {
          diff = System.currentTimeMillis() - mPlaybackTime;
        }

        long lastTimeReported = 0;
        long position = mState.getMediaPlayState().getPlayState().mPositionMsecs;
        if(position >0) {
          lastTimeReported = position;
        }

        return lastTimeReported + diff;
       }
    }

    MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener() {
        @Override
        public void OnGetMediaPlayerCompleted(MediaPlayerAttr attribute, boolean status) {
          getMediaPlayState().setAvailable(true);
          if(status) {
            Log.d(TAG,"*****Get Media Player status ***** succeed!");
            updatePlayStatePosition(true);
            if(attribute != null)
              getMediaPlayState().update(attribute);

            if(attribute.mPlayState.mQueuedItems.size() > 0) {
              for(QueuedItemAttr item: attribute.mPlayState.mQueuedItems) {
                Log.d(TAG,"Play item in queue:" + item.mPlayItem.mUrl);
              }
            }
          } else {
            Log.d(TAG,"*****Get Media Player status failed:*****"+getPlayerId());
          }

          mFetchAllLatch.countDown();
        }

    };

    GroupListener mGroupListener = new GroupListener() {

        @Override
        public void OnGetGroupCompleted(GroupAttr attribute, boolean status) {
          getGroupState().setAvailable(status);
          if(status) {
            Log.d(TAG,"*****Get Group info ***** succeed!");
            if(attribute != null) {
              getGroupState().update(attribute);
            }
          }
          else {
            Log.e(TAG, "*****Get Group info ***** failed:"+getPlayerId());
          }
          mFetchAllLatch.countDown();
        }
    };

    VolumeControlListener mVolumeControlListener = new VolumeControlListener() {

        @Override
        public void OnGetVolumeControlCompleted(VolumeControlAttr attribute, boolean status) {
          if(status) {
            Log.d(TAG, "*****Get volume control with success *****");
            Log.d(TAG, "***** Player muted? " + getVolumeControlState().isMute()+"*****");
            getVolumeControlState().update(attribute);
          } else {
            Log.e(TAG, "*****Get volume control with failed status *****:"+getPlayerId());
          }

          mFetchAllLatch.countDown();
        }

    };

    PlaylistListener mPlaylistListener = new PlaylistListener() {

        @Override
        public void OnGetPlaylistCompleted(PlaylistAttr attribute, boolean status) {
          if(status) {
            Log.d(TAG,"*****Get play list success ***** ");
            if(attribute != null) {
              getPlaylistState().update(attribute);
              getPlaylistState().setSupported(true);
            }
          }
          else {
            getPlaylistState().setSupported(false);
            Log.e(TAG, "Get IoTPlaylist response with failure status:"+getPlayerId());
          }

          mFetchAllLatch.countDown();
        }
    };

    PlaylistGetRangeListener mPlaylistGetRangeListener = new PlaylistGetRangeListener() {

        @Override
        public void OnPlaylistGetRangeOutCompleted(PlaylistGetRangeOutAttr attribute, boolean status) {
          if(status && attribute != null) {
            getPlaylistState().setSize(attribute.mTotalSize);
            getPlaylistState().setSnapShotId(attribute.mLatestSnapshotId);
            List<MediaItem> mediaItems =new ArrayList<>();
            List<PlayItemAttr> itemsInRange = attribute.mItemsInRange;

            for(PlayItemAttr item : itemsInRange) {
              MediaItem mediaItem = new MediaItem(item.mTitle,item.mUrl);
              mediaItem.setAlbum(item.mAlbum);
              mediaItem.setArtist(item.mArtist);
              mediaItem.setDuration(item.mDurationMsecs);
              mediaItem.setGenre(item.mGenre);
              mediaItem.setThumbnailUrl(item.mThumbnailUrl);
              mediaItem.setUserData("");
              mediaItems.add(mediaItem);
            }
            getPlaylistState().clearPlayItems();
            getPlaylistState().setPlayItems(mediaItems);
            Log.d(TAG, "***** Snap shot id:="+ attribute.mLatestSnapshotId + ", total size:="+attribute.mTotalSize + "... *****" );
          }
          else {
            Log.e(TAG, "***** Get PlaylistGetRangeOut attributes failed... *****"+getPlayerId());
          }
          mFetchAllLatch.countDown();
        }

    };

    BassboostListener mBassBoostListener = new BassboostListener() {

      public void OnGetBassboostCompleted(boolean status) {
        mState.getBassBoostState().setAvailable(status);
        if(status) {
          Log.d(TAG,"***** Get Bassboost attributes success... *****");
        }
        else {
          Log.e(TAG, "***** Get Bassboost attributes failed... *****"+getPlayerId());
        }

        mFetchAllLatch.countDown();
      }

      public void OnBassboostCompleted(boolean status) {
        mFetchAllLatch.countDown();
      }
    };

    private AudioEffectsListener mAudioEffectsListener = (attribute, status) -> {
      if(status && attribute != null) {
        Log.d(TAG,"***** Get AudioEffects attributes success... *****");
        getAudioEffectState().update(attribute);
      }
      else {
        Log.e(TAG,"***** Get AudioEffects attributes failed... *****"+getPlayerId());
      }
      getAudioEffectState().setAvailable(status);

      mFetchAllLatch.countDown();
    };

    EqualizerListener mEqualizerListener = new EqualizerListener() {

      @Override
      public void OnGetEqualizerCompleted(boolean status) {
        mState.getEqualizerState().setAvailable(status);
        if(status) {
          Log.d(TAG, "***** Get EqualizerAttr attributes success... *****");
        } else {
          Log.e(TAG, "***** Get EqualizerAttr attributes failed... *****" + getPlayerId());
        }
        mFetchAllLatch.countDown();
      }

      @Override
      public void OnEqualizerCompleted(boolean status) {
        mFetchAllLatch.countDown();
      }

    };

    PresetReverbListener mPresetReverbListener = new PresetReverbListener() {

      @Override
      public void OnGetPresetReverbCompleted(boolean status) {
        getPresetRevertState().setAvailable(status);
        if(status) {
           Log.d(TAG,"***** Get PresetReverb attributes success... *****");
         } else {
           Log.e(TAG, "***** Get PresetReverb attributes failed... *****"+getPlayerId());
         }
         mFetchAllLatch.countDown();
      }

      @Override
      public void OnPresetReverbCompleted(boolean status) {
        mFetchAllLatch.countDown();
      }
    };

    MultichannelGroupMainListener multichannelGroupMainListener = (attribute, status) -> {
      if(status && attribute != null) {
         Log.d(TAG,"***** Get MultichannelGroupMain attributes success... *****");
         getMultichannelGroupMainState().update(attribute);
       } else {
         Log.e(TAG, "***** Get MultichannelGroupMain attributes failed... *****"+getPlayerId());
       }
       getMultichannelGroupMainState().setAvailable(status);
       mFetchAllLatch.countDown();
    };

    public void updateWithCompletion(final IoTCompletionCallback callback) {
       TaskExecutors.getExecutor().executeOnRequestExecutor(()-> {

        Log.d(TAG, "**** Setting up IoT player initial state...:" + getPlayerId());
        int numOfTasks = 0;

        List<Callable<Void>> taskList = new ArrayList<>();

        Callable<Void> task = () -> {
          mClient.getMediaPlayerWithCompletion(mMediaPlayerListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getGroupWithCompletion(mGroupListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getVolumeControlWithCompletion(mVolumeControlListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getPlaylistWithCompletion(mPlaylistListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          PlaylistGetRangeInAttr inAttr = new PlaylistGetRangeInAttr();
          inAttr.mSnapshotId = "";
          inAttr.mStart = 0;
          inAttr.mCount = 0xFFFF;
          mClient.postPlaylistGetRange(inAttr, mPlaylistGetRangeListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        /*task = () -> {
          mClient.getBassboostWithCompletion(mBassBoostListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getAudioEffectsWithCompletion(mAudioEffectsListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getPresetReverbWithCompletion(mPresetReverbListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getEqualizerWithCompletion(mEqualizerListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getDolbyWithCompletion(((attribute, status) -> {
            if(status) {
              Log.d(TAG,"***** Get Dolby attribute success... *****");
              if(attribute != null)
                mState.getDolbyState().update(attribute);

            } else {
              Log.e(TAG, "***** Get Dolby attribute failed... *****");
            }
            mState.getDolbyState().setAvailable(status);
            mFetchAllLatch.countDown();
          }));
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getVirtualXSoundXWithCompletion(((attribute, status) -> {
            if(status) {
              Log.d(TAG,"***** Get VirtualXSoundX attribute success... *****");
              if(attribute != null)
                mState.getDtsVirtualXState().update(attribute);
            } else {
              Log.e(TAG, "***** Get VirtualXSoundX attribute failed... *****");
            }
            mState.getDtsVirtualXState().setAvailable(status);
            mFetchAllLatch.countDown();
          }));
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
         mClient.getEffectsTrumpetWithCompletion(((attribute, status) -> {
          if(status) {
            Log.d(TAG, "***** Get Trumpet attributes succeed... *****");
            if(attribute != null)
              mState.getTrumpetState().update(attribute);
          } else {
            Log.e(TAG, "***** Get Trumpet attributes failed... *****");
          }
          mState.getTrumpetState().setAvailable(status);
          mFetchAllLatch.countDown();
        }));
        return null;
        };
        numOfTasks++;
        taskList.add(task);*/

        task = () -> {
          mClient.getMultichannelGroupMainWithCompletion(multichannelGroupMainListener);
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getInputSelectorWithCompletion(((attribute, status) -> {
            if(status) {
              Log.d(TAG,"***** Get Input selector attribute success... *****");
              if(attribute != null)
                mState.getInputSelectorState().update(attribute);
            } else {
              Log.e(TAG, "***** Get Input selector attribute failed... *****");
            }
            mState.getInputSelectorState().setAvailable(status);
            mFetchAllLatch.countDown();
          }));
          return null;
        };
        numOfTasks++;
        taskList.add(task);

        task = () -> {
          mClient.getOutputSelectorWithCompletion(((attribute, status) -> {
            if(status) {
              Log.d(TAG,"***** Get Output selector attribute success... *****");
              if(attribute != null)
                mState.getOutputSelectorState().update(attribute);
            } else {
              Log.e(TAG, "***** Get Output selector attribute failed... *****");
            }
            mState.getOutputSelectorState().setAvailable(status);
            mFetchAllLatch.countDown();
          }));
          return null;
        };
        numOfTasks++;
        taskList.add(task);

       task = () -> {
        mClient.getGroupVolumeControlWithCompletion(((attribute, status) -> {
          VolumeControlState state = mState.getGroupVolumeControlState();
          if(status) {
            Log.e(TAG, "***** get Group Volume attributes succeed... *****");
            state.setMute(attribute.mMute);
            state.setVolume(attribute.mVolume);
          } else {
            Log.e(TAG, "***** get Group Volume attributes failed... *****");
            state.setMute(true);
          }

          state.setAvailable(status);
          mFetchAllLatch.countDown();
        }));
        return null;
      };
      numOfTasks++;
      taskList.add(task);

      task = () -> {
        mClient.getMultichannelGroupSatelliteWithCompletion(((attribute, status) -> {
          if(status) {
            Log.d(TAG, "***** get Multi channel Satellite attributes succeed... *****");
          } else {
            Log.e(TAG, "***** get Multi channel Satellite attributes failed... *****");
          }
          mState.getMultiChannelGroupSatelliteState().setAvailable(status);
          mFetchAllLatch.countDown();
        }));
        return null;
      };
      numOfTasks++;
      taskList.add(task);

      mFetchAllLatch = new CountDownLatch(numOfTasks);

      for (Callable<Void> call : taskList) {
        try {
          /**
           * Memory consistency effects: Actions in a thread prior to
           * the submission of a Runnable or Callable task to an ExecutorService
           * happen-before any actions taken by that task,
           * which in turn happen-before the result is retrieved via Future.get().
           */
          mExecutor.submit(call).get();
        } catch (InterruptedException |
                 ExecutionException |
                 RejectedExecutionException e ) {
          e.printStackTrace();
        }
      }

      try {
        mFetchAllLatch.await(8000,TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        taskList.clear();
        callback.onCompletion(false);
        e.printStackTrace();
        Log.e(TAG,"Fetch resource timeout!");
        return;
      }
      isFirstTimeFetchComplete.compareAndSet(false,true);
      taskList.clear();

      callback.onCompletion(getMediaPlayState().isAvailable());

      });
    }

    public void updatePlaylistState(IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(()-> {
        PlaylistGetRangeInAttr inAttr = new PlaylistGetRangeInAttr();
        inAttr.mSnapshotId = "";
        inAttr.mStart = 0;
        inAttr.mCount = 0xFFFF;
        try {

          mClient.postPlaylistGetRange(inAttr, (attribute, status) -> {
            if (status) {
              getPlaylistState().setSize(attribute.mTotalSize);
              getPlaylistState().setSnapShotId(attribute.mLatestSnapshotId);

              List<PlayItemAttr> itemsInRange = attribute.mItemsInRange;

              List<MediaItem> mediaItems = getPlaylistState().getPlayItem();
              mediaItems.clear();

              for (PlayItemAttr item : itemsInRange) {
                MediaItem mediaItem = new MediaItem(item.mTitle, item.mUrl);
                mediaItem.setAlbum(item.mAlbum);
                mediaItem.setArtist(item.mArtist);
                mediaItem.setDuration(item.mDurationMsecs);
                mediaItem.setGenre(item.mGenre);
                mediaItem.setThumbnailUrl(item.mThumbnailUrl);
                mediaItem.setUserData("");
                mediaItems.add(mediaItem);
              }

              Log.d(TAG,
                  "***** Snap latest shot id:=" + attribute.mLatestSnapshotId + ", total size:="
                      + attribute.mTotalSize + ",media items:" + mediaItems.size() + "... *****");
              getPlaylistState().setSnapShotId(attribute.mLatestSnapshotId);
              getPlaylistState().setSize(attribute.mTotalSize);
              getPlaylistState().clearPlayItems();
              getPlaylistState().setPlayItems(mediaItems);
            } else {
              Log.e(TAG, "***** get PlaylistGetRangeOut attributes failed... *****");
            }
            callback.onCompletion(status);
          });
        } catch (OcException e) {
          e.printStackTrace();
          callback.onCompletion(false);
        }
      });
    }

    public  List<GroupInfoHelperAttr> getGroupInfo() {
      return mState.getGroupState().getGroupInfo();
    }

    public void updateGroupInfo(long delay, final boolean notifyUI) {
      TaskExecutors.getExecutor().executeDelayForRequest(()->
      {
        try {
          mClient.getGroupWithCompletion((attribute,status)-> {
            if (status) {
                if(attribute != null && isFirstTimeFetchComplete.get()) {
                  mState.getGroupState().update(attribute);
                  Log.d(TAG,"Fetching group info ...");
                  if(notifyUI) {
                    IoTService.getInstance().updateGroupInfo(this);
                    if (mUpdatesDelegate != null)
                      mUpdatesDelegate.didChangeGroupInfo(this);
                  }
                }
            }
          });
        } catch (OcException e) {
          e.printStackTrace();
        }


      },delay);
    }

    /*package*/ void updatePlayerState(IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {
        try {
          mClient.getMediaPlayerWithCompletion((attribute,status) -> {
            if(status) {

              String name = attribute.mDisplayName;
              List<String> caps = attribute.mCapabilities;
              String capabilities = "";
              for (String item : caps) {
                capabilities += item + ";";
              }

              LoopMode loopMode = attribute.mLoopMode;
              ShuffleMode shuffle = attribute.mShuffleMode;
              int version = attribute.mVersion;
              getMediaPlayState().update(attribute);

              Log.d(TAG,
                  "Media player name:" + name + ",capabilities:" + capabilities + ",loopMode:"
                      + loopMode + ",shuffle mode:" + shuffle + ",version:" + version);
            }
            callback.onCompletion(status);
          });
        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    private void updateGroupVolumeControl() {
       TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {
         try {
           mClient.getGroupVolumeControlWithCompletion((attribute, status) -> {
           VolumeControlState state = mState.getGroupVolumeControlState();
           if(status) {
             Log.d(TAG, "***** Fetching Group Volume attributes succeed... *****");
             state.setMute(attribute.mMute);
             state.setVolume(attribute.mVolume);
           } else {
             Log.e(TAG, "***** Fetching Group Volume attributes failed... *****");
             state.setMute(true);
           }
           state.setAvailable(status);
        });
         } catch (OcException e) {
           e.printStackTrace();
         }
       });
    }

    public PlayState getPlayState() {
      return getMediaPlayState().getPlayState().mPlayState;
    }

    /*package*/ MediaPlayerState getMediaPlayState() {
        return mState.getMediaPlayState();
    }

    /*package*/ GroupState getGroupState() {
        return mState.getGroupState();
    }

    /*package*/ InputSourceSelectionState getInputSelection() {
        return mState.getInputSelectorState();
    }

    /*package*/ OutputSourceSelectionState getOutputSelection() {
        return mState.getOutputSelectorState();
    }

    /*package*/ VolumeControlState getVolumeControlState() {
        return mState.getVolumeControlState();
    }

    public PlaylistState getPlaylistState() {
       return mState.getPlaylistState();
    }

    private BassBoostState getBassBoostState() {
        return mState.getBassBoostState();
    }

    private DolbyState getDolbyState() {
        return mState.getDolbyState();
    }

    private AudioEffectsState getAudioEffectState() {
        return mState.getAudioEffectState();
    }

    private DtsVirtaulXState getVirtualXState() {
        return mState.getDtsVirtualXState();
    }

    private EqualizerState getEqualizerState() {
        return mState.getEqualizerState();
    }

    private PresetRevertState getPresetRevertState() {
        return mState.getPresetRevertState();
    }

    public MultiChannelGroupMainState getMultichannelGroupMainState() {
        return mState.getMultiChannelGroupMainState();
    }

    public MultichannelGroupSatellite getMultiChannelGroupSatelliteState() {
        return mState.getMultiChannelGroupSatelliteState();
    }

    public VolumeControlState getGroupVolumeState() {
      return mState.getGroupVolumeControlState();
    }

    public TrumpetEffectState getTrumpetEffectState() {
      return mState.getTrumpetState();
    }

    public void setVolume(final double volume, IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {
        try {

          VolumeControlAttr attr = mState.getVolumeControlState().getAttribute();
          double volumeSet = volume;

          if(volume < minVolume()) {
            volumeSet = minVolume();
          } else if(volume > maxVolume()) {
            volumeSet = maxVolume();
          }

          attr.mVolume = volumeSet;
          mClient.postVolumeControl(attr, new VolumeControlListener() {
                @Override
                public void OnGetVolumeControlCompleted(VolumeControlAttr attribute, boolean status) {
                  if(status) {
                    Log.d(TAG, "set volume succeed, muted:" + attribute.mMute);
                    Log.d(TAG, "set volume succeed:" + attribute.mVolume);
                  }
                  callback.onCompletion(status);
                }

                @Override
                public void OnVolumeControlCompleted(boolean status) {
                  if(status) {
                  } else {
                    Log.e(TAG,"Volume Control respond error!");
                  }
                  callback.onCompletion(status);
                }
              });

        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    protected void setGroupVolume(double volume, IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {

        VolumeControlAttr attr = mState.getGroupVolumeControlState().getAttribute();
        double volumeSet = volume;

        if(volume < minVolume()) {
          volumeSet = minVolume();
        } else if(volume > maxVolume()) {
          volumeSet = maxVolume();
        }

        attr.mVolume = volumeSet;

        try{

          mClient.postGroupVolumeControl(attr, new GroupVolumeControlListener() {
                @Override
                public void OnVolumeControlCompleted(boolean status) {
                  if(status) {
                  } else {
                    Log.e(TAG,"Group Volume Control respond error!");
                  }
                  callback.onCompletion(status);
                }

                @Override
                public void OnGetGroupVolumeControlCompleted(VolumeControlAttr attribute, boolean status) {
                  callback.onCompletion(status);
                }

              });
        } catch (OcException e) {
          callback.onException(e);
        }
      });
    }

    public void setDisplayName(String name) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {
        try {

          MediaPlayerAttr attr = getMediaPlayState().getAttribute();
          attr.mDisplayName = name;
          mClient.postMediaPlayer(attr, (attribute, success) -> {
            if(success)
              getMediaPlayState().setDisplayName(attribute.mDisplayName);
          });

        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    /*package*/ void setMuted(boolean mute) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {
        try {

          VolumeControlAttr attr = getVolumeControlState().getAttribute();
          attr.mMute = mute;
          mClient.postVolumeControl(attr, (attribute, success) -> {
            if(success)
              getVolumeControlState().setMute(mute);
          });

        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    public void createGroup(String name, IoTGroupCallback callback) {
      GroupCreateInAttr attrIn = new GroupCreateInAttr();
      attrIn.mGroupName = name;
      mIsGrouping.set(true);

      /**
       * resets after a breakage has occurred for other reasons
       * can be complicated to carry out, create a new one every time.
       */
      mBarrier = new CyclicBarrier(2, () -> {
        mIsGrouping.set(false);
        String groupId;
        boolean isGroupCreated;
        synchronized (mLock) {
          groupId = mGroupIdCreated;
          isGroupCreated = mIsGroupCreated;
        }
        callback.OnGroupCreated(groupId, isGroupCreated);
      });

      try {
        mClient.postGroupCreate(attrIn, (attribute, success) -> {
          Log.d(TAG, "Create Group state:" + success);

          synchronized (mLock) {
            if (attribute != null)
              mGroupIdCreated = attribute.mGroupId;
            else
              mGroupIdCreated = null;

            mIsGroupCreated = success;
          }

          try {
           mBarrier.await(30,TimeUnit.SECONDS);
          } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            e.printStackTrace();
            callback.OnGroupCreated(mGroupIdCreated, false);
          }

        });
      } catch (OcException e) {
        e.printStackTrace();
      }

    }

    public void addPlayerInGroup(String groupId, IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(()-> {
        GroupAddToGroupInAttr attrIn = new GroupAddToGroupInAttr();
        attrIn.mGroupId = groupId;
        try {
          mClient.postGroupAddToGroup(attrIn, success -> callback.onCompletion(success));
        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    public void renameGroup(String groupId, String newName, IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(()-> {
        GroupSetGroupNameInAttr attrIn = new GroupSetGroupNameInAttr();
        attrIn.mGroupId = groupId;
        attrIn.mGroupName = newName;

        try {
          mClient.postGroupSetGroupName(attrIn, success -> callback.onCompletion(success));
        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    public void deleteGroup(String groupId, IoTCompletionCallback callback) {
       TaskExecutors.getExecutor().executeOnRequestExecutor(()-> {
         GroupDeleteInAttr attr = new GroupDeleteInAttr();
         attr.mGroupId = groupId;
         try {
           mClient.postGroupDelete(attr, success -> callback.onCompletion(success));
         } catch (OcException e) {
           e.printStackTrace();
         }
       });
    }

    public void deletePlayerFromGroup(IoTCompletionCallback callback) {
      TaskExecutors.getExecutor().executeOnRequestExecutor(() -> {

        GroupRemoveFromGroupInAttr attr = new GroupRemoveFromGroupInAttr();
        attr.mDeviceId = getPlayerId();
        attr.mGroupId = getGroupState().getCurrentGroupId();
        try {
          mClient.postGroupRemoveFromGroup(attr, success -> callback.onCompletion(success));
        } catch (OcException e) {
          e.printStackTrace();
        }
      });
    }

    public boolean isLicensed() {
      return mState.getGroupState().isAvailable();
    }

    public boolean isSoundBar() {
      return mState.getMultiChannelGroupMainState().isAvailable();
    }

    public boolean isSatellite() {
      return mState.getMultiChannelGroupSatelliteState().isAvailable();
    }

    public boolean isDialogClarify() {
      return mState.getDtsVirtualXState().isDialogClarityEnabled();
    }

    public boolean isBassBoostAvailable() {
      return mState.getBassBoostState().isAvailable();
    }

    public boolean enableDolby(boolean enabled, IoTCompletionCallback callback) {
      DolbyAttr attr = mState.getDolbyState().getAttribute();
      attr.mEnabled = enabled;
      try {
        mClient.postDolby(attr, new DolbyListener() {
          @Override
          public void OnGetDolbyCompleted(DolbyAttr attribute, boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnDolbyCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        return false;
      }

      return true;
    }

    public int getVirtualXOutModeRangeMin() {
      return mState.getDtsVirtualXState().getOutModeRange().mMin;
    }

    public int getVirtualXOutModeRangeMax() {
      return mState.getDtsVirtualXState().getOutModeRange().mMax;
    }

    public int getOutMode() {
      return mState.getDtsVirtualXState().getOutMode();
    }

    public boolean enableVirtualX(boolean enabled, IoTCompletionCallback callback) {
      VirtualXSoundXAttr attr = mState.getDtsVirtualXState().getAttribute();
      attr.mEnabled = enabled;
      try {
        mClient.postVirtualXSoundX(attr, new VirtualXSoundXListener() {

          @Override
          public void OnGetVirtualXSoundXCompleted(VirtualXSoundXAttr attribute, boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnVirtualXSoundXCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        return false;
      }

      return true;
    }

    public boolean enabledEqualizer(boolean enabled, IoTCompletionCallback callback) {

      EqualizerAttr attr = mState.getEqualizerState().getAttribute();
      attr.mEnabled = enabled;
      try {
        mClient.postEqualizer(attr, new EqualizerListener() {
          @Override
          public void OnGetEqualizerCompleted(boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnEqualizerCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        return false;
      }

      return true;
    }

    public boolean enableDialogClarity(boolean enabled, IoTCompletionCallback callback) {
      VirtualXSoundXAttr attr = mState.getDtsVirtualXState().getAttribute();
      attr.mDialogClarityEnabled = enabled;
      try {
        mClient.postVirtualXSoundX(attr, new VirtualXSoundXListener() {

          @Override
          public void OnGetVirtualXSoundXCompleted(VirtualXSoundXAttr attribute, boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnVirtualXSoundXCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        return false;
      }

      return true;
    }

    public boolean enableBassboost(boolean enabled, IoTCompletionCallback callback) {
      BassboostAttr attr = mState.getBassBoostState().getAttribute();
      attr.mEnabled = enabled;
      try {
        mClient.postBassboost(attr, new BassboostListener() {
          @Override
          public void OnGetBassboostCompleted(boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnBassboostCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        return false;
      }
      return true;
    }

    public boolean enableReverb(boolean enabled, IoTCompletionCallback callback) {
      PresetReverbAttr attr = mState.getPresetRevertState().getAttribute();
      attr.mEnabled = enabled;
      try {
        mClient.postPresetReverb(attr, new PresetReverbListener() {

              @Override
              public void OnGetPresetReverbCompleted(boolean status) {
                callback.onCompletion(status);
              }

              @Override
              public void OnPresetReverbCompleted(boolean status) {
                callback.onCompletion(status);
              }
            }
        );

      } catch (OcException e) {
        return false;
      }

      return true;
    }

    public boolean isDolbyAvailable() {
      return mState.getDolbyState().isAvailable();
    }

    public boolean isDolbyEnabled() {
      return mState.getDolbyState().isEnabled();
    }

    public int getDolbyMode() {
      return mState.getDolbyState().geMode();
    }


    public void setDolbyMode(int mode, IoTCompletionCallback callback) {
      DolbyAttr attr = mState.getDolbyState().getAttribute();
      attr.mMode = mode;
      try {
        mClient.postDolby(attr, new DolbyListener() {
          @Override
          public void OnGetDolbyCompleted(DolbyAttr attribute, boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnDolbyCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public boolean isVirtualXAvailable() {
      return mState.getDtsVirtualXState().isAvailable();
    }
    public boolean isVirtualXEnabled() {
      return mState.getDtsVirtualXState().isEnabled();
    }

    /**
    *   Set VirtualX mode
        @param mode output mode - 0(2.0), 1(2.1), 2(3.0), 3(3.1), 4(5.1)"
        @return true if success
     */
    public boolean setVirtualXOutMode(int mode, IoTCompletionCallback callback) {
      VirtualXSoundXAttr attr = mState.getDtsVirtualXState().getAttribute();
      attr.mOutMode = mode;
      try {
        mClient.postVirtualXSoundX(attr, (attribute, success) -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    public boolean setCurrentReverbPreset(String reveb,IoTCompletionCallback callback) {
      PresetReverbAttr attr = mState.getPresetRevertState().getAttribute();
      attr.mCurrentPreset = reveb;
      try {
        mClient.postPresetReverb(attr, new PresetReverbListener() {
          @Override
          public void OnGetPresetReverbCompleted(boolean status) {
          }

          @Override
          public void OnPresetReverbCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    public boolean isEqualizerAvailable() {
      return mState.getEqualizerState().isAvailable();
    }

    public boolean isEqualizerEnabled() {
      return mState.getEqualizerState().isEnabled();
    }

    public boolean isTrumpetAvailable() {
      return mState.getTrumpetState().isAvailable();
    }

    public boolean isTrumpetEnabled() {
      return mState.getTrumpetState().isEnabled();
    }

    public boolean isTrumpetirtualizationEnabled() {
      return mState.getTrumpetState().isVirtualization();
    }

    public boolean isReverbAvaliable() {
      return mState.getPresetRevertState().isAvailable();
    }

    public boolean isReverbEnabled() {
      return mState.getPresetRevertState().isEnabled();
    }

    public boolean isBassboostEnabled() {
      return mState.getBassBoostState().isEnabled();
    }

    public void setBassBoosts(int strength, IoTCompletionCallback callback) {

      BassboostAttr attr = mState.getBassBoostState().getAttribute();
      attr.mStrength = strength;
      try {
        mClient.postBassboost(attr, new BassboostListener() {
          @Override
          public void OnGetBassboostCompleted(boolean status) {
            callback.onCompletion(status);
          }

          @Override
          public void OnBassboostCompleted(boolean status) {
            callback.onCompletion(status);
          }
        });
      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public int getBassboostsMax() {
      return mState.getBassBoostState().getStrengthRange().mMax;
    }

    public int getBassboostsMin() {
      return mState.getBassBoostState().getStrengthRange().mMin;
    }

    public int getBassboostsStrength() {
      return mState.getBassBoostState().getStrength();
    }

    public String getCurrentReverbPreset() {
      return mState.getPresetRevertState().getCurrentPreset();
    }

    public List<String> getPresetReverbList() {
      return mState.getPresetRevertState().getPresets();
    }

    public String getCurrentEqPreset() {
      return mState.getEqualizerState().getCurrentPreset();
    }

    public List<EqualizerBandSettings> getEqualizerSettings() {
      List<EqualizerBandSettings> retList = new ArrayList<>();
      EqualizerPropertyAttr attr = mState.getEqualizerState().getEqualizerProperty();
      if(attr.mNumBands > 0 && attr.mNumBands <= attr.mBandLevel.size()) {
        for(int i=0; i < attr.mNumBands; i++) {
          int level = attr.mBandLevel.get(i);
          int centerFrequency = attr.mCenterFrequency.get(i);
          float range = (float)(attr.mLevelRange.mMax - attr.mLevelRange.mMin);
          if(range > 0.0f) {
            float percentage = (float)level / range;
            retList.add(new EqualizerBandSettings(level,centerFrequency,percentage,attr.mLevelRange.mMax,attr.mLevelRange.mMin ));
          }
        }
      }
      return retList;
    }

    public void setEqualizer(List<EqualizerBandSettings> settings, IoTCompletionCallback callback) {
       if(settings != null && !settings.isEmpty()) {
        EqualizerAttr attr = mState.getEqualizerState().getAttribute();

        List<Integer> values = new ArrayList<>();

        for(EqualizerBandSettings entry: settings) {
          float percentage = Math.min(Math.max(entry.percentage,1.0f),0.0f);
          float max = (float)attr.mEqualizerProperty.mLevelRange.mMax;
          float min = (float)attr.mEqualizerProperty.mLevelRange.mMin;
          float value = min + percentage * (max - min);
          values.add((int)value);
        }

        attr.mEqualizerProperty.mBandLevel = values;
         try {
           mClient.postEqualizer(attr, new EqualizerListener() {
             @Override
             public void OnGetEqualizerCompleted( boolean status) {
               callback.onCompletion(status);
             }

             @Override
             public void OnEqualizerCompleted(boolean status) {
              callback.onCompletion(status);
             }
           });
         } catch (OcException e) {
           e.printStackTrace();
         }
       }
    }

    public List<TrumpetEqualizerBand> getTrumpetEqBands() {
      List<TrumpetEqualizerBand> retList = new ArrayList<>();
      List<TrumpetEqualizerAttr> attrs = mState.getTrumpetState().getTrumpetEqbands();
      if(attrs != null) {
        attrs.forEach(attr -> {
          double centerFrequency = attr.mFrequency;
          double gain = attr.mGain;
          retList.add(new TrumpetEqualizerBand(centerFrequency, gain));
        });
      }
      return retList;
    }

    public List<String> getTrumpetPresetList() {
      return TrumpetEffectState.PresetList;
    }

    public TrumpetPreset getTrumpetCurrentPreset() {
      return mState.getTrumpetState().getCurrentPreset();
    }

    public double getTrumpetGain() {
      return mState.getTrumpetState().getGain();
    }

    public boolean enabledTrumpet(boolean enabled, IoTCompletionCallback callback) {

      EffectsTrumpetAttr attr = mState.getTrumpetState().getAttribute();
      attr.mEnabled = enabled;
      try {
        mClient.postEffectsTrumpet(attr, new EffectsTrumpetListener() {

              @Override
              public void OnGetEffectsTrumpetCompleted(EffectsTrumpetAttr attribute, boolean status) {
                callback.onCompletion(status);
              }

              @Override
              public void OnEffectsTrumpetCompleted(boolean status) {
                callback.onCompletion(status);
              }
            }
        );
        return true;
      } catch (OcException e) {
        e.printStackTrace();
      }
      return false;
    }

    public boolean enabledTrumpetVirtualization(boolean enabled, IoTCompletionCallback callback) {
      EffectsTrumpetAttr attr = mState.getTrumpetState().getAttribute();
      attr.mVirtualization = enabled;
      try {
        mClient.postEffectsTrumpet(attr, new EffectsTrumpetListener() {

              @Override
              public void OnGetEffectsTrumpetCompleted(EffectsTrumpetAttr attribute, boolean status) {
                callback.onCompletion(status);
              }

              @Override
              public void OnEffectsTrumpetCompleted(boolean status) {
                callback.onCompletion(status);
              }
            }
        );

        return true;
      } catch (OcException e) {
        e.printStackTrace();
      }
      return false;
    }

    public void setCurrentTrumpetPreset(String preset, IoTCompletionCallback callback) {
      EffectsTrumpetAttr attr = mState.getTrumpetState().getAttribute();
      attr.mPreset = TrumpetEffectState.getPreset(preset);
      try {
        mClient.postEffectsTrumpet(attr, new EffectsTrumpetListener() {

              @Override
              public void OnGetEffectsTrumpetCompleted(EffectsTrumpetAttr attribute, boolean status) {
                callback.onCompletion(status);
              }

              @Override
              public void OnEffectsTrumpetCompleted(boolean status) {
                callback.onCompletion(status);
              }
            }
        );

      } catch (OcException e) {
      }
    }

    public void setTrumpetEqBands(List<TrumpetEqualizerBand> bands, IoTCompletionCallback callback) {
      if(bands != null) {
        EffectsTrumpetAttr attr = mState.getTrumpetState().getAttribute();

        attr.mEqualizer.clear();
        for(TrumpetEqualizerBand entry: bands) {
          TrumpetEqualizerAttr newValue = new TrumpetEqualizerAttr();
          newValue.mFrequency = entry.mFrequency;
          newValue.mGain = entry.mGain;
          attr.mEqualizer.add(newValue);
        }

       try {
         mClient.postEffectsTrumpet(attr, new EffectsTrumpetListener() {

           @Override
           public void OnGetEffectsTrumpetCompleted(EffectsTrumpetAttr attribute, boolean status) {
             callback.onCompletion(status);
           }

           @Override
           public void OnEffectsTrumpetCompleted(boolean status) {
            callback.onCompletion(status);
           }
         });
       } catch (OcException e) {
         e.printStackTrace();
       }
     }
   }

    public void setTrumpetGain(double gain, IoTCompletionCallback callback) {

      EffectsTrumpetAttr attr = mState.getTrumpetState().getAttribute();
      attr.mGain = gain;
      try {
        mClient.postEffectsTrumpet(attr, new EffectsTrumpetListener() {

              @Override
              public void OnGetEffectsTrumpetCompleted(EffectsTrumpetAttr attribute, boolean status) {
                callback.onCompletion(status);
              }

              @Override
              public void OnEffectsTrumpetCompleted(boolean status) {
                callback.onCompletion(status);
              }
            }
        );

      } catch (OcException e) {
        e.printStackTrace();
      }
    }

    public boolean isHomeTheaterChannelPlayerInfoAvailable(HomeTheaterChannel channel) {
      IoTChannelMap ch = MultiChannelMapping.getIoTChannelMap(channel);

      List<GroupPlayersAttr> attrs = getMultichannelGroupMainState().getPlayerAttrs();
      for(GroupPlayersAttr attr:attrs) {
        if(attr.mChannelMap.contains(ch.ordinal())) {
          return attr.mConnected;
        }
      }
      return false;
    }

    public boolean isHomeTheaterChannelSupported() {
      return getMultichannelGroupMainState().isAvailable() || getMultiChannelGroupSatelliteState().isAvailable();
    }

    public AddHomeTheaterChannelData addHomeTheaterChannel(HomeTheaterChannel channel, IoTPlayer player, IoTCompletionCallback callback ) {
      AddHomeTheaterChannelData retVal = new AddHomeTheaterChannelData();
      retVal.error = NONE;
      retVal.previouslyKnown = false;

      IoTChannelMap IoTChannel = MultiChannelMapping.getIoTChannelMap(channel);
      if(IoTChannel == IoTChannelMap.IoTChannelMapNumberNone) {
        retVal.error = NOT_SUPPORTED;
        return retVal;
      }

      List<GroupPlayersAttr>  speakers= mState.getMultiChannelGroupMainState().getPlayerAttrs();
      if(speakers.stream().anyMatch(attr->attr.mChannelMap.contains(IoTChannel.ordinal()))) {
        retVal.error = NOT_SUPPORTED;
        retVal.previouslyKnown = true;
        return retVal;
      }

      AddConfiguredDevicesInAttr attr = new AddConfiguredDevicesInAttr();
      AddConfiguredDevicesPlayersAttr configuredAttr = new AddConfiguredDevicesPlayersAttr();
      configuredAttr.mChannelMap.add(IoTChannel.ordinal());
      configuredAttr.mDeviceId = player.getDeviceId();
      attr.mPlayers.add(configuredAttr);
      try {
        mClient.postAddConfiguredDevices(attr, (attribute, status) ->
          callback.onCompletion(status)
        );
      } catch (OcException e) {
        e.printStackTrace();
        retVal.error = UNKNOWN;
      }
      return retVal;
    }

    public AddHomeTheaterChannelData addHomeTheaterChannel(HomeTheaterChannel channel, String ssid, String deviceId, IoTCompletionCallback callback) {
      AddHomeTheaterChannelData retVal = new AddHomeTheaterChannelData();
      retVal.error = NONE;
      retVal.previouslyKnown = false;

      IoTChannelMap IoTChannel = MultiChannelMapping.getIoTChannelMap(channel);
      if(IoTChannel == IoTChannelMap.IoTChannelMapNumberNone) {
        retVal.error = NOT_SUPPORTED;
        return retVal;
      }

      List<GroupPlayersAttr>  speakers= getMultichannelGroupMainState().getPlayerAttrs();
      if(speakers.stream().anyMatch(attr->attr.mChannelMap.contains(IoTChannel.ordinal()))) {
        retVal.error = NOT_SUPPORTED;
        retVal.previouslyKnown = true;
      }

      AddUnconfiguredDeviceInAttr attr = new AddUnconfiguredDeviceInAttr();
      attr.mChannelMap.add(IoTChannel.ordinal());
      attr.mSsid = ssid;
      attr.mDeviceId = deviceId;
      try {
        mClient.postAddUnconfiguredDevice(attr, (attribute, status) ->
          callback.onCompletion(status)
        );
      } catch (OcException e) {
        e.printStackTrace();
        retVal.error = UNKNOWN;
      }
      return retVal;
    }

    public void setHomeTheaterChannelVolume(HomeTheaterChannel channel , int volmue){
      IoTChannelMap ch = MultiChannelMapping.getIoTChannelMap(channel);
      if(ch == IoTChannelMap.IoTChannelMapNumberNone) {
        return;
      }
    }

    public IoTError removeHomeTheaterChannelSub(IoTCompletionCallback callback) {
      return removeHomeTheaterChannelSurround(HomeTheaterChannel.SUBWOOFER,callback);
    }

    public IoTError removeHomeTheaterChannelSurround(HomeTheaterChannel channel, IoTCompletionCallback callback) {

      IoTChannelMap IoTChannel = MultiChannelMapping.getIoTChannelMap(channel);
      if(IoTChannel == IoTChannelMap.IoTChannelMapNumberNone) {
        return NOT_SUPPORTED;
      }

      String removingDeviceId = null;
      List<GroupPlayersAttr>  speakers= getMultichannelGroupMainState().getPlayerAttrs();
      for(GroupPlayersAttr attr: speakers) {
        if(attr.mChannelMap.contains(IoTChannel.ordinal())) {
          removingDeviceId = attr.mDeviceId;
          break;
        }
      }

      if(removingDeviceId == null) {
        return NOT_SUPPORTED;
      }

      RemoveDevicesInAttr removeAttr = new RemoveDevicesInAttr();
      removeAttr.mDeviceIdList.add(removingDeviceId);
      try {
        mClient.postRemoveDevices(removeAttr, success->callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
        return IoTError.UNKNOWN;
      }

      return NONE;
    }

    public IoTError removeHomeTheaterChannelSurrounds(List<HomeTheaterChannel> channels, IoTCompletionCallback callback) {

      if(channels == null || channels.isEmpty()) return NOT_SUPPORTED;

      RemoveDevicesInAttr removeAttr = new RemoveDevicesInAttr();
      List<GroupPlayersAttr>  speakers= getMultichannelGroupMainState().getPlayerAttrs();
      channels.forEach(channel -> {
          IoTChannelMap IoTChannel = MultiChannelMapping.getIoTChannelMap(channel);
          if(IoTChannel != IoTChannelMap.IoTChannelMapNumberNone) {
            GroupPlayersAttr attr = speakers.stream()
                .filter(speaker -> speaker.mChannelMap.contains(IoTChannel.ordinal())).findAny()
                .orElse(null);
            if (attr != null && attr.mDeviceId != null && !attr.mDeviceId.isEmpty())
              removeAttr.mDeviceIdList.add(attr.mDeviceId);
          }
      });

      try {
        mClient.postRemoveDevices(removeAttr, success->callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
        return IoTError.UNKNOWN;
      }
      return NONE;
    }

    public IoTError removeHomeTheaterChannelSurrounds(IoTCompletionCallback callback) {
      List<GroupPlayersAttr> attrs = getMultichannelGroupMainState().getPlayerAttrs();
      RemoveDevicesInAttr removeAttr = new RemoveDevicesInAttr();
      attrs.forEach(attr-> removeAttr.mDeviceIdList.add(attr.mDeviceId));
      try {
        mClient.postRemoveDevices(removeAttr, success->{callback.onCompletion(success);});
      } catch (OcException e) {
        e.printStackTrace();
        return IoTError.UNKNOWN;
      }
      return IoTError.NONE;
    }

    public boolean haveHomeTheaterChannel(HomeTheaterChannel channel) {
      List<GroupPlayersAttr> attrs = getMultichannelGroupMainState().getPlayerAttrs();
      IoTChannelMap ch = MultiChannelMapping.getIoTChannelMap(channel);
      return attrs.stream().anyMatch(attr->attr.mChannelMap.contains(ch.ordinal()));
    }

    public int getHomeTheaterChannelMaxVolume(HomeTheaterChannel channel) {
      return 100;
    }

    public int getMaxVolume() {
      return (int)getVolumeControlState().getMaxVolume() * 100;
    }

    public boolean isVolumeEnabled() {
      return getVolumeControlState().isMute();
    }

    public boolean isInterruptible() {
      return true;
    }

    public IoTError setInputSelector(String inputSelector, IoTCompletionCallback callback) {

        InputSelectorAttr attr = mState.getInputSelectorState().getInputSourceSelection();
        boolean isFound = false;
        for(InputOutputInfoAttr inputAttr : attr.mInputList) {
          if(inputAttr.mFriendlyName.equalsIgnoreCase(inputSelector)) {
            attr.mActiveInput = inputAttr;
            isFound = true;
            break;
          }
        }
        if(isFound) {
          try {
            mClient.postInputSelector(attr, (selectorAttr, success) -> callback.onCompletion(success));
          } catch (OcException e) {
            e.printStackTrace();
          }
        }
      return IoTError.NONE;
    }


    public IoTError setOutputSourceSelector(List<String> output, IoTCompletionCallback callback) {

      OutputSelectorAttr attr = mState.getOutputSelectorState().getOutputSourceSelection();
      attr.mActiveOutputs.clear();

      List<InputOutputInfoAttr> activateList = new ArrayList<>();

      attr.mOutputList.forEach(attribute ->{
          if(output != null && !output.isEmpty()) {
            output.forEach(name -> {
              if (attribute.mFriendlyName.equalsIgnoreCase(name))
                activateList.add(attribute);
            });
       }});

      attr.mActiveOutputs = activateList;
      try {
        mClient.postOutputSelector(attr, (selectorAttr, success) -> callback.onCompletion(success));
      } catch (OcException e) {
        e.printStackTrace();
        return IoTError.REQUEST;
      }
      return IoTError.NONE;
    }

    public int getHomeTheaterChannelVolume(HomeTheaterChannel channel) {
      HomeTheaterChannelMap  mapping = getMultichannelGroupMainState().getChannelInfo();
      MultiChannelInfo channelInfo = mapping.getChannelInfo(channel);
      if( channelInfo != null)
        return (int)channelInfo.getVolumeRatio() * 100;
      else
        return 0;
    }

    public int getVolume() {
      return (int)(getVolumeControlState().getVolume() * 100);
    }

    public List<String> getInputSelectorNameList() {
     return mState.getInputSelectorState().getInputListName();
    }

    public String getActiveInputSource() {
      InputOutputInfo info = getInputSelection().getActiveInput();
      if(info != null)
        return info.name;
      else
        return null;
    }

    public List<String> getActiveOutputSource() {
      List<InputOutputInfoAttr> attrs = getOutputSelection().getActiveOutputs();
      List<String> retList = new ArrayList<>();
      if(attrs != null)
        for(InputOutputInfoAttr ioAttr: attrs) {
          retList.add(ioAttr.mFriendlyName);
        }

      return retList;
    }

    public List<InputOutputSourceInfo> getOutputSource() {
      List<InputOutputInfoAttr> outputs = getOutputSelection().getOutputList();
      List<InputOutputInfoAttr> ActivateOutput = getOutputSelection().getActiveOutputs();
      List<InputOutputSourceInfo> retList  = new ArrayList<>();

      for(InputOutputInfoAttr attr : outputs) {
        boolean isActivated = ActivateOutput.stream().anyMatch(attribute->
                    attr.mId!=null && !attr.mId.trim().isEmpty()
                    &&attr.mFriendlyName!=null && !attr.mFriendlyName.trim().isEmpty()
                    &&attribute.mFriendlyName.equalsIgnoreCase(attr.mFriendlyName)
                    && attribute.mId.equalsIgnoreCase(attr.mId));
        retList.add(new InputOutputSourceInfo(attr.mFriendlyName,attr.mId,isActivated));
      }
      return retList;
    }

    public IoTError updateHomeTheaterChannelFirmware(HomeTheaterChannel channel) {
      return IoTError.NONE;
    }

    public boolean checkNewHomeTheaterChannelFirmwareUpdate(HomeTheaterChannel channel) {
      return false;
    }

    public boolean haveNewHomeTheaterChannelFirmware(HomeTheaterChannel channel) {
      return false;
    }
}
