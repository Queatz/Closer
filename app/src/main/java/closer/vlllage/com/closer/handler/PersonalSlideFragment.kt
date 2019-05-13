package closer.vlllage.com.closer.handler

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMember
import closer.vlllage.com.closer.store.models.GroupMember_
import closer.vlllage.com.closer.store.models.Group_
import closer.vlllage.com.closer.ui.Animate
import io.objectbox.android.AndroidScheduler
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.util.*

class PersonalSlideFragment : PoolFragment() {

    private lateinit var yourCurrentStatus: EditText
    private lateinit var yourName: TextView
    private lateinit var yourPhoto: ImageButton
    private lateinit var shareYourLocationSwitch: Switch
    private var previousStatus: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)

        val view = inflater.inflate(R.layout.activity_personal, container, false)

        val subscribedGroupsRecyclerView = view.findViewById<RecyclerView>(R.id.subscribedGroupsRecyclerView)
        val youveSubscribedEmpty = view.findViewById<TextView>(R.id.youveSubscribedEmpty)

        val searchGroupsAdapter = SearchGroupsAdapter(`$`(GroupHandler::class.java), { group, v -> `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(v, group.id) }, null)

        searchGroupsAdapter.setActionText(`$`(ResourcesHandler::class.java).resources.getString(R.string.open_group))
        searchGroupsAdapter.setIsSmall(true)
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_large_padding)

        subscribedGroupsRecyclerView.adapter = searchGroupsAdapter
        subscribedGroupsRecyclerView.layoutManager = LinearLayoutManager(subscribedGroupsRecyclerView.context)

        `$`(DisposableHandler::class.java).add(`$`(StoreHandler::class.java).store.box(GroupMember::class.java).query()
                .equal(GroupMember_.phone, `$`(Val::class.java).of(`$`(PersistenceHandler::class.java).phoneId))
                .equal(GroupMember_.subscribed, true)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupMembers ->
                    if (groupMembers.isEmpty()) {
                        youveSubscribedEmpty.visibility = View.VISIBLE
                        searchGroupsAdapter.setGroups(ArrayList())
                    } else {
                        youveSubscribedEmpty.visibility = View.GONE

                        val ids = HashSet<String>()
                        for (groupMember in groupMembers) {
                            ids.add(groupMember.group!!)
                        }

                        `$`(StoreHandler::class.java).findAll(Group::class.java, Group_.id, ids, `$`(SortHandler::class.java).sortGroups()).observer { searchGroupsAdapter.setGroups(it) }
                    }
                })

        yourCurrentStatus = view.findViewById(R.id.currentStatus)
        shareYourLocationSwitch = view.findViewById(R.id.shareYourLocationSwitch)
        yourName = view.findViewById(R.id.yourName)
        yourPhoto = view.findViewById(R.id.yourPhoto)

        updateLocationInfo()

        shareYourLocationSwitch.setOnCheckedChangeListener { switchView, isChecked ->
            `$`(AccountHandler::class.java).updateActive(isChecked)
            updateLocationInfo()
        }

        shareYourLocationSwitch.isChecked = `$`(AccountHandler::class.java).active
        previousStatus = `$`(AccountHandler::class.java).status
        yourCurrentStatus.setText(previousStatus)

        yourCurrentStatus.setOnFocusChangeListener { editTextView, isFocused ->
            if (yourCurrentStatus.text.toString() == previousStatus) {
                return@setOnFocusChangeListener
            }

            `$`(AccountHandler::class.java).updateStatus(yourCurrentStatus.text.toString())
            `$`(KeyboardHandler::class.java).showKeyboard(yourCurrentStatus, false)
        }

        yourName.text = `$`(Val::class.java).of(`$`(AccountHandler::class.java).name, `$`(ResourcesHandler::class.java).resources.getString(R.string.update_your_name))

        yourPhoto.setOnClickListener { v ->
            `$`(DefaultMenus::class.java).uploadPhoto { photoId ->
                val photo = `$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId)
                `$`(AccountHandler::class.java).updatePhoto(photo)
            }
        }

        if (!`$`(Val::class.java).isEmpty(`$`(PersistenceHandler::class.java).myPhoto)) {
            `$`(ImageHandler::class.java).get().load(`$`(PersistenceHandler::class.java).myPhoto + "?s=128")
                    .noPlaceholder()
                    .transform(CropCircleTransformation())
                    .into(yourPhoto)
        }

        `$`(DisposableHandler::class.java).add(`$`(AccountHandler::class.java).changes().subscribe(
                { accountChange ->
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_NAME) {
                        yourName.text = `$`(AccountHandler::class.java).name
                    }
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_PHOTO) {
                        `$`(PhotoHelper::class.java).loadCircle(yourPhoto, `$`(PersistenceHandler::class.java).myPhoto + "?s=128")
                    }
                },
                { `$`(DefaultAlerts::class.java).thatDidntWork() }
        ))

        yourName.setOnClickListener { v -> `$`(SetNameHandler::class.java).modifyName() }

        yourName.requestFocus()

        return view
    }

    private fun updateLocationInfo() {
        `$`(Animate::class.java).alpha(yourCurrentStatus, shareYourLocationSwitch.isChecked)
    }

}
