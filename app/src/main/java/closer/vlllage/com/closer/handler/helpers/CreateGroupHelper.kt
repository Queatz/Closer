package closer.vlllage.com.closer.handler.helpers

import android.widget.EditText
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On
import kotlinx.android.synthetic.main.create_group_modal.view.*

class CreateGroupHelper(private val on: On) {
    fun createGroup(groupName: String?, isPublic: Boolean) {
        if (groupName.isNullOrBlank()) {
            on<AlertHandler>().make().apply {
                title = on<ResourcesHandler>().resources.getString(if (isPublic) R.string.create_public_group else R.string.add_new_private_group)
                layoutResId = R.layout.create_group_modal
                textViewId = R.id.input
                onTextViewSubmitCallback = {
                    addGroupDescription(it.trim(), isPublic)
                }
                onAfterViewCreated = { alertConfig, view ->
                    alertConfig.alertResult = view.input
                }
                buttonClickCallback = { it, _ ->
                    (it as EditText).text.isNotBlank()
                }
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.continue_text)
                show()
            }
        } else {
            addGroupDescription(groupName, isPublic)
        }
    }

    private fun addGroupDescription(groupName: String, isPublic: Boolean) {
        on<MapHandler>().center?.let { latLng ->
            on<LocalityHelper>().getLocality(on<MapHandler>().center!!) { locality ->
                on<AlertHandler>().make().apply {
                    title = groupName
                    message = if (isPublic) locality?.let { on<ResourcesHandler>().resources.getString(R.string.group_in_x, it) } ?: on<ResourcesHandler>().resources.getString(R.string.group) else on<ResourcesHandler>().resources.getString(R.string.private_group)
                    layoutResId = R.layout.create_public_group_modal
                    textViewId = R.id.input
                    onTextViewSubmitCallback = { about ->
                        val group = on<StoreHandler>().create(Group::class.java)
                        group!!.name = groupName
                        group.about = about
                        group.isPublic = isPublic
                        group.latitude = latLng.latitude
                        group.longitude = latLng.longitude
                        on<StoreHandler>().store.box(Group::class).put(group)
                        on<SyncHandler>().sync(group) { groupId ->
                            openGroup(groupId)
                        }
                    }
                    positiveButton = on<ResourcesHandler>().resources.getString(if (isPublic) R.string.create_public_group else R.string.create_private_group)
                    show()
                }
            }
        } ?: run { on<DefaultAlerts>().thatDidntWork() }
    }

    private fun openGroup(groupId: String?) {
        on<GroupActivityTransitionHandler>().showGroupMessages(null, groupId)
    }
}
