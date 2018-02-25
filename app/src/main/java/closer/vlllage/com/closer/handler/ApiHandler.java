package closer.vlllage.com.closer.handler;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.handler.bubble.MapBubble;
import closer.vlllage.com.closer.pool.PoolMember;
import io.reactivex.Observable;

public class ApiHandler extends PoolMember {
    public Observable<List<MapBubble>> load(LatLng latLng) {
        List<MapBubble> mapBubbles = new ArrayList<>();

        MapBubble alfred = new MapBubble(new LatLng(latLng.latitude + Math.random() * 0.06 - 0.03, latLng.longitude + Math.random() * 0.06 - 0.03), "Alfred", "Walking the doggo");
        mapBubbles.add(alfred);
        mapBubbles.add(new MapBubble(new LatLng(latLng.latitude + Math.random() * 0.06 - 0.03, latLng.longitude + Math.random() * 0.06 - 0.03), "Meghan", "Homework"));

        return Observable.just(mapBubbles);
    }

    public void updatePhone(String deviceToken) {

    }
}
