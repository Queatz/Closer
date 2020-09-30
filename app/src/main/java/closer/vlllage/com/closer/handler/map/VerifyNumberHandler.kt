package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.queatz.on.On

class VerifyNumberHandler constructor(private val on: On) {

    fun verify() {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.send_code_layout
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.send_code)
            textViewId = R.id.input
            onTextViewSubmitCallback = { sendPhoneNumber(it) }
            title = on<ResourcesHandler>().resources.getString(R.string.enter_your_phone_number)
            message = on<ResourcesHandler>().resources.getString(R.string.automatic)
            show()
        }
    }

    private fun sendPhoneNumber(phoneNumber: String) {
        on<DisposableHandler>().add(on<ApiHandler>().setPhoneNumber(phoneNumber).subscribe({ result ->
            if (!result.success) {
                on<DefaultAlerts>().thatDidntWork()
            }
        }, { error -> on<DefaultAlerts>().thatDidntWork() }))

        getCodeFromUser(phoneNumber)
    }

    private fun getCodeFromUser(phoneNumber: String) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.verify_number_layout
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.verify_number)
            textViewId = R.id.input
            onTextViewSubmitCallback = { verificationCode -> sendVerificationCode(phoneNumber, verificationCode) }
            title = on<ResourcesHandler>().resources.getString(R.string.wait_for_it)
            show()
        }
    }

    private fun sendVerificationCode(phoneNumber: String, verificationCode: String) {
        on<DisposableHandler>().add(on<ApiHandler>().sendVerificationCode(verificationCode).subscribe({ result ->
            if (result.verified) {
                codeConfirmed()
                confirmToken(result.token)
            } else {
                codeNotConfirmed(phoneNumber)
            }
        }, { codeNotConfirmed(phoneNumber) }))
    }

    private fun codeNotConfirmed(phoneNumber: String) {
        on<AlertHandler>().make().apply {
            message = on<ResourcesHandler>().resources.getString(R.string.number_not_verified)
            title = on<ResourcesHandler>().resources.getString(R.string.oh_no)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.resend_code)
            positiveButtonCallback = { result -> sendPhoneNumber(phoneNumber) }
            show()
        }
    }

    private fun codeConfirmed() {
        on<PersistenceHandler>().access = true
        on<PersistenceHandler>().isVerified = true
        on<MyGroupsLayoutActionsHandler>().showVerifyMyNumber(false)

        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.welcome_to_closer)
            message = on<ResourcesHandler>().resources.getString(R.string.number_verified)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.yaay)
            show()
        }
    }

    private fun confirmToken(token: String?) {
        if (on<AccountHandler>().phone != token && token != null) {
            on<AccountHandler>().updatePhone(token)
        }
    }
}
