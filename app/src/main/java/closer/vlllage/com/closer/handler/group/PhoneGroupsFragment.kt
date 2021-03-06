package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupContact_
import closer.vlllage.com.closer.store.models.Group_
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.fragment_phone_photos.*

class PhoneGroupsFragment : PoolActivityFragment() {
    private lateinit var searchGroupsAdapter: SearchGroupsAdapter
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var phoneDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_phone_photos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        phoneDisposableGroup = disposableGroup.group()

        searchGroupsAdapter = SearchGroupsAdapter(on, false, { group, view ->
               on<GroupActivityTransitionHandler>().showGroupMessages(view, group.id)
        }, null).apply {
            setActionText(on<ResourcesHandler>().resources.getString(R.string.open_group))
            setLayoutResId(R.layout.search_groups_item_light)
            setBackgroundResId(R.drawable.clickable_green_flat)
            flat = true
            transparentBackground = true
        }

        photosRecyclerView.layoutManager = LinearLayoutManager(photosRecyclerView.context)
        photosRecyclerView.adapter = searchGroupsAdapter

        on<GroupHandler> {
            onPhoneChanged(disposableGroup) { phone ->
                phoneDisposableGroup.clear()

                on<RefreshHandler>().refreshGroupContactsForPhone(phone.id!!)

                val queryBuilder = on<StoreHandler>().store.box(GroupContact::class).query()
                phoneDisposableGroup.add(queryBuilder
                        .sort(on<SortHandler>().sortGroupContacts())
                        .equal(GroupContact_.contactId, phone.id!!)
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupContacts ->
                            searchGroupsAdapter.setGroups(
                                    on<StoreHandler>().store.box(Group::class).query(
                                            Group_.id.oneOf(groupContacts.map { it.groupId }.toTypedArray())
                                    ).build().find())
                        })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
