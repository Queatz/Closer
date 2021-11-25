package closer.vlllage.com.closer.handler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.group.SearchGroupsAdapter
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.NetworkConnectionViewHandler
import closer.vlllage.com.closer.handler.map.SetNameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.pool.PoolFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMember
import closer.vlllage.com.closer.store.models.GroupMember_
import closer.vlllage.com.closer.store.models.Group_
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder
import kotlinx.android.synthetic.main.activity_personal.*
import java.util.*

class PersonalSlideFragment : PoolFragment() {

    private var previousStatus: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        on<NetworkConnectionViewHandler>().attach(connectionError)

        val searchGroupsAdapter = SearchGroupsAdapter(on, false, { group, v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, group.id) }, null)

        searchGroupsAdapter.setActionText(on<ResourcesHandler>().resources.getString(R.string.open_group))
        searchGroupsAdapter.setIsSmall(true)
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_large_padding)

        subscribedGroupsRecyclerView.adapter = searchGroupsAdapter
        subscribedGroupsRecyclerView.layoutManager = LinearLayoutManager(subscribedGroupsRecyclerView.context)

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMember::class).query()
                .equal(GroupMember_.phone, on<Val>().trimmed(on<PersistenceHandler>().phoneId), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .equal(GroupMember_.subscribed, true)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupMembers ->
                    if (groupMembers.isEmpty()) {
                        youveSubscribedEmpty.visible = true
                        searchGroupsAdapter.setGroups(listOf())
                    } else {
                        youveSubscribedEmpty.visible = false

                        val ids = HashSet<String>()
                        for (groupMember in groupMembers) {
                            ids.add(groupMember.group!!)
                        }

                        on<StoreHandler>().findAll(Group::class.java, Group_.id, ids, on<SortHandler>().sortGroups()).observer { searchGroupsAdapter.setGroups(it) }
                    }
                })

        shareYourLocationSwitch.isChecked = on<AccountHandler>().active

        shareYourLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            on<AccountHandler>().updateActive(isChecked)

            if (isChecked) {
                on<DefaultAlerts>().message(R.string.your_sharing_location)
            }
        }

        previousStatus = on<AccountHandler>().status
        currentStatus.setText(previousStatus)

        currentStatus.setOnFocusChangeListener { _, _ ->
            if (currentStatus.text.toString() == previousStatus) {
                return@setOnFocusChangeListener
            }

            on<AccountHandler>().updateStatus(currentStatus.text.toString())
            on<KeyboardHandler>().showKeyboard(currentStatus, false)
        }

        yourName.text = on<Val>().of(on<AccountHandler>().name, on<ResourcesHandler>().resources.getString(R.string.update_your_name))

        yourPhoto.setOnClickListener {
            on<DefaultMenus>().uploadPhoto { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                on<AccountHandler>().updatePhoto(photo)
            }
        }

        if (!on<PersistenceHandler>().myPhoto.isBlank()) {
            on<ImageHandler>().get().load(on<PersistenceHandler>().myPhoto + "?s=128")
                    .apply(RequestOptions().circleCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
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
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_ACTIVE) {
                        shareYourLocationSwitch.isChecked = accountChange.value == true
                    }
                },
                { on<DefaultAlerts>().thatDidntWork() }
        ))

        yourName.setOnClickListener { v -> on<SetNameHandler>().modifyName() }

        yourName.requestFocus()

        actionViewProfile.setOnClickListener {
            on<NavigationHandler>().showMyProfile(actionViewProfile)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_personal, container, false)
    }
}
