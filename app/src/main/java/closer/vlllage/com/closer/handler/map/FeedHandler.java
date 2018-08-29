package closer.vlllage.com.closer.handler.map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Group_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class FeedHandler extends PoolMember {
    private FeedAdapter feedAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    public void attach(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        layoutManager = new LinearLayoutManager(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recyclerView.setLayoutManager(layoutManager);

        feedAdapter = new FeedAdapter(this);
        feedAdapter.setReversed(true);
        feedAdapter.setHeaderHeightCallback(recyclerView::getHeight);
        feedAdapter.setOnSuggestionClickListener(suggestion -> {
            $(MapActivityHandler.class).showSuggestionOnMap(suggestion);
            hide();
            });
        feedAdapter.setOnEventClickListener(event -> {
            $(MapActivityHandler.class).showEventOnMap(event);
            hide();
        });
        feedAdapter.setOnMessageClickListener(message -> {
            if (message.getTo() != null) {
                $(GroupActivityTransitionHandler.class).showGroupMessages(recyclerView, message.getTo());
            }
        });
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                feedAdapter.notifyHeaderHeightChanged());
        recyclerView.setAdapter(feedAdapter);

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query();

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .filter(groupMessage -> {
                    if (groupMessage.getTo() == null) {
                        return true;
                    }

                    Group group = $(StoreHandler.class).getStore().box(Group.class).query().equal(Group_.id,groupMessage.getTo()).build().findFirst();
                    return group == null || group.isPublic();
                })
                .build()
                .subscribe().on(AndroidScheduler.mainThread())
                .observer(feedAdapter::setGroupMessages));
    }

    public void hide() {
        if (layoutManager.findFirstVisibleItemPosition() > 2) {
            recyclerView.scrollToPosition(2);
        }

        recyclerView.smoothScrollToPosition(0);
    }
}
