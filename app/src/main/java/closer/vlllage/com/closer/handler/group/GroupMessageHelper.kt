package closer.vlllage.com.closer.handler.group

import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.helpers.*
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
                on<MessageDisplay>().toggleMessageActionLayout(groupMessage, holder)
            }
        }

        holder.itemView.setOnLongClickListener {
            on<MessageDisplay>().toggleMessageActionLayout(groupMessage, holder)
            true
        }

        holder.photo.setOnLongClickListener {
            on<MessageDisplay>().toggleMessageActionLayout(groupMessage, holder)
            true
        }

        holder.messageActionLayout.visible = false
        holder.time.movementMethod = LinkMovementMethod.getInstance()
        holder.eventMessage.movementMethod = LinkMovementMethod.getInstance()

        holder.messageLayout.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }
        holder.custom.updateLayoutParams { width = ViewGroup.LayoutParams.WRAP_CONTENT }

        val replyCount = groupMessage.replies?.let { it - 1 } ?: 0

        holder.messageRepliesCount.visible = replyCount > 0
        if (replyCount > 0) {
            holder.messageRepliesCount.text = on<ResourcesHandler>().resources.getQuantityString(R.plurals.x_replies, replyCount, replyCount)
            holder.messageRepliesCount.setOnClickListener {
                on<MessageDisplay>().openGroup(groupMessage)
            }
        }

        holder.pinned = pinned
        holder.global = global
        holder.inFeed = inFeed
        on<MessageDisplay>().display(holder, groupMessage, onEventClickListener!!, onGroupClickListener!!, onSuggestionClickListener!!)

        if (groupMessage.reactions.isNullOrEmpty()) {
            holder.reactionsRecyclerView.visible = false
        } else {
            holder.reactionsRecyclerView.visible = true
            holder.reactionAdapter.setItems(groupMessage.reactions)
            holder.reactionAdapter.setGroupMessage(groupMessage)
        }

        holder.disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            holder.messageActionShorthand.setTextColor(it.text)
            holder.messageActionProfile.setTextColor(it.text)
            holder.messageActionShare.setTextColor(it.text)
            holder.messageActionRemind.setTextColor(it.text)
            holder.messageActionPin.setTextColor(it.text)
            holder.messageActionReply.setTextColor(it.text)
            holder.messageActionDelete.setTextColor(it.text)
            holder.messageActionShorthand.setBackgroundResource(it.clickableRoundedBackground)
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
            holder.messageActionShorthand.compoundDrawableTintList = it.tint
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
}

class GroupMessageViewHolder(on: On, itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var on: On

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
    internal var messageActionShorthand: TextView
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
    internal var inFeed: Boolean = false

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
        messageActionShorthand = itemView.findViewById(R.id.messageActionShorthand)
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