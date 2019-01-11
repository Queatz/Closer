package closer.vlllage.com.closer.handler.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.api.models.EventResult;
import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Event_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import io.reactivex.Single;

public class DataHandler extends PoolMember {
    public Single<List<Phone>> getPhonesNear(LatLng latLng) {
        return $(ApiHandler.class).getPhonesNear(latLng)
                .doOnSuccess(phoneResults -> {
                    $(RefreshHandler.class).handleFullListResult(phoneResults, Phone.class, Phone_.id, false, PhoneResult::from, PhoneResult::updateFrom);
                })
                .map(phoneResults -> {
                    List<Phone> result = new ArrayList<>(phoneResults.size());
                    for (PhoneResult phoneResult : phoneResults) {
                        result.add(PhoneResult.from(phoneResult));
                    }
                    return result;
                });
    }

    public Single<Event> getEventById(String eventId) {
        Event event = $(StoreHandler.class).getStore().box(Event.class).query()
                .equal(Event_.id, eventId)
                .build().findFirst();

        if (event != null) {
            return Single.just(event);
        }

        return $(ApiHandler.class).getEvent(eventId).map(EventResult::from)
                .doOnSuccess(eventFromServer -> $(RefreshHandler.class).refresh(eventFromServer));
    }

}
