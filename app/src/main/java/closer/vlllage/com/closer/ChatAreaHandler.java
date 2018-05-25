package closer.vlllage.com.closer;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.List;

import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesAdapter;
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.CameraHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.map.AreaMessagesHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

public class ChatAreaHandler extends PoolMember {

    private GroupMessagesAdapter groupMessagesAdapter;
    private EditText replyMessage;
    private ImageButton sendButton;
    private RecyclerView recyclerView;

    public void attach(View areaChat) {
        replyMessage = areaChat.findViewById(R.id.replyMessage);
        sendButton = areaChat.findViewById(R.id.sendButton);
        recyclerView = areaChat.findViewById(R.id.messagesRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(
                $(ActivityHandler.class).getActivity(),
                LinearLayoutManager.VERTICAL,
                true
        ));

        groupMessagesAdapter = new GroupMessagesAdapter(this);
        groupMessagesAdapter.setNoPadding(true);
        recyclerView.setAdapter(groupMessagesAdapter);

        groupMessagesAdapter.setOnSuggestionClickListener(suggestion -> $(MapActivityHandler.class).showSuggestionOnMap(suggestion));
        groupMessagesAdapter.setOnEventClickListener(event -> $(MapActivityHandler.class).showEventOnMap(event));
        replyMessage.setOnEditorActionListener((textView, action, keyEvent) -> {
            if (EditorInfo.IME_ACTION_GO == action) {
                if (replyMessage.getText().toString().trim().isEmpty()) {
                    return false;
                }
                send(replyMessage.getText().toString());
                return true;
            }

            return false;
        });

        sendButton.setOnClickListener(view -> {
            if (replyMessage.getText().toString().trim().isEmpty()) {
                $(CameraHandler.class).showCamera((photoUri, groupId) -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> {
                    boolean success = $(GroupMessageAttachmentHandler.class).sharePhoto($(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId), null/*TODO*/);
                    if (!success) {
                        $(DefaultAlerts.class).thatDidntWork();
                    }
                }));
                return;
            }

            send(replyMessage.getText().toString());
        });

        replyMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton();
            }
        });
        updateSendButton();

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query()
                .isNull(GroupMessage_.to);

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .build()
                .subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroupMessages));
    }

    private void send(String message) {
        $(AreaMessagesHandler.class).send(message);
        replyMessage.setText("");
    }

    private void updateSendButton() {
        if (replyMessage.getText().toString().trim().isEmpty()) {
            sendButton.setImageResource(R.drawable.ic_camera_black_24dp);
        } else {
            sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp);
        }
    }

    private void setGroupMessages(List<GroupMessage> groupMessages) {
        groupMessagesAdapter.setGroupMessages(groupMessages);
    }
}
