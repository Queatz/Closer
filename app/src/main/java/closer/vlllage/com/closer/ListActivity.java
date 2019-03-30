package closer.vlllage.com.closer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolActivity;

public abstract class ListActivity extends PoolActivity {

    protected View background;
    protected RecyclerView recyclerView;

    private TranslateAnimation finishAnimator;
    private Runnable finishCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        setContentView(R.layout.activity_share);

        background = findViewById(R.id.background);
        recyclerView = findViewById(R.id.shareRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        background.setOnTouchListener((view, motionEvent) -> {
            background.setOnTouchListener(null);
            finish();
            return true;
        });

        ViewTreeObserver viewTreeObserver = background.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                background.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                reveal();
            }

            private void reveal() {
                float yPercent = ($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.feedPeekHeight) * 2) / (float) recyclerView.getMeasuredHeight();
                TranslateAnimation animation = new TranslateAnimation(
                        Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, yPercent, Animation.RELATIVE_TO_PARENT, 0
                );
                animation.setDuration(225);
                animation.setInterpolator(new DecelerateInterpolator());
                recyclerView.startAnimation(animation);
            }
        });
    }

    protected boolean isDone() {
        return finishAnimator != null;
    }

    @Override
    public void finish() {
        if (finishAnimator != null) {
            return;
        }

        finishAnimator = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 1
        );;
        finishAnimator.setDuration(195);
        finishAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        finishAnimator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ListActivity.super.finish();
                getWindow().getDecorView().setVisibility(View.GONE);
                overridePendingTransition(0, 0);
                finishAnimator = null;

                if (finishCallback != null) {
                    finishCallback.run();
                    finishCallback = null;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recyclerView.startAnimation(finishAnimator);
    }

    public void finish(Runnable callback) {
        finishCallback = callback;
        finish();
    }
}
