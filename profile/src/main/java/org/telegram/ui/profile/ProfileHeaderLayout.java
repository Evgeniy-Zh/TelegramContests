package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedColor;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFileDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.CanvasButton;
import org.telegram.ui.Components.ChatActivityInterface;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.ProfileGalleryView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.VectorAvatarThumbDrawable;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.PeerColorActivity;
import org.telegram.ui.Stars.StarGiftPatterns;
import org.telegram.ui.Stories.ProfileStoriesView;

import java.util.ArrayList;
import java.util.List;

public class ProfileHeaderLayout {

    private static final String TAG = "ProfileHeaderLayout";

    private Context context;


    public FrameLayout avatarContainer;

    public FrameLayout innerAvatarContainer;

    public org.telegram.ui.ProfileActivity.AvatarImageView avatarImage;

    public View messageButton;

    public ProfileStoriesView storyView;

    public ProfileGiftsView giftsView;

    private View transitionOnlineText;

    public AudioPlayerAlert.ClippingTextViewSwitcher mediaCounterTextView;

    public SimpleTextView[] onlineTextView = new SimpleTextView[4];
    public SimpleTextView[] nameTextView = new SimpleTextView[2];

    private RecyclerListView listView;

    public org.telegram.ui.profile.ProfileActivity.OverlaysView overlaysView; //TODO: make private
    public org.telegram.ui.profile.ProfileActivity.PagerIndicatorView avatarsViewPagerIndicatorView; //TODO: make private
    public TopView topView;


    private float avatarAnimationProgress;

    private ImageReceiver fallbackImage;

    private TLRPC.UserFull userInfo;

    private boolean hasFallbackPhoto;

    private BaseFragment currentFragment;

    public float photoDescriptionProgress = -1;

    private float customAvatarProgress;

    private float customPhotoOffset;

    public ProfileGalleryView avatarsViewPager;

    private int smallAvatarSize;
    private int smallAvatarRadius;

    public final float headerHeight;
    public final float expandedHeaderHeight;

    private ActionBar actionBar;

    public float avatarY;

    public float[] expandAnimatorValues = new float[]{0f, 1f};

    private float titleAnimationsYDiff;

    private float avatarScale;
    public float nameX;
    public float nameY;
    private float expandProgress;
    public float currentExpandAnimatorValue;
    public float currentExpanAnimatorFracture;

    public float extraHeight;
    public float listViewVelocityY;
    private boolean allowPullingDown;
    private boolean isPulledDown;

    public boolean openAnimationInProgress;

    MessagesController.PeerColor peerColor = new MessagesController.PeerColor(); //TODO: applyPeerColor(statusColor, true, online)

    public int playProfileAnimation;

    private FrameLayout textContainer;

    private ProfileButtonsView profileButtonsView;

    private Animator profileButtonAnimator;
    public AnimatorSet avatarCollapseAnimator;
    public ValueAnimator expandAnimator;

    public int actionBarBackgroundColor;

    public int searchTransitionOffset;

    public ChatActivityInterface previousTransitionFragment;
    public BaseFragment previousTransitionMainFragment;

    public float headerShadowAlpha = 1.0f;


    public boolean isInLandscapeMode;
    private float nameActionBarX;
    private float nameActionBarY;


    public ProfileHeaderLayout(BaseFragment currentFragment) {
        this.currentFragment = currentFragment;

        smallAvatarSize = 90;
        smallAvatarRadius = AndroidUtilities.dp(smallAvatarSize / 2f);

        headerHeight = 200f;
        expandedHeaderHeight = 450f;
        searchTransitionOffset = 0;

    }

    public void createView(Context context){
        this.context = context;

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        innerAvatarContainer = new FrameLayout(context) {
            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                super.dispatchDraw(canvas);

//                paint.setColor(Color.GREEN);
//                canvas.drawCircle(getPivotX(), getPivotY(), 12f, paint);

//                paint.setAlpha(99);
//                canvas.drawRect(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
            }
        };
        avatarCollapseAnimator = new AnimatorSet();

