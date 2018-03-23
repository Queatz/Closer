package closer.vlllage.com.closer;

import android.animation.Animator;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewAnimationUtils;

import closer.vlllage.com.closer.pool.PoolActivity;

public class GroupActivity extends PoolActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_group);

        if (getIntent().getSourceBounds() != null) {
            getWindow().getDecorView().post(() -> {
                Rect sourceBounds = getIntent().getSourceBounds();
                Animator animator = ViewAnimationUtils.createCircularReveal(findViewById(R.id.background),
                        sourceBounds.centerX(),
                        sourceBounds.centerY(),
                        0,
                        (float) Math.hypot(getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight())
                );
                animator.setDuration(225);
                animator.start();
            });
        }
    }

    @Override
    public void finish() {
        Rect sourceBounds = getIntent().getSourceBounds();

        if (sourceBounds == null) {
            super.finish();
            return;
        }

        Animator animator = ViewAnimationUtils.createCircularReveal(findViewById(R.id.background),
                sourceBounds.centerX(),
                sourceBounds.centerY(),
                (float) Math.hypot(getWindow().getDecorView().getWidth(), getWindow().getDecorView().getHeight()),
                0
        );
        animator.setDuration(225);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                GroupActivity.super.finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator.start();
    }
}
