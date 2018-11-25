package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;
import closer.vlllage.com.closer.store.models.GroupContact;
import closer.vlllage.com.closer.store.models.GroupInvite;

public class PhoneContactAdapter extends PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder> {

    private String phoneNumber = null;
    private boolean isFiltered;
    private final List<GroupInvite> invites = new ArrayList<>();
    private final List<GroupContact> groupContacts = new ArrayList<>();
    private final List<PhoneContact> contacts = new ArrayList<>();
    private OnPhoneContactClickListener onPhoneContactClickListener;
    private OnGroupInviteClickListener onGroupInviteClickListener;
    private OnGroupContactClickListener onGroupContactClickListener;

    public PhoneContactAdapter(PoolMember poolMember,
                               OnPhoneContactClickListener onPhoneContactClickListener,
                               OnGroupInviteClickListener onGroupInviteClickListener,
                               OnGroupContactClickListener onGroupContactClickListener) {
        super(poolMember);
        this.onPhoneContactClickListener = onPhoneContactClickListener;
        this.onGroupInviteClickListener = onGroupInviteClickListener;
        this.onGroupContactClickListener = onGroupContactClickListener;
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

        if (position < getMemberAndInviteCount()) {
            if (phoneNumber != null) {
                contact = new PhoneContact(null, phoneNumber);
            } else {
                if (position < groupContacts.size()) {
                    GroupContact groupContact = groupContacts.get(position);

                    holder.phoneIcon.setImageResource(R.drawable.ic_person_black_24dp);
                    boolean isMe = $(PersistenceHandler.class).getPhoneId().equals(groupContact.getContactId());
                    holder.name.setText(groupContact.getContactName());
                    holder.action.setText(isMe ? "" : $(ResourcesHandler.class).getResources().getString(R.string.send_message));
                    holder.number.setText($(ResourcesHandler.class).getResources().getString(isMe ? R.string.member_you : R.string.member));
                    holder.itemView.setOnClickListener(view -> {
                        if (onGroupContactClickListener != null) {
                            onGroupContactClickListener.onGroupContactClicked(groupContact);
                        }
                    });
                } else {
                    position -= groupContacts.size();
                    GroupInvite invite = invites.get(position);

                    holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp);
                    holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.cancel_invite));
                    holder.name.setText(invite.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.invite) : invite.getName());
                    holder.number.setText($(ResourcesHandler.class).getResources().getString(R.string.invited));
                    holder.itemView.setOnClickListener(view -> {
                        if (onGroupInviteClickListener != null) {
                            onGroupInviteClickListener.onGroupInviteClicked(invite);
                        }
                    });
                }

                return;
            }
        } else {
            contact = contacts.get(position - getMemberAndInviteCount());
        }

        holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp);
        holder.action.setText($(ResourcesHandler.class).getResources().getString(R.string.invite));
        holder.name.setText(contact.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.invite_by_phone) : contact.getName());
        holder.number.setText(contact.getPhoneNumber() == null ? $(ResourcesHandler.class).getResources().getString(R.string.no_name) : contact.getPhoneNumber());
        holder.itemView.setOnClickListener(view -> {
            if (onPhoneContactClickListener != null) {
                onPhoneContactClickListener.onPhoneContactClicked(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size() + getMemberAndInviteCount();
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

    public void setGroupContacts(List<GroupContact> groupContacts) {
        this.groupContacts.clear();
        this.groupContacts.addAll(groupContacts);
        notifyDataSetChanged();
    }

    private int getMemberAndInviteCount() {
        return isFiltered ? (phoneNumber == null ? 0 : 1) : groupContacts.size() + invites.size();
    }

    public void setIsFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    class PhoneContactViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;
        TextView action;
        ImageView phoneIcon;

        public PhoneContactViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
            phoneIcon = itemView.findViewById(R.id.phoneIcon);
            action = itemView.findViewById(R.id.action);
        }
    }

    public interface OnPhoneContactClickListener {
        void onPhoneContactClicked(PhoneContact phoneContact);
    }

    public interface OnGroupInviteClickListener {
        void onGroupInviteClicked(GroupInvite groupInvite);
    }

    public interface OnGroupContactClickListener {
        void onGroupContactClicked(GroupContact groupContact);
    }
}
