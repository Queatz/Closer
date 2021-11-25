package closer.vlllage.com.closer.handler.group

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.InputModalBinding
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On

class PhysicalGroupUpgradeHandler constructor(private val on: On) {
    fun convertToHub(group: Group, onGroupUpdateListener: (Group) -> Unit) {
        on<AlertHandler>().view { InputModalBinding.inflate(it) }.apply {
            title = on<ResourcesHandler>().resources.getString(R.string.set_name)
            textViewId = R.id.input
            onAfterViewCreated = { _, view -> view.input.setText(group.name ?: "") }
            onTextViewSubmitCallback = { result ->
                on<DisposableHandler>().add(on<ApiHandler>().convertToHub(group.id!!, result).subscribe({
                    group.name = result
                    on<StoreHandler>().store.box(Group::class).put(group)
                    onGroupUpdateListener.invoke(group)
                }, { on<DefaultAlerts>().thatDidntWork() }))
            }
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.set_name)
            show()
        }
    }

    fun setBackground(group: Group, onGroupUpdateListener: (Group) -> Unit) {
        on<DefaultMenus>().uploadPhoto { photoId -> handlePhoto(group, photoId, onGroupUpdateListener) }
    }

    private fun handlePhoto(group: Group, photoId: String, onGroupUpdateListener: (Group) -> Unit) {
        val photo = on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId)
        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().setGroupPhoto(group.id!!, photo).subscribe(
                { successResult ->
                    if (successResult.success) {
                        group.photo = photo
                        on<StoreHandler>().store.box(Group::class).put(group)
                        onGroupUpdateListener.invoke(group)
                    } else {
                        on<DefaultAlerts>().thatDidntWork()
                    }
                },
                { on<DefaultAlerts>().thatDidntWork() }
        ))
    }

    fun setAbout(group: Group, about: String, onGroupUpdateListener: (Group) -> Unit) {
        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().setGroupAbout(group.id!!, about).subscribe(
                { successResult ->
                    if (successResult.success) {
                        group.about = about
                        on<StoreHandler>().store.box(Group::class).put(group)
                        onGroupUpdateListener.invoke(group)
                    } else {
                        on<DefaultAlerts>().thatDidntWork()
                    }
                },
                { on<DefaultAlerts>().thatDidntWork() }
        ))
    }
}
