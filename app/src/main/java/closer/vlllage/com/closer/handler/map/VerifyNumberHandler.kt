package closer.vlllage.com.closer.handler.map

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.pool.PoolMember

class VerifyNumberHandler : PoolMember() {

    fun verify() {
        `$`(AlertHandler::class.java).make().apply {
            layoutResId = R.layout.send_code_layout
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.send_code)
            textViewId = R.id.input
            onTextViewSubmitCallback = { sendPhoneNumber(it) }
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.enter_your_phone_number)
            message = `$`(ResourcesHandler::class.java).resources.getString(R.string.automatic)
            show()
        }
    }

    private fun sendPhoneNumber(phoneNumber: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).setPhoneNumber(phoneNumber).subscribe({ result ->
            if (!result.success) {
                `$`(DefaultAlerts::class.java).thatDidntWork()
            }
        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))

        getCodeFromUser(phoneNumber)
    }

    private fun getCodeFromUser(phoneNumber: String) {
        `$`(AlertHandler::class.java).make().apply {
            layoutResId = R.layout.verify_number_layout
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.verify_number)
            textViewId = R.id.input
            onTextViewSubmitCallback = { verificationCode -> sendVerificationCode(phoneNumber, verificationCode) }
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.wait_for_it)
            show()
        }
    }

    private fun sendVerificationCode(phoneNumber: String, verificationCode: String) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).sendVerificationCode(verificationCode).subscribe({ result ->
            if (result.success) {
                codeConfirmed()
            } else {
                codeNotConfirmed(phoneNumber)
            }
        }, { codeNotConfirmed(phoneNumber) }))
    }

    private fun codeNotConfirmed(phoneNumber: String) {
        `$`(AlertHandler::class.java).make().apply {
            message = `$`(ResourcesHandler::class.java).resources.getString(R.string.number_not_verified)
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.oh_no)
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.resend_code)
            positiveButtonCallback = { result -> sendPhoneNumber(phoneNumber) }
            show()
        }
    }

    private fun codeConfirmed() {
        `$`(PersistenceHandler::class.java).isVerified = true
        `$`(MyGroupsLayoutActionsHandler::class.java).showVerifyMyNumber(false)

        `$`(AlertHandler::class.java).make().apply {
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.welcome_to_closer)
            message = `$`(ResourcesHandler::class.java).resources.getString(R.string.number_verified)
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.yaay)
            show()
        }
    }
}
