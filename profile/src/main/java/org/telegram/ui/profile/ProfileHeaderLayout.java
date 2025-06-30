package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CanvasButton;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.ProfileGiftsView;
import org.telegram.ui.Stories.ProfileStoriesView;

import java.util.Arrays;

public class ProfileHeaderLayout {
    private Context context;


    public FrameLayout avatarContainer;

    public FrameLayout innerAvatarContainer;

    public org.telegram.ui.ProfileActivity.AvatarImageView avatarImage;

    public RLottieImageView writeButton;

    public ProfileStoriesView storyView;

    public ProfileGiftsView giftsView;

    private View transitionOnlineText;

    public AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;

    public SimpleTextView[] onlineTextView = new SimpleTextView[4];
    public SimpleTextView[] nameTextView = new SimpleTextView[2];

    private RecyclerListView listView;

    public org.telegram.ui.profile.ProfileActivity.OverlaysView overlaysView; //TODO: make private
    public org.telegram.ui.profile.ProfileActivity.PagerIndicatorView avatarsViewPagerIndicatorView; //TODO: make private
    public View topView;


    private AnimatorSet writeButtonAnimation;

    private float avatarAnimationProgress;

    private ImageReceiver fallbackImage;

    private TLRPC.UserFull userInfo;

    private boolean hasFallbackPhoto;

    private BaseFragment parentFragment;

    public float photoDescriptionProgress = -1;

    private float customAvatarProgress;

    private float customPhotoOffset;

    public ProfileGalleryView avatarsViewPager;

    private float smallAvatarRadius;

    private ActionBar actionBar;

    public float avatarX;
    public float avatarY;

    public float[] expandAnimatorValues = new float[]{0f, 1f};

    private float titleAnimationsYDiff;

    public float avatarScale;
    public float nameX;
    public float nameY;
    public float onlineX;
    public float onlineY;
    public float expandProgress;
    public float currentExpandAnimatorValue;
    public float currentExpanAnimatorFracture;

    public float extraHeight;
    public float listViewVelocityY;
    public boolean allowPullingDown;
    private boolean isPulledDown;

    public boolean openAnimationInProgress;

    MessagesController.PeerColor peerColor = new MessagesController.PeerColor(); //TODO: applyPeerColor(statusColor, true, online)

    public int playProfileAnimation;

    //TODO: Initialize
    public ValueAnimator expandAnimator;
    public boolean isInLandscapeMode;


