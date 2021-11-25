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
import io.objectbox.query.QueryBuilder
import kotlinx.android.synthetic.main.phone_contact_item.view.*

class PhoneContactAdapter(on: On,
                          private val onPhoneContactClickListener: ((phoneContact: PhoneContact) -> Unit)?,
                          private val onGroupInviteClickListener: ((groupInvite: GroupInvite) -> Unit)?,
                          private val onGroupContactClickListener: ((groupContact: GroupContact) -> Unit)?)
    : PoolRecyclerAdapter<PhoneContactAdapter.PhoneContactViewHolder>(on) {

    private var phoneNumber: String? = null
    private var isFiltered: Boolean = false
    private val invites = mutableListOf<GroupInvite>()
    private val groupContacts = mutableListOf<GroupContact>()
    private val contacts = mutableListOf<PhoneContact>()

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

        val isMember = position < memberAndInviteCount

        holder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            if (!isMember) {
                holder.itemView.setBackgroundResource(it.clickableRoundedBackground8dp)
                holder.name.setTextColor(it.text)
                holder.number.setTextColor(it.text)
                holder.action.setTextColor(it.action)

                if (!holder.phoneIconIsPhoto) {
                    holder.phoneIcon.imageTintList = it.tint
                }
            }
        })

        val pad = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble)

        if (isMember) {
            holder.name.setTextColor(on<LightDarkHandler>().LIGHT.text)
            holder.number.setTextColor(on<LightDarkHandler>().LIGHT.text)
            holder.action.setTextColor(on<LightDarkHandler>().LIGHT.action)
            holder.itemView.setPaddingRelative(pad, pad, pad, pad)

            if (!holder.phoneIconIsPhoto) {
                holder.phoneIcon.imageTintList = on<LightDarkHandler>().LIGHT.tint
            }
        } else {
            holder.itemView.setPaddingRelative(0, 0, pad, 0)
        }

        if (isMember) {
            holder.itemView.setBackgroundResource(R.drawable.clickable_white_8dp_flat)
            holder.itemView.elevation = on<ResourcesHandler>().resources.getDimension(R.dimen.elevation)

            if (phoneNumber != null) {
                contact = PhoneContact(null, phoneNumber)
            } else {
                if (position < groupContacts.size) {
                    val groupContact = groupContacts[position]

                    holder.phoneIcon.setImageResource(R.drawable.ic_person_black_24dp)

                    holder.phoneIcon.setOnClickListener(null)
                    holder.itemView.setOnLongClickListener(null)

                    if (groupContact.photo != null) {
                        holder.phoneIconIsPhoto = true
                        holder.phoneIcon.imageTintList = null
                        holder.phoneIcon.alpha = 1f
                        on<PhotoHelper>().loadCircle(holder.phoneIcon, groupContact.photo!!, R.dimen.profilePhotoSmall)
                        holder.phoneIcon.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.phoneIcon, groupContact.photo!!) }
                    } else {
                        holder.disposableGroup.add(on<StoreHandler>().store.box(Phone::class).query()
                                .equal(Phone_.id, groupContact.contactId!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
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
                                            on<PhotoHelper>().loadCircle(holder.phoneIcon, phone.photo!!, R.dimen.profilePhotoSmall)
                                            holder.phoneIcon.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.phoneIcon, phone.photo!!) }
                                        }
                                    }
                                })
                    }

                    val isMe = on<PersistenceHandler>().phoneId == groupContact.contactId
                    holder.name.text = on<NameHandler>().getName(groupContact)
                    holder.action.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.options else R.string.profile)

                    if (groupContact.status != null) {
                        holder.number.text = if (isMe) on<ResourcesHandler>().resources.getString(R.string.text_you, groupContact.status) else groupContact.status
                    } else {
                        holder.number.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.member_you else R.string.member)
                    }

                    holder.itemView.setOnClickListener {
                        onGroupContactClickListener?.invoke(groupContact)
                    }

                    holder.itemView.setOnLongClickListener {
                        on<DefaultAlerts>().message(
                                on<ResourcesHandler>().resources.getString(R.string.about),
                                "${
                                on<ResourcesHandler>().resources.getString(R.string.joined_date, on<TimeStr>().prettyDate(groupContact.created))
                                }${
                                groupContact.inviter?.let { if(it != groupContact.contactId) " ${on<ResourcesHandler>().resources.getString(R.string.by_invite_from, "@$it")}" else "" } ?: ""
                                }")

                        true
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

        holder.itemView.elevation = 0f
        holder.phoneIcon.setImageResource(R.drawable.ic_person_add_black_24dp)
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.invite)
        holder.name.text = if (contact.name == null) on<ResourcesHandler>().resources.getString(R.string.invite_by_phone) else contact.name
        holder.number.text = if (contact.phoneNumber == null) on<ResourcesHandler>().resources.getString(R.string.no_details) else contact.phoneNumber
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
