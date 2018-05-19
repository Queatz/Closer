package closer.vlllage.com.closer.handler.bubble;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Group;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class MapBubblePhysicalGroupView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, MapBubblePhysicalGroupClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble_physical_group, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onPhysicalGroupClick(mapBubble));
        update(view, mapBubble);

        return view;
    }

    public void update(View view, MapBubble mapBubble) {
        int margin = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.pad);
        int size = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.physicalGroupIcon);
        ImageView photo = view.findViewById(R.id.photo);
        if (mapBubble.getTag() != null & mapBubble.getTag() instanceof Group) {
            Group group = (Group) mapBubble.getTag();
            if (group.getPhoto() != null) {
                margin /= 4;
                size = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.physicalGroupPhoto);
                photo.setColorFilter(null);
                photo.setImageTintList(ColorStateList.valueOf($(ResourcesHandler.class).getResources().getColor(android.R.color.transparent)));
                Picasso.get()
                        .load(group.getPhoto())
                        .fit()
                        .transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.physicalGroupCorners), 0))
                        .into(photo);
            } else {
                photo.setImageResource(R.drawable.ic_wifi_black_24dp);
                photo.setImageTintList(ColorStateList.valueOf($(ResourcesHandler.class).getResources().getColor(android.R.color.white)));
            }
        }

        ((ViewGroup.MarginLayoutParams) photo.getLayoutParams()).setMargins(margin, margin, margin, margin);
        ((ViewGroup.MarginLayoutParams) photo.getLayoutParams()).height = size;
        ((ViewGroup.MarginLayoutParams) photo.getLayoutParams()).width= size;
    }

    public interface MapBubblePhysicalGroupClickListener {
        void onPhysicalGroupClick(MapBubble mapBubble);
    }
}
