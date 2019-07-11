package closer.vlllage.com.closer.handler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMember
import closer.vlllage.com.closer.store.models.GroupMember_
import closer.vlllage.com.closer.store.models.Group_
import io.objectbox.android.AndroidScheduler
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.activity_personal.*
import java.util.*

class PersonalSlideFragment : PoolFragment() {

    private var previousStatus: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val subscribedGroupsRecyclerView = view.findViewById<RecyclerView>(R.id.subscribedGroupsRecyclerView)
        val youveSubscribedEmpty = view.findViewById<TextView>(R.id.youveSubscribedEmpty)

        val searchGroupsAdapter = SearchGroupsAdapter(on, false, { group, v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, group.id) }, null)

        searchGroupsAdapter.setActionText(on<ResourcesHandler>().resources.getString(R.string.open_group))
        searchGroupsAdapter.setIsSmall(true)
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_large_padding)

        subscribedGroupsRecyclerView.adapter = searchGroupsAdapter
        subscribedGroupsRecyclerView.layoutManager = LinearLayoutManager(subscribedGroupsRecyclerView.context)

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMember::class).query()
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

        shareYourLocationSwitch.setOnCheckedChangeListener { switchView, isChecked ->
            on<AccountHandler>().updateActive(isChecked)
        }

        shareYourLocationSwitch.isChecked = on<AccountHandler>().active
        previousStatus = on<AccountHandler>().status
        currentStatus.setText(previousStatus)

        currentStatus.setOnFocusChangeListener { editTextView, isFocused ->
            if (currentStatus.text.toString() == previousStatus) {
                return@setOnFocusChangeListener
            }

            on<AccountHandler>().updateStatus(currentStatus.text.toString())
            on<KeyboardHandler>().showKeyboard(currentStatus, false)
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

        actionViewProfile.setOnClickListener {
            on<NavigationHandler>().showProfile(on<PersistenceHandler>().phoneId!!, actionViewProfile)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_personal, container, false)
    }

}
