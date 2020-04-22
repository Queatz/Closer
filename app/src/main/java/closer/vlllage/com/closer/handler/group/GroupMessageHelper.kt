package closer.vlllage.com.closer.handler.group

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.models.Event
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.Suggestion
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import com.queatz.on.On

class GroupMessageHelper constructor(private val on: On) {
    var onMessageClickListener: ((GroupMessage) -> Unit)? = null
    var onSuggestionClickListener: ((Suggestion) -> Unit)? = null
    var onEventClickListener: ((Event) -> Unit)? = null
    var onGroupClickListener: ((Group) -> Unit)? = null

    var pinned = false
    var global = false
    var inFeed = false

    fun areItemsTheSame(oldGroupMessage: GroupMessage, newGroupMessage: GroupMessage): Boolean {
        return oldGroupMessage.id == newGroupMessage.id ||
                oldGroupMessage.objectBoxId == newGroupMessage.objectBoxId
    }

    fun areContentsTheSame(oldGroupMessage: GroupMessage, newGroupMessage: GroupMessage): Boolean {
        return oldGroupMessage.id == newGroupMessage.id &&
                on<ListEqual>().isEqual(oldGroupMessage.reactions, newGroupMessage.reactions) &&
                oldGroupMessage.attachment == newGroupMessage.attachment &&
                oldGroupMessage.from == newGroupMessage.from &&
                oldGroupMessage.text == newGroupMessage.text
    }

    fun createViewHolder(parent: ViewGroup) = GroupMessageViewHolder(on, LayoutInflater.from(parent.context)
            .inflate(R.layout.message_item, parent, false)).also {
        it.disposableGroup = on<DisposableHandler>().group()
    }

    fun recycleViewHolder(holder: GroupMessageViewHolder) {
        holder.messageActionLayoutRevealAnimator?.cancel()
        holder.disposableGroup.clear()
    }

    fun onBind(groupMessage: GroupMessage, holder: GroupMessageViewHolder) {
        if (inFeed) {
            holder.itemView.setBackgroundResource(R.color.white)
            holder.itemView.elevation = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.elevation).toFloat()
        }

        if (pinned) {
            holder.itemView.setPadding(0, 0, 0, 0)
            holder.itemView.setBackgroundResource(R.color.dim)
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

            params = holder.messageRepliesCount.layoutParams as ViewGroup.MarginLayoutParams
            params.leftMargin = pad
            params.bottomMargin = pad
            holder.messageRepliesCount.layoutParams = params
        }

        holder.photo.visible = false
        holder.rating.visible = false
        holder.custom.visible = false

        holder.itemView.setOnClickListener {
            if (onMessageClickListener != null) {
                onMessageClickListener!!.invoke(groupMessage)
            } else {
                on<MessageDisplay>().toggleMessageActionLayout(holder)
            }
        }

        holder.itemView.setOnLongClickListener {
            on<MessageDisplay>().toggleMessageActionLayout(holder)
            true
        }

        holder.photo.setOnLongClickListener {
            on<MessageDisplay>().toggleMessageActionLayout(holder)
            true
        }

        holder.messageActionProfile.text = on<ResourcesHandler>().resources.getString(
                if (global) R.string.group else R.string.profile
        )

        holder.messageActionShare.visible = groupMessage.from != null
        holder.messageActionPin.visible = groupMessage.from != null
        holder.messageActionDelete.visible = groupMessage.from == on<PersistenceHandler>().phoneId

        holder.messageActionProfile.visible = global || groupMessage.from != null

        if (global) {
            holder.messageActionProfile.setOnClickListener {
                on<NavigationHandler>().showGroup(groupMessage.to!!)
                on<MessageDisplay>().toggleMessageActionLayout(holder)
            }
        } else if (groupMessage.from != null) {
            holder.messageActionProfile.setOnClickListener {
                on<NavigationHandler>().showProfile(groupMessage.from!!)
                on<MessageDisplay>().toggleMessageActionLayout(holder)
            }
        }

