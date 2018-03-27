package closer.vlllage.com.closer.handler;

import android.widget.TextView;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class SetNameHandler extends PoolMember {
    public void modifyName() {
        $(AlertHandler.class).makeAlert()
                .setLayoutResId(R.layout.set_name_modal)
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.update_name))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.update_name))
                .setTextView(R.id.input, value -> $(AccountHandler.class).updateName(value))
                .setOnAfterViewCreated(view -> ((TextView) view.findViewById(R.id.input)).setText($(AccountHandler.class).getName()))
                .show();
    }
}
