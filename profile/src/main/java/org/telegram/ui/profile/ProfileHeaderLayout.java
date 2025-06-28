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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

import com.google.android.datatransport.runtime.firebase.transport.LogEventDropped;

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
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CanvasButton;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.Stars.ProfileGiftsView;
import org.telegram.ui.Stories.ProfileStoriesView;

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
        MessagesController.PeerColor peerColor = new MessagesController.PeerColor(); //TODO: applyPeerColor(statusColor, true, online)
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

}
