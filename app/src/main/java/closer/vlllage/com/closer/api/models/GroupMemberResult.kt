package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.GroupMember

class GroupMemberResult : ModelResult() {
    var from: String? = null
    var to: String? = null
    var muted: Boolean = false
    var subscribed: Boolean = false

    companion object {

        fun from(groupMemberResult: GroupMemberResult): GroupMember {
            val groupMember = GroupMember()
            groupMember.id = groupMemberResult.id
            updateFrom(groupMember, groupMemberResult)
            return groupMember
        }

        fun updateFrom(groupMember: GroupMember, groupMemberResult: GroupMemberResult): GroupMember {
            groupMember.group = groupMemberResult.to
            groupMember.phone = groupMemberResult.from
            groupMember.muted = groupMemberResult.muted
            groupMember.subscribed = groupMemberResult.subscribed
            return groupMember
        }
    }
}
