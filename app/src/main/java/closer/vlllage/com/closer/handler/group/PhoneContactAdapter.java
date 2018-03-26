package closer.vlllage.com.closer.handler.group;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

public class PhoneContactAdapter extends PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder> {

    private final List<PhoneContact> contacts;

    public PhoneContactAdapter(PoolMember poolMember, List<PhoneContact> contacts) {
        super(poolMember);
        this.contacts = contacts;
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
    }

    @Override
    public int getItemCount() {
        return contacts.size();
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
}
