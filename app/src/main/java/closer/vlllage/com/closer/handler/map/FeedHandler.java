package closer.vlllage.com.closer.handler.map;

import android.animation.ValueAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import closer.vlllage.com.closer.pool.PoolMember;

public class FeedHandler extends PoolMember {
    private FeedAdapter feedAdapter;

    public void attach(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(
                recyclerView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        ));

        feedAdapter = new FeedAdapter(this);
        feedAdapter.setHeaderHeightCallback(recyclerView::getHeight);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                feedAdapter.notifyHeaderHeightChanged());
        recyclerView.setAdapter(feedAdapter);
    }
}
