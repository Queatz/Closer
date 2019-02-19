package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.RefreshHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.phone.NameHandler;
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler;
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler;
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
    private OnGroupClickListener onGroupClickListener;
    private boolean pinned;

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

    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener) {
        this.onGroupClickListener = onGroupClickListener;
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

        if (pinned) {
            holder.itemView.setPadding(0, 0, 0, 0);
            holder.itemView.setBackgroundResource(R.color.white_15);
            holder.messageLayout.setBackground(null);
            int pad = $(ResourcesHandler.class).getResources().getDimensionPixelSize(R.dimen.pad);
            holder.messageActionLayout.setPadding(
                    pad,
                    0,
                    pad,
                    pad
            );

            ViewGroup.MarginLayoutParams params;
            params = ((ViewGroup.MarginLayoutParams) holder.messageLayout.getLayoutParams());
            params.topMargin = 0;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.messageLayout.setLayoutParams(params);

            holder.reactionsRecyclerView.setPadding(pad, 0, pad, 0);

            params = ((ViewGroup.MarginLayoutParams) holder.action.getLayoutParams());
            params.leftMargin = pad;
            params.bottomMargin = pad;
            holder.action.setLayoutParams(params);
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

        holder.messageActionReply.setOnClickListener(view -> {
            $(PhoneMessagesHandler.class).openMessagesWithPhone(groupMessage.getFrom(), $(NameHandler.class).getName(groupMessage.getFrom()), "");
            toggleMessageActionLayout(holder);
        });
        holder.messageActionShare.setOnClickListener(view -> {
            $(ShareActivityTransitionHandler.class).shareGroupMessage(groupMessage.getId());
            toggleMessageActionLayout(holder);
        });
        holder.messageActionRemind.setOnClickListener(view -> {
            $(DefaultAlerts.class).message("That doesn't work yet!");
            toggleMessageActionLayout(holder);
        });
        holder.messageActionPin.setOnClickListener(view -> {
            $(DefaultAlerts.class).message("That doesn't work yet!");
            toggleMessageActionLayout(holder);
        });
        holder.messageActionVote.setOnClickListener(view -> {
            $(ApplicationHandler.class).getApp().$(DisposableHandler.class).add($(ApiHandler.class)
                    .reactToMessage(groupMessage.getId(), "♥", false)
                    .subscribe(successResult -> {
                        $(RefreshHandler.class).refreshGroupMessage(groupMessage.getId());
                    }, error -> $(DefaultAlerts.class).thatDidntWork()));
            toggleMessageActionLayout(holder);
        });

        holder.messageActionLayout.setVisibility(View.GONE);

        $(MessageDisplay.class).setPinned(pinned);
        $(MessageDisplay.class).display(holder, groupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener);

        if (groupMessage.getReactions() == null || groupMessage.getReactions().isEmpty()) {
            holder.reactionsRecyclerView.setVisibility(View.GONE);
        } else {
            holder.reactionsRecyclerView.setVisibility(View.VISIBLE);
            holder.reactionAdapter.setItems(groupMessage.getReactions());
            holder.reactionAdapter.setGroupMessage(groupMessage);
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

    public GroupMessagesAdapter setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    protected class GroupMessageViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView eventMessage;
        View messageLayout;
        TextView message;
        TextView action;
        TextView time;
        TextView group;
        ImageView photo;
        ImageView pinnedIndicator;
        MaxSizeFrameLayout messageActionLayout;
        TextView messageActionReply;
        TextView messageActionShare;
        TextView messageActionRemind;
        TextView messageActionPin;
        TextView messageActionVote;
        RevealAnimator messageActionLayoutRevealAnimator;
        RecyclerView reactionsRecyclerView;
        ReactionAdapter reactionAdapter;

        public GroupMessageViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            eventMessage = itemView.findViewById(R.id.eventMessage);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            message = itemView.findViewById(R.id.message);
            action = itemView.findViewById(R.id.action);
            time = itemView.findViewById(R.id.time);
            group = itemView.findViewById(R.id.group);
            photo = itemView.findViewById(R.id.photo);
            pinnedIndicator = itemView.findViewById(R.id.pinnedIndicator);
            reactionsRecyclerView = itemView.findViewById(R.id.reactionsRecyclerView);
            messageActionLayout = itemView.findViewById(R.id.messageActionLayout);
            messageActionReply = itemView.findViewById(R.id.messageActionReply);
            messageActionShare = itemView.findViewById(R.id.messageActionShare);
            messageActionRemind = itemView.findViewById(R.id.messageActionReminder);
            messageActionPin = itemView.findViewById(R.id.messageActionPin);
            messageActionVote = itemView.findViewById(R.id.messageActionVote);

            reactionsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false));
            reactionAdapter = new ReactionAdapter($pool());
            reactionsRecyclerView.setAdapter(reactionAdapter);
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

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }
}
