package closer.vlllage.com.closer.handler.group

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.InviteCodeResult
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.ui.RevealAnimator
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.fragment_group_contacts.*

class GroupContactsFragment : PoolActivityFragment() {

    private lateinit var disposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_group_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()

        val group = on<GroupHandler>().group!!

        on<GroupContactsHandler>().attach(group, contactsRecyclerView, searchContacts, showPhoneContactsButton)

        showPhoneContactsButton.setOnClickListener {
            if (on<PermissionHandler>().denied(Manifest.permission.READ_CONTACTS)) {
                on<AlertHandler>().make().apply {
                    title = on<ResourcesHandler>().resources.getString(R.string.enable_contacts_permission)
                    message = on<ResourcesHandler>().resources.getString(R.string.enable_contacts_permission_rationale)
                    positiveButton = on<ResourcesHandler>().resources.getString(R.string.open_settings)
                    positiveButtonCallback = { on<SystemSettingsHandler>().showSystemSettings() }
                    show()
                }
                return@setOnClickListener
            }

            on<PermissionHandler>().check(Manifest.permission.READ_CONTACTS).`when` { granted ->
                if (granted) {
                    on<GroupContactsHandler>().showContactsForQuery()
                    showPhoneContactsButton.visible = false
                }
            }
        }

        if (!on<PermissionHandler>().has(Manifest.permission.READ_CONTACTS)) {
            showPhoneContactsButton.visible = true
        }

        if (on<PermissionHandler>().has(Manifest.permission.READ_CONTACTS)) {
            on<GroupContactsHandler>().showContactsForQuery()
        }

        searchContacts.setText("")

        on<GroupHandler> {
            onGroupUpdated(disposableGroup) {
                updateQrCode(it)
                on<GroupContactsHandler>().attach(group, contactsRecyclerView, searchContacts, showPhoneContactsButton)
            }

            onGroupChanged {
                updateQrCode(it)
            }
        }

        disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            searchContacts.setTextColor(it.text)
            searchContacts.setHintTextColor(it.hint)
            inviteByQRCodeButton.imageTintList = it.tint
            inviteByQRCodeButton.setBackgroundResource(it.clickableRoundedBackground)
            qrCodeDescription.setTextColor(it.hint)
            searchContacts.setBackgroundResource(it.clickableRoundedBackground)
        })

        inviteByQRCodeButton.setOnClickListener {
            val show = qrCodeLayout.visible.not()
            val s = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.qr_code) +
                    on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padQuadruple) * 3
            RevealAnimator(qrCodeLayout, s).show(show)

            if (show) on<MiniWindowHandler>().expand()

            if (show) {
                generateQrCode()
            }
        }
    }

    private fun generateQrCode() {
        qrCode.setImageDrawable(null)

        on<GroupHandler>().group?.id?.let {
            on<DisposableHandler>().add(on<ApiHandler>().createInviteCode(it).subscribe({ inviteCodeResult: InviteCodeResult ->
                try {
                    val s = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.qr_code)
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.encodeBitmap("https://closer.group/invite/${inviteCodeResult.code}", BarcodeFormat.QR_CODE, s, s)
                    qrCode.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    on<DefaultAlerts>().thatDidntWork(on<ResourcesHandler>().resources.getString(R.string.failed_to_generate_qr_code))
                }
            }, { on<DefaultAlerts>().thatDidntWork() }))
        }
    }

    private fun updateQrCode(group: Group) {
        qrCodeDescription.text = on<ResourcesHandler>().resources.getString(R.string.scan_this_qr_code_to_join, group.name ?: on<ResourcesHandler>().resources.getString(R.string.generic_group))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
