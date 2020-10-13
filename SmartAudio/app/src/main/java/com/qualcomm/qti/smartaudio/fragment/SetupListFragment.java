/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

public class SetupListFragment extends SetupFragment {

	protected TextView mInstructionTextView = null;
	protected ExpandableListView mExpandableListView = null;
	protected ListView mListView;
	protected FrameLayout mEmptyFrameLayout = null;

	protected SetupListFragmentListener mSetupListFragmentListener = null;

	@Override
	public void onAttach (Context context) {
		super.onAttach(context);
		mSetupListFragmentListener = (SetupListFragmentListener)context;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

		View frameView = inflater.inflate(R.layout.frame_setup_list, mFrameLayout, true);

		mInstructionTextView = (TextView) frameView.findViewById(R.id.setup_instruction_text);

		mListView = (ListView)view.findViewById(R.id.setup_listview);

		mExpandableListView = (ExpandableListView) frameView.findViewById(R.id.setup_expand_listview);
		mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return true;
			}
		});

		mEmptyFrameLayout = (FrameLayout) frameView.findViewById(R.id.setup_empty_text_frame);
		mExpandableListView.setEmptyView(mEmptyFrameLayout);
		mListView.setEmptyView(mEmptyFrameLayout);

		return view;
	}

	public interface SetupListFragmentListener {
		void onItemClicked(final String tag, final Object object);
	}
}
