package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;

public class PinnedMessagesHandler extends PoolMember {

    private RecyclerView pinnedMessagesRecyclerView;
    private GroupMessagesAdapter groupMessagesAdapter;
    private DataSubscription groupMessagesSubscription;

    public void attach(RecyclerView pinnedMessagesRecyclerView) {
        this.pinnedMessagesRecyclerView = pinnedMessagesRecyclerView;

        pinnedMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.VERTICAL,
                true
        ));

        groupMessagesAdapter = new GroupMessagesAdapter(this);
        groupMessagesAdapter.setNoPadding(true);
        pinnedMessagesRecyclerView.setAdapter(groupMessagesAdapter);

        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(group -> {
            if (groupMessagesSubscription != null) {
                $(DisposableHandler.class).dispose(groupMessagesSubscription);
            }

            groupMessagesSubscription = $(StoreHandler.class).getStore().box(GroupMessage.class).query()
                    .equal(GroupMessage_.to, group.getId())
                    .sort($(SortHandler.class).sortGroupMessages())
                    .build()
                    .subscribe().on(AndroidScheduler.mainThread())
                    .observer(this::setGroupMessages);

            $(DisposableHandler.class).add(groupMessagesSubscription);
        }));
    }

    private void setGroupMessages(List<GroupMessage> groupMessages) {
        groupMessagesAdapter.setGroupMessages(groupMessages.subList(0, 1));
    }
}
