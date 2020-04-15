package closer.vlllage.com.closer.api.models

import closer.vlllage.com.closer.store.models.GroupContact

class GroupContactResult : ModelResult() {
    var to: String? = null
    var from: String? = null
    var phone: PhoneResult? = null
    var group: GroupResult? = null
    var status: String? = null
    var inviter: String? = null
    var photo: String? = null

    companion object {

        fun from(groupContactResult: GroupContactResult): GroupContact {
            val groupContact = GroupContact()
            groupContact.id = groupContactResult.id
            groupContact.contactId = groupContactResult.from
            groupContact.groupId = groupContactResult.to
            groupContact.contactName = groupContactResult.phone!!.name
            groupContact.contactActive = groupContactResult.phone!!.updated
            groupContact.updated = groupContactResult.updated
            groupContact.status = groupContactResult.status
            groupContact.inviter = groupContactResult.inviter
            groupContact.photo = groupContactResult.photo
            return groupContact
        }
    }
}
