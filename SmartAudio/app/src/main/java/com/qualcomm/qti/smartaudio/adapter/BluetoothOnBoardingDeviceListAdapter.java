/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.holder.SetupListItemViewHolder;
import com.qualcomm.qti.smartaudio.holder.SetupListItemViewHolder.SetupListItemViewHolderListener;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTBluetoothDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class binds the data of a list of {@link IoTBluetoothDevice IoTBluetoothDevice} with a
 * {@link RecyclerView RecyclerView}.</p>
 */
public class BluetoothOnBoardingDeviceListAdapter extends RecyclerView.Adapter<SetupListItemViewHolder>
        implements SetupListItemViewHolderListener {

    /**
     * The data managed by this adapter.
     */
    private final List<IoTBluetoothDevice> mDevices = new ArrayList<>();
    /**
     * The listener for all user interactions.
     */
    private final BluetoothOnBoardingDeviceListAdapterListener mListener;

    /**
     * Default constructor to build a new instance of this adapter.
     */
    public BluetoothOnBoardingDeviceListAdapter(BluetoothOnBoardingDeviceListAdapterListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override // RecyclerView.Adapter<DeviceViewHolder>
    public SetupListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_setup_list, parent, false);
        return new SetupListItemViewHolder(view, this);
    }

    @Override // RecyclerView.Adapter<DeviceViewHolder>
    public void onBindViewHolder(@NonNull SetupListItemViewHolder holder, int position) {
        // we define the content of this view depending on the data set of this adapter.
        IoTBluetoothDevice device = mDevices.get(position);
        String deviceName = device.getName();
        deviceName = deviceName == null || deviceName.length() < 1 ? device.getAddress() : deviceName;
        deviceName = deviceName == null ? "" : deviceName;

        // fill data
        holder.refreshValues(R.drawable.ic_bluetooth_disconnected_19dp, deviceName,
                             R.string.cont_desc_bluetooth_add_scanned_device);
    }

    @Override // RecyclerView.Adapter<DeviceViewHolder>
    public int getItemCount() {
        return mDevices.size();
    }

    @Override // DeviceViewHolder.SetupListItemViewHolderListener
    public void onClickItem(int position) {
        mListener.onItemSelected(mDevices.get(position));
    }

    /**
     * <p>To update the list with a new device.</p>
     * <p>If the device already exists in the list, this method discards the device added as a
     * parameter.</p>
     *
     * @param device
     *         The device to add or to update.
     */
    public void add(IoTBluetoothDevice device) {
        synchronized (mDevices) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                notifyItemInserted(mDevices.size() - 1);
            }
        }
    }

    /**
     * To completely reset the data set list by clearing it.
     */
    public void reset() {
        mDevices.clear();
        notifyDataSetChanged();
    }

    /**
     * This interface allows the adapter to communicate with the element which controls the
     * RecyclerView. Such as a fragment or an activity.
     */
    public interface BluetoothOnBoardingDeviceListAdapterListener {

        /**
         * This method is called by the adapter when the user selects or deselects an item of the list.
         *
         * @param device
         *         the device which has been selected.
         */
        void onItemSelected(IoTBluetoothDevice device);
    }
}