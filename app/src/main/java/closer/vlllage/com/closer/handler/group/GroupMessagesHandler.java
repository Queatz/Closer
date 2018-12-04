package closer.vlllage.com.closer.handler.group;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.CameraHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.handler.media.MediaHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupDraftHandler;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.ui.CircularRevealActivity;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

import static android.text.format.DateUtils.HOUR_IN_MILLIS;

public class GroupMessagesHandler extends PoolMember {

    private GroupMessagesAdapter groupMessagesAdapter;
    private EditText replyMessage;
    private ImageButton sendButton;
    private ImageButton sendMoreButton;
    private View sendMoreLayout;

    public void attach(RecyclerView recyclerView, EditText replyMessage, ImageButton sendButton, ImageButton sendMoreButton, View sendMoreLayout) {
        this.replyMessage = replyMessage;
        this.sendButton = sendButton;
        this.sendMoreButton = sendMoreButton;
        this.sendMoreLayout = sendMoreLayout;

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
            ((CircularRevealActivity) $(ActivityHandler.class).getActivity()).finish(() -> $(GroupActivityTransitionHandler.class).showGroupForEvent(null, event));
        });

        this.replyMessage.setOnEditorActionListener((textView, action, keyEvent) -> {
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
        });

        this.sendButton.setOnClickListener(view -> {
            if (replyMessage.getText().toString().trim().isEmpty()) {
                $(CameraHandler.class).showCamera(photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> {
                    boolean success = $(GroupMessageAttachmentHandler.class).sharePhoto($(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId), $(GroupHandler.class).getGroup().getId());
                    if (!success) {
                        $(DefaultAlerts.class).thatDidntWork();
                    }
                }));
                return;
            }

            boolean success = send(replyMessage.getText().toString());
            if (success) {
                replyMessage.setText("");
            }
        });

        updateSendButton();
        $(DisposableHandler.class).add($(GroupHandler.class).onGroupChanged().subscribe(group -> {
            if (replyMessage.getText().toString().isEmpty()) {
                replyMessage.setText($(GroupDraftHandler.class).getDraft(group));
                updateSendButton();
            }
        }, error -> $(DefaultAlerts.class).thatDidntWork()));

        this.replyMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                updateSendButton();
                $(GroupDraftHandler.class).saveDraft($(GroupHandler.class).getGroup(), text.toString());
                showSuggestionsForName(extractName(text, replyMessage.getSelectionStart()));
            }
        });

        this.sendMoreButton.setOnClickListener(view -> {
            if (sendMoreLayout.getVisibility() == View.VISIBLE) {
                sendMoreButton.setImageResource(R.drawable.ic_more_horiz_black_24dp);
                sendMoreLayout.setVisibility(View.GONE);
            } else {
                sendMoreButton.setImageResource(R.drawable.ic_close_black_24dp);
                sendMoreLayout.setVisibility(View.VISIBLE);
            }
        });

        View sendMoreActionAudio = this.sendMoreLayout.findViewById(R.id.sendMoreActionAudio);
        View sendMoreActionVideo = this.sendMoreLayout.findViewById(R.id.sendMoreActionVideo);
        View sendMoreActionFile = this.sendMoreLayout.findViewById(R.id.sendMoreActionFile);
        View sendMoreActionPhoto = this.sendMoreLayout.findViewById(R.id.sendMoreActionPhoto);

        sendMoreActionAudio.setOnClickListener(view -> {
            this.sendMoreButton.callOnClick();
            $(DefaultAlerts.class).message("Woah matey!");
        });
        sendMoreActionVideo.setOnClickListener(view -> {
            this.sendMoreButton.callOnClick();
            $(DefaultAlerts.class).message("Woah matey!");
        });
        sendMoreActionFile.setOnClickListener(view -> {
            this.sendMoreButton.callOnClick();
            $(DefaultAlerts.class).message("Woah matey!");
        });
        sendMoreActionPhoto.setOnClickListener(view -> {
            this.sendMoreButton.callOnClick();
            $(MediaHandler.class).getPhoto(photoUri -> $(PhotoUploadGroupMessageHandler.class).upload(photoUri, photoId -> {
                boolean success = $(GroupMessageAttachmentHandler.class).sharePhoto($(PhotoUploadGroupMessageHandler.class).getPhotoPathFromId(photoId), $(GroupHandler.class).getGroup().getId());
                if (!success) {
                    $(DefaultAlerts.class).thatDidntWork();
                }
            }));
        });

        Date thirySixHoursAgo = new Date();
        thirySixHoursAgo.setTime(thirySixHoursAgo.getTime() - 36 * HOUR_IN_MILLIS);

        Group group = $(GroupHandler.class).getGroup();

        if (group == null) {
            return;
        }

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query()
                .equal(GroupMessage_.to, group.getId());

        if (!group.isPublic()) {
            queryBuilder.greater(GroupMessage_.time, thirySixHoursAgo);
        }

        $(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .build()
                .subscribe().on(AndroidScheduler.mainThread())
                .observer(this::setGroupMessages));
    }

    public void insertMention(Phone mention) {
        CharSequence replaceString = extractName(replyMessage.getText(), replyMessage.getSelectionStart());

        if (replaceString == null) {
            replaceString = "";
        }

        replyMessage.getText().replace(replyMessage.getSelectionStart() - replaceString.length(), replyMessage.getSelectionStart(), "@" + mention.getId());
        replyMessage.getText().setSpan(getImageSpan(mention.getName()),
                replyMessage.getSelectionStart() - mention.getId().length() - 1, replyMessage.getSelectionStart(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void showSuggestionsForName(CharSequence name) {
        if (name == null) {
            $(GroupMessageMentionHandler.class).show(false);
        } else {
            if (name.charAt(0) == '@') {
                name = name.subSequence(1, name.length());
            }
            List<Phone> phones = $(StoreHandler.class).getStore().box(Phone.class).query().
                    contains(Phone_.name, name.toString(), QueryBuilder.StringOrder.CASE_INSENSITIVE)
                    .build().find();

            if (phones.isEmpty()) {
                $(GroupMessageMentionHandler.class).show(false);
            } else {
                $(GroupMessageMentionHandler.class).show(true);
                $(GroupMessageMentionHandler.class).setItems(phones);
            }
        }
    }

    private ImageSpan getImageSpan(String name) {
        TextView textView = createContactTextView(name);
        BitmapDrawable bitmapDrawable = convertViewToDrawable(textView);
        bitmapDrawable.setBounds(0, 0, bitmapDrawable.getIntrinsicWidth(),bitmapDrawable.getIntrinsicHeight());
        return new ImageSpan(bitmapDrawable);
    }

    private TextView createContactTextView(String text) {
        TextView textView = new TextView($(ActivityHandler.class).getActivity());
        textView.setText("@" + text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, $(ResourcesHandler.class).getResources().getDimension(R.dimen.groupMessageMentionTextSize));
        textView.setTextColor($(ResourcesHandler.class).getResources().getColor(R.color.colorAccentLight));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        return textView;
    }

    private BitmapDrawable convertViewToDrawable(View view) {
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable($(ResourcesHandler.class).getResources(), viewBmp);
    }

    private CharSequence extractName(Editable text, int position) {
        if (position > 0 && position <= text.length()) {
            for (int i = position - 1; i >= 0; i--) {
                if (text.charAt(i) == '@') {
                    return text.subSequence(i, position);
                } else if (Character.isWhitespace(text.charAt(i))) {
                    return null;
                }
            }
        }

        return null;
    }

    private void updateSendButton() {
        if (replyMessage.getText().toString().trim().isEmpty()) {
            sendButton.setImageResource(R.drawable.ic_camera_black_24dp);
            sendMoreButton.setVisibility(View.VISIBLE);
            $(GroupActionHandler.class).show(true);
        } else {
            sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp);
            sendMoreButton.setVisibility(View.GONE);
            sendMoreLayout.setVisibility(View.GONE);
            sendMoreButton.setImageResource(R.drawable.ic_more_horiz_black_24dp);
            $(GroupActionHandler.class).show(false);
        }
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
            if (!$(GroupHandler.class).getGroup().isPublic()) {
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
