package closer.vlllage.com.closer.handler.group;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;

import static java.lang.Math.max;

public class GroupActionHandler extends PoolMember {

    private GroupActionAdapter groupActionAdapter;
    private RecyclerView actionRecyclerView;
    private MaxSizeFrameLayout container;
    private ValueAnimator animator;
    private int initialHeight;
    private DataSubscription groupActionsDisposable;

    public void attach(MaxSizeFrameLayout container, RecyclerView actionRecyclerView) {
        this.container = container;
        this.actionRecyclerView = actionRecyclerView;
        actionRecyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        groupActionAdapter = new GroupActionAdapter(this);

        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(group -> {
            if (groupActionsDisposable != null) {
                $(DisposableHandler.class).dispose(groupActionsDisposable);
            }

            groupActionsDisposable = $(StoreHandler.class).getStore().box(GroupAction.class).query()
                    .equal(GroupAction_.group, group.getId())
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer(groupActions -> {
                        boolean wasEmpty = groupActionAdapter.getItemCount() == 0;
                        groupActionAdapter.setActions(groupActions);
                        boolean isEmpty = groupActionAdapter.getItemCount() == 0;
                        if (isEmpty != wasEmpty) {
                            show(!isEmpty);
                        }
                    });

            $(DisposableHandler.class).add(groupActionsDisposable);
        }));

        actionRecyclerView.setAdapter(groupActionAdapter);
    }

    public void show(boolean show) {
        if (container == null) {
            return;
        }

        if (groupActionAdapter != null && groupActionAdapter.getItemCount() == 0) {
            show = false;
        }

        if (animator != null) {
            animator.pause();
            animator.cancel();
        }

        if (show) {
            animator = ValueAnimator.ofInt(0, initialHeight);
            animator.setDuration(500);
            animator.setStartDelay(1700);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                container.setMaxHeight((int) animation.getAnimatedValue());
                container.setAlpha(animation.getAnimatedFraction());
            });
            animator.addListener(new Animator.AnimatorListener() {

                private boolean cancelled;

                @Override
                public void onAnimationStart(Animator animation) {
                    container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (cancelled) {
                        return;
                    }
                    container.setMaxHeight(MaxSizeFrameLayout.UNSPECIFIED);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    cancelled = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        } else if (container.getVisibility() != View.GONE) {
            initialHeight = max(initialHeight, container.getMeasuredHeight());
            animator = ValueAnimator.ofInt(container.getMeasuredHeight(), 0);
            animator.setDuration(195);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                container.setMaxHeight((int) animation.getAnimatedValue());
                container.setAlpha(1 - animation.getAnimatedFraction());
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    container.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    container.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animator.start();
        }
    }
}
