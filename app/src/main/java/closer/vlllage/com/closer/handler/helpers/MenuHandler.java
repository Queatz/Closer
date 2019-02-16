package closer.vlllage.com.closer.handler.helpers;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class MenuHandler extends PoolMember {
    public void show(MenuOption... menuOptions) {
        $(AlertHandler.class).make()
                .setLayoutResId(R.layout.menu_modal)
                .setOnAfterViewCreated((alertConfig, view) -> {
                    RecyclerView menuRecyclerView = view.findViewById(R.id.menuRecyclerView);
                    menuRecyclerView.setLayoutManager(new LinearLayoutManager($(ActivityHandler.class).getActivity(), LinearLayoutManager.VERTICAL, false));
                    List<MenuOption> options = new ArrayList<>();

                    for (MenuOption option : menuOptions) {
                        if (option.visible) options.add(option);
                    }

                    menuRecyclerView.setAdapter(new MenuOptionAdapter(options, menuOption -> {
                        menuOption.callback.run();
                        alertConfig.getDialog().dismiss();
                    }));
                })
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.close))
                .show();
    }

    public static class MenuOption {

        @DrawableRes int iconRes;
        @StringRes int titleRes;
        @NonNull Runnable callback;
        boolean visible = true;

        public MenuOption(@DrawableRes int iconRes, @StringRes int titleRes, @NonNull Runnable callback) {
            this.iconRes = iconRes;
            this.titleRes = titleRes;
            this.callback = callback;
        }

        public MenuOption visible(boolean visible) {
            this.visible = visible;
            return this;
        }
    }
}
