/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr.CoordinatorState;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTBluetoothAdapterState;
import com.qualcomm.qti.iotcontrollersdk.utils.ControllerSdkUtils;
import com.qualcomm.qti.iotcontrollersdk.utils.TaskMonitor;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupInfo;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerGroupMember;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.PlayerInfo;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.controller.DeviceDiscoveryInfo.ScanType;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.DiscoveryInterface;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTCompletionCallback;
import com.qualcomm.qti.iotcontrollersdk.controller.interfaces.IoTGroupCallback;
import com.qualcomm.qti.iotcontrollersdk.controller.listeners.IoTAppListener;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer.IoTPlayerUpdatesDelegate;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTError;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice.IoTSysUpdatesDelegate;
import com.qualcomm.qti.iotcontrollersdk.model.iotsys.IoTSysInfo;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIAttr;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import org.iotivity.base.ModeType;
import org.iotivity.base.OcConnectivityType;
import org.iotivity.base.OcException;
import org.iotivity.base.OcPlatform;
import org.iotivity.base.PlatformConfig;
import org.iotivity.base.QualityOfService;
import org.iotivity.base.ServiceType;


public class IoTService {

  private final static String TAG = "IoTService";

  private static IoTService mInstance = new IoTService();

  private List<String> mDeviceTypes;
  private DiscoveryInterface mDiscovery;
  private Handler mTimer;

  private WeakReference<Context> mContextRef;

  private IoTPlayerGroupManager mGroupManager;

  private final CopyOnWriteArrayList<DeviceDiscoveryInfo> mDiscoveredDevices = new CopyOnWriteArrayList<>();
  private final CopyOnWriteArrayList<IoTPlayer> mIoTPlayers = new CopyOnWriteArrayList<>() ;
  private final CopyOnWriteArrayList<IoTDevice> mIoTDevices = new CopyOnWriteArrayList<>();

  private IoTAppListener mAppDelegate;

  private IoTPlayerUpdatesDelegate mAllPlayerDelegate;
  private IoTSysUpdatesDelegate mIoTSysDelegate;

  private boolean isStackStarted;
  private AtomicBoolean isManuallyScan = new AtomicBoolean(false);
  private AtomicBoolean isDiscovering = new AtomicBoolean(false);
  private AtomicBoolean mFastScanningMode = new AtomicBoolean(true);
  private AtomicBoolean isSurroundDiscovering = new AtomicBoolean(false);

  private IoTService() {
    mGroupManager = IoTPlayerGroupManager.getInstance();
  }

  public static void init(List<String> deviceType, DiscoveryInterface discovery) {
      mInstance.isStackStarted = false;
      mInstance.mDeviceTypes = deviceType;
      mInstance.mDiscovery = discovery;
      mInstance.mTimer = new InternalTimer(Looper.getMainLooper(),mInstance);
  }

  public static IoTService getInstance() {
    return mInstance;
  }

  public void setAppListener(IoTAppListener delegate) {
    mAppDelegate = delegate;
  }

  public void setPlayerDelegate(IoTPlayerUpdatesDelegate delegate) {
    mAllPlayerDelegate = delegate;
  }

  public void setIoTSysDelegate(IoTSysUpdatesDelegate delegate) {
    mIoTSysDelegate = delegate;
  }

  public void dispose() {

    mTimer.removeCallbacksAndMessages(null);

    mIoTPlayers.removeIf(IoTPlayer::dispose);

    mIoTDevices.removeIf(IoTDevice::dispose);

    mGroupManager.dispose();
    TaskExecutors.getExecutor().shutdown();
    OcPlatform.Shutdown();
    isStackStarted = false;
  }

  public void start(Context context) {
      mInstance.startStack(context);
      Message message = mTimer.obtainMessage(IoTConstants.START_DISCOVERY);
      mTimer.sendMessageDelayed(message,500);
  }

  public IoTPlayer getPlayerById(String id) {
    if (id == null || id.isEmpty())
      return null;
    else
      return mIoTPlayers.stream().filter(player -> id.equalsIgnoreCase(player.getDeviceId()))
          .findAny().orElse(null);
  }

