package closer.vlllage.com.closer.handler.feed.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On
import kotlinx.android.synthetic.main.messages_contact_item.view.*

class MessagesContactMixedItem(val group: Group) : MixedItem(MixedItemType.MessageContact)

class MessagesContactViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.MessageContact) {
    val photo = itemView.photo!!
    val name = itemView.name!!
    val lastMessage = itemView.lastMessage!!
    val callButton = itemView.callButton!!
}

class MessagesContactItemAdapter(private val on: On) : MixedItemAdapter<MessagesContactMixedItem, MessagesContactViewHolder> {
    override fun bind(holder: MessagesContactViewHolder, item: MessagesContactMixedItem, position: Int) {
        holder.name.text = "Mai Pham"
        holder.lastMessage.text = "I luv u"
//        holder.photo.setImageResource(R.drawable.ic_person_black_24dp)
//
//        holder.photo.setOnClickListener(null)
//        holder.itemView.setOnLongClickListener(null)
//
//        if ("groupContact.photo" != null) {
//            holder.photo.imageTintList = null
//            holder.photo.alpha = 1f
//            on<PhotoHelper>().loadCircle(holder.photo, "groupContact.photo", R.dimen.profilePhotoSmall)
//            holder.photo.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.photo, "groupContact.photo") }
//        } else {
//            on<DisposableHandler>().add(on<StoreHandler>().store.box(Phone::class).query()
//                    .equal(Phone_.id, groupContact.contactId!!)
//                    .build()
//                    .subscribe()
//                    .on(AndroidScheduler.mainThread())
//                    .single()
//                    .observer {
//                        it.firstOrNull()?.let { phone ->
//                            if (phone.photo != null) {
//                                holder.photo.imageTintList = null
//                                holder.photo.alpha = 1f
//                                on<PhotoHelper>().loadCircle(holder.photo, phone.photo!!, R.dimen.profilePhotoSmall)
//                                holder.photo.setOnClickListener { on<PhotoActivityTransitionHandler>().show(holder.photo, phone.photo!!) }
//                            }
//                        }
//                    })
//        }
//
//        val isMe = on<PersistenceHandler>().phoneId == groupContact.contactId
//        holder.name.text = on<NameHandler>().getName(groupContact)
//        holder.action.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.options else R.string.profile)
//
//        if (groupContact.status != null) {
//            holder.number.text = if (isMe) on<ResourcesHandler>().resources.getString(R.string.text_you, groupContact.status) else groupContact.status
//        } else {
//            holder.number.text = on<ResourcesHandler>().resources.getString(if (isMe) R.string.member_you else R.string.member)
//        }
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