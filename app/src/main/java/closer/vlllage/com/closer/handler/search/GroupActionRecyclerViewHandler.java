package closer.vlllage.com.closer.handler.search;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.FeatureHandler;
import closer.vlllage.com.closer.handler.FeatureType;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.group.GroupActionAdapter;
import closer.vlllage.com.closer.handler.group.GroupActionUpgradeHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.MenuHandler;
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

    public void attach(RecyclerView actionRecyclerView, GroupActionAdapter.Layout layout) {
        this.actionRecyclerView = actionRecyclerView;
        actionRecyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));

        groupActionAdapter = new GroupActionAdapter(this, layout, groupAction -> {
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
                    .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.post))
                    .show();
        }, groupAction -> {
            if ($(FeatureHandler.class).has(FeatureType.FEATURE_MANAGE_PUBLIC_GROUP_SETTINGS)) {
                $(MenuHandler.class).show(
                        new MenuHandler.MenuOption(R.drawable.ic_camera_black_24dp, R.string.take_photo, () -> takeGroupActionPhoto(groupAction)),
                        new MenuHandler.MenuOption(R.drawable.ic_photo_black_24dp, R.string.upload_photo, () -> uploadGroupActionPhoto(groupAction)),
                        new MenuHandler.MenuOption(R.drawable.ic_close_black_24dp, R.string.remove_action_menu_item, () -> removeGroupAction(groupAction))
                );
            }
        });

        actionRecyclerView.setAdapter(groupActionAdapter);
    }

    private void uploadGroupActionPhoto(GroupAction groupAction) {
        $(GroupActionUpgradeHandler.class).setPhotoFromMedia(groupAction);
    }

    private void takeGroupActionPhoto(GroupAction groupAction) {
        $(GroupActionUpgradeHandler.class).setPhotoFromCamera(groupAction);
    }

    private void removeGroupAction(GroupAction groupAction) {
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
