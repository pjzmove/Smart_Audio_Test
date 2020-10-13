/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import android.util.Log;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.DiscoveryInterface;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.*;
import com.qualcomm.qti.iotcontrollersdk.allplay.resource.clients.*;
import com.qualcomm.qti.iotcontrollersdk.allplay.listeners.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.iotivity.base.ObserveType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.OcPlatform.OnResourceFoundListener;
import org.iotivity.base.OcResource;
import org.iotivity.base.OcResource.OnObserveListener;
import org.iotivity.base.OcException;
import org.iotivity.base.OcRepresentation;
import org.iotivity.base.OcResource.OnPostListener;
import org.iotivity.base.OcResource.OnGetListener;
import org.iotivity.base.OcHeaderOption;


public class IoTAllPlayClient extends ControllerInterface {

    private final static String TAG = "IoTAllPlayClient";

    public enum IoTAllPlayObserveOrigin {
        IoTAllPlayObserveOriginAudioEffects,
        IoTAllPlayObserveOriginBassboost,
        IoTAllPlayObserveOriginCapabilities,
        IoTAllPlayObserveOriginDolby,
        IoTAllPlayObserveOriginEffectsTrumpet,
        IoTAllPlayObserveOriginEqualizer,
        IoTAllPlayObserveOriginGroup,
        IoTAllPlayObserveOriginGroupVolumeControl,
        IoTAllPlayObserveOriginInputSelector,
        IoTAllPlayObserveOriginMediaPlayer,
        IoTAllPlayObserveOriginMultichannelGroupMain,
        IoTAllPlayObserveOriginMultichannelGroupSatellite,
        IoTAllPlayObserveOriginOutputSelector,
        IoTAllPlayObserveOriginPlaylist,
        IoTAllPlayObserveOriginPresetReverb,
        IoTAllPlayObserveOriginVirtualXSoundX,
        IoTAllPlayObserveOriginVolumeControl,
    }

    public interface IoTAllPlayClientObserver {
      void onRegistration(IoTAllPlayObserveOrigin type);
      void deRegistration(IoTAllPlayObserveOrigin type);
      void onUpdate(IoTAllPlayObserveOrigin type, OcRepresentation rep);
      void onFailed(IoTAllPlayObserveOrigin type, String exception);
    }

    private TaskExecutors mExecutor;
    private IoTAllPlayClientObserver mObserver;

    /** The followings are resource clients instance variables */
    private AddConfiguredDevicesResourceClient mAddConfiguredDevices;
    private AddUnconfiguredDeviceResourceClient mAddUnconfiguredDevice;
    private AdjustVolumeResourceClient mAdjustVolume;
    private AudioEffectsResourceClient mAudioEffects;
    private BassboostResourceClient mBassboost;
    private CapabilitiesResourceClient mCapabilities;
    private ConnectGroupResourceClient mConnectGroup;
    private DolbyResourceClient mDolby;
    private EffectsTrumpetResourceClient mEffectsTrumpet;
    private EqualizerResourceClient mEqualizer;
    private GetPresetDetailsResourceClient mGetPresetDetails;
    private GroupResourceClient mGroup;
    private GroupAddToGroupResourceClient mGroupAddToGroup;
    private GroupCreateResourceClient mGroupCreate;
    private GroupDeleteResourceClient mGroupDelete;
    private GroupRemoveFromGroupResourceClient mGroupRemoveFromGroup;
    private GroupSetGroupNameResourceClient mGroupSetGroupName;
    private GroupSyncResourceClient mGroupSync;
    private GroupVolumeControlResourceClient mGroupVolumeControl;
    private GroupVolumeSyncResourceClient mGroupVolumeSync;
    private InputSelectorResourceClient mInputSelector;
    private JoinGroupResourceClient mJoinGroup;
    private LeaveGroupResourceClient mLeaveGroup;
    private MediaPlayerResourceClient mMediaPlayer;
    private MultichannelGroupMainResourceClient mMultichannelGroupMain;
    private MultichannelGroupSatelliteResourceClient mMultichannelGroupSatellite;
    private NextResourceClient mNext;
    private OutputSelectorResourceClient mOutputSelector;
    private PauseResourceClient mPause;
    private PlayResourceClient mPlay;
    private PlayIndexResourceClient mPlayIndex;
    private PlayItemResourceClient mPlayItem;
    private PlaylistResourceClient mPlaylist;
    private PlaylistDeleteResourceClient mPlaylistDelete;
    private PlaylistGetHistoryResourceClient mPlaylistGetHistory;
    private PlaylistGetRangeResourceClient mPlaylistGetRange;
    private PlaylistInsertResourceClient mPlaylistInsert;
    private PlaylistMoveResourceClient mPlaylistMove;
    private PresetReverbResourceClient mPresetReverb;
    private PreviousResourceClient mPrevious;
    private RegisterForSurroundSignalsResourceClient mRegisterForSurroundSignals;
    private RemoveDevicesResourceClient mRemoveDevices;
    private SetCustomPropertyResourceClient mSetCustomProperty;
    private SetPositionResourceClient mSetPosition;
    private StopResourceClient mStop;
    private VirtualXSoundXResourceClient mVirtualXSoundX;
    private VolumeControlResourceClient mVolumeControl;

