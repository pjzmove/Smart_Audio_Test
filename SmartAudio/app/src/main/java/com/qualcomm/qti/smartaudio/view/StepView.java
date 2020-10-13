/*
 * *************************************************************************************************
 * * © 2019 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.              *
 * *************************************************************************************************
 */

package com.qualcomm.qti.smartaudio.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.qualcomm.qti.smartaudio.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This view is used to represent a step status by displaying a progress, a check icon or a cross icon and a
 * message.</p>
 * <p>It inflates the layout {@link R.layout#frame_setup_step frame_setup_step}.</p>
 * <p>This view contains a text, an error text, an icon and a progressbar.</p>
 */
public class StepView extends FrameLayout {

    // ====== PRIVATE FIELDS ========================================================================

    /**
     * <p>The icon of the view: its value and visibility depends on the {@link StepState}.</p>
     */
    private ImageView mIcon;
    /**
     * <p>The status text.</p>
     */
    private TextView mText;
    /**
     * <p>The status text.</p>
     */
    private TextView mTextError;
    /**
     * <p>The progressbar of the view: its visibility depends on the {@link StepState}.</p>
     */
    private View mProgressBar;
    /**
     * The text view which displays a chronometer during the {@link StepState#PROGRESS} state.
     */
    private Chronometer mChronometer;


    // ====== ENUMS ========================================================================

    /**
     * <p>Depending on the type of status this view adjusts its look. This enumeration lists all the types.</p>
     */
    @IntDef({StepState.SUCCESS, StepState.ERROR, StepState.PROGRESS, StepState.WAIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface StepState {

        /**
         * <p>For a step of type SUCCESS, the view adjusts the following:
         * <ul>
         * <li>Icon: {@link R.drawable#ic_check_17dp ic_check_17dp}.</li>
         * <li>Icon colour: {@link R.color#setup_step_icon_tick setup_tick}.</li>
         * <li>Progress bar: hidden.</li>
         * <li>Text: black and normal.</li>
         * <li>Error text: hidden.</li>
         * </ul></p>
         */
        int SUCCESS = 0;
        /**
         * <p>For a step of type ERROR, the view adjusts the following:
         * <ul>
         * <li>Icon: {@link R.drawable#ic_cross_17dp ic_cross_17dp}.</li>
         * <li>Icon colour: {@link R.color#setup_step_icon_cross setup_cross}.</li>
         * <li>Progress bar: hidden.</li>
         * <li>Text: red and in bold.</li>
         * <li>Error text: visible.</li>
         * </ul></p>
         */
        int ERROR = 1;
        /**
         * <p>For a step of type PROGRESS type, the view adjusts the following:
         * <ul>
         * <li>Icon: hidden.</li>
         * <li>Progress bar: visible.</li>
         * <li>Text: black and in bold.</li>
         * <li>Error text: hidden.</li>
         * </ul></p>
         */
        int PROGRESS = 2;
        /**
         * <p>For a step of type PROGRESS type, the view adjusts the following:
         * <ul>
         * <li>Icon: hidden.</li>
         * <li>Progress bar: visible.</li>
         * <li>Text: grey and normal.</li>
         * <li>Error text: hidden.</li>
         * </ul></p>
         */
        int WAIT = 3;
    }


    // ====== CONSTRUCTORS ========================================================================

    /* All mandatory constructors when implementing a View */

    public StepView(@NonNull Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public StepView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public StepView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @SuppressWarnings("unused")
    public StepView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr,
                    @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    // ====== PUBLIC METHODS ========================================================================

    /**
     * <p>This method refreshes the content of this view with the given parameters.</p>
     *
     * @param type
     *         The type of status as one of {@link StepState}.
     */
    public void updateState(@StepState int type) {
        switch (type) {
            case StepState.ERROR:
                mProgressBar.setVisibility(GONE);
                mTextError.setVisibility(VISIBLE);
                mChronometer.setVisibility(VISIBLE);
                mChronometer.stop();
                setIcon(R.drawable.ic_cross_17dp, R.color.setup_step_icon_cross);
                setTextStyle(Typeface.BOLD, R.color.setup_error_message);
                return;

            case StepState.PROGRESS:
                mProgressBar.setVisibility(VISIBLE);
                mTextError.setVisibility(GONE);
                mChronometer.setVisibility(VISIBLE);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                setIcon(GONE);
                setTextStyle(Typeface.BOLD, R.color.custom_dialog_message);
                return;

            case StepState.SUCCESS:
                mProgressBar.setVisibility(GONE);
                mTextError.setVisibility(GONE);
                mChronometer.setVisibility(VISIBLE);
                mChronometer.stop();
                setIcon(R.drawable.ic_check_17dp, R.color.setup_step_icon_tick);
                setTextStyle(Typeface.NORMAL, R.color.custom_dialog_message);
                return;

            case StepState.WAIT:
                mProgressBar.setVisibility(INVISIBLE);
                mTextError.setVisibility(GONE);
                mChronometer.setVisibility(GONE);
                setIcon(GONE);
                setTextStyle(Typeface.NORMAL, R.color.setup_step_text_waiting);
                break;
        }
    }

    /**
     * <p>This method displays the given message within the UI as an error message and sets the state of the view to
     * {@link StepState#ERROR}.</p>
     *
     * @param message
     *         The message to display in the UI.
     */
    public void showErrorMessage(String message) {
        mTextError.setText(message);
        updateState(StepState.ERROR);
    }


    // ====== PRIVATE METHODS ========================================================================

    /**
     * <p>Inflates the layout used for the view and initialises all the view components.</p>
     *
     * @param context
     *         Context fo the application, used to inflate the layout.
     * @param attrs
     *         The attributes set up within the declaration of {@link StepView} in an xml file.
     * @param defStyleAttr
     *         An attribute in the current theme that contains a reference to a style resource that supplies defaults
     *         values for the TypedArray. Can be 0 to not look for defaults.
     * @param defStyleRes
     *         A resource identifier of a style resource that supplies default values for the TypedArray, used only
     *         if defStyleAttr is 0 or can not be found in the theme. Can be 0 to not look for defaults.
     */
    private void init(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        // inflate the layout
        inflate(getContext(), R.layout.frame_setup_step, this);

        // get views
        mIcon = findViewById(R.id.setup_step_image);
        mText = findViewById(R.id.setup_step_text);
        mTextError = findViewById(R.id.setup_step_text_error);
        mProgressBar = findViewById(R.id.setup_step_progress);
        mChronometer = findViewById(R.id.setup_step_chronometer);

        // init text
        if (attrs != null) {
            TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Step, defStyleAttr,
                                                                         defStyleRes);
            try {
                String text = array.getString(R.styleable.Step_stepText);
                mText.setText(text);
            }
            finally {
                array.recycle();
            }
        }

        // sets the view in its default state
        updateState(StepState.WAIT);
    }

    /**
     * <p>To set the message of the view in the specified style and with the given colour.</p>
     *
     * @param style
     *         The Typeface style as {@link Typeface#BOLD}, {@link Typeface#ITALIC} or {@link Typeface#NORMAL}.
     * @param colour
     *         The resource ID of the colour to use.
     */
    private void setTextStyle(int style, int colour) {
        mText.setTypeface(mText.getTypeface(), style);
        //noinspection deprecation
        mText.setTextColor(getResources().getColor(colour));
    }

    /**
     * <p>To show the icon of the view and set it with the given image and colour.</p>
     *
     * @param drawable
     *         The resource ID of the drawable to use.
     * @param colour
     *         The resource ID of the colour to use.
     */
    private void setIcon(int drawable, int colour) {
        setIcon(VISIBLE);
        mIcon.setImageDrawable(getContext().getDrawable(drawable));
        //noinspection deprecation
        mIcon.setColorFilter(getResources().getColor(colour), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    /**
     * <p>To show the icon of the view and set it with the given image and colour.</p>
     *
     * @param visibility
     *         the visibility to sets the icon with, as one of {@link #VISIBLE}, {@link #INVISIBLE} or
     *         {@link #GONE}.
     */
    private void setIcon(int visibility) {
        mIcon.setVisibility(visibility);
    }

}
