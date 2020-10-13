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
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.*;
import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.clients.*;
import com.qualcomm.qti.iotcontrollersdk.iotsys.listeners.*;
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


public class IoTSysClient extends ControllerInterface {

    private final static String TAG = "IoTSysClient";

    public enum IoTSysObserveOrigin {
        IoTSysObserveOriginBluetooth,
        IoTSysObserveOriginNetwork,
        IoTSysObserveOriginSystem,
        IoTSysObserveOriginVoiceUI,
        IoTSysObserveOriginZigbee,
    }

    public interface IoTSysClientObserver {
      void onRegistration(IoTSysObserveOrigin type);
      void deRegistration(IoTSysObserveOrigin type);
      void onUpdate(IoTSysObserveOrigin type, OcRepresentation rep);
      void onFailed(IoTSysObserveOrigin type, String exception);
    }

    TaskExecutors mExecutor;
    IoTSysClientObserver mObserver;

    /** The followings are resource clients instance variables */
    private BluetoothResourceClient mBluetooth;
    private CancelBtDiscoveryModeResourceClient mCancelBtDiscoveryMode;
    private ClearBtPairedListResourceClient mClearBtPairedList;
    private ConnectBtDeviceResourceClient mConnectBtDevice;
    private DeleteCredentialResourceClient mDeleteCredential;
    private DisconnectBtDeviceResourceClient mDisconnectBtDevice;
    private EnableBtAdapterResourceClient mEnableBtAdapter;
    private EnableVoiceUIClientResourceClient mEnableVoiceUIClient;
    private EnableZbAdapterResourceClient mEnableZbAdapter;
    private EnterBtDiscoveryModeResourceClient mEnterBtDiscoveryMode;
    private FactoryResetResourceClient mFactoryReset;
    private FormZbNetworkResourceClient mFormZbNetwork;
    private GetBtAdapterStateResourceClient mGetBtAdapterState;
    private GetBtPairedDevicesResourceClient mGetBtPairedDevices;
    private JoinZbNetworkResourceClient mJoinZbNetwork;
    private NetworkResourceClient mNetwork;
    private PairBtDeviceResourceClient mPairBtDevice;
    private RestartResourceClient mRestart;
    private SetZbFriendlyNameResourceClient mSetZbFriendlyName;
    private StartAVSOnboardingResourceClient mStartAVSOnboarding;
    private StartBtScanResourceClient mStartBtScan;
    private StartZbJoiningResourceClient mStartZbJoining;
    private StopBtScanResourceClient mStopBtScan;
    private SystemResourceClient mSystem;
    private UnpairBtDeviceResourceClient mUnpairBtDevice;
    private VoiceUIResourceClient mVoiceUI;
    private ZigbeeResourceClient mZigbee;

    /*private List<String> mDiscoveryResourceTypes;*/

    /**
     * Class constructor
     */

    public IoTSysClient(final String host, final String id, DiscoveryInterface discovery, final TaskExecutors executor) {

        mHost = host;
        mId = id;
        mDeviceDiscovery = discovery;
        mUriPrefix = "/qti/iotsys";
        mExecutor = executor;
/*        mDiscoveryResourceTypes = Arrays.asList(

                  ""qti.iotsys.r.bluetooth"",
                  ""qti.iotsys.r.cancelBtDiscoveryMode"",
                  ""qti.iotsys.r.clearBtPairedList"",
                  ""qti.iotsys.r.connectBtDevice"",
                  ""qti.iotsys.r.deleteCredential"",
                  ""qti.iotsys.r.disconnectBtDevice"",
                  ""qti.iotsys.r.enableBtAdapter"",
                  ""qti.iotsys.r.enableVoiceUIClient"",
                  ""qti.iotsys.r.enableZbAdapter"",
                  ""qti.iotsys.r.enterBtDiscoveryMode"",
                  ""qti.iotsys.r.factoryReset"",
                  ""qti.iotsys.r.formZbNetwork"",
                  ""qti.iotsys.r.getBtAdapterState"",
                  ""qti.iotsys.r.getBtPairedDevices"",
                  ""qti.iotsys.r.joinZbNetwork"",
                  ""qti.iotsys.r.network"",
                  ""qti.iotsys.r.pairBtDevice"",
                  ""qti.iotsys.r.restart"",
                  ""qti.iotsys.r.setZbFriendlyName"",
                  ""qti.iotsys.r.startAVSOnboarding"",
                  ""qti.iotsys.r.startBtScan"",
                  ""qti.iotsys.r.startZbJoining"",
                  ""qti.iotsys.r.stopBtScan"",
                  ""qti.iotsys.r.system"",
                  ""qti.iotsys.r.unpairBtDevice"",
                  ""qti.iotsys.r.voiceUI"",
                  ""qti.iotsys.r.zigbee"",

                  "qti.iotsys.r.system"
        );
*/
    }