    public ProfileHeaderLayout(Context context, BaseFragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = context;

        innerAvatarContainer = new FrameLayout(context);
        avatarContainer = new FrameLayout(context) {

            CanvasButton canvasButton;

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                if (transitionOnlineText != null) {
                    canvas.save();
                    canvas.translate(onlineTextView[0].getX(), onlineTextView[0].getY());
                    canvas.saveLayerAlpha(0, 0, transitionOnlineText.getMeasuredWidth(), transitionOnlineText.getMeasuredHeight(), (int) (255 * (1f - avatarAnimationProgress)), Canvas.ALL_SAVE_FLAG);
                    transitionOnlineText.draw(canvas);
                    canvas.restore();
                    canvas.restore();
                    invalidate();
                }
                if (hasFallbackPhoto && photoDescriptionProgress != 0 && customAvatarProgress != 1f) {
                    float cy = onlineTextView[1].getY() + onlineTextView[1].getMeasuredHeight() / 2f;
                    float size = AndroidUtilities.dp(22);
                    float x = AndroidUtilities.dp(28) - customPhotoOffset + onlineTextView[1].getX() - size;

                    fallbackImage.setImageCoords(x, cy - size / 2f, size, size);
                    fallbackImage.setAlpha(photoDescriptionProgress);
                    canvas.save();
                    float s = photoDescriptionProgress;
                    canvas.scale(s, s, fallbackImage.getCenterX(), fallbackImage.getCenterY());
                    fallbackImage.draw(canvas);
                    canvas.restore();

                    if (customAvatarProgress == 0) {
                        if (canvasButton == null) {
                            canvasButton = new CanvasButton(this);
                            canvasButton.setDelegate(() -> {
                                if (customAvatarProgress != 1f) {
                                    avatarsViewPager.scrollToLastItem();
                                }
                            });
                        }
                        AndroidUtilities.rectTmp.set(x - AndroidUtilities.dp(4), cy - AndroidUtilities.dp(14), x + onlineTextView[2].getTextWidth() + AndroidUtilities.dp(28) * (1f - customAvatarProgress) + AndroidUtilities.dp(4), cy + AndroidUtilities.dp(14));
                        canvasButton.setRect(AndroidUtilities.rectTmp);
                        canvasButton.setRounded(true);
                        canvasButton.setColor(Color.TRANSPARENT, ColorUtils.setAlphaComponent(Color.WHITE, 50));
                        canvasButton.draw(canvas);
                    } else {
                        if (canvasButton != null) {
                            canvasButton.cancelRipple();
                        }
                    }
                }

            }


            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return (canvasButton != null && canvasButton.checkTouchEvent(ev)) || super.onInterceptTouchEvent(ev);
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                return (canvasButton != null && canvasButton.checkTouchEvent(event)) || super.onTouchEvent(event);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                fallbackImage.onAttachedToWindow();
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                fallbackImage.onDetachedFromWindow();
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateCollectibleHint();
            }
        };

        // TODO: move back to ProfileActivity
        fallbackImage = new ImageReceiver(avatarContainer);
        writeButton = new RLottieImageView(context);

        actionBar = parentFragment.getActionBar();

        final float diff = Math.min(1f, extraHeight / dp(88f));

        avatarX = -AndroidUtilities.dpf2(47f) * diff;
        avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff + actionBar.getTranslationY();

    }

    public void setUpView(){

        avatarContainer.addView(writeButton, LayoutHelper.createFrame(60, 60, Gravity.RIGHT | Gravity.TOP, 0, 0, 16, 0));

        innerAvatarContainer.setPivotX(0);
        innerAvatarContainer.setPivotY(0);
        avatarContainer.addView(innerAvatarContainer, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        innerAvatarContainer.addView(avatarImage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        
        avatarContainer.addView(giftsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarContainer.addView(mediaCounterTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118.33f, -2, 8, 0));

        setUpOnlineText();

        setUpNameText();

        fallbackImage.setRoundRadius(AndroidUtilities.dp(11));

    }
    
    private void setUpNameText() {
        for (int a = 0; a < nameTextView.length; a++) {
            // TODO:
//            if (playProfileAnimation == 0 && a == 0) {
//                continue;
//            }
            nameTextView[a] = new SimpleTextView(context) {
                @Override
                public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                    super.onInitializeAccessibilityNodeInfo(info);
                    //TODO: setup accessibility
                }
                @Override
                protected void onDraw(Canvas canvas) {
                    final int wasRightDrawableX = getRightDrawableX();
                    super.onDraw(canvas);
                    if (wasRightDrawableX != getRightDrawableX()) {
                        updateCollectibleHint();
                    }
                }
            };
            if (a == 1) {
                nameTextView[a].setTextColor(parentFragment.getThemedColor(Theme.key_profile_title));
            } else {
                nameTextView[a].setTextColor(parentFragment.getThemedColor(Theme.key_actionBarDefaultTitle));
            }
            nameTextView[a].setPadding(0, dp(6), 0, dp(a == 0 ? 12 : 4));
            nameTextView[a].setTextSize(18);
            nameTextView[a].setGravity(Gravity.LEFT);
            nameTextView[a].setTypeface(AndroidUtilities.bold());
            nameTextView[a].setLeftDrawableTopPadding(-dp(1.3f));
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

            avatarContainer.addView(nameTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, -6, 0, 0));
        }

    }

    private void setUpOnlineText() {
        for (int a = 0; a < onlineTextView.length; a++) {
            if (a == 1) {
                onlineTextView[a] = new LinkSpanDrawable.ClickableSmallTextView(context) {

                    @Override
                    public void setAlpha(float alpha) {
                        super.setAlpha(alpha);
                       //TODO: checkPhotoDescriptionAlpha();
                    }

                    @Override
                    public void setTranslationY(float translationY) {
                        super.setTranslationY(translationY);
                        onlineTextView[2].setTranslationY(translationY);
                        onlineTextView[3].setTranslationY(translationY);
                    }

                    @Override
                    public void setTranslationX(float translationX) {
                        super.setTranslationX(translationX);
                        onlineTextView[2].setTranslationX(translationX);
                        onlineTextView[3].setTranslationX(translationX);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        if (onlineTextView[2] != null) {
                            onlineTextView[2].setTextColor(color);
                            onlineTextView[3].setTextColor(color);
                        }
                        //TODO:
//                        if (showStatusButton != null) {
//                            showStatusButton.setTextColor(Theme.multAlpha(Theme.adaptHSV(color, -.02f, +.15f), 1.4f));
//                        }
                    }
                };
            } else {
                onlineTextView[a] = new LinkSpanDrawable.ClickableSmallTextView(context);
            }

            onlineTextView[a].setEllipsizeByGradient(true);
            onlineTextView[a].setTextColor(  parentFragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue)); //TODO: applyPeerColor
            onlineTextView[a].setTextSize(14);
            onlineTextView[a].setGravity(Gravity.LEFT);
            onlineTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1 || a == 2 || a == 3) {
                onlineTextView[a].setPadding(dp(4), dp(2), dp(4), dp(2));
            }
            if (a > 0) {
                onlineTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            onlineTextView[a].setFocusable(a == 0);
            avatarContainer.addView(
                    onlineTextView[a],
                    LayoutHelper.createFrame(
                            LayoutHelper.WRAP_CONTENT,
                            LayoutHelper.WRAP_CONTENT,
                            Gravity.LEFT | Gravity.TOP,
                            118 - (a == 1 || a == 2 || a == 3? 4 : 0),
                            (a == 1 || a == 2 || a == 3 ? -2 : 0),
                            0,
                            0
                    )
            );
        }

    }
    
    public void setUserInfo(TLRPC.UserFull user) {
        userInfo = user;
        updateData();
    }

    public void setAvatarAnimationProgress(float progress) {
        avatarAnimationProgress = progress;
        Log.d("AvatarHeader", "setAvatarAnimationProgress = " + progress);
        // TODO
    }

    public float getAvatarAnimationProgress() {
        return avatarAnimationProgress;
    }

    public void setTransitionOnlineText(View transitionOnlineText) {
        this.transitionOnlineText = transitionOnlineText;
    }


    public boolean isPulledDown() {
        return isPulledDown;
    }


    public void updateData() {
        hasFallbackPhoto = false;
        if (userInfo.id == parentFragment. getUserConfig().getClientUserId()) {
            if (UserObject.hasFallbackPhoto(userInfo)) {
                hasFallbackPhoto = true;
                TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(userInfo.fallback_photo.sizes, 1000);
                if (smallSize != null) {
                    fallbackImage.setImage(ImageLocation.getForPhoto(smallSize, userInfo.fallback_photo), "50_50", (Drawable) null, 0, null, UserConfig.getInstance(parentFragment.getCurrentAccount()).getCurrentUser(), 0);
                }
            }
        }
    }
    
    public void requestLayout(boolean animated) {

    }

    private void updateCollectibleHint() {
        //TODO
    }

    public boolean hasFallbackPhoto() {
        return hasFallbackPhoto;
    }

    public void refreshNameAndOnlineXY() {
        nameX = AndroidUtilities.dp(-21f) + innerAvatarContainer.getMeasuredWidth() * (avatarScale - (42f + 18f) / 42f);
        nameY = (float) Math.floor(avatarY) + AndroidUtilities.dp(1.3f) + AndroidUtilities.dp(7f) + innerAvatarContainer.getMeasuredHeight() * (avatarScale - (42f + 18f) / 42f) / 2f;
        onlineX = AndroidUtilities.dp(-21f) + innerAvatarContainer.getMeasuredWidth() * (avatarScale - (42f + 18f) / 42f);
        onlineY = (float) Math.floor(avatarY) + AndroidUtilities.dp(24) + (float) Math.floor(11 * AndroidUtilities.density) + innerAvatarContainer.getMeasuredHeight() * (avatarScale - (42f + 18f) / 42f) / 2f;
    }
    
    public void setCustomAvatarProgress(float customAvatarProgress) {
        this.customAvatarProgress = customAvatarProgress;
    }

    public void setCustomPhotoOffset(float customPhotoOffset) {
        this.customPhotoOffset = customPhotoOffset;
    }

    // transition between rounded avatar and rectangular avatar
    // transition between views' positions
    public void setAvatarExpandProgress(float animatedFracture) {
        Log.d("AvatarHeader", "avatarScale = " + avatarScale);
        final int newTop = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        final float value = currentExpandAnimatorValue = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture = animatedFracture);
        // TODO: checkPhotoDescriptionAlpha();
        innerAvatarContainer.setScaleX(avatarScale);
        innerAvatarContainer.setScaleY(avatarScale);
        innerAvatarContainer.setTranslationX(AndroidUtilities.lerp(avatarX, 0f, value));
        innerAvatarContainer.setTranslationY(AndroidUtilities.lerp((float) Math.ceil(avatarY), 0f, value));
        avatarImage.setRoundRadius((int) AndroidUtilities.lerp(smallAvatarRadius, 0f, value));
        if (storyView != null) {
            storyView.setExpandProgress(value);
        }
        if (giftsView != null) {
            giftsView.setExpandProgress(value);
        }

        //TODO: searchItem

        if (extraHeight > AndroidUtilities.dp(88f) && expandProgress < 0.33f) {
            refreshNameAndOnlineXY();
        }

        //TODO: update animated drawables
