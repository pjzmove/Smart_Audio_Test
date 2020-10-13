/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.iotcontrollersdk.constants.IoTType;
import java.util.List;

/**
 * <p>This class inflates the view which represents a child item in the Network Tree view of this
 * application. It keeps the references to the different components of the child view and gives
 * some helper to set them up.</p>
 */
public class NetworkTreeChildView extends FrameLayout {
  /**
   * The view to display the name of the item.
   */
  private TextView mSubTextView;
  /**
   * The view to contain the children of the item.
   */
  private LinearLayout mChildrenContainer;
  /**
   * The vertical line to visually link the next item to this one.
   */
  private View mVerticalLineBottomHalf;
  /**
   * The icon of the item.
   */
  private ImageView mChildIcon;
  /**
   * To indicate it has BT children.
   */
  private View mBluetoothIndicator;
  /**
   * To indicate it has ZigBee devices.
   */
  private View mZigBeeIndicator;
  /**
   * The icon for the menu.
   */
  private View mOptionsButton;

  // ====== CONSTRUCTORS ========================================================================

  /* All mandatory constructors when implementing a View */

  public NetworkTreeChildView(@NonNull Context context) {
    super(context);
    init();
  }

  public NetworkTreeChildView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public NetworkTreeChildView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @SuppressWarnings("unused")
  public NetworkTreeChildView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }


  // ====== PUBLIC METHODS ========================================================================

  /**
   * <p>To set the title of the view.</p>
   *
   * @param title the text to display.
   */
  public void setTitle(String title) {
    mSubTextView.setText(title);
  }

  /**
   * <p>To set up the icon to display.</p>
   * <p>The expected size for the icon is 23dp.</p>
   *
   * @param icon The icon to display in this view.
   */
  public void setIcon(int icon) {
    mChildIcon.setImageResource(icon);
  }

  /**
   * <p>The item view displays some root lines to link the items together. If the item is the last
   * one of a list, the bottom line must be hidden.</p>
   *
   * @param isLast True to hide the root lines, false to display them.
   */
  public void setIsLastChild(boolean isLast) {
      mVerticalLineBottomHalf.setVisibility(isLast ? View.GONE : View.VISIBLE);
  }

  /**
   * <p>Depending on the children of an item some indicators are displayed.</p>
   *
   * @param subTypes All the types of the children.
   */
  public void setSubTypes(List<IoTType> subTypes) {
    // get status
    boolean hasBTChildren = subTypes != null && subTypes.contains(IoTType.BLUETOOTH_DEVICE);
    boolean hasZbChildren = subTypes != null && subTypes.contains(IoTType.ZIGBEE_DEVICE);
    // display corresponding indicators
    mBluetoothIndicator.setVisibility(hasBTChildren ? View.VISIBLE : View.GONE);
    mZigBeeIndicator.setVisibility(hasZbChildren ? View.VISIBLE : View.GONE);
  }

  /**
   * <p>To get the container which will display the potential children of this item.</p>
   *
   * @return the children container.
   */
  public LinearLayout getChildrenContainer() {
    return mChildrenContainer;
  }


  // ====== PRIVATE METHODS ========================================================================

  /**
   * <p>Inflate the layout used for a child item in the network tree view and initialises all the
   * view components.</p>
   */
  private void init() {
    // inflate the layout
    inflate(getContext(), R.layout.network_tree_child, this);

    // get views
    mSubTextView = findViewById(R.id.node_title);
    mChildrenContainer = findViewById(R.id.children_container);
    mVerticalLineBottomHalf = findViewById(R.id.vertical_line_half_bottom);
    mChildIcon = findViewById(R.id.node_icon);
    mBluetoothIndicator = findViewById(R.id.node_bluetooth_indicator);
    mZigBeeIndicator = findViewById(R.id.node_zigbee_indicator);
    mOptionsButton = findViewById(R.id.node_options_button);
  }

}
