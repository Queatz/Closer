package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.FragmentPhonePhotosBinding
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.ui.GridSpacingItemDecoration
import com.google.gson.JsonObject
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder

class GroupPhotosFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentPhonePhotosBinding
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var groupMessagesDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentPhonePhotosBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        groupMessagesDisposableGroup = disposableGroup.group()

        val groupMessagesAdapter = PhotosAdapter(on)

        binding.photosRecyclerView.layoutManager = GridLayoutManager(binding.photosRecyclerView.context, 3)
        binding.photosRecyclerView.adapter = groupMessagesAdapter
        binding.photosRecyclerView.addItemDecoration(GridSpacingItemDecoration(3,
                on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padQuarter)))

        on<GroupHandler> {
            onGroupChanged(disposableGroup) { group ->
                groupMessagesDisposableGroup.clear()

                on<RefreshHandler>().refreshGroupMessages(group.id!!)

                val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
                groupMessagesDisposableGroup.add(queryBuilder
                        .sort(on<SortHandler>().sortGroupMessages())
                        .equal(GroupMessage_.to, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                        .build()
                        .subscribe()
                        .on(AndroidScheduler.mainThread())
                        .observer { groupMessages ->
                            groupMessagesAdapter.items = groupMessages.filter { it.attachment?.let { on<JsonHandler>().from(it, JsonObject::class.java).get("photo")?.isJsonPrimitive } ?: false }
                        })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}
