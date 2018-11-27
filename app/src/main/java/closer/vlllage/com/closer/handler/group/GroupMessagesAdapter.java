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
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Event;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Group_;
import closer.vlllage.com.closer.store.models.Suggestion;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;
import closer.vlllage.com.closer.ui.RevealAnimator;

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
            } else {
                toggleMessageActionLayout(holder);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            toggleMessageActionLayout(holder);
            return true;
        });

        holder.photo.setOnLongClickListener(view -> {
            toggleMessageActionLayout(holder);
            return true;
        });

        holder.messageActionShare.setOnClickListener(view -> toggleMessageActionLayout(holder));
        holder.messageActionRemind.setOnClickListener(view -> toggleMessageActionLayout(holder));
        holder.messageActionPin.setOnClickListener(view -> toggleMessageActionLayout(holder));
        holder.messageActionVote.setOnClickListener(view -> toggleMessageActionLayout(holder));

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

    private void toggleMessageActionLayout(GroupMessageViewHolder holder) {
        if (holder.messageActionLayoutRevealAnimator == null) {
            holder.messageActionLayoutRevealAnimator = new RevealAnimator(holder.messageActionLayout, (int) ($(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f));
        }

        if (holder.messageActionLayout.getVisibility() == View.VISIBLE) {
            holder.messageActionLayoutRevealAnimator.show(false);
        } else {
            holder.messageActionLayoutRevealAnimator.show(true);
        }
    }

    @Override
    public void onViewRecycled(@NonNull GroupMessageViewHolder holder) {
        if (holder.messageActionLayoutRevealAnimator != null) {
            holder.messageActionLayoutRevealAnimator.cancel();
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
        MaxSizeFrameLayout messageActionLayout;
        View messageActionReply;
        View messageActionShare;
        View messageActionRemind;
        View messageActionPin;
        View messageActionVote;
        RevealAnimator messageActionLayoutRevealAnimator;

        public GroupMessageViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            action = itemView.findViewById(R.id.action);
            time = itemView.findViewById(R.id.time);
            group = itemView.findViewById(R.id.group);
            photo = itemView.findViewById(R.id.photo);
            messageActionLayout = itemView.findViewById(R.id.messageActionLayout);
            messageActionReply = itemView.findViewById(R.id.messageActionReply);
            messageActionShare = itemView.findViewById(R.id.messageActionShare);
            messageActionRemind = itemView.findViewById(R.id.messageActionReminder);
            messageActionPin = itemView.findViewById(R.id.messageActionPin);
            messageActionVote = itemView.findViewById(R.id.messageActionVote);
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