    /*private List<String> mDiscoveryResourceTypes;*/

    /**
     * Class constructor
     */

    public IoTAllPlayClient(final String host, final String id, DiscoveryInterface discovery, final TaskExecutors executor, long time) {

        mHost = host;
        mId = id;
        mDeviceDiscovery = discovery;
        mUriPrefix = "/qti/allplay";
        mExecutor = executor;
        mDeviceDiscoveryTime = time;
/*        mDiscoveryResourceTypes = Arrays.asList(

                  "qti.allplay.r.addConfiguredDevices",
                  "qti.allplay.r.addUnconfiguredDevice",
                  "qti.allplay.r.adjustVolume",
                  "qti.allplay.r.audioEffects",
                  "qti.allplay.r.bassboost",
                  "qti.allplay.r.capabilities",
                  "qti.allplay.r.connectGroup",
                  "qti.allplay.r.dolby",
                  "qti.allplay.r.effectsTrumpet",
                  "qti.allplay.r.equalizer",
                  "qti.allplay.r.getPresetDetails",
                  "qti.allplay.r.group",
                  "qti.allplay.r.groupAddToGroup",
                  "qti.allplay.r.groupCreate",
                  "qti.allplay.r.groupDelete",
                  "qti.allplay.r.groupRemoveFromGroup",
                  "qti.allplay.r.groupSetGroupName",
                  "qti.allplay.r.groupSync",
                  "qti.allplay.r.groupVolumeControl",
                  "qti.allplay.r.groupVolumeSync",
                  "qti.allplay.r.inputSelector",
                  "qti.allplay.r.joinGroup",
                  "qti.allplay.r.leaveGroup",
                  "qti.allplay.r.mediaPlayer",
                  "qti.allplay.r.multichannelGroupMain",
                  "qti.allplay.r.multichannelGroupSatellite",
                  "qti.allplay.r.next",
                  "qti.allplay.r.outputSelector",
                  "qti.allplay.r.pause",
                  "qti.allplay.r.play",
                  "qti.allplay.r.playIndex",
                  "qti.allplay.r.playItem",
                  "qti.allplay.r.playlist",
                  "qti.allplay.r.playlistDelete",
                  "qti.allplay.r.playlistGetHistory",
                  "qti.allplay.r.playlistGetRange",
                  "qti.allplay.r.playlistInsert",
                  "qti.allplay.r.playlistMove",
                  "qti.allplay.r.presetReverb",
                  "qti.allplay.r.previous",
                  "qti.allplay.r.registerForSurroundSignals",
                  "qti.allplay.r.removeDevices",
                  "qti.allplay.r.setCustomProperty",
                  "qti.allplay.r.setPosition",
                  "qti.allplay.r.stop",
                  "qti.allplay.r.virtualXSoundX",
                  "qti.allplay.r.volumeControl",

                  "qti.allplay.r.mediaPlayer"
        );
*/
    }

    public void registerObserver(IoTAllPlayClientObserver observer) {
          mObserver = observer;
    }

    public synchronized String getHostName() {
        return mHost;
    }