//        if (scamDrawable != null) {
//            scamDrawable.setColor(ColorUtils.blendARGB(parentFragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue), Color.argb(179, 255, 255, 255), value));
//        }
//
//        if (lockIconDrawable != null) {
//            lockIconDrawable.setColorFilter(ColorUtils.blendARGB(parentFragment.getThemedColor(Theme.key_chat_lockIcon), Color.WHITE, value), PorterDuff.Mode.MULTIPLY);
//        }
//
//        if (verifiedCrossfadeDrawable[0] != null) {
//            verifiedCrossfadeDrawable[0].setProgress(value);
//        }
//        if (verifiedCrossfadeDrawable[1] != null) {
//            verifiedCrossfadeDrawable[1].setProgress(value);
//        }
//
//        if (premiumCrossfadeDrawable[0] != null) {
//            premiumCrossfadeDrawable[0].setProgress(value);
//        }
//        if (premiumCrossfadeDrawable[1] != null) {
//            premiumCrossfadeDrawable[1].setProgress(value);
//        }

        //TODO: updateEmojiStatusDrawableColor(value);

        final float k = AndroidUtilities.dpf2(8f);

        final float nameTextViewXEnd = AndroidUtilities.dpf2(18f) - nameTextView[1].getLeft();
        final float nameTextViewYEnd = newTop + extraHeight - AndroidUtilities.dpf2(38f) - nameTextView[1].getBottom();
        final float nameTextViewCx = k + nameX + (nameTextViewXEnd - nameX) / 2f;
        final float nameTextViewCy = k + nameY + (nameTextViewYEnd - nameY) / 2f;
        final float nameTextViewX = (1 - value) * (1 - value) * nameX + 2 * (1 - value) * value * nameTextViewCx + value * value * nameTextViewXEnd;
        final float nameTextViewY = (1 - value) * (1 - value) * nameY + 2 * (1 - value) * value * nameTextViewCy + value * value * nameTextViewYEnd;

        final float onlineTextViewXEnd = AndroidUtilities.dpf2(16f) - onlineTextView[1].getLeft();
        final float onlineTextViewYEnd = newTop + extraHeight - AndroidUtilities.dpf2(18f) - onlineTextView[1].getBottom();
        final float onlineTextViewCx = k + onlineX + (onlineTextViewXEnd - onlineX) / 2f;
        final float onlineTextViewCy = k + onlineY + (onlineTextViewYEnd - onlineY) / 2f;
        final float onlineTextViewX = (1 - value) * (1 - value) * onlineX + 2 * (1 - value) * value * onlineTextViewCx + value * value * onlineTextViewXEnd;
        final float onlineTextViewY = (1 - value) * (1 - value) * onlineY + 2 * (1 - value) * value * onlineTextViewCy + value * value * onlineTextViewYEnd;

        nameTextView[1].setTranslationX(nameTextViewX);
        nameTextView[1].setTranslationY(nameTextViewY);
        onlineTextView[1].setTranslationX(onlineTextViewX + customPhotoOffset);
        onlineTextView[1].setTranslationY(onlineTextViewY);
        mediaCounterTextView.setTranslationX(onlineTextViewX);
        mediaCounterTextView.setTranslationY(onlineTextViewY);
        final Object onlineTextViewTag = onlineTextView[1].getTag();
        int statusColor;
        boolean online = false;
        if (onlineTextViewTag instanceof Integer) {
            statusColor = parentFragment.getThemedColor((Integer) onlineTextViewTag);
            online = (Integer) onlineTextViewTag == Theme.key_profile_status;
        } else {
            statusColor = parentFragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
        }
        int color = statusColor;
        onlineTextView[1].setTextColor(ColorUtils.blendARGB(color, 0xB3FFFFFF, value));
        if (extraHeight > AndroidUtilities.dp(88f)) {
            nameTextView[1].setPivotY(AndroidUtilities.lerp(0, nameTextView[1].getMeasuredHeight(), value));
            nameTextView[1].setScaleX(AndroidUtilities.lerp(1.12f, 1.67f, value));
            nameTextView[1].setScaleY(AndroidUtilities.lerp(1.12f, 1.67f, value));
        }

