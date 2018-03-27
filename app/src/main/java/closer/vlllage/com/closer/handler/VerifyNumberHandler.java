package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class VerifyNumberHandler extends PoolMember {
    public void verify() {
        $(AlertHandler.class).makeAlert(String.class)
            .setLayoutResId(R.layout.send_code_layout)
            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.close))
            .setPositiveButtonCallback(result -> sendCode())
            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.send_code))
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.enter_your_phone_number))
            .show();
    }

    private void sendCode() {
        $(AlertHandler.class).makeAlert(String.class)
            .setLayoutResId(R.layout.verify_number_layout)
            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.close))
            .setPositiveButtonCallback(result -> codeConfirmed())
            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.verify_number))
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.wait_for_it))
            .show();
    }

    private void codeConfirmed() {
        $(AlertHandler.class).makeAlert()
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.close))
                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.number_verified))
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.welcome_to_closer))
                .show();
    }
}
