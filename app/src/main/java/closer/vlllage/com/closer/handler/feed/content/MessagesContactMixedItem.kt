package closer.vlllage.com.closer.handler.feed.content

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
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
    lateinit var on: On
    val click = itemView.click!!
    val photo = itemView.photo!!
    val name = itemView.name!!
    val lastMessage = itemView.lastMessage!!
    val callButton = itemView.callButton!!
}

class MessagesContactItemAdapter(private val on: On) : MixedItemAdapter<MessagesContactMixedItem, MessagesContactViewHolder> {
    override fun bind(holder: MessagesContactViewHolder, item: MessagesContactMixedItem, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }
        holder.name.setTextColor(ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse)))
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.loading)
        holder.lastMessage.text = ""

        holder.photo.setImageResource(R.drawable.ic_person_black_24dp)

        holder.photo.setOnClickListener(null)
        holder.callButton.setOnClickListener {
            on<DefaultAlerts>().message("Woah matey!")
        }

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
                        holder.on<DataHandler>().getPhone(groupContact.contactId!!)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    setContact(holder, item.group, groupContact, it)
                                }, {}).also {
                                    holder.on<DisposableHandler>().add(it)
                                }
                    }
                }.also {
                    holder.on<DisposableHandler>().add(it)
                }
    }

    private fun setContact(holder: MessagesContactViewHolder, group: Group, groupContact: GroupContact, phone: Phone) {
        if (groupContact.photo != null) {
            holder.photo.imageTintList = null
            holder.photo.alpha = 1f
            holder.on<PhotoHelper>().loadCircle(holder.photo, groupContact.photo!!, R.dimen.profilePhotoSmall)
            holder.photo.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.photo, groupContact.photo!!) }
        } else if (phone.photo != null) {
            holder.photo.imageTintList = null
            holder.photo.alpha = 1f
            holder.on<PhotoHelper>().loadCircle(holder.photo, phone.photo!!, R.dimen.profilePhotoSmall)
            holder.photo.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.photo, phone.photo!!) }
        }

        holder.name.text = on<NameHandler>().getName(groupContact)
        holder.name.setTextColor(ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.textInverse)))

        if (groupContact.status != null) {
            holder.lastMessage.text = "${groupContact.status} • ${on<TimeStr>().prettyDate(group.updated)}"
        } else {
            holder.lastMessage.text = on<TimeStr>().prettyDate(group.updated)
        }
    }

    override fun getMixedItemClass() = MessagesContactMixedItem::class
    override fun getMixedItemType() = MixedItemType.MessageContact

    override fun areItemsTheSame(old: MessagesContactMixedItem, new: MessagesContactMixedItem) = false

    override fun areContentsTheSame(old: MessagesContactMixedItem, new: MessagesContactMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = MessagesContactViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.messages_contact_item, parent, false))

    override fun onViewRecycled(holder: MessagesContactViewHolder) {
        holder.on.off()
    }
}