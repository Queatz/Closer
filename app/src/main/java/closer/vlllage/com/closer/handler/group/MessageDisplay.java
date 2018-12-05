package closer.vlllage.com.closer.handler.group;

import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.View;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.JsonHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.store.models.Suggestion;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class MessageDisplay extends PoolMember {

    public void displayAction(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage) {
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
            holder.message.setAlpha(1f);
            holder.message.setText(action.get("comment").getAsString());
        }
        holder.action.setVisibility(View.GONE);

        holder.action.setVisibility(View.VISIBLE);
        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.reply));
        holder.action.setOnClickListener(view -> $(PhoneMessagesHandler.class).openMessagesWithPhone(phone.getId(), contactName, comment));
    }

    public void displayGroupMessage(GroupMessagesAdapter.GroupMessageViewHolder holder, boolean isReversed, GroupMessage groupMessage, List<GroupMessage> groupMessages, int position, int itemCount) {
        int pairIndex = position + (isReversed ? -1 : 1);
        boolean validIndex = pairIndex >= 0 && pairIndex < itemCount;

        boolean previousMessageIsSameContact = validIndex &&
                groupMessages.get(pairIndex).getAttachment() == null &&
                groupMessages.get(pairIndex).getFrom() != null &&
                groupMessages.get(pairIndex).getFrom().equals(groupMessage.getFrom());

        boolean previousMessageIsSameGroup = validIndex &&
                groupMessages.get(pairIndex).getTo() != null &&
                groupMessages.get(pairIndex).getTo().equals(groupMessage.getTo());

        boolean previousMessageIsSameTime = validIndex &&
                groupMessages.get(pairIndex).getAttachment() == null &&
                getTimeString(groupMessages.get(pairIndex).getTime()).equals(getTimeString(groupMessage.getTime()));

        holder.action.setVisibility(View.GONE);
        holder.name.setVisibility(previousMessageIsSameContact && previousMessageIsSameGroup ? View.GONE : View.VISIBLE);
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.START);

        if (previousMessageIsSameTime) {
            holder.time.setVisibility(View.GONE);
        } else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(getTimeString(groupMessage.getTime()));
        }

        Phone phone = null;

        if (groupMessage.getFrom() != null) {
            phone = getPhone(groupMessage.getFrom());
        }

        holder.name.setText($(NameHandler.class).getName(phone));
        holder.message.setText($(GroupMessageParseHandler.class).parse(groupMessage.getText(), mention -> {
            List<Phone> phoneList = $(StoreHandler.class).getStore().box(Phone.class).find(Phone_.id, mention);
            if (phoneList.isEmpty()) {
                return $(ResourcesHandler.class).getResources().getString(R.string.unknown);
            }
            return $(NameHandler.class).getName(phoneList.get(0));
        }, mention -> {
            List<Phone> phoneList = $(StoreHandler.class).getStore().box(Phone.class).find(Phone_.id, mention);
            String name;
            if (phoneList.isEmpty()) {
                name = $(ResourcesHandler.class).getResources().getString(R.string.unknown);
            } else {
                name = $(NameHandler.class).getName(phoneList.get(0));
            }

            $(PhoneMessagesHandler.class).openMessagesWithPhone(mention, name, "");
        }));
        holder.message.setAlpha(groupMessage.isLocalOnly() ? .5f : 1f);
    }

    public void displayMessage(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage) {
        holder.name.setVisibility(View.GONE);
        holder.time.setVisibility(View.GONE);
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.CENTER_HORIZONTAL);
        holder.message.setText(jsonObject.get("message").getAsString());
        holder.message.setAlpha(.5f);
        holder.itemView.setOnClickListener(null);
        holder.action.setVisibility(View.GONE);
    }

    public void displayEvent(GroupMessagesAdapter.GroupMessageViewHolder holder, JsonObject jsonObject, GroupMessage groupMessage, GroupMessagesAdapter.OnEventClickListener onEventClickListener) {
        final Event event = $(JsonHandler.class).from(jsonObject.get("event"), Event.class);

        holder.name.setVisibility(View.VISIBLE);
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));

        Phone phone = getPhone(groupMessage.getFrom());

        String contactName = $(NameHandler.class).getName(phone);

        holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.phone_shared_an_event, contactName));

        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.START);
        holder.message.setAlpha(1f);
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
            holder.message.setAlpha(1f);
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
        Picasso.get().cancelRequest(holder.photo);
        Picasso.get().load(photo).transform(new RoundedCornersTransformation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.imageCorners), 0)).into(holder.photo);
    }

    public void displayFallback(GroupMessagesAdapter.GroupMessageViewHolder holder, GroupMessage groupMessage) {
        holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.unknown));
        holder.message.setText("");
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(getTimeString(groupMessage.getTime()));
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
            return "";
        }

        if (new Date().getTime() - date.getTime() < 5 * MINUTE_IN_MILLIS) {
            return "";
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
