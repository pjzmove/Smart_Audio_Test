/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import java.util.concurrent.Executor;

public class UiThreadExecutor implements Executor {

  private Handler mHandler;
  private static UiThreadExecutor mInstance = new UiThreadExecutor();

  private UiThreadExecutor() {
    mHandler = new Handler(Looper.getMainLooper());
  }

  public static UiThreadExecutor getInstance() {
    return mInstance;
  }

  @Override
  public void execute(@NonNull Runnable runnable) {
    mHandler.post(runnable);
  }

  public void executeAtDelayTime(@NonNull Runnable command, long delayed) {
    mHandler.postDelayed(command,delayed);
  }

}
