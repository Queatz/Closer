package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.JsonHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Suggestion;

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
            holder.itemView.setElevation($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.padDouble));
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
                    $(MessageDisplay.class).displayAction(holder, jsonObject, groupMessage);
                } else if (jsonObject.has("message")) {
                    $(MessageDisplay.class).displayMessage(holder, jsonObject, groupMessage);
                } else if (jsonObject.has("event")) {
                    $(MessageDisplay.class).displayEvent(holder, jsonObject, groupMessage, onEventClickListener);
                } else if (jsonObject.has("suggestion")) {
                    $(MessageDisplay.class).displaySuggestion(holder, jsonObject, groupMessage, onSuggestionClickListener);
                } else if (jsonObject.has("photo")) {
                    $(MessageDisplay.class).displayPhoto(holder, jsonObject, groupMessage);
                } else {
                    $(MessageDisplay.class).displayFallback(holder, groupMessage);
                }
            } catch (JsonSyntaxException e) {
                $(MessageDisplay.class).displayFallback(holder, groupMessage);
                e.printStackTrace();
            }
        } else {
            $(MessageDisplay.class).displayGroupMessage(holder, isReversed, groupMessage, groupMessages, position, getItemCount());
        }
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

    private Group getGroup(String groupId) {
        if (groupId == null) {
            return null;
        }

        return $(StoreHandler.class).getStore().box(Group.class).query()
                .equal(Group_.id, groupId)
                .build()
                .findFirst();
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
