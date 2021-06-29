package closer.vlllage.com.closer

import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.PushHandler
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CloserFirebaseMessagingService : FirebaseMessagingService() {

    // XXX TODO ChoiceSDK might be able to delete this
    override fun onNewToken(p0: String) {
        val deviceToken = FirebaseInstanceId.getInstance().token
        (application as App).on<AccountHandler>().updateDeviceToken(deviceToken!!)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        (application as App).on<PushHandler>().onMessageReceived(data)
    }
}
