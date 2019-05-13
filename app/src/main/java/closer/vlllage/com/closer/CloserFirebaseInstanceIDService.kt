package closer.vlllage.com.closer

import closer.vlllage.com.closer.handler.data.AccountHandler
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class CloserFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val deviceToken = FirebaseInstanceId.getInstance().token
        (application as App).`$`(AccountHandler::class.java).updateDeviceToken(deviceToken!!)
    }
}
