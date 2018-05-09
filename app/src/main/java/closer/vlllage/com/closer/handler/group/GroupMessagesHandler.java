package closer.vlllage.com.closer.handler.group;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.CircularRevealActivity;
import closer.vlllage.com.closer.handler.ActivityHandler;
import closer.vlllage.com.closer.handler.DefaultAlerts;
import closer.vlllage.com.closer.handler.DisposableHandler;
import closer.vlllage.com.closer.handler.MapActivityHandler;
import closer.vlllage.com.closer.handler.PersistenceHandler;
import closer.vlllage.com.closer.handler.SortHandler;
import closer.vlllage.com.closer.handler.SyncHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;

public class GroupMessagesHandler extends PoolMember {

    private GroupMessagesAdapter groupMessagesAdapter;

    public void attach(RecyclerView recyclerView, EditText replyMessage, ImageButton sendButton) {
        recyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.VERTICAL,
                true
        ));

        groupMessagesAdapter = new GroupMessagesAdapter(this);
        recyclerView.setAdapter(groupMessagesAdapter);

        groupMessagesAdapter.setOnSuggestionClickListener(suggestion -> {
            ((CircularRevealActivity) $(ActivityHandler.class).getActivity()).finish(() -> $(MapActivityHandler.class).showSuggestionOnMap(suggestion));
        });

        groupMessagesAdapter.setOnEventClickListener(event -> {
            ((CircularRevealActivity) $(ActivityHandler.class).getActivity()).finish(() -> $(MapActivityHandler.class).showEventOnMap(event));
        });

        replyMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                if (EditorInfo.IME_ACTION_GO == action) {
                    if (replyMessage.getText().toString().trim().isEmpty()) {
                        return false;
                    }
                    boolean success = send(replyMessage.getText().toString());
                    if (success) {
                        textView.setText("");
                    }

                    return true;
                }

                return false;
            }
        });

        sendButton.setOnClickListener(view -> {
            if (replyMessage.getText().toString().trim().isEmpty()) {
                return;
            }
            boolean success = send(replyMessage.getText().toString());
            if (success) {
                replyMessage.setText("");
            }
        });

        Date twelveHoursAgo = new Date();
        twelveHoursAgo.setTime(twelveHoursAgo.getTime() - 12 * HOUR_IN_MILLIS);

        Group group = $(GroupHandler.class).getGroup();

        if (group == null) {
            return;
        }

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query()
                .equal(GroupMessage_.to, group.getId());

        if (!group.isPublic()) {
            queryBuilder.greater(GroupMessage_.time, twelveHoursAgo);
        }

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .build()
                .subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroupMessages));
    }

    private void setGroupMessages(List<GroupMessage> groupMessages) {
        groupMessagesAdapter.setGroupMessages(groupMessages);
    }

    private boolean send(String text) {
        if ($(PersistenceHandler.class).getPhoneId() == null) {
            $(DefaultAlerts.class).thatDidntWork();
            return false;
        }

        if ($(GroupHandler.class).getGroup() == null) {
            return false;
        }

        if ($(GroupHandler.class).getGroupContact() == null) {
            if ($(GroupHandler.class).getGroup().isPublic()) {
                // todo: create groupcontact locally and sync if public
            } else {
                return false;
            }
        }

        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setText(text);
        groupMessage.setTo($(GroupHandler.class).getGroup().getId());
        groupMessage.setFrom($(PersistenceHandler.class).getPhoneId());
        groupMessage.setTime(new Date());
        $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
        $(SyncHandler.class).sync(groupMessage);

        return true;
    }
}
