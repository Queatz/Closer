package closer.vlllage.com.closer.handler.feed;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.data.SyncHandler;
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler;
import closer.vlllage.com.closer.handler.group.GroupMessagesAdapter;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.KeyboardHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.handler.helpers.SortHandler;
import closer.vlllage.com.closer.handler.helpers.Val;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.pool.TempPool;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.Group;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.GroupMessage_;
import io.objectbox.android.AndroidScheduler;
import io.objectbox.query.QueryBuilder;

import static closer.vlllage.com.closer.pool.Pool.tempPool;
import static java.lang.Math.min;

public class GroupPreviewAdapter extends PoolRecyclerAdapter<GroupPreviewAdapter.ViewHolder> {

    private final List<Group> groups = new ArrayList<>();


    public GroupPreviewAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_preview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.pool = tempPool();
        Group group = groups.get(position);
        viewHolder.groupName.setText($(Val.class).of(group.getName(), $(ResourcesHandler.class).getResources().getString(R.string.app_name)));
        viewHolder.groupName.setOnClickListener(view -> $(GroupActivityTransitionHandler.class).showGroupMessages(viewHolder.groupName, group.getId()));

        GroupMessagesAdapter groupMessagesAdapter = new GroupMessagesAdapter($pool());

        QueryBuilder<GroupMessage> queryBuilder = $(StoreHandler.class).getStore().box(GroupMessage.class).query();
        viewHolder.pool.$(DisposableHandler.class).add(queryBuilder
                .sort($(SortHandler.class).sortGroupMessages())
                .equal(GroupMessage_.to, group.getId())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .transform(groupMessages -> groupMessages.subList(0, min(groupMessages.size() - 1, 5)))
                .observer(groupMessagesAdapter::setGroupMessages));

        viewHolder.messagesRecyclerView.setAdapter(groupMessagesAdapter);
        viewHolder.messagesRecyclerView.setLayoutManager(new LinearLayoutManager(viewHolder.messagesRecyclerView.getContext(), LinearLayoutManager.VERTICAL, true));

        viewHolder.replyMessage.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                $(KeyboardHandler.class).showViewAboveKeyboard(view);
            }
        });

        viewHolder.replyMessage.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                viewHolder.sendButton.callOnClick();
            }

            return false;
        });

        viewHolder.sendButton.setOnClickListener(view -> {
            String message = viewHolder.replyMessage.getText().toString();

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

            viewHolder.replyMessage.setText("");
            $(KeyboardHandler.class).showKeyboard(view, false);
        });
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.pool.end();
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(List<Group> groups) {
        this.groups.clear();
        this.groups.addAll(groups);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TempPool pool;

        TextView groupName;
        RecyclerView messagesRecyclerView;
        ImageButton sendButton;
        EditText replyMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.groupName);
            messagesRecyclerView = itemView.findViewById(R.id.messagesRecyclerView);
            sendButton = itemView.findViewById(R.id.sendButton);
            replyMessage = itemView.findViewById(R.id.replyMessage);
        }
    }
}
