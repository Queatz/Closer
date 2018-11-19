package closer.vlllage.com.closer.handler.bubble;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class MapBubbleProxyView extends PoolMember {

    private MapBubbleProxyAdapter adapter;

    public View from(ViewGroup layer, MapBubble mapBubble, MapBubbleView.OnMapBubbleClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_proxy, layer, false);

        RecyclerView recyclerView = view.findViewById(R.id.bubbleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new MapBubbleProxyAdapter(this, mapBubble, onClickListener);
        recyclerView.setAdapter(adapter);

        update(view, mapBubble);

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
        adapter.setItems(mapBubble.proxies());
    }
}
