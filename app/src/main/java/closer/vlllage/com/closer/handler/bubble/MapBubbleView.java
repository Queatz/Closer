package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler;
import closer.vlllage.com.closer.handler.helpers.PhotoHelper;
import closer.vlllage.com.closer.handler.helpers.TimeStr;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Phone;

/**
 * Created by jacob on 2/18/18.
 */

public class MapBubbleView extends PoolMember {
    public View from(ViewGroup layer, MapBubble mapBubble, OnMapBubbleClickListener onClickListener) {
        View view = LayoutInflater.from(layer.getContext()).inflate(R.layout.map_bubble, layer, false);

        view.findViewById(R.id.click).setOnClickListener(v -> onClickListener.onMapBubbleClick(mapBubble));
        update(view, mapBubble);

        return view;
    }

    public void update(View view, MapBubble mapBubble) {

        TextView name = view.findViewById(R.id.name);
        TextView info = view.findViewById(R.id.info);
        TextView action = view.findViewById(R.id.action);
        ImageView photo = view.findViewById(R.id.photo);

        if (mapBubble.isInProxy()) {
            name.setVisibility(View.VISIBLE);
            name.setText(mapBubble.getName() + "\n" + mapBubble.getStatus());

            if (info != null) {
                info.setText($(TimeStr.class).pretty(((Phone) mapBubble.getTag()).getUpdated()));
            }
        } else {
            if (mapBubble.getName().isEmpty()) {
                name.setVisibility(View.GONE);
            } else {
                name.setVisibility(View.VISIBLE);
                name.setText(mapBubble.getName());
            }

            ((TextView) view.findViewById(R.id.status)).setText($(Val.class).of(mapBubble.getStatus()));

            if (mapBubble.getAction() != null && action != null) {
                action.setText(mapBubble.getAction());
            } else if (mapBubble.getTag() instanceof Phone) {
                Phone phone = (Phone) mapBubble.getTag();

                if (action != null) {
                    action.setText($(TimeStr.class).pretty(phone.getUpdated()));
                }

                if (photo != null) {
                    if (!$(Val.class).isEmpty(phone.getPhoto())) {
                        $(PhotoHelper.class).loadCircle(photo, phone.getPhoto());
                        photo.setOnClickListener(v -> {
                            $(PhotoActivityTransitionHandler.class).show(photo, phone.getPhoto());
                        });
                    }
                }
            }
        }
    }

    public interface OnMapBubbleClickListener {
        void onMapBubbleClick(MapBubble mapBubble);
    }
}
