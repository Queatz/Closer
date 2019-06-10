package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
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
    private var pinned: Boolean = false

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
            on<PhoneMessagesHandler>().openMessagesWithPhone(groupMessage.from!!, on<NameHandler>().getName(groupMessage.from!!), "")
            toggleMessageActionLayout(holder)
        }
        holder.messageActionShare.setOnClickListener { view ->
            on<ShareActivityTransitionHandler>().shareGroupMessage(groupMessage.id!!)
            toggleMessageActionLayout(holder)
        }
        holder.messageActionRemind.setOnClickListener { view ->
            on<DefaultAlerts>().message("That doesn't work yet!")
            toggleMessageActionLayout(holder)
        }
        holder.messageActionPin.setOnClickListener { view ->
            if (pinned) {
                on<DisposableHandler>().add(on<ApiHandler>().removePin(groupMessage.id!!, groupMessage.to!!)
                        .subscribe({ successResult ->
                            if (!successResult.success) {
                                on<DefaultAlerts>().thatDidntWork()
                            } else {
                                on<RefreshHandler>().refreshPins(groupMessage.to!!)
                            }
                        }, { error -> on<DefaultAlerts>().thatDidntWork() }))
            } else {
                on<DisposableHandler>().add(on<ApiHandler>().addPin(groupMessage.id!!, groupMessage.to!!)
                        .subscribe({ successResult ->
                            if (!successResult.success) {
                                on<DefaultAlerts>().thatDidntWork()
                            } else {
                                on<RefreshHandler>().refreshPins(groupMessage.to!!)
                            }
                        }, { error -> on<DefaultAlerts>().thatDidntWork() }))
            }
            toggleMessageActionLayout(holder)
        }
        holder.messageActionVote.setOnClickListener { view ->
            on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                    .reactToMessage(groupMessage.id!!, "♥", false)
                    .subscribe({ successResult -> on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!) }, { error -> on<DefaultAlerts>().thatDidntWork() }))
            toggleMessageActionLayout(holder)
        }

        holder.messageActionLayout.visibility = View.GONE

        on<MessageDisplay>().setPinned(pinned)
        on<MessageDisplay>().display(holder, groupMessage, onEventClickListener!!, onGroupClickListener!!, onSuggestionClickListener!!)

        if (groupMessage.reactions.isEmpty()) {
            holder.reactionsRecyclerView.visibility = View.GONE
        } else {
            holder.reactionsRecyclerView.visibility = View.VISIBLE
            holder.reactionAdapter.setItems(groupMessage.reactions)
            holder.reactionAdapter.setGroupMessage(groupMessage)
        }

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            holder.messageActionReply.setTextColor(it.text)
            holder.messageActionShare.setTextColor(it.text)
            holder.messageActionRemind.setTextColor(it.text)
            holder.messageActionPin.setTextColor(it.text)
            holder.messageActionVote.setTextColor(it.text)
            holder.messageActionReply.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionShare.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionRemind.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionPin.setBackgroundResource(it.clickableRoundedBackground)
            holder.messageActionVote.setBackgroundResource(it.clickableRoundedBackground)
            holder.message.setTextColor(it.text)
            holder.name.setTextColor(it.text)
            holder.time.setTextColor(it.text)
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
        if (holder.messageActionLayoutRevealAnimator != null) {
            holder.messageActionLayoutRevealAnimator!!.cancel()
        }
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
        internal lateinit var disposableGroup: DisposableGroup

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
            reactionAdapter = ReactionAdapter(on)
            reactionsRecyclerView.adapter = reactionAdapter
        }
    }

    private fun getGroup(groupId: String?): Group? {
        return if (groupId == null) {
            null
        } else on<StoreHandler>().store.box(Group::class.java).query()
                .equal(Group_.id, groupId)
                .build()
                .findFirst()

    }
}
