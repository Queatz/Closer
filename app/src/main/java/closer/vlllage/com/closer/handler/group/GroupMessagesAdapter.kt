package closer.vlllage.com.closer.handler.group

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.Suggestion
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import com.queatz.on.On
import java.util.*

class GroupMessagesAdapter(on: On) : PoolRecyclerAdapter<GroupMessagesAdapter.GroupMessageViewHolder>(on) {

    var onMessageClickListener: ((GroupMessage) -> Unit)? = null
    var onSuggestionClickListener: ((Suggestion) -> Unit)? = null
    var onEventClickListener: ((Event) -> Unit)? = null
    var onGroupClickListener: ((Group) -> Unit)? = null
    private var groupMessages: List<GroupMessage> = ArrayList()
    var pinned: Boolean = false
    var global: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMessageViewHolder {
        return GroupMessageViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item, parent, false)).also {
            it.disposableGroup = on<DisposableHandler>().group()
        }
    }

    override fun onBindViewHolder(holder: GroupMessageViewHolder, position: Int) {
        val groupMessage = groupMessages[position]

        if (pinned) {
            holder.itemView.setPadding(0, 0, 0, 0)
            holder.itemView.setBackgroundResource(R.color.white_15)
            holder.messageLayout.background = null
            val pad = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
            holder.messageActionLayout.setPadding(
                    pad,
                    0,
                    pad,
                    pad
            )

            var params = holder.messageLayout.layoutParams as ViewGroup.MarginLayoutParams
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
        holder.rating.visibility = View.GONE

        holder.itemView.setOnClickListener {
            if (onMessageClickListener != null) {
                onMessageClickListener!!.invoke(groupMessages[position])
            } else {
                toggleMessageActionLayout(holder)
            }
        }

        holder.itemView.setOnLongClickListener {
            toggleMessageActionLayout(holder)
            true
        }

        holder.photo.setOnLongClickListener {
            toggleMessageActionLayout(holder)
            true
        }

        holder.messageActionReply.text = on<ResourcesHandler>().resources.getString(
                if (global) R.string.group else R.string.profile
        )

        holder.messageActionShare.visible = groupMessage.from != null
        holder.messageActionPin.visible = groupMessage.from != null

        holder.messageActionReply.visible = global || groupMessage.from != null

        if (global) {
            holder.messageActionReply.setOnClickListener {
                on<NavigationHandler>().showGroup(groupMessage.to!!)
                toggleMessageActionLayout(holder)
            }
        } else if (groupMessage.from != null) {
            holder.messageActionReply.setOnClickListener {
                on<NavigationHandler>().showProfile(groupMessage.from!!)
                toggleMessageActionLayout(holder)
            }
        }

        holder.messageActionShare.setOnClickListener {
            on<ShareActivityTransitionHandler>().shareGroupMessage(groupMessage.id!!)
            toggleMessageActionLayout(holder)
        }

        holder.messageActionRemind.setOnClickListener {
            on<DefaultAlerts>().message("That doesn't work yet!")
            toggleMessageActionLayout(holder)
        }

        holder.messageActionPin.setOnClickListener {
            if (pinned) {
                on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().removePin(groupMessage.id!!, groupMessage.to!!)
                        .subscribe({ successResult ->
                            if (!successResult.success) {
                                on<DefaultAlerts>().thatDidntWork()
                            } else {
                                on<RefreshHandler>().refreshPins(groupMessage.to!!)
                            }
                        }, { on<DefaultAlerts>().thatDidntWork() }))
            } else {
                on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>().addPin(groupMessage.id!!, groupMessage.to!!)
                        .subscribe({ successResult ->
                            if (!successResult.success) {
                                on<DefaultAlerts>().thatDidntWork()
                            } else {
                                on<RefreshHandler>().refreshPins(groupMessage.to!!)
                            }
                        }, { on<DefaultAlerts>().thatDidntWork() }))
            }
            toggleMessageActionLayout(holder)
        }

        holder.messageActionVote.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "♥", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteLaugh.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE02", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteYummy.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE0B", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteKiss.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE18", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteCool.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE0E", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionLayout.visibility = View.GONE
        holder.time.movementMethod = LinkMovementMethod.getInstance()

        on<MessageDisplay>().pinned = pinned
        on<MessageDisplay>().global = global
        on<MessageDisplay>().display(holder, groupMessage, onEventClickListener!!, onGroupClickListener!!, onSuggestionClickListener!!)

        if (groupMessage.reactions.isNullOrEmpty()) {
            holder.reactionsRecyclerView.visibility = View.GONE
        } else {
            holder.reactionsRecyclerView.visibility = View.VISIBLE
            holder.reactionAdapter.setItems(groupMessage.reactions)
            holder.reactionAdapter.setGroupMessage(groupMessage)
        }

        holder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            holder.messageActionReply.setTextColor(it.text)
            holder.messageActionShare.setTextColor(it.text)
            holder.messageActionRemind.setTextColor(it.text)
            holder.messageActionPin.setTextColor(it.text)
            holder.messageActionReply.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionShare.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionRemind.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionPin.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVote.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteLaugh.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteYummy.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteKiss.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteCool.setBackgroundResource(it.clickableRoundedBackground)
            holder.message.setTextColor(it.text)
            holder.name.setTextColor(it.text)
            holder.time.setTextColor(it.text)
            holder.group.setTextColor(it.text)
            holder.action.setTextColor(it.text)
            holder.eventMessage.setTextColor(it.text)
            holder.pinnedIndicator.imageTintList = it.tint
        })
    }

    private fun toggleMessageActionLayout(holder: GroupMessageViewHolder) {
        if (holder.messageActionLayoutRevealAnimator == null) {
            holder.messageActionLayoutRevealAnimator = RevealAnimator(holder.messageActionLayout, (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())
        }

        if (holder.messageActionLayout.visibility == View.VISIBLE) {
            holder.messageActionLayoutRevealAnimator!!.show(false)
        } else {
            holder.messageActionLayoutRevealAnimator!!.show(true)
        }
    }

    override fun onViewRecycled(holder: GroupMessageViewHolder) {
        holder.messageActionLayoutRevealAnimator?.cancel()
        holder.disposableGroup.clear()
    }

    override fun getItemCount(): Int {
        return groupMessages.size
    }

    fun setGroupMessages(groupMessages: List<GroupMessage>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = this@GroupMessagesAdapter.groupMessages.size
            override fun getNewListSize() = groupMessages.size

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return this@GroupMessagesAdapter.groupMessages[oldPosition].objectBoxId == groupMessages[newPosition].objectBoxId ||
                        this@GroupMessagesAdapter.groupMessages[oldPosition].id == groupMessages[newPosition].id
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                val oldGroupMessage = this@GroupMessagesAdapter.groupMessages[oldPosition]
                val newGroupMessage = groupMessages[newPosition]

                return oldGroupMessage.id == newGroupMessage.id &&
                        on<ListEqual>().isEqual(oldGroupMessage.reactions, newGroupMessage.reactions) &&
                        oldGroupMessage.attachment == newGroupMessage.attachment &&
                        oldGroupMessage.from == newGroupMessage.from &&
                        oldGroupMessage.text == newGroupMessage.text
            }
        })
        this.groupMessages = groupMessages
        diffResult.dispatchUpdatesTo(this)
    }

    inner class GroupMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var name: TextView
        internal var eventMessage: TextView
        internal var messageLayout: View
        internal var message: TextView
        internal var action: TextView
        internal var time: TextView
        internal var group: TextView
        internal var photo: ImageView
        internal var rating: RatingBar
        internal var pinnedIndicator: ImageView
        internal var messageActionLayout: MaxSizeFrameLayout
        internal var messageActionReply: TextView
        internal var messageActionShare: TextView
        internal var messageActionRemind: TextView
        internal var messageActionPin: TextView
        internal var messageActionVote: TextView
        internal var messageActionVoteLaugh: TextView
        internal var messageActionVoteYummy: TextView
        internal var messageActionVoteKiss: TextView
        internal var messageActionVoteCool: TextView
        internal var messageActionLayoutRevealAnimator: RevealAnimator? = null
        internal var reactionsRecyclerView: RecyclerView
        internal var reactionAdapter: ReactionAdapter
        internal lateinit var disposableGroup: DisposableGroup

        init {
            name = itemView.findViewById(R.id.name)
            eventMessage = itemView.findViewById(R.id.eventMessage)
            messageLayout = itemView.findViewById(R.id.messageLayout)
            message = itemView.findViewById(R.id.message)
            action = itemView.findViewById(R.id.action)
            time = itemView.findViewById(R.id.time)
            group = itemView.findViewById(R.id.group)
            photo = itemView.findViewById(R.id.photo)
            rating = itemView.findViewById(R.id.rating)
            pinnedIndicator = itemView.findViewById(R.id.pinnedIndicator)
            reactionsRecyclerView = itemView.findViewById(R.id.reactionsRecyclerView)
            messageActionLayout = itemView.findViewById(R.id.messageActionLayout)
            messageActionReply = itemView.findViewById(R.id.messageActionReply)
            messageActionShare = itemView.findViewById(R.id.messageActionShare)
            messageActionRemind = itemView.findViewById(R.id.messageActionReminder)
            messageActionPin = itemView.findViewById(R.id.messageActionPin)
            messageActionVote = itemView.findViewById(R.id.messageActionVote)
            messageActionVoteLaugh = itemView.findViewById(R.id.messageActionVoteLaugh)
            messageActionVoteYummy = itemView.findViewById(R.id.messageActionVoteYummy)
            messageActionVoteKiss = itemView.findViewById(R.id.messageActionVoteKiss)
            messageActionVoteCool = itemView.findViewById(R.id.messageActionVoteCool)

            reactionsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            reactionAdapter = ReactionAdapter(on)
            reactionsRecyclerView.adapter = reactionAdapter
        }
    }
}
