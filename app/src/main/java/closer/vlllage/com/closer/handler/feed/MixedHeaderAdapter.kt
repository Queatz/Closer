package closer.vlllage.com.closer.handler.feed

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.feed.content.*
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.map.HeaderAdapter
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit

class MixedHeaderAdapter(on: On, private val hideText: Boolean = false) : HeaderAdapter<MixedItemViewHolder>(on) {

    var goals = mutableListOf<Goal>()
        set(value) {
            field = value
            generate()
        }

    var lifestyles = mutableListOf<Lifestyle>()
        set(value) {
            field = value
            generate()
        }

    var groups = mutableListOf<Group>()
        set(value) {
            field = value
            generate()
        }

    var contacts = mutableListOf<Group>()
        set(value) {
            field = value
            generate()
        }

    var quests = mutableListOf<Quest>()
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

    var loadingStories = true
        set(value) {
            field = value
            generate()
        }

    var stories = mutableListOf<Story>()
        set(value) {
            field = value
            generate()
        }

    var content: FeedContent = FeedContent.POSTS
        set(value) {
            field = value
            generateInternal()
        }

    var showFeedHeader: Boolean = true
        set(value) {
            field = value
            generate()
        }

    var items = mutableListOf<MixedItem>()
        set(value) {
            val diffResult  = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val old = field[oldPosition]
                    val new = value[newPosition]

                    return old.type == new.type && adapters[old.type]?.areItemsTheSame(old, new) ?: false
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    val old = field[oldPosition]
                    val new = value[newPosition]
                    return old.type == new.type && adapters[old.type]?.areContentsTheSame(old, new) ?: false
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    private val adapters = listOf(
            on<HeaderMixedItemAdapter>(),
            on<TextMixedItemAdapter>(),
            on<GroupActionMixedItemAdapter>(),
            on<GroupMessageMixedItemAdapter>(),
            on<QuestMixedItemAdapter>(),
            on<NotificationMixedItemAdapter>(),
            on<GroupPreviewMixedItemAdapter>(),
            on<CalendarDayMixedItemAdapter>(),
            on<MessagesContactItemAdapter>(),
            on<LifestyleMixedItemAdapter>(),
            on<GoalMixedItemAdapter>(),
            on<WelcomeMixedItemAdapter>(),
            on<StoryMixedItemAdapter>(),
    ).map { Pair(it.getMixedItemType(), it as MixedItemAdapter<MixedItem, MixedItemViewHolder>) }.toMap()

    private val regenerate = BehaviorSubject.create<Unit>()

    private fun generate() {
        regenerate.onNext(Unit)
    }

    init {
        regenerate.debounce(250, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe {
            generateInternal()
        }.also {
            on<DisposableHandler>().add(it)
        }
    }

    private fun generateInternal() {
        items = mutableListOf<MixedItem>().apply {

            val empty = { item: TextMixedItem -> if (!hideText) add(item) }

            if (showFeedHeader) add(HeaderMixedItem())
            when (content) {
                FeedContent.GROUPS, FeedContent.PLACES -> groups.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_around_here)))
                    else {
                        forEach { add(GroupPreviewMixedItem(it)) }
                    }
                }
                FeedContent.FRIENDS -> groups.apply {
                    forEach { add(GroupPreviewMixedItem(it)) }
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_here)))
                }
                FeedContent.POSTS -> stories.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(if (loadingStories) R.string.loading_stories else R.string.nothing_around_here)))
                    else {
                        forEach { add(StoryMixedItem(it)) }
                        if (!hideText) add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.view_more_stories)))
                    }
                }
                FeedContent.ACTIVITIES -> groupActions.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_around_here)))
                    else forEach { add(GroupActionMixedItem(it)) }

                }
                FeedContent.QUESTS -> quests.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_around_here)))
                    else forEach { add(QuestMixedItem(it)) }
                }
                FeedContent.NOTIFICATIONS -> notifications.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.no_notifications)))
                    else forEach { add(NotificationMixedItem(it)) }
                }
                FeedContent.CONTACTS -> contacts.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.no_contacts)))
                    else forEach { add(MessagesContactMixedItem(it)) }
                }
                FeedContent.CALENDAR -> IntArray(14)
                        .mapIndexed { i, _ -> i }
                        .forEach {
                            add(CalendarDayMixedItem(it, Calendar.getInstance(TimeZone.getDefault()).let { cal ->
                                cal.set(Calendar.HOUR_OF_DAY, 0)
                                cal.set(Calendar.MINUTE, 0)
                                cal.set(Calendar.SECOND, 0)
                                cal.set(Calendar.MILLISECOND, 0)
                                cal.add(Calendar.DATE, it)
                                cal.time
                            }))
                        }
                FeedContent.LIFESTYLES -> lifestyles.apply {
                        if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_around_here)))
                        else {
                            forEach {
                                add(LifestyleMixedItem(it))
                            }
                            add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.view_more_lifestyles)))
                        }
                    }
                FeedContent.GOALS -> goals.apply {
                        if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_around_here)))
                        else {
                            forEach {
                                add(GoalMixedItem(it))
                            }
                            add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.view_more_goals)))
                        }
                    }
                FeedContent.WELCOME -> {
                    add(WelcomeMixedItem("\uD83D\uDE00"))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MixedItemViewHolder {
        return adapters[MixedItemType.values()[viewType]]?.onCreateViewHolder(parent)
                ?: object : MixedItemViewHolder(View(parent.context), MixedItemType.Text) {}
    }

    override fun onBindViewHolder(holder: MixedItemViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = items[position]
        adapters[item.type]?.bind(holder, item, position)
    }

    override fun getItemViewType(position: Int) = items[position].type.ordinal

    override fun onViewRecycled(holder: MixedItemViewHolder) {
        adapters[MixedItemType.values()[holder.itemViewType]]?.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun getItemCount() = items.size
}
