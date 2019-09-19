package closer.vlllage.com.closer.handler.group

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolActivityFragment
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
                on<GroupContactsHandler>().attach(group, contactsRecyclerView, searchContacts, showPhoneContactsButton)
            }
        }

        disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            searchContacts.setTextColor(it.text)
            searchContacts.setHintTextColor(it.hint)
            searchContacts.setBackgroundResource(it.clickableRoundedBackground)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
