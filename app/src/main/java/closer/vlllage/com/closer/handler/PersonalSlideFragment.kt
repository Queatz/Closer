package closer.vlllage.com.closer.handler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.ActivityPersonalBinding
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
import java.util.*

class PersonalSlideFragment : PoolFragment() {

    private lateinit var binding: ActivityPersonalBinding
    private var previousStatus: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        on<NetworkConnectionViewHandler>().attach(binding.connectionError)

        val searchGroupsAdapter = SearchGroupsAdapter(on, false, { group, v -> on<GroupActivityTransitionHandler>().showGroupMessages(v, group.id) }, null)

        searchGroupsAdapter.setActionText(on<ResourcesHandler>().resources.getString(R.string.open_group))
        searchGroupsAdapter.setIsSmall(true)
        searchGroupsAdapter.setLayoutResId(R.layout.search_groups_item_large_padding)

        binding.subscribedGroupsRecyclerView.adapter = searchGroupsAdapter
        binding.subscribedGroupsRecyclerView.layoutManager = LinearLayoutManager(binding.subscribedGroupsRecyclerView.context)

        on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupMember::class).query()
                .equal(GroupMember_.phone, on<Val>().trimmed(on<PersistenceHandler>().phoneId), QueryBuilder.StringOrder.CASE_SENSITIVE)
                .equal(GroupMember_.subscribed, true)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupMembers ->
                    if (groupMembers.isEmpty()) {
                        binding.youveSubscribedEmpty.visible = true
                        searchGroupsAdapter.setGroups(listOf())
                    } else {
                        binding.youveSubscribedEmpty.visible = false

                        val ids = HashSet<String>()
                        for (groupMember in groupMembers) {
                            ids.add(groupMember.group!!)
                        }

                        on<StoreHandler>().findAll(Group::class.java, Group_.id, ids, on<SortHandler>().sortGroups()).observer { searchGroupsAdapter.setGroups(it) }
                    }
                })

        binding.shareYourLocationSwitch.isChecked = on<AccountHandler>().active

        binding.shareYourLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            on<AccountHandler>().updateActive(isChecked)

            if (isChecked) {
                on<DefaultAlerts>().message(R.string.your_sharing_location)
            }
        }

        previousStatus = on<AccountHandler>().status
        binding.currentStatus.setText(previousStatus)

        binding.currentStatus.setOnFocusChangeListener { _, _ ->
            if (binding.currentStatus.text.toString() == previousStatus) {
                return@setOnFocusChangeListener
            }

            on<AccountHandler>().updateStatus(binding.currentStatus.text.toString())
            on<KeyboardHandler>().showKeyboard(binding.currentStatus, false)
        }

        binding.yourName.text = on<Val>().of(on<AccountHandler>().name, on<ResourcesHandler>().resources.getString(R.string.update_your_name))

        binding.yourPhoto.setOnClickListener {
            on<DefaultMenus>().uploadPhoto { photoId ->
                val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
                on<AccountHandler>().updatePhoto(photo)
            }
        }

        if (!on<PersistenceHandler>().myPhoto.isBlank()) {
            on<ImageHandler>().get().load(on<PersistenceHandler>().myPhoto + "?s=128")
                    .apply(RequestOptions().circleCrop())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.yourPhoto)
        }

        on<DisposableHandler>().add(on<AccountHandler>().changes().subscribe(
                { accountChange ->
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_NAME) {
                        binding.yourName.text = on<AccountHandler>().name
                    }
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_PHOTO) {
                        on<PhotoHelper>().loadCircle(binding.yourPhoto, on<PersistenceHandler>().myPhoto + "?s=128")
                    }
                    if (accountChange.prop == AccountHandler.ACCOUNT_FIELD_ACTIVE) {
                        binding.shareYourLocationSwitch.isChecked = accountChange.value == true
                    }
                },
                { on<DefaultAlerts>().thatDidntWork() }
        ))

        binding.yourName.setOnClickListener { v -> on<SetNameHandler>().modifyName() }

        binding.yourName.requestFocus()

        binding.actionViewProfile.setOnClickListener {
            on<NavigationHandler>().showMyProfile(binding.actionViewProfile)
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ActivityPersonalBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }
}
