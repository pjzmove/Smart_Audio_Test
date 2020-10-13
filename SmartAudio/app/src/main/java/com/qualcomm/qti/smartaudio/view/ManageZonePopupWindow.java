/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.manager.BlurManager;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.ref.WeakReference;

public class ManageZonePopupWindow implements BlurManager.BlurListener, PopupWindow.OnDismissListener {
	private static final String TAG = ManageZonePopupWindow.class.getSimpleName();

	private final PopupWindow mPopupWindow;
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final View mZoneView;
	private View mContentView = null;
	private ImageView mArrowView = null;
	private ListView mListView = null;
	private LinearLayout mNoZonesLayout = null;
	private LinearLayout mSetupZoneLayout = null;

	private WeakReference<BaseActivity> mActivityRef = null;

	public ManageZonePopupWindow(final Context context, final View zoneView) {
		mContext = context;
		mPopupWindow = new PopupWindow(mContext);
		mPopupWindow.setOnDismissListener(this);
		mActivityRef = new WeakReference<>((BaseActivity)mContext);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mZoneView = zoneView;
	}

	public void show() {
		final BaseActivity baseActivity = mActivityRef.get();
		if (Utils.isActivityActive(baseActivity)) {
			// Since BlurManager handles all of the blur, we will send the blur task to its queue.
			baseActivity.showPopupWindow(this);
		}
	}

	@Override
	public void onDismiss() {
		dismiss();
	}

	public void dismiss() {
		final BaseActivity baseActivity = mActivityRef.get();
		if (Utils.isActivityActive(baseActivity)) {
			// Since BlurManager handles all of the blur, we will send the dismiss task to its queue.
			baseActivity.dismiss(this);
		}
	}

	public boolean isShowing() {
		return mPopupWindow.isShowing();
	}

	private void showPopup() {
		final BaseActivity baseActivity = mActivityRef.get();
		if (!Utils.isActivityActive(baseActivity) || (mZoneView == null)) {
			return;
		}
		final Rect viewRect = Utils.getRectOnScreenFromView(mZoneView);
		if (viewRect == null || isShowing()) {
			return;
		}

		mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		int width = baseActivity.isTablet() ? ((ViewGroup)mZoneView.getParent().getParent()).getWidth() :
				ViewGroup.LayoutParams.MATCH_PARENT;
		mPopupWindow.setWidth(width);

		mContentView = mLayoutInflater.inflate(R.layout.popupwindow_zones, null);
		mArrowView = (ImageView) mContentView.findViewById(R.id.popupwindow_arrow_up);
		mListView = (ListView) mContentView.findViewById(R.id.popupwindow_zone_list);
		mNoZonesLayout = (LinearLayout) mContentView.findViewById(R.id.popupwindow_no_zones_layout);
		mSetupZoneLayout = (LinearLayout) mContentView.findViewById(R.id.popupwindow_setup_zones_layout);
		mSetupZoneLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		mPopupWindow.setContentView(mContentView);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

		int positionY = viewRect.bottom - (viewRect.height() / 2);
		int positionX = baseActivity.isTablet() ? (viewRect.right - (viewRect.height() / 2) - (width / 2)) : 0;

		int endOfActionBarHeight = Utils.getActionBarHeight(baseActivity) + Utils.getStatusBarHeight(baseActivity);
		int arrowSide = (endOfActionBarHeight - positionY);
		mArrowView.setLayoutParams(new LinearLayout.LayoutParams(arrowSide, arrowSide));

		float xOffset = baseActivity.isTablet() ? positionX + mContext.getResources().getDimension(R.dimen.app_bar_icon_half_margin) : 0;
		float arrowX = viewRect.exactCenterX() - xOffset - mContext.getResources().getDimension(R.dimen.app_bar_icon_half_size);

		mArrowView.setX(arrowX);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(mZoneView, Gravity.NO_GRAVITY, positionX, positionY);
	}

	private void dismissPopup() {
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}
	}

	@Override
	public void blurStarted() {

	}

	@Override
	public void blurFinished() {
		showPopup();
	}

	@Override
	public void unblurStarted() {
		dismissPopup();
	}

	@Override
	public void unblurFinished() {

	}
}
