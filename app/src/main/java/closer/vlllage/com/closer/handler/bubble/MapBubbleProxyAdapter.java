package closer.vlllage.com.closer.handler.bubble;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.helpers.ImageHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class MapBubbleProxyAdapter extends PoolRecyclerAdapter<MapBubbleProxyAdapter.ProxyMapBubbleViewHolder> {
    private final List<MapBubble> items = new ArrayList<>();
    private MapBubbleView.OnMapBubbleClickListener onClickListener;
    private MapBubble proxyMapBubble;

    public MapBubbleProxyAdapter(PoolMember poolMember, MapBubble proxyMapBubble, MapBubbleView.OnMapBubbleClickListener onClickListener) {
        super(poolMember);
        this.onClickListener = onClickListener;
        this.proxyMapBubble = proxyMapBubble;
    }

    public MapBubbleProxyAdapter setItems(List<MapBubble> items) {
        this.items.clear();
        this.items.addAll(items);
        return this;
    }

    @NonNull
    @Override
    public ProxyMapBubbleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProxyMapBubbleViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_bubble_proxy_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProxyMapBubbleViewHolder holder, int position) {
        MapBubble mapBubble = items.get(position);
        switch (mapBubble.getType()) {
            case STATUS:
                holder.click.setBackgroundResource(R.drawable.clickable_blue_4dp);
                holder.photo.setVisibility(View.GONE);
                holder.name.setText(mapBubble.getName() + "\n" + mapBubble.getStatus());
                break;
            case PHYSICAL_GROUP:
                holder.click.setBackgroundResource(R.drawable.clickable_purple_4dp);
                holder.photo.setVisibility(View.VISIBLE);

                if (mapBubble.getTag() != null & mapBubble.getTag() instanceof Group) {
                    Group group = (Group) mapBubble.getTag();
                    if (group.getPhoto() != null) {
                        holder.photo.setColorFilter(null);
                        holder.photo.setImageTintList(ColorStateList.valueOf($(ResourcesHandler.class).getResources().getColor(android.R.color.transparent)));
                        $(ImageHandler.class).get()
                                .load(group.getPhoto() + "?s=32")
                                .fit()
                                .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.physicalGroupCorners), 0))
                                .into(holder.photo);
                    } else {
                        holder.photo.setImageResource(R.drawable.ic_wifi_black_24dp);
                        holder.photo.setImageTintList(ColorStateList.valueOf($(ResourcesHandler.class).getResources().getColor(android.R.color.white)));
                    }
                }

                holder.name.setText($(Val.class).of(((Group) mapBubble.getTag()).getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));
                break;
            case EVENT:
                holder.click.setBackgroundResource(R.drawable.clickable_red_4dp);
                holder.photo.setVisibility(View.GONE);

                Event event = ((Event) mapBubble.getTag());

                holder.name.setText(event.getName() + "\n" + $(EventDetailsHandler.class).formatEventDetails(event));

                if (event.isPublic()) {
                    holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    holder.name.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_lock_black_18dp, 0, 0, 0);
                }
                break;
        }

        holder.itemView.setOnClickListener(view -> {
            mapBubble.setView(view);
            onClickListener.onMapBubbleClick(mapBubble);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ProxyMapBubbleViewHolder extends RecyclerView.ViewHolder {

        View click;
        ImageView photo;
        TextView name;

        public ProxyMapBubbleViewHolder(View itemView) {
            super(itemView);
            click = itemView.findViewById(R.id.click);
            photo = itemView.findViewById(R.id.photo);
            name = itemView.findViewById(R.id.name);
        }
    }
}
