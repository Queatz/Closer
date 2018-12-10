package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.search.GroupActionRecyclerViewHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.GroupAction_;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;
import closer.vlllage.com.closer.ui.RevealAnimator;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;

public class GroupActionHandler extends PoolMember {

    private RevealAnimator animator;
    private DataSubscription groupActionsDisposable;
    private boolean isShowing;

    public void attach(MaxSizeFrameLayout container, RecyclerView actionRecyclerView) {
        animator = new RevealAnimator(container, (int) ($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f));

        $(GroupActionRecyclerViewHandler.class).attach(actionRecyclerView, GroupActionAdapter.Layout.PHOTO);

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
                        $(GroupActionRecyclerViewHandler.class).getAdapter().setGroupActions(groupActions);
                        show(!groupActions.isEmpty(), true);
                    });

            $(DisposableHandler.class).add(groupActionsDisposable);

            $(RefreshHandler.class).refreshGroupActions(group.getId());
        }));
    }

    public void cancelPendingAnimation() {
        animator.cancel();
    }

    public void show(boolean show) {
        show(show, false);
    }

    private void show(boolean show, boolean immediate) {
        if (animator == null) {
            return;
        }

        if ($(GroupActionRecyclerViewHandler.class).getAdapter() != null && $(GroupActionRecyclerViewHandler.class).getAdapter().getItemCount() == 0) {
            show = false;
        }

        if (isShowing == show) {
            return;
        }

        isShowing = show;

        animator.show(show, immediate);
    }

    public void addActionToGroup(Group group) {
        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.add_an_action))
                .setNegativeButton($(ResourcesHandler.class).getResources().getString(R.string.nope))
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
        $(SyncHandler.class).sync(groupAction);
    }

    private static class AddToGroupModalModel {
        String name;
        String intent;
    }
}
