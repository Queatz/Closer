package closer.vlllage.com.closer.handler.feed

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.feed.content.*
import closer.vlllage.com.closer.handler.helpers.CreateGroupHelper
import closer.vlllage.com.closer.handler.helpers.ProfileHelper
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.map.FeedHandler
import closer.vlllage.com.closer.handler.map.HeaderAdapter
import closer.vlllage.com.closer.store.models.*
import com.queatz.on.On
import java.util.*

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

    var content: FeedContent = FeedContent.POSTS
        set(value) {
            field = value
            generate()
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
            on<WelcomeMixedItemAdapter>()
    ).map { Pair(it.getMixedItemType(), it as MixedItemAdapter<MixedItem, MixedItemViewHolder>) }.toMap()

    private fun generate() {
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
                FeedContent.POSTS -> groupMessages.apply {
                    if (isEmpty()) empty(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.nothing_around_here)))
                    else {
                        forEach { add(GroupMessageMixedItem(it)) }
                        add(TextMixedItem(on<ResourcesHandler>().resources.getString(R.string.view_more_conversations)))
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
                    add(WelcomeMixedItem("<b>Welcome to Closer, ${on<AccountHandler>().name.takeIf { it.isNotBlank() } ?: "Human"}!</b> \uD83D\uDC4B\n\n" +
                            "\uD83D\uDC6F Closer is an amazing way to connect and stay in touch with people in your area.\n\n" +
                            "\uD83D\uDE00 Don't worry if there's not a lot going on around you yet, there's still plenty to do!\n\n" +
                            "You can scroll through the tabs above to explore your area.\n\nBut first, let's get you familiar with a few things!"))

                    when (groups.size < 5) {
                        true -> add(WelcomeMixedItem("<b>Communities</b> are the best way to connect with people around topics. Join one or create your own community for others to join!\n\nIt looks like there aren't many communities in your area, why not create one yourself?", "Create a community") {
                            on<CreateGroupHelper>().createGroup(null, true)
                        })
                        false -> add(WelcomeMixedItem("<b>Communities</b> are the best way to connect with people around topics. Join one or create your own community for others to join!\n\nIt looks like there are ${groups.size} communities in your area, that's a lot!", "See communities in my area") {
                            on<FeedHandler>().show(ContentViewType.HOME_GROUPS)
                        })
                    }

                    when (groupActions.size < 5) {
                        false -> add(WelcomeMixedItem("Browse <b>Activities</b> to find things to do in your area.\n\nIt looks like there are ${groupActions.size} activities in your area, that's a lot!", "See activities in my area") {
                            on<FeedHandler>().show(ContentViewType.HOME_ACTIVITIES)
                        })
                    }

                    when (lifestyles.size > 0) {
                        true -> add(WelcomeMixedItem("<b>Lifestyles</b> are a great way to get to know people! Join one or create your own.\n\nIt looks like there are ${lifestyles.size} lifestyles in your area, why not go check them out?", "See lifestyles in my area") {
                            on<FeedHandler>().show(ContentViewType.HOME_LIFESTYLES)
                        })
                        false -> add(WelcomeMixedItem("<b>Lifestyles</b> are a great way to get to know people! Join one or create your own.\n\nIt looks like there are no lifestyles in your area, why not join one yourself?", "Join a lifestyle") {
                            on<ProfileHelper>().joinLifestyle(on<PersistenceHandler>().phoneId!!)
                        })
                    }

                    when (goals.size > 0) {
                        true -> add(WelcomeMixedItem("<b>Goals</b> help you know what people are going for around you.  Share someone's goal, or make your own.\n\nIt looks like there are ${goals.size} goals in your area, why not go check them out?", "See goals in my area") {
                            on<FeedHandler>().show(ContentViewType.HOME_GOALS)
                        })
                        false -> add(WelcomeMixedItem("<b>Goals</b> help you know what people are going for around you.  Share someone's goal, or make your own.\n\nIt looks like there are no goals in your area, why not join one yourself?", "Add a goal") {
                            on<ProfileHelper>().addGoal(on<PersistenceHandler>().phoneId!!)
                        })
                    }

                    add(WelcomeMixedItem("That's it for now! Enjoy! \uD83D\uDE00"))
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
