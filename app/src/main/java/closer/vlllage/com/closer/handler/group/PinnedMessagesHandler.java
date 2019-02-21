package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Pin;
import closer.vlllage.com.closer.store.models.Pin_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.reactive.DataSubscription;

public class PinnedMessagesHandler extends PoolMember {

    private RecyclerView pinnedMessagesRecyclerView;
    private GroupMessagesAdapter groupMessagesAdapter;
    private DataSubscription groupMessagesSubscription;
    private DataSubscription groupMessagesActualSubscription;

    public void attach(RecyclerView pinnedMessagesRecyclerView) {
        this.pinnedMessagesRecyclerView = pinnedMessagesRecyclerView;

        pinnedMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.VERTICAL,
                true
        ));

        groupMessagesAdapter = new GroupMessagesAdapter(this);
        groupMessagesAdapter.setPinned(true);
        pinnedMessagesRecyclerView.setAdapter(groupMessagesAdapter);
    }

    public void show(Group group) {
        if (groupMessagesSubscription != null) {
            $(DisposableHandler.class).dispose(groupMessagesSubscription);
        }

        if (groupMessagesActualSubscription != null) {
            $(DisposableHandler.class).dispose(groupMessagesSubscription);
        }

        $(RefreshHandler.class).refreshPins(group.getId());

        groupMessagesSubscription = $(StoreHandler.class).getStore().box(Pin.class).query()
                .equal(Pin_.to, group.getId())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer(pins -> {
                    if (pins.isEmpty()) {
                        setGroupMessages(new ArrayList<>());
                        return;
                    }

                    List<String> ids = new ArrayList<>();

                    for (Pin pin : pins) {
                        ids.add(pin.getFrom());
                    }

                    groupMessagesActualSubscription = $(StoreHandler.class).getStore().box(GroupMessage.class).query()
                            .in(GroupMessage_.id, ids.toArray(new String[0]))
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer(this::setGroupMessages);
                    $(DisposableHandler.class).add(groupMessagesActualSubscription);
                });

        $(DisposableHandler.class).add(groupMessagesSubscription);
    }

    private void setGroupMessages(List<GroupMessage> pinnedMessages) {
        pinnedMessagesRecyclerView.setVisibility(pinnedMessages.isEmpty() ? View.GONE : View.VISIBLE);
        groupMessagesAdapter.setGroupMessages(pinnedMessages);
    }
}
