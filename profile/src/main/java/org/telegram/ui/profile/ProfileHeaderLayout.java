package org.telegram.ui.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.Components.CanvasButton;
import org.telegram.ui.Components.ProfileGalleryView;

public class ProfileHeaderLayout {
    private Context context;


    public FrameLayout avatarContainer;

    public FrameLayout innerAvatarContainer;

    private View transitionOnlineText;

    public SimpleTextView[] onlineTextView = new SimpleTextView[4];

    private float avatarAnimationProgress;

    private ImageReceiver fallbackImage;

    private TLRPC.UserFull userInfo;

    private boolean hasFallbackPhoto;

    private BaseFragment parentFragment;

    public float photoDescriptionProgress = -1;

    private float customAvatarProgress;

    private float customPhotoOffset;

    public ProfileGalleryView avatarsViewPager;



    public ProfileHeaderLayout(Context context, BaseFragment parentFragment) {
        this.parentFragment = parentFragment;
        this.context = context;


        fallbackImage = new ImageReceiver(avatarContainer);
        fallbackImage.setRoundRadius(AndroidUtilities.dp(11));

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
    }

    public void setUserInfo(TLRPC.UserFull user) {
        userInfo = user;
        updateData();
    }

    public void setAvatarAnimationProgress(float progress) {
        avatarAnimationProgress = progress;
        // TODO
    }

    public float getAvatarAnimationProgress() {
        return avatarAnimationProgress;
    }

    public void setTransitionOnlineText(View transitionOnlineText) {
        this.transitionOnlineText = transitionOnlineText;
    }

    private void updateCollectibleHint() {
        //TODO
    }

    public boolean hasFallbackPhoto() {
        return hasFallbackPhoto;
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


    public void setCustomAvatarProgress(float customAvatarProgress) {
        this.customAvatarProgress = customAvatarProgress;
    }

    public void setCustomPhotoOffset(float customPhotoOffset) {
        this.customPhotoOffset = customPhotoOffset;
    }
}
