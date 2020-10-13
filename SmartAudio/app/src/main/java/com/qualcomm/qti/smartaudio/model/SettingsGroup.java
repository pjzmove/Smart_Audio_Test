/*
  * *************************************************************************************************
  * * Copyright 2019 Qualcomm Technologies International, Ltd.                                      *
  * *************************************************************************************************
  */

 package com.qualcomm.qti.smartaudio.model;

 import android.support.annotation.IntDef;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;

 /**
  * <p>This class encapsulates the data of the groups of settings which can be displayed within the
  * UI.</p>
  * <p>A group of settings contains a list of {@link SettingsItem SettingsItem} with a unique
  * ID.</p>
  */
 public class SettingsGroup {

   /**
    * contains the ID of the group when the user interacts with it.
    */
   private final int mId;
   /**
    * contains the group title to display.
    */
   private final String mTitle;
   /**
    * contains all the child items to display.
    */
   private final List<SettingsItem> mItemsList = new ArrayList<>();
   /**
    * The array to keep the values for all the states of the group.
    */
   private final boolean[] mStates = new boolean[GroupStates.COUNT];

   /**
    * <p>All display states a child can have.</p>
    */
   @IntDef({GroupStates.DISPLAYED})
   @Retention(RetentionPolicy.SOURCE)
   public @interface GroupStates {

     /**
      * When set to <code>True</code>, the item must be displayed. When set to <code>False</code> the
      * item must be hidden.
      */
     int DISPLAYED = 0;
     /**
      * The total number of existing states. This value is not part of the enumeration.
      */
     int COUNT = 1;
   }

   /**
    * <p>To create a new group of settings without a title.</p>
    *
    * @param id The group id.
    */
   public SettingsGroup(int id) {
     this(id, null);
   }

   /**
    * <p>To create a group if settings with a title.</p>
    *
    * @param id The id to identify the group when the user interacts with it.
    * @param title The group title to display.
    */
   public SettingsGroup(int id, String title) {
     mId = id;
     mTitle = title;
     initStates();
   }

   /**
    * <p>To add an item to this group.</p>
    * <p>This method only adds the item if a similar one is not already contained in it.</p>
    *
    * @param data <p>The data that corresponds to the item.</p>
    */
   public void addItem(SettingsItem data) {
     if (!mItemsList.contains(data)) {
       mItemsList.add(data);
     }
   }

   /**
    * <p>To set the given state of the group.</p>
    *
    * @param state The state to set up, One of {@link GroupStates GroupStates}.
    * @param value The new value of the state.
    */
   public void setState(@GroupStates int state, boolean value) {
     mStates[state] = value;
   }

   /**
    * <p>This method gets the item depending on its position in the list of items.</p>
    *
    * @param position The position to get the item from.
    * @return The corresponding {@link SettingsItem SettingsItem} or null if it couldn't be found.
    */
   public SettingsItem getItem(int position) {
     if (position < 0 || position >= mItemsList.size()) {
       return null;
     }
     return mItemsList.get(position);
   }

   /**
    * <p>This method gets the item which corresponds to the given id.</p>
    *
    * @param id The id of the item to get.
    * @return The item if it could be found, null otherwise.
    */
   public SettingsItem getItemFromId(int id) {
     for (SettingsItem data : mItemsList) {
       if (data.getId() == id) {
         return data;
       }
     }
     return null;
   }

   /**
    * <p>This method checks if an item with the given ID exists within this group of settings.</p>
    *
    * @param itemId The item id to look for.
    * @return True if the group contains an item with the given id, false otherwise.
    */
   public boolean containsItemId(int itemId) {
     for (SettingsItem item : mItemsList) {
       if (itemId == item.getId()) {
         return true;
       }
     }
     return false;
   }

   /**
    * <p>To get the id of this group.</p>
    *
    * @return The id as given when creating the group.
    */
   public int getId() {
     return mId;
   }

   /**
    * <p>This method checks if this group has a title.</p>
    * <p>For a group to have a title, the constructor {@link #SettingsGroup(int, String)
    * SettingsGroup(int, String)}
    * must be used.</p>
    *
    * @return True if the title exists.
    */
   public boolean hasTitle() {
     return mTitle != null;
   }

   /**
    * <p>To get the title of this group.</p>
    *
    * @return The title or <code>null</code> if this group doesn't have a title.
    */
   public String getTitle() {
     return mTitle;
   }

   /**
    * <p>To get the number of items this group contains.</p>
    * <p>If this group is not in a {@link GroupStates#DISPLAYED DISPLAYED} state, this method returns
    * <code>0</code>.</p>
    *
    * @return The number of items this group contains.
    */
   public int getItemsCount() {
     return mStates[GroupStates.DISPLAYED] ? mItemsList.size() : 0;
   }

   /**
    * <p>To get the value of the given state this item should be in.</p>
    *
    * @return True if the item is in that state, false otherwise.
    */
   public boolean getState(@GroupStates int state) {
     return mStates[state];
   }

   /**
    * <p>To remove the given item from the group of settings.</p>
    *
    * @param itemId The id of the item to remove from the group.
    */
   public void removeItem(int itemId) {
     SettingsItem item = getItemFromId(itemId);
     if (item != null) {
       mItemsList.remove(item);
     }
   }

   /**
    * <p>To remove all items from the group of settings.</p>
    */
   public void removeAllItems() {
     mItemsList.clear();
   }

   @Override // Object
   public boolean equals(Object o) {
     if (this == o) {
       return true;
     }
     if (o == null || getClass() != o.getClass()) {
       return false;
     }
     SettingsGroup groupData = (SettingsGroup) o;
     return mId == groupData.mId &&
         Objects.equals(mTitle, groupData.mTitle) &&
         Objects.equals(mItemsList, groupData.mItemsList);
   }

   /**
    * <p>This method initialises all the {@link GroupStates GroupStates} states of the group as
    * follows:
    * <ul>
    * <li>Group is displayed.</li>
    * </ul></p>
    */
   private void initStates() {
     mStates[GroupStates.DISPLAYED] = true;
   }

 }