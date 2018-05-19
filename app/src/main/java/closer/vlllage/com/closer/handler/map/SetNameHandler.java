package closer.vlllage.com.closer.handler.map;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.AccountHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class SetNameHandler extends PoolMember {
    public void modifyName() {
        modifyName(null, false);
    }
    public void modifyName(final OnNameModifiedCallback onNameModifiedCallback, boolean allowSkip) {
        $(AlertHandler.class).make()
                .setLayoutResId(R.layout.set_name_modal)
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.update_your_name))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.update_your_name))
                .setNegativeButton(allowSkip ? $(ResourcesHandler.class).getResources().getString(R.string.skip) : null)
                .setNegativeButtonCallback(allowSkip ? result -> {
                    if (onNameModifiedCallback != null) {
                        onNameModifiedCallback.onNameModified(null);
                    }
                } : null)
                .setTextView(R.id.input, name -> {
                    $(AccountHandler.class).updateName(name);
                    if (onNameModifiedCallback != null) {
                        onNameModifiedCallback.onNameModified(name);
                    }
                })
                .setOnAfterViewCreated((alertConfig, view) -> {
                    ((TextView) view.findViewById(R.id.input)).setText($(AccountHandler.class).getName());

                    if (allowSkip) {
                        view.findViewById(R.id.optionalText).setVisibility(View.VISIBLE);
                    }

                })
                .show();
    }

    public interface OnNameModifiedCallback {
        void onNameModified(@Nullable String name);
    }
}
