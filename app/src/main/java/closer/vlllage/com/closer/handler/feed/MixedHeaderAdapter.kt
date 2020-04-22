package closer.vlllage.com.closer.handler.feed

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.FeedHandler
import closer.vlllage.com.closer.handler.map.HeaderAdapter
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.calendar_day_item.view.*
import kotlinx.android.synthetic.main.calendar_event_item.view.*
import kotlinx.android.synthetic.main.group_action_photo_large_item.view.*
import kotlinx.android.synthetic.main.group_preview_item.view.*
import kotlinx.android.synthetic.main.group_preview_item.view.groupName
import kotlinx.android.synthetic.main.notification_item.view.*
import kotlinx.android.synthetic.main.text_item.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class MixedHeaderAdapter(on: On, private val changed: () -> Unit) : HeaderAdapter<RecyclerView.ViewHolder>(on) {

    var items = mutableListOf<MixedItem>()
        set(value) {
            val diffResult  = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val old = field[oldPosition]
                    val new = value[newPosition]
                    return old.type == new.type && when (old) {
                        is HeaderMixedItem -> true
                        is GroupMixedItem -> old.group.objectBoxId == (new as GroupMixedItem).group.objectBoxId
                        is GroupActionMixedItem -> old.groupAction.objectBoxId == (new as GroupActionMixedItem).groupAction.objectBoxId
                        is NotificationMixedItem -> old.notification.objectBoxId == (new as NotificationMixedItem).notification.objectBoxId
                        is CalendarDayMixedItem -> false
                        is TextMixedItem -> false
                        is GroupMessageMixedItem -> on<GroupMessageHelper>().areItemsTheSame(old.groupMessage, (new as GroupMessageMixedItem).groupMessage)
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
                        is GroupActionMixedItem -> old.groupAction.about == (new as GroupActionMixedItem).groupAction.about
                        is TextMixedItem -> false
                        is CalendarDayMixedItem -> false
                        is GroupMessageMixedItem -> on<GroupMessageHelper>().areContentsTheSame(old.groupMessage, (new as GroupMessageMixedItem).groupMessage)
                        else -> false
                    }
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
            changed()
        }

    var groups = mutableListOf<Group>()
        set(value) {
            field = value
            generate()
        }

    var groupActions = mutableListOf<GroupAction>()
        set(value) {
            field = value
            generate()
        }

    var groupMessages = mutableListOf<GroupMessage>()
        set(value) {
            field = value
            generate()
        }

    var notifications = mutableListOf<Notification>()
        set(value) {
            field = value
            generate()
        }

    var content: FeedContent = FeedContent.EXPLORE
        set(value) {
            field = value
            generate()
        }

    private fun generate() {
        items = mutableListOf<MixedItem>().apply {
            add(HeaderMixedItem())
            when (content) {
                FeedContent.EXPLORE -> groups.forEach { add(GroupMixedItem(it)) }
                FeedContent.GROUPS -> groups.forEach { add(GroupMixedItem(it)) }
                FeedContent.POSTS -> groupMessages.apply {
                    if (isEmpty()) add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_to_do_around_here)))
                    else {
                        forEach { add(GroupMessageMixedItem(it)) }
                        add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.view_more_conversations)))
                    }
                }
                FeedContent.ACTIVITIES -> groupActions.apply {
                    if (isEmpty()) add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_to_do_around_here)))
                    else forEach { add(GroupActionMixedItem(it)) }

                }
                FeedContent.NOTIFICATIONS -> notifications.forEach { add(NotificationMixedItem(it)) }
                FeedContent.CALENDAR -> IntArray(14)
                        .mapIndexed { i, _ -> i }
                        .forEach { add(CalendarDayMixedItem(it, Calendar.getInstance(TimeZone.getDefault()).let { cal ->
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            cal.add(Calendar.DATE, it)
                            cal.time
                        })) }
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
            3 -> CalendarDayViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.calendar_day_item, parent, false)).also {
                it.disposableGroup = on<DisposableHandler>().group()
            }
            4 -> GroupActionViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_action_photo_large_item, parent, false)).also {
                it.disposableGroup = on<DisposableHandler>().group()
            }
            5 -> TextViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.text_item, parent, false))
            6 -> on<GroupMessageHelper>().createViewHolder(parent)
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
            is CalendarDayViewHolder -> bindCalendarDay(viewHolder, (item as CalendarDayMixedItem).date, item.position)
            is GroupActionViewHolder -> bindGroupAction(viewHolder, (item as GroupActionMixedItem).groupAction)
            is GroupMessageViewHolder -> bindGroupMessage(viewHolder, (item as GroupMessageMixedItem).groupMessage)
            is TextViewHolder -> bindText(viewHolder, (item as TextMixedItem).text)
        }
    }

    private fun bindGroupMessage(holder: GroupMessageViewHolder, groupMessage: GroupMessage) {
        On(on).apply {
            use<LightDarkHandler>().setLight(true)
            use<GroupMessageHelper> {
                global = true
                inFeed = true
                onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) }
                onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(null, group1.id) }
            }
        }<GroupMessageHelper>().onBind(groupMessage, holder)
    }

    private fun bindText(holder: TextViewHolder, text: String) {
        holder.text.text = text
    }

    private fun bindGroupAction(holder: GroupActionViewHolder, groupAction: GroupAction) {
        holder.on = branch()
        holder.on<GroupActionDisplay>().display(holder.itemView.groupAction, groupAction, GroupActionDisplay.Layout.PHOTO, holder.itemView.groupActionDescription, 1.5f)
   }

    private fun bindCalendarDay(holder: CalendarDayViewHolder, date: Date, position: Int) {
        holder.on = branch()

        holder.date.text = on<TimeStr>().day(date)

        holder.itemView.headerPadding.visible = false && position == 0
        holder.itemView.headerPadding.clipToOutline = true

        holder.day.clipToOutline = true

        val distance = .12f

        holder.disposableGroup.add(on<MapHandler>().onMapIdleObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cameraPosition ->
            holder.eventsObservable?.let { holder.disposableGroup.dispose(it) }
            holder.eventsObservable = on<StoreHandler>().store.box(Event::class).query(
                    Event_.startsAt.between(date, Date(date.time + TimeUnit.DAYS.toMillis(1))).or(
                            Event_.endsAt.between(date, Date(date.time + TimeUnit.DAYS.toMillis(1)))
                    ).and(
                            Event_.latitude.between(cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance).and(
                                    Event_.longitude.between(cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                            ).or(
                                    Event_.isPublic.equal(false)
                            )
                    )
            )
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { setCalendarDayEvents(holder, date, it) }.also {
                        holder.disposableGroup.add(it)
                    }
        })
    }

    private fun setCalendarDayEvents(holder: CalendarDayViewHolder, date: Date, events: List<Event>? = null) {
        holder.views.forEach { holder.day.removeView(it) }
        holder.views.clear()

        if (events != null) {
            holder.events = events
        }

        val vH = holder.day.measuredHeight

        if (vH == 0) {
            holder.itemView.post { setCalendarDayEvents(holder, date, events) }
            return
        }

        val dayOfYear = Calendar.getInstance(TimeZone.getDefault()).let {
            it.time = date
            it.get(Calendar.DAY_OF_YEAR)
        }

        holder.events?.forEach { event ->
            val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.calendar_event_item, holder.day, false)

            view.name.text = event.name
            view.about.text = on<EventDetailsHandler>().formatEventDetails(event)
            (view.layoutParams as ConstraintLayout.LayoutParams).apply {
                val h = (event.endsAt!!.time - event.startsAt!!.time).toFloat() / TimeUnit.DAYS.toMillis(1) * vH

                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                width = MATCH_PARENT
                height = h.toInt()
                constrainedHeight = true
                constrainedWidth = true
                marginStart = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
                marginEnd = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble) * 3
            }

            view.translationY = (vH * Calendar.getInstance(TimeZone.getDefault()).let {
                it.time = event.startsAt!!
                (it.get(Calendar.DAY_OF_YEAR) - dayOfYear).toFloat() +
                        it.get(Calendar.HOUR_OF_DAY).toFloat() / TimeUnit.DAYS.toHours(1).toFloat() +
                        it.get(Calendar.MINUTE).toFloat() / TimeUnit.DAYS.toMinutes(1).toFloat()
            })

            view.name.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    if (event.isPublic) R.drawable.ic_public_black_18dp else R.drawable.ic_lock_black_18dp, 0, 0, 0
            )

            view.setBackgroundResource(if (event.isPublic) R.drawable.clickable_red_8dp else R.drawable.clickable_blue_8dp)

            view.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForEvent(view, event)
            }

            holder.day.addView(view)
            holder.views.add(view)
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

        holder.itemView.setOnLongClickListener {
            on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.sent_at, on<TimeStr>().pretty(notification.created)))
            true
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

        val groupMessagesAdapter = GroupMessagesAdapter(On(on).apply {
            use<GroupMessageHelper> {
                onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(holder.itemView, event) }
                onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(holder.itemView, group1.id) }
            }
        })

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
            groupMessage.created = Date()
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

        holder.disposableGroup.add(holder.on<LightDarkHandler>().onLightChanged
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
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
            is CalendarDayViewHolder -> {
                holder.disposableGroup.clear()
                holder.on.off()
            }
            is GroupActionViewHolder -> {
                holder.disposableGroup.clear()
                holder.on.off()
            }
            is GroupMessageViewHolder -> {
                on<GroupMessageHelper>().recycleViewHolder(holder)
            }
        }
    }

    override fun getItemCount() = items.size

    private fun bindHeader(holder: HeaderViewHolder) {
        holder.on = branch()
        holder.on<PublicGroupFeedItemHandler>().attach(holder.itemView) { on<FeedHandler>().show(it) }
    }

    private fun branch() = On().apply {
        use(on<FeedHandler>())
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
    class GroupMessageMixedItem(val groupMessage: GroupMessage) : MixedItem(6)
    class NotificationMixedItem(val notification: Notification) : MixedItem(2)
    class CalendarDayMixedItem(val position: Int, val date: Date) : MixedItem(3)
    class GroupActionMixedItem(val groupAction: GroupAction) : MixedItem(4)
    class TextMixedItem(val text: String) : MixedItem(5)
    open class MixedItem(val type: Int)

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
    }

    class CalendarDayViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
        lateinit var disposableGroup: DisposableGroup
        var eventsObservable: DataSubscription? = null
        val views = mutableSetOf<View>()
        var date = itemView.date!!
        var day = itemView.day!!
        var events: List<Event>? = null
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
        lateinit var disposableGroup: DisposableGroup
        var name = itemView.notificationName!!
        var message = itemView.notificationMessage!!
        var time = itemView.notificationTime!!
    }

    class GroupActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
        lateinit var disposableGroup: DisposableGroup
    }

    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text = itemView.text!!
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
}
