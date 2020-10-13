/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.MultichannelSetupActivity;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnHomeTheaterChannelChangedListener;
import com.qualcomm.qti.smartaudio.util.UiThreadExecutor;
import com.qualcomm.qti.iotcontrollersdk.constants.MultiChannelMapping.HomeTheaterChannel;
import com.qualcomm.qti.iotcontrollersdk.model.allplay.HomeTheaterChannelMap;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetupSurroundsMainFragment extends BaseFragment implements ExpandableListView.OnChildClickListener,
    OnHomeTheaterChannelChangedListener {

  private final static String TAG = "SetupSurrounds";
  private static final String EXTRA_ID= "DEVICE_ID";
	private static final String EXTRA_HOST= "HOST_NAME";
	private static final String REMOVE_SURROUNDS_ERROR_TAG = "RemoveSurroundsErrorTag";
	private static final String REMOVE_SUBWOOFER_ERROR_TAG = "RemoveSubwooferErrorTag";

  private String mID;
	private SurroundSetupAdapter mAdapter;
	private final static int NONE_OPERATION = 0;
  private final static int ADD_OPERATION  = 1;
  private final static int REMOVE_OPERATION  = 2;

  //REMOVE Operations
  private final static int REMOVE_SUBWOOFER = 0 ;
  private final static int REMOVE_LEFT_RIGHT = 1 ;
  private final static int REMOVE_LEFT_RIGHT_REAR = 2 ;
  private final static int REMOVE_LEFT_RIGHT_UPFIRING = 3 ;
  private final static int REMOVE_LEFT_RIGHT_REAR_UPFIRING = 4 ;

  public static SetupSurroundsMainFragment newInstance(String id) {
    SetupSurroundsMainFragment fragment = new SetupSurroundsMainFragment();
    Bundle bundle = new Bundle();
    bundle.putString(EXTRA_ID,id);
    fragment.setArguments(bundle);
    return fragment;
  }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_surrounds_setup, container, false);

    Bundle arg = getArguments();
    mID = arg.getString(EXTRA_ID);
		ExpandableListView expandableListView = view.findViewById(R.id.surrounds_setup_fragment_listview);

		mAdapter = new SurroundSetupAdapter();
		expandableListView.setAdapter(mAdapter);
    expandableListView.setOnChildClickListener(this);
    return view;
  }

  @Override
	public void onResume() {
    super.onResume();
    IoTPlayer player = mAllPlayManager.getPlayer(mID);
    mAdapter.updateItems(updateItems(player));
    if(mAllPlayManager != null)
      mAllPlayManager.addOnHomeTheaterChannelChangedListener(this);
  }


  @Override
  public void onPause() {
    super.onPause();
    if(mAllPlayManager != null)
      mAllPlayManager.removeOnHomeTheaterChannelChangedListener(this);
  }

  private enum SurroundSettingItemType {
    SURROUNDS,
    ADVANCED_OPTIONS,
  }

  private enum SurroundItemType {
    SUBWOOFER,
    LEFT_RIGHT,
    EXPANDABLE_ITEM,
    ADJUST_AUDIO
  }

  private class SurroundItemDetails {
      private SurroundItemType mItemDetailsType;
      private String mSurroundItem;
      private String mSurroundSubItem;
      private int mOperation = NONE_OPERATION;

      public SurroundItemDetails(SurroundItemType type, String deviceSetting, String subItem) {
        mItemDetailsType = type;
        mSurroundItem = deviceSetting;
        mSurroundSubItem = subItem;
        mOperation = NONE_OPERATION;
      }

      public SurroundItemDetails(SurroundItemType type, String deviceSetting, String subItem, int operation) {
        mItemDetailsType = type;
        mSurroundItem = deviceSetting;
        mSurroundSubItem = subItem;
        mOperation = operation;
      }

      public String getSurroundItem() {
        return mSurroundItem;
      }

      public SurroundItemType getItemDetailsType() {
			return mItemDetailsType;
		}
	}

  private class SurroundSettingItem {

		private String mTitle;
		private SurroundSettingItemType mType;
		private List<SurroundItemDetails> mChildItems;


		public SurroundSettingItem(SurroundSettingItemType type, String title, List<SurroundItemDetails> childItems) {
			mType = type;
			mTitle = title;
			mChildItems = childItems;
		}

		public String getTitle() {
			return mTitle;
		}

		public List<SurroundItemDetails> getChildItems() {
			return mChildItems;
		}
	}

  private List<SurroundSettingItem> updateItems(IoTPlayer player) {

		List<SurroundSettingItem> settingsItems = new ArrayList<>();

		if(player != null) {
      List<SurroundItemDetails> detailList = new ArrayList<>();
      int operation = player.haveHomeTheaterChannel(HomeTheaterChannel.SUBWOOFER) ?REMOVE_OPERATION : ADD_OPERATION;
      SurroundItemDetails detailItem = new SurroundItemDetails(SurroundItemType.SUBWOOFER,
          getString(R.string.setup_surround_item_subwoofer), "", operation);
      detailList.add(detailItem);

      operation = player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_SURROUND) && player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_SURROUND) ?REMOVE_OPERATION : ADD_OPERATION;
      detailItem = new SurroundItemDetails(SurroundItemType.LEFT_RIGHT,
          getString(R.string.setup_surround_item_left_right_surrounds), "",operation);
      detailList.add(detailItem);

      SurroundSettingItem item = new SurroundSettingItem(SurroundSettingItemType.SURROUNDS,
          getString(R.string.setup_surround_listview_title), detailList);
      settingsItems.add(item);

      detailList = new ArrayList<>();
      detailItem = new SurroundItemDetails(SurroundItemType.EXPANDABLE_ITEM,
          getString(R.string.setup_surround_item_additional_surrounds), null);
      detailList.add(detailItem);

      detailItem = new SurroundItemDetails(SurroundItemType.ADJUST_AUDIO,
          getString(R.string.setup_surround_item_adjust_audio_level), null);
      detailList.add(detailItem);

      item = new SurroundSettingItem(SurroundSettingItemType.ADVANCED_OPTIONS,
          getString(R.string.setup_surround_item_advanced_options), detailList);
      settingsItems.add(item);

      updateAdditionViewHolders(player);
    }

		return settingsItems;
	}

	 private void createAdditionalSurroundsView(LinearLayout container) {

    AdditionalItemView addView = new AdditionalItemView(container.getContext());
	  addView.mName.setText(getString(R.string.setup_surround_item_left_right_rear_surrounds));
	  addView.mDetail.setText(getString(R.string.setup_surround_item_left_right_rear_surrounds_detail));
	  final int op = mAdditionalViewHolder.get(0);
	  addView.mIndicator.setText( op == REMOVE_OPERATION ? "⊖" : op == ADD_OPERATION?"⊕":"");
	  container.addView(addView.mView);
	  addView.mView.setClickable(true);
	  addView.mView.setOnClickListener(v->{
      if(op == ADD_OPERATION)
          startMultiChannelSetupActivity(MultichannelSetupActivity.SetupType.ADD_REAR_SURROUNDS);
      else if(op == REMOVE_OPERATION)
        removeSurrounds(REMOVE_LEFT_RIGHT_REAR);
	  });

    addView = new AdditionalItemView(container.getContext());
	  addView.mName.setText(getString(R.string.setup_surround_item_left_right_upfiring_surrounds));
	  addView.mDetail.setText(getString(R.string.setup_surround_item_left_right_upfiring_surrounds_detail));
	  final int op1 = mAdditionalViewHolder.get(1);
	  addView.mIndicator.setText( op == REMOVE_OPERATION ? "⊖" : op == ADD_OPERATION?"⊕":"");
	  container.addView(addView.mView);
	  addView.mView.setClickable(true);
	  addView.mView.setOnClickListener(v->{
	      if(op1 == ADD_OPERATION)
          startMultiChannelSetupActivity(MultichannelSetupActivity.SetupType.ADD_UPFIRING_SURROUNDS);
        else if(op1 == REMOVE_OPERATION)
          removeSurrounds(REMOVE_LEFT_RIGHT_UPFIRING);
	  });

	  /*addView = new AdditionalItemView(container.getContext());
	  addView.mName.setText(getString(R.string.setup_surround_item_left_right_rear_upfiring_surrounds));
	  addView.mDetail.setText(getString(R.string.setup_surround_item_left_right_rear_upfiring_surrounds_detail));
	  final op2 = mAdditionalViewHolder.get(2);
	  addView.mIndicator.setText( op == REMOVE_OPERATION ? "⊖" : op == ADD_OPERATION?"⊕":"");
	  container.addView(addView.mView);
	  addView.mView.setClickable(true);
	  addView.mView.setOnClickListener(v->{
	      if(op2 == ADD_OPERATION)
          startMultiChannelSetupActivity(MultichannelSetupActivity.SetupType.ADD_REAR_UPFIRING_SURROUNDS);
        else if(op2 == REMOVE_OPERATION)
          removeSurrounds(REMOVE_LEFT_RIGHT_REAR_UPFIRING);
	  });
	  */
   }

   private void updateAdditionViewHolders(IoTPlayer player) {

     int operation = player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_REAR_SURROUND) && player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_REAR_SURROUND)?REMOVE_OPERATION: ADD_OPERATION;
     mAdditionalViewHolder.set(0,operation);

     operation = player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_UPFIRING_SURROUND) && player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_UPFIRING_SURROUND)?REMOVE_OPERATION: ADD_OPERATION;
     mAdditionalViewHolder.set(1,operation);

     operation = player.haveHomeTheaterChannel(HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND) && player.haveHomeTheaterChannel(HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND)?REMOVE_OPERATION: ADD_OPERATION;
     mAdditionalViewHolder.set(2,operation);
   }

  @Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
    SurroundSettingItemType itemType = ((SurroundSettingItem) parent.getExpandableListAdapter()
        .getGroup(groupPosition)).mType;
    SurroundItemDetails childItem = (SurroundItemDetails) parent.getExpandableListAdapter()
        .getChild(groupPosition, childPosition);
    SurroundItemType itemDetailsType = childItem.getItemDetailsType();
    switch (itemType) {
      case SURROUNDS:
        switch (itemDetailsType) {
          case SUBWOOFER:
          if(childItem.mOperation == ADD_OPERATION) {
            startMultiChannelSetupActivity(MultichannelSetupActivity.SetupType.ADD_SUBWOOFER);
          } else if(childItem.mOperation == REMOVE_OPERATION) {
              removeSurrounds(REMOVE_SUBWOOFER);
          }
          break;
          case LEFT_RIGHT:
          if(childItem.mOperation == ADD_OPERATION)
            startMultiChannelSetupActivity(MultichannelSetupActivity.SetupType.ADD_SURROUNDS);
          else if(childItem.mOperation == REMOVE_OPERATION)
            removeSurrounds(REMOVE_LEFT_RIGHT);
          break;
        }
        break;
      case ADVANCED_OPTIONS:
        switch (itemDetailsType) {
          case ADJUST_AUDIO:
            startMultiChannelSetupActivity(MultichannelSetupActivity.SetupType.ADJUST_AUDIO);
          break;
          case EXPANDABLE_ITEM:
            mAdapter.clickOnAdditions();
          break;
        }
        break;
    }
    return true;
  }

  private class SurroundSetupAdapter extends BaseExpandableListAdapter {

		private List<SurroundSettingItem> mSurroundSettingsItem;
		private boolean mIsAdditionExpanded = false;

		public void updateItems(List<SurroundSettingItem> deviceSettingsItem) {
			mSurroundSettingsItem = deviceSettingsItem;
			notifyDataSetChanged();
		}

		public void clickOnAdditions() {
		  mIsAdditionExpanded = !mIsAdditionExpanded;
		  notifyDataSetChanged();
		}

		@Override
		public int getGroupCount() {
		  if( mSurroundSettingsItem == null) return 0;
			return mSurroundSettingsItem.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			List<SurroundItemDetails> children = mSurroundSettingsItem.get(groupPosition).getChildItems();
			if(children != null) {
        return children.size();
      } else {
        return 0;
      }
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mSurroundSettingsItem.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mSurroundSettingsItem.get(groupPosition).getChildItems().get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.list_item_settings_headers, parent, false);
			}

			TextView textView = convertView.findViewById(R.id.settings_header_text_view);
			textView.setText(((SurroundSettingItem) getGroup(groupPosition)).getTitle());

			ExpandableListView listView = (ExpandableListView) parent;
      listView.expandGroup(groupPosition);
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

			SurroundItemDetails itemDetails = ((SurroundItemDetails) getChild(groupPosition, childPosition));

      if (convertView == null) {
          LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          convertView = layoutInflater.inflate(R.layout.surround_list_item_child, parent, false);
			}

      TextView indicatorView = convertView.findViewById(R.id.device_settings_child_indicator);
			if (itemDetails != null) {
			  if(itemDetails.mItemDetailsType == SurroundItemType.EXPANDABLE_ITEM) {
				  ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
				  layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
				  LinearLayout additionalLayout = convertView.findViewById(R.id.expandable_items);
				  additionalLayout.removeAllViews();
				  indicatorView.setVisibility(View.VISIBLE);
				  if(mIsAdditionExpanded) {
            createAdditionalSurroundsView(additionalLayout);
            indicatorView.setText("▲");
          } else {
            indicatorView.setText("▼");
          }
        } else if(itemDetails.mOperation != NONE_OPERATION) {
           indicatorView.setVisibility(View.VISIBLE);
				   indicatorView.setText(itemDetails.mOperation == REMOVE_OPERATION ?"⊖" : "⊕");
        } else {
          indicatorView.setVisibility(View.GONE);
        }

        TextView textView = convertView.findViewById(R.id.device_settings_child_text_view);
        textView.setText(itemDetails.getSurroundItem());

        TextView subTextView = convertView.findViewById(R.id.device_settings_child_sub_text_view);
        if(itemDetails.mSurroundSubItem != null && !itemDetails.mSurroundSubItem.isEmpty()) {
          subTextView.setVisibility(View.VISIBLE);
          subTextView.setText(itemDetails.mSurroundSubItem);
        }

			}
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

  private List<Integer> mAdditionalViewHolder = Arrays.asList(NONE_OPERATION,NONE_OPERATION,NONE_OPERATION);
  public class AdditionalItemView {
    View mView;
    TextView mName;
    TextView mDetail;
    TextView mIndicator;

    public AdditionalItemView(Context context) {
        this(LayoutInflater.from(context).inflate(R.layout.additional_surround_view,null));
    }

    private AdditionalItemView(View view) {
        mView = view;
        mName = view.findViewById(R.id.device_settings_child_text_view);
        mIndicator = view.findViewById(R.id.add_remove_surround_view);
        mDetail = view.findViewById(R.id.device_settings_child_sub_text_view);
    }
  }

  private void startMultiChannelSetupActivity(final MultichannelSetupActivity.SetupType setupType) {
		Intent intent = new Intent(getActivity(), MultichannelSetupActivity.class);
		intent.putExtra(MultichannelSetupActivity.SOUNDBAR_ID_KEY, mID);
		intent.putExtra(MultichannelSetupActivity.SETUP_TYPE_KEY, setupType);
		startActivity(intent);
	}

  private void removeSurrounds(int op) {

    List<HomeTheaterChannel> channels = null;

    switch (op) {
      case REMOVE_SUBWOOFER:
        channels = Arrays.asList(HomeTheaterChannel.SUBWOOFER);
        break;
      case REMOVE_LEFT_RIGHT:
        channels = Arrays.asList(HomeTheaterChannel.LEFT_SURROUND,HomeTheaterChannel.RIGHT_SURROUND);
        break;
      case REMOVE_LEFT_RIGHT_REAR:
        channels = Arrays.asList(HomeTheaterChannel.LEFT_REAR_SURROUND,HomeTheaterChannel.RIGHT_REAR_SURROUND);
        break;
      case REMOVE_LEFT_RIGHT_UPFIRING:
        channels = Arrays.asList(HomeTheaterChannel.LEFT_UPFIRING_SURROUND,HomeTheaterChannel.RIGHT_UPFIRING_SURROUND);
        break;
      case REMOVE_LEFT_RIGHT_REAR_UPFIRING:
        channels = Arrays.asList(HomeTheaterChannel.LEFT_REARUPFIRING_SURROUND,HomeTheaterChannel.RIGHT_REARUPFIRING_SURROUND);
        break;
      default:
        break;
    }

    IoTPlayer player = mAllPlayManager.getPlayer(mID);
    if (channels!= null && player != null) {

      List<HomeTheaterChannel> removeChannels = new ArrayList<>();
      for(HomeTheaterChannel ch:channels) {
          if(player.isHomeTheaterChannelPlayerInfoAvailable(ch)) {
            removeChannels.add(ch);
          }
      }

      player.removeHomeTheaterChannelSurrounds(removeChannels,success -> {
          if(success) {

          } else {
            CustomDialogFragment removeSurrondDialogFragment = CustomDialogFragment
                .newDialog(REMOVE_SURROUNDS_ERROR_TAG,
                    getString(R.string.remove_error_dialog_title),
                    getString(R.string.remove_error_dialog_message), getString(R.string.ok), null);
            removeSurrondDialogFragment.setButtonClickedListener(
                new CustomDialogFragment.OnCustomDialogButtonClickedListener() {
                  @Override
                  public void onPositiveButtonClicked(String tag) {
                  }

                  @Override
                  public void onNegativeButtonClicked(String tag) {
                  }
                });

            mBaseActivity.showDialog(removeSurrondDialogFragment, REMOVE_SURROUNDS_ERROR_TAG);
          }
      });
    }
  }

  @Override
  public void onHomeTheaterChannelUpdate(final IoTPlayer player, HomeTheaterChannelMap channelMap) {
    Log.d(TAG,"onHomeTheaterChannelUpdate :"+ player.getPlayerId() + ",mID:" + mID);
    if(player != null && player.getPlayerId().equalsIgnoreCase(mID)) {
      UiThreadExecutor.getInstance().execute(()->
        mAdapter.updateItems(updateItems(player))
      );
    }
  }

  @Override
  public void onHomeTheaterChannelPlayerInfoAvailable(IoTPlayer player, HomeTheaterChannel channel,
      boolean available) {
    if(player != null && player.getPlayerId().equalsIgnoreCase(mID)) {
      UiThreadExecutor.getInstance().execute(()->
        mAdapter.updateItems(updateItems(player))
      );
    }
  }

  @Override
  public void onHomeTheaterChannelDeviceInfoAvailable(IoTPlayer player, HomeTheaterChannel channel,
      boolean available) {
    if(player != null && player.getPlayerId().equalsIgnoreCase(mID)) {
      UiThreadExecutor.getInstance().execute(()->
        mAdapter.updateItems(updateItems(player))
      );
    }
  }

  @Override
  public void onHomeTheaterChannelVolumeChanged(IoTPlayer player,
      HomeTheaterChannel channel, int volume, boolean user) {

  }
}
