package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.event.EventDetailsHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.JsonHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.store.models.Suggestion;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class GroupMessagesAdapter extends PoolRecyclerAdapter<GroupMessagesAdapter.GroupMessageViewHolder> {

    private List<GroupMessage> groupMessages = new ArrayList<>();
    private OnMessageClickListener onMessageClickListener;
    private OnSuggestionClickListener onSuggestionClickListener;
    private OnEventClickListener onEventClickListener;
    private boolean noPadding;
    private boolean isReversed;

    public GroupMessagesAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListener = onMessageClickListener;
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener onSuggestionClickListener) {
        this.onSuggestionClickListener = onSuggestionClickListener;
    }

    public void setOnEventClickListener(OnEventClickListener onEventClickListener) {
        this.onEventClickListener = onEventClickListener;
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupMessageViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageViewHolder holder, int position) {
        GroupMessage groupMessage = groupMessages.get(position);

        if (noPadding) {
            holder.itemView.setPadding(0, 0, 0, 0);
        }

        holder.photo.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(view -> {
            if (onMessageClickListener != null) {
                onMessageClickListener.onMessageClick(groupMessages.get(position));
            }
        });

        if (onMessageClickListener != null) {
            holder.itemView.setBackgroundResource(R.drawable.clickable_green_flat);
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
//            int pad = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.pad);
//            params.bottomMargin = pad;
//            params.leftMargin = pad;
//            params.rightMargin = pad;
//            holder.itemView.setPadding(
//                    holder.itemView.getPaddingLeft(),
//                    holder.itemView.getPaddingTop(),
//                    holder.itemView.getPaddingRight(),
//                    holder.itemView.getPaddingTop()
//            );
//            holder.itemView.setElevation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.elevation));

            holder.group.setVisibility(View.VISIBLE);
            Group group = getGroup(groupMessage.getTo());

            if (group == null) {
                holder.group.setText(R.string.near_here);
            } else {
                if ($(Val.class).isEmpty(group.getName())) {
                    holder.group.setText(R.string.on_map);
                } else {
                    holder.group.setText(group.getName());
                }
            }
        }

        if (groupMessage.getAttachment() != null) {
            try {
                JsonObject jsonObject = $(JsonHandler.class).from(groupMessage.getAttachment(), JsonObject.class);
                if (jsonObject.has("action")) {
                    Phone phone = getPhone(groupMessage.getFrom());
                    String contactName = $(NameHandler.class).getName(phone);

                    JsonObject action = jsonObject.get("action").getAsJsonObject();
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText(contactName + " " + action.get("intent").getAsString());
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(getTimeString(groupMessage.getTime()));
                    holder.message.setVisibility(View.VISIBLE);
                    holder.message.setText(action.get("comment").getAsString());
                    holder.itemView.setOnClickListener(null);
                    holder.action.setVisibility(View.GONE);

                    holder.action.setVisibility(View.VISIBLE);
                    holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.reply));
                    holder.action.setOnClickListener(view -> {
                        // todo
                    });
                } else if (jsonObject.has("message")) {
                    holder.name.setVisibility(View.GONE);
                    holder.time.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    holder.message.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.message.setText(jsonObject.get("message").getAsString());
                    holder.message.setAlpha(.5f);
                    holder.itemView.setOnClickListener(null);
                    holder.action.setVisibility(View.GONE);
                } else if (jsonObject.has("event")) {
                    final Event event = $(JsonHandler.class).from(jsonObject.get("event"), Event.class);

                    holder.name.setVisibility(View.VISIBLE);
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(getTimeString(groupMessage.getTime()));

                    Phone phone = getPhone(groupMessage.getFrom());

                    String contactName = $(NameHandler.class).getName(phone);

                    holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.phone_shared_an_event, contactName));

                    holder.message.setVisibility(View.VISIBLE);
                    holder.message.setText(
                            (event.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.unknown) : event.getName()) +
                                    "\n" +
                                    $(EventDetailsHandler.class).formatEventDetails(event));

                    holder.action.setVisibility(View.VISIBLE);
                    holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.show_on_map));
                    holder.action.setOnClickListener(view -> {
                        if (onEventClickListener != null) {
                            onEventClickListener.onEventClick(event);
                        }
                    });
                } else if (jsonObject.has("suggestion")) {
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
                        holder.message.setText(suggestion.getName());
                    }

                    holder.action.setVisibility(View.VISIBLE);
                    holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.show_on_map));
                    holder.action.setOnClickListener(view -> {
                        if (onSuggestionClickListener != null) {
                            onSuggestionClickListener.onSuggestionClick(suggestion);
                        }
                    });
                } else if (jsonObject.has("photo")) {
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
                } else {
                    holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.unknown));
                    holder.message.setText("");
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(getTimeString(groupMessage.getTime()));
                }

                return;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        int pairIndex = position + (isReversed ? -1 : 1);
        boolean validIndex = pairIndex >= 0 && pairIndex < getItemCount();

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
        holder.message.setText(groupMessage.getText());
        holder.message.setAlpha(groupMessage.isLocalOnly() ? .5f : 1f);
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

    private Group getGroup(String groupId) {
        if (groupId == null) {
            return null;
        }

        return $(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.id, groupId)
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

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }

    public void setGroupMessages(List<GroupMessage> groupMessages) {
        this.groupMessages = groupMessages;
        notifyDataSetChanged();
    }

    public GroupMessagesAdapter setNoPadding(boolean noPadding) {
        this.noPadding = noPadding;
        return this;
    }

    public GroupMessagesAdapter setReversed(boolean reversed) {
        isReversed = reversed;
        return this;
    }

    protected class GroupMessageViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView message;
        TextView action;
        TextView time;
        TextView group;
        ImageView photo;

        public GroupMessageViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            action = itemView.findViewById(R.id.action);
            time = itemView.findViewById(R.id.time);
            group = itemView.findViewById(R.id.group);
            photo = itemView.findViewById(R.id.photo);
        }
    }

    public interface OnMessageClickListener {
        void onMessageClick(GroupMessage message);
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Suggestion suggestion);
    }

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
}
