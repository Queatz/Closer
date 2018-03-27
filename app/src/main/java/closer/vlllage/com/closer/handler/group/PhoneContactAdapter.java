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

public class PhoneContactAdapter extends PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder> {

    private final List<PhoneContact> contacts = new ArrayList<>();
    private OnPhoneContactClickListener onPhoneContactClickListener;

    public PhoneContactAdapter(PoolMember poolMember, OnPhoneContactClickListener onPhoneContactClickListener) {
        super(poolMember);
        this.onPhoneContactClickListener = onPhoneContactClickListener;
    }

    @NonNull
    @Override
    public PhoneContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PhoneContactViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.phone_contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneContactViewHolder holder, int position) {
        PhoneContact contact = contacts.get(position);
        holder.name.setText(contact.getName() == null ? $(ResourcesHandler.class).getResources().getString(R.string.unknown) : contact.getName());
        holder.number.setText(contact.getPhoneNumber() == null ? $(ResourcesHandler.class).getResources().getString(R.string.unknown) : contact.getPhoneNumber());
        holder.itemView.setOnClickListener(view -> {
            if (onPhoneContactClickListener != null) {
                onPhoneContactClickListener.onPhoneContactClicked(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setContacts(List<PhoneContact> contacts) {
        this.contacts.clear();
        this.contacts.addAll(contacts);
        notifyDataSetChanged();
    }

    class PhoneContactViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView number;

        public PhoneContactViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            number = itemView.findViewById(R.id.number);
        }
    }

    public interface OnPhoneContactClickListener {
        void onPhoneContactClicked(PhoneContact phoneContact);
    }
}
