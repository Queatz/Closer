package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.JsonHandler;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.StoreHandler;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupContact_;
import closer.vlllage.com.closer.store.models.GroupMessage;
import closer.vlllage.com.closer.store.models.Suggestion;

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
                    holder.message.setGravity(Gravity.CENTER_HORIZONTAL);
                    holder.message.setText(jsonObject.get("message").getAsString());
                    holder.message.setAlpha(.5f);
                    holder.itemView.setOnClickListener(null);
                    holder.action.setVisibility(View.GONE);
                } else if (jsonObject.has("suggestion")) {
                    final Suggestion suggestion = $(JsonHandler.class).from(jsonObject.get("suggestion"), Suggestion.class);

                    boolean suggestionHasNoName = suggestion == null || suggestion.getName() == null || suggestion.getName().isEmpty();

                    holder.name.setVisibility(View.VISIBLE);

                    GroupContact groupContact = $(StoreHandler.class).getStore().box(GroupContact.class).query()
                            .equal(GroupContact_.id, groupMessage.getContactId())
                            .build()
                            .findFirst();

                    String contactName;

                    if (groupContact == null || groupContact.getContactName() == null) {
                        contactName = $(ResourcesHandler.class).getResources().getString(R.string.unknown);
                    } else {
                        contactName = groupContact.getContactName();
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

        holder.action.setVisibility(View.GONE);
        holder.name.setVisibility(View.VISIBLE);
        holder.message.setVisibility(View.VISIBLE);
        holder.message.setGravity(Gravity.START);

        GroupContact groupContact = $(StoreHandler.class).getStore().box(GroupContact.class).query()
                .equal(GroupContact_.id, groupMessage.getContactId())
                .build()
                .findFirst();

        if (groupContact == null || groupContact.getContactName() == null) {
            holder.name.setText($(ResourcesHandler.class).getResources().getString(R.string.unknown));
        } else {
            holder.name.setText(groupContact.getContactName());
        }

        holder.message.setText(groupMessage.getText());

        holder.message.setAlpha(groupMessage.isLocalOnly() ? .5f : 1f);

        holder.itemView.setOnClickListener(view -> {
            if (onMessageClickListener != null) {
                onMessageClickListener.onMessageClick(groupMessages.get(position));
            }
        });
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

        public GroupMessageViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            action = itemView.findViewById(R.id.action);
        }
    }

    public interface OnMessageClickListener {
        void onMessageClick(GroupMessage message);
    }

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Suggestion suggestion);
    }
}