        avatarContainer = new FrameLayout(context) {

            CanvasButton canvasButton;

            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
//                paint.setColor(Color.RED);
//                canvas.drawLine(canvas.getWidth()/2f, 0f, canvas.getWidth() / 2f, canvas.getHeight(), paint);
//                 paint.setColor(Color.GREEN);
//                canvas.drawLine(0, extraHeight, canvas.getWidth(), extraHeight, paint);

//                paint.setColor(Color.CYAN);
//                canvas.drawLine(0, dp(headerHeight) + statusBarHeight + ActionBar.getCurrentActionBarHeight(), canvas.getWidth(), dp(headerHeight)  + statusBarHeight + ActionBar.getCurrentActionBarHeight(), paint);


                paint.setColor(Color.BLUE);
                canvas.drawLine(0, dp(expandedHeaderHeight), canvas.getWidth(), dp(expandedHeaderHeight), paint);


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

        profileButtonsView = new ProfileButtonsView(context);

        messageButton = profileButtonsView.messageButton;

        actionBar = currentFragment.getActionBar();

        topView = new ProfileHeaderLayout.TopView(context);

        final float diff = Math.min(1f, extraHeight / dp(headerHeight));

        avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff + actionBar.getTranslationY();

    }


    public void setUpView(){

        // Add Views

        avatarContainer.addView(giftsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarContainer.addView(innerAvatarContainer, LayoutHelper.createFrameMarginPx(smallAvatarSize, smallAvatarSize, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));

        avatarImage.setRoundRadius(smallAvatarRadius);
        innerAvatarContainer.addView(avatarImage, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        float pointX = dp(smallAvatarSize) / 2f;
        innerAvatarContainer.setPivotX(pointX);
        innerAvatarContainer.setPivotY(0);

        innerAvatarContainer.setTranslationY(avatarY);

        avatarContainer.addView(mediaCounterTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118.33f, -2, 8, 0));

        avatarContainer.addView(avatarsViewPager);
        avatarContainer.addView(overlaysView);
        avatarImage.setAvatarsViewPager(avatarsViewPager);

        textContainer = new FrameLayout(context);
        avatarContainer.addView(textContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarContainer.addView(storyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        avatarContainer.addView(profileButtonsView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 60, Gravity.CENTER_HORIZONTAL, 4, 20, 4, 0));

        setUpOnlineText();

        setUpNameText();

        avatarContainer.post(() -> {
            nameX = 0f;
            nameY = calculateBaseTextContainerPosition();
            textContainer.setTranslationY(nameY);
        });

        fallbackImage.setRoundRadius(dp(11));


        extraHeight = dp(headerHeight);


        ValueAnimator topViewAnimator = ValueAnimator.ofFloat(0f, 1f);

        topViewAnimator.setInterpolator(CubicBezierInterpolator.EASE_IN);

        topViewAnimator.addUpdateListener(animation -> {
            topView.setAnimationFracture(animation.getAnimatedFraction());
        });

        Animator giftsAnimator = giftsView.getAnimator(false);

        topViewAnimator.setStartDelay(200);
        giftsAnimator.setStartDelay(200);

        ValueAnimator storyAnimator = ValueAnimator.ofFloat(0f, 1f);
        storyAnimator.addUpdateListener(animation -> storyView.invalidate());
        AnimatorSet avSet = new AnimatorSet();
        avSet.playTogether(

                ObjectAnimator.ofFloat(innerAvatarContainer, View.SCALE_Y, 1f, 0.4f),
                ObjectAnimator.ofFloat(innerAvatarContainer, View.SCALE_X, 1f, 0.4f),
                ObjectAnimator.ofFloat(innerAvatarContainer, View.TRANSLATION_Y, avatarY, -140f),
                storyAnimator

        );


        avSet.setInterpolator(CubicBezierInterpolator.EASE_IN);

        avSet.setDuration(700);

        giftsAnimator.setStartDelay(100);
        giftsAnimator.setDuration(1200);

        topViewAnimator.setStartDelay(90);
        topViewAnimator.setDuration(800);

        AnimatorSet textAnimator = new AnimatorSet();
        ValueAnimator textY = ValueAnimator.ofFloat(1f,0f );
        ValueAnimator textX = ValueAnimator.ofFloat(nameX, 0f);

        textY.addUpdateListener(animation -> {
            float y  = lerp(calculateBaseTextContainerPosition(), AndroidUtilities.statusBarHeight, animation.getAnimatedFraction());
            textContainer.setTranslationY(y);
        });
        textX.addUpdateListener(animation -> {});

        textAnimator.playTogether(textX, textY);

        textAnimator.setStartDelay(100);
        textAnimator.setDuration(800);

        avatarCollapseAnimator.playTogether(
                giftsAnimator,
                topViewAnimator,
                avSet,
                textAnimator
        );


        avatarCollapseAnimator.addListener(new AnimatorListenerAdapter() { //TODO: remove listener
            @Override
            public void onAnimationEnd(Animator animation) {
                if(animation != avatarCollapseAnimator) return;
                animation.cancel();
                avatarContainer.post(() -> needLayout(true, false, dp(headerHeight)));
            }
        });



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ((AnimatorSet)animator).setCurrentPlayTime(0);
        }

        avatarContainer.invalidate();

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                Log.d(TAG, "isPulledDown = " + isPulledDown);

            }
        });

        profileButtonsView.muteButton.setOnClickListener(v -> {
            avatarCollapseAnimator.start();
        });

        profileButtonsView.callButton.setOnClickListener(v -> {
            avatarCollapseAnimator.reverse();
        });

        profileButtonsView.videoButton.setOnClickListener(v -> {
            listView.smoothScrollBy(0, (int) extraHeight, CubicBezierInterpolator.EASE_IN);
        });

    }
    
    private void setUpNameText() {
        for (int a = 0; a < nameTextView.length; a++) {
            // TODO:
//            if (playProfileAnimation == 0 && a == 0) {
//                continue;
//            }
            int finalA = a;
            nameTextView[a] = new SimpleTextView(context) {
                int index = finalA;
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

                @Override
                public boolean setText(CharSequence value) {
                    return super.setText(value);
                }
            };
            if (a == 1) {
                nameTextView[a].setTextColor(currentFragment.getThemedColor(Theme.key_profile_title));
            } else {
                nameTextView[a].setTextColor(currentFragment.getThemedColor(Theme.key_actionBarDefaultTitle));
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
            nameTextView[a].setWidthWrapContent(true);

            //TODO: white text on a white image???
//            nameTextView[a].getPaint().setShadowLayer(dpf2(0.66f), 0, 1, 0x72000000);

            textContainer.addView(nameTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));
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
            onlineTextView[a].setTextColor(  currentFragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue)); //TODO: applyPeerColor
            onlineTextView[a].setTextSize(14);
            onlineTextView[a].setGravity(Gravity.LEFT);
            onlineTextView[a].setAlpha(a == 0 ? 0.0f : 1.0f);
            if (a == 1 || a == 2 || a == 3) {
                onlineTextView[a].setPadding(dp(0), dp(2), dp(0), dp(2));
            }
            if (a > 0) {
                onlineTextView[a].setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
            onlineTextView[a].setWidthWrapContent(true);
            onlineTextView[a].setFocusable(a == 0);
            textContainer.addView(
                    onlineTextView[a],
                    LayoutHelper.createFrame(
                            LayoutHelper.WRAP_CONTENT,
                            LayoutHelper.WRAP_CONTENT,
                             Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                            0,
                            32,
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
        if(avatarContainer == null) return;
        if(userInfo == null) return;
        hasFallbackPhoto = false;
        if (userInfo.id == currentFragment.getUserConfig().getClientUserId()) {
            if (UserObject.hasFallbackPhoto(userInfo)) {
                hasFallbackPhoto = true;
                TLRPC.PhotoSize smallSize = FileLoader.getClosestPhotoSizeWithSize(userInfo.fallback_photo.sizes, 1000);
                if (smallSize != null) {
                    fallbackImage.setImage(ImageLocation.getForPhoto(smallSize, userInfo.fallback_photo), "50_50", (Drawable) null, 0, null, UserConfig.getInstance(currentFragment.getCurrentAccount()).getCurrentUser(), 0);
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
    
    public void setCustomAvatarProgress(float customAvatarProgress) {
        this.customAvatarProgress = customAvatarProgress;
    }

    public void setCustomPhotoOffset(float customPhotoOffset) {
        this.customPhotoOffset = customPhotoOffset;
    }

    private float calculateBaseTextContainerPosition(){
        return avatarY + dp(92) * avatarScale;
    }

    // transition between rounded avatar and rectangular avatar
    // transition between views' positions
    public void setAvatarExpandProgress(float animatedFracture) {
//        Log.d(TAG, "animationFracture = " + animatedFracture);
        final int newTop = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
        final float value = AndroidUtilities.lerp(expandAnimatorValues, currentExpanAnimatorFracture = animatedFracture);
        currentExpandAnimatorValue = value;

        // TODO: checkPhotoDescriptionAlpha();
//        innerAvatarContainer.setScaleX(avatarScale);
//        innerAvatarContainer.setScaleY(avatarScale);

        innerAvatarContainer.setTranslationY(AndroidUtilities.lerp(avatarY, 0, value)); //TODO: end value

//        avatarsViewPager.setTranslationY(innerAvatarContainer.getTranslationY());


        avatarImage.setRoundRadius((int) AndroidUtilities.lerp(smallAvatarRadius, 0f, value));
        if (storyView != null) {
            storyView.setExpandProgress(value);
        }
        if (giftsView != null) {
            giftsView.setExpandProgress(value);
        }

        //TODO: searchItem

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



        float y1 =  calculateBaseTextContainerPosition();
        float y = lerp(y1, y1 * 1.3f, expandProgress);

        nameY = lerp(y, extraHeight - profileButtonsView.getHeight(), value);

        textContainer.setTranslationY(nameY); //animate expand

        nameX = lerp(0f, -avatarContainer.getMeasuredWidth() / 2f + nameTextView[1].getWidth() / 2f + 32f, value);
        textContainer.setTranslationX(nameX);

        onlineTextView[1].setTranslationX(lerp(0, -(onlineTextView[1].getLeft() - nameTextView[1].getLeft()), value));

        //TODO:
//        mediaCounterTextView.setTranslationX(onlineTextViewX);
//        mediaCounterTextView.setTranslationY(onlineTextViewY);

        final Object onlineTextViewTag = onlineTextView[1].getTag();
        int statusColor;
        boolean online = false;
        if (onlineTextViewTag instanceof Integer) {
            statusColor = currentFragment.getThemedColor((Integer) onlineTextViewTag);
            online = (Integer) onlineTextViewTag == Theme.key_profile_status;
        } else {
            statusColor = currentFragment.getThemedColor(Theme.key_avatar_subtitleInProfileBlue);
        }
        int color = statusColor;
        onlineTextView[1].setTextColor(ColorUtils.blendARGB(color, 0xB3FFFFFF, value));

        //TODO:
//        if (showStatusButton != null) {
//            showStatusButton.setBackgroundColor(ColorUtils.blendARGB(Theme.multAlpha(Theme.adaptHSV(actionBarBackgroundColor, +0.18f, -0.1f), 0.5f), 0x23ffffff, currentExpandAnimatorValue));
//        }

//        needLayoutText(Math.min(1f, extraHeight / AndroidUtilities.dp(expandThreshold)));

        nameTextView[1].setTextColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : currentFragment.getThemedColor(Theme.key_profile_title), Color.WHITE, currentExpandAnimatorValue));
        actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : currentFragment.getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE, value), false);
        actionBar.setMenuOffsetSuppressed(true);

        avatarImage.setForegroundAlpha(value);

        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) innerAvatarContainer.getLayoutParams();
        params.width = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(smallAvatarSize), avatarContainer.getMeasuredWidth() / avatarScale, value);
        params.height = (int) AndroidUtilities.lerp(AndroidUtilities.dpf2(smallAvatarSize), (extraHeight + newTop) / avatarScale, value);

        innerAvatarContainer.setPivotX(params.width / 2f);
//        innerAvatarContainer.setPivotY(0);


        avatarContainer.requestLayout();

        innerAvatarContainer.requestLayout();

        updateCollectibleHint();
    }


    private boolean avatarCollapsed = false;

    public void needLayout(boolean animated, boolean openingAvatar, float initialAnimationExtraHeight) {

        final int topBarHeight = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();

        profileButtonsView.setTranslationY(topBarHeight + extraHeight - profileButtonsView.getHeight());

        if (innerAvatarContainer != null) {

            final float diff = Math.min(1f, extraHeight / dp(headerHeight));

//            avatarX = 0f;
//            avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff + actionBar.getTranslationY();


            listView.setTopGlowOffset((int) extraHeight);

            listView.setOverScrollMode(extraHeight > dp(headerHeight) && extraHeight < dp(expandedHeaderHeight) - topBarHeight ? View.OVER_SCROLL_NEVER : View.OVER_SCROLL_ALWAYS);

            if (profileButtonsView != null) {
                float searchTransitionOffset = 0f; //TODO: searchTransitionOffset

                // TODO: check visibility
                profileButtonsView.setVisibility(View.INVISIBLE); //TODO
                boolean showProfileButtons = profileButtonsView.getY() + profileButtonsView.getHeight() / 2f > topBarHeight * 2f; // TODO:  && !searchMode && (imageUpdater == null || setAvatarRow == -1);
                boolean hideProfileButtons = diff < 0.8f;
                //TODO in ProfileButtonsView
//                if (writeButtonVisible && chatId != 0) {
//                    writeButtonVisible = ChatObject.isChannel(currentChat) && !currentChat.megagroup && chatInfo != null && chatInfo.linked_chat_id != 0 && infoHeaderRow != -1;
//                }

                if (!openAnimationInProgress) {
                    if (profileButtonAnimator != null) {
                        Animator old = profileButtonAnimator;
                        profileButtonAnimator = null;
                        old.cancel();
                    }
                    if (showProfileButtons) {
                        profileButtonAnimator = profileButtonsView.getAnimator(1f);
                        profileButtonAnimator.setInterpolator(new DecelerateInterpolator());
                    } else {
                        profileButtonAnimator = profileButtonsView.getAnimator(0f);
                        profileButtonAnimator.setInterpolator(new DecelerateInterpolator());
                    }
                    profileButtonAnimator.setDuration(150);
                    profileButtonAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            profileButtonAnimator = null;
                        }
                    });

                    if (animated)
                        profileButtonAnimator.start();
                    else
                        profileButtonAnimator.end();

                }

                if (storyView != null) {
                    storyView.setExpandCoords(avatarContainer.getMeasuredWidth() - dp(40), false, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + extraHeight + searchTransitionOffset);
                }
                if (giftsView != null) {
                    giftsView.setExpandCoords(avatarContainer.getMeasuredWidth() - dp(40), false, (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() + dp(headerHeight) + searchTransitionOffset);
                }
            }


            float h = openAnimationInProgress ? initialAnimationExtraHeight : extraHeight;
            expandProgress = Math.max(0f, Math.min(1f, (h - dp(headerHeight)) / (dp(expandedHeaderHeight) - topBarHeight - dp(headerHeight))));

            Log.d(TAG, "h = " + h);

            float collapseProgress = (dp(headerHeight) / h) - 1f;

            if(h <= dp(headerHeight)) {
                if(h < 400) {
                    if(!avatarCollapseAnimator.isRunning() && !avatarCollapsed) { // collapse
                        avatarCollapseAnimator.start();
                        avatarCollapsed = true;
                    }
                } else {
                    if(!avatarCollapseAnimator.isRunning() && avatarCollapsed) { // show
                        avatarCollapseAnimator.reverse();
                        avatarCollapsed = false;
                    }
                }
            }

            Log.d(TAG, "avatarCollapsed = " + avatarCollapsed);

            if (h > dp(headerHeight) || isPulledDown) {                                   //     ✅  when pulling up or down
                avatarScale = lerp(1f, 1.2f, Math.min(1f, expandProgress * 3f));

                float y =  calculateBaseTextContainerPosition();
                nameY = lerp(y, y * 1.3f, expandProgress);


                if (storyView != null) {
                    storyView.invalidate();
                }
                if (giftsView != null) {
                    giftsView.invalidate();
                }

                final float durationFactor = Math.min(AndroidUtilities.dpf2(2000f), Math.max(AndroidUtilities.dpf2(1100f), Math.abs(listViewVelocityY))) / AndroidUtilities.dpf2(1100f);

                if (isAllowedPullingDown() && (openingAvatar || expandProgress >= 0.33f)) { //    ✅ while scrolling expanded header
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
                    params.height = (int) (h + topBarHeight);
                    avatarsViewPager.requestLayout();
                    if (!expandAnimator.isRunning()) {
                        nameY = extraHeight - profileButtonsView.getHeight();
                        textContainer.setTranslationY(nameY);         //pulling expanded

                        mediaCounterTextView.setTranslationX(textContainer.getTranslationX());
                        mediaCounterTextView.setTranslationY(textContainer.getTranslationY());
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

                        expandAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                expandAnimator.removeListener(this);
                            }
                        });

                        topView.setBackgroundColor(currentFragment.getThemedColor(Theme.key_avatar_backgroundActionBarBlue));

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

                    textContainer.setTranslationY(nameY); // pulling collapsed
                    if (expandAnimator == null || !expandAnimator.isRunning()) {
                        //TODO:
//                        mediaCounterTextView.setTranslationX(onlineX);
//                        mediaCounterTextView.setTranslationY(onlineY);
                        updateCollectibleHint();
                    }
                }
            }

            if (openAnimationInProgress && playProfileAnimation == 2) {     //      ✅   when animator is running

                nameTextView[0].setScaleX(1.0f);
                nameTextView[0].setScaleY(1.0f);

                avatarScale = lerp(1.0f, (smallAvatarSize + smallAvatarSize + 18f) / smallAvatarSize, getAvatarAnimationProgress());
                if (storyView != null) {
                    storyView.setExpandProgress(1f);
                }
                if (giftsView != null) {
                    giftsView.setExpandProgress(1f);
                }

                avatarImage.setRoundRadius((int) lerp(smallAvatarRadius, 0f, getAvatarAnimationProgress()));
//                innerAvatarContainer.setTranslationY(lerp((float) Math.ceil(avatarY), 0f, getAvatarAnimationProgress()));
                float extra = (innerAvatarContainer.getMeasuredWidth() - dp(42)) * avatarScale;

                //TODO
//                timeItem.setTranslationX(innerAvatarContainer.getX() + dp(16) + extra);
//                timeItem.setTranslationY(innerAvatarContainer.getY() + dp(15) + extra);
//                starBgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                starBgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
//                starFgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                starFgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
//                innerAvatarContainer.setScaleX(avatarScale);
//                innerAvatarContainer.setScaleY(avatarScale);

                overlaysView.setAlphaValue(getAvatarAnimationProgress(), false);
                actionBar.setItemsColor(ColorUtils.blendARGB(peerColor != null ? Color.WHITE : currentFragment.getThemedColor(Theme.key_actionBarDefaultIcon), Color.WHITE,getAvatarAnimationProgress()), false);

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

                innerAvatarContainer.requestLayout();

                updateCollectibleHint();
            } else if (extraHeight <= dp(headerHeight)) { // ✅while scrolling collapsed header
                avatarScale = 1f;
                if (storyView != null) {
                    storyView.invalidate();
                }
                if (giftsView != null) {
                    giftsView.invalidate();
                }
                float nameScale = 1.0f; //TODO
                if (expandAnimator == null || !expandAnimator.isRunning()) {
                    if(avatarCollapseAnimator == null || !avatarCollapseAnimator.isRunning()) {
//                        innerAvatarContainer.setScaleX(avatarScale);
//                        innerAvatarContainer.setScaleY(avatarScale);
//                        innerAvatarContainer.setTranslationY((float) Math.ceil(avatarY));
                    }
                    float extra = dp(42) * avatarScale - dp(42);
                    //TODO:
//                    timeItem.setTranslationX(innerAvatarContainer.getX() + dp(16) + extra);
//                    timeItem.setTranslationY(innerAvatarContainer.getY() + dp(15) + extra);
//                    starBgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                    starBgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
//                    starFgItem.setTranslationX(innerAvatarContainer.getX() + dp(28) + extra);
//                    starFgItem.setTranslationY(innerAvatarContainer.getY() + dp(24) + extra);
                }

                //TODO: showStatus button
//                if (showStatusButton != null) {
//                    showStatusButton.setAlpha((int) (0xFF * diff));
//                }

                for (int a = 0; a < nameTextView.length; a++) {
                    if (nameTextView[a] == null) {
                        continue;
                    }

                    nameTextView[a].setScaleX(nameScale);
                    nameTextView[a].setScaleY(nameScale);
                }
                updateCollectibleHint();
            }

            if (!openAnimationInProgress && (expandAnimator == null || !expandAnimator.isRunning())) {
                try {
//                    needLayoutText(diff);
                } catch (Exception e) {
                    //TODO: fix crash
                }
            }
        }

    }

    public void needLayoutText(float diff) {
        FrameLayout.LayoutParams layoutParams;
        float scale = nameTextView[1].getScaleX();
        float maxScale = extraHeight > AndroidUtilities.dp(headerHeight) ? 1.67f : 1.12f;

        if (extraHeight > AndroidUtilities.dp(headerHeight) && scale != maxScale) {
            return;
        }

        int viewWidth = AndroidUtilities.isTablet() ? AndroidUtilities.dp(490) : AndroidUtilities.displaySize.x;
        int extra = 0;

        float mediaHeaderAnimationProgress = 1f; //TODO: animation progress
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

    public boolean isAllowedPullingDown() {
        return allowPullingDown && !avatarCollapseAnimator.isRunning();
    }

    public void setAllowPullingDown(boolean allowPullingDown) {
        this.allowPullingDown = allowPullingDown;
    }

    public class TopView extends FrameLayout {

        private int currentColor;
        private Paint paint = new Paint();

//        private float animationFracture = 1f;

        public TopView(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec) + AndroidUtilities.dp(3));
        }

        @Override
        public void setBackgroundColor(int color) {
            if (color != currentColor) {
                currentColor = color;
                paint.setColor(color);
                invalidate();
                if (!hasColorById) {
                    actionBarBackgroundColor = currentColor;
                }
            }
        }

        private boolean hasColorById;
        private final AnimatedFloat hasColorAnimated = new AnimatedFloat(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
        public int color1, color2;
        private final AnimatedColor color1Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
        private final AnimatedColor color2Animated = new AnimatedColor(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

        private int backgroundGradientColor1, backgroundGradientColor2, backgroundGradientHeight;
        private LinearGradient backgroundGradient;
        private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public void setBackgroundColorId(MessagesController.PeerColor peerColor, boolean animated) {
            if (peerColor != null) {
                hasColorById = true;
                color1 = peerColor.getBgColor1(Theme.isCurrentThemeDark());
                color2 = peerColor.getBgColor2(Theme.isCurrentThemeDark());
                actionBarBackgroundColor = ColorUtils.blendARGB(color1, color2, 0.25f);
                if (peerColor.patternColor != 0) {
                    emojiColor = peerColor.patternColor;
                } else {
                    emojiColor = PeerColorActivity.adaptProfileEmojiColor(color1);
                }
            } else {
                actionBarBackgroundColor = currentColor;
                hasColorById = false;
                if (AndroidUtilities.computePerceivedBrightness(currentFragment.getThemedColor(Theme.key_actionBarDefault)) > .8f) {
                    emojiColor = currentFragment.getThemedColor(Theme.key_windowBackgroundWhiteBlueText);
                } else if (AndroidUtilities.computePerceivedBrightness(currentFragment.getThemedColor(Theme.key_actionBarDefault)) < .2f) {
                    emojiColor = Theme.multAlpha(currentFragment.getThemedColor(Theme.key_actionBarDefaultTitle), .5f);
                } else {
                    emojiColor = PeerColorActivity.adaptProfileEmojiColor(currentFragment.getThemedColor(Theme.key_actionBarDefault));
                }
            }
            if (!animated) {
                color1Animated.set(color1, true);
                color2Animated.set(color2, true);
            }
            invalidate();
        }

        private int emojiColor;
        private final AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable emoji = new AnimatedEmojiDrawable.SwapAnimatedEmojiDrawable(this, false, dp(20), AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW_STATIC);

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            emoji.attach();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            emoji.detach();
        }

        public final AnimatedFloat emojiLoadedT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);
        public final AnimatedFloat emojiFullT = new AnimatedFloat(this, 0, 440, CubicBezierInterpolator.EASE_OUT_QUINT);

        private boolean hasEmoji;
        private boolean emojiIsCollectible;
        public void setBackgroundEmojiId(long emojiId, boolean isCollectible, boolean animated) {
            emoji.set(emojiId, animated);
            emoji.setColor(emojiColor);
            emojiIsCollectible = isCollectible;
            if (!animated) {
                emojiFullT.force(isCollectible);
            }
            hasEmoji = hasEmoji || emojiId != 0 && emojiId != -1;
            invalidate();
        }

        private boolean emojiLoaded;
        private boolean isEmojiLoaded() {
            if (emojiLoaded) {
                return true;
            }
            if (emoji != null && emoji.getDrawable() instanceof AnimatedEmojiDrawable) {
                AnimatedEmojiDrawable drawable = (AnimatedEmojiDrawable) emoji.getDrawable();
                if (drawable.getImageReceiver() != null && drawable.getImageReceiver().hasImageLoaded()) {
                    return emojiLoaded = true;
                }
            }
            return false;
        }

        public float getBottomPosition() {
            final int height = ActionBar.getCurrentActionBarHeight() + (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0);
            final float bottom = extraHeight + height + searchTransitionOffset;

            final float contentBottom = innerAvatarContainer.getTranslationY() + 600 * innerAvatarContainer.getScaleX(); //TODO: calculate content size

            return Math.max(bottom, contentBottom);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            final float bottom = getBottomPosition();

            float mediaHeaderAnimationProgress = 0f; //TODO: set progress
            int y1 = (int) (bottom * (1.0f - mediaHeaderAnimationProgress));

            if (y1 != 0) {
                if (previousTransitionFragment != null && previousTransitionFragment.getContentView() != null) {
                    blurBounds.set(0, 0, getMeasuredWidth(), y1);
                    if (previousTransitionFragment.getActionBar() != null && !previousTransitionFragment.getContentView().blurWasDrawn() && previousTransitionFragment.getActionBar().getBackground() == null) {
                        paint.setColor(Theme.getColor(Theme.key_actionBarDefault, previousTransitionFragment.getResourceProvider()));
                        canvas.drawRect(blurBounds, paint);
                    } else if (previousTransitionMainFragment != null && previousTransitionMainFragment instanceof DialogsActivity && previousTransitionMainFragment.getFragmentView() instanceof SizeNotifierFrameLayout) {
                        previousTransitionMainFragment.getActionBar().blurScrimPaint.setColor(Theme.getColor(Theme.key_actionBarDefault, previousTransitionMainFragment.getResourceProvider()));
                        ((SizeNotifierFrameLayout) previousTransitionMainFragment.getFragmentView()).drawBlurRect(canvas, getY(), blurBounds, previousTransitionMainFragment.getActionBar().blurScrimPaint, true);
                    } else {
                        previousTransitionFragment.getContentView().drawBlurRect(canvas, getY(), blurBounds, previousTransitionFragment.getActionBar().blurScrimPaint, true);
                    }
                }
                paint.setColor(currentColor);
                final int color1 = color1Animated.set(this.color1);
                final int color2 = color2Animated.set(this.color2);
                final int gradientHeight = AndroidUtilities.statusBarHeight + AndroidUtilities.dp(144);
                if (backgroundGradient == null || backgroundGradientColor1 != color1 || backgroundGradientColor2 != color2 || backgroundGradientHeight != gradientHeight) {
                    backgroundGradient = new LinearGradient(0, 0, 0, backgroundGradientHeight = gradientHeight, new int[] { backgroundGradientColor2 = color2, backgroundGradientColor1 = color1 }, new float[] { 0, 1 }, Shader.TileMode.CLAMP);
                    backgroundPaint.setShader(backgroundGradient);
                }
                final float progressToGradient = (playProfileAnimation == 0 ? 1f : getAvatarAnimationProgress()) * hasColorAnimated.set(hasColorById);
                if (progressToGradient < 1) {
                    canvas.drawRect(0, 0, getMeasuredWidth(), y1, paint);
                }
                if (progressToGradient > 0) {
                    backgroundPaint.setAlpha((int) (0xFF * progressToGradient));
                    canvas.drawRect(0, 0, getMeasuredWidth(), y1, backgroundPaint);
                }
                if (hasEmoji) {
                    final float loadedScale = emojiLoadedT.set(isEmojiLoaded());
                    if (loadedScale > 0) {
                        canvas.save();
                        canvas.clipRect(0, 0, getMeasuredWidth(), y1);
                        StarGiftPatterns.drawProfilePatternEllipse(
                                canvas,
                                emoji,
                                innerAvatarContainer.getX() + innerAvatarContainer.getMeasuredWidth() / 2f,
                                avatarY + innerAvatarContainer.getMeasuredHeight() / 2f,
                                dp(smallAvatarSize),
                                1f,
                                animationFracture

                        );
                        canvas.restore();
                    }
                }
                if (previousTransitionFragment != null) {
                    ActionBar actionBar = previousTransitionFragment.getActionBar();
                    ActionBarMenu menu = actionBar.menu;
                    if (actionBar != null && menu != null) {
                        int restoreCount = canvas.save();
                        canvas.translate(actionBar.getX() + menu.getX(), actionBar.getY() + menu.getY());
                        canvas.saveLayerAlpha(0, 0, menu.getMeasuredWidth(), menu.getMeasuredHeight(), (int) (255 * (1f - getAvatarAnimationProgress())), Canvas.ALL_SAVE_FLAG);
                        menu.draw(canvas);
                        canvas.restoreToCount(restoreCount);
                    }
                }
            }
            SizeNotifierFrameLayout contentView;
            contentView = ((SizeNotifierFrameLayout) currentFragment.fragmentView);

            if (y1 != bottom) {
                int color = currentFragment.getThemedColor(Theme.key_windowBackgroundWhite);
                paint.setColor(color);
                blurBounds.set(0, y1, getMeasuredWidth(), (int) bottom);
                contentView.drawBlurRect(canvas, getY(), blurBounds, paint, true);
            }
            if (currentFragment.getParentLayout() != null) {
                currentFragment.getParentLayout().drawHeaderShadow(canvas, (int) (headerShadowAlpha * 255), (int) bottom);
            }
        }
        private Rect blurBounds = new Rect();

        private float animationFracture = 0f;
        public void setAnimationFracture(float animatedFraction) {
            this.animationFracture = animatedFraction;
            invalidate();
        }
    }

}
