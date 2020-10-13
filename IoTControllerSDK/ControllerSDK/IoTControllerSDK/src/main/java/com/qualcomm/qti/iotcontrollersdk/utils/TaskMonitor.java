/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.iotcontrollersdk.utils;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskMonitor {

    private AtomicInteger mTotalTask = new AtomicInteger(0);
    private AtomicInteger mCounter = new AtomicInteger(0);
    private AtomicBoolean mSuccess = new AtomicBoolean(false);

    public TaskMonitor(int taskNum) {
      mTotalTask.set(taskNum);
      mCounter.set(0);
      mSuccess.set(true);
    }

    public void decrementTask() {
      int num = mTotalTask.decrementAndGet();
      if(num < 0)  mTotalTask.set(0);
    }


    public synchronized void increment(boolean status) {
      mCounter.incrementAndGet();
      if(!status)
        mSuccess.set(false);
    }

    public boolean getResult() {
      return mSuccess.get();
    }

    public synchronized boolean isDone() {
      return mCounter.get() >= mTotalTask.get();
    }

}
