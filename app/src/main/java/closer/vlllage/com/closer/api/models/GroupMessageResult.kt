package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.ReactionCount

class GroupMessageResult : ModelResult() {
    var from: String? = null
    var to: String? = null
    var text: String? = null
    var attachment: String? = null
    var phone: PhoneResult? = null
    var reactions: List<ReactionCount> = listOf()
    var replies: Int? = null

    companion object {
        fun from(result: GroupMessageResult): GroupMessage {
            val groupMessage = GroupMessage()
            groupMessage.id = result.id
            groupMessage.from = result.from
            groupMessage.to = result.to
            groupMessage.text = result.text
            groupMessage.time = result.created
            groupMessage.updated = result.updated
            groupMessage.attachment = result.attachment
            groupMessage.reactions = result.reactions
            groupMessage.replies = result.replies
            return groupMessage
        }
    }
}
