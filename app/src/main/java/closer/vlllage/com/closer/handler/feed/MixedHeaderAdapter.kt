package closer.vlllage.com.closer.handler.feed

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.FeedHandler
import closer.vlllage.com.closer.handler.map.HeaderAdapter
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.store.models.Notification
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.group_preview_item.view.*
import kotlinx.android.synthetic.main.notification_item.view.*
import java.util.*
import kotlin.math.min

class MixedHeaderAdapter(on: On) : HeaderAdapter<RecyclerView.ViewHolder>(on) {

    var items = mutableListOf<MixedItem>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val old = field[oldPosition]
                    val new = value[newPosition]
                    return old.type == new.type && when (old) {
                        is HeaderMixedItem -> true
                        is GroupMixedItem -> old.group.objectBoxId == (new as GroupMixedItem).group.objectBoxId
                        is NotificationMixedItem -> old.notification.objectBoxId == (new as NotificationMixedItem).notification.objectBoxId
                        else -> false
                    }
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val old = field[oldPosition]
                    val new = value[newPosition]
                    return old.type == new.type && when (old) {
                        is HeaderMixedItem -> true
                        is GroupMixedItem -> false
                        is NotificationMixedItem -> true
                        else -> false
                    }
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    var groups = mutableListOf<Group>()
        set(value) {
            field = value
            generate()
        }

    var notifications = mutableListOf<Notification>()
        set(value) {
            field = value
            generate()
        }

    var content: FeedContent = FeedContent.GROUPS
        set(value) {
            field = value
            generate()
        }

