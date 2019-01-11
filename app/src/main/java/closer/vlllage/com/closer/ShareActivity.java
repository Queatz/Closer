package closer.vlllage.com.closer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewTreeObserver;

import closer.vlllage.com.closer.handler.SearchGroupHandler;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.ToastHandler;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.share.SearchGroupsHeaderAdapter;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class ShareActivity extends ListActivity {

    public static final String EXTRA_GROUP_MESSAGE_ID = "groupMessageId";
    public static final String EXTRA_INVITE_TO_GROUP_PHONE_ID = "inviteToGroupPhoneId";

    private SearchGroupsHeaderAdapter searchGroupsAdapter;

    private String groupMessageId;
    private String phoneId;
    private Uri data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        $(SearchGroupHandler.class).hideCreateGroupOption();

        searchGroupsAdapter = new SearchGroupsHeaderAdapter($(PoolMember.class), (group, view) -> onGroupSelected(group), null, query -> $(SearchGroupHandler.class).showGroupsForQuery(searchGroupsAdapter, query));

        searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.share));
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_light);
        searchGroupsAdapter.setBackgroundResId(R.drawable.clickable_green_flat);

        $(SearchGroupHandler.class).showGroupsForQuery(searchGroupsAdapter, "");

        QueryBuilder<Group> queryBuilder = $(StoreHandler.class).getStore().box(Group.class).query();
        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroups())
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer($(SearchGroupHandler.class)::setGroups));

        if (getIntent() != null) {
            groupMessageId = getIntent().getStringExtra(EXTRA_GROUP_MESSAGE_ID);
            phoneId = getIntent().getStringExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID);

            searchGroupsAdapter.setHeaderText($(ResourcesHandler.class).getResources().getString(R.string.share_to));

            if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
                data = getIntent().getData();

                if (data == null) {
                    data = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
                }
            } else if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                if (phoneId != null) {
                    searchGroupsAdapter.setHeaderText($(ResourcesHandler.class).getResources().getString(R.string.add_person_to, $(NameHandler.class).getName(phoneId)));
                    searchGroupsAdapter.setActionText($(ResourcesHandler.class).getResources().getString(R.string.add));
                }
            }
        }

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                recyclerView.setAdapter(searchGroupsAdapter);
            }
        });
    }

    private void onGroupSelected(Group group) {
        if (isDone()) {
            return;
        }

        if (phoneId != null) {
            $(DisposableHandler.class).add($(ApiHandler.class).inviteToGroup(group.getId(), phoneId).subscribe(
                    successResult -> {
                        if (successResult.success) {
                            $(ToastHandler.class).show($(ResourcesHandler.class).getResources().getString(R.string.added_phone, $(NameHandler.class).getName(phoneId)));
                            finish();
                        } else {
                            $(DefaultAlerts.class).thatDidntWork();
                        }
                    }, error -> $(DefaultAlerts.class).thatDidntWork()));
        } else if (groupMessageId != null) {
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