        holder.messageActionShare.setOnClickListener {
            on<ShareActivityTransitionHandler>().shareGroupMessage(groupMessage.id!!)
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionRemind.setOnClickListener {
            on<DefaultAlerts>().message("That doesn't work yet!")
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionReply.setOnClickListener {
            openGroup(groupMessage)
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionDelete.setOnClickListener {
            on<AlertHandler>().make().apply {
                message = on<ResourcesHandler>().resources.getString(R.string.delete_message_confirm)
                negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
                positiveButton = on<ResourcesHandler>().resources.getString(R.string.yes_delete)
                positiveButtonCallback = {
                    on<ApiHandler>().deleteGroupMessage(groupMessage.id!!).subscribe({
                        if (!it.success) {
                            on<DefaultAlerts>().thatDidntWork()
                        } else {
                            on<ToastHandler>().show(R.string.message_deleted)
                        }
                    }, { on<DefaultAlerts>().thatDidntWork() })
                    on<MessageDisplay>().toggleMessageActionLayout(holder)
                }
                show()
            }
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
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionVote.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "♥", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteLaugh.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE02", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteYummy.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE0B", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteKiss.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE18", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionVoteCool.setOnClickListener {
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "\uD83D\uDE0E", false)
                    .subscribe({
                        on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    }))
            on<MessageDisplay>().toggleMessageActionLayout(holder)
        }

        holder.messageActionLayout.visible = false
        holder.time.movementMethod = LinkMovementMethod.getInstance()
        holder.eventMessage.movementMethod = LinkMovementMethod.getInstance()

        holder.messageRepliesCount.visible = groupMessage.replies ?: 0 > 0
        if (groupMessage.replies ?: 0 > 0) {
            holder.messageRepliesCount.text = on<ResourcesHandler>().resources.getQuantityString(R.plurals.x_replies, groupMessage.replies ?: 0, groupMessage.replies ?: 0)
            holder.messageRepliesCount.setOnClickListener {
                openGroup(groupMessage)
            }
        }

        holder.pinned = pinned
        holder.global = global
        on<MessageDisplay>().display(holder, groupMessage, onEventClickListener!!, onGroupClickListener!!, onSuggestionClickListener!!)

        if (groupMessage.reactions.isNullOrEmpty()) {
            holder.reactionsRecyclerView.visible = false
        } else {
            holder.reactionsRecyclerView.visible = true
            holder.reactionAdapter.setItems(groupMessage.reactions)
            holder.reactionAdapter.setGroupMessage(groupMessage)
        }

        holder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            holder.messageActionProfile.setTextColor(it.text)
            holder.messageActionShare.setTextColor(it.text)
            holder.messageActionRemind.setTextColor(it.text)
            holder.messageActionPin.setTextColor(it.text)
            holder.messageActionReply.setTextColor(it.text)
            holder.messageActionDelete.setTextColor(it.text)
            holder.messageActionProfile.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionShare.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionRemind.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionPin.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionReply.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionDelete.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVote.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteLaugh.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteYummy.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteKiss.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVoteCool.setBackgroundResource(it.clickableRoundedBackground)
            holder.message.setTextColor(it.text)
            holder.name.setTextColor(it.text)
            holder.time.setTextColor(it.text)
            holder.messageRepliesCount.compoundDrawableTintList = it.tint
            holder.messageRepliesCount.setTextColor(it.text)
            holder.messageRepliesCount.setBackgroundResource(it.clickableRoundedBackground)
            holder.group.setTextColor(it.text)
            holder.action.setTextColor(it.text)
            holder.eventMessage.setTextColor(it.hint)
            holder.pinnedIndicator.imageTintList = it.tint
        })
    }

    private fun openGroup(groupMessage: GroupMessage) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupForGroupMessage(groupMessage.id!!).subscribe({ group ->
            on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id, true)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }))
    }
}

class GroupMessageViewHolder(on: On, itemView: View) : RecyclerView.ViewHolder(itemView) {

    internal var name: TextView
    internal var eventMessage: TextView
    internal var messageLayout: View
    internal var message: TextView
    internal var action: TextView
    internal var time: TextView
    internal var group: TextView
    internal var photo: ImageView
    internal var rating: RatingBar
    internal var custom: ConstraintLayout
    internal var pinnedIndicator: ImageView
    internal var messageRepliesCount: TextView
    internal var messageActionLayout: MaxSizeFrameLayout
    internal var messageActionProfile: TextView
    internal var messageActionShare: TextView
    internal var messageActionRemind: TextView
    internal var messageActionReply: TextView
    internal var messageActionDelete: TextView
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
    internal var pinned: Boolean = false
    internal var global: Boolean = false

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
        custom = itemView.findViewById(R.id.custom)
        pinnedIndicator = itemView.findViewById(R.id.pinnedIndicator)
        reactionsRecyclerView = itemView.findViewById(R.id.reactionsRecyclerView)
        messageRepliesCount = itemView.findViewById(R.id.messageRepliesCount)
        messageActionLayout = itemView.findViewById(R.id.messageActionLayout)
        messageActionProfile = itemView.findViewById(R.id.messageActionProfile)
        messageActionShare = itemView.findViewById(R.id.messageActionShare)
        messageActionRemind = itemView.findViewById(R.id.messageActionRemind)
        messageActionReply = itemView.findViewById(R.id.messageActionReply)
        messageActionDelete = itemView.findViewById(R.id.messageActionDelete)
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