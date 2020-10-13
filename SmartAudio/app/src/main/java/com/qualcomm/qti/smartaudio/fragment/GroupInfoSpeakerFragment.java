/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupListChangedListener;
import com.qualcomm.qti.smartaudio.manager.AllPlayManager.OnGroupInfoStateChangedListener;
import com.qualcomm.qti.smartaudio.util.Utils;
import com.qualcomm.qti.smartaudio.view.ExpandCollapseButton;
import com.qualcomm.qti.smartaudio.view.PlayPauseButton;
import com.qualcomm.qti.smartaudio.view.SmallPlayingAnimationView;

import com.qualcomm.qti.iotcontrollersdk.repository.IoTPlayer;
import com.qualcomm.qti.iotcontrollersdk.repository.IoTGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.qualcomm.qti.smartaudio.manager.BlurManager.BlurListener;
import org.iotivity.base.OcException;

public class GroupInfoSpeakerFragment extends BaseFragment implements OnGroupListChangedListener,
    OnGroupInfoStateChangedListener, BlurListener, View.OnClickListener {
	public static final String TAG = GroupInfoSpeakerFragment.class.getSimpleName();
	private static final int TRANSITION_DURATION = 300;
	private static final int EXPAND_COLLAPSE_DURATION = 500;
	private static final int TRANSITION_START_DELAY = 200;

	private static final String KEY_WIDTH = "keyWidth";
	private static final String KEY_INIT_X = "keyInitX";
	private static final String KEY_INIT_Y = "keyInitY";
	private static final String KEY_ZONE_ID = "keyZoneID";
	private static final String KEY_TOP_BOUND = "keyTopBound";
	private static final String KEY_BOTTOM_BOUND = "keyBottomBound";

	private String mZoneID = null;
	private int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
	private float mInitX = 0.0f;
	private float mInitY = 0.0f;
	private float mTopBound = 0.0f;
	private float mBottomBound = 0.0f;
	private float mBoundDistance = 0.0f;

	private LinearLayout mGroupSpeakerLayout = null;
	private LinearLayout mExpandableGroupSpeakerLayout = null;
	private LinearLayout mSpeakerLayout = null;
	private ExpandCollapseButton mExpandCollapseButton = null;
	private PlayerAdapter mAdapter = null;
	private List<IoTPlayer> mPlayers = Collections.synchronizedList(new ArrayList<IoTPlayer>());
	private Set<IoTPlayer> mPlayersSet = Collections.synchronizedSet(new HashSet<IoTPlayer>());
	private OnGroupSpeakerFragmentListener mOnGroupSpeakerFragmentListener = null;

	private boolean mIsFirstTimeAnimation = true;
	private boolean mUserInteraction = false;

	private enum GroupItemType {
		LABEL,
		PLAYER
	}

	public static GroupInfoSpeakerFragment newInstance(final String zoneID, final int width, final float x, final float y,
												   final float topBound, final float bottomBound) {
		GroupInfoSpeakerFragment fragment = new GroupInfoSpeakerFragment();
		Bundle args = new Bundle();
		args.putString(KEY_ZONE_ID, zoneID);
		args.putInt(KEY_WIDTH, width);
		args.putFloat(KEY_INIT_X, x);
		args.putFloat(KEY_INIT_Y, y);
		args.putFloat(KEY_TOP_BOUND, topBound);
		args.putFloat(KEY_BOTTOM_BOUND, bottomBound);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mOnGroupSpeakerFragmentListener = (OnGroupSpeakerFragmentListener)context;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mZoneID = getArguments().getString(KEY_ZONE_ID);
			mWidth = getArguments().getInt(KEY_WIDTH);
			mInitX = getArguments().getFloat(KEY_INIT_X);
			mInitY = getArguments().getFloat(KEY_INIT_Y);
			mTopBound = getArguments().getFloat(KEY_TOP_BOUND);
			mBottomBound = getArguments().getFloat(KEY_BOTTOM_BOUND);

			mBoundDistance = mBottomBound - mTopBound;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_group_speaker, container, false);

		mGroupSpeakerLayout = (LinearLayout) view.findViewById(R.id.group_speaker_layout);
		ViewGroup.LayoutParams layoutParams = mGroupSpeakerLayout.getLayoutParams();
		layoutParams.width = mWidth;
		mGroupSpeakerLayout.setLayoutParams(layoutParams);

		mGroupSpeakerLayout.setX(mInitX);
		mGroupSpeakerLayout.setY(mInitY);

		mSpeakerLayout = (LinearLayout) view.findViewById(R.id.speaker_layout);

		mExpandableGroupSpeakerLayout = (LinearLayout) mGroupSpeakerLayout.findViewById(R.id.expandable_group_speaker_layout);

		ListView listView = (ListView) mGroupSpeakerLayout.findViewById(R.id.group_speaker_listview);
		mAdapter = new PlayerAdapter();
		listView.setAdapter(mAdapter);

		mExpandCollapseButton = mGroupSpeakerLayout.findViewById(R.id.node_options_button);
		mExpandCollapseButton.setState(ExpandCollapseButton.ExpandCollapseState.EXPAND);
		mExpandCollapseButton.setOnClickListener(view1 -> {
      ExpandCollapseButton button = (ExpandCollapseButton) view1;
      if (button.getState() == ExpandCollapseButton.ExpandCollapseState.COLLAPSE) {
        collapseGroupSpeakerLayout(false);
      }
    });

		Button okButton = (Button) view.findViewById(R.id.group_speaker_button);
		okButton.setOnClickListener(this);

		mGroupSpeakerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (!mGroupSpeakerLayout.isShown()) {
					return;
				}
				mGroupSpeakerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				mBaseActivity.showGroupView(GroupInfoSpeakerFragment.this);
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if ((mApp != null) && mApp.isInit()) {
			// Add the listeners
			mAllPlayManager.addOnZoneListChangedListener(this);
			mAllPlayManager.addOnZoneStateChangedListener(this);

			updateState();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mAllPlayManager != null) {
			// Remove listeners
			mAllPlayManager.removeOnZoneListChangedListener(this);
			mAllPlayManager.removeOnZoneStateChangedListener(this);
		}
	}

	@Override
	protected void updateState() {
		final IoTGroup zone = mAllPlayManager.getZone(mZoneID);
		final List<IoTPlayer> players = mAllPlayManager.getPlayers();
		if ((zone == null) || (players.size() <= 1)) {
			synchronized (this) {
				if (!mIsFirstTimeAnimation) {
					mBaseActivity.dismiss(GroupInfoSpeakerFragment.this);
				}
			}
			return;
		}
		Utils.setNowPlayingSpeakerListItem(getContext(), mGroupSpeakerLayout, zone);

		final PlayPauseButton playPauseButton = mGroupSpeakerLayout.findViewById(R.id.speaker_album_art_play_pause_button);
		playPauseButton.setOnClickListener(view -> {
      try {
        if (playPauseButton.isPlaying()) {
        zone.pause(success -> {});
        } else {
        zone.play(success -> {});
        }
        }catch (OcException e) {
        e.printStackTrace();
        }
    });

		final List<IoTPlayer> zonePlayers = zone.getPlayers();

		synchronized (this) {
			mPlayers.clear();
			mPlayers.addAll(players);
			if (!mUserInteraction) {
				mPlayersSet.clear();
				mPlayersSet.addAll(zonePlayers);
			}
		}

		mAdapter.updatePlayers();

		synchronized (this) {
			if (mIsFirstTimeAnimation) {
				return;
			}
		}

		setCorrectDisplayNameBackground(true);

		Point xyPoint = getXY();
		if (mGroupSpeakerLayout.getY() != xyPoint.y) {
			mGroupSpeakerLayout.setY(xyPoint.y);
		}

		int targetHeight = measureExpandableLayoutHeight(mSpeakerLayout.getMeasuredHeight());
		if (targetHeight != mSpeakerLayout.getMeasuredHeight()) {
			ViewGroup.LayoutParams layoutParams = mExpandableGroupSpeakerLayout.getLayoutParams();
			layoutParams.height = targetHeight;
			mExpandableGroupSpeakerLayout.setLayoutParams(layoutParams);
		}
	}

	@Override
	public void onZoneListChanged() {
		updateInUiThread();
	}

	@Override
	public void onGroupInfoStateChanged() {
		updateInUiThread();
	}

	@Override
	public void blurStarted() {
		mGroupSpeakerLayout.setVisibility(View.INVISIBLE);
	}

	@Override
	public void blurFinished() {
		mGroupSpeakerLayout.setVisibility(View.VISIBLE);
		mGroupSpeakerLayout.bringToFront();
		Point xyPoint = getXY();
		mGroupSpeakerLayout
				.animate()
				.translationX(xyPoint.x)
				.translationY(xyPoint.y)
				.setDuration(TRANSITION_DURATION)
				.setStartDelay(TRANSITION_START_DELAY)
				.setInterpolator(new LinearInterpolator())
				.setListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animator) {}

					@Override
					public void onAnimationEnd(Animator animator) {
						expandGroupSpeakerLayout(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animator) {
								setCorrectDisplayNameBackground(true);
							}

							@Override
							public void onAnimationEnd(Animator animator) {
								mExpandCollapseButton.setState(ExpandCollapseButton.ExpandCollapseState.COLLAPSE);
								synchronized (GroupInfoSpeakerFragment.this) {
									mIsFirstTimeAnimation = false;
								}
								updateInUiThread();
							}

							@Override
							public void onAnimationCancel(Animator animator) {}

							@Override
							public void onAnimationRepeat(Animator animator) {}
						});
					}

					@Override
					public void onAnimationCancel(Animator animator) {}

					@Override
					public void onAnimationRepeat(Animator animator) {}
				}).start();
	}

	@Override
	public void unblurStarted() {}

	@Override
	public void unblurFinished() {
		mOnGroupSpeakerFragmentListener.onGroupSpeakerFragmentDismiss();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() != R.id.group_speaker_button) {
			return;
		}
		collapseGroupSpeakerLayout(true);
	}

	public void updateInitLocation(final float x, final float y) {
		synchronized (this) {
			mInitX = x;
			mInitY = y;
		}
	}

	private void setCorrectDisplayNameBackground(final boolean expanded) {
		RelativeLayout nowPlayingLayout = mGroupSpeakerLayout.findViewById(R.id.node_now_playing_layout);

		mSpeakerLayout.setBackground(ResourcesCompat.getDrawable(getResources(),
				(nowPlayingLayout.isShown()) ? ((expanded) ?
						R.drawable.bgd_list_item_speaker_with_now_playing_expanded :
						R.drawable.bgd_list_item_speaker_with_now_playing) :
						((expanded) ? R.drawable.bgd_list_item_speaker_expanded : R.drawable.bgd_list_item_speaker), null));
	}

	private Point getXY() {
		Point xyPoint = new Point();
		float x;
		synchronized (this) {
			x = mInitX;
		}

		final int speakerLayoutHeight = mSpeakerLayout.getMeasuredHeight();

		if (mBaseActivity.isTablet()) {
			DisplayMetrics displayMetrics = new DisplayMetrics();
			mBaseActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			x = (displayMetrics.widthPixels / 2);
			x -= (mWidth / 2);
		}
		float y = mTopBound + ((mBoundDistance - (speakerLayoutHeight + measureExpandableLayoutHeight(speakerLayoutHeight))) / 2);

		xyPoint.set((int)x, (int)y);
		return xyPoint;
	}

	private int measureExpandableLayoutHeight(final int speakerLayoutHeight) {
		final int cellHeight = getResources().getDimensionPixelSize(R.dimen.speaker_group_cell_height);
		final int buttonHeight = getResources().getDimensionPixelSize(R.dimen.speaker_group_button_height);
		int expandableLayoutHeight = cellHeight + buttonHeight;
		final int size = mPlayers.size();
		for (int i = 0; i < size; i++) {
			if ((expandableLayoutHeight + cellHeight + speakerLayoutHeight) > mBoundDistance) {
				break;
			}
			expandableLayoutHeight += cellHeight;
		}
		return expandableLayoutHeight;
	}

	private void expandGroupSpeakerLayout(final Animator.AnimatorListener listener) {
		int targetHeight = measureExpandableLayoutHeight(mSpeakerLayout.getMeasuredHeight());
		Utils.expandView(mExpandableGroupSpeakerLayout, targetHeight, EXPAND_COLLAPSE_DURATION, new LinearInterpolator(), listener);
	}

	private void collapseGroupSpeakerLayout(final boolean submit) {
		int currentHeight = mExpandableGroupSpeakerLayout.getMeasuredHeight();
		Utils.collapseView(mExpandableGroupSpeakerLayout, currentHeight, EXPAND_COLLAPSE_DURATION, new LinearInterpolator(),
				new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {}

			@Override
			public void onAnimationEnd(Animator animator) {
				mExpandCollapseButton.setState(ExpandCollapseButton.ExpandCollapseState.EXPAND);
				setCorrectDisplayNameBackground(false);
				if (submit) {
					mOnGroupSpeakerFragmentListener.onGroupSpeakerFragmentSubmit(mZoneID, mPlayersSet);
					return;
				}
				final float x, y;
				synchronized (GroupInfoSpeakerFragment.this) {
					x = mInitX;
					y = mInitY;
				}
				mGroupSpeakerLayout
						.animate()
						.translationX(x)
						.translationY(y)
						.setDuration(TRANSITION_DURATION)
						.setInterpolator(new LinearInterpolator())
						.setListener(new Animator.AnimatorListener() {
							@Override
							public void onAnimationStart(Animator animator) {}

							@Override
							public void onAnimationEnd(Animator animator) {
								mBaseActivity.dismiss(GroupInfoSpeakerFragment.this);
							}

							@Override
							public void onAnimationCancel(Animator animator) {}

							@Override
							public void onAnimationRepeat(Animator animator) {}
						}).start();
			}
			@Override
			public void onAnimationCancel(Animator animator) {}

			@Override
			public void onAnimationRepeat(Animator animator) {}
		});

	}

	private class GroupItem {
		public GroupItemType groupItemType;
		public IoTPlayer player;

		public GroupItem(final GroupItemType type, final IoTPlayer player) {
			this.groupItemType = type;
			this.player = player;
		}
	}

	private class PlayerAdapter extends BaseAdapter {

		private List<GroupItem> mGroupItems = Collections.synchronizedList(new ArrayList<GroupItem>());

		public void updatePlayers() {
			mGroupItems.clear();
			mGroupItems.add(new GroupItem(GroupItemType.LABEL, null));
			final List<IoTPlayer> players = mPlayers;
			for (IoTPlayer player : players) {
				mGroupItems.add(new GroupItem(GroupItemType.PLAYER, player));
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mGroupItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mGroupItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			final GroupItem groupItem = (GroupItem)getItem(position);

			if ((convertView == null) || !convertView.getTag().equals(groupItem.groupItemType.toString())) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				switch (groupItem.groupItemType) {
					case LABEL:
						convertView = inflater.inflate(R.layout.list_item_speaker_group_label, viewGroup, false);
						break;
					case PLAYER:
						convertView = inflater.inflate(R.layout.list_item_speaker_group, viewGroup, false);
						break;
				}
			}

			switch (groupItem.groupItemType) {
				case LABEL:
					final TextView playTextView = (TextView) convertView.findViewById(R.id.group_speaker_play_text);
					playTextView.setText(getString(R.string.play_on));
					break;
				case PLAYER:
					final IoTPlayer player = groupItem.player;
					final IoTGroup zone = mAllPlayManager.getZone(player);
					final CheckBox playerCheckBox = (CheckBox) convertView.findViewById(R.id.speaker_group_player_name);
					final SmallPlayingAnimationView playerAnimationView =
							(SmallPlayingAnimationView) convertView.findViewById(R.id.speaker_group_playing_animation);
					playerCheckBox.setText(player.getName().toUpperCase());
					playerCheckBox.setChecked(mPlayersSet.contains(player));
					playerCheckBox.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							synchronized (this) {
								if (playerCheckBox.isChecked()) {
									mPlayersSet.add(player);
								} else {
									mPlayersSet.remove(player);
								}
								mUserInteraction = true;
							}
						}
					});

					boolean visibleAnimation = false;
					if (zone != null) {
						visibleAnimation = (zone.getCurrentItem() != null);
						playerAnimationView.setPlayerState(zone.getPlayerState());
					}
					playerAnimationView.setVisibility((visibleAnimation) ? View.VISIBLE : View.GONE);

					RelativeLayout.LayoutParams checkBoxLayout = (RelativeLayout.LayoutParams) playerCheckBox.getLayoutParams();
					int leftMargin = (int) getResources().getDimension(R.dimen.speaker_group_player_side_margin);
					int rightMargin = leftMargin;
					if (playerAnimationView.getVisibility() == View.VISIBLE) {
						rightMargin = (int) getResources().getDimension(R.dimen.speaker_group_player_animation_spacing);
					}
					checkBoxLayout.setMargins(leftMargin, 0, rightMargin, 0);
					playerCheckBox.setLayoutParams(checkBoxLayout);
					final View dividerView = convertView.findViewById(R.id.speaker_group_divider);
					dividerView.setVisibility((position != mGroupItems.size() - 1) ? View.VISIBLE : View.INVISIBLE);
					break;
			}

			convertView.setTag(groupItem.groupItemType.toString());

			return convertView;
		}
	}

	public interface OnGroupSpeakerFragmentListener {
		void onGroupSpeakerFragmentSubmit(final String zoneID, final Set<IoTPlayer> playerSet);
		void onGroupSpeakerFragmentDismiss();
	}
}
