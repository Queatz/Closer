package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupInvite
import com.queatz.on.On
import java.util.*

class PhoneContactAdapter(on: On,
                          private val onPhoneContactClickListener: ((phoneContact: PhoneContact) -> Unit)?,
                          private val onGroupInviteClickListener: ((groupInvite: GroupInvite) -> Unit)?,
                          private val onGroupContactClickListener: ((groupContact: GroupContact) -> Unit)?)
    : PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder>(on) {

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
                    val isMe = on<PersistenceHandler>().phoneId == groupContact.contactId
                    holder.name.text = on<NameHandler>().getName(groupContact)
                    holder.action.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.options else R.string.send_message)
                    holder.number.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.member_you else R.string.member)
                    holder.itemView.setOnClickListener {
                        onGroupContactClickListener?.invoke(groupContact)
                    }
                } else {
                    position -= groupContacts.size
                    val invite = invites[position]

                    holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp)
                    holder.action.text = on<ResourcesHandler>().resources.getString(R.string.cancel_invite)
                    holder.name.text = if (invite.name == null) on<ResourcesHandler>().resources.getString(R.string.invite) else invite.name
                    holder.number.text = on<ResourcesHandler>().resources.getString(R.string.invited)
                    holder.itemView.setOnClickListener {
                        onGroupInviteClickListener?.invoke(invite)
                    }
                }

                return
            }
        } else {
            contact = contacts[position - memberAndInviteCount]
        }

        holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp)
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.invite)
        holder.name.text = if (contact.name == null) on<ResourcesHandler>().resources.getString(R.string.invite_by_phone) else contact.name
        holder.number.text = if (contact.phoneNumber == null) on<ResourcesHandler>().resources.getString(R.string.no_name) else contact.phoneNumber
        holder.itemView.setOnClickListener {
            onPhoneContactClickListener?.invoke(contact)
        }
    }

    override fun getItemCount(): Int {
        return contacts.size + memberAndInviteCount
    }

    fun setPhoneNumber(phoneNumber: String?) {
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
        var name: TextView = itemView.findViewById(R.id.name)
        var number: TextView = itemView.findViewById(R.id.number)
        var action: TextView = itemView.findViewById(R.id.action)
        var phoneIcon: ImageView = itemView.findViewById(R.id.phoneIcon)

    }
}
