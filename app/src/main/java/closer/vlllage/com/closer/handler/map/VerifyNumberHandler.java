package closer.vlllage.com.closer.handler.map;

import closer.vlllage.com.closer.R;
import closer.vlllage.com.closer.handler.data.ApiHandler;
import closer.vlllage.com.closer.handler.data.PersistenceHandler;
import closer.vlllage.com.closer.handler.helpers.AlertHandler;
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts;
import closer.vlllage.com.closer.handler.helpers.DisposableHandler;
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler;
import closer.vlllage.com.closer.pool.PoolMember;

public class VerifyNumberHandler extends PoolMember {

    public void verify() {
        $(AlertHandler.class).make()
            .setLayoutResId(R.layout.send_code_layout)
            .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.send_code))
            .setTextView(R.id.input, this::sendPhoneNumber)
            .setTitle($(ResourcesHandler.class).getResources().getString(R.string.enter_your_phone_number))
            .setMessage($(ResourcesHandler.class).getResources().getString(R.string.automatic))
            .show();
    }

    private void sendPhoneNumber(String phoneNumber) {
        $(DisposableHandler.class).add($(ApiHandler.class).setPhoneNumber(phoneNumber).subscribe(result -> {
            if (!result.success) {
                $(DefaultAlerts.class).thatDidntWork();
            }
        }, error -> $(DefaultAlerts.class).thatDidntWork()));

        getCode();
    }

    private void getCode() {
        $(AlertHandler.class).make()
                .setLayoutResId(R.layout.verify_number_layout)
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.verify_number))
                .setTextView(R.id.input, this::sendVerificationCode)
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.wait_for_it))
                .show();
    }

    private void sendVerificationCode(String verificationCode) {
        $(DisposableHandler.class).add($(ApiHandler.class).sendVerificationCode(verificationCode).subscribe(result -> {
            if (result.success) {
                codeConfirmed();
            } else {
                codeNotConfirmed();
            }
        }, error -> codeNotConfirmed()));
    }

    private void codeNotConfirmed() {
        $(AlertHandler.class).make()
                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.number_not_verified))
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.oh_no))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.resend_code))
                .setPositiveButtonCallback(result -> getCode())
                .show();
    }

    private void codeConfirmed() {
        $(PersistenceHandler.class).setIsVerified(true);
        $(MyGroupsLayoutActionsHandler.class).showVerifyMyNumber(false);

        $(AlertHandler.class).make()
                .setTitle($(ResourcesHandler.class).getResources().getString(R.string.welcome_to_closer))
                .setMessage($(ResourcesHandler.class).getResources().getString(R.string.number_verified))
                .setPositiveButton($(ResourcesHandler.class).getResources().getString(R.string.yaay))
                .show();
    }
}
