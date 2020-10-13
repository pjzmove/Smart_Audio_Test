/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.qualcomm.qti.smartaudio.R;

import java.util.Stack;

public class FragmentController {

	FragmentManager mFragmentManager;
	int mFrameID;
	Fragment mCurrentFragment = null;
	Stack<String> mTagStack = null;

	public FragmentController(final FragmentManager manager, final int frameID) {
		mFragmentManager = manager;
		mFrameID = frameID;
		mTagStack = new Stack<>();
	}

	public void push(final Fragment fragment, final String tag) {
		if (tag == null) {
			return;
		}
		if (tag.equals(getCurrentFragmentTag())) {
			return;
		}

		push(fragment, tag, true);
	}

	public void push(final Fragment fragment, final String tag, final boolean animate) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		if (animate) {
			ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
		}

		if (mCurrentFragment != null) {
			ft.hide(mCurrentFragment);
		}
		mCurrentFragment = fragment;

		ft.add(mFrameID, fragment, tag);
		mTagStack.add(tag);
		ft.commit();
		mFragmentManager.executePendingTransactions();
	}

	public void pop() {
		pop(true);
	}

	public void pop(final boolean animate) {
		if (mTagStack.size() > 0) {
			FragmentTransaction ft = mFragmentManager.beginTransaction();
			if (mCurrentFragment != null) {
				if (animate) {
					ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
				}
				ft.remove(mCurrentFragment);
				mTagStack.pop();
			}
			mCurrentFragment = (mTagStack.size() > 0) ?
					mFragmentManager.findFragmentByTag(getCurrentFragmentTag()) :
					null;
			if (mCurrentFragment != null) {
				ft.show(mCurrentFragment);
			}

			ft.commit();
			mFragmentManager.executePendingTransactions();
		}
	}
	public int getCount() {
		return mTagStack.size();
	}

	public String getCurrentFragmentTag() {
		if (mTagStack.isEmpty()) {
			return null;
		}
		return mTagStack.peek();
	}

	public Fragment getCurrentFragment() {
		return mCurrentFragment;
	}

	public Fragment getParentFragment() {
		if (mTagStack.isEmpty() || (mTagStack.size() == 1)) {
			return null;
		}

		String parentTag = mTagStack.elementAt(mTagStack.size() - 2);
		return mFragmentManager.findFragmentByTag(parentTag);
	}

	public void startFragment(final Fragment fragment, final String tag, final boolean animate) {
		if (tag == null) {
			return;
		}
		if (tag.equals(getCurrentFragmentTag())) {
			return;
		}
		clear();
		push(fragment, tag, animate);
	}

	public void clear() {
		while (mTagStack.size() > 0) {
			pop(false);
		}
	}
}
