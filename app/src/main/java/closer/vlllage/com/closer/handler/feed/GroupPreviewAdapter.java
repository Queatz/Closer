package closer.vlllage.com.closer.handler.feed;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMemberHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesAdapter;
import closer.vlllage.com.closer.handler.helpers.ActivityHandler;
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.DistanceHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.handler.map.MapActivityHandler;
import closer.vlllage.com.closer.handler.map.MapHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.pool.TempPool;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import closer.vlllage.com.closer.ui.CombinedRecyclerAdapter;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;
import jp.wasabeef.picasso.transformations.BlurTransformation;

import static closer.vlllage.com.closer.pool.Pool.tempPool;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class GroupPreviewAdapter extends PoolRecyclerAdapter<GroupPreviewAdapter.ViewHolder> implements CombinedRecyclerAdapter.PrioritizedAdapter {

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

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query();
        holder.pool.$(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .equal(GroupMessage_.to, group.getId())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .transform(groupMessages -> groupMessages.subList(0, min(groupMessages.size(), 5)))
                .observer(groupMessagesAdapter::setGroupMessages));

        holder.messagesRecyclerView.setAdapter(groupMessagesAdapter);
        holder.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(holder.messagesRecyclerView.getContext(), LinearLayoutManager.VERTICAL, true));

        holder.replyMessage.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                $(KeyboardHandler.class).showViewAboveKeyboard(view);
            }
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

        if (group.hasEvent()) {
            holder.itemView.setBackgroundResource(R.drawable.color_red_rounded);
        } else if (group.isPhysical()) {
            holder.itemView.setBackgroundResource(R.drawable.color_purple_rounded);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.color_green_rounded);
        }

        Picasso.get().cancelRequest(holder.backgroundPhoto);
        if (group.getPhoto() != null) {
            holder.backgroundPhoto.setVisibility(View.VISIBLE);
            holder.backgroundPhoto.setImageDrawable(null);
            Picasso.get().load(group.getPhoto() + "?s=32")
                    .noPlaceholder()
                    .transform(new BlurTransformation($(ActivityHandler.class).getActivity(), 2))
                    .into(holder.backgroundPhoto);
        } else {
            holder.backgroundPhoto.setVisibility(View.GONE);
        }
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
                } else if (newPosition < HEADER_COUNT) {
                    return true;
                }

                return false;
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
        ImageButton sendButton;
        EditText replyMessage;
        ImageView backgroundPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            messagesRecyclerView = itemView.findViewById(R.id.messagesRecyclerView);
            sendButton = itemView.findViewById(R.id.sendButton);
            replyMessage = itemView.findViewById(R.id.replyMessage);
            backgroundPhoto = itemView.findViewById(R.id.backgroundPhoto);
        }
    }
}
