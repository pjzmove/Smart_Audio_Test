/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTDevice;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTRepository;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.adapter.NetworkTreeAdapter.RootViewHolder;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.ExpandCollapseButton;
import com.qualcomm.qti.smartaudio.view.NetworkTreeChildView;
import com.qualcomm.qti.smartaudio.view.PlayPauseButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>This class binds the data of a list of {@link IoTGroup IoTGroup} with a
 * {@link RecyclerView RecyclerView} in order to display the group and its child elements as a
 * tree.</p>
 */
public class NetworkTreeAdapter extends RecyclerView.Adapter<RootViewHolder> {

    // ========================================================================
    // FIELDS

    /**
     * The list of the groups to display in the tree
     */
    private final List<IoTGroup> mGroupsList;
    /**
     * The listener to dispatch actions and get information from for the adapter
     */
    private final NetworkTreeAdapterListener mListener;


    // ========================================================================
    // CONSTRUCTOR

    /**
     * Default constructor to build a new instance of this adapter.
     */
    public NetworkTreeAdapter(NetworkTreeAdapterListener listener) {
        mListener = listener;
        mGroupsList = new ArrayList<>();
    }


    // ========================================================================
    // OVERRIDE METHODS

    @NonNull
    @Override // extends RecyclerView.Adapter<RootViewHolder>
    public RootViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflating the view
        View v = LayoutInflater
                .from(parent.getContext()).inflate(R.layout.network_tree_root, parent, false);
        // building the view holder
        return new RootViewHolder(v);
    }

    @Override // extends RecyclerView.Adapter<RootViewHolder>
    public void onBindViewHolder(@NonNull RootViewHolder holder, int position) {
        // get the group
        IoTGroup group = mGroupsList.get(position);

        // displaying what is played now & group information
        setGroupNowPlaying(group, holder.itemView);

        boolean isSinglePlayer = group.isSinglePlayer();

        // set the root icon
        int icon = isSinglePlayer ? R.drawable.ic_speaker_23dp : R.drawable.ic_group_list_23dp;
        holder.mmIconView.setImageResource(icon);

        // display the options button
        holder.mmOptionsButton.setVisibility(isSinglePlayer ? View.VISIBLE : View.INVISIBLE);

        // add listener to the view
        holder.itemView.setOnClickListener(view -> mListener.onIoTElementSelected(group));

        holder.mmOptionsButton.setOnClickListener(view -> mListener.onIoTElementMenuSelected(group));

        // adding children
        List<IoTType> childrenTypes = addChildren(isSinglePlayer ? group.getLeadPlayer() : group,
                holder.mmChildrenContainer);

        // display indicators
        boolean hasBTChildren = childrenTypes != null && childrenTypes.contains(IoTType.BLUETOOTH_DEVICE);
        boolean hasZbChildren = childrenTypes != null && childrenTypes.contains(IoTType.ZIGBEE_DEVICE);
        holder.mmBtIndicator.setVisibility(hasBTChildren ? View.VISIBLE : View.GONE);
        holder.mmZbIndicator.setVisibility(hasZbChildren ? View.VISIBLE : View.GONE);
    }

    @Override // extends RecyclerView.Adapter<RootViewHolder>
    public int getItemCount() {
        return mGroupsList.size();
    }

    // ========================================================================
    // PUBLIC METHODS

    /**
     * <p>To set the list of groups with the given one.</p>
     * <p>This method discards the previous list.</p>
     * <p>This method updates the UI.</p>
     *
     * @param groups The lists of groups to display.
     */
    public void setList(List<? extends IoTRepository> groups) {
        mGroupsList.clear();
        if (groups != null) {
            for (IoTRepository element : groups) {
                // only groups are displayed as roots of the network tree
                if (element instanceof IoTGroup) {
                    mGroupsList.add((IoTGroup) element);
                }
            }
        }
        notifyDataSetChanged();
    }


    // ========================================================================
    // PRIVATE METHODS

    /**
     * <p>This method adds all the views of the children to the container of the parent element.</p>
     * <p>This method builds the list of all element types which have been added as children.</p>
     *
     * @param parent    The parent element to add the children.
     * @param container The main vew container to add the children views to.
     * @return the list of all element types which have been added as children. The list can have
     * duplicates.
     */
    private List<IoTType> addChildren(IoTRepository parent, LinearLayout container) {
        // get list of child devices
        List<IoTRepository> subDeviceList = buildChildrenList(parent);

        if (subDeviceList == null || subDeviceList.isEmpty()) {
            // the device has no child
            return null;
        }

        // init container
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);

        // integers to check if the child is the last one of the list
        int size = subDeviceList.size();
        int index = 0;

        // prepare list of subtypes
        List<IoTType> subTypes = new ArrayList<>();

        for (IoTRepository subDevice : subDeviceList) {
            if (subDevice != null) {
                boolean isLast = size <= ++index;
                // set a view for the sub device
                addChildView(container, subDevice, isLast);
                // keep the type of the device
                IoTType type = subDevice.getType();
                subTypes.add(type); // types can be duplicated
            }
        }

        return subTypes;
    }

    /**
     * <p>To build the list of children of an element.</p>
     * <p>An IoT element can be virtual and have physical elements as well. This method also looks for
     * physical elements which are children of the given parent.</p>
     *
     * @param parent The parent to get the list of children elements for.
     * @return The list of children.
     */
    private List<IoTRepository> buildChildrenList(IoTRepository parent) {
        if (parent == null) {
            // no element
            return null;
        }

        // get list of child devices
        List<IoTRepository> childrenList = new ArrayList<>();

        List<? extends IoTRepository> elementSubList = parent.getList();
        if (elementSubList != null && !elementSubList.isEmpty()) {
            childrenList.addAll(elementSubList);
        }

        // if the iot element is a player, gets its corresponding IoTDevice to also get the children devices
        if (parent instanceof IoTPlayer) {
            IoTDevice device = mListener.getIoTDeviceFromPlayer((IoTPlayer) parent);
            if (device != null) {
                childrenList.addAll(device.getList());
            }
        }

        // sort the list
        Collections.sort(childrenList);

        return childrenList;
    }

    /**
     * <p>This method builds a child view for the given IoT element and adds it to the given container.</p>
     *
     * @param container The layout which will contain the child view.
     * @param element   The element to add a view for.
     * @param isLast    True if the element is the last one of the children.
     */
    private void addChildView(LinearLayout container, final IoTRepository element, boolean isLast) {
        // create the view and bind components
        NetworkTreeChildView childView = new NetworkTreeChildView(mListener.getContext());

        // set up the view
        childView.setTitle(element.getName());
        childView.setIsLastChild(isLast);
        childView.setIcon(Utils.getListIconFromIoTRepository(element));

        // set up the view listener
        childView.setOnClickListener(view -> mListener.onIoTElementSelected(element));

        // add the view to the container
        container.addView(childView);

        // set the children of the sub device
        List<IoTType> subTypes = addChildren(element, childView.getChildrenContainer());
        childView.setSubTypes(subTypes);
    }


    /**
     * <p>To set up the now playing view for the group.</p>
     *
     * @param group  The group to set up the now playing view for.
     * @param parent The parent container.
     */
    private void setGroupNowPlaying(IoTGroup group, View parent) {
        // displaying what is played now & group information
        Utils.setNowPlayingSpeakerListItem(mListener.getContext(), parent, group);
        // updating up the pay pause button
        PlayPauseButton playPauseButton = parent.findViewById(R.id.speaker_album_art_play_pause_button);
        playPauseButton.setBaseActivity(mListener.getBaseActivity());
        playPauseButton.setGroup(group);
    }


    // ========================================================================
    // INNER CLASS

    /**
     * <p>A holder to keep the references of the views for the root items.</p>
     */
    class RootViewHolder extends RecyclerView.ViewHolder {

        /**
         * The icon to display the type of the view.
         */
        private ImageView mmIconView;
        /**
         * The options button to access more options for the root item.
         */
        private ExpandCollapseButton mmOptionsButton;
        /**
         * The layout which contains the children views of the root.
         */
        private LinearLayout mmChildrenContainer;
        /**
         * The Bluetooth icon indicator.
         */
        private View mmBtIndicator;
        /**
         * The ZigBee icon indicator.
         */
        private View mmZbIndicator;

        /**
         * Default constructor for the View Holder. This binds all the view components.
         *
         * @param v The view hosted in this holder
         */
        RootViewHolder(View v) {
            super(v);
            mmIconView = v.findViewById(R.id.node_icon);
            mmOptionsButton = v.findViewById(R.id.node_options_button);
            mmChildrenContainer = v.findViewById(R.id.sub_items_container);
            mmBtIndicator = v.findViewById(R.id.node_bluetooth_indicator);
            mmZbIndicator = v.findViewById(R.id.node_zigbee_indicator);
        }
    }


    // ========================================================================
    // INTERFACE

    /**
     * This interface allows the adapter to communicate with the element which controls the
     * RecyclerView. Such as a fragment or an activity.
     */
    public interface NetworkTreeAdapterListener {

        /**
         * <p>To retrieve the IoTDevice linked to an IoTPlayer.</p>
         *
         * @param player The player to get the corresponding IoTDevice from.
         * @return The corresponding IoTDevice if it exists, null otherwise.
         */
        IoTDevice getIoTDeviceFromPlayer(IoTPlayer player);

        /**
         * <p>To dispatch the event when the user taps on the UI representation of an IoT element.</p>
         *
         * @param element The element selected by the user.
         */
        void onIoTElementSelected(IoTRepository element);

        /**
         * <p>To dispatch the event when the user taps on the UI representation of the menu of an IoT element.</p>
         *
         * @param element The element selected by the user.
         */
        void onIoTElementMenuSelected(IoTRepository element);

        /**
         * <p>To get the context of the application.</p>
         *
         * @return The context of the application.
         */
        Context getContext();

        /**
         * <p>To get the base activity for the play pause button.</p>
         *
         * @return the base activity this adapter is linked to.
         */
        BaseActivity getBaseActivity();
    }

}
