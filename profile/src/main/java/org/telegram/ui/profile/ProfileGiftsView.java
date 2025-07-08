package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.lerp;
import static org.telegram.ui.Stars.StarsController.findAttribute;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.dynamicanimation.animation.SpringAnimation;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_stars;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.ButtonBounce;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.ProfileActivity;
import org.telegram.ui.Stars.StarsController;

import java.util.ArrayList;
import java.util.HashSet;

public class ProfileGiftsView extends View implements NotificationCenter.NotificationCenterDelegate {

    private final int currentAccount;
    private final long dialogId;
    private final View avatarContainer;
    private final ProfileActivity.AvatarImageView avatarImage;
    private final Theme.ResourcesProvider resourcesProvider;

    public ProfileGiftsView(Context context, int currentAccount, long dialogId, @NonNull View avatarContainer, ProfileActivity.AvatarImageView avatarImage, Theme.ResourcesProvider resourcesProvider) {
        super(context);

        this.currentAccount = currentAccount;
        this.dialogId = dialogId;

        this.avatarContainer = avatarContainer;
        this.avatarImage = avatarImage;

        this.resourcesProvider = resourcesProvider;

    }

    private float expandProgress;

    public void setExpandProgress(float progress) {
        if (this.expandProgress != progress) {
            this.expandProgress = progress;
            invalidate();
        }
    }

    private float actionBarProgress;

    public void setActionBarActionMode(float progress) {
//        if (Theme.isCurrentThemeDark()) {
//            return;
//        }
        actionBarProgress = progress;
        invalidate();
    }

    private float left, right, cy;
    private float expandRight, expandY;
    private boolean expandRightPad;
    private final AnimatedFloat expandRightPadAnimated = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedFloat rightAnimated = new AnimatedFloat(this, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

    public void setBounds(float left, float right, float cy, boolean animated) {
        boolean changed = Math.abs(left - this.left) > 0.1f || Math.abs(right - this.right) > 0.1f || Math.abs(cy - this.cy) > 0.1f;
        this.left = left;
        this.right = right;
        if (!animated) {
            this.rightAnimated.set(this.right, true);
        }
        this.cy = cy;
        if (changed) {
            invalidate();
        }
    }

    public void setExpandCoords(float right, boolean rightPadded, float y) {
        this.expandRight = right;
        this.expandRightPad = rightPadded;
        this.expandY = y;
        invalidate();
    }

    private float progressToInsets = 1f;

    public void setProgressToStoriesInsets(float progressToInsets) {
        if (this.progressToInsets == progressToInsets) {
            return;
        }
        this.progressToInsets = progressToInsets;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.starUserGiftsLoaded);

        for (Gift gift : gifts) {
            gift.emojiDrawable.addView(this);
        }

        update();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.starUserGiftsLoaded);

