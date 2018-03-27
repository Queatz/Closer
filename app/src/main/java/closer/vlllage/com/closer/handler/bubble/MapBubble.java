package closer.vlllage.com.closer.handler.bubble;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.util.LatLngStr;

/**
 * Created by jacob on 2/18/18.
 */

public class MapBubble {
    private String phone;
    private LatLng latLng;
    private String name;
    private String status;
    private View view;
    private boolean pinned;
    private String action;
    private boolean onTop;
    private BubbleType type = BubbleType.STATUS;
    private OnItemClickListener onItemClickListener;

    public MapBubble(LatLng latLng, String name, String status) {
        this.latLng = latLng;
        this.name = name;
        this.status = status;
    }

    public MapBubble(LatLng latLng, BubbleType type) {
        this.latLng = latLng;
        this.type = type;
        setPinned(true);
        setOnTop(true);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public View getView() {
        return view;
    }

    public MapBubble setView(View view) {
        this.view = view;
        return this;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public String getPhone() {
        return phone;
    }

    public MapBubble setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public static List<MapBubble> from(List<PhoneResult> phoneResults) {
        List<MapBubble> mapBubbles = new ArrayList<>();

        for (PhoneResult phoneResult : phoneResults) {
            if (phoneResult.geo == null) {
                continue;
            }

            mapBubbles.add(new MapBubble(
                    LatLngStr.to(phoneResult.geo),
                    phoneResult.name == null ? "" : phoneResult.name,
                    phoneResult.status
            ).setPhone(phoneResult.id));
        }

        return mapBubbles;
    }

    public void updateFrom(MapBubble mapBubble) {
        this.phone = mapBubble.phone;
        this.name = mapBubble.name;
        this.status = mapBubble.status;
    }

    public void setOnTop(boolean onTop) {
        this.onTop = onTop;
    }

    public boolean isOnTop() {
        return onTop;
    }

    public BubbleType getType() {
        return type;
    }

    public MapBubble setType(BubbleType type) {
        this.type = type;
        return this;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