//        if (showStatusButton != null) {
//            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
//        }

        needLayoutText(Math.min(1f, extraHeight / AndroidUtilities.dp(88f)));

//        nameTextView[1].setTextColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : parentFragment.getThemedColor(Theme.key_profile_title), Color.WHITE, currentExpandAnimatorValue));
//        actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : parentFragment.getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE, value), false);
        actionBar.setMenuOffsetSuppressed(true);

        avatarImage.setForegroundAlpha(value);

        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) innerAvatarContainer.getLayoutParams();
        params.width = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42f), avatarContainer.getMeasuredWidth() / avatarScale, value);
        params.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(42f), (extraHeight + newTop) / avatarScale, value);
        params.leftMargin = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(64f), 0f, value);
        innerAvatarContainer.requestLayout();

        updateCollectibleHint();
    }
    
    public void needLayout(boolean animated, int newTop, boolean openingAvatar, float initialAnimationExtraHeight){
        if (innerAvatarContainer != null) {
            final float diff = Math.min(1f, extraHeight / dp(88f));

            listView.setTopGlowOffset((int) extraHeight);

            listView.setOverScrollMode(extraHeight > dp(88f) && extraHeight < listView.getMeasuredWidth() - newTop ? View.OVER_SCROLL_NEVER : View.OVER_SCROLL_ALWAYS);

            if (writeButton != null) {
                float searchTransitionOffset = 0f; //TODO: searchTransitionOffset
                writeButton.setTranslationY((actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset - dp(29.5f));

                boolean writeButtonVisible = true;
                // TODO: check visibility
//                boolean writeButtonVisible = diff > 0.2f && !searchMode && (imageUpdater == null || setAvatarRow == -1);
//                if (writeButtonVisible && chatId != 0) {
//                    writeButtonVisible = ChatObject.isChannel(currentChat) && !currentChat.megagroup && chatInfo != null && chatInfo.linked_chat_id != 0 && infoHeaderRow != -1;
//                }
                if (!openAnimationInProgress) {
                    boolean currentVisible = writeButton.getTag() == null;
                    if (writeButtonVisible != currentVisible) {
                        if (writeButtonVisible) {
                            writeButton.setTag(null);
                        } else {
                            writeButton.setTag(0);
                        }
                        if (writeButtonAnimation != null) {
                            AnimatorSet old = writeButtonAnimation;
                            writeButtonAnimation = null;
                            old.cancel();
                        }
                        if (animated) {
                            writeButtonAnimation = new AnimatorSet();
                            if (writeButtonVisible) {
                                writeButtonAnimation.setInterpolator(new DecelerateInterpolator());
                                writeButtonAnimation.playTogether(
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_X, 1.0f),
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_Y, 1.0f),
                                        ObjectAnimator.ofFloat(writeButton, View.ALPHA, 1.0f)
                                );
                            } else {
                                writeButtonAnimation.setInterpolator(new AccelerateInterpolator());
                                writeButtonAnimation.playTogether(
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_X, 0.2f),
                                        ObjectAnimator.ofFloat(writeButton, View.SCALE_Y, 0.2f),
                                        ObjectAnimator.ofFloat(writeButton, View.ALPHA, 0.0f)
                                );
                            }
                            writeButtonAnimation.setDuration(150);
                            writeButtonAnimation.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (writeButtonAnimation != null && writeButtonAnimation.equals(animation)) {
                                        writeButtonAnimation = null;
                                    }
                                }
                            });
                            writeButtonAnimation.start();
                        } else {
                            writeButton.setScaleX(writeButtonVisible ? 1.0f : 0.2f);
                            writeButton.setScaleY(writeButtonVisible ? 1.0f : 0.2f);
                            writeButton.setAlpha(writeButtonVisible ? 1.0f : 0.0f);
                        }
                    }
                }

                if (storyView != null) {
                    storyView.setExpandCoords(avatarContainer.getMeasuredWidth() - dp(40), writeButtonVisible, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset);
                }
                if (giftsView != null) {
                    giftsView.setExpandCoords(avatarContainer.getMeasuredWidth() - dp(40), writeButtonVisible, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset);
                }
            }

            float h = openAnimationInProgress ? initialAnimationExtraHeight : extraHeight;
            if (h > dp(88f) || isPulledDown) {
                expandProgress = Math.max(0f, Math.min(1f, (h - dp(88f)) / (listView.getMeasuredWidth() - newTop - dp(88f))));
                avatarScale = lerp((42f + 18f) / 42f, (42f + 42f + 18f) / 42f, Math.min(1f, expandProgress * 3f));
                if (storyView != null) {
                    storyView.invalidate();
                }
                if (giftsView != null) {
                    giftsView.invalidate();
                }

                final float durationFactor = Math.min(AndroidUtilities.dpf2(2000f), Math.max(AndroidUtilities.dpf2(1100f), Math.abs(listViewVelocityY))) / AndroidUtilities.dpf2(1100f);

                if (allowPullingDown && (openingAvatar || expandProgress >= 0.33f)) {
                    if (!isPulledDown) {
                        //TODO: other item
//                        if (otherItem != null) {
//                            if (!getMessagesController().isChatNoForwards(currentChat)) {
//                                otherItem.showSubItem(gallery_menu_save);
//                            } else {
//                                otherItem.hideSubItem(gallery_menu_save);
//                            }
//                            if (imageUpdater != null) {
//                                otherItem.showSubItem(add_photo);
//                                otherItem.showSubItem(edit_avatar);
//                                otherItem.showSubItem(delete_avatar);
//                                otherItem.hideSubItem(set_as_main);
//                                otherItem.hideSubItem(logout);
//                            }
//                        }
                        //TODO: search item
//                        if (searchItem != null) {
//                            searchItem.setEnabled(false);
//                        }
                        isPulledDown = true;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                        overlaysView.setOverlaysVisible(true, durationFactor);
                        avatarsViewPagerIndicatorView.refreshVisibility(durationFactor);
                        avatarsViewPager.setCreateThumbFromParent(true);
                        avatarsViewPager.getAdapter().notifyDataSetChanged();
                        expandAnimator.cancel();;
                        float value = lerp(expandAnimatorValues, currentExpanAnimatorFracture);
                        expandAnimatorValues[0] = value;
                        expandAnimatorValues[1] = 1f;
                        if (storyView != null && !storyView.isEmpty()) {
                            expandAnimator.setInterpolator(new FastOutSlowInInterpolator());
                            expandAnimator.setDuration((long) ((1f - value) * 1.3f * 250f / durationFactor));
                        } else {
                            expandAnimator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
                            expandAnimator.setDuration((long) ((1f - value) * 250f / durationFactor));
                        }
                        expandAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                setForegroundImage(false);
                                avatarsViewPager.setAnimatedFileMaybe(avatarImage.getImageReceiver().getAnimation());
                                avatarsViewPager.resetCurrentItem();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                expandAnimator.removeListener(this);
                                topView.setBackgroundColor(Color.BLACK);
                                innerAvatarContainer.setVisibility(View.GONE);
                                avatarsViewPager.setVisibility(View.VISIBLE);
                            }
                        });
                        expandAnimator.start();
                    }
                    ViewGroup.LayoutParams params = avatarsViewPager.getLayoutParams();
                    params.width = listView.getMeasuredWidth();
                    params.height = (int) (h + newTop);
                    avatarsViewPager.requestLayout();
                    if (!expandAnimator.isRunning()) {
                        float additionalTranslationY = 0;
                        if (openAnimationInProgress && playProfileAnimation == 2) {
                            additionalTranslationY = -(1.0f -getAvatarAnimationProgress()) * dp(50);
                        }
                        onlineX = AndroidUtilities.dpf2(16f) - onlineTextView[1].getLeft();
                        nameTextView[1].setTranslationX(AndroidUtilities.dpf2(18f) - nameTextView[1].getLeft());
                        nameTextView[1].setTranslationY(newTop + h - AndroidUtilities.dpf2(38f) - nameTextView[1].getBottom() + additionalTranslationY);
                        onlineTextView[1].setTranslationX(onlineX + customPhotoOffset);
                        onlineTextView[1].setTranslationY(newTop + h - AndroidUtilities.dpf2(18f) - onlineTextView[1].getBottom() + additionalTranslationY);
                        mediaCounterTextView.setTranslationX(onlineTextView[1].getTranslationX());
                        mediaCounterTextView.setTranslationY(onlineTextView[1].getTranslationY());
                        updateCollectibleHint();
                    }
                } else {
                    if (isPulledDown) {
                        isPulledDown = false;
                        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors, true);
                        //TODO: other item
//                        if (otherItem != null) {
//                            otherItem.hideSubItem(gallery_menu_save);
//                            if (imageUpdater != null) {
//                                otherItem.hideSubItem(set_as_main);
//                                otherItem.hideSubItem(edit_avatar);
//                                otherItem.hideSubItem(delete_avatar);
//                                otherItem.showSubItem(add_photo);
//                                otherItem.showSubItem(logout);
//                            }
//                        }
                        //TODO: search item
//                        if (searchItem != null) {
//                            searchItem.setEnabled(!scrolling);
//                        }
                        overlaysView.setOverlaysVisible(false, durationFactor);
                        avatarsViewPagerIndicatorView.refreshVisibility(durationFactor);
                        expandAnimator.cancel();
                        avatarImage.getImageReceiver().setAllowStartAnimation(true);
                        avatarImage.getImageReceiver().startAnimation();

                        float value = lerp(expandAnimatorValues, currentExpanAnimatorFracture);
                        expandAnimatorValues[0] = value;
                        expandAnimatorValues[1] = 0f;
                        expandAnimator.setInterpolator(CubicBezierInterpolator.EASE_BOTH);
                        if (!isInLandscapeMode) {
                            expandAnimator.setDuration((long) (value * 250f / durationFactor));
                        } else {
                            expandAnimator.setDuration(0);
                        }
                        topView.setBackgroundColor(parentFragment.getThemedColor(Theme.key_avatar_backgroundActionBarBlue));

                        boolean doNotSetForeground = false; //TODO: check foreground
                        if (!doNotSetForeground) {
                            BackupImageView imageView = avatarsViewPager.getCurrentItemView();
                            if (imageView != null) {
                                if (imageView.getImageReceiver().getDrawable() instanceof VectorAvatarThumbDrawable) {
                                    avatarImage.drawForeground(false);
                                } else {
                                    avatarImage.drawForeground(true);
                                    avatarImage.setForegroundImageDrawable(imageView.getImageReceiver().getDrawableSafe());
                                }
                            }
                        }

                        avatarImage.setForegroundAlpha(1f);
                        innerAvatarContainer.setVisibility(View.VISIBLE);
                        avatarsViewPager.setVisibility(View.GONE);
                        expandAnimator.start();
                    }

                    innerAvatarContainer.setScaleX(avatarScale);
                    innerAvatarContainer.setScaleY(avatarScale);

                    if (expandAnimator == null || !expandAnimator.isRunning()) {
                        refreshNameAndOnlineXY();
                        nameTextView[1].setTranslationX(nameX);
                        nameTextView[1].setTranslationY(nameY);
                        onlineTextView[1].setTranslationX(onlineX + customPhotoOffset);
                        onlineTextView[1].setTranslationY(onlineY);
                        mediaCounterTextView.setTranslationX(onlineX);
                        mediaCounterTextView.setTranslationY(onlineY);
                        updateCollectibleHint();
                    }
                }
            }

            if (openAnimationInProgress && playProfileAnimation == 2) {
                float avX = 0;
                float avY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f - 21 * AndroidUtilities.density + actionBar.getTranslationY();

                nameTextView[0].setTranslationX(0);
                nameTextView[0].setTranslationY((float) Math.floor(avY) + dp(1.3f));
                onlineTextView[0].setTranslationX(0);
                onlineTextView[0].setTranslationY((float) Math.floor(avY) + dp(24));
                nameTextView[0].setScaleX(1.0f);
                nameTextView[0].setScaleY(1.0f);

                nameTextView[1].setPivotY(nameTextView[1].getMeasuredHeight());
                nameTextView[1].setScaleX(1.67f);
                nameTextView[1].setScaleY(1.67f);

                avatarScale = lerp(1.0f, (42f + 42f + 18f) / 42f, getAvatarAnimationProgress());
                if (storyView != null) {
                    storyView.setExpandProgress(1f);
                }
                if (giftsView != null) {
                    giftsView.setExpandProgress(1f);
                }

                avatarImage.setRoundRadius((int) lerp(smallAvatarRadius, 0f,getAvatarAnimationProgress()));
                innerAvatarContainer.setTranslationX(lerp(avX, 0,getAvatarAnimationProgress()));
                innerAvatarContainer.setTranslationY(lerp((float) Math.ceil(avY), 0f,getAvatarAnimationProgress()));
                float extra = (innerAvatarContainer.getMeasuredWidth() - dp(42)) * avatarScale;

                //TODO
//                timeItem.setTranslationX(innerAvatarContainer.getX() + dp(16) + extra);
//                timeItem.setTranslationY(innerAvatarContainer.getY() + dp(15) + extra);
//                starBgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                starBgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
//                starFgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                starFgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
                innerAvatarContainer.setScaleX(avatarScale);
                innerAvatarContainer.setScaleY(avatarScale);

                overlaysView.setAlphaValue(getAvatarAnimationProgress(), false);
                actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : parentFragment.getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE,getAvatarAnimationProgress()), false);

                //TODO: Update drawables
