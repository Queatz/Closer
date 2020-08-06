package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.PhotoHelper
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.Store
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupContact
import closer.vlllage.com.closer.store.models.GroupContact_
import closer.vlllage.com.closer.store.models.Phone
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.messages_contact_item.view.*

class MessagesContactMixedItem(val group: Group) : MixedItem(MixedItemType.MessageContact)

class MessagesContactViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.MessageContact) {
    val click = itemView.click!!
    val photo = itemView.photo!!
    val name = itemView.name!!
    val lastMessage = itemView.lastMessage!!
    val callButton = itemView.callButton!!
}

class MessagesContactItemAdapter(private val on: On) : MixedItemAdapter<MessagesContactMixedItem, MessagesContactViewHolder> {
    override fun bind(holder: MessagesContactViewHolder, item: MessagesContactMixedItem, position: Int) {
        holder.name.text = ""
        holder.lastMessage.text = ""

        holder.photo.setImageResource(R.drawable.ic_person_black_24dp)

        holder.photo.setOnClickListener(null)

        holder.click.setOnClickListener {
            on<GroupActivityTransitionHandler>().showGroupMessages(holder.click, item.group.id!!)
        }

        on<StoreHandler>().store.box(GroupContact::class).query(
                GroupContact_.groupId.equal(item.group.id!!).and(
                        GroupContact_.contactId.notEqual(on<PersistenceHandler>().phoneId!!)
                )
        )
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer {
                    it.firstOrNull()?.let { groupContact ->
                        on<DataHandler>().getPhone(groupContact.contactId!!)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    setContact(holder, groupContact, it)
                                }, {})
                    }
                }.also {
                    on<DisposableHandler>().add(it)
                }
    }

    private fun setContact(holder: MessagesContactViewHolder, groupContact: GroupContact, phone: Phone) {
        if (groupContact.photo != null) {
            holder.photo.imageTintList = null
            holder.photo.alpha = 1f
            on<PhotoHelper>().loadCircle(holder.photo, groupContact.photo!!, R.dimen.profilePhotoSmall)
            holder.photo.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.photo, groupContact.photo!!) }
        } else if (phone.photo != null) {
            holder.photo.imageTintList = null
            holder.photo.alpha = 1f
            on<PhotoHelper>().loadCircle(holder.photo, phone.photo!!, R.dimen.profilePhotoSmall)
            holder.photo.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.photo, phone.photo!!) }
        }

        val isMe = on<PersistenceHandler>().phoneId == phone.id
        holder.name.text = on<NameHandler>().getName(groupContact)

        if (groupContact.status != null) {
            holder.lastMessage.text = if (isMe) on<ResourcesHandler>().resources.getString(R.string.text_you, groupContact.status) else groupContact.status
        } else {
            holder.lastMessage.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.member_you else R.string.member)
        }
    }

    override fun getMixedItemClass() = MessagesContactMixedItem::class
    override fun getMixedItemType() = MixedItemType.MessageContact

    override fun areItemsTheSame(old: MessagesContactMixedItem, new: MessagesContactMixedItem) = false

    override fun areContentsTheSame(old: MessagesContactMixedItem, new: MessagesContactMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = MessagesContactViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.messages_contact_item, parent, false))

    override fun onViewRecycled(holder: MessagesContactViewHolder) {
    }
}