  public IoTPlayer getPlayerByDeviceId(String deviceId) {
    if(deviceId == null || deviceId.isEmpty()) return null;
      return mIoTPlayers.stream().filter(player -> deviceId.equalsIgnoreCase(player.getDeviceId()))
          .findAny().orElse(null);
  }

  private void startStack(Context context) {

     if(mContextRef == null) {
      mContextRef = new WeakReference<>(context);
     }

     mFastScanningMode.set(true);

     if(!isStackStarted && mContextRef.get() != null) {
       Log.d(TAG,"Start IoTivity stack");
       PlatformConfig platformConfig = new PlatformConfig(
           mContextRef.get(),
           ServiceType.IN_PROC,
           ModeType.CLIENT,
           "0.0.0.0",
           0,
           QualityOfService.HIGH
       );

       OcPlatform.Configure(platformConfig);
       isStackStarted = true;
     }
  }

  public void stopStack() {
     if(isStackStarted) {
        Log.d(TAG,"Stop IoTivity Stack");
       mTimer.removeCallbacksAndMessages(null);
       OcPlatform.Shutdown();
       isStackStarted = false;
     }
  }

  private void goToFastScan() {
    if(mFastScanningMode.get()) {
      isDiscovering.set(false);
      reDiscovery();
    }
  }

  private static class InternalTimer extends Handler {

    private WeakReference<IoTService> mClientRef;