    public synchronized String getDeviceId() {
        return mId;
    }

    @Override
    public void discoverWithCompletion(final IoTCompletionCallback callback) throws OcException {
        if(mDeviceDiscovery != null) {

            mDeviceDiscovery.findResource(mHost,/*mUriPrefix*/ OcPlatform.WELL_KNOWN_QUERY,EnumSet.of(OcConnectivityType.CT_ADAPTER_IP),
                                 new OnResourceFoundListener() {

                                   private AtomicInteger mCounter = new AtomicInteger(0);

                                   @Override
                                   public void onResourceFound(OcResource resource) {

                                      TaskExecutors.getExecutor().executeOnResourceExecutor(() -> {
                                          if (resource.getUri().contains(mUriPrefix)) {

                                              Log.d(TAG,String.format("Required resource found uri:%s, %d",
                                                          resource.getUri(),resource.getResourceTypes().size()));

                                              if(mCounter.incrementAndGet() == IoTConstants.ALLPLAY_RESOURCE_NUMBER) {
                                                Log.d(TAG,"All required resources found!");
                                                callback.onCompletion(true);
                                              }
                                          }
                                      });
                                   }

                                   @Override
                                   public void onFindResourceFailed(Throwable ex, String uri) {
                                        Log.e(TAG,String.format("Finding Resource failed:%s",uri));
                                        callback.onCompletion(false);
                                        ex.printStackTrace();
                                   }
                                 }
                             );
            }
    }

