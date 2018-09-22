package closer.vlllage.com.closer.handler.map;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import closer.vlllage.com.closer.handler.feed.GroupPreviewAdapter;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.ui.FeedInjectionsAdapter;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class FeedHandler extends PoolMember {
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private HeaderAdapter headerAdapter;
    private GroupPreviewAdapter groupPreviewAdapter;

    public void attach(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        layoutManager = new LinearLayoutManager(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (top != oldTop || bottom != oldBottom) {
                headerAdapter.notifyHeaderHeightChanged();
            }
        });

        setupFeedInjections();

        float distance = .12f;

        $(DisposableHandler.class).add($(MapHandler.class).onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cameraPosition -> {
                    QueryBuilder<Group> groupPreviewQueryBuilder = $(StoreHandler.class).getStore().box(Group.class).query()
                            .between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                            .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                            .equal(Group_.isPublic, true);
                    $(DisposableHandler.class).add(groupPreviewQueryBuilder
                            .sort($(SortHandler.class).sortGroups())
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer(groupPreviewAdapter::setGroups));
                }));

    }

    private void setupFeedInjections() {
        headerAdapter = new HeaderAdapter(this);

        FeedInjectionsAdapter feedInjectionsAdapter = new FeedInjectionsAdapter(this);
        headerAdapter.addAdapter(feedInjectionsAdapter);
        groupPreviewAdapter = new GroupPreviewAdapter(this);
        headerAdapter.addAdapter(groupPreviewAdapter);

        recyclerView.setAdapter(headerAdapter);
        headerAdapter.setHeaderHeightCallback(recyclerView::getHeight);
    }

    public void hide() {
        if (layoutManager.findFirstVisibleItemPosition() > 2) {
            recyclerView.scrollToPosition(2);
        }

        recyclerView.smoothScrollToPosition(0);
    }
}
