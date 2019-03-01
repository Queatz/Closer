package closer.vlllage.com.closer.handler.group;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.event.EventHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.OutboundHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.ui.CircularRevealActivity;
import io.reactivex.subjects.BehaviorSubject;

import static com.google.android.gms.common.util.Strings.isEmptyOrWhitespace;

public class GroupToolbarHandler extends PoolMember {

    private RecyclerView recyclerView;
    private ToolbarAdapter adapter;
    private final BehaviorSubject<Boolean> isShareActiveObservable = BehaviorSubject.createDefault(false);

    private Group group;

    public void attach(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        adapter = new ToolbarAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
                child.getLayoutParams().width = recyclerView.getMeasuredWidth() / adapter.getItemCount();
                super.measureChild(child, widthUsed, heightUsed);
            }

            @Override
            public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
                child.getLayoutParams().width = recyclerView.getMeasuredWidth() / adapter.getItemCount();
                super.measureChildWithMargins(child, widthUsed, heightUsed);
            }
        });
        recyclerView.setAdapter(adapter);

        $(DisposableHandler.class).add(isShareActiveObservable.subscribe(isShareActive -> show(group)));
        $(DisposableHandler.class).add($(GroupHandler.class).onGroupUpdated().subscribe(this::show));
        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(this::show));
        $(DisposableHandler.class).add($(GroupHandler.class).onEventChanged().subscribe(g -> show(group)));
        $(DisposableHandler.class).add($(GroupHandler.class).onPhoneChanged().subscribe(g -> show(group)));
    }

    private void show(Group group) {
        if (group == null) {
            return;
        }

        this.group = group;
        Event event = $(GroupHandler.class).onEventChanged().getValue();
        Phone phone = $(GroupHandler.class).onPhoneChanged().getValue();

        List<ToolbarItem> items = new ArrayList<>();

        if (group.hasPhone()) {
            items.add(new ToolbarItem(
                    R.string.messages,
                    R.drawable.ic_message_black_24dp,
                    v -> {}
            ));

            items.add(new ToolbarItem(
                    R.string.photos,
                    R.drawable.ic_photo_black_24dp,
                    v -> {}
            ));

            items.add(new ToolbarItem(
                    R.string.groups,
                    R.drawable.ic_person_black_24dp,
                    v -> {}
            ));
        }

        if (items.size() < 3 && event != null) {
            items.add(new ToolbarItem(
                    isShareActiveObservable.getValue() ? R.string.cancel : R.string.share,
                    isShareActiveObservable.getValue() ? R.drawable.ic_close_black_24dp : R.drawable.ic_share_black_24dp,
                    v -> toggleShare()
            ));
        }

        if (items.size() < 3 && (event != null || group.isPhysical())) {
            items.add(new ToolbarItem(
                    R.string.show_on_map,
                    R.drawable.ic_my_location_black_24dp,
                    v -> {
                        if (event != null) showEventOnMap(event);
                        else showGroupOnMap(group);
                    }
            ));
        }

        if (items.size() < 3 && isEventCancelable(event)) {
            items.add(new ToolbarItem(
                    R.string.cancel,
                    R.drawable.ic_close_black_24dp,
                    v -> $(AlertHandler.class).make()
                            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.cancel_event))
                            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.event_will_be_cancelled, event.getName()))
                            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.cancel_event))
                            .setPositiveButtonCallback(result -> {
                                $(DisposableHandler.class).add($(ApiHandler.class).cancelEvent(event.getId()).subscribe(successResult -> {
                                    if (successResult.success) {
                                        $(DefaultAlerts.class).message($(ResourcesHandler.class).getResources().getString(R.string.event_cancelled, event.getName()));
                                        $(RefreshHandler.class).refreshEvents(new LatLng(event.getLatitude(), event.getLongitude()));
                                    } else {
                                        $(DefaultAlerts.class).thatDidntWork();
                                    }
                                }, error -> $(DefaultAlerts.class).thatDidntWork()));
                            })
                            .show()
            ));
        }

        if (items.size() < 3 && group.isPhysical() && isEmptyOrWhitespace(group.getName())) {
            items.add(new ToolbarItem(
                    R.string.set_name,
                    R.drawable.ic_edit_location_black_24dp,
                    v -> $(PhysicalGroupUpgradeHandler.class).convertToHub(group, updatedGroup -> {
                        $(GroupHandler.class).showGroupName(updatedGroup);
                        show(updatedGroup);
                    })
            ));
        }

        if (items.size() < 3 && group.isPhysical() && isEmptyOrWhitespace(group.getPhoto())) {
            items.add(new ToolbarItem(
                    R.string.set_background,
                    R.drawable.ic_camera_black_24dp,
                    v -> $(PhysicalGroupUpgradeHandler.class).setBackground(group, updateGroup -> {
                        $(GroupHandler.class).setGroupBackground(updateGroup);
                        show(updateGroup);
                    })
            ));
        }

        if (items.size() < 3 && (event != null || group.isPhysical())) {
            items.add(new ToolbarItem(
                    R.string.get_directions,
                    R.drawable.ic_directions_black_24dp,
                    v -> $(OutboundHandler.class).openDirections(new LatLng(
                            group.getLatitude(),
                            group.getLongitude()
                    ))
            ));
        }

        if (items.size() < 3 && group.isPhysical()) {
            items.add(new ToolbarItem(
                    R.string.host_event,
                    R.drawable.ic_event_note_black_24dp,
                    v -> $(EventHandler.class).createNewEvent(new LatLng(
                            group.getLatitude(),
                            group.getLongitude()
                    ), group.isPublic(), this::showEventOnMap)
            ));
        }

        adapter.setItems(items);
    }

    private boolean isEventCancelable(@Nullable Event event) {
        return event != null && $(PersistenceHandler.class).getPhoneId() != null &&
                !event.isCancelled() && event.getCreator() != null &&
                new Date().before(event.getEndsAt()) &&
                event.getCreator().equals($(PersistenceHandler.class).getPhoneId());
    }

    private void showGroupOnMap(Group group) {
        ((CircularRevealActivity) $(ActivityHandler.class).getActivity())
                .finish(() -> $(MapActivityHandler.class).showGroupOnMap(group));
    }

    private void toggleShare() {
        if (isShareActiveObservable.getValue()) {
            isShareActiveObservable.onNext(false);
            return;
        }

        isShareActiveObservable.onNext(true);
    }



    private void showEventOnMap(Event event) {
        ((CircularRevealActivity) $(ActivityHandler.class).getActivity())
                .finish(() -> $(MapActivityHandler.class).showEventOnMap(event));
    }

    public BehaviorSubject<Boolean> getIsShareActiveObservable() {
        return isShareActiveObservable;
    }

    static class ToolbarItem {
        View.OnClickListener onClickListener;
        @StringRes int name;
        @DrawableRes int icon;

        public ToolbarItem(int name, int icon, View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
            this.name = name;
            this.icon = icon;
        }
    }
}
