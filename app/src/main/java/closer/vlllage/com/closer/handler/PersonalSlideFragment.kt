package closer.vlllage.com.closer.handler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        on<ApiHandler>().setAuthorization(on<AccountHandler>().phone)

        val view = inflater.inflate(R.layout.activity_personal, container, false)

        val subscribedGroupsRecyclerView = view.findViewById<RecyclerView>(R.id.subscribedGroupsRecyclerView)
        val youveSubscribedEmpty = view.findViewById<TextView>(R.id.youveSubscribedEmpty)

        val searchGroupsAdapter = SearchGroupsAdapter(on, { group, v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, group.id) }, null)

        searchGroupsAdapter.setActionText(on<ResourcesHandler>().resources.getString(R.string.open_group))
        searchGroupsAdapter.setIsSmall(true)
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_large_padding)

        subscribedGroupsRecyclerView.adapter = searchGroupsAdapter
        subscribedGroupsRecyclerView.layoutManager = LinearLayoutManager(subscribedGroupsRecyclerView.context)

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMember::class.java).query()
                .equal(GroupMember_.phone, on<Val>().of(on<PersistenceHandler>().phoneId))
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

                        on<StoreHandler>().findAll(Group::class.java, Group_.id, ids, on<SortHandler>().sortGroups()).observer { searchGroupsAdapter.setGroups(it) }
                    }
                })

        yourCurrentStatus = view.findViewById(R.id.currentStatus)
        shareYourLocationSwitch = view.findViewById(R.id.shareYourLocationSwitch)
        yourName = view.findViewById(R.id.yourName)
        yourPhoto = view.findViewById(R.id.yourPhoto)

        updateLocationInfo()

        shareYourLocationSwitch.setOnCheckedChangeListener { switchView, isChecked ->
            on<AccountHandler>().updateActive(isChecked)
            updateLocationInfo()
        }

        shareYourLocationSwitch.isChecked = on<AccountHandler>().active
        previousStatus = on<AccountHandler>().status
        yourCurrentStatus.setText(previousStatus)

        yourCurrentStatus.setOnFocusChangeListener { editTextView, isFocused ->
            if (yourCurrentStatus.text.toString() == previousStatus) {
                return@setOnFocusChangeListener
            }

            on<AccountHandler>().updateStatus(yourCurrentStatus.text.toString())
            on<KeyboardHandler>().showKeyboard(yourCurrentStatus, false)
        }

        yourName.text = on<Val>().of(on<AccountHandler>().name, on<ResourcesHandler>().resources.getString(R.string.update_your_name))

        yourPhoto.setOnClickListener { v ->
            on<DefaultMenus>().uploadPhoto { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                on<AccountHandler>().updatePhoto(photo)
            }
        }

        if (!on<Val>().isEmpty(on<PersistenceHandler>().myPhoto)) {
            on<ImageHandler>().get().load(on<PersistenceHandler>().myPhoto + "?s=128")
                    .noPlaceholder()
                    .transform(CropCircleTransformation())
                    .into(yourPhoto)
        }

        on<DisposableHandler>().add(on<AccountHandler>().changes().subscribe(
                { accountChange ->
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_NAME) {
                        yourName.text = on<AccountHandler>().name
                    }
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_PHOTO) {
                        on<PhotoHelper>().loadCircle(yourPhoto, on<PersistenceHandler>().myPhoto + "?s=128")
                    }
                },
                { on<DefaultAlerts>().thatDidntWork() }
        ))

        yourName.setOnClickListener { v -> on<SetNameHandler>().modifyName() }

        yourName.requestFocus()

        return view
    }

    private fun updateLocationInfo() {
        on<Animate>().alpha(yourCurrentStatus, shareYourLocationSwitch.isChecked)
    }

}
