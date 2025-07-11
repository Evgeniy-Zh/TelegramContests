package org.telegram.ui.profile;


import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.List;

public class ProfileButtonsView extends LinearLayout {

    public static int statrId = 43042;
    public static final int MESSAGE_BUTTON = statrId++;
    public static final int MUTE_BUTTON = statrId++;
    public static final int UNMUTE_BUTTON = statrId++;
    public static final int CALL_BUTTON = statrId++;
    public static final int VIDEO_BUTTON = statrId++;
    public static final int GIFT_BUTTON = statrId++;
    private final int verticalMargin;
    private OnClickListener buttonsListener;

    private List<ProfileButton> buttons;



    public ProfileButtonsView(Context context) {
        super(context);

        buttons = new ArrayList<>();
//        buttons.add(new ProfileButton(context, R.drawable.profile_unmute, R.string.Unmute));
//        buttons.add(new ProfileButton(context, R.drawable.profile_call, R.string.Call));
//        buttons.add(new ProfileButton(context, R.drawable.profile_video_call, R.string.VideoCall));
//        buttons.add(new ProfileButton(context, R.drawable.profile_gift, R.string.Gift2TitleProfile));

        verticalMargin = dp(3);

        ProfileButton messageButton = new ProfileButton(context, R.drawable.profile_message, "Message"); //TODO: str id
        messageButton.setId(MESSAGE_BUTTON);
        ProfileButton muteButton = new ProfileButton(context, R.drawable.profile_mute, "Mute"); // TODO: str id
        muteButton.setId(MUTE_BUTTON);

        LayoutParams layoutParams = LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f);
        layoutParams.setMargins(verticalMargin, 0, verticalMargin, 0);

        addView(messageButton, layoutParams);
        addView(muteButton, layoutParams);
    }

    public ProfileButton get(int id) {
        return findViewById(id);
    }

    public ProfileButton addButton(int id, int iconRes, int stringRes) {
        if(get(id) != null) return null;
        ProfileButton button = new ProfileButton(getContext(), iconRes, stringRes);
        button.setId(id);

        LayoutParams layoutParams = LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f);
        layoutParams.setMargins(verticalMargin, 0, verticalMargin, 0);

        addView(button, layoutParams);
        button.setOnClickListener(buttonsListener);
        return button;
    }

    public ProfileButton addButton(int id, int iconRes, String string) {
        if(get(id) != null) return null;
        ProfileButton button = new ProfileButton(getContext(), iconRes, string);
        button.setId(id);

        LayoutParams layoutParams = LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1f);
        layoutParams.setMargins(verticalMargin, 0, verticalMargin, 0);

        addView(button, layoutParams);
        button.setOnClickListener(buttonsListener);
        return button;
    }

    public void setButtonsCLickListener(OnClickListener listener){
        buttonsListener = listener;
    }

    public Animator getAnimator(float value){
        ArrayList<Animator> list = new ArrayList<>();
//
//        list.add(ObjectAnimator.ofFloat(this, View.SCALE_Y, value));
//        list.add(ObjectAnimator.ofFloat(this, View.ALPHA, value));
//
//        forEachViews(this, view -> {
//            if(view instanceof ProfileButton)
//                list.add(((ProfileButton) view).getAnimator(value));
//        });

        AnimatorSet set = new AnimatorSet();

        set.playTogether(
                ObjectAnimator.ofFloat(this, View.SCALE_Y, value),
                ObjectAnimator.ofFloat(this, View.ALPHA, value)

                //TODO: add buttons
        );
        return set;
    }


    @SuppressLint("ViewConstructor")
    public static class ProfileButton extends LinearLayout {

        private Animator animator;
        ImageView iconView;
        TextView textView;

        public ProfileButton(Context context, int iconRes, int stringRes) {
            this(context, iconRes, LocaleController.getString(stringRes));
        }

        public ProfileButton(Context context, int iconRes, String string) {
            super(context);


            int contentColor = Color.WHITE; // TODO: get theme color

            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER);
            float horizontalPadding = 0f;
            float verticalPadding = 0f;
            setPadding(dp(horizontalPadding), dp(verticalPadding), dp(horizontalPadding), dp(verticalPadding));

            iconView = new ImageView(context);
            iconView.setImageResource(iconRes);

            textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);

            textView.setTextColor(contentColor);
            textView.setMaxLines(1);
            textView.setText(string);

            addView(iconView, LayoutHelper.createLinear(30, 30));
            addView(textView);

            setBackground(Theme.AdaptiveRipple.filledRect(Color.parseColor("#44313131"), 8));


        }

        public Animator getAnimator(float value) {
            AnimatorSet set = new AnimatorSet();
            animator = set;
            set.playTogether(
                    ObjectAnimator.ofFloat(iconView, View.SCALE_X, value),
                    ObjectAnimator.ofFloat(textView, View.SCALE_X, value)
            );

            return set;
        }

        private int getThemedColor(int key) {
            //TODO: maybe get active theme
            return Theme.getColor(key);
        }
    }

    private int getThemedColor(int key) {
        //TODO: maybe get active theme
        return Theme.getColor(key);
    }

}
