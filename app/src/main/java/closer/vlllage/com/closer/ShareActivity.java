package closer.vlllage.com.closer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.share.SearchGroupsHeaderAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class ShareActivity extends ListActivity {

    public static final String EXTRA_GROUP_MESSAGE_ID = "groupMessageId";

    private SearchGroupsHeaderAdapter searchGroupsAdapter;

    private String groupMessageId;
    private Uri data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        searchGroupsAdapter = new SearchGroupsHeaderAdapter($(PoolMember.class), (group, view) -> onGroupSelected(group), null);

        searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.share));
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_light);
        searchGroupsAdapter.setBackgroundResId(R.drawable.clickable_green_flat);

        recyclerView.setAdapter(searchGroupsAdapter);

        QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query();

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroups())
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer(searchGroupsAdapter::setGroups));

        if (getIntent() != null) {
            groupMessageId = getIntent().getStringExtra(EXTRA_GROUP_MESSAGE_ID);

            if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
                data = getIntent().getData();

                if (data == null) {
                    data = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
                }
            }
        }
    }

    private void onGroupSelected(Group group) {
        if (isDone()) {
            return;
        }

        if (groupMessageId != null) {
            $(GroupMessageAttachmentHandler.class).shareGroupMessage(group.getId(), groupMessageId);
            finish(() -> $(GroupActivityTransitionHandler.class).showGroupMessages(null, group.getId()));
        } else if (data != null) {
            $(PhotoUploadGroupMessageHandler.class).upload(data, photoId -> {
                boolean success = $(GroupMessageAttachmentHandler.class).sharePhoto($(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId), group.getId());
                if (!success) {
                    $(DefaultAlerts.class).thatDidntWork();
                }

                finish(() -> $(GroupActivityTransitionHandler.class).showGroupMessages(null, group.getId()));
            });
        }
    }
}
