package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupInvite
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.phone_contact_item.view.*
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
                .inflate(R.layout.phone_contact_item, parent, false)).also {
            it.disposableGroup = on<DisposableHandler>().group()
        }
    }

    override fun onBindViewHolder(holder: PhoneContactViewHolder, position: Int) {
        var position = position
        val contact: PhoneContact

        holder.phoneIconIsPhoto = false
        holder.phoneIcon.alpha = .75f

        holder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            holder.itemView.setBackgroundResource(it.clickableBackground)
            holder.name.setTextColor(it.text)
            holder.number.setTextColor(it.text)
            holder.action.setTextColor(it.action)

            if (!holder.phoneIconIsPhoto) {
                holder.phoneIcon.imageTintList = it.tint
            }
        })

        if (position < memberAndInviteCount) {
            if (phoneNumber != null) {
                contact = PhoneContact(null, phoneNumber)
            } else {
                if (position < groupContacts.size) {
                    val groupContact = groupContacts[position]

                    holder.phoneIcon.setImageDrawable(null)

                    holder.disposableGroup.add(on<StoreHandler>().store.box(Phone::class).query()
                            .equal(Phone_.id, groupContact.contactId!!)
                            .build()
                            .subscribe()
                            .on(AndroidScheduler.mainThread())
                            .single()
                            .observer {
                                it.firstOrNull()?.let { phone ->
                                    if (phone.photo != null) {
                                        holder.phoneIconIsPhoto = true
                                        holder.phoneIcon.imageTintList = null
                                        holder.phoneIcon.alpha = 1f
                                        on<PhotoHelper>().loadCircle(holder.phoneIcon, phone.photo!!)
                                    } else {
                                        holder.phoneIcon.setImageResource(R.drawable.ic_person_black_24dp)
                                    }
                                } ?: let {
                                    holder.phoneIcon.setImageResource(R.drawable.ic_person_black_24dp)
                                }
                            })

                    val isMe = on<PersistenceHandler>().phoneId == groupContact.contactId
                    holder.name.text = on<NameHandler>().getName(groupContact)
                    holder.action.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.options else R.string.profile)
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

    override fun onViewRecycled(holder: PhoneContactViewHolder) {
        holder.disposableGroup.clear()
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
        var name: TextView = itemView.name
        var number: TextView = itemView.number
        var action: TextView = itemView.action
        var phoneIcon: ImageView = itemView.phoneIcon
        var phoneIconIsPhoto = false
        lateinit var disposableGroup: DisposableGroup
    }
}
