package org.telegram.ui.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ProfileActivity;

public class ProfileTopView extends FrameLayout {

    private ProfileActivity.AvatarImageView avatarImageView;

    private SimpleTextView[] nameTextView = new SimpleTextView[2];

    private int playProfileAnimation = 0;

    private Theme.ResourcesProvider resourcesProvider;

    private int userId; //TODO: Init

    private boolean callItemVisible = true; //TODO: Init

    private INavigationLayout parentLayout = null; //TODO: Init



    public ProfileTopView(@NonNull Context context) {
        super(context);

        avatarImageView = new ProfileActivity.AvatarImageView(context){
            //TODO: animatedEmojiDrawable
        };


        float rightMargin = (54 + ((callItemVisible && userId != 0) ? 54 : 0));
        boolean hasTitleExpanded = false;
        int initialTitleWidth = LayoutHelper.WRAP_CONTENT;
        if (parentLayout != null && parentLayout.getLastFragment() instanceof ChatActivity) {
            ChatAvatarContainer avatarContainer = ((ChatActivity) parentLayout.getLastFragment()).getAvatarContainer();
            if (avatarContainer != null) {
                hasTitleExpanded = avatarContainer.getTitleTextView().getPaddingRight() != 0;
                if (avatarContainer.getLayoutParams() != null && avatarContainer.getTitleTextView() != null) {
                    rightMargin =
                            (((ViewGroup.MarginLayoutParams) avatarContainer.getLayoutParams()).rightMargin +
                                    (avatarContainer.getWidth() - avatarContainer.getTitleTextView().getRight())) / AndroidUtilities.density;
//                initialTitleWidth = (int) (avatarContainer.getTitleTextView().getWidth() / AndroidUtilities.density);
                }
            }
        }

        for (int a = 0; a < nameTextView.length; a++) {
            if (playProfileAnimation == 0 && a == 0) {
                continue;
            }
            nameTextView[a] = new SimpleTextView(context) {

                @Override
                protected void onDraw(Canvas canvas) {
                    final int wasRightDrawableX = getRightDrawableX();
                    super.onDraw(canvas);
                    if (wasRightDrawableX != getRightDrawableX()) {
                      //TODO:  updateCollectibleHint();
                    }
                }
            };
            if (a == 1) {
                nameTextView[a].setTextColor(getThemedColor(Theme.key_profile_title));
            } else {
                nameTextView[a].setTextColor(getThemedColor(Theme.key_actionBarDefaultTitle));
            }
            nameTextView[a].setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(a == 0 ? 12 : 4));
            nameTextView[a].setTextSize(18);
            nameTextView[a].setGravity(Gravity.LEFT);
            nameTextView[a].setTypeface(AndroidUtilities.bold());
            nameTextView[a].setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
            nameTextView[a].setPivotX(0);
            nameTextView[a].setPivotY(0);
            nameTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1) {
                nameTextView[a].setScrollNonFitText(true);
                nameTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            nameTextView[a].setFocusable(a == 0);
            nameTextView[a].setEllipsizeByGradient(true);
            nameTextView[a].setRightDrawableOutside(a == 0);
            addView(nameTextView[a], LayoutHelper.createFrame(a == 0 ? initialTitleWidth : LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, -6, (a == 0 ? rightMargin - (hasTitleExpanded ? 10 : 0) : 0), 0));
        }

    }



    public int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }


}
