package closer.vlllage.com.closer.handler.group;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter;

class ToolbarAdapter extends PoolRecyclerAdapter<ToolbarAdapter.ToolbarViewHolder> {

    private List<GroupToolbarHandler.ToolbarItem> items = new ArrayList<>();

    public ToolbarAdapter(PoolMember poolMember) {
        super(poolMember);
    }

    @NonNull
    @Override
    public ToolbarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToolbarViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.toolbar_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToolbarViewHolder viewHolder, int position) {
        GroupToolbarHandler.ToolbarItem item = items.get(position);

        viewHolder.button.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, item.icon, 0, 0
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewHolder.button.setCompoundDrawableTintList(ColorStateList.valueOf(
                    $(ResourcesHandler.class).getResources().getColor(R.color.text)
            ));
        }

        viewHolder.button.setText(item.name);

        viewHolder.button.setOnClickListener(item.onClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<GroupToolbarHandler.ToolbarItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    static class ToolbarViewHolder extends RecyclerView.ViewHolder {
        final Button button;

        public ToolbarViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
        }
    }
}
