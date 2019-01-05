package closer.vlllage.com.closer.handler.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
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
        if (mapBubble.isInProxy()) {
            view.findViewById(R.id.name).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.name)).setText(mapBubble.getName() + "\n" + mapBubble.getStatus());
            ((TextView) view.findViewById(R.id.info)).setText($(TimeStr.class).pretty(((Phone) mapBubble.getTag()).getUpdated()));
        } else {
            if (mapBubble.getName().isEmpty()) {
                view.findViewById(R.id.name).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.name).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.name)).setText(mapBubble.getName());
            }

            ((TextView) view.findViewById(R.id.status)).setText($(Val.class).of(mapBubble.getStatus()));

            if (mapBubble.getAction() != null) {
                ((TextView) view.findViewById(R.id.action)).setText(mapBubble.getAction());
            } else if (mapBubble.getTag() instanceof Phone) {
                Phone phone = (Phone) mapBubble.getTag();
                ((TextView) view.findViewById(R.id.action)).setText($(TimeStr.class).pretty(phone.getUpdated()));

                if (!$(Val.class).isEmpty(phone.getPhoto())) {
                    $(PhotoHelper.class).loadCircle(view.findViewById(R.id.photo), phone.getPhoto());
                }
            }
        }
    }

    public interface OnMapBubbleClickListener {
        void onMapBubbleClick(MapBubble mapBubble);
    }
}
