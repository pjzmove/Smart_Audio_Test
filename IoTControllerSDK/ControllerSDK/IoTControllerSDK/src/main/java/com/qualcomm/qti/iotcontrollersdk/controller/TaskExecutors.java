/**************************************************************************************************
 * Copyright 2018-2019 Qualcomm Technologies International, Ltd.                                  *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.controller;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TaskExecutors {

  private final static String TAG = "TaskExecutors";
  private final static String THREAD_POOL_NAME = "QSAP";
  private final static int NUMBER_OF_THREADS = 5;

  private static TaskExecutors mInstance = new TaskExecutors();
  private ScheduledExecutorService mExecutorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS,new AppThreadFactory());
  private ExecutorService mDeviceDiscoveryExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("DD"));
  private ExecutorService mResourceDiscoveryExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("RD"));
  private ScheduledExecutorService mRequestExecutor = Executors.newSingleThreadScheduledExecutor(new ResourceRequestThreadFactory("Request"));
  private ScheduledExecutorService mGroupExecutor = Executors.newSingleThreadScheduledExecutor(new ResourceRequestThreadFactory("Group"));
  private ExecutorService mIoTSysResourceDiscoveryExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("IoTsysRD"));
  private ExecutorService mAllPlayNotificationExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("Allplay_Notif"));
  private ExecutorService mIoTSysNotificationExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("IoTSys_Notif"));

  private Handler mHandler;

  private static class AppThreadFactory implements ThreadFactory {

    private static int count = 1;

    @Override
    public Thread newThread(@NonNull Runnable r) {
      Thread thread = new Thread(r);
      thread.setName(String.format(THREAD_POOL_NAME + "-%d",count++));
      thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
      return thread;
    }
  }

  private static class ResourceRequestThreadFactory implements ThreadFactory {

    private String mName;

    public ResourceRequestThreadFactory(String name) {
      mName = name;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
      Thread thread = new Thread(r);
      thread.setName(mName);
      thread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
      return thread;
    }
  }

  private TaskExecutors() {
    mHandler = new Handler(Looper.getMainLooper());
  }

  public static TaskExecutors getExecutor() {
    return mInstance;
  }

  public synchronized void executeOnSearchExecutor (@NonNull Runnable runnable) {
      if(mDeviceDiscoveryExecutor.isShutdown() || mDeviceDiscoveryExecutor.isTerminated()) {
          mDeviceDiscoveryExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("DD"));
      }
      try {
        mDeviceDiscoveryExecutor.execute(runnable);
      } catch (RejectedExecutionException e) {
        if(!mDeviceDiscoveryExecutor.isShutdown()) {
          Log.d(TAG,"Task submission rejected");
        }
      }
  }

  public synchronized void executeOnResourceExecutor(@NonNull Runnable runnable) {
    if(mResourceDiscoveryExecutor.isShutdown() || mResourceDiscoveryExecutor.isTerminated()) {
        mResourceDiscoveryExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("RD"));
    }
    try {
      mResourceDiscoveryExecutor.execute(runnable);
    } catch (RejectedExecutionException e) {
      if(!mResourceDiscoveryExecutor.isShutdown()) {
          Log.d(TAG,"Task submission rejected");
        }
    }
  }

  public synchronized void executeOnIoTSysResourceExecutor(@NonNull Runnable runnable) {
    if(mIoTSysResourceDiscoveryExecutor.isShutdown() || mIoTSysResourceDiscoveryExecutor.isTerminated()) {
        mIoTSysResourceDiscoveryExecutor = Executors.newSingleThreadExecutor(new ResourceRequestThreadFactory("IoTsysRD"));
    }
    try {
      mIoTSysResourceDiscoveryExecutor.execute(runnable);
    } catch (RejectedExecutionException e) {
      if(!mIoTSysResourceDiscoveryExecutor.isShutdown()) {
        Log.d(TAG,"Task submission rejected");
      }
    }
  }

  public synchronized void executeOnRequestExecutor(@NonNull Runnable runnable) {
    if(mRequestExecutor.isShutdown() || mRequestExecutor.isTerminated()) {
        mRequestExecutor = Executors.newSingleThreadScheduledExecutor(new ResourceRequestThreadFactory("Request"));
    }
    try {
      mRequestExecutor.execute(runnable);
    } catch (RejectedExecutionException e) {
      if(!mRequestExecutor.isShutdown()) {
        Log.d(TAG,"Task submission rejected");
      }
    }
  }

  public synchronized void executeGroupExecutor(@NonNull Runnable runnable) {
    if(mGroupExecutor.isShutdown() || mGroupExecutor.isTerminated()) {
        mGroupExecutor = Executors.newSingleThreadScheduledExecutor(new ResourceRequestThreadFactory("Request"));
    }
    try {
      mGroupExecutor.execute(runnable);
    } catch(RejectedExecutionException e) {
      if(!mGroupExecutor.isShutdown()) {
        Log.d(TAG,"Task submission rejected");
      }
    }
  }

  public synchronized void scheduleOnGroupExecutor(@NonNull Runnable command, long delay) {
    if(!mGroupExecutor.isShutdown() && !mGroupExecutor.isTerminated()) {
      try {
        mGroupExecutor.schedule(command, delay, TimeUnit.MILLISECONDS);
      } catch (RejectedExecutionException e) {
        if(!mGroupExecutor.isShutdown())
          Log.d(TAG,"Task submission rejected");
      }
    }
  }

  public synchronized void execute(@NonNull Runnable command) {
    if(mExecutorService.isShutdown() || mExecutorService.isTerminated()) {
      Log.e(TAG,"Current executor service is shut down, restart it");
      mExecutorService = Executors.newScheduledThreadPool(NUMBER_OF_THREADS,new AppThreadFactory());
      mHandler.removeCallbacksAndMessages(null);
    }

    try {
      mExecutorService.execute(command);
    } catch (RejectedExecutionException e) {
        if(!mExecutorService.isShutdown())
          Log.d(TAG,"Task submission rejected");
    }
  }

  public synchronized void execute(@NonNull Runnable command, long delay) {
    if(!mExecutorService.isShutdown() && !mExecutorService.isTerminated()) {
      try {
        mExecutorService.schedule(command, delay, TimeUnit.MILLISECONDS);
      } catch (RejectedExecutionException e) {
        if(!mExecutorService.isShutdown())
          Log.d(TAG,"Task submission rejected");
      }
    }
  }

  public synchronized void executeAllplayNotification(@NonNull Runnable command) {
    if(mAllPlayNotificationExecutor.isShutdown() || mAllPlayNotificationExecutor.isTerminated()) {
        mAllPlayNotificationExecutor = Executors.newSingleThreadScheduledExecutor(new ResourceRequestThreadFactory("Allplay_Notif"));
    }
    try {
      mAllPlayNotificationExecutor.execute(command);
    } catch(RejectedExecutionException e) {
      if(!mAllPlayNotificationExecutor.isShutdown())
        Log.d(TAG,"Task submission rejected");
      e.printStackTrace();
    }
  }

  public synchronized void executeIoTSysNotification(@NonNull Runnable command) {
    if(mIoTSysNotificationExecutor.isShutdown() || mIoTSysNotificationExecutor.isTerminated()) {
        mIoTSysNotificationExecutor = Executors.newSingleThreadScheduledExecutor(new ResourceRequestThreadFactory("IoTSys_Notif"));
    }
    try {
      mIoTSysNotificationExecutor.execute(command);
    } catch (RejectedExecutionException e) {
      if(!mIoTSysNotificationExecutor.isShutdown())
        Log.d(TAG,"Task submission rejected");
      e.printStackTrace();
    }
  }



  public synchronized void executeDelayForRequest(@NonNull Runnable command, long delay) {
    if(!mRequestExecutor.isShutdown() && !mRequestExecutor.isTerminated()) {
      try {
        mRequestExecutor.schedule(command, delay, TimeUnit.MILLISECONDS);
      } catch (RejectedExecutionException e) {
        Log.d(TAG,"Task submission rejected");
      }
    }
  }

  public <T> Future<T> submit(Callable<T> task) {
    return mExecutorService.submit(task);
  }

  public void shutdown() {
    mExecutorService.shutdown();
    mResourceDiscoveryExecutor.shutdown();
    mDeviceDiscoveryExecutor.shutdown();
    try {
       if (!mExecutorService.awaitTermination(60, TimeUnit.SECONDS)) {
         mExecutorService.shutdownNow();
         if (!mExecutorService.awaitTermination(60, TimeUnit.SECONDS))
             Log.e(TAG,"Executor pool did not terminate");
       }
     } catch (InterruptedException ie) {
       mExecutorService.shutdownNow();
     }
  }

  public void executeOnMain(@NonNull Runnable r) {
    mHandler.post(r);
  }

}
