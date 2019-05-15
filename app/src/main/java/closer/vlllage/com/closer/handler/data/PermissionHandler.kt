package closer.vlllage.com.closer.handler.data

import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import com.queatz.on.On
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.*

class PermissionHandler constructor(private val on: On) {
    private val permissionChanges = PublishSubject.create<String>()

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_CODE_PERMISSION) {
            return
        }

        permissions.forEach { permissionChanges.onNext(it) }
    }

    fun check(vararg permissions: String): LocationCheck {
        val check: LocationCheck

        if (has(*permissions)) {
            check = LocationCheck(true)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            check = LocationCheck(arrayOf(*permissions))
            on<ActivityHandler>().activity!!.requestPermissions(permissions, REQUEST_CODE_PERMISSION)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            check = LocationCheck(arrayOf(*permissions))
            ActivityCompat.requestPermissions(on<ActivityHandler>().activity!!, permissions, REQUEST_CODE_PERMISSION)
        } else {
            check = LocationCheck(has(*permissions))
        }

        return check
    }

    fun has(vararg permissions: String): Boolean {
        val context = on<ApplicationHandler>().app

        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    fun denied(vararg permissions: String): Boolean {
        val activity = on<ActivityHandler>().activity

        if (has(*permissions)) {
            return false
        }

        for (permission in permissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission)) {
                return true
            }
        }

        return false
    }

    inner class LocationCheck {

        private var override: Boolean? = null
        private var permissions: Array<String> = arrayOf()
        private var permissionsGranted = HashSet<String>()
        private var callback: ((granted: Boolean) -> Unit)? = null
        private var disposable: Disposable? = null

        constructor(permissions: Array<String>) {
            this.permissions = permissions
        }

        constructor(granted: Boolean) {
            override = granted
        }

        fun `when`(callback: ((granted: Boolean) -> Unit)) {
            if (override != null) {
                callback.invoke(override!!)
                return
            }

            if (permissions.isEmpty()) {
                callback.invoke(true)
                return
            }

            this.callback = callback

            subscribe()
        }

        private fun subscribe() {
            disposable = permissionChanges.filter { permission ->
                for (p in permissions) {
                    if (p == permission) {
                        return@filter true
                    }
                }

                false
            }.subscribe(
                    { permission ->
                        if (has(permission)) {
                            permissionsGranted.add(permission)
                            if (permissionsGranted.size == permissions.size) {
                                callback!!.invoke(true)
                                on<DisposableHandler>().dispose(disposable!!)
                            }
                        } else {
                            callback!!.invoke(false)
                            on<DisposableHandler>().dispose(disposable!!)
                        }
                    }, { error -> on<DefaultAlerts>().thatDidntWork() })

            on<DisposableHandler>().add(disposable!!)
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSION = 1009293
    }
}
