/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.adapter;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.model.SettingsGroup;
import com.qualcomm.qti.smartaudio.model.SettingsItem;
import com.qualcomm.qti.smartaudio.view.SettingsItemView;

import java.util.ArrayList;
import java.util.List;

import static com.qualcomm.qti.smartaudio.model.SettingsGroup.GroupStates;
import static com.qualcomm.qti.smartaudio.model.SettingsItem.ItemStates;
import static com.qualcomm.qti.smartaudio.view.SettingsItemView.Events;
import static com.qualcomm.qti.smartaudio.view.SettingsItemView.Types;

/**
 * <p>This adapter manages a list of settings to display within an {@link ExpandableListView
 * ExpandableListView} and their display within the UI.</p>
 */
public class SettingsAdapter extends BaseExpandableListAdapter {

    // ========================================================================
    // PRIVATE FIELDS

    /**
     * The list of data managed by this adapter.
     */
    private final List<SettingsGroup> mmGroupsData = new ArrayList<>();
    /**
     * The listener registered to get user events from the adapter.
     */
    private SettingsAdapterListener mmListener;
    /**
     * The listener for the {@link SettingsItemView SettingsItemView} elements created by this
     * adapter.
     */
    private SettingsItemView.SettingsItemViewListener mSettingsItemViewListener =
            (event, groupId, childId, data) -> mmListener
                    .onSettingsItemEvent(event, groupId, childId, data);


    // ========================================================================
    // CONSTRUCTORS

    /**
     * <p>To build a new instance of this adapter.</p>
     *
     * @param listener
     *         To get events from specific components.
     */
    public SettingsAdapter(SettingsAdapterListener listener) {
        mmListener = listener;
    }


    // ========================================================================
    // OVERRIDE METHODS

    @Override // BaseExpandableListAdapter
    public int getGroupCount() {
        return mmGroupsData.size();
    }

    @Override // BaseExpandableListAdapter
    public int getChildrenCount(int groupPosition) {
        return getItemsCount(groupPosition);
    }

    @Override // BaseExpandableListAdapter
    public SettingsGroup getGroup(int groupPosition) {
        return mmGroupsData.get(groupPosition);
    }

    @Override // BaseExpandableListAdapter
    public SettingsItem getChild(int groupPosition, int childPosition) {
        return getItem(groupPosition, childPosition);
    }

    @Override // BaseExpandableListAdapter
    public long getGroupId(int groupPosition) {
        SettingsGroup group = getGroup(groupPosition);
        return group == null ? -1 : group.getId();
    }

    @Override // BaseExpandableListAdapter
    public long getChildId(int groupPosition, int childPosition) {
        return getItemId(groupPosition, childPosition);
    }

