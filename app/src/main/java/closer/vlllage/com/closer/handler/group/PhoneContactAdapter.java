package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.GroupInvite;

public class PhoneContactAdapter extends PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder> {

    private String phoneNumber = null;
    private final List<GroupInvite> invites = new ArrayList<>();
    private final List<PhoneContact> contacts = new ArrayList<>();
    private OnPhoneContactClickListener onPhoneContactClickListener;
    private OnGroupInviteClickListener onGroupInviteClickListener;

    public PhoneContactAdapter(PoolMember poolMember, OnPhoneContactClickListener onPhoneContactClickListener, OnGroupInviteClickListener onGroupInviteClickListener) {
        super(poolMember);
        this.onPhoneContactClickListener = onPhoneContactClickListener;
        this.onGroupInviteClickListener = onGroupInviteClickListener;
    }

    @NonNull
    @Override
    public PhoneContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhoneContactViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phone_contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneContactViewHolder holder, int position) {
        PhoneContact contact;

        if (position < getSuggestionCount()) {
            if (phoneNumber != null && position == 0) {
                contact = new PhoneContact(null, phoneNumber);
            } else {
                GroupInvite invite = invites.get(position - (phoneNumber == null ? 0 : 1));

                holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.cancel_invite));
                holder.name.setText(invite.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.invite) : invite.getName());
                holder.number.setText($(ResourcesHandler.class).getResources().getString(R.string.invited));
                holder.itemView.setOnClickListener(view -> {
                    if (onGroupInviteClickListener != null) {
                        onGroupInviteClickListener.onGroupInviteClicked(invite);
                    }
                });

                return;
            }
        } else {
            contact = contacts.get(position - getSuggestionCount());
        }

        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.invite));
        holder.name.setText(contact.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.invite_by_phone) : contact.getName());
        holder.number.setText(contact.getPhoneNumber() == null ? $(ResourcesHandler.class).getResources().getString(R.string.unknown) : contact.getPhoneNumber());
        holder.itemView.setOnClickListener(view -> {
            if (onPhoneContactClickListener != null) {
                onPhoneContactClickListener.onPhoneContactClicked(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size() + getSuggestionCount();
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        notifyDataSetChanged();
    }

    public void setContacts(List<PhoneContact> contacts) {
        this.contacts.clear();
        this.contacts.addAll(contacts);
        notifyDataSetChanged();
    }

    public void setInvites(List<GroupInvite> invites) {
        this.invites.clear();
        this.invites.addAll(invites);
        notifyDataSetChanged();
    }

    private int getSuggestionCount() {
        return (phoneNumber == null ? 0 : 1) + invites.size();
    }

    class PhoneContactViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;
        TextView action;

        public PhoneContactViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            action = itemView.findViewById(R.id.action);
        }
    }

    public interface OnPhoneContactClickListener {
        void onPhoneContactClicked(PhoneContact phoneContact);
    }

    public interface OnGroupInviteClickListener {
        void onGroupInviteClicked(GroupInvite groupInvite);
    }
}