    @Override
    public void createResourceClientsForHost(String host) throws OcException {

        mAddConfiguredDevices = new AddConfiguredDevicesResourceClient(host);
        mAddUnconfiguredDevice = new AddUnconfiguredDeviceResourceClient(host);
        mAdjustVolume = new AdjustVolumeResourceClient(host);
        mAudioEffects = new AudioEffectsResourceClient(host);
        mAudioEffects.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginAudioEffects);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginAudioEffects);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginAudioEffects,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginAudioEffects,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mAudioEffects);
        mBassboost = new BassboostResourceClient(host);
        mBassboost.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginBassboost);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginBassboost);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginBassboost,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginBassboost,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mBassboost);
        mCapabilities = new CapabilitiesResourceClient(host);
        mCapabilities.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginCapabilities);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginCapabilities);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginCapabilities,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginCapabilities,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mCapabilities);
        mConnectGroup = new ConnectGroupResourceClient(host);
        mDolby = new DolbyResourceClient(host);
        mDolby.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginDolby);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginDolby);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginDolby,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginDolby,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mDolby);
        mEffectsTrumpet = new EffectsTrumpetResourceClient(host);
        mEffectsTrumpet.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEffectsTrumpet);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEffectsTrumpet);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEffectsTrumpet,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEffectsTrumpet,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mEffectsTrumpet);
        mEqualizer = new EqualizerResourceClient(host);
        mEqualizer.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEqualizer);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEqualizer);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEqualizer,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginEqualizer,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mEqualizer);
        mGetPresetDetails = new GetPresetDetailsResourceClient(host);
        mGroup = new GroupResourceClient(host);
        mGroup.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroup);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroup);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroup,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroup,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mGroup);
        mGroupAddToGroup = new GroupAddToGroupResourceClient(host);
        mGroupCreate = new GroupCreateResourceClient(host);
        mGroupDelete = new GroupDeleteResourceClient(host);
        mGroupRemoveFromGroup = new GroupRemoveFromGroupResourceClient(host);
        mGroupSetGroupName = new GroupSetGroupNameResourceClient(host);
        mGroupSync = new GroupSyncResourceClient(host);
        mGroupVolumeControl = new GroupVolumeControlResourceClient(host);
        mGroupVolumeControl.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroupVolumeControl);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroupVolumeControl);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroupVolumeControl,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginGroupVolumeControl,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mGroupVolumeControl);
        mGroupVolumeSync = new GroupVolumeSyncResourceClient(host);
        mInputSelector = new InputSelectorResourceClient(host);
        mInputSelector.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginInputSelector);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginInputSelector);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginInputSelector,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginInputSelector,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mInputSelector);
        mJoinGroup = new JoinGroupResourceClient(host);
        mLeaveGroup = new LeaveGroupResourceClient(host);
        mMediaPlayer = new MediaPlayerResourceClient(host);
        mMediaPlayer.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMediaPlayer);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMediaPlayer);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMediaPlayer,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMediaPlayer,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mMediaPlayer);
        mMultichannelGroupMain = new MultichannelGroupMainResourceClient(host);
        mMultichannelGroupMain.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupMain);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupMain);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupMain,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupMain,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mMultichannelGroupMain);
        mMultichannelGroupSatellite = new MultichannelGroupSatelliteResourceClient(host);
        mMultichannelGroupSatellite.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupSatellite);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupSatellite);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupSatellite,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginMultichannelGroupSatellite,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mMultichannelGroupSatellite);
        mNext = new NextResourceClient(host);
        mOutputSelector = new OutputSelectorResourceClient(host);
        mOutputSelector.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginOutputSelector);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginOutputSelector);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginOutputSelector,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginOutputSelector,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mOutputSelector);
        mPause = new PauseResourceClient(host);
        mPlay = new PlayResourceClient(host);
        mPlayIndex = new PlayIndexResourceClient(host);
        mPlayItem = new PlayItemResourceClient(host);
        mPlaylist = new PlaylistResourceClient(host);
        mPlaylist.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPlaylist);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPlaylist);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPlaylist,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPlaylist,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mPlaylist);
        mPlaylistDelete = new PlaylistDeleteResourceClient(host);
        mPlaylistGetHistory = new PlaylistGetHistoryResourceClient(host);
        mPlaylistGetRange = new PlaylistGetRangeResourceClient(host);
        mPlaylistInsert = new PlaylistInsertResourceClient(host);
        mPlaylistMove = new PlaylistMoveResourceClient(host);
        mPresetReverb = new PresetReverbResourceClient(host);
        mPresetReverb.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPresetReverb);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPresetReverb);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPresetReverb,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginPresetReverb,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mPresetReverb);
        mPrevious = new PreviousResourceClient(host);
        mRegisterForSurroundSignals = new RegisterForSurroundSignalsResourceClient(host);
        mRemoveDevices = new RemoveDevicesResourceClient(host);
        mSetCustomProperty = new SetCustomPropertyResourceClient(host);
        mSetPosition = new SetPositionResourceClient(host);
        mStop = new StopResourceClient(host);
        mVirtualXSoundX = new VirtualXSoundXResourceClient(host);
        mVirtualXSoundX.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVirtualXSoundX);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVirtualXSoundX);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVirtualXSoundX,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVirtualXSoundX,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mVirtualXSoundX);
        mVolumeControl = new VolumeControlResourceClient(host);
        mVolumeControl.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {

                TaskExecutors.getExecutor().executeAllplayNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                        mObserver.onRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVolumeControl);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                        mObserver.deRegistration(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVolumeControl);
                    } else if(mObserver != null) {
                        mObserver.onUpdate(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVolumeControl,ocRepresentation);
                    }
                 });
            }


            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeAllplayNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTAllPlayObserveOrigin.IoTAllPlayObserveOriginVolumeControl,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mVolumeControl);

    }

    @Override
    public void stopObserving() {
        TaskExecutors.getExecutor().executeOnResourceExecutor(() -> {
            try {
              mAudioEffects.stopObserving();
              mBassboost.stopObserving();
              mCapabilities.stopObserving();
              mDolby.stopObserving();
              mEffectsTrumpet.stopObserving();
              mEqualizer.stopObserving();
              mGroup.stopObserving();
              mGroupVolumeControl.stopObserving();
              mInputSelector.stopObserving();
              mMediaPlayer.stopObserving();
              mMultichannelGroupMain.stopObserving();
              mMultichannelGroupSatellite.stopObserving();
              mOutputSelector.stopObserving();
              mPlaylist.stopObserving();
              mPresetReverb.stopObserving();
              mVirtualXSoundX.stopObserving();
              mVolumeControl.stopObserving();
            }catch(OcException e) {
               e.printStackTrace();
            }
        });

    }

    public boolean postAddConfiguredDevices(AddConfiguredDevicesInAttr attr, final AddConfiguredDevicesListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mAddConfiguredDevices == null) return false;
        mAddConfiguredDevices.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          AddConfiguredDevicesOutAttr outAttribute = new AddConfiguredDevicesOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnAddConfiguredDevicesOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnAddConfiguredDevicesOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnAddConfiguredDevicesOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postAddUnconfiguredDevice(AddUnconfiguredDeviceInAttr attr, final AddUnconfiguredDeviceListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mAddUnconfiguredDevice == null) return false;
        mAddUnconfiguredDevice.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          AddUnconfiguredDeviceOutAttr outAttribute = new AddUnconfiguredDeviceOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnAddUnconfiguredDeviceOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnAddUnconfiguredDeviceOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnAddUnconfiguredDeviceOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postAdjustVolume(AdjustVolumeInAttr attr, final AdjustVolumeListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mAdjustVolume == null) return false;
        mAdjustVolume.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnAdjustVolumeInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnAdjustVolumeInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getAudioEffectsWithCompletion(final AudioEffectsListener listener) throws OcException {
        if(mAudioEffects == null) return false;
        mAudioEffects.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          AudioEffectsAttr outAttribute = new AudioEffectsAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetAudioEffectsCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetAudioEffectsCompleted(null,false);
                      }
                      mAudioEffects.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mAudioEffects.setResourceAvailable(false);

                    listener.OnGetAudioEffectsCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean getBassboostWithCompletion(final BassboostListener listener) throws OcException {

       if(mBassboost == null) return false;
       mBassboost.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnGetBassboostCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGetBassboostCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postBassboost(BassboostAttr attr, final BassboostListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mBassboost == null) return false;
        mBassboost.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnBassboostCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnBassboostCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getCapabilitiesWithCompletion(final CapabilitiesListener listener) throws OcException {
        if(mCapabilities == null) return false;
        mCapabilities.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          CapabilitiesAttr outAttribute = new CapabilitiesAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetCapabilitiesCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetCapabilitiesCompleted(null,false);
                      }
                      mCapabilities.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mCapabilities.setResourceAvailable(false);

                    listener.OnGetCapabilitiesCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postConnectGroup(ConnectGroupInAttr attr, final ConnectGroupListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mConnectGroup == null) return false;
        mConnectGroup.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          ConnectGroupOutAttr outAttribute = new ConnectGroupOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnConnectGroupOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnConnectGroupOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnConnectGroupOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getDolbyWithCompletion(final DolbyListener listener) throws OcException {
        if(mDolby == null) return false;
        mDolby.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          DolbyAttr outAttribute = new DolbyAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetDolbyCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetDolbyCompleted(null,false);
                      }
                      mDolby.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mDolby.setResourceAvailable(false);

                    listener.OnGetDolbyCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postDolby(DolbyAttr attr, final DolbyListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mDolby == null) return false;
        mDolby.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnDolbyCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnDolbyCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getEffectsTrumpetWithCompletion(final EffectsTrumpetListener listener) throws OcException {
        if(mEffectsTrumpet == null) return false;
        mEffectsTrumpet.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          EffectsTrumpetAttr outAttribute = new EffectsTrumpetAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetEffectsTrumpetCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetEffectsTrumpetCompleted(null,false);
                      }
                      mEffectsTrumpet.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mEffectsTrumpet.setResourceAvailable(false);

                    listener.OnGetEffectsTrumpetCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postEffectsTrumpet(EffectsTrumpetAttr attr, final EffectsTrumpetListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mEffectsTrumpet == null) return false;
        mEffectsTrumpet.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnEffectsTrumpetCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnEffectsTrumpetCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getEqualizerWithCompletion(final EqualizerListener listener) throws OcException {

       if(mEqualizer == null) return false;
       mEqualizer.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnGetEqualizerCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGetEqualizerCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postEqualizer(EqualizerAttr attr, final EqualizerListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mEqualizer == null) return false;
        mEqualizer.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnEqualizerCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnEqualizerCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGetPresetDetails(GetPresetDetailsInAttr attr, final GetPresetDetailsListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGetPresetDetails == null) return false;
        mGetPresetDetails.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          EqualizerPropertyAttr outAttribute = new EqualizerPropertyAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnEqualizerPropertyCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnEqualizerPropertyCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnEqualizerPropertyCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getGroupWithCompletion(final GroupListener listener) throws OcException {
        if(mGroup == null) return false;
        mGroup.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          GroupAttr outAttribute = new GroupAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetGroupCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetGroupCompleted(null,false);
                      }
                      mGroup.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mGroup.setResourceAvailable(false);

                    listener.OnGetGroupCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postGroupAddToGroup(GroupAddToGroupInAttr attr, final GroupAddToGroupListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupAddToGroup == null) return false;
        mGroupAddToGroup.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnGroupAddToGroupInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGroupAddToGroupInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGroupCreate(GroupCreateInAttr attr, final GroupCreateListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupCreate == null) return false;
        mGroupCreate.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          GroupCreateOutAttr outAttribute = new GroupCreateOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGroupCreateOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnGroupCreateOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnGroupCreateOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGroupDelete(GroupDeleteInAttr attr, final GroupDeleteListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupDelete == null) return false;
        mGroupDelete.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnGroupDeleteInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGroupDeleteInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGroupRemoveFromGroup(GroupRemoveFromGroupInAttr attr, final GroupRemoveFromGroupListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupRemoveFromGroup == null) return false;
        mGroupRemoveFromGroup.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnGroupRemoveFromGroupInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGroupRemoveFromGroupInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGroupSetGroupName(GroupSetGroupNameInAttr attr, final GroupSetGroupNameListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupSetGroupName == null) return false;
        mGroupSetGroupName.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnGroupSetGroupNameInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGroupSetGroupNameInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGroupSync(GroupSyncInAttr attr, final GroupSyncListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupSync == null) return false;
        mGroupSync.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnGroupSyncInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGroupSyncInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getGroupVolumeControlWithCompletion(final GroupVolumeControlListener listener) throws OcException {
        if(mGroupVolumeControl == null) return false;
        mGroupVolumeControl.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          VolumeControlAttr outAttribute = new VolumeControlAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetGroupVolumeControlCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetGroupVolumeControlCompleted(null,false);
                      }
                      mGroupVolumeControl.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mGroupVolumeControl.setResourceAvailable(false);

                    listener.OnGetGroupVolumeControlCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postGroupVolumeControl(VolumeControlAttr attr, final GroupVolumeControlListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupVolumeControl == null) return false;
        mGroupVolumeControl.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnVolumeControlCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnVolumeControlCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postGroupVolumeSync(GroupVolumeSyncInAttr attr, final GroupVolumeSyncListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mGroupVolumeSync == null) return false;
        mGroupVolumeSync.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnGroupVolumeSyncInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGroupVolumeSyncInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getInputSelectorWithCompletion(final InputSelectorListener listener) throws OcException {
        if(mInputSelector == null) return false;
        mInputSelector.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          InputSelectorAttr outAttribute = new InputSelectorAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetInputSelectorCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetInputSelectorCompleted(null,false);
                      }
                      mInputSelector.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mInputSelector.setResourceAvailable(false);

                    listener.OnGetInputSelectorCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postInputSelector(InputSelectorAttr attr, final InputSelectorListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mInputSelector == null) return false;
        mInputSelector.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnInputSelectorCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnInputSelectorCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postJoinGroup(JoinGroupInAttr attr, final JoinGroupListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mJoinGroup == null) return false;
        mJoinGroup.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          JoinGroupOutAttr outAttribute = new JoinGroupOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnJoinGroupOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnJoinGroupOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnJoinGroupOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postLeaveGroupWithCompletion(final LeaveGroupListener listener) throws OcException {

       if(mLeaveGroup == null) return false;
       mLeaveGroup.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostLeaveGroupCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostLeaveGroupCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean getMediaPlayerWithCompletion(final MediaPlayerListener listener) throws OcException {
        if(mMediaPlayer == null) return false;
        mMediaPlayer.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          MediaPlayerAttr outAttribute = new MediaPlayerAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetMediaPlayerCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetMediaPlayerCompleted(null,false);
                      }
                      mMediaPlayer.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mMediaPlayer.setResourceAvailable(false);

                    listener.OnGetMediaPlayerCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postMediaPlayer(MediaPlayerAttr attr, final MediaPlayerListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mMediaPlayer == null) return false;
        mMediaPlayer.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnMediaPlayerCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnMediaPlayerCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getMultichannelGroupMainWithCompletion(final MultichannelGroupMainListener listener) throws OcException {
        if(mMultichannelGroupMain == null) return false;
        mMultichannelGroupMain.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          MultichannelGroupMainAttr outAttribute = new MultichannelGroupMainAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetMultichannelGroupMainCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetMultichannelGroupMainCompleted(null,false);
                      }
                      mMultichannelGroupMain.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mMultichannelGroupMain.setResourceAvailable(false);

                    listener.OnGetMultichannelGroupMainCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean getMultichannelGroupSatelliteWithCompletion(final MultichannelGroupSatelliteListener listener) throws OcException {
        if(mMultichannelGroupSatellite == null) return false;
        mMultichannelGroupSatellite.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          MultichannelGroupSatelliteAttr outAttribute = new MultichannelGroupSatelliteAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetMultichannelGroupSatelliteCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetMultichannelGroupSatelliteCompleted(null,false);
                      }
                      mMultichannelGroupSatellite.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mMultichannelGroupSatellite.setResourceAvailable(false);

                    listener.OnGetMultichannelGroupSatelliteCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postNextWithCompletion(final NextListener listener) throws OcException {

       if(mNext == null) return false;
       mNext.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostNextCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostNextCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean getOutputSelectorWithCompletion(final OutputSelectorListener listener) throws OcException {
        if(mOutputSelector == null) return false;
        mOutputSelector.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          OutputSelectorAttr outAttribute = new OutputSelectorAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetOutputSelectorCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetOutputSelectorCompleted(null,false);
                      }
                      mOutputSelector.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mOutputSelector.setResourceAvailable(false);

                    listener.OnGetOutputSelectorCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postOutputSelector(OutputSelectorAttr attr, final OutputSelectorListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mOutputSelector == null) return false;
        mOutputSelector.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnOutputSelectorCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnOutputSelectorCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPauseWithCompletion(final PauseListener listener) throws OcException {

       if(mPause == null) return false;
       mPause.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostPauseCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostPauseCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postPlayWithCompletion(final PlayListener listener) throws OcException {

       if(mPlay == null) return false;
       mPlay.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostPlayCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostPlayCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postPlayIndex(PlayIndexInAttr attr, final PlayIndexListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlayIndex == null) return false;
        mPlayIndex.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnPlayIndexInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPlayIndexInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPlayItem(PlayItemInAttr attr, final PlayItemListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlayItem == null) return false;
        mPlayItem.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnPlayItemInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPlayItemInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getPlaylistWithCompletion(final PlaylistListener listener) throws OcException {
        if(mPlaylist == null) return false;
        mPlaylist.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          PlaylistAttr outAttribute = new PlaylistAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetPlaylistCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetPlaylistCompleted(null,false);
                      }
                      mPlaylist.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mPlaylist.setResourceAvailable(false);

                    listener.OnGetPlaylistCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postPlaylist(PlaylistAttr attr, final PlaylistListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlaylist == null) return false;
        mPlaylist.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnPlaylistCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPlaylistCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPlaylistDelete(PlaylistDeleteInAttr attr, final PlaylistDeleteListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlaylistDelete == null) return false;
        mPlaylistDelete.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          PlaylistDeleteOutAttr outAttribute = new PlaylistDeleteOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnPlaylistDeleteOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnPlaylistDeleteOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnPlaylistDeleteOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPlaylistGetHistory(PlaylistGetHistoryInAttr attr, final PlaylistGetHistoryListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlaylistGetHistory == null) return false;
        mPlaylistGetHistory.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          PlaylistGetHistoryOutAttr outAttribute = new PlaylistGetHistoryOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnPlaylistGetHistoryOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnPlaylistGetHistoryOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnPlaylistGetHistoryOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPlaylistGetRange(PlaylistGetRangeInAttr attr, final PlaylistGetRangeListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlaylistGetRange == null) return false;
        mPlaylistGetRange.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          PlaylistGetRangeOutAttr outAttribute = new PlaylistGetRangeOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnPlaylistGetRangeOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnPlaylistGetRangeOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnPlaylistGetRangeOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPlaylistInsert(PlaylistInsertInAttr attr, final PlaylistInsertListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlaylistInsert == null) return false;
        mPlaylistInsert.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          PlaylistInsertOutAttr outAttribute = new PlaylistInsertOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnPlaylistInsertOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnPlaylistInsertOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnPlaylistInsertOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPlaylistMove(PlaylistMoveInAttr attr, final PlaylistMoveListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPlaylistMove == null) return false;
        mPlaylistMove.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                        try {
                          PlaylistMoveOutAttr outAttribute = new PlaylistMoveOutAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnPlaylistMoveOutCompleted(outAttribute, true);
                        } catch(OcException | NullPointerException e) {
                          listener.OnPlaylistMoveOutCompleted(null, false);
                          e.printStackTrace();
                        }
                      }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                    ex.printStackTrace();
                   mExecutor.execute(() ->
                        listener.OnPlaylistMoveOutCompleted(null, false)
                    );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getPresetReverbWithCompletion(final PresetReverbListener listener) throws OcException {

       if(mPresetReverb == null) return false;
       mPresetReverb.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnGetPresetReverbCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnGetPresetReverbCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postPresetReverb(PresetReverbAttr attr, final PresetReverbListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPresetReverb == null) return false;
        mPresetReverb.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnPresetReverbCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPresetReverbCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postPreviousWithCompletion(final PreviousListener listener) throws OcException {

       if(mPrevious == null) return false;
       mPrevious.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostPreviousCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostPreviousCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postRegisterForSurroundSignalsWithCompletion(final RegisterForSurroundSignalsListener listener) throws OcException {

       if(mRegisterForSurroundSignals == null) return false;
       mRegisterForSurroundSignals.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostRegisterForSurroundSignalsCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostRegisterForSurroundSignalsCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postRemoveDevices(RemoveDevicesInAttr attr, final RemoveDevicesListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mRemoveDevices == null) return false;
        mRemoveDevices.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnRemoveDevicesInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnRemoveDevicesInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postSetCustomProperty(SetCustomPropertyInAttr attr, final SetCustomPropertyListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mSetCustomProperty == null) return false;
        mSetCustomProperty.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnSetCustomPropertyInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnSetCustomPropertyInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postSetPosition(SetPositionInAttr attr, final SetPositionListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mSetPosition == null) return false;
        mSetPosition.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnSetPositionInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnSetPositionInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postStopWithCompletion(final StopListener listener) throws OcException {

       if(mStop == null) return false;
       mStop.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostStopCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostStopCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean getVirtualXSoundXWithCompletion(final VirtualXSoundXListener listener) throws OcException {
        if(mVirtualXSoundX == null) return false;
        mVirtualXSoundX.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          VirtualXSoundXAttr outAttribute = new VirtualXSoundXAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetVirtualXSoundXCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetVirtualXSoundXCompleted(null,false);
                      }
                      mVirtualXSoundX.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mVirtualXSoundX.setResourceAvailable(false);

                    listener.OnGetVirtualXSoundXCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postVirtualXSoundX(VirtualXSoundXAttr attr, final VirtualXSoundXListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mVirtualXSoundX == null) return false;
        mVirtualXSoundX.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnVirtualXSoundXCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnVirtualXSoundXCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getVolumeControlWithCompletion(final VolumeControlListener listener) throws OcException {
        if(mVolumeControl == null) return false;
        mVolumeControl.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          VolumeControlAttr outAttribute = new VolumeControlAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetVolumeControlCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetVolumeControlCompleted(null,false);
                      }
                      mVolumeControl.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mVolumeControl.setResourceAvailable(false);

                    listener.OnGetVolumeControlCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postVolumeControl(VolumeControlAttr attr, final VolumeControlListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mVolumeControl == null) return false;
        mVolumeControl.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnVolumeControlCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnVolumeControlCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

}