//                if (scamDrawable != null) {
//                    scamDrawable.setColor(ColorUtils.blendARGB(getThemedColor(Theme.key_avatar_subtitleInProfileBlue), Color.argb(179, 255, 255, 255),getAvatarAnimationProgress()));
//                }
//                if (lockIconDrawable != null) {
//                    lockIconDrawable.setColorFilter(ColorUtils.blendARGB(getThemedColor(Theme.key_chat_lockIcon), Color.WHITE,getAvatarAnimationProgress()), PorterDuff.Mode.MULTIPLY);
//                }
//                if (verifiedCrossfadeDrawable[1] != null) {
//                    verifiedCrossfadeDrawable[1].setProgress(getAvatarAnimationProgress());
//                    nameTextView[1].invalidate();
//                }
//                if (premiumCrossfadeDrawable[1] != null) {
//                    premiumCrossfadeDrawable[1].setProgress(getAvatarAnimationProgress());
//                    nameTextView[1].invalidate();
//                }

                //TODO: updateEmojiStatusDrawableColor(getAvatarAnimationProgress());

                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) innerAvatarContainer.getLayoutParams();
                params.width = params.height = (int) lerp(AndroidUtilities.dpf2(42f), (extraHeight + newTop) / avatarScale,getAvatarAnimationProgress());
                params.leftMargin = (int) lerp(AndroidUtilities.dpf2(64f), 0f,getAvatarAnimationProgress());
                innerAvatarContainer.requestLayout();

                updateCollectibleHint();
            } else if (extraHeight <= dp(88f)) {
                avatarScale = (42 + 18 * diff) / 42.0f;
                if (storyView != null) {
                    storyView.invalidate();
                }
                if (giftsView != null) {
                    giftsView.invalidate();
                }
                float nameScale = 1.0f + 0.12f * diff;
                if (expandAnimator == null || !expandAnimator.isRunning()) {
                    innerAvatarContainer.setScaleX(avatarScale);
                    innerAvatarContainer.setScaleY(avatarScale);
                    innerAvatarContainer.setTranslationX(avatarX);
                    innerAvatarContainer.setTranslationY((float) Math.ceil(avatarY));
                    float extra = dp(42) * avatarScale - dp(42);
                    //TODO:
//                    timeItem.setTranslationX(innerAvatarContainer.getX() + dp(16) + extra);
//                    timeItem.setTranslationY(innerAvatarContainer.getY() + dp(15) + extra);
//                    starBgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                    starBgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
//                    starFgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                    starFgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
                }
                nameX = -21 * AndroidUtilities.density * diff;
                nameY = (float) Math.floor(avatarY) + dp(1.3f) + dp(7) * diff + titleAnimationsYDiff * (1f -getAvatarAnimationProgress());
                onlineX = -21 * AndroidUtilities.density * diff;
                onlineY = (float) Math.floor(avatarY) + dp(24) + (float) Math.floor(11 * AndroidUtilities.density) * diff;
                //TODO: showStatus button
//                if (showStatusButton != null) {
//                    showStatusButton.setAlpha((int) (0xFF * diff));
//                }
                for (int a = 0; a < nameTextView.length; a++) {
                    if (nameTextView[a] == null) {
                        continue;
                    }
                    if (expandAnimator == null || !expandAnimator.isRunning()) {
                        nameTextView[a].setTranslationX(nameX);
                        nameTextView[a].setTranslationY(nameY);

                        onlineTextView[a].setTranslationX(onlineX + customPhotoOffset);
                        onlineTextView[a].setTranslationY(onlineY);
                        if (a == 1) {
                            mediaCounterTextView.setTranslationX(onlineX);
                            mediaCounterTextView.setTranslationY(onlineY);
                        }
                    }
                    nameTextView[a].setScaleX(nameScale);
                    nameTextView[a].setScaleY(nameScale);
                }
                updateCollectibleHint();
            }

            if (!openAnimationInProgress && (expandAnimator == null || !expandAnimator.isRunning())) {
                try {
                    needLayoutText(diff);
                } catch (Exception e) {
                    //TODO: fix crash
                }
            }
        }

    }

    public void needLayoutText(float diff) {
        FrameLayout.LayoutParams layoutParams;
        float scale = nameTextView[1].getScaleX();
        float maxScale = extraHeight > AndroidUtilities.dp(88f) ? 1.67f : 1.12f;

        if (extraHeight > AndroidUtilities.dp(88f) && scale != maxScale) {
            return;
        }

        int viewWidth = AndroidUtilities.isTablet() ? AndroidUtilities.dp(490) : AndroidUtilities.displaySize.x;
        int extra = 0;

        float mediaHeaderAnimationProgress = 0f; //TODO: animation progress
        int buttonsWidth = AndroidUtilities.dp(118 + 8 + (40 + extra * (1.0f - mediaHeaderAnimationProgress)));
        int minWidth = viewWidth - buttonsWidth;

        int width = (int) (viewWidth - buttonsWidth * Math.max(0.0f, 1.0f - (diff != 1.0f ? diff * 0.15f / (1.0f - diff) : 1.0f)) - nameTextView[1].getTranslationX());
        float width2 = nameTextView[1].getPaint().measureText(nameTextView[1].getText().toString()) * scale + nameTextView[1].getSideDrawablesSize();
        layoutParams = (FrameLayout.LayoutParams) nameTextView[1].getLayoutParams();
        int prevWidth = layoutParams.width;
        if (width < width2) {
            layoutParams.width = Math.max(minWidth, (int) Math.ceil((width - AndroidUtilities.dp(24)) / (scale + ((maxScale - scale) * 7.0f))));
        } else {
            layoutParams.width = (int) Math.ceil(width2);
        }
        layoutParams.width = (int) Math.min((viewWidth - nameTextView[1].getX()) / scale - AndroidUtilities.dp(8), layoutParams.width);
        if (layoutParams.width != prevWidth) {
            nameTextView[1].requestLayout();
        }

        width2 = onlineTextView[1].getPaint().measureText(onlineTextView[1].getText().toString()) + onlineTextView[1].getRightDrawableWidth();
        layoutParams = (FrameLayout.LayoutParams) onlineTextView[1].getLayoutParams();
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) mediaCounterTextView.getLayoutParams();
        prevWidth = layoutParams.width;
        layoutParams2.rightMargin = layoutParams.rightMargin = (int) Math.ceil(onlineTextView[1].getTranslationX() + AndroidUtilities.dp(8) + AndroidUtilities.dp(40) * (1.0f - diff));
        if (width < width2) {
            layoutParams2.width = layoutParams.width = (int) Math.ceil(width);
        } else {
            layoutParams2.width = layoutParams.width = LayoutHelper.WRAP_CONTENT;
        }
        if (prevWidth != layoutParams.width) {
            onlineTextView[2].getLayoutParams().width = layoutParams.width;
            onlineTextView[2].requestLayout();
            onlineTextView[3].getLayoutParams().width = layoutParams.width;
            onlineTextView[3].requestLayout();
            onlineTextView[1].requestLayout();
            mediaCounterTextView.requestLayout();
        }
    }



    public void setSmallAvatarRadius(float smallAvatarRadius) {
        this.smallAvatarRadius = smallAvatarRadius;
    }

    public void setListView(RecyclerListView listView) {
        this.listView = listView;
    }

    public void setForegroundImage(boolean secondParent) {
        Drawable drawable = avatarImage.getImageReceiver().getDrawable();
        if (drawable instanceof VectorAvatarThumbDrawable) {
            avatarImage.setForegroundImage(null, null, drawable);
        } else if (drawable instanceof AnimatedFileDrawable) {
            AnimatedFileDrawable fileDrawable = (AnimatedFileDrawable) drawable;
            avatarImage.setForegroundImage(null, null, fileDrawable);
            if (secondParent) {
                fileDrawable.addSecondParentView(avatarImage);
            }
        } else {
            ImageLocation location = avatarsViewPager.getImageLocation(0);
            String filter;
            if (location != null && location.imageType == FileLoader.IMAGE_TYPE_ANIMATION) {
                filter = "avatar";
            } else {
                filter = null;
            }
            avatarImage.setForegroundImage(location, filter, drawable);
        }
    }

}
