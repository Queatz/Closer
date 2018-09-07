package closer.vlllage.com.closer.handler.helpers;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import closer.vlllage.com.closer.R;

class MenuOptionAdapter extends RecyclerView.Adapter<MenuOptionAdapter.ViewHolder> {

    private List<MenuHandler.MenuOption> menuOptions;
    private OnMenuOptionClickListener onMenuOptionClickListener;

    public MenuOptionAdapter(
            @NonNull List<MenuHandler.MenuOption> menuOptions,
            @NonNull OnMenuOptionClickListener onMenuOptionClickListener) {
        this.menuOptions = menuOptions;
        this.onMenuOptionClickListener = onMenuOptionClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_modal_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        MenuHandler.MenuOption menuOption = menuOptions.get(position);
        viewHolder.name.setText(menuOption.titleRes);
        viewHolder.name.setCompoundDrawablesWithIntrinsicBounds(menuOption.iconRes, 0, 0, 0);
        viewHolder.itemView.setOnClickListener(view -> onMenuOptionClickListener.onMenuOptionClick(menuOption));
    }

    @Override
    public int getItemCount() {
        return menuOptions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
        }
    }

    interface OnMenuOptionClickListener {
        void onMenuOptionClick(MenuHandler.MenuOption menuOption);
    }
}
