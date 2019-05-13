package closer.vlllage.com.closer.handler.group

import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import java.util.*

class GroupMessagesAdapter(poolMember: PoolMember) : PoolRecyclerAdapter<GroupMessagesAdapter.GroupMessageViewHolder>(poolMember) {

    var onMessageClickListener: ((GroupMessage) -> Unit)? = null
    var onSuggestionClickListener: ((Suggestion) -> Unit)? = null
    var onEventClickListener: ((Event) -> Unit)? = null
    var onGroupClickListener: ((Group) -> Unit)? = null
    private var groupMessages: List<GroupMessage> = ArrayList()
    private var pinned: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMessageViewHolder {
        return GroupMessageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item, parent, false))
    }

    override fun onBindViewHolder(holder: GroupMessageViewHolder, position: Int) {
        val groupMessage = groupMessages[position]

        if (pinned) {
            holder.itemView.setPadding(0, 0, 0, 0)
            holder.itemView.setBackgroundResource(R.color.white_15)
            holder.messageLayout.background = null
            val pad = `$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.pad)
            holder.messageActionLayout.setPadding(
                    pad,
                    0,
                    pad,
                    pad
            )

            var params: ViewGroup.MarginLayoutParams
            params = holder.messageLayout.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            holder.messageLayout.layoutParams = params

            holder.reactionsRecyclerView.setPadding(pad, 0, pad, 0)

            params = holder.action.layoutParams as ViewGroup.MarginLayoutParams
            params.leftMargin = pad
            params.bottomMargin = pad
            holder.action.layoutParams = params

            params = holder.reactionsRecyclerView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            holder.reactionsRecyclerView.layoutParams = params
        }

        holder.photo.visibility = View.GONE

        holder.itemView.setOnClickListener { view ->
            if (onMessageClickListener != null) {
                onMessageClickListener!!.invoke(groupMessages[position])
            } else {
                toggleMessageActionLayout(holder)
            }
        }

        holder.itemView.setOnLongClickListener { view ->
            toggleMessageActionLayout(holder)
            true
        }

        holder.photo.setOnLongClickListener { view ->
            toggleMessageActionLayout(holder)
            true
        }

        holder.messageActionReply.setOnClickListener { view ->
            `$`(PhoneMessagesHandler::class.java).openMessagesWithPhone(groupMessage.from!!, `$`(NameHandler::class.java).getName(groupMessage.from!!), "")
            toggleMessageActionLayout(holder)
        }
        holder.messageActionShare.setOnClickListener { view ->
            `$`(ShareActivityTransitionHandler::class.java).shareGroupMessage(groupMessage.id!!)
            toggleMessageActionLayout(holder)
        }
        holder.messageActionRemind.setOnClickListener { view ->
            `$`(DefaultAlerts::class.java).message("That doesn't work yet!")
            toggleMessageActionLayout(holder)
        }
        holder.messageActionPin.setOnClickListener { view ->
            if (pinned) {
                `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).removePin(groupMessage.id!!, groupMessage.to!!)
                        .subscribe({ successResult ->
                            if (!successResult.success) {
                                `$`(DefaultAlerts::class.java).thatDidntWork()
                            } else {
                                `$`(RefreshHandler::class.java).refreshPins(groupMessage.to!!)
                            }
                        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
            } else {
                `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).addPin(groupMessage.id!!, groupMessage.to!!)
                        .subscribe({ successResult ->
                            if (!successResult.success) {
                                `$`(DefaultAlerts::class.java).thatDidntWork()
                            } else {
                                `$`(RefreshHandler::class.java).refreshPins(groupMessage.to!!)
                            }
                        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
            }
            toggleMessageActionLayout(holder)
        }
        holder.messageActionVote.setOnClickListener { view ->
            `$`(ApplicationHandler::class.java).app.`$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java)
                    .reactToMessage(groupMessage.id!!, "♥", false)
                    .subscribe({ successResult -> `$`(RefreshHandler::class.java).refreshGroupMessage(groupMessage.id!!) }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionLayout.visibility = View.GONE

        `$`(MessageDisplay::class.java).setPinned(pinned)
        `$`(MessageDisplay::class.java).display(holder, groupMessage, onEventClickListener!!, onGroupClickListener!!, onSuggestionClickListener!!)

        if (groupMessage.reactions.isEmpty()) {
            holder.reactionsRecyclerView.visibility = View.GONE
        } else {
            holder.reactionsRecyclerView.visibility = View.VISIBLE
            holder.reactionAdapter.setItems(groupMessage.reactions)
            holder.reactionAdapter.setGroupMessage(groupMessage)
        }
    }

    private fun toggleMessageActionLayout(holder: GroupMessageViewHolder) {
        if (holder.messageActionLayoutRevealAnimator == null) {
            holder.messageActionLayoutRevealAnimator = RevealAnimator(holder.messageActionLayout, (`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())
        }

        if (holder.messageActionLayout.visibility == View.VISIBLE) {
            holder.messageActionLayoutRevealAnimator!!.show(false)
        } else {
            holder.messageActionLayoutRevealAnimator!!.show(true)
        }
    }

    override fun onViewRecycled(holder: GroupMessageViewHolder) {
        if (holder.messageActionLayoutRevealAnimator != null) {
            holder.messageActionLayoutRevealAnimator!!.cancel()
        }
    }

    override fun getItemCount(): Int {
        return groupMessages.size
    }

    fun setGroupMessages(groupMessages: List<GroupMessage>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@GroupMessagesAdapter.groupMessages.size
            }

            override fun getNewListSize(): Int {
                return groupMessages.size
            }

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@GroupMessagesAdapter.groupMessages[oldPosition].objectBoxId == groupMessages[newPosition].objectBoxId ||
                        this@GroupMessagesAdapter.groupMessages[oldPosition].id == groupMessages[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                val oldGroupMessage = this@GroupMessagesAdapter.groupMessages[oldPosition]
                val newGroupMessage = groupMessages[newPosition]

                return oldGroupMessage.id == newGroupMessage.id &&
                        `$`(ListEqual::class.java).isEqual(oldGroupMessage.reactions, newGroupMessage.reactions) &&
                        oldGroupMessage.attachment == newGroupMessage.attachment &&
                        oldGroupMessage.from == newGroupMessage.from &&
                        oldGroupMessage.text == newGroupMessage.text
            }
        })
        this.groupMessages = groupMessages
        diffResult.dispatchUpdatesTo(this)
    }

    fun setPinned(pinned: Boolean): GroupMessagesAdapter {
        this.pinned = pinned
        return this
    }

    inner class GroupMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var name: TextView
        internal var eventMessage: TextView
        internal var messageLayout: View
        internal var message: TextView
        internal var action: TextView
        internal var time: TextView
        internal var photo: ImageView
        internal var pinnedIndicator: ImageView
        internal var messageActionLayout: MaxSizeFrameLayout
        internal var messageActionReply: TextView
        internal var messageActionShare: TextView
        internal var messageActionRemind: TextView
        internal var messageActionPin: TextView
        internal var messageActionVote: TextView
        internal var messageActionLayoutRevealAnimator: RevealAnimator? = null
        internal var reactionsRecyclerView: RecyclerView
        internal var reactionAdapter: ReactionAdapter

        init {
            name = itemView.findViewById(R.id.name)
            eventMessage = itemView.findViewById(R.id.eventMessage)
            messageLayout = itemView.findViewById(R.id.messageLayout)
            message = itemView.findViewById(R.id.message)
            action = itemView.findViewById(R.id.action)
            time = itemView.findViewById(R.id.time)
            photo = itemView.findViewById(R.id.photo)
            pinnedIndicator = itemView.findViewById(R.id.pinnedIndicator)
            reactionsRecyclerView = itemView.findViewById(R.id.reactionsRecyclerView)
            messageActionLayout = itemView.findViewById(R.id.messageActionLayout)
            messageActionReply = itemView.findViewById(R.id.messageActionReply)
            messageActionShare = itemView.findViewById(R.id.messageActionShare)
            messageActionRemind = itemView.findViewById(R.id.messageActionReminder)
            messageActionPin = itemView.findViewById(R.id.messageActionPin)
            messageActionVote = itemView.findViewById(R.id.messageActionVote)

            reactionsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            reactionAdapter = ReactionAdapter(`$pool`())
            reactionsRecyclerView.adapter = reactionAdapter
        }
    }

    private fun getGroup(groupId: String?): Group? {
        return if (groupId == null) {
            null
        } else `$`(StoreHandler::class.java).store.box(Group::class.java).query()
                .equal(Group_.id, groupId)
                .build()
                .findFirst()

    }
}
