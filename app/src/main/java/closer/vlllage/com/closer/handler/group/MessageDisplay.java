package closer.vlllage.com.closer.handler.group;

import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.Date;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.ImageHandler;
import closer.vlllage.com.closer.handler.helpers.JsonHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.store.models.Suggestion;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class MessageDisplay extends PoolMember {

    public void displayShare(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage, GroupMessagesAdapter.OnEventClickListener onEventClickListener, GroupMessagesAdapter.OnSuggestionClickListener onSuggestionClickListener) {
        GroupMessage sharedGroupMessage = $(StoreHandler.class).getStore().box(GroupMessage.class).query().equal(GroupMessage_.id, jsonObject.get("share").getAsString()).build().findFirst();

        if (sharedGroupMessage != null) {
            display(holder, sharedGroupMessage, onEventClickListener, onSuggestionClickListener);
        } else {
            displayFallback(holder, groupMessage);
        }

        holder.time.setText($(GroupMessageParseHandler.class).parseText($(ResourcesHandler.class).getResources().getString(R.string.shared_by, getTimeString(groupMessage.getTime()), "@" + groupMessage.getFrom())));
    }

    public void displayAction(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage) {
        holder.eventMessage.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.VISIBLE);

        Phone phone = getPhone(groupMessage.getFrom());
        String contactName = $(NameHandler.class).getName(phone);

        JsonObject action = jsonObject.get("action").getAsJsonObject();
        holder.name.setVisibility(View.VISIBLE);
        holder.name.setText(contactName + " " + action.get("intent").getAsString());
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));

        String comment = action.get("comment").getAsString();
        if ($(Val.class).isEmpty(comment)) {
            holder.message.setVisibility(View.GONE);
        } else {
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setGravity(Gravity.START);
            holder.message.setText(action.get("comment").getAsString());
        }
        holder.action.setVisibility(View.GONE);

        holder.action.setVisibility(View.VISIBLE);
        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.reply));
        holder.action.setOnClickListener(view -> $(PhoneMessagesHandler.class).openMessagesWithPhone(phone.getId(), contactName, comment));
    }

    public void displayGroupMessage(GroupMessagesAdapter.GroupMessageViewHolder holder, GroupMessage groupMessage) {
        holder.eventMessage.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.VISIBLE);

        holder.action.setVisibility(View.GONE);
        holder.name.setVisibility(View.VISIBLE);
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.START);

        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));

        Phone phone = null;

        if (groupMessage.getFrom() != null) {
            phone = getPhone(groupMessage.getFrom());
        }

        holder.name.setText($(NameHandler.class).getName(phone));
        holder.message.setText($(GroupMessageParseHandler.class).parseText(groupMessage.getText()));
    }

    public void displayMessage(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage) {
        holder.name.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.GONE);
        holder.eventMessage.setVisibility(View.VISIBLE);
        holder.eventMessage.setText(jsonObject.get("message").getAsString());
        holder.itemView.setOnClickListener(null);
        holder.action.setVisibility(View.GONE);
    }

    public void displayEvent(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage, GroupMessagesAdapter.OnEventClickListener onEventClickListener) {
        holder.eventMessage.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.VISIBLE);

        final Event event = $(JsonHandler.class).from(jsonObject.get("event"), Event.class);

        holder.name.setVisibility(View.VISIBLE);
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));

        Phone phone = getPhone(groupMessage.getFrom());

        String contactName = $(NameHandler.class).getName(phone);

        holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.phone_shared_an_event, contactName));

        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.START);
        holder.message.setText(
                (event.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.unknown) : event.getName()) +
                        "\n" +
                        $(EventDetailsHandler.class).formatEventDetails(event));

        holder.action.setVisibility(View.VISIBLE);
        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.open_event));
        holder.action.setOnClickListener(view -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    public void displaySuggestion(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage, GroupMessagesAdapter.OnSuggestionClickListener onSuggestionClickListener) {
        holder.eventMessage.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.VISIBLE);

        final Suggestion suggestion = $(JsonHandler.class).from(jsonObject.get("suggestion"), Suggestion.class);

        boolean suggestionHasNoName = suggestion == null || suggestion.getName() == null || suggestion.getName().isEmpty();

        holder.name.setVisibility(View.VISIBLE);
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));

        Phone phone = getPhone(groupMessage.getFrom());

        String contactName = $(NameHandler.class).getName(phone);

        if (suggestionHasNoName) {
            holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.phone_shared_a_location, contactName));
        } else {
            holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.phone_shared_a_suggestion, contactName));
        }

        if (suggestionHasNoName) {
            holder.message.setVisibility(View.GONE);
        } else {
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setGravity(Gravity.START);
            holder.message.setText(suggestion.getName());
        }

        holder.action.setVisibility(View.VISIBLE);
        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.show_on_map));
        holder.action.setOnClickListener(view -> {
            if (onSuggestionClickListener != null) {
                onSuggestionClickListener.onSuggestionClick(suggestion);
            }
        });
    }

    public void displayPhoto(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage) {
        holder.eventMessage.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.VISIBLE);

        Phone phone = getPhone(groupMessage.getFrom());
        String contactName = $(NameHandler.class).getName(phone);

        final String photo = jsonObject.get("photo").getAsString() + "?s=500";
        holder.name.setVisibility(View.VISIBLE);
        holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.phone_shared_a_photo, contactName));
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));
        holder.message.setVisibility(View.GONE);
        holder.action.setVisibility(View.GONE);// or Share / save photo?
        holder.photo.setVisibility(View.VISIBLE);
        holder.photo.setOnClickListener(view -> $(PhotoActivityTransitionHandler.class).show(view, photo));
        $(ImageHandler.class).get().cancelRequest(holder.photo);
        $(ImageHandler.class).get().load(photo).transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0)).into(holder.photo);
    }

    public void displayFallback(GroupMessagesAdapter.GroupMessageViewHolder holder, GroupMessage groupMessage) {
        holder.eventMessage.setVisibility(View.GONE);
        holder.messageLayout.setVisibility(View.VISIBLE);

        holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.unknown));
        holder.message.setText("");
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));
    }

    public void display(GroupMessagesAdapter.GroupMessageViewHolder holder, GroupMessage groupMessage,
                        GroupMessagesAdapter.OnEventClickListener onEventClickListener,
                        GroupMessagesAdapter.OnSuggestionClickListener onSuggestionClickListener) {
        if (groupMessage.getAttachment() != null) {
            try {
                JsonObject jsonObject = $(JsonHandler.class).from(groupMessage.getAttachment(), JsonObject.class);
                if (jsonObject.has("action")) {
                    displayAction(holder, jsonObject, groupMessage);
                } else if (jsonObject.has("message")) {
                    displayMessage(holder, jsonObject, groupMessage);
                } else if (jsonObject.has("event")) {
                    displayEvent(holder, jsonObject, groupMessage, onEventClickListener);
                } else if (jsonObject.has("suggestion")) {
                    displaySuggestion(holder, jsonObject, groupMessage, onSuggestionClickListener);
                } else if (jsonObject.has("photo")) {
                    displayPhoto(holder, jsonObject, groupMessage);
                } else if (jsonObject.has("share")) {
                    displayShare(holder, jsonObject, groupMessage, onEventClickListener, onSuggestionClickListener);
                } else {
                    displayFallback(holder, groupMessage);
                }
            } catch (JsonSyntaxException e) {
                displayFallback(holder, groupMessage);
                e.printStackTrace();
            }
        } else {
            displayGroupMessage(holder, groupMessage);
        }
    }

    private Phone getPhone(String phoneId) {
        if (phoneId == null) {
            return null;
        }

        return $(StoreHandler.class).getStore().box(Phone.class).query()
                .equal(Phone_.id, phoneId)
                .build()
                .findFirst();
    }

    private String getTimeString(Date date) {
        if (date == null) {
            return "-";
        }

        if (new Date().getTime() - date.getTime() < 5 * MINUTE_IN_MILLIS) {
            return $(ResourcesHandler.class).getResources().getString(R.string.just_now);
        }

        if (DateUtils.isToday(date.getTime())) {
            return DateUtils.getRelativeTimeSpanString(date.getTime()).toString();
        }

        return DateUtils.getRelativeDateTimeString(
                $(ApplicationHandler.class).getApp(),
                date.getTime(),
                MINUTE_IN_MILLIS,
                WEEK_IN_MILLIS,
                0
        ).toString();
    }
}
