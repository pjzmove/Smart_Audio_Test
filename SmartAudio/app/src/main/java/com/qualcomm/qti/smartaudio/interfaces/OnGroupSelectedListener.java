package com.qualcomm.qti.smartaudio.interfaces;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;

public interface OnGroupSelectedListener {
    void onZoneSelected(final IoTRepository object);
    void onZoneMenuSelected(final IoTRepository object);

    default void onDeviceSetting(String id, String host) {
    }
}
