package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class SetNameHandler extends PoolMember {
    public void modifyName() {
        $(AlertHandler.class).showAlert(R.layout.set_name_modal, R.string.update_name, 0,
                $(AccountHandler.class).getName(), input -> $(AccountHandler.class).updateName(input));
    }
}
