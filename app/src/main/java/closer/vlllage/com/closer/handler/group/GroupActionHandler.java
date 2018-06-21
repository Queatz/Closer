package closer.vlllage.com.closer.handler.group;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
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
                            show(!isEmpty, true);
                        }
                    });

            $(DisposableHandler.class).add(groupActionsDisposable);
        }));

        actionRecyclerView.setAdapter(groupActionAdapter);
    }

    public void show(boolean show) {
        show(show, false);
    }

    private void show(boolean show, boolean immediate) {
        if (container == null) {
            return;
        }

        if (groupActionAdapter != null && groupActionAdapter.getItemCount() == 0) {
            show = false;
        }

        if (animator != null) {
            animator.cancel();
        }

        if (show) {
            animator = ValueAnimator.ofInt(0, initialHeight == 0 ? (int) ($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.groupActionHeight) * 1.5f) : initialHeight);
            animator.setDuration(500);
            animator.setStartDelay(immediate ? 0 : 1700);
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

    public void addActionToGroup(Group group) {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_an_action))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.add_action))
                .setLayoutResId(R.layout.add_action_modal)
                .setOnAfterViewCreated((alertConfig, view) -> {
                    TextView name = view.findViewById(R.id.name);
                    TextView intent = view.findViewById(R.id.intent);
                    AddToGroupModalModel model = new AddToGroupModalModel();
                    alertConfig.setAlertResult(model);

                    name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            model.name = name.getText().toString();
                        }
                    });
                    intent.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            model.intent = intent.getText().toString();
                        }
                    });
                })
                .setPositiveButtonCallback(alertResult -> {
                    AddToGroupModalModel model = ((AddToGroupModalModel) alertResult);

                    if ($(Val.class).isEmpty(model.name) || $(Val.class).isEmpty(model.name)) {
                        $(DefaultAlerts.class).message(R.string.enter_a_name_and_intent);
                        return;
                    }

                    createGroupAction(group, model.name, model.intent);
                })
                .show();
    }

    private void createGroupAction(Group group, String name, String intent) {
        GroupAction groupAction = new GroupAction();
        groupAction.setGroup(group.getId());
        groupAction.setName(name);
        groupAction.setIntent(intent);

        $(StoreHandler.class).getStore().box(GroupAction.class).put(groupAction);

        // TODO sync
        // $(SyncHandler.class).sync(groupAction);
    }

    private static class AddToGroupModalModel {
        String name;
        String intent;
    }
}
