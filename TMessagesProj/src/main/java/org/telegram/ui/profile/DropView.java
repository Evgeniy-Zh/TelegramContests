package org.telegram.ui.profile;

import static org.telegram.messenger.AndroidUtilities.lerp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.PathParser;

public class DropView extends View {

    private Paint paint;

    private Interpolator interpolator;

    private Path path;

    public final AnimatorSet animator;

    private float yTranslation;

    private float scaleFactor;

    public DropView(Context context) {
        super(context);
        paint = new Paint();
        path = new Path();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        ValueAnimator valueAnimatorIn = ObjectAnimator.ofFloat(0f, 1f);
        valueAnimatorIn.setDuration(300);
        valueAnimatorIn.addUpdateListener(animation -> animatePathIn(animation.getAnimatedFraction()));

        ValueAnimator valueAnimatorOut = ObjectAnimator.ofFloat(0f, 1f);
        valueAnimatorOut.setDuration(300);
        valueAnimatorOut.addUpdateListener(animation -> animatePathOut(animation.getAnimatedFraction()));


        animator = new AnimatorSet();

        animator.playSequentially(
                valueAnimatorIn,
                valueAnimatorOut
        );

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(@NonNull Animator animation, boolean isReverse) {
                path = new Path();
                if (isReverse) animation.cancel();
            }

        });

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        scaleFactor = Math.min(getWidth(), getHeight()) / 240f;

        canvas.save();

        //TODO scale to dp
        canvas.scale(scaleFactor, scaleFactor, getWidth() / 2f, 0);
        canvas.translate(getWidth() / 2f, yTranslation);

        if (animator.isRunning())
            canvas.drawPath(path, paint);

        canvas.restore();

    }

    // from "M -120 0 Q -103 -2 -90 7 A 10 10 90 0 0 91 6 Q 97 -3 120 0"
    // to ""M -120 0 Q -91 14 -100 70 A 10 10 90 0 0 100 70 Q 91 14 120 0

    private PathParser.PathDataNode[] start = PathParser.createNodesFromPathData("M -120 0 Q -75 1 -10 5 A 10 10 90 0 0 5 5 Q 70 1 120 0");
    private PathParser.PathDataNode[] end = PathParser.createNodesFromPathData("M -120 0 Q -91 14 -100 70 A 10 10 90 0 0 100 70 Q 91 14 120 0");
    private PathParser.PathDataNode[] result = PathParser.createNodesFromPathData("M -120 0 Q -75 1 -10 5 A 10 10 90 0 0 5 5 Q 70 1 120 0");

    private void animatePathIn(float fraction) {
        PathParser.interpolatePathDataNodes(result, start, end, fraction);
        PathParser.PathDataNode.nodesToPath(result, path);
        invalidate();
    }

    private void animatePathOut(float fraction) {
        yTranslation = lerp(0f, -200f, fraction);
        invalidate();
    }
}
