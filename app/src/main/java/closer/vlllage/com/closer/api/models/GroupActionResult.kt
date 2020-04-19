package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.GroupAction
import java.util.*

class GroupActionResult : ModelResult() {
    var group: String? = null
    var name: String? = null
    var intent: String? = null
    var about: String? = null
    var photo: String? = null
    var flow: String? = null
    var used: Date? = null

    companion object {

        fun from(groupActionResult: GroupActionResult): GroupAction {
            val groupAction = GroupAction()
            groupAction.id = groupActionResult.id
            updateFrom(groupAction, groupActionResult)
            return groupAction
        }

        fun updateFrom(groupAction: GroupAction, groupActionResult: GroupActionResult): GroupAction {
            groupAction.name = groupActionResult.name
            groupAction.intent = groupActionResult.intent
            groupAction.about = groupActionResult.about
            groupAction.group = groupActionResult.group
            groupAction.photo = groupActionResult.photo
            groupAction.flow = groupActionResult.flow
            groupAction.used = groupActionResult.used
            return groupAction
        }
    }
}