    @Override // BaseExpandableListAdapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // BaseExpandableListAdapter
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        return getGroupView(groupPosition, convertView, parent);
    }

    @Override // BaseExpandableListAdapter
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView,
                             ViewGroup parent) {
        return getItemView(groupPosition, childPosition, convertView, parent);
    }

    @Override // BaseExpandableListAdapter
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // none of the items can be selected
        return false;
    }


    // ========================================================================
    // PUBLIC METHODS

    /**
     * <p>To get the id of the item using the group and the item positions in the list of groups and
     * items.</p>
     *
     * @param groupPosition
     *         The position of the group the item belongs to.
     * @param childPosition
     *         The position of the child which corresponds to the item.
     *
     * @return the id of the item or <code>-1</code> if the item couldn't be found.
     */
    public long getItemId(int groupPosition, int childPosition) {
        SettingsItem data = getChild(groupPosition, childPosition);
        return data == null ? -1 : data.getId();
    }

    /**
     * <p>To get the item using its group and item positions in the list of groups and items.</p>
     *
     * @param groupPosition
     *         The position of the group the item belongs to.
     * @param childPosition
     *         The position of the child which corresponds to the item.
     *
     * @return The item or null if it couldn't be found.
     */
    public SettingsItem getItem(int groupPosition, int childPosition) {
        SettingsGroup group = getGroup(groupPosition);
        return group == null ? null : group.getItem(childPosition);
    }

    /**
     * <p>To get the item using its group and item id.</p>
     *
     * @param groupId
     *         The id of the group the item belongs to.
     * @param itemId
     *         The id of the item to get.
     *
     * @return The item or null if it couldn't be found.
     */
    public SettingsItem getItemFromId(int groupId, int itemId) {
        SettingsGroup group = getGroupFromId(groupId);
        return group == null ? null : group.getItemFromId(itemId);
    }

    /**
     * <p>This method allows to set the display state in order to hide, disable, etc. the item.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group the item belongs to.
     * @param itemId
     *         The id of the item to change the state of.
     * @param state
     *         The state to set up, one of {@link ItemStates ItemStates}.
     * @param value
     *         The value the state should be.
     */
    public void setItemState(int groupId, int itemId, @ItemStates int state, boolean value) {
        SettingsItem data = getItemFromId(groupId, itemId);
        if (data == null) {
            return;
        }
        data.setState(state, value);
    }

    /**
     * <p>This method allows to set up the given state of the given group, see {@link GroupStates
     * GroupStates}.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group to set up the state for.
     * @param state
     *         The state to set up, one of {@link GroupStates GroupStates}.
     * @param value
     *         <code>True</code> to enable the state, <code>False</code> to disable it.
     */
    public void setGroupState(int groupId, @GroupStates int state, boolean value) {
        SettingsGroup group = getGroupFromId(groupId);
        if (group == null) {
            return;
        }
        group.setState(state, value);
        notifyDataSetChanged();
    }

    /**
     * <p>To add a group to the data structure.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group.
     */
    public void addGroup(int groupId) {
        SettingsGroup group = new SettingsGroup(groupId);
        addGroup(group);
    }

    /**
     * <p>To add a group to the data structure with a title.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param title
     *         The title of the group to display.
     * @param groupId
     *         The id of the group.
     */
    public void addGroup(int groupId, String title) {
        SettingsGroup group = new SettingsGroup(groupId, title);
        addGroup(group);
    }

    /**
     * <p>To add a settings item to the structure.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id of the item.
     * @param title
     *         The title of the item to display.
     * @param description
     *         The description of the item to display, can be null or empty.
     * @param type
     *         The type of view to display for the item, one of {@link Types Types}.
     * @param contentDescription
     *         The content description of the item.
     */
    public void addItem(int groupId, int itemId, String title, String description, @Types int type,
                        String contentDescription) {
        SettingsItem data = new SettingsItem(itemId, title, description, type, contentDescription);
        addItem(groupId, data);
    }

    /**
     * <p>To add a settings item to the structure.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id of the item.
     * @param title
     *         The title of the item to display.
     * @param description
     *         The description of the item to display, can be null or empty.
     * @param type
     *         The type of view to display for the item, one of {@link Types Types}.
     * @param value
     *         The value to display for the settings, see the {@link Types Types} to see the
     *         expected values.
     * @param contentDescription
     *         The content description of the item.
     */
    public void addItem(int groupId, int itemId, String title, String description, @Types int type,
                        Object value, String contentDescription) {
        SettingsItem data = new SettingsItem(itemId, title, description, type, value, contentDescription);
        addItem(groupId, data);
    }

    /**
     * <p>To update the value of the settings.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id if the item to update.
     * @param value
     *         The value of the item. See {@link SettingsItem#setValue(Object) setValue(Object)}
     *         for more information.
     */
    public void setItemValue(int groupId, int itemId, Object value) {
        SettingsItem item = getItemFromId(groupId, itemId);
        if (item != null) {
            item.setValue(value);
        }
    }

    /**
     * <p>To update the description of an item.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id if the item to update.
     * @param description
     *         The new description of an item.
     */
    public void setItemDescription(int groupId, int itemId, String description) {
        SettingsItem item = getItemFromId(groupId, itemId);
        if (item != null) {
            item.setDescription(description);
        }
    }

    /**
     * <p>To delete all settings.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     */
    public void removeAll() {
        mmGroupsData.clear();
    }

    /**
     * <p>To remove a group of settings.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group to remove.
     */
    public void removeGroup(int groupId) {
        SettingsGroup group = getGroupFromId(groupId);
        if (group != null) {
            mmGroupsData.remove(group);
        }
    }

    /**
     * <p>To remove all items from a group.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group to delete the items from.
     */
    public void removeItemsFromGroup(int groupId) {
        SettingsGroup group = getGroupFromId(groupId);
        if (group != null) {
            group.removeAllItems();
        }
    }

    /**
     * <p>To remove an item from the adapter.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group the item is attached to.
     * @param itemId
     *         The id of the item to delete.
     */
    public void removeItem(int groupId, int itemId) {
        SettingsGroup group = getGroupFromId(groupId);
        if (group != null) {
            group.removeItem(itemId);
        }
    }


    // ========================================================================
    // PRIVATE METHODS

    /**
     * <p>To get the group data from its id.</p>
     *
     * @param groupId
     *         The id to get the group for.
     *
     * @return The group or null if it couldn't be found.
     */
    private SettingsGroup getGroupFromId(int groupId) {
        for (SettingsGroup group : mmGroupsData) {
            if (group.getId() == groupId) {
                return group;
            }
        }
        return null;
    }

    /**
     * <p>To get the number of items for a group based on its position.</p>
     *
     * @param groupPosition
     *         The position of the group.
     *
     * @return the number of items in the group or <code>0</code> if it couldn't be found.
     */
    private int getItemsCount(int groupPosition) {
        SettingsGroup group = getGroup(groupPosition);
        return group == null ? 0 : group.getItemsCount();
    }

    /**
     * <p>To create or update the view for the corresponding group depending on its position.</p>
     * <p>If the group is hidden or cannot be found this method returns an empty {@link View
     * View}.</p>
     * <p>This method also sets the view with the data of the group.</p>
     *
     * @param groupPosition
     *         The group to get the view for.
     * @param convertView
     *         The view which currently corresponds to the given group.
     * @param parent
     *         The parent view the view will be attached to.
     *
     * @return The view which corresponds to the group.
     */
    private View getGroupView(int groupPosition, View convertView, ViewGroup parent) {
        // getting data
        SettingsGroup group = getGroup(groupPosition);

        if (group == null || !group.hasTitle() || !group.getState(GroupStates.DISPLAYED)) {
            // nothing to display
            convertView = new View(parent.getContext());
        }
        else {
            // build view
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.list_item_settings_headers, parent, false);

            // set up view
            TextView textView = convertView.findViewById(R.id.settings_header_text_view);
            textView.setText(group.getTitle());
        }

        ExpandableListView listView = (ExpandableListView) parent;
        listView.expandGroup(groupPosition);

        return convertView;
    }

    /**
     * <p>To create or update the view for the corresponding item depending on its positions.</p>
     * <p>If the item is hidden or cannot be found this method returns an empty {@link View
     * View}.</p>
     * <p>This method also sets the view with the data of the item.</p>
     *
     * @param groupPosition
     *         The group the item (child) belongs to.
     * @param itemPosition
     *         The position of the item to get the view for.
     * @param convertView
     *         The view which currently corresponds to the given group. param parent The
     *         parent view the view will be attached to.
     *
     * @return The view which corresponds to the item.
     */
    private View getItemView(int groupPosition, int itemPosition, View convertView,
                             ViewGroup parent) {
        // getting data
        SettingsGroup group = getGroup(groupPosition);
        SettingsItem item = group == null ? null : group.getItem(itemPosition);

        if (group == null || !group.getState(GroupStates.DISPLAYED)
                || item == null || !item.getState(ItemStates.DISPLAYED)) {
            // nothing to display
            return new View(parent.getContext());
        }

        // getting the view component
        SettingsItemView settingsView = getSettingsItemView(convertView, parent);

        // setting up the view with the data
        setUpSettingsItemView(settingsView, group, item);

        // set specific behaviours
        mmListener.initItemViewDisplay(settingsView, group, item);

        // return the set up view
        return settingsView;
    }

    /**
     * <p>This method sets up the given view with the given data.</p>
     *
     * @param settingsView
     *         The view to set up.
     * @param group
     *         The group the view is related to.
     * @param item
     *         The item data to set up the view with.
     */
    private void setUpSettingsItemView(SettingsItemView settingsView, SettingsGroup group,
                                       SettingsItem item) {
        // As a BaseExpandableList reuses the existing views, the type of SettingsItemView can changed
        @Types int type = item.getType();
        settingsView.setType(type);
        settingsView.setGroupId(group.getId());
        settingsView.setItemId(item.getId());

        // set up displayed values
        settingsView.setTitle(item.getTitle());
        settingsView.setDescription(item.getDescription());
        settingsView.setEnabled(item.getState(ItemStates.ENABLED));
        settingsView.showProgress(item.getState(ItemStates.UPDATING));
        switch (type) {
            case Types.OPTIONS:
                settingsView.setOptionsMenu(item.getIntValue());
                break;
            case Types.SWITCH:
                settingsView.setSwitch(item.getBooleanValue());
                break;
            case Types.DEFAULT:
                break;
        }
        settingsView.setContentDescription(item.getContentDescription());
    }

    /**
     * <p>This method checks if the given <code>convertView</code> implements
     * {@link SettingsItemView SettingsItemView} in which case it returns the version version.
     * Otherwise it creates a new instance of {@link SettingsItemView SettingsItemView} with the given
     * parameters and returns it.</p>
     *
     * @param convertView
     *         The view to check and cast.
     * @param parent
     *         The ViewGroup the view to return belongs to.
     *
     * @return The given view casted or a new instance of {@link SettingsItemView SettingsItemView}.
     */
    private SettingsItemView getSettingsItemView(View convertView, ViewGroup parent) {
        if (!(convertView instanceof SettingsItemView)) {
            // create view
            return new SettingsItemView(parent.getContext(), mSettingsItemViewListener);
        }

        return (SettingsItemView) convertView;
    }

    /**
     * <p>To add a group to the list of groups.</p>
     * <p>This method checks first if the group already exists.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param group
     *         The group to add to the list.
     */
    private void addGroup(@NonNull SettingsGroup group) {
        if (!containsGroupId(group.getId())) {
            mmGroupsData.add(group);
        }
    }

    /**
     * <p>To add an item to the given group.</p>
     * <p>This method checks first if the item already exists.</p>
     * <p>This method does NOT update the UI with the new state. To update the UI, call
     * {@link #notifyDataSetChanged() notifyDataSetChanged()}.</p>
     *
     * @param groupId
     *         The id of the group to add the item to.
     * @param item
     *         The item to add to the group.
     */
    private void addItem(int groupId, @NonNull SettingsItem item) {
        SettingsGroup group = getGroupFromId(groupId);
        if (group != null && !group.containsItemId(item.getId())) {
            group.addItem(item);
        }
    }

    /**
     * <p>To check if there is a group in the list which contains the given id.</p>
     *
     * @param groupId
     *         The id to check.
     *
     * @return True if there is already a group with the given id.
     */
    private boolean containsGroupId(int groupId) {
        for (SettingsGroup group : mmGroupsData) {
            if (groupId == group.getId()) {
                return true;
            }
        }
        return false;
    }


    // ========================================================================
    // INTERFACES

    /**
     * <p>A listener to get events or information from this adapter.</p>
     */
    public interface SettingsAdapterListener {

        /**
         * <p>This method is called when the adapter is setting up the view which corresponds to the
         * given item.</p>
         * <p>This method is also called when the UI is updated.</p>
         *
         * @param view
         *         The view component which will be displayed for the given data.
         * @param groupData
         *         The group the item is attached to.
         * @param data
         *         The data of the item.
         */
        void initItemViewDisplay(SettingsItemView view, SettingsGroup groupData, SettingsItem data);

        /**
         * <p>Called when the user interacts with specific components views  from a
         * {@link SettingsItemView SettingsItemView}, such as a {@link android.widget.Switch Switch}
         * when type is {@link Types#SWITCH SWITCH} or options menu when the type is {@link
         * Types#OPTIONS OPTIONS}.</p>
         *
         * @param event
         *         The user interaction type.
         * @param groupId
         *         The id of the group in which the interaction happened.
         * @param childId
         *         The id of the item the user interacted with.
         * @param data
         *         Any complementary data related to the event, see {@link Events Events} for more
         *         information.
         *
         * @return True if the event was consumed, false otherwise.
         */
        boolean onSettingsItemEvent(@Events int event, int groupId, int childId, Object data);

    }

}