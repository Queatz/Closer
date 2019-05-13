package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.GroupAction

class GroupActionResult : ModelResult() {
    var group: String? = null
    var name: String? = null
    var intent: String? = null
    var photo: String? = null

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
            groupAction.group = groupActionResult.group
            groupAction.photo = groupActionResult.photo
            return groupAction
        }
    }
}
