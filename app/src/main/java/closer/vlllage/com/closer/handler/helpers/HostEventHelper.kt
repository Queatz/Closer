package closer.vlllage.com.closer.handler.helpers

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.TaskDefinition
import closer.vlllage.com.closer.handler.TaskHandler
import closer.vlllage.com.closer.handler.TaskType
import closer.vlllage.com.closer.handler.event.EventHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On

class HostEventHelper constructor(private val on: On) {
    fun hostEvent(group: Group) {
        on<TaskHandler>().activeTask = TaskDefinition(TaskType.CREATE_EVENT_IN_GROUP, group)

        if (group.physical) {
            on<EventHandler>().createNewEvent(LatLng(
                    group.latitude!!,
                    group.longitude!!
            ), group.isPublic) {
                showEventOnMap(it)
            }
        } else {
            on<NavigationHandler>().showMap(on<ResourcesHandler>().resources.getString(R.string.create_event_in_group_instructions))
        }
    }

    private fun showEventOnMap(event: Event?) {
        (on<ActivityHandler>().activity as CircularRevealActivity)
                .finish { on<MapActivityHandler>().showEventOnMap(event!!) }
    }

}
