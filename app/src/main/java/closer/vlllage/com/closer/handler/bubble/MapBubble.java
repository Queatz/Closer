package closer.vlllage.com.closer.handler.bubble;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jacob on 2/18/18.
 */

public class MapBubble {
    private String phone;
    private LatLng latLng;
    private LatLng rawLatLng;
    private boolean inProxy;
    private String name;
    private String status;
    private View view;
    private boolean pinned;
    private String action;
    private boolean onTop;
    private BubbleType type = BubbleType.STATUS;
    private OnItemClickListener onItemClickListener;
    private Object tag;

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

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    public void setRawLatLng(LatLng latLng) {
        this.rawLatLng = latLng;
    }

    public LatLng getRawLatLng() {
        if (rawLatLng != null) {
            return rawLatLng;
        }

        return latLng;
    }

    public boolean isInProxy() {
        return inProxy;
    }

    public MapBubble setInProxy(boolean inProxy) {
        this.inProxy = inProxy;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
