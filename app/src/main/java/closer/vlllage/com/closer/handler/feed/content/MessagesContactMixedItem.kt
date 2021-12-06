package closer.vlllage.com.closer.handler.feed.content

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.MessagesContactItemBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.call.CallHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MessagesContactMixedItem(val group: Group) : MixedItem(MixedItemType.MessageContact)
class MessagesContactViewHolder(val binding: MessagesContactItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.MessageContact) {
    lateinit var on: On
}

class MessagesContactItemAdapter(private val on: On) : MixedItemAdapter<MessagesContactMixedItem, MessagesContactViewHolder> {
    override fun bind(holder: MessagesContactViewHolder, item: MessagesContactMixedItem, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<LightDarkHandler>().setLight(true)
        }

        holder.binding.name.setTextColor(ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.textHintInverse)))
        holder.binding.name.text = on<ResourcesHandler>().resources.getString(R.string.loading)
        holder.binding.lastMessage.text = ""

        holder.binding.photo.setImageResource(R.drawable.ic_person_black_24dp)

        holder.binding.photo.setOnClickListener(null)
        holder.binding.callButton.setOnClickListener(null)

        holder.binding.click.setOnClickListener {
            on<GroupActivityTransitionHandler>().showGroupMessages(holder.binding.click, item.group.id!!)
        }

        holder.binding.click.setOnLongClickListener {
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_baseline_visibility_off_24, R.string.hide_from_contacts) {
                        on<HideHandler>().hide(item.group)
                    }
            )

            true
        }

        holder.on<GroupActionRecyclerViewHandler>().attach(holder.binding.actionRecyclerView, GroupActionDisplay.Layout.TEXT)
        holder.on<DisposableHandler>().add(on<StoreHandler>().store.box(GroupAction::class).query()
                .equal(GroupAction_.group, item.group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .subscribe()
                .single()
                .on(AndroidScheduler.mainThread())
                .observer { groupActions ->
                    holder.on<GroupActionRecyclerViewHandler>().recyclerView!!.visible = groupActions.isNotEmpty()
                    holder.on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions)
                })

        if (on<PersistenceHandler>().phoneId == null) {
            return
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
        holder.binding.callButton.setOnClickListener {
            on<CallHandler>().startCall(phone.id!!)
        }

        holder.binding.photo.setOnClickListener { on<GroupActivityTransitionHandler>().showGroupForPhone(holder.binding.photo, phone.id!!) }

        when {
            groupContact.photo != null -> {
                holder.binding.photo.imageTintList = null
                holder.binding.photo.alpha = 1f
                holder.on<PhotoHelper>().loadCircle(holder.binding.photo, groupContact.photo!!, R.dimen.profilePhotoSmall)
            }
            phone.photo != null -> {
                holder.binding.photo.imageTintList = null
                holder.binding.photo.alpha = 1f
                holder.on<PhotoHelper>().loadCircle(holder.binding.photo, phone.photo!!, R.dimen.profilePhotoSmall)
            }
            else -> {
                holder.on<ImageHandler>().get().clear(holder.binding.photo)
            }
        }

        holder.binding.name.text = on<NameHandler>().getName(groupContact)
        holder.binding.name.setTextColor(ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(R.color.textInverse)))

        if (groupContact.status != null) {
            holder.binding.lastMessage.text = "${groupContact.status} • ${on<TimeStr>().prettyDate(group.updated)}"
        } else {
            holder.binding.lastMessage.text = on<TimeStr>().prettyDate(group.updated)
        }

        on<StoreHandler>().store.box(GroupMessage::class).query()
            .equal(GroupMessage_.to, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
            .orderDesc(GroupMessage_.created)
            .build()
            .subscribe()
            .on(AndroidScheduler.mainThread())
            .observer {
                it.firstOrNull()?.let { groupMessage ->
                    on<MessageDisplay>().displayText(groupMessage)?.let {
                        it.observeOn(AndroidSchedulers.mainThread()).subscribe { text ->
                            holder.binding.lastMessage.text = if (
                                groupMessage.from == on<PersistenceHandler>().phoneId &&
                                on<MessageDisplay>().attachmentType(groupMessage) != "message"
                            ) "${
                                on<ResourcesHandler>().resources.getString(R.string.you)
                            }: $text" else text
                        }.also {
                            holder.on<DisposableHandler>().add(it)
                        }
                    }
                }
            }.also {
                holder.on<DisposableHandler>().add(it)
            }

        holder.binding.actionRecyclerView
    }

    override fun getMixedItemClass() = MessagesContactMixedItem::class
    override fun getMixedItemType() = MixedItemType.MessageContact

    override fun areItemsTheSame(old: MessagesContactMixedItem, new: MessagesContactMixedItem) = old.group.id == new.group.id

    override fun areContentsTheSame(old: MessagesContactMixedItem, new: MessagesContactMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = MessagesContactViewHolder(MessagesContactItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: MessagesContactViewHolder) {
        holder.on.off()
    }
}