package closer.vlllage.com.closer.handler;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.pool.PoolMember;

public class VerifyNumberHandler extends PoolMember {
    public void verify() {
        $(AlertHandler.class).makeAlert(String.class)
            .setLayoutResId(R.layout.send_code_layout)
            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.send_code))
            .setPositiveButtonCallback(result -> sendCode())
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.enter_your_phone_number))
            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.automatic))
            .show();
    }

    private void sendCode() {
        $(AlertHandler.class).makeAlert(String.class)
            .setLayoutResId(R.layout.verify_number_layout)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.verify_number))
                .setPositiveButtonCallback(result -> codeConfirmed())
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.wait_for_it))
            .show();
    }

    private void codeNotConfirmed() {
        $(AlertHandler.class).makeAlert(String.class)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.resend_code))
                .setPositiveButtonCallback(result -> sendCode())
                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.number_not_verified))
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.oh_no))
                .show();
    }

    private void codeConfirmed() {
        $(AlertHandler.class).makeAlert()
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.yaay))
                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.number_verified))
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.welcome_to_closer))
                .show();
    }
}
