package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class VerifyNumberHandler extends PoolMember {
    public void verify() {
        $(AlertHandler.class).showAlert(R.layout.send_code_layout, R.string.send_code, R.string.enter_your_phone_number, null, input -> {
            sendCode();
        });
    }

    private void sendCode() {
        $(AlertHandler.class).showAlert(R.layout.verify_number_layout, R.string.verify_number, R.string.wait_for_it, null, input -> {
            codeConfirmed();
        });
    }

    private void codeConfirmed() {
        $(AlertHandler.class).showAlert(R.string.number_verified, R.string.welcome_to_closer, R.string.close, null);
    }
}
