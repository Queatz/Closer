package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ApplicationHandler;
import closer.vlllage.com.closer.handler.JsonHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Phone;
import closer.vlllage.com.closer.store.models.Phone_;
import closer.vlllage.com.closer.store.models.Suggestion;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.WEEK_IN_MILLIS;

public class GroupMessagesAdapter extends PoolRecyclerAdapter<GroupMessagesAdapter.GroupMessageViewHolder> {

    private List<GroupMessage> groupMessages = new ArrayList<>();
    private OnMessageClickListener onMessageClickListener;
    private OnSuggestionClickListener onSuggestionClickListener;

    public GroupMessagesAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    public void setOnMessageClickListener(OnMessageClickListener onMessageClickListener) {
        this.onMessageClickListener = onMessageClickListener;
    }

    public void setOnSuggestionClickListener(OnSuggestionClickListener onSuggestionClickListener) {
        this.onSuggestionClickListener = onSuggestionClickListener;
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

        if (groupMessage.getAttachment() != null) {
            try {
                JsonObject jsonObject = $(JsonHandler.class).from(groupMessage.getAttachment(), JsonObject.class);
                if (jsonObject.has("message")) {
                    holder.name.setVisibility(View.GONE);
                    holder.time.setVisibility(View.GONE);
                    holder.message.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.message.setText(jsonObject.get("message").getAsString());
                    holder.message.setAlpha(.5f);
                    holder.itemView.setOnClickListener(null);
                    holder.action.setVisibility(View.GONE);
                } else if (jsonObject.has("suggestion")) {
                    final Suggestion suggestion = $(JsonHandler.class).from(jsonObject.get("suggestion"), Suggestion.class);

                    boolean suggestionHasNoName = suggestion == null || suggestion.getName() == null || suggestion.getName().isEmpty();

                    holder.name.setVisibility(View.VISIBLE);
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(getTimeString(groupMessage.getTime()));

                    Phone phone = getPhone(groupMessage.getFrom());

                    String contactName;

                    if (phone == null || phone.getName() == null) {
                        contactName = $(ResourcesHandler.class).getResources().getString(R.string.unknown);
                    } else {
                        contactName = phone.getName();
                    }

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
                }

                return;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        boolean previousMessageIsSameContact = position + 1 < getItemCount() - 1 &&
                groupMessages.get(position + 1).getAttachment() == null &&
                groupMessages.get(position + 1).getFrom() != null &&
                groupMessages.get(position + 1).getFrom().equals(groupMessage.getFrom());

        holder.action.setVisibility(View.GONE);
        holder.name.setVisibility(previousMessageIsSameContact ? View.GONE : View.VISIBLE);
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.START);
        holder.time.setVisibility(View.VISIBLE);

        holder.time.setText(getTimeString(groupMessage.getTime()));

        Phone phone = null;

        if (groupMessage.getFrom() != null) {
            phone = getPhone(groupMessage.getFrom());
        }

        if (phone == null || phone.getName() == null) {
            holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.unknown));
        } else {
            holder.name.setText(phone.getName());
        }

        holder.message.setText(groupMessage.getText());

        holder.message.setAlpha(groupMessage.isLocalOnly() ? .5f : 1f);

        holder.itemView.setOnClickListener(view -> {
            if (onMessageClickListener != null) {
                onMessageClickListener.onMessageClick(groupMessages.get(position));
            }
        });
    }

    private Phone getPhone(String phoneId) {
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

    @Override
    public int getItemCount() {
        return groupMessages.size();
    }

    public void setGroupMessages(List<GroupMessage> groupMessages) {
        this.groupMessages = groupMessages;
        notifyDataSetChanged();
    }

    class GroupMessageViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView message;
        TextView action;
        TextView time;

        public GroupMessageViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            action = itemView.findViewById(R.id.action);
            time = itemView.findViewById(R.id.time);
        }
    }

    public interface OnMessageClickListener {
        void onMessageClick(GroupMessage message);
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Suggestion suggestion);
    }
}
