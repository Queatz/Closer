package closer.vlllage.com.closer.handler.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.FeatureHandler;
import closer.vlllage.com.closer.handler.FeatureType;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.group.GroupActionAdapter;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupAction;
import closer.vlllage.com.closer.store.models.Group_;

public class GroupActionRecyclerViewHandler extends PoolMember {

    private GroupActionAdapter groupActionAdapter;
    private RecyclerView actionRecyclerView;
    private OnGroupActionRepliedListener onGroupActionRepliedListener;

    public void attach(RecyclerView actionRecyclerView) {
        this.actionRecyclerView = actionRecyclerView;
        actionRecyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        groupActionAdapter = new GroupActionAdapter(this, groupAction -> {
            Group group = $(StoreHandler.class).getStore().box(Group.class).query()
                    .equal(Group_.id, groupAction.getGroup()).build().findFirst();

            if (group == null) {
                $(DefaultAlerts.class).thatDidntWork();
                return;
            }

            $(AlertHandler.class).make()
                    .setLayoutResId(R.layout.comments_modal)
                    .setTextView(R.id.input, comment -> {
                        boolean success = $(GroupMessageAttachmentHandler.class).groupActionReply(groupAction.getGroup(), groupAction, comment);
                        if (!success) {
                            $(DefaultAlerts.class).thatDidntWork();
                        } else {
                            if (onGroupActionRepliedListener != null) {
                                onGroupActionRepliedListener.onGroupActionReplied(groupAction);
                            }
                        }
                    })
                    .setTitle(groupAction.getName())
                    .setMessage(group.getName())
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.go))
                    .show();
        }, groupAction -> {
            if ($(FeatureHandler.class).has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
                $(AlertHandler.class).make()
                        .setMessage($(ResourcesHandler.class).getResources().getString(R.string.remove_action_message, groupAction.getName()))
                        .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.remove_action))
                        .setPositiveButtonCallback(alertResult -> {
                            $(ApiHandler.class).removeGroupAction(groupAction.getId()).subscribe(
                                    successResult -> $(StoreHandler.class).getStore().box(GroupAction.class).remove(groupAction),
                                    error -> $(DefaultAlerts.class).thatDidntWork()
                            );
                        })
                        .show();
            }
        });

        actionRecyclerView.setAdapter(groupActionAdapter);
    }

    public GroupActionAdapter getAdapter() {
        return groupActionAdapter;
    }

    public RecyclerView getRecyclerView() {
        return actionRecyclerView;
    }

    public GroupActionRecyclerViewHandler setOnGroupActionRepliedListener(OnGroupActionRepliedListener onGroupActionRepliedListener) {
        this.onGroupActionRepliedListener = onGroupActionRepliedListener;
        return this;
    }

    public interface OnGroupActionRepliedListener {
        void onGroupActionReplied(GroupAction groupAction);
    }
}