    InternalTimer(Looper looper, IoTService client) {
      super(looper);
      mClientRef = new WeakReference<>(client);
    }

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case IoTConstants.START_DISCOVERY:{
          try {
            if(mClientRef.get() != null)
              mClientRef.get().discovery(false);
          } catch(OcException e) {
            e.printStackTrace();
          }
        }
        break;
        case IoTConstants.DEVICE_DISCOVERY_TIMEOUT: {
          if(mClientRef.get() != null) {
            mClientRef.get().isDiscovering.compareAndSet(true,false);
            mClientRef.get().isManuallyScan.compareAndSet(true,false);
            mClientRef.get().repeat();
          }
        }
        break;

      }
    }
  }

  private void reDiscovery() {
    mTimer.removeCallbacksAndMessages(null);
    Message message = mTimer.obtainMessage(IoTConstants.START_DISCOVERY);
    if (mFastScanningMode.get())
      mTimer.sendMessageDelayed(message, IoTConstants.FAST_SCAN_TIME_INTERVAL_IN_MS);
    else
      mTimer.sendMessageDelayed(message, IoTConstants.SLOW_SCAN_TIME_INTERVAL_IN_MS);
  }

  public boolean isAutoScan() {
    return !isManuallyScan.get();
  }

  public void enableDeviceDiscovery(boolean enabled) {
      if(!enabled)
        mTimer.removeCallbacksAndMessages(null);
      else {
        try {
          if(!isDiscovering.get())
            discovery(false);
        } catch (OcException e) {
          e.printStackTrace();
        }
      }
  }

  public void forceScan() {
    isManuallyScan.compareAndSet(false, true);
    if(isManuallyScan.get()) {

      if (mTimer != null)
        mTimer.removeCallbacksAndMessages(null);

      clearPlayerAndDevices((success) -> {
        if( mAppDelegate != null)
          mAppDelegate.onPlayerReDiscovered();

        try {
          discovery(false);
        } catch (OcException e) {
          e.printStackTrace();
        }

      });
    }

  }

  public void setSurroundDiscovery(boolean enabled) {
    isSurroundDiscovering.compareAndSet(!enabled, enabled);
  }

  private void discovery(boolean isForced) throws OcException {

    Log.d(TAG, "Device Discovering...");
    isDiscovering.set(true);

    if(mDiscovery != null) {
      final long currentTime = System.currentTimeMillis();
      mDiscovery.getDeviceInfo("", OcPlatform.WELL_KNOWN_DEVICE_QUERY,
          EnumSet.of(OcConnectivityType.CT_ADAPTER_IP), ocRepresentation -> {

           List<String> types = ocRepresentation.getResourceTypes();

           TaskExecutors.getExecutor().executeOnSearchExecutor(()-> {
             boolean anyResourceFound = false;

             for (String type : types) {

               if (mDeviceTypes.contains(type)) {

                 ScanType scanType;
                 if (type.equalsIgnoreCase(IoTConstants.OCF_RESOURCE_TYPE_ALLPLAY)) {
                   scanType = ScanType.ALLPLAY;
                 } else if (type.equalsIgnoreCase(IoTConstants.OCF_RESOURCE_TYPE_IOTSYS)) {
                   scanType = ScanType.IOTSYS;
                 } else {
                   continue;
                 }

                 String msg = "";
                 for (String t : types) {
                   msg += t + " ; ";
                 }

                 String identifier;
                 try {
                   identifier = ocRepresentation.getValue(IoTConstants.OC_RSRVD_DEVICE_ID);
                 } catch (OcException e) {
                  continue;
                 }

                 Log.d(TAG, String.format("[Host:%s][Id:%s][services Type:%s]", ocRepresentation.getHost(), identifier, msg));

                 final DeviceDiscoveryInfo scanInfo = new DeviceDiscoveryInfo(ocRepresentation.getHost(), identifier, scanType);

                 if(!mDiscoveredDevices.addIfAbsent(scanInfo)) {
                   int idx = mDiscoveredDevices.indexOf(scanInfo);
                   if (idx >= 0) {
                     DeviceDiscoveryInfo existingScanInfo = mDiscoveredDevices.get(idx);
                     existingScanInfo.updateTime();
                     if(scanInfo.mType == ScanType.ALLPLAY)
                      scanInfo.setAllPlayFoundFlag(true);
                     else if(scanInfo.mType == ScanType.IOTSYS)
                      scanInfo.setIoTSysFoundFlag(true);
                   }
                 }

                 if ((!scanInfo.isAllPlayFound() || isManuallyScan.get()) && scanType == ScanType.ALLPLAY) {
                   Log.d(TAG, "**** Found Allplay resource:" + scanInfo.mHost + ", " + scanInfo.mIdentifier);

                   if (isSurroundDiscovering.get()) {
                     mAppDelegate.onSurroundPlayerDiscovered(scanInfo.mHost, scanInfo.mIdentifier);
                     isSurroundDiscovering.set(false);
                     return;
                   }

                   anyResourceFound = true;
                   scanInfo.setAllPlayFoundFlag(true);
                   IoTAllPlayClient allPlayClient = new IoTAllPlayClient(scanInfo.mHost,
                       scanInfo.mIdentifier, mDiscovery, TaskExecutors.getExecutor(), currentTime);
                   String host = scanInfo.mHost;
                   try {
                         allPlayClient.discoverWithCompletion((success) -> {

                           if (!success) {
                             Log.e(TAG, "Allplay resource discovery failed");
                             scanInfo.setAllPlayFoundFlag(false);
                             mDiscoveredDevices.removeIf(
                                 iteration -> host.equalsIgnoreCase(iteration.mHost)
                                     && iteration.mType == ScanType.ALLPLAY);
                             return;
                           }

                           try {
                             IoTPlayer player = new IoTPlayer(allPlayClient);
                             allPlayClient.createResourceClientsForHost(host);
                             player.setDelegate(mAllPlayerDelegate);
                             player.updateWithCompletion((status) -> {
                               if (status) {
                                 boolean isPlayerExisted = mIoTPlayers.stream().anyMatch(p->p.getPlayerId().equalsIgnoreCase(player.getPlayerId()));

                                 if (!isPlayerExisted) {
                                   mIoTPlayers.add(player);
                                   Log.d(TAG, String.format("[TEST]A new player was found in %d ms, device host: %s",
                                       System.currentTimeMillis() - player.getDeviceDiscoveryTime(), host));

                                   if(mAppDelegate != null) {
                                     mGroupManager.processPlayerUpdate(player, this, mAppDelegate);
                                   }
                                 }
                                 if(mIoTPlayers.size() > 3) {
                                   mFastScanningMode.compareAndSet(true, false);
                                 }
                               } else {
                                 mDiscoveredDevices.removeIf(
                                     iteration -> host.equalsIgnoreCase(iteration.mHost)
                                         && iteration.mType == ScanType.ALLPLAY);
                                 Log.e(TAG, "Failed to fetch resources for player:" + host);
                               }
                            });
                           } catch (OcException e) {
                               mDiscoveredDevices.removeIf(iteration->host.equalsIgnoreCase(iteration.mHost) && iteration.mType==ScanType.ALLPLAY);
                               e.printStackTrace();
                           }
                         });
                     } catch (OcException e) {
                       e.printStackTrace();
                     }
                 } else if (scanInfo.isAllPlayFound() && scanType == ScanType.ALLPLAY) {
                   anyResourceFound = true;
                 }

                 if (!anyResourceFound && (!scanInfo.isIoTSysFound() || isManuallyScan.get()) && scanType == ScanType.IOTSYS) {
                   Log.d(TAG, "**** Found a IoTSys resource:" + scanInfo.mHost + ", "
                       + scanInfo.mIdentifier);
                   anyResourceFound = true;
                   scanInfo.setIoTSysFoundFlag(true);
                   final IoTSysClient sysController = new IoTSysClient(scanInfo.mHost,
                       scanInfo.mIdentifier, mDiscovery, TaskExecutors.getExecutor());

                   final String hostName = ControllerSdkUtils.stripHostName(scanInfo.mHost);
                   final String scanInfoId = scanInfo.mIdentifier;
                   try {

                      sysController.discoverWithCompletion((success) -> {
                        if (!success) {
                          Log.e(TAG, "IoTSys resource discovery failed");
                          scanInfo.setIoTSysFoundFlag(false);
                          return;
                        }

                        Log.d(TAG, "IoTService find IoT Sys resources !");

                        boolean isDeviceExisted = false;
                        boolean isInOtherNetwork = false;
                        for (IoTDevice dev : mIoTDevices) {
                          if (dev.getId().equalsIgnoreCase(scanInfoId)) {
                            isDeviceExisted = true;
                            break;
                          }
                        }

                       if(!isDeviceExisted) {

                         if(isInOtherNetwork) {
                           Log.d(TAG, "IoT device rejoin ...");
                           mIoTDevices.removeIf(iter -> iter.getId().equalsIgnoreCase(scanInfoId));
                         } else {
                           Log.d(TAG, "New IoT sys device found !:" + mIoTDevices.size() + 1);
                         }

                         final IoTDevice device = new IoTDevice(sysController);
                         device.setDelegate(mIoTSysDelegate);
                         device.setId(scanInfoId);

                         try {
                             sysController.createResourceClientsForHost(sysController.getHostName());

                             device.updateWithCompletion((status) -> {
                               if (status) {
                                  mIoTDevices.add(device);
                                 if (mAppDelegate != null) {
                                   mAppDelegate.onDeviceAdded(device);
                                 }
                               } else {
                                   mDiscoveredDevices.removeIf(
                                       iteration -> hostName.equalsIgnoreCase(iteration.mHost)
                                           && iteration.mType == ScanType.IOTSYS);
                               }
                             });
                           } catch (OcException e) {
                             mDiscoveredDevices
                                 .removeIf(iteration -> hostName.equalsIgnoreCase(iteration.mHost)
                                       && iteration.mType == ScanType.IOTSYS);
                             e.printStackTrace();
                           }
                        }
                     });
                   } catch (OcException e) {
                     e.printStackTrace();
                   }

                 } else if (scanInfo.isIoTSysFound() && scanType == ScanType.IOTSYS) {
                   anyResourceFound = true;
                 }
              }
           }

           isDiscovering.compareAndSet(true, false);
           isManuallyScan.compareAndSet(true, false);

           if (!anyResourceFound) {
             mFastScanningMode.compareAndSet(false, true);
           }
          });
        });

        if(!isForced)
          repeat();
    }
  }

  private void repeat() {
    updateScanResult();
    reDiscovery();
  }

  public void clearPlayerAndDevices(final IoTCompletionCallback callback) {

    mDiscoveredDevices.clear();


    if(!mIoTDevices.isEmpty()) {
      mIoTDevices.forEach(device -> device.dispose((success) -> {
      }));

      TaskExecutors.getExecutor().executeOnMain(mIoTDevices::clear);
    }


    if (!mIoTPlayers.isEmpty()) {

      mIoTPlayers.forEach(player -> player.dispose((success) -> {

      }));

      TaskExecutors.getExecutor().executeOnMain(() -> {

        mIoTPlayers.clear();

        mGroupManager.clear();

        Log.d(TAG, "Stop observing success!");
        callback.onCompletion(true);
      });
    } else {
      mGroupManager.clear();
      callback.onCompletion(true);
    }
  }

  private void updateScanResult() {

      List<DeviceDiscoveryInfo> unAvailableList = new ArrayList<>();

      mDiscoveredDevices.forEach( scanResult -> {
        long age = scanResult.getLastUpdate();
        boolean isAvailable = (System.currentTimeMillis() - age) < IoTConstants.MAX_TIME_DELTA_IN_MS;

        if (!isAvailable) {
          unAvailableList.add(new DeviceDiscoveryInfo(scanResult));
          mDiscoveredDevices.remove(scanResult);
        }
      });

      unAvailableList.forEach(scanInfo->{

        String ageTime = DateFormat.getInstance().format(scanInfo.mLastUpdated);
        Log.d(TAG, String.format("Scan devices result:%s, identifier:%s, %s", scanInfo.mHost,scanInfo.mIdentifier, ageTime));
        if (scanInfo.mType == ScanType.ALLPLAY) {

          Log.d(TAG, "Remove IoT Player:" + scanInfo.mHost + ","+scanInfo.mIdentifier);

          boolean isEmpty;
          mIoTPlayers.removeIf(player -> {
            boolean isFound = player.getPlayerId().equalsIgnoreCase(scanInfo.mIdentifier);
            if(isFound) {
              Log.d(TAG, String.format("Removing players:%s, identifier:%s, %d", scanInfo.mHost,scanInfo.mIdentifier, scanInfo.mLastUpdated));
              mGroupManager.processDeadPlayer(player, mAppDelegate);
            }
            return isFound;
          });

          isEmpty = mIoTPlayers.isEmpty();

          if (isEmpty) {
            mFastScanningMode.compareAndSet(false, true);
            goToFastScan();
          }

        } else if (scanInfo.mType == ScanType.IOTSYS) {
          Log.d(TAG, "Remove IoT Device with host name:" + scanInfo.mIdentifier);
          removeIoTDeviceById(scanInfo.mIdentifier);
        }
      });

      unAvailableList.clear();
  }

  public List<IoTGroup> getAvailableGroups() {
    return mGroupManager.getAvailableGroups();
  }

  public IoTGroup getGroupById(String id) {
    return mGroupManager.getGroupById(id);
  }

  public IoTGroup getGroupByPlayerId(String id) {
    return mGroupManager.getGroupByPlayerId(id);
  }

  public List<PlayerGroupInfo> getGroupInfo(String id) {
    List<PlayerGroupInfo> retList = null;
    IoTPlayer player;
    player = mIoTPlayers.stream().filter(p->p.getPlayerId().equalsIgnoreCase(id)).findAny().orElse(null);

    if( player != null)
      retList = player.getGroupsInfo().getList();

    return retList;
  }

  public List<IoTPlayer> getAllPlayers() {
    return mIoTPlayers;
  }

  public List<IoTDevice> getAllDevices() {
    return mIoTDevices;
  }

  private boolean createGroup(String playerId, String groupName, IoTGroupCallback callback) {
    if(playerId == null || playerId.isEmpty()) {
      Log.e(TAG, "Invalid player Id");
      return false;
    }

    if(groupName == null || groupName.isEmpty()) {
      Log.e(TAG, "Invalid group name");
      return false;
    }

    IoTPlayer player = getPlayerById(playerId);
    if(player == null) return false;

    player.createGroup(groupName,callback);
    return true;
  }

  public boolean addPlayers(List<String> playerIds, final String groupId, IoTCompletionCallback callback) {

    if(playerIds == null || playerIds.size() == 0) {
      Log.e(TAG, "No player Ids");
      return false;
    }

    TaskMonitor monitor = new TaskMonitor(playerIds.size());
    for(String id: playerIds) {
        IoTPlayer player = getPlayerById(id);
        if(player !=null) {
          player.addPlayerInGroup(groupId, status -> {
            monitor.increment(status);
            if (monitor.isDone()) {
              callback.onCompletion(monitor.getResult());
            }
          });
        } else {
          monitor.decrementTask();
        }
    }
    return true;
  }

  public void addPlayerInNewGroup(final List<String> playerIds, String groupName, IoTCompletionCallback callback) {
    TaskExecutors.getExecutor().executeGroupExecutor(()-> {

      boolean isGroupCreated = createGroup(playerIds.get(0),groupName,(newGroupId, success) -> {
        if(success) {
          TaskExecutors.getExecutor().scheduleOnGroupExecutor( ()->
            addPlayers(playerIds, newGroupId, status -> {
                  if(status) {
                    Log.d(TAG,"IoT Response:Add player ids in the group success!");
                  } else {
                    Log.e(TAG,"IoT Response:Add player ids in the group failed!");
                  }
                  callback.onCompletion(status);
                })
          ,1000);
        }
      });

      if(!isGroupCreated) {
        Log.e(TAG,"player in group is null!");
        callback.onCompletion(false);
      }

    });

  }

  public boolean removePlayers(List<String> playerIds, final String groupId, IoTCompletionCallback callback) {
    if(playerIds == null || playerIds.size() == 0) {
      Log.e(TAG, "No player Ids");
      return false;
    }

    // Validate Group ID
    IoTGroup group = getGroupById(groupId);
    if(group == null || group.isSinglePlayer()) {
      Log.e(TAG,"Group ID is invalid :" + groupId);
      return false;
    }

    TaskMonitor counter = new TaskMonitor(playerIds.size());
    for(String id: playerIds) {
        IoTPlayer player = getPlayerByDeviceId(id);
        if(player !=null)
          player.deletePlayerFromGroup(status -> {
            counter.increment(status);
            if(counter.isDone()) {
              callback.onCompletion(counter.getResult());
            }
          });
    }
    return true;

  }

  public IoTError deleteGroup(final String groupId,IoTCompletionCallback callback) {
    // Validate Group ID
    IoTGroup group = getGroupById(groupId);
    if(group == null || group.isSinglePlayer()) {
      Log.e(TAG,"Group ID is invalid :" + groupId);
      return IoTError.INVALID_OBJECT;
    }

    IoTPlayer player = group.getLeadPlayer();
    if( player == null) {
      List<IoTPlayer> players = getAllPlayers();
      player = players.stream().filter(p ->
          p.getGroupInfo().stream().anyMatch(info -> info.mGroupId.equalsIgnoreCase(groupId))
      ).findAny().orElse(null);
      Log.d(TAG,"find a player which know of this empty group!");
    }

    if(player != null) {
      player.deleteGroup(groupId, status ->
          callback.onCompletion(status)
      );
    }

    return IoTError.NONE;
  }

  public IoTError renameGroup(String groupId, String newName, IoTCompletionCallback callback) {
    // Validate Group ID
    IoTGroup group = getGroupById(groupId);
    if(group == null || group.isSinglePlayer()) {
      Log.e(TAG,"Group ID is invalid :" + groupId);
      return IoTError.INVALID_OBJECT;
    }

    IoTPlayer player = group.getLeadPlayer();
    if( player == null) {
      List<IoTPlayer> players = getAllPlayers();
      player = players.stream().filter(p ->
          p.getGroupInfo().stream().anyMatch(info -> info.mGroupId.equalsIgnoreCase(groupId))
      ).findAny().orElse(null);

      Log.d(TAG,"find a player which know of this empty group!");
    }

    if(player != null) {
      player.renameGroup(groupId, newName, status ->
          callback.onCompletion(status)
      );
    }

    return IoTError.NONE;
  }

  public List<PlayerGroupInfo> getGroupsInfo() {
     return mGroupManager.getPlayersGroupInfo();
  }

  public void updateGroupInfo(IoTPlayer player) {
    mGroupManager.processPlayerUpdate(player, this, mAppDelegate);
  }

  public void startAvsOnBoarding(String host,IoTCompletionCallback callback) {
    IoTDevice device = getIoTSysDeviceByHost(host);
    if(device != null) {
      device.startAvsOnBoarding(callback);
    }
  }

  public void rebootDevice(String id, IoTCompletionCallback callback) {
    IoTDevice device = getIoTSysDeviceByHost(id);
    if(device != null) {
      device.rebootDevice(callback);
    }
  }

  public void setDeviceName(String host, String name, IoTCompletionCallback callback) {
    IoTDevice device = getIoTSysDeviceByHost(host);
    if(device != null) {
      device.setDeviceName(name,callback);
    }
  }

  public void setZigbeeName(String host, String name, int zgId, IoTCompletionCallback callback) {
    IoTDevice device = getIoTSysDeviceByHost(host);
    if(device != null) {
      device.startSetZbDeviceName(name,zgId,callback);
    }
  }

  public List<IoTSysInfo> getIoTSysInfoList() {
    List<IoTSysInfo> ioTSysInfoList = new ArrayList<>();

    mIoTDevices.forEach(device ->  {
      IoTSysInfo info = new IoTSysInfo(device.getName(),device.getId(), device.getHostName(), device.isVoiceUIEnabled(), device.isAvsOnBoarded());
      info.isBluetoothOnBoarded = device.isBluetoothAvailable();
      info.updateSystemInfo(device);
      ioTSysInfoList.add(info);
    });
    return ioTSysInfoList;
  }

  public IoTSysInfo getIoTSysInfo(String host) {
    IoTSysInfo retVal = null;
    String hostName = ControllerSdkUtils.stripHostName(host);
    IoTDevice dev = mIoTDevices.stream().filter(device ->  {
        String name = ControllerSdkUtils.stripHostName(device.getHostName());
        return (hostName != null && hostName.equalsIgnoreCase(name));
      }).findAny().orElse(null);

    if(dev != null) {
      retVal = new IoTSysInfo(dev.getName(), dev.getId(), dev.getHostName(),
              dev.isVoiceUIEnabled(), dev.isAvsOnBoarded());
      retVal.isVoicdUIEnabled = dev.isVoiceUIEnabled();
      retVal.isBluetoothOnBoarded = dev.isBluetoothAvailable();
      retVal.isAVSWakeWord = dev.isAVSWakeword();
      retVal.mAVSLanguage = dev.getAVSLocale();
    }
    return retVal;
  }

  private IoTDevice getIoTSysDeviceByHost(String host) {
    String hostName = ControllerSdkUtils.stripHostName(host);
    return mIoTDevices.stream().filter( device ->  {
      String name = ControllerSdkUtils.stripHostName(device.getHostName());
      return (hostName != null && hostName.equalsIgnoreCase(name));
    }).findAny().orElse(null);
  }

  public VoiceUIAttr getVoiceSetting(final String host) {
    VoiceUIAttr retAttr = null;

    String hostName = ControllerSdkUtils.stripHostName(host);
    IoTDevice dev = mIoTDevices.stream().filter( device ->  {
      String id = ControllerSdkUtils.stripHostName(device.getHostName());
      return hostName != null && hostName.equalsIgnoreCase(id);
      }).findAny().orElse(null);

    if(dev != null)
      retAttr = dev.getVoiceSetting();

    return retAttr;
  }

  public IoTSysInfo getDeviceByHost(String host) {
    IoTSysInfo retVal = null;
    if(host == null || host.trim().length() == 0) return null;
    String hostName = ControllerSdkUtils.stripHostName(host);

    IoTDevice dev = mIoTDevices.stream().filter(device -> {
      String name = ControllerSdkUtils.stripHostName(device.getHostName());
      return (name != null && name.equalsIgnoreCase(hostName));
    }).findAny().orElse(null);

    if(dev != null) {
        retVal = new IoTSysInfo(dev.getName(), dev.getId(), dev.getHostName(),
            dev.isVoiceUIEnabled(), dev.isAvsOnBoarded());
        retVal.isBluetoothOnBoarded = (dev.getBluetoothAdapterState() == IoTBluetoothAdapterState.Enabled);
        retVal.isZigBeeOnBoarded = dev.isZigbeeEnabled();
    }
    return retVal;
  }

  public String getDefaultVoiceUIClient(String host) {
    String uiClient = "";
    String hostName = ControllerSdkUtils.stripHostName(host);
    IoTDevice dev = mIoTDevices.stream().filter(device -> {
      String name = ControllerSdkUtils.stripHostName(device.getHostName());
      return ( name != null && name.equalsIgnoreCase(hostName));
    }).findAny().orElse(null);

    if( dev != null) {
      uiClient = dev.getDefaultVoiceUIClientFromPlayer();
    }
    return uiClient;
  }

  private String mLogMessage = "";
  public List<PlayerInfo> getPlayerInfoList() {
    ArrayList<PlayerInfo> playerInfoList = new ArrayList<>();
    List<PlayerGroupInfo> groupsInfo = getGroupsInfo();

    mLogMessage = "";
    for(PlayerGroupInfo grpInfo : groupsInfo) {

      grpInfo.mMembers.forEach(mem ->
          mLogMessage += String
              .format("[id:%s, name:%s, host:%s] ", mem.deviceId, mem.displayName, mem.host)
      );
      Log.d(TAG, String
          .format("[GrpInfo] Id: %s, Name:%s, members:%s, isSingle:%b", grpInfo.mGroupId,
              grpInfo.mGroupName, mLogMessage, grpInfo.isSinglePlayer));

      if (grpInfo.mMembers == null || grpInfo.mMembers.isEmpty()) {
        if(!grpInfo.isSinglePlayer && grpInfo.mGroupId != null) {
          PlayerInfo newPlayerInfo = new PlayerInfo(null, null,
              null, true);
          newPlayerInfo.isEmptyGroup = true;
          newPlayerInfo.addGroupId(grpInfo.mGroupId);
          newPlayerInfo.addGroupName(grpInfo.mGroupName);
          playerInfoList.add(newPlayerInfo);
        }
      } else {
        for (PlayerGroupMember playerInfo : grpInfo.mMembers) {
          if (playerInfo.available) {
            IoTPlayer player = getPlayerById(playerInfo.deviceId);
            String displayName = playerInfo.displayName;
            boolean isAvailable = playerInfo.available && playerInfo.isLicensed;
            if (player != null)
              displayName = player.getName();

            PlayerInfo newPlayerInfo = new PlayerInfo(playerInfo.host, displayName,
                playerInfo.deviceId, isAvailable);

            if (!grpInfo.isSinglePlayer) {
              newPlayerInfo.addGroupId(grpInfo.mGroupId);
              newPlayerInfo.addGroupName(grpInfo.mGroupName);
            }

            if (!playerInfoList.contains(newPlayerInfo)) {
              playerInfoList.add(newPlayerInfo);
            } else {
              if (!grpInfo.isSinglePlayer) {
                int idx = playerInfoList.indexOf(newPlayerInfo);
                playerInfoList.get(idx).addGroupId(grpInfo.mGroupId);
                playerInfoList.get(idx).addGroupName(grpInfo.mGroupName);
              }
            }
          }
        }
      }
    }
    return playerInfoList;
  }

  private void removeIoTDeviceById(String id) {
    mIoTDevices.removeIf(device -> device.getId().equalsIgnoreCase(id));
  }

  public boolean isZbCoordinatorExisted(String id) {
    return mIoTDevices.stream()
        .anyMatch(device -> (!device.getId().equalsIgnoreCase(id) && device.getCoordinatorState() == CoordinatorState.kNominated));
  }

  public boolean isZbCoordinator(String deviceId) {
    return mIoTDevices.stream().anyMatch(device->device.getId().equalsIgnoreCase(deviceId) && device.getCoordinatorState() == CoordinatorState.kNominated);
  }


  public boolean enableVoiceUI(String id, boolean enabled, IoTCompletionCallback callback) {
    List<IoTDevice> deviceList = getAllDevices();
    IoTDevice device = deviceList.stream().filter(d->d.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    if(device != null) {
      return device.enableVoiceUI(enabled, callback);
    }
    return false;
  }

  public boolean enableWakeWord(String id, String clientName, boolean enabled, IoTCompletionCallback callback) {
    List<IoTDevice> deviceList = getAllDevices();
    IoTDevice device = deviceList.stream().filter(d->d.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    if(device != null) {
      return device.enableWakeWord(clientName,enabled, callback);
    }
    return false;
  }

  public boolean removeAVSCredential(String id, IoTCompletionCallback callback) {
    List<IoTDevice> deviceList = getAllDevices();
    IoTDevice device = deviceList.stream().filter(d->d.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    if(device != null) {
      Log.d(TAG,"remove AVS credential");
      return device.removeAVSCredential(callback);
    }
    return false;
  }

}
