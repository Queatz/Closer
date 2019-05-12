package closer.vlllage.com.closer.handler.feed;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMemberHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageMentionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessageParseHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesAdapter;
import closer.vlllage.com.closer.handler.group.PinnedMessagesHandler;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.DistanceHandler;
import closer.vlllage.com.closer.handler.helpers.GroupColorHandler;
import closer.vlllage.com.closer.handler.helpers.GroupScopeHandler;
import closer.vlllage.com.closer.handler.helpers.ImageHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.PhotoLoader;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.map.HeaderAdapter;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.handler.map.MapHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.TempPool;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.handler.group.GroupDraftHandler;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.ui.CombinedRecyclerAdapter;
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

import static closer.vlllage.com.closer.pool.Pool.tempPool;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class GroupPreviewAdapter extends HeaderAdapter<GroupPreviewAdapter.ViewHolder> implements CombinedRecyclerAdapter.PrioritizedAdapter {

    private static final int HEADER_COUNT = 1;
    private final List<Group> groups = new ArrayList<>();

    public GroupPreviewAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutResId;
        switch (viewType) {
            case 1: layoutResId = R.layout.feed_item_public_groups; break;
            default: layoutResId = R.layout.group_preview_item; break;
        }
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.pool = tempPool();

        if (position < HEADER_COUNT) {
            holder.pool.$set($(StoreHandler.class));
            holder.pool.$set($(SyncHandler.class));
            holder.pool.$set($(MapHandler.class));
            holder.pool.$set($(ApplicationHandler.class));
            holder.pool.$set($(ActivityHandler.class));
            holder.pool.$set($(SortHandler.class));
            holder.pool.$set($(KeyboardHandler.class));
            holder.pool.$set($(GroupMemberHandler.class));
            holder.pool.$(PublicGroupFeedItemHandler.class).attach(holder.itemView);
            return;
        } else {
            holder.pool.$set($(ApiHandler.class));
            holder.pool.$set($(ApplicationHandler.class));
            holder.pool.$set($(ActivityHandler.class));
            holder.pool.$set($(ResourcesHandler.class));
            position--;
        }

        Group group = groups.get(position);
        holder.groupName.setText($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));
        holder.groupName.setOnClickListener(view -> $(GroupActivityTransitionHandler.class).showGroupMessages(holder.groupName, group.getId()));
        holder.groupName.setOnLongClickListener(view -> {
            $(GroupMemberHandler.class).changeGroupSettings(group);
            return true;
        });

        GroupMessagesAdapter groupMessagesAdapter = new GroupMessagesAdapter($pool());
        groupMessagesAdapter.setOnSuggestionClickListener(suggestion -> $(MapActivityHandler.class).showSuggestionOnMap(suggestion));
        groupMessagesAdapter.setOnEventClickListener(event -> $(GroupActivityTransitionHandler.class).showGroupForEvent(holder.itemView, event));
        groupMessagesAdapter.setOnGroupClickListener(group1 -> $(GroupActivityTransitionHandler.class).showGroupMessages(holder.itemView, group1.getId()));

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query();
        holder.pool.$(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .equal(GroupMessage_.to, group.getId())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .transform(groupMessages -> groupMessages.subList(0, min(groupMessages.size(), 5)))
                .observer(groupMessagesAdapter::setGroupMessages));

        holder.pool.$(PinnedMessagesHandler.class).attach(holder.pinnedMessagesRecyclerView);
        holder.pool.$(PinnedMessagesHandler.class).show(group);

        holder.messagesRecyclerView.setAdapter(groupMessagesAdapter);
        holder.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.messagesRecyclerView.getContext(), LinearLayoutManager.VERTICAL, true));

        if (holder.textWatcher != null) {
            holder.replyMessage.removeTextChangedListener(holder.textWatcher);
        }

        holder.replyMessage.setText($(GroupMessageParseHandler.class).parseText($(GroupDraftHandler.class).getDraft(group)));

        holder.textWatcher = new TextWatcher() {

            private boolean isDeleteMention;
            private boolean shouldDeleteMention;

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                shouldDeleteMention = !isDeleteMention && after == 0 && holder.pool.$(GroupMessageParseHandler.class).isMentionSelected(holder.replyMessage);
                isDeleteMention = false;
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable text) {
                $(GroupDraftHandler.class).saveDraft(group, text.toString());
                holder.pool.$(GroupMessageMentionHandler.class).showSuggestionsForName(holder.pool.$(GroupMessageParseHandler.class).extractName(text, holder.replyMessage.getSelectionStart()));

                if (shouldDeleteMention) {
                    isDeleteMention = true;
                    holder.pool.$(GroupMessageParseHandler.class).deleteMention(holder.replyMessage);
                }
            }
        };

        holder.replyMessage.addTextChangedListener(holder.textWatcher);

        holder.pool.$(GroupMessageMentionHandler.class).attach(holder.mentionSuggestionsLayout, holder.mentionSuggestionRecyclerView, mention -> {
            holder.pool.$(GroupMessageParseHandler.class).insertMention(holder.replyMessage, mention);
        });

        holder.replyMessage.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                holder.sendButton.callOnClick();
            }

            return false;
        });

        holder.sendButton.setOnClickListener(view -> {
            String message = holder.replyMessage.getText().toString();

            if (message.trim().isEmpty()) {
                return;
            }

            GroupMessage groupMessage = new GroupMessage();
            groupMessage.setText(message);
            groupMessage.setFrom($(PersistenceHandler.class).getPhoneId());
            groupMessage.setTo(group.getId());
            groupMessage.setTime(new Date());
            $(StoreHandler.class).getStore().box(GroupMessage.class).put(groupMessage);
            $(SyncHandler.class).sync(groupMessage);

            holder.replyMessage.setText("");
            $(KeyboardHandler.class).showKeyboard(view, false);
        });

        holder.backgroundColor.setBackgroundResource($(GroupColorHandler.class).getColorBackground(group));

        $(ImageHandler.class).get().cancelRequest(holder.backgroundPhoto);
        if (group.getPhoto() != null) {
            holder.backgroundPhoto.setVisibility(View.VISIBLE);
            holder.backgroundPhoto.setImageDrawable(null);
            $(PhotoLoader.class).softLoad(group.getPhoto(), holder.backgroundPhoto);
        } else {
            holder.backgroundPhoto.setVisibility(View.GONE);
        }

        $(GroupScopeHandler.class).setup(group, holder.scopeIndicatorButton);
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0: return 1;
            default: return 0;
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.pool.end();
    }

    @Override
    public int getItemCount() {
        return groups.size() + HEADER_COUNT;
    }

    public void setGroups(List<Group> groups) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return GroupPreviewAdapter.this.groups.size() + HEADER_COUNT;
            }

            @Override
            public int getNewListSize() {
                return groups.size() + HEADER_COUNT;
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                if ((newPosition < HEADER_COUNT) != (oldPosition < HEADER_COUNT)) {
                    return false;
                } else if (newPosition < HEADER_COUNT) {
                    return true;
                }

                return GroupPreviewAdapter.this.groups.get(oldPosition - 1).getObjectBoxId() ==
                        groups.get(newPosition - 1).getObjectBoxId();
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                if ((newPosition < HEADER_COUNT) != (oldPosition < HEADER_COUNT)) {
                    return false;
                } else return newPosition < HEADER_COUNT;

            }
        });
        this.groups.clear();
        this.groups.addAll(groups);
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemPriority(int position) {
        return max(0, position - ($(DistanceHandler.class).isUserNearGroup(groups.get(position - 1)) ? 100 : 0));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TempPool pool;

        TextView groupName;
        RecyclerView messagesRecyclerView;
        RecyclerView pinnedMessagesRecyclerView;
        ImageButton sendButton;
        EditText replyMessage;
        ImageView backgroundPhoto;
        ImageButton scopeIndicatorButton;
        MaxSizeFrameLayout mentionSuggestionsLayout;
        RecyclerView mentionSuggestionRecyclerView;
        View backgroundColor;

        TextWatcher textWatcher;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            messagesRecyclerView = itemView.findViewById(R.id.messagesRecyclerView);
            pinnedMessagesRecyclerView = itemView.findViewById(R.id.pinnedMessagesRecyclerView);
            sendButton = itemView.findViewById(R.id.sendButton);
            replyMessage = itemView.findViewById(R.id.replyMessage);
            backgroundPhoto = itemView.findViewById(R.id.backgroundPhoto);
            scopeIndicatorButton = itemView.findViewById(R.id.scopeIndicatorButton);
            mentionSuggestionsLayout = itemView.findViewById(R.id.mentionSuggestionsLayout);
            mentionSuggestionRecyclerView = itemView.findViewById(R.id.mentionSuggestionRecyclerView);
            backgroundColor = itemView.findViewById(R.id.backgroundColor);
        }
    }
}
