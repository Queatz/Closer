package closer.vlllage.com.closer

import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolApplication
import com.google.firebase.FirebaseApp

class App : PoolApplication() {
    override fun onCreate() {
        super.onCreate()
        `$`(ApplicationHandler::class.java).app = this
        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)
        FirebaseApp.initializeApp(this)
    }
}
