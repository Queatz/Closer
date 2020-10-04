package closer.vlllage.com.closer

import android.os.Bundle
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.group.GroupDraftHandler
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler
import closer.vlllage.com.closer.handler.group.PhotoUploadGroupMessageHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.post.CreatePostActionType
import closer.vlllage.com.closer.handler.post.CreatePostHeaderAdapter
import closer.vlllage.com.closer.handler.post.PostSection
import closer.vlllage.com.closer.store.models.Group
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.reactivex.android.schedulers.AndroidSchedulers

class CreatePostActivity : ListActivity() {

    private var saved = false

    lateinit var adapter: CreatePostHeaderAdapter

    var groupId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        on<LightDarkHandler>().setLight(true)

        groupId = intent?.getStringExtra(EXTRA_GROUP_ID)

        if (groupId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return
        }

        recyclerView.itemAnimator = null

        adapter = CreatePostHeaderAdapter(on) {
            when (it.action) {
                CreatePostActionType.AddHeading -> {
                    addSection(it.position, on<JsonHandler>().from("{\"header\":{\"text\":\"\"}}", JsonObject::class.java))
                }
                CreatePostActionType.AddText -> {
                    addSection(it.position, on<JsonHandler>().from("{\"text\":{\"text\":\"\"}}", JsonObject::class.java))
                }
                CreatePostActionType.AddPhoto -> {
                    on<DefaultMenus>().uploadPhoto { photoId ->
                        addSection(it.position, on<JsonHandler>().from("{\"photo\":{\"photo\":\"${on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)}\"}}", JsonObject::class.java))
                    }
                }
                CreatePostActionType.AddGroupAction -> {
                    addSection(it.position, on<JsonHandler>().from("{\"activity\":null}", JsonObject::class.java))
                }
                CreatePostActionType.Delete -> {
                    if (adapter.items.size == 1) {
                        on<DefaultAlerts>().message(getString(R.string.cannot_delete_last_message))
                    } else {
                        removeSection(it.position)
                    }
                }
                CreatePostActionType.EditPhoto -> {
                    on<MenuHandler>().show(
                            MenuHandler.MenuOption(R.drawable.ic_baseline_photo_size_select_large_24, R.string.resize_photo) {
                                val photo = adapter.items[it.position].attachment.get("photo").asJsonObject
                                val large = photo.get("large")?.asBoolean ?: false

                                if (large) {
                                    photo.remove("large")
                                } else {
                                    photo.add("large", JsonPrimitive(true))
                                }

                                it.callback?.invoke()
                            })
                }
                CreatePostActionType.Post -> {
                    if (!verify()) {
                        on<DefaultAlerts>().message(R.string.blank_sections_error)
                    } else {
                        saved = true
                        on<GroupMessageAttachmentHandler>().sharePost(groupId!!, adapter.items.map { it.attachment })
                        on<GroupDraftHandler>().saveDraft(groupId!!, post = "")
                        finish()
                    }
                }
            }
        }

        adapter.setHeaderText(on<ResourcesHandler>().resources.getString(R.string.create_a_post))

        on<DataHandler>().getGroup(groupId!!).subscribe({
            setGroup(it)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }).also {
            on<DisposableHandler>().add(it)
        }
    }

    override fun onDestroy() {
        if (!saved) {
            on<GroupDraftHandler>().saveDraft(groupId!!, post = on<JsonHandler>().to(adapter.items.map { it.attachment }))
        }
        super.onDestroy()
    }

    private fun verify() = adapter.items.all {
        when {
            it.attachment.has("header") -> it.attachment.get("header").asJsonObject.get("text").asString.isNullOrBlank().not()
            it.attachment.has("text") -> it.attachment.get("text").asJsonObject.get("text").asString.isNullOrBlank().not()
            it.attachment.has("photo") -> it.attachment.get("photo").asJsonObject.get("photo").asString.isNullOrBlank().not()
            it.attachment.has("activity") -> it.attachment.get("activity").isJsonObject
            else -> false
        }
    }

    private fun setGroup(group: Group) {
        on<GroupNameHelper>().getName(group).observeOn(AndroidSchedulers.mainThread()).subscribe({
            adapter.groupName = it
        }, { on<DefaultAlerts>().thatDidntWork() }).also {
            on<DisposableHandler>().add(it)
        }

        adapter.items = (on<GroupDraftHandler>().getDraft(group.id!!)
                ?.post
                ?.takeIf { it.isNotBlank() }
                ?.let { on<JsonHandler>().from(it, JsonArray::class.java) }
                ?.toList()
                ?.map { it.asJsonObject } ?: listOf(
                on<JsonHandler>().from( "{\"header\":{\"text\":\"\"}}", JsonObject::class.java)
        )).map { PostSection(on<Val>().rndId(), it) }

        recyclerView.adapter = adapter
    }

    private fun removeSection(position: Int) {
        adapter.items = adapter.items.toMutableList().apply {
            removeAt(position)
        }
    }

    private fun addSection(position: Int, jsonObject: JsonObject) {
        adapter.items = adapter.items.toMutableList().apply {
            add(position + 1, PostSection(on<Val>().rndId(), jsonObject))
        }

        recyclerView.post {
            recyclerView.smoothScrollToPosition(position + 1 + 1)
        }
    }

    companion object {
        const val EXTRA_GROUP_ID = "groupId"
    }
}