    private fun generate() {
        items = mutableListOf<MixedItem>().apply {
            add(HeaderMixedItem())
            when (content) {
                FeedContent.GROUPS -> groups.forEach { add(GroupMixedItem(it)) }
                FeedContent.NOTIFICATIONS -> notifications.forEach { add(NotificationMixedItem(it)) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.feed_item_public_groups, parent, false))
            1 -> GroupPreviewViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_preview_item, parent, false)).also {
                it.disposableGroup = on<DisposableHandler>().group()
            }
            2 -> NotificationViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_item, parent, false)).also {
                it.disposableGroup = on<DisposableHandler>().group()
            }
            else -> object : RecyclerView.ViewHolder(View(parent.context)) {}
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(viewHolder, position)

        val item = items[position]

        when (viewHolder) {
            is HeaderViewHolder -> bindHeader(viewHolder)
            is GroupPreviewViewHolder -> bindGroupPreview(viewHolder, (item as GroupMixedItem).group)
            is NotificationViewHolder -> bindNotification(viewHolder, (item as NotificationMixedItem).notification)
        }
    }

    private fun bindNotification(holder: NotificationViewHolder, notification: Notification) {
        holder.on = branch()
        holder.name.text = notification.name ?: ""
        holder.message.text = notification.message ?: ""
        holder.time.text = on<TimeStr>().prettyDate(notification.created)
        holder.itemView.setOnClickListener {
            on<NotificationHandler>().launch(notification)
        }
    }

    private fun bindGroupPreview(holder: GroupPreviewViewHolder, group: Group) {
        holder.on = branch()

        holder.groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
        holder.groupName.setOnClickListener { on<GroupActivityTransitionHandler>().showGroupMessages(holder.groupName, group.id) }
        holder.groupName.setOnLongClickListener {
            on<GroupMemberHandler>().changeGroupSettings(group)
            true
        }

        val groupMessagesAdapter = GroupMessagesAdapter(on)
        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        groupMessagesAdapter.onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(holder.itemView, event) }
        groupMessagesAdapter.onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(holder.itemView, group1.id) }

        val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
        holder.on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortGroupMessages())
                .equal(GroupMessage_.to, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .transform<List<GroupMessage>> { groupMessages -> groupMessages.subList(0, min(groupMessages.size, 5)) }
                .observer { groupMessagesAdapter.setGroupMessages(it) })

        holder.on<PinnedMessagesHandler>().attach(holder.pinnedMessagesRecyclerView)
        holder.on<PinnedMessagesHandler>().show(group)

        holder.messagesRecyclerView.adapter = groupMessagesAdapter
        holder.messagesRecyclerView.layoutManager = LinearLayoutManager(holder.messagesRecyclerView.context, RecyclerView.VERTICAL, true)

        if (holder.textWatcher != null) {
            holder.replyMessage.removeTextChangedListener(holder.textWatcher)
        }

        holder.replyMessage.setText(on<GroupMessageParseHandler>().parseText(on<GroupDraftHandler>().getDraft(group)!!))

        holder.textWatcher = object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && holder.on<GroupMessageParseHandler>().isMentionSelected(holder.replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                on<GroupDraftHandler>().saveDraft(group, text.toString())
                holder.on<GroupMessageMentionHandler>().showSuggestionsForName(holder.on<GroupMessageParseHandler>().extractName(text, holder.replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    holder.on<GroupMessageParseHandler>().deleteMention(holder.replyMessage)
                }
            }
        }

        holder.replyMessage.addTextChangedListener(holder.textWatcher)

        holder.on<GroupMessageMentionHandler>().attach(holder.mentionSuggestionsLayout, holder.mentionSuggestionRecyclerView) {
            mention -> holder.on<GroupMessageParseHandler>().insertMention(holder.replyMessage, mention)
        }

        holder.replyMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                holder.sendButton.callOnClick()
            }

            false
        }

        holder.sendButton.setOnClickListener { view ->
            val message = holder.replyMessage.text.toString()

            if (message.isBlank()) {
                return@setOnClickListener
            }

            val groupMessage = GroupMessage()
            groupMessage.text = message
            groupMessage.from = on<PersistenceHandler>().phoneId
            groupMessage.to = group.id
            groupMessage.time = Date()
            on<StoreHandler>().store.box(GroupMessage::class).put(groupMessage)
            on<SyncHandler>().sync(groupMessage)

            holder.replyMessage.setText("")
            on<KeyboardHandler>().showKeyboard(view, false)
        }

        on<ImageHandler>().get().cancelRequest(holder.backgroundPhoto)
        if (group.photo != null) {
            holder.backgroundPhoto.visibility = View.VISIBLE
            holder.backgroundPhoto.setImageDrawable(null)
            on<PhotoLoader>().softLoad(group.photo!!, holder.backgroundPhoto)
        } else {
            holder.backgroundPhoto.visibility = View.GONE
        }

        on<GroupScopeHandler>().setup(group, holder.scopeIndicatorButton)

        holder.disposableGroup.add(holder.on<LightDarkHandler>().onLightChanged.subscribe {
            holder.groupName.setTextColor(it.text)
            holder.sendButton.imageTintList = it.tint
            holder.sendButton.setBackgroundResource(it.clickableRoundedBackground)
            holder.replyMessage.setTextColor(it.text)
            holder.replyMessage.setHintTextColor(it.hint)
            holder.replyMessage.setBackgroundResource(it.clickableRoundedBackground)
            holder.scopeIndicatorButton.imageTintList = it.tint
            holder.goToGroup.imageTintList = it.tint

            if (it.light) {
                holder.backgroundPhoto.alpha = .15f
                holder.itemView.setBackgroundResource(R.color.offwhite)
                holder.backgroundColor.setBackgroundResource(R.drawable.color_white_rounded)
            } else {
                holder.backgroundPhoto.alpha = 1f
                holder.itemView.setBackgroundResource(R.color.white)
                holder.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
            }

            holder.groupName.setBackgroundResource(it.clickableRoundedBackground8dp)
        })
    }

    override fun getItemViewType(position: Int) = items[position].type

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is HeaderViewHolder -> holder.on.off()
            is GroupPreviewViewHolder -> {
                holder.disposableGroup.clear()
                holder.on.off()
            }
            is NotificationViewHolder -> {
                holder.disposableGroup.clear()
                holder.on.off()
            }
        }
    }

    override fun getItemCount() = items.size

    private fun bindHeader(holder: HeaderViewHolder) {
        holder.on = branch()
        holder.on<PublicGroupFeedItemHandler>().attach(holder.itemView) { on<FeedHandler>().show(it) }
    }

    private fun branch() = On().apply {
        use(on<StoreHandler>())
        use(on<SyncHandler>())
        use(on<MapHandler>())
        use(on<ApplicationHandler>())
        use(on<ActivityHandler>())
        use(on<SortHandler>())
        use(on<KeyboardHandler>())
        use(on<GroupMemberHandler>())
        use(on<MediaHandler>())
        use(on<CameraHandler>())
        use(on<ApiHandler>())
        use(on<LightDarkHandler>())
        use(on<ResourcesHandler>())
    }

    class HeaderMixedItem : MixedItem(0)
    class GroupMixedItem(val group: Group) : MixedItem(1)
    class NotificationMixedItem(val notification: Notification) : MixedItem(2)
    open class MixedItem(val type: Int)

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
        lateinit var disposableGroup: DisposableGroup
        var name = itemView.notificationName!!
        var message = itemView.notificationMessage!!
        var time = itemView.notificationTime!!
    }

    class GroupPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var on: On
        var groupName = itemView.groupName!!
        var messagesRecyclerView = itemView.messagesRecyclerView!!
        var pinnedMessagesRecyclerView = itemView.pinnedMessagesRecyclerView!!
        var sendButton = itemView.sendButton!!
        var replyMessage = itemView.replyMessage!!
        var backgroundPhoto = itemView.backgroundPhoto!!
        var scopeIndicatorButton = itemView.scopeIndicatorButton!!
        var goToGroup = itemView.goToGroup!!
        var mentionSuggestionsLayout = itemView.mentionSuggestionsLayout!!
        var mentionSuggestionRecyclerView = itemView.mentionSuggestionRecyclerView!!
        var backgroundColor = itemView.backgroundColor!!
        var textWatcher: TextWatcher? = null
        lateinit var disposableGroup: DisposableGroup
    }

    companion object {
        private const val HEADER_COUNT = 1
    }
}
