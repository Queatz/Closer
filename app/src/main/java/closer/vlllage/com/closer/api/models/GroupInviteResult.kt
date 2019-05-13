package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.GroupInvite

class GroupInviteResult : ModelResult() {
    var name: String? = null
    var group: String? = null

    companion object {

        fun from(result: GroupInviteResult): GroupInvite {
            val groupInvite = GroupInvite()
            groupInvite.id = result.id
            groupInvite.group = result.group
            groupInvite.name = result.name
            groupInvite.updated = result.updated
            return groupInvite
        }
    }
}
