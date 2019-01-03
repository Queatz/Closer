package closer.vlllage.com.closer.handler.bubble;

import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jacob on 2/18/18.
 */

public class MapBubble {
    private String phone;
    private LatLng latLng;
    private LatLng rawLatLng;
    private boolean inProxy;
    private boolean canProxy = true;
    private String name;
    private String status;
    private View view;
    private boolean pinned;
    private String action;
    private boolean onTop;
    private BubbleType type = BubbleType.STATUS;
    private OnItemClickListener onItemClickListener;
    private OnViewReadyListener onViewReadyListener;
    private Object tag;
    private List<MapBubble> proxies = new ArrayList<>();

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

    public MapBubble setTag(Object tag) {
        this.tag = tag;
        return this;
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

    public void proxies(Collection<MapBubble> proxiedBubbles) {
        this.proxies.addAll(proxiedBubbles);
    }

    public List<MapBubble> proxies() {
        return this.proxies;
    }

    public boolean isCanProxy() {
        return canProxy;
    }

    public MapBubble setCanProxy(boolean canProxy) {
        this.canProxy = canProxy;
        return this;
    }

    public MapBubble setOnViewReadyListener(OnViewReadyListener onViewReadyListener) {
        this.onViewReadyListener = onViewReadyListener;
        return this;
    }

    public OnViewReadyListener getOnViewReadyListener() {
        return onViewReadyListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnViewReadyListener {
        void onViewReady(View view);
    }
}
