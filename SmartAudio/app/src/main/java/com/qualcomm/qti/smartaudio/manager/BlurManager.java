/*
 * *************************************************************************************************
 *  * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 *  *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
//import android.support.v8.renderscript.Allocation;
//import android.support.v8.renderscript.Element;
//import android.support.v8.renderscript.RSRuntimeException;
//import android.support.v8.renderscript.RenderScript;
//import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.qualcomm.qti.smartaudio.R;
import com.qualcomm.qti.smartaudio.activity.BaseActivity;
import com.qualcomm.qti.smartaudio.fragment.CustomDialogFragment;
import com.qualcomm.qti.smartaudio.util.AsyncTaskQueue;
import com.qualcomm.qti.smartaudio.util.BaseAsyncTask;
import com.qualcomm.qti.smartaudio.util.Utils;

import java.lang.ref.WeakReference;

public class BlurManager {
	private static final String TAG = BlurManager.class.getSimpleName();

	// The holding activity
	private WeakReference<BaseActivity> mActivityRef;

	// A queue to have 1 task being run at a time
	private AsyncTaskQueue mAsyncQueue = null;

	private String mCurrentTag = null;
	private BlurListener mCurrentBlurListener = null;

	// Tracking the blurred image
	private ImageView mBlurredView = null;

	private boolean mUseMainResource = false;
	private boolean mBlurActionBar = true;

	public void onResume() {
		mAsyncQueue.onResume();
	}

	public void onPause() {
		mAsyncQueue.onPause();
	}

	public void showDialog(CustomDialogFragment dialogFragment, String tag) {
		if ((mBlurredView != null) && !mBlurActionBar) {
			dismiss();
		}
		mAsyncQueue.add(new ShowDialogAsyncTask(dialogFragment, tag));
	}

	public String getCurrentDialogTag() {
		return null;
	}

	public void showNavigationDrawer(final BlurListener listener) {
		if ((mBlurredView != null) && !mUseMainResource) {
			dismiss();
		}
		mAsyncQueue.add(new BlurListenerAsyncTask(true, true, mActivityRef.get(), listener));
	}

	public void showPopupWindow(final BlurListener listener) {
		if ((mBlurredView != null) && mBlurActionBar) {
			dismiss();
		}
		mAsyncQueue.add(new BlurListenerAsyncTask(false, false, mActivityRef.get(), listener));
	}

	public void showGroupView(final BlurListener listener) {
		if ((mBlurredView != null) && !mUseMainResource) {
			dismiss();
		}
		mAsyncQueue.add(new BlurListenerAsyncTask(true, true, mActivityRef.get(), listener));
	}

	public boolean isDialogShown(String tag) {
		final BaseActivity baseActivity = mActivityRef.get();
		if (!Utils.isActivityActive(baseActivity)) {
			return false;
		}
		return (baseActivity.getSupportFragmentManager().findFragmentByTag(tag) != null);
	}

	public void dismissDialog(String tag) {
		mAsyncQueue.add(new DismissDialogAsyncTask(tag));
	}

	public void dismiss(BlurListener listener) {
		mAsyncQueue.add(new UnblurListenerAsyncTask(listener));
	}

	private void dismiss() {
		if (mCurrentTag != null) {
			dismissDialog(mCurrentTag);
		}
		if (mCurrentBlurListener != null) {
			dismiss(mCurrentBlurListener);
		}
	}

	public BlurManager(final BaseActivity activity) {
		mActivityRef = new WeakReference<>(activity);
		mAsyncQueue = new AsyncTaskQueue(activity);
	}

	public void blur(final boolean useMainResource, final boolean blurActionBar) {
		if ((mBlurredView != null) && ((mUseMainResource != useMainResource) || (mBlurActionBar != blurActionBar))) {
			dismiss();
		}
		mAsyncQueue.add(new ShowViewAsyncTask(useMainResource, blurActionBar, mActivityRef.get()));
	}

	public void unblur() {
		mAsyncQueue.add(new DismissViewAsyncTask(mActivityRef.get()));
	}

	/**
	 * This function shows the dialog fragment with associated tag
	 * @param dialogFragment the DialogFragment object
	 * @param tag the tag of the DialogFragment for tracking purpose
	 */
	private void show(final DialogFragment dialogFragment, final String tag) {
		// Check if dialogFragment/tag are null, and that activity is still visible
		final BaseActivity baseActivity = mActivityRef.get();
		if ((dialogFragment != null) && (tag != null) &&
				Utils.isActivityActive(baseActivity)) {
			mCurrentTag = tag;
			// Show dialog fragment
			dialogFragment.show(baseActivity.getSupportFragmentManager(), tag);
			// This call waits for dialog to be actually shown
			baseActivity.getSupportFragmentManager().executePendingTransactions();
		}
	}

	/**
	 * This function dismisses the dialog fragment
	 * @param dialogFragment the DialogFragment object
	 */
	private void dismiss(final DialogFragment dialogFragment) {
		// Check if dialogFragment/tag are null, and that activity is still visible
		final BaseActivity baseActivity = mActivityRef.get();
		if ((dialogFragment != null) && Utils.isActivityActive(baseActivity)) {
			mCurrentTag = null;
			// Dismiss the dialog fragment
			dialogFragment.dismiss();
			// This call waits for the dialog to be actually dismissed
			baseActivity.getSupportFragmentManager().executePendingTransactions();
		}
	}

	/**
	 * This will be the async task that dismiss dialogs
	 */
	private class DismissDialogAsyncTask extends DismissViewAsyncTask {
		final private String mTag;

		public DismissDialogAsyncTask(final String tag) {
			super(mActivityRef.get());
			mTag = tag;
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					dismissDialg();
				}

				@Override
				public void onRequestFailed() {}
			};
		}

		@Override
		protected void onPreExecute() {
			if (isNoWifi() || !isDialogShown(mTag)) {
				cancel(true);
			}
			super.onPreExecute();
		}

		private void dismissDialg() {
			final BaseActivity baseActivity = mActivityRef.get();
			if (Utils.isActivityActive(baseActivity)) {
				final DialogFragment dialogFragment = (DialogFragment) baseActivity.getSupportFragmentManager().findFragmentByTag(mTag);
				if (dialogFragment != null) {
					// We found the fragment and activity is still there, dismiss the dialog
					dismiss(dialogFragment);
				}
			}
		}
	}

	/**
	 * This async task class will handle showing dialog
	 */
	private class ShowDialogAsyncTask extends ShowViewAsyncTask {
		// The dialog fragment and tag to show
		final DialogFragment mDialogFragment;
		final String mTag;

		public ShowDialogAsyncTask(final DialogFragment dialogFragment, final String tag) {
			super(mActivityRef.get());
			mDialogFragment = dialogFragment;
			mTag = tag;
			mListener = new RequestListener() {
				@Override
				public void onRequestSuccess() {
					showDialog();
				}

				@Override
				public void onRequestFailed() {}
			};
		}

		private void showDialog() {
			final BaseActivity baseActivity = getActiveActivity();
			if (baseActivity != null) {
				// We already have the blurred view, we might have a dialog already.
				final DialogFragment dialogFragment = (DialogFragment) baseActivity.getSupportFragmentManager().findFragmentByTag(mCurrentTag);
				if (dialogFragment != null) {
					// Dismiss the existing dialog
					dismiss(dialogFragment);
				}
				// Show the new dialog
				show(mDialogFragment, mTag);
			}
		}
	}

	private class BlurListenerAsyncTask extends ShowViewAsyncTask {
		protected BlurListener mBlurListener;

		public BlurListenerAsyncTask(final boolean useMainResource, final boolean blurActionBar,
									 final BaseActivity baseActivity, final BlurListener blurListener) {
			super(useMainResource, blurActionBar, baseActivity);
			mBlurListener = blurListener;
		}

		@Override
		protected void prepare() {
			if (mBlurListener != null) {
				mBlurListener.blurStarted();
			}
		}

		@Override
		protected void finished() {
			if (mBlurListener != null) {
				mBlurListener.blurFinished();
			}
			mCurrentBlurListener = mBlurListener;
			super.finished();
		}
	}

	private class UnblurListenerAsyncTask extends DismissViewAsyncTask {
		protected BlurListener mBlurListener;
		public UnblurListenerAsyncTask(final BlurListener blurListener) {
			super(mActivityRef.get());
			mBlurListener = blurListener;
		}

		@Override
		protected void prepare() {
			if (mBlurListener != null) {
				mBlurListener.unblurStarted();
			}
		}

		@Override
		protected void finished() {
			if (mBlurListener != null) {
				mBlurListener.unblurFinished();
			}
			mCurrentBlurListener = null;
			super.finished();
		}
	}

	/**
	 * This will be the base class to show views such as dialog, drawer or popup window.
	 * The real purpose for it is for the pre execute to check if we can even show the view based on connectivity.
	 * If no wifi dialog is shown, no other views can be shown.
	 */
	private class ShowViewAsyncTask extends BlurViewAsyncTask {
		public ShowViewAsyncTask(BaseActivity baseActivity) {
			super(baseActivity);
		}

		public ShowViewAsyncTask(boolean useMainResource, boolean blurActionBar, BaseActivity baseActivity) {
			super(useMainResource, blurActionBar, baseActivity);
		}

		@Override
		protected void onPreExecute() {
			final BaseActivity baseActivity = getActiveActivity();
			if ((baseActivity == null) || isDialogShown(baseActivity.DIALOG_NO_WIFI_TAG)) {
				// If no wifi dialog is shown, or activity is down, we can forget about this.  Can cancel
				cancel(true);
			}
			super.onPreExecute();
		}
	}

	/**
	 * This will be the base class to dismiss views such as dialog, drawer or popup window.
	 * The real purpose for it is for the pre execute to check if we can even dismiss the view based on connectivity.
	 * If no wifi dialog is shown, no other views can be dismissed.
	 */
	private class DismissViewAsyncTask extends UnblurViewAsyncTask {
		public DismissViewAsyncTask(BaseActivity baseActivity) {
			super(baseActivity);
		}

		@Override
		protected void onPreExecute() {
			if (isNoWifi()) {
				cancel(true);
				return;
			}
			prepare();
		}

		protected boolean isNoWifi() {
			final BaseActivity baseActivity = getActiveActivity();
			if (baseActivity != null) {
				if (isDialogShown(baseActivity.DIALOG_NO_WIFI_TAG) && !baseActivity.isConnectedToNetwork()) {
					// If no wifi is shown, and we are not connected to network, then we can say it is no wifi.
					return true;
				}
			}
			return false;
		}
	}

	private class BlurViewAsyncTask extends BaseAsyncTask {
		public static final String BLUR_VIEW_TAG = "BlurViewTag";

		// Animation duration of blur and un-blur
		public static final int ANIMATION_DURATION = 200;

		// The blur radius used for Intrinsic Gausian blur filter
		private static final int BLUR_RADIUS = 15;

		// The internal helper objects to create the blur
		private Bitmap mBackgroundBitmap;
		private Bitmap mBlurredBitmap;
		private View mBackgroundView;
		private int mHeight = 0;
		private int mWidth = 0;
		private int mTopOffset = 0;
		private int mActionBarHeight = 0;
		private int mStatusBarHeight = 0;

		public BlurViewAsyncTask(final BaseActivity baseActivity) {
			super(baseActivity);
			mUseMainResource = false;
			mBlurActionBar = true;
		}

		public BlurViewAsyncTask(final boolean useMainResource, final boolean blurActionBar, final BaseActivity baseActivity) {
			super(baseActivity);
			mUseMainResource = useMainResource;
			mBlurActionBar = blurActionBar;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			final BaseActivity baseActivity = getActiveActivity();
			if (!Utils.isActivityActive(baseActivity)) {
				return;
			}

			if (mBlurredView != null) {
				// We already have the blurred image, no need to do it again.
				return;
			}

			// How this work?
			// We blur the image in 3 parts:
			// In pre execute, we obtain the current bitmap of what the activity shows
			// In background, we take the bitmap obtained and blur it
			// In post execute, we take the blurred image and a new overlay image and add them to the activity, then show dialog
			// This gets the current bitmap on the activity view
			mBackgroundView = baseActivity.getWindow().getDecorView();
			mBackgroundView.destroyDrawingCache();
			mBackgroundView.setDrawingCacheEnabled(true);
			mBackgroundView.buildDrawingCache(true);
			mBackgroundBitmap = mBackgroundView.getDrawingCache(true);

			if (mBackgroundBitmap == null) {
				// I think this happens when we have an orientation change.  We will have to re-measure the layout and get the view again.
				Rect rect = new Rect();
				mBackgroundView.measure(
						View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY),
						View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
				);
				mBackgroundView.layout(0, 0, mBackgroundView.getMeasuredWidth(),
						mBackgroundView.getMeasuredHeight());
				mBackgroundView.destroyDrawingCache();
				mBackgroundView.setDrawingCacheEnabled(true);
				mBackgroundView.buildDrawingCache(true);
				mBackgroundBitmap = mBackgroundView.getDrawingCache(true);
			}

			//We cannot obtain background view width and height in background thread, so get values here.
			mWidth = mBackgroundView.getWidth();
			mHeight = mBackgroundView.getHeight();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			final BaseActivity baseActivity = getActiveActivity();
			if (isCancelled() || (mBlurredView != null) || (mBackgroundBitmap == null) || (baseActivity == null)) {
				// If we are cancelled, or we have blurred view, or somehow we are still unable to get the background bitmap
				return null;
			}
      /*
			// Here we need to figure out the offset for the new blurred bitmap size
			mStatusBarHeight = ControllerSdkUtils.getStatusBarHeight(baseActivity);
			mActionBarHeight = (mBlurActionBar) ? 0 : ControllerSdkUtils.getActionBarHeight(baseActivity);

			mTopOffset = mStatusBarHeight + mActionBarHeight;

			int bottomOffset = ControllerSdkUtils.getNavigationBarHeight(baseActivity);

			// This will be the what we get from the background bitmap, minus the offsets
			Rect srcRect = new Rect(0, mTopOffset, mBackgroundBitmap.getWidth(), mBackgroundBitmap.getHeight() - bottomOffset);

			// We will crop at the end, to add to the blurred-ness, so we can go with 1/4 scale image
			double height = Math.ceil((mHeight - mTopOffset - bottomOffset) / 4.0f);
			double width = Math.ceil((mWidth * height / (mHeight - mTopOffset - bottomOffset)));

			// Create the new blurred bitmap with the new size
			mBlurredBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mBlurredBitmap);
			Paint paint = new Paint();
			paint.setFlags(Paint.FILTER_BITMAP_FLAG);
			final RectF destRect = new RectF(0, 0, mBlurredBitmap.getWidth(), mBlurredBitmap.getHeight());

			// Here we draw the new bitmap with background bitmap
			canvas.drawBitmap(mBackgroundBitmap, srcRect, destRect, paint);

			try {
				// This uses Intrinsic Gausian blur filter to blur the bitmap with blur radius
				final RenderScript renderScript = RenderScript.create(baseActivity);
				final Allocation input = Allocation.createFromBitmap(renderScript, mBlurredBitmap,
						Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
				final Allocation output = Allocation.createTyped(renderScript, input.getType());
				final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
				script.setRadius(BLUR_RADIUS);
				script.setInput(input);
				script.forEach(output);
				output.copyTo(mBlurredBitmap);
			} catch (RSRuntimeException e) {
				e.printStackTrace();
			}
      */
			return null;
		}

		@Override
		protected void onPostExecute(final Void voidParam) {
			boolean done = true;
			final BaseActivity baseActivity = getActiveActivity();
			if (baseActivity != null) {
				// If the activity is still around.
				if (mBlurredView == null) {
					done = false;
					mBlurredView = new ImageView(baseActivity);
					mBlurredView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					mBlurredView.setImageDrawable(ResourcesCompat.getDrawable(baseActivity.getResources(), R.drawable.bgd_overlay, null));
					mBlurredView.setBackground(new BitmapDrawable(baseActivity.getResources(), mBlurredBitmap));
					mBlurredView.setTag(BLUR_VIEW_TAG);
					if (mUseMainResource && (baseActivity.findViewById(R.id.main_layout) != null)) {
						FrameLayout frameLayout = (FrameLayout)baseActivity.findViewById(R.id.main_layout);
						// This is to prevent any views on top of the blurred view to pass the click event to below views
						mBlurredView.setClickable(true);
						frameLayout.addView(mBlurredView, getBlurFrameLayout());
					} else {
						baseActivity.getWindow().addContentView(mBlurredView, getBlurFrameLayout());
					}

					// blur image starts off invisible
					mBlurredView.setAlpha(0f);
					mBlurredView
							.animate()
							.alpha(1f)
							.setDuration(ANIMATION_DURATION)
							.setInterpolator(new LinearInterpolator())
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									super.onAnimationEnd(animation);
									BlurViewAsyncTask.super.onPostExecute(voidParam);
								}
							}).start();

					// Clean up
					if (mBackgroundBitmap != null) {
 						mBackgroundBitmap.recycle();
 						mBackgroundBitmap = null;
 					}
 					if (mBackgroundView != null) {
 						mBackgroundView.destroyDrawingCache();
 						mBackgroundView.setDrawingCacheEnabled(false);
 						mBackgroundView = null;
 					}
				}
			}
			if (done) {
				super.onPostExecute(voidParam);
			}
		}

		private FrameLayout.LayoutParams getBlurFrameLayout() {
			FrameLayout.LayoutParams frameLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			final BaseActivity baseActivity = getActiveActivity();
			if ((baseActivity != null) && (baseActivity.getSupportActionBar() != null)) {
				int topOffset = mTopOffset;
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
					topOffset -= mStatusBarHeight;
				}
				frameLayout.setMargins(0, (!mUseMainResource) ? topOffset : 0, 0, 0);
				frameLayout.gravity = Gravity.TOP;
			}
			return frameLayout;
		}
	}

	private class UnblurViewAsyncTask extends BaseAsyncTask {

		public UnblurViewAsyncTask(BaseActivity baseActivity) {
			super(baseActivity);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			try {
				Thread.sleep(BlurViewAsyncTask.ANIMATION_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void voidParam) {
			boolean done = true;
			final BaseActivity baseActivity = getActiveActivity();
			if (baseActivity != null) {
				if (mBlurredView != null) {
					done = false;
					mBlurredView
							.animate()
							.alpha(0f)
							.setDuration(BlurViewAsyncTask.ANIMATION_DURATION)
							.setInterpolator(new AccelerateInterpolator())
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									// Animation ended
									super.onAnimationEnd(animation);
									removeBlurredView(baseActivity);
									UnblurViewAsyncTask.super.onPostExecute(voidParam);
								}

								@Override
								public void onAnimationCancel(Animator animation) {
									// Animation cancelled
									super.onAnimationCancel(animation);
									removeBlurredView(baseActivity);
									UnblurViewAsyncTask.super.onPostExecute(voidParam);
								}
							}).start();
				}
			}
			if (done) {
				super.onPostExecute(voidParam);
			}
		}

		private void removeBlurredView(final BaseActivity baseActivity) {
			if (mBlurredView != null) {
				// Set blurred view invisible first
				mBlurredView.setTag(null);

				ViewGroup parent = (ViewGroup) mBlurredView.getParent();
				if (parent != null) {
					// Remove blurred view
					parent.removeView(mBlurredView);
				}
			}
			mBlurredView = null;
		}
	}

	public interface BlurListener {
		void blurStarted();
		void blurFinished();
		void unblurStarted();
		void unblurFinished();
	}
}
