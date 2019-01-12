package closer.vlllage.com.closer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import closer.vlllage.com.closer.api.models.PhoneResult;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.ToastHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.phone.PhoneAdapter;
import closer.vlllage.com.closer.handler.phone.PhoneAdapterHeaderAdapter;
import closer.vlllage.com.closer.pool.PoolMember;

public class PhoneListActivity extends ListActivity {

    public static final String EXTRA_GROUP_MESSAGE_ID = "groupMessageId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        $(ApiHandler.class).setAuthorization($(AccountHandler.class).getPhone());

        PhoneAdapter adapter = new PhoneAdapterHeaderAdapter($(PoolMember.class), reactionResult -> {
            if (reactionResult.from.equals($(PersistenceHandler.class).getPhoneId())) {
                $(DefaultAlerts.class).message($(ResourcesHandler.class).getResources().getString(R.string.remove_heart), result -> {
                    $(DisposableHandler.class).add($(ApiHandler.class).reactToMessage(reactionResult.to, "♥", true)
                            .subscribe(successResult -> {
                                if (successResult.success) {
                                    $(ToastHandler.class).show(R.string.heart_removed);
                                    $(ApplicationHandler.class).getApp().$(RefreshHandler.class).refreshGroupMessage(reactionResult.to);
                                    finish();
                                } else {
                                    $(DefaultAlerts.class).thatDidntWork();
                                }
                            }, error -> $(DefaultAlerts.class).thatDidntWork()));
                });

                return;
            }

            finish(() -> {
                $(MapActivityHandler.class).replyToPhone(
                        reactionResult.phone.id,
                        $(NameHandler.class).getName(PhoneResult.from(reactionResult.phone)),
                        reactionResult.reaction,
                        reactionResult.phone.geo == null ? null : new LatLng(
                                reactionResult.phone.geo.get(0),
                                reactionResult.phone.geo.get(1)
                        ));
            });
        });
        recyclerView.setAdapter(adapter);

        if (getIntent() != null && Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            String groupMessageId = getIntent().getStringExtra(EXTRA_GROUP_MESSAGE_ID);

            if (groupMessageId == null) {
                $(DefaultAlerts.class).thatDidntWork();
            } else {
                $(DisposableHandler.class).add($(ApiHandler.class).groupMessageReactions(groupMessageId)
                        .subscribe(adapter::setItems, error -> $(DefaultAlerts.class).thatDidntWork()));
            }
        }

    }
}
