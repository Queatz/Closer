package closer.vlllage.com.closer.handler.group

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupInvite
import java.util.*

class PhoneContactAdapter(poolMember: PoolMember,
                          private val onPhoneContactClickListener: OnPhoneContactClickListener?,
                          private val onGroupInviteClickListener: OnGroupInviteClickListener?,
                          private val onGroupContactClickListener: OnGroupContactClickListener?) : PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder>(poolMember) {

    private var phoneNumber: String? = null
    private var isFiltered: Boolean = false
    private val invites = ArrayList<GroupInvite>()
    private val groupContacts = ArrayList<GroupContact>()
    private val contacts = ArrayList<PhoneContact>()

    private val memberAndInviteCount: Int
        get() = if (isFiltered) if (phoneNumber == null) 0 else 1 else groupContacts.size + invites.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneContactViewHolder {
        return PhoneContactViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.phone_contact_item, parent, false))
    }

    override fun onBindViewHolder(holder: PhoneContactViewHolder, position: Int) {
        var position = position
        val contact: PhoneContact

        if (position < memberAndInviteCount) {
            if (phoneNumber != null) {
                contact = PhoneContact(null, phoneNumber)
            } else {
                if (position < groupContacts.size) {
                    val groupContact = groupContacts[position]

                    holder.phoneIcon.setImageResource(R.drawable.ic_person_black_24dp)
                    val isMe = `$`(PersistenceHandler::class.java).phoneId == groupContact.contactId
                    holder.name.text = `$`(NameHandler::class.java).getName(groupContact)
                    holder.action.text = `$`(ResourcesHandler::class.java).resources.getString(if (isMe) R.string.options else R.string.send_message)
                    holder.number.text = `$`(ResourcesHandler::class.java).resources.getString(if (isMe) R.string.member_you else R.string.member)
                    holder.itemView.setOnClickListener { view ->
                        onGroupContactClickListener?.onGroupContactClicked(groupContact)
                    }
                } else {
                    position -= groupContacts.size
                    val invite = invites[position]

                    holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp)
                    holder.action.text = `$`(ResourcesHandler::class.java).resources.getString(R.string.cancel_invite)
                    holder.name.text = if (invite.name == null) `$`(ResourcesHandler::class.java).resources.getString(R.string.invite) else invite.name
                    holder.number.text = `$`(ResourcesHandler::class.java).resources.getString(R.string.invited)
                    holder.itemView.setOnClickListener { view ->
                        onGroupInviteClickListener?.onGroupInviteClicked(invite)
                    }
                }

                return
            }
        } else {
            contact = contacts[position - memberAndInviteCount]
        }

        holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp)
        holder.action.text = `$`(ResourcesHandler::class.java).resources.getString(R.string.invite)
        holder.name.text = if (contact.name == null) `$`(ResourcesHandler::class.java).resources.getString(R.string.invite_by_phone) else contact.name
        holder.number.text = if (contact.phoneNumber == null) `$`(ResourcesHandler::class.java).resources.getString(R.string.no_name) else contact.phoneNumber
        holder.itemView.setOnClickListener { view ->
            onPhoneContactClickListener?.onPhoneContactClicked(contact)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size + memberAndInviteCount
    }

    fun setPhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        notifyDataSetChanged()
    }

    fun setContacts(contacts: List<PhoneContact>) {
        this.contacts.clear()
        this.contacts.addAll(contacts)
        notifyDataSetChanged()
    }

    fun setInvites(invites: List<GroupInvite>) {
        this.invites.clear()
        this.invites.addAll(invites)
        notifyDataSetChanged()
    }

    fun setGroupContacts(groupContacts: List<GroupContact>) {
        this.groupContacts.clear()
        this.groupContacts.addAll(groupContacts)
        notifyDataSetChanged()
    }

    fun setIsFiltered(isFiltered: Boolean) {
        this.isFiltered = isFiltered
    }

    inner class PhoneContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var number: TextView
        var action: TextView
        var phoneIcon: ImageView

        init {
            name = itemView.findViewById(R.id.name)
            number = itemView.findViewById(R.id.number)
            phoneIcon = itemView.findViewById(R.id.phoneIcon)
            action = itemView.findViewById(R.id.action)
        }
    }

    interface OnPhoneContactClickListener {
        fun onPhoneContactClicked(phoneContact: PhoneContact)
    }

    interface OnGroupInviteClickListener {
        fun onGroupInviteClicked(groupInvite: GroupInvite)
    }

    interface OnGroupContactClickListener {
        fun onGroupContactClicked(groupContact: GroupContact)
    }
}
