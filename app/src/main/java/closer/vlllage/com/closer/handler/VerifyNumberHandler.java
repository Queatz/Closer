package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class VerifyNumberHandler extends PoolMember {
    public void verify() {
        $(AlertHandler.class).showAlert(R.layout.verify_number_layout, R.string.verify_number, R.string.verify_number, null, input -> {

        });
    }
}
