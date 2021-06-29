package closer.vlllage.com.closer

import android.content.Context
import at.bluesource.choicesdk.core.ChoiceSdk
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PushHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.pool.PoolApplication
import com.google.firebase.FirebaseApp
import java.io.File


class App : PoolApplication() {

    override fun onCreate() {
        super.onCreate()

        val googleBug = getSharedPreferences("google_bug_154855417", Context.MODE_PRIVATE)

        if (!googleBug.contains("fixed")) {
            val corruptedZoomTables = File(filesDir, "ZoomTables.data")
            corruptedZoomTables.delete()
            googleBug.edit().putBoolean("fixed", true).apply()
        }

        on<ApplicationHandler>().app = this
        on<ApiHandler>().setAuthorization(on<AccountHandler>().phone)
        ChoiceSdk.init(this)
        FirebaseApp.initializeApp(this)
        on<PushHandler>().init()
    }
}
