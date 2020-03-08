package closer.vlllage.com.closer.handler.helpers

import android.content.Intent
import android.net.Uri
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.SuccessResult
import closer.vlllage.com.closer.api.models.UseInviteCodeResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import com.google.zxing.integration.android.IntentIntegrator
import com.queatz.on.On


class ScanQrCodeHandler constructor(private val on: On) {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                handleResult(result.contents)
            }
        }
    }

    fun handleResult(qrCode: String) {
        handleResult(Uri.parse(qrCode))
    }

    fun handleResult(url: Uri) {
        url.path ?: return

        on<DisposableHandler>().add(on<ApiHandler>().useInviteCode(url.path!!.split('/').last()).subscribe({ result: UseInviteCodeResult ->
            if (result.success) {
                on<ApiHandler>().getGroup(result.group!!).subscribe({ group ->
                    on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id, isNewMember = true)
                }, { on<DefaultAlerts>().thatDidntWork() })
            } else {
                on<DefaultAlerts>().thatDidntWork(result.error)
            }
        }) { on<DefaultAlerts>().thatDidntWork()})
    }

    fun scan() {
        IntentIntegrator(on<ActivityHandler>().activity).apply {
            setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            setPrompt("Scan an invite QR Code")
            setBeepEnabled(false)
        }.initiateScan();
    }
}