        for (Gift gift : gifts) {
            gift.emojiDrawable.removeView(this);
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.starUserGiftsLoaded) {
            if ((long) args[0] == dialogId) {
                update();
            }
        }
    }

    public final class Gift {

        public final long id;
        public final TLRPC.Document document;
        public final long documentId;
        public final int color;
        public final String slug;

        public Gift(TL_stars.TL_starGiftUnique gift) {
            id = gift.id;
            document = gift.getDocument();
            documentId = document == null ? 0 : document.id;
            final TL_stars.starGiftAttributeBackdrop backdrop = findAttribute(gift.attributes, TL_stars.starGiftAttributeBackdrop.class);
            color = backdrop.center_color | 0xFF000000;
            slug = gift.slug;
        }

        public Gift(TLRPC.TL_emojiStatusCollectible status) {
            id = status.collectible_id;
            document = null;
            documentId = status.document_id;
            color = status.center_color | 0xFF000000;
            slug = status.slug;
        }

        public boolean equals(Gift b) {
            return b != null && b.id == id;
        }

        public RadialGradient gradient;
        public final Matrix gradientMatrix = new Matrix();
        public Paint gradientPaint;
        public AnimatedEmojiDrawable emojiDrawable;
        public AnimatedFloat animatedFloat;

        public final RectF bounds = new RectF();
        public final ButtonBounce bounce = new ButtonBounce(ProfileGiftsView.this);

        public float cx;
        public float cy;

        public void copy(Gift b) {
            gradient = b.gradient;
            emojiDrawable = b.emojiDrawable;
            gradientPaint = b.gradientPaint;
            animatedFloat = b.animatedFloat;
        }

        public void draw(
                Canvas canvas,
                float cx, float cy,
                float ascale, float rotate,
                float alpha,
                float gradientAlpha
        ) {
            this.cx = cx;
            this.cy = cy;

            if (alpha <= 0.0f) return;
            final float gsz = dp(45);
            bounds.set(cx - gsz / 2, cy - gsz / 2, cx + gsz / 2, cy + gsz / 2);
            canvas.save();
            canvas.translate(cx, cy);
            canvas.rotate(rotate);
            final float scale = ascale * bounce.getScale(0.1f);
            canvas.scale(scale, scale);
            if (gradientPaint != null) {
                gradientPaint.setAlpha((int) (0xFF * alpha * gradientAlpha));
                canvas.drawRect(-gsz / 2.0f, -gsz / 2.0f, gsz / 2.0f, gsz / 2.0f, gradientPaint);
            }
            if (emojiDrawable != null) {
                final int sz = dp(24);
                emojiDrawable.setBounds(-sz / 2, -sz / 2, sz / 2, sz / 2);
                emojiDrawable.setAlpha((int) (0xFF * alpha));
                emojiDrawable.draw(canvas);
            }
            canvas.restore();
        }
    }

    private StarsController.GiftsList list;

    public final ArrayList<Gift> oldGifts = new ArrayList<>();
    public final ArrayList<Gift> gifts = new ArrayList<>();
    public final HashSet<Long> giftIds = new HashSet<>();
    public int maxCount;

    public void update() {
        avY = avatarContainer.getY();
        if (!MessagesController.getInstance(currentAccount).enableGiftsInProfile) {
            return;
        }

        maxCount = MessagesController.getInstance(currentAccount).stargiftsPinnedToTopLimit;
        oldGifts.clear();
        oldGifts.addAll(gifts);
        gifts.clear();
        giftIds.clear();

        final TLRPC.EmojiStatus emojiStatus;
        if (dialogId >= 0) {
            final TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(dialogId);
            emojiStatus = user == null ? null : user.emoji_status;
        } else {
            final TLRPC.User chat = MessagesController.getInstance(currentAccount).getUser(-dialogId);
            emojiStatus = chat == null ? null : chat.emoji_status;
        }
        if (emojiStatus instanceof TLRPC.TL_emojiStatusCollectible) {
            giftIds.add(((TLRPC.TL_emojiStatusCollectible) emojiStatus).collectible_id);
        }
        list = StarsController.getInstance(currentAccount).getProfileGiftsList(dialogId);
        if (list != null) {
            for (int i = 0; i < list.gifts.size(); i++) {
                final TL_stars.SavedStarGift savedGift = list.gifts.get(i);
                if (!savedGift.unsaved && savedGift.pinned_to_top && savedGift.gift instanceof TL_stars.TL_starGiftUnique) {
                    final Gift gift = new Gift((TL_stars.TL_starGiftUnique) savedGift.gift);
                    if (!giftIds.contains(gift.id)) {
                        gifts.add(gift);
                        giftIds.add(gift.id);
                    }
                }
            }
        }

        boolean changed = false;
        if (gifts.size() != oldGifts.size()) {
            changed = true;
        } else for (int i = 0; i < gifts.size(); i++) {
            if (!gifts.get(i).equals(oldGifts.get(i))) {
                changed = true;
                break;
            }
        }

        for (int i = 0; i < gifts.size(); i++) {
            final Gift g = gifts.get(i);
            Gift oldGift = null;
            for (int j = 0; j < oldGifts.size(); ++j) {
                if (oldGifts.get(j).id == g.id) {
                    oldGift = oldGifts.get(j);
                    break;
                }
            }

            if (oldGift != null) {
                g.copy(oldGift);
            } else {
                g.gradient = new RadialGradient(0, 0, dp(22.5f), new int[]{g.color, Theme.multAlpha(g.color, 0.0f)}, new float[]{0, 1}, Shader.TileMode.CLAMP);
                g.gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                g.gradientPaint.setShader(g.gradient);
                if (g.document != null) {
                    g.emojiDrawable = AnimatedEmojiDrawable.make(currentAccount, AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, g.document);
                } else {
                    g.emojiDrawable = AnimatedEmojiDrawable.make(currentAccount, AnimatedEmojiDrawable.CACHE_TYPE_MESSAGES, g.documentId);
                }
                g.animatedFloat = new AnimatedFloat(this, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);
                g.animatedFloat.force(0.0f);
                if (isAttachedToWindow()) {
                    g.emojiDrawable.addView(this);
                }
            }
        }

        for (int i = 0; i < oldGifts.size(); i++) {
            final Gift g = oldGifts.get(i);
            Gift newGift = null;
            for (int j = 0; j < gifts.size(); ++j) {
                if (gifts.get(j).id == g.id) {
                    newGift = gifts.get(j);
                    break;
                }
            }
            if (newGift == null) {
                g.emojiDrawable.removeView(this);
                g.emojiDrawable = null;
                g.gradient = null;
            }
        }

        if (changed)
            invalidate();
    }

    public final AnimatedFloat animatedCount = new AnimatedFloat(this, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

    Interpolator interpolator = CubicBezierInterpolator.EASE_BOTH;

    private float animationProgress;

    private float avY = 0f;
    public Animator getAnimator(boolean reverse) {

        avY = avatarContainer.getY();

        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(0f, 1f);

        valueAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT);

        valueAnimator.addUpdateListener(animation -> {
            animationProgress = reverse ? 1f - animation.getAnimatedFraction() : animation.getAnimatedFraction();
            invalidate();
        });

        return valueAnimator;
    }

    private float calculateTimeFraction(float x, int index){
//        index = gifts.size() - index;
        float c = index < 3 ? (1 + index) : index * 1.5f;
        double exp = Math.pow(x, c);
        float y  = (float) ( (2*exp) / (exp + Math.pow (1 - x, c)) );
        Log.d("EXP", "x = " + x + "; y = " + y);
        return y;
    }

    Paint paint = new Paint();

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        if (gifts.isEmpty() || expandProgress >= 1.0f) return;

        final float ax = avatarContainer.getX();
        final float ay = 180; //TODO
        final float aw = (avatarContainer.getWidth()) * avatarContainer.getScaleX();
        final float ah = (avatarContainer.getHeight()) * avatarContainer.getScaleY();

        canvas.save();
        canvas.clipRect(0, 0, getWidth(), expandY);

        float expandY = this.expandY + dp(400);

        final float acx = ax + aw / 2.0f;
        final float cacx = Math.min(acx, dp(48));
        final float acy = ay + ah / 2.0f;
        final float ar = Math.min(aw, ah) / 2.0f + dp(6);
        final float cx = getWidth() / 2.0f;

        final float closedAlpha = Utilities.clamp01((float) (expandY - (AndroidUtilities.statusBarHeight + ActionBar.getCurrentActionBarHeight())) / dp(50));


        for (int i = 0; i < gifts.size(); ++i) {
            final Gift gift = gifts.get(i);
            final float alpha = gift.animatedFloat.set(1.0f);
            float scale = lerp(0.5f, 1.0f, alpha);
            final int index = i; // gifts.size() == maxCount ? i - 1 : i;

            float x;
            float y;

            float yDest = 0f;
            float scaleDest = 0.4f;

            float animationProgress = calculateTimeFraction(this.animationProgress, index);


            //TODO: animate gifts
//            if(!gift.emojiDrawable.getImageReceiver().isAnimationRunning()) {
//                gift.emojiDrawable.getImageReceiver().startAnimation(true);
//            }
//            gift.emojiDrawable.setTime(System.currentTimeMillis());
//            gift.emojiDrawable.update(t++);


            if (index == 0) {
                y = lerp(ay + dp(12), yDest, animationProgress * animationProgress);
                x = lerp(cx + dp(55), cx, animationProgress);
                scale = lerp(1f, .3f, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        -65 + 90,
                        alpha * (1.0f - 0), lerp(0.9f, 0.25f, actionBarProgress)
                );
            } else if (index == 1) {

                y = lerp(ay + dp(14), yDest, animationProgress * animationProgress);
                x = lerp(cx - dp(66), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        -4.0f,
                        alpha * alpha * (1.0f - 0) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 2) {

                y = lerp(ay + dp(24), yDest, animationProgress * animationProgress);
                x = lerp(cx + dp(87), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale, 8.0f,
                        alpha * (1.0f - 0) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 3) {

                y = lerp(ay + dp(69), yDest, animationProgress * animationProgress);
                x = lerp(cx - dp(55), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        3.0f,
                        alpha * (1.0f - 0) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 4) {

                y = lerp(ay + dp(54), yDest, animationProgress * animationProgress);
                x = lerp(cx + dp(74), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        -3.0f,
                        alpha * (1.0f - expandProgress) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 5) {
                y = lerp(ay + dp(56), yDest, animationProgress * animationProgress);
                x = lerp(cx - dp(100), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        2.0f,
                        alpha * (1.0f - expandProgress) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 6) {
                y = lerp(ay + ah + dp(59), yDest, animationProgress * animationProgress);
                x = lerp(cx + dp(69), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        0,
                        alpha * (1.0f - expandProgress) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 7) {
                y = lerp(ay + dp(76), yDest, animationProgress * animationProgress);
                x = lerp(cx - dp(53), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        -5.0f,
                        alpha * (1.0f - expandProgress) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            } else if (index == 8) {
                y = lerp(ay + dp(71), yDest, animationProgress * animationProgress);
                x = lerp(cx + dp(23), cx, animationProgress);
                scale = lerp(1f, scaleDest, animationProgress);

                gift.draw(
                        canvas,
                        x,
                        y,
                        scale,
                        5.0f,
                        alpha * (1.0f - expandProgress) * (1.0f - actionBarProgress) * (closedAlpha),
                        1.0f
                );
            }
        }

        canvas.restore();
    }

    public Gift getGiftUnder(float x, float y) {
        for (int i = 0; i < gifts.size(); ++i) {
            if (gifts.get(i).bounds.contains(x, y))
                return gifts.get(i);
        }
        return null;
    }

    private Gift pressedGift;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final Gift hit = getGiftUnder(event.getX(), event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressedGift = hit;
            if (pressedGift != null) {
                pressedGift.bounce.setPressed(true);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (pressedGift != hit && pressedGift != null) {
                pressedGift.bounce.setPressed(false);
                pressedGift = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pressedGift != null) {
                onGiftClick(pressedGift);
                pressedGift.bounce.setPressed(false);
                pressedGift = null;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedGift != null) {
                pressedGift.bounce.setPressed(false);
                pressedGift = null;
            }
        }
        return pressedGift != null;
    }

    public void onGiftClick(Gift gift) {
        Browser.openUrl(getContext(), "https://t.me/nft/" + gift.slug);
    }
}