    public void registerObserver(IoTSysClientObserver observer) {
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

            mDeviceDiscovery.findResource(mHost,OcPlatform.WELL_KNOWN_QUERY,EnumSet.of(OcConnectivityType.CT_ADAPTER_IP),
                                 new OnResourceFoundListener() {

                                   private AtomicInteger mCounter = new AtomicInteger(0);

                                   @Override
                                   public void onResourceFound(OcResource resource) {
                                      TaskExecutors.getExecutor().executeOnIoTSysResourceExecutor(() -> {

                                          if (resource.getUri().contains(mUriPrefix)) {

                                              Log.d(TAG,String.format("IoTSys Resource Found URI:%s, %d",
                                                          resource.getUri(),resource.getResourceTypes().size()));

                                              if(mCounter.incrementAndGet()  == IoTConstants.IOTSYS_RESOURCE_NUMBER) {
                                                Log.d(TAG,"All required IoTSys resources found!");
                                                callback.onCompletion(true);
                                              }
                                          }
                                      });
                                   }

                                   @Override
                                   public synchronized void onFindResourceFailed(Throwable ex, String uri) {
                                        Log.e(TAG,String.format("Finding Resource failed:%s",uri));
                                        callback.onCompletion(false);
                                        ex.printStackTrace();
                                   }
                                 }
                             );
            }
    }

    public void createResourceClientsForHost(String host) throws OcException {

        mBluetooth = new BluetoothResourceClient(host);
        mBluetooth.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {

            @Override
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                      mObserver.onRegistration(IoTSysObserveOrigin.IoTSysObserveOriginBluetooth);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                      mObserver.deRegistration(IoTSysObserveOrigin.IoTSysObserveOriginBluetooth);
                    } else if(mObserver != null) {
                      mObserver.onUpdate(IoTSysObserveOrigin.IoTSysObserveOriginBluetooth,ocRepresentation);
                    }
                });

            }

            @Override
            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTSysObserveOrigin.IoTSysObserveOriginBluetooth,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mBluetooth);
        mCancelBtDiscoveryMode = new CancelBtDiscoveryModeResourceClient(host);
        mClearBtPairedList = new ClearBtPairedListResourceClient(host);
        mConnectBtDevice = new ConnectBtDeviceResourceClient(host);
        mDeleteCredential = new DeleteCredentialResourceClient(host);
        mDisconnectBtDevice = new DisconnectBtDeviceResourceClient(host);
        mEnableBtAdapter = new EnableBtAdapterResourceClient(host);
        mEnableVoiceUIClient = new EnableVoiceUIClientResourceClient(host);
        mEnableZbAdapter = new EnableZbAdapterResourceClient(host);
        mEnterBtDiscoveryMode = new EnterBtDiscoveryModeResourceClient(host);
        mFactoryReset = new FactoryResetResourceClient(host);
        mFormZbNetwork = new FormZbNetworkResourceClient(host);
        mGetBtAdapterState = new GetBtAdapterStateResourceClient(host);
        mGetBtPairedDevices = new GetBtPairedDevicesResourceClient(host);
        mJoinZbNetwork = new JoinZbNetworkResourceClient(host);
        mNetwork = new NetworkResourceClient(host);
        mNetwork.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {

            @Override
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                      mObserver.onRegistration(IoTSysObserveOrigin.IoTSysObserveOriginNetwork);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                      mObserver.deRegistration(IoTSysObserveOrigin.IoTSysObserveOriginNetwork);
                    } else if(mObserver != null) {
                      mObserver.onUpdate(IoTSysObserveOrigin.IoTSysObserveOriginNetwork,ocRepresentation);
                    }
                });

            }

            @Override
            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTSysObserveOrigin.IoTSysObserveOriginNetwork,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mNetwork);
        mPairBtDevice = new PairBtDeviceResourceClient(host);
        mRestart = new RestartResourceClient(host);
        mSetZbFriendlyName = new SetZbFriendlyNameResourceClient(host);
        mStartAVSOnboarding = new StartAVSOnboardingResourceClient(host);
        mStartBtScan = new StartBtScanResourceClient(host);
        mStartZbJoining = new StartZbJoiningResourceClient(host);
        mStopBtScan = new StopBtScanResourceClient(host);
        mSystem = new SystemResourceClient(host);
        mSystem.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {

            @Override
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                      mObserver.onRegistration(IoTSysObserveOrigin.IoTSysObserveOriginSystem);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                      mObserver.deRegistration(IoTSysObserveOrigin.IoTSysObserveOriginSystem);
                    } else if(mObserver != null) {
                      mObserver.onUpdate(IoTSysObserveOrigin.IoTSysObserveOriginSystem,ocRepresentation);
                    }
                });

            }

            @Override
            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTSysObserveOrigin.IoTSysObserveOriginSystem,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mSystem);
        mUnpairBtDevice = new UnpairBtDeviceResourceClient(host);
        mVoiceUI = new VoiceUIResourceClient(host);
        mVoiceUI.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {

            @Override
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                      mObserver.onRegistration(IoTSysObserveOrigin.IoTSysObserveOriginVoiceUI);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                      mObserver.deRegistration(IoTSysObserveOrigin.IoTSysObserveOriginVoiceUI);
                    } else if(mObserver != null) {
                      mObserver.onUpdate(IoTSysObserveOrigin.IoTSysObserveOriginVoiceUI,ocRepresentation);
                    }
                });

            }

            @Override
            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTSysObserveOrigin.IoTSysObserveOriginVoiceUI,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mVoiceUI);
        mZigbee = new ZigbeeResourceClient(host);
        mZigbee.observe(ObserveType.OBSERVE.getValue(),new HashMap<>(),new OnObserveListener() {

            @Override
            public void onObserveCompleted(List<OcHeaderOption> headerOptionList,
                                       OcRepresentation ocRepresentation,
                                       int sequenceNumber) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {

                    if (OcResource.OnObserveListener.REGISTER == sequenceNumber) {
                      mObserver.onRegistration(IoTSysObserveOrigin.IoTSysObserveOriginZigbee);
                    } else if (OcResource.OnObserveListener.MAX_SEQUENCE_NUMBER + 1 == sequenceNumber) {
                      mObserver.deRegistration(IoTSysObserveOrigin.IoTSysObserveOriginZigbee);
                    } else if(mObserver != null) {
                      mObserver.onUpdate(IoTSysObserveOrigin.IoTSysObserveOriginZigbee,ocRepresentation);
                    }
                });

            }

            @Override
            public void onObserveFailed(Throwable ex) {
                TaskExecutors.getExecutor().executeIoTSysNotification(() -> {
                    if(mObserver != null) {
                        mObserver.onFailed(IoTSysObserveOrigin.IoTSysObserveOriginZigbee,ex.toString());
                    }
                });
            }
        });
        mObserverList.add(mZigbee);

    }

    @Override
    public void stopObserving() {
       try {
            IoTBaseResourceClient client = mObserverList.pop();
            if(client != null) {
              if(!client.stopObserving())
                cancelNextObserver();
            }
/*         mBluetooth.stopObserving();
         mNetwork.stopObserving();
         mSystem.stopObserving();
         mVoiceUI.stopObserving();
         mZigbee.stopObserving();
*/
       } catch(OcException e) {
         e.printStackTrace();
       }
    }


    public boolean getBluetoothWithCompletion(final BluetoothListener listener) throws OcException {
        if(mBluetooth == null) return false;
        mBluetooth.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          BluetoothAttr outAttribute = new BluetoothAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetBluetoothCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetBluetoothCompleted(null,false);
                      }
                      mBluetooth.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mBluetooth.setResourceAvailable(false);

                    listener.OnGetBluetoothCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postCancelBtDiscoveryModeWithCompletion(final CancelBtDiscoveryModeListener listener) throws OcException {

       if(mCancelBtDiscoveryMode == null) return false;
       mCancelBtDiscoveryMode.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostCancelBtDiscoveryModeCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostCancelBtDiscoveryModeCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postClearBtPairedListWithCompletion(final ClearBtPairedListListener listener) throws OcException {

       if(mClearBtPairedList == null) return false;
       mClearBtPairedList.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostClearBtPairedListCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostClearBtPairedListCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postConnectBtDevice(AddressInAttr attr, final ConnectBtDeviceListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mConnectBtDevice == null) return false;
        mConnectBtDevice.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnAddressInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnAddressInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postDeleteCredential(ClientInAttr attr, final DeleteCredentialListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mDeleteCredential == null) return false;
        mDeleteCredential.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnClientInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnClientInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postDisconnectBtDevice(AddressInAttr attr, final DisconnectBtDeviceListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mDisconnectBtDevice == null) return false;
        mDisconnectBtDevice.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnAddressInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnAddressInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postEnableBtAdapter(EnableBtAdapterInAttr attr, final EnableBtAdapterListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mEnableBtAdapter == null) return false;
        mEnableBtAdapter.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnEnableBtAdapterInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnEnableBtAdapterInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postEnableVoiceUIClient(SelectClientInAttr attr, final EnableVoiceUIClientListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mEnableVoiceUIClient == null) return false;
        mEnableVoiceUIClient.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnSelectClientInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnSelectClientInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postEnableZbAdapter(EnableZbAdapterInAttr attr, final EnableZbAdapterListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mEnableZbAdapter == null) return false;
        mEnableZbAdapter.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnEnableZbAdapterInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnEnableZbAdapterInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postEnterBtDiscoveryMode(TimeoutSecInAttr attr, final EnterBtDiscoveryModeListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mEnterBtDiscoveryMode == null) return false;
        mEnterBtDiscoveryMode.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnTimeoutSecInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnTimeoutSecInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postFactoryResetWithCompletion(final FactoryResetListener listener) throws OcException {

       if(mFactoryReset == null) return false;
       mFactoryReset.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostFactoryResetCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostFactoryResetCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postFormZbNetworkWithCompletion(final FormZbNetworkListener listener) throws OcException {

       if(mFormZbNetwork == null) return false;
       mFormZbNetwork.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostFormZbNetworkCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostFormZbNetworkCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postGetBtAdapterStateWithCompletion(final GetBtAdapterStateListener listener) throws OcException {

       if(mGetBtAdapterState == null) return false;
       mGetBtAdapterState.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostGetBtAdapterStateCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostGetBtAdapterStateCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postGetBtPairedDevicesWithCompletion(final GetBtPairedDevicesListener listener) throws OcException {

       if(mGetBtPairedDevices == null) return false;
       mGetBtPairedDevices.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostGetBtPairedDevicesCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostGetBtPairedDevicesCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postJoinZbNetworkWithCompletion(final JoinZbNetworkListener listener) throws OcException {

       if(mJoinZbNetwork == null) return false;
       mJoinZbNetwork.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostJoinZbNetworkCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostJoinZbNetworkCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean getNetworkWithCompletion(final NetworkListener listener) throws OcException {
        if(mNetwork == null) return false;
        mNetwork.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          NetworkAttr outAttribute = new NetworkAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetNetworkCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetNetworkCompleted(null,false);
                      }
                      mNetwork.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mNetwork.setResourceAvailable(false);

                    listener.OnGetNetworkCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postPairBtDevice(AddressInAttr attr, final PairBtDeviceListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mPairBtDevice == null) return false;
        mPairBtDevice.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnAddressInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnAddressInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postRestartWithCompletion(final RestartListener listener) throws OcException {

       if(mRestart == null) return false;
       mRestart.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostRestartCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostRestartCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postSetZbFriendlyName(SetZbFriendlyNameInAttr attr, final SetZbFriendlyNameListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mSetZbFriendlyName == null) return false;
        mSetZbFriendlyName.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnSetZbFriendlyNameInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnSetZbFriendlyNameInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postStartAVSOnboardingWithCompletion(final StartAVSOnboardingListener listener) throws OcException {

       if(mStartAVSOnboarding == null) return false;
       mStartAVSOnboarding.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostStartAVSOnboardingCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostStartAVSOnboardingCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postStartBtScanWithCompletion(final StartBtScanListener listener) throws OcException {

       if(mStartBtScan == null) return false;
       mStartBtScan.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostStartBtScanCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostStartBtScanCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean postStartZbJoining(TimeoutSecInAttr attr, final StartZbJoiningListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mStartZbJoining == null) return false;
        mStartZbJoining.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnTimeoutSecInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnTimeoutSecInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postStopBtScanWithCompletion(final StopBtScanListener listener) throws OcException {

       if(mStopBtScan == null) return false;
       mStopBtScan.post(new OcRepresentation(), new HashMap<>(), new OnPostListener() {
              @Override
              public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {
                   mExecutor.execute(() ->
                    listener.OnPostStopBtScanCompleted(true)
                  );

              }

              @Override
              public void onPostFailed(Throwable ex) {
                ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnPostStopBtScanCompleted(false)
                );
              }
        } , DEFAULT_QoS);
        return true;
    }

    public boolean getSystemWithCompletion(final SystemListener listener) throws OcException {
        if(mSystem == null) return false;
        mSystem.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          SystemAttr outAttribute = new SystemAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetSystemCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetSystemCompleted(null,false);
                      }
                      mSystem.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mSystem.setResourceAvailable(false);

                    listener.OnGetSystemCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postSystem(SystemAttr attr, final SystemListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mSystem == null) return false;
        mSystem.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnSystemCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnSystemCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean postUnpairBtDevice(AddressInAttr attr, final UnpairBtDeviceListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mUnpairBtDevice == null) return false;
        mUnpairBtDevice.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnAddressInCompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnAddressInCompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getVoiceUIWithCompletion(final VoiceUIListener listener) throws OcException {
        if(mVoiceUI == null) return false;
        mVoiceUI.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          VoiceUIAttr outAttribute = new VoiceUIAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetVoiceUICompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetVoiceUICompleted(null,false);
                      }
                      mVoiceUI.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mVoiceUI.setResourceAvailable(false);

                    listener.OnGetVoiceUICompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

    public boolean postVoiceUI(VoiceUIAttr attr, final VoiceUIListener listener) throws OcException {
        OcRepresentation rep = attr.pack();
        if(mVoiceUI == null) return false;
        mVoiceUI.post(rep, new HashMap<>(), new OnPostListener() {
               @Override
               public void onPostCompleted(List<OcHeaderOption> headerOptionList,
                   OcRepresentation ocRepresentation) {
                   mExecutor.execute(() -> {
                       if(listener != null) {
                         listener.OnVoiceUICompleted(true);
                       }
                   });
               }

               @Override
               public void onPostFailed(Throwable ex) {
                   ex.printStackTrace();
                   mExecutor.execute(() ->
                    listener.OnVoiceUICompleted(false)
                   );
               }

             }, DEFAULT_QoS);
        return true;
    }

    public boolean getZigbeeWithCompletion(final ZigbeeListener listener) throws OcException {
        if(mZigbee == null) return false;
        mZigbee.get(new HashMap<>(), new OnGetListener() {
              @Override
              public void onGetCompleted(List<OcHeaderOption> headerOptionList,
                  OcRepresentation ocRepresentation) {

                   mExecutor.execute(() -> {
                      boolean isAvailable;
                      try{
                          ZigbeeAttr outAttribute = new ZigbeeAttr();
                          outAttribute.unpack(ocRepresentation);
                          listener.OnGetZigbeeCompleted(outAttribute,true);
                          isAvailable = true;
                      } catch(OcException | NullPointerException e ) {
                          e.printStackTrace();
                          isAvailable = false;
                          listener.OnGetZigbeeCompleted(null,false);
                      }
                      mZigbee.setResourceAvailable(isAvailable);
                  });
              }

              @Override
              public void onGetFailed(Throwable ex) {
                  ex.printStackTrace();
                   mExecutor.execute(() -> {
                   mZigbee.setResourceAvailable(false);

                    listener.OnGetZigbeeCompleted(null,false);
                  });
              }
        } , DEFAULT_QoS);
        return true;

    }

}