package closer.vlllage.com.closer.handler.feed.content

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.Rect
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.core.widget.doAfterTextChanged
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.feed.FeedVisibilityHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.GroupMessageAttachmentHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Story
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.story_item.view.*
import java.util.*
import kotlin.math.abs


class StoryMixedItem(val story: Story) : MixedItem(MixedItemType.Story)

class StoryViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.Story) {
    lateinit var on: On
}

class StoryMixedItemAdapter(private val on: On) : MixedItemAdapter<StoryMixedItem, StoryViewHolder> {
    @SuppressLint("ClickableViewAccessibility")
    override fun bind(holder: StoryViewHolder, item: StoryMixedItem, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.itemView.text.movementMethod = ScrollingMovementMethod.getInstance()

        holder.itemView.text.apply {
            post { scrollTo(0, 0) }
        }

        var isChildScrolling = false
        val originPosition = Point()

        holder.itemView.text.setOnTouchListener { v, event ->
            if (!v.canScrollVertically(-1) && !v.canScrollVertically(1)) {
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_UP -> isChildScrolling = false
                MotionEvent.ACTION_DOWN -> {
                    originPosition.x = event.rawX.toInt()
                    originPosition.y = event.rawY.toInt()
                    isChildScrolling = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - originPosition.x).toInt()
                    val deltaY = (event.rawY - originPosition.y).toInt()

                    if (abs(deltaY) > 8 || abs(deltaX) > 8) {
                        isChildScrolling = if (abs(deltaY) > abs(deltaX)) {
                            if (deltaY > 0 && v.canScrollVertically(-1)) {
                                true
                            } else
                                deltaY < 0 && v.canScrollVertically(1)
                        } else {
                            false
                        }
                    }
                }
            }

            if (isChildScrolling) {
                v.parent?.requestDisallowInterceptTouchEvent(true)
            }

            false
        }

        holder.itemView.replyMessage.setOnEditorActionListener(null)
        holder.itemView.sendButton.setOnClickListener(null)

        updateSendIcon(holder)

        holder.itemView.replyMessage.doAfterTextChanged {
            updateSendIcon(holder)
        }

        item.story.phone?.let {
            showPhone(holder, item.story, it)
        } ?: run {
            on<DataHandler>().getPhone(item.story.creator!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showPhone(holder, item.story, it)
                    }, {
                        on<DefaultAlerts>().thatDidntWork()
                    })
        }

        item.story.photo?.let {
            holder.itemView.photo.visible = true

            on<ImageHandler>().get().load("${it}?s=512")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.itemView.photo)
        } ?: run {
            holder.itemView.photo.visible = false
            on<ImageHandler>().get().clear(holder.itemView.photo)
        }

        val isMe = on<PersistenceHandler>().phoneId == item.story.creator

        holder.itemView.replyMessage.isEnabled = !isMe
        holder.itemView.replyMessage.alpha = if (isMe) .5f else 1f

        on<FeedVisibilityHandler>().positionOnScreen
                .filter { it == position }
                .filter {
                    val r = Rect()
                    holder.itemView.getLocalVisibleRect(r)
                    r.height() / holder.itemView.height > .75f
                }
                .take(1)
                .subscribe({
                    on<ApiHandler>().storyViewed(item.story.id!!).subscribe({}, {}).also {
                        on<DisposableHandler>().add(it)
                    }
                }, {}).also {
                    holder.on<DisposableHandler>().add(it)
                }
    }

    private fun showPhone(holder: StoryViewHolder, story: Story, phone: Phone) {
        val name = on<NameHandler>().getName(phone)

        holder.itemView.replyMessage.hint = on<ResourcesHandler>().resources.getString(R.string.reply_to_x, name)
        holder.itemView.text.text = story.text

        holder.itemView.name.text = "$name • ${on<TimeStr>().tiny(story.created)}"

        holder.itemView.activeNowIndicator.visible = on<TimeAgo>().fifteenMinutesAgo().before(phone.updated
                ?: Date(0))

        if (phone.photo.isNullOrBlank()) {
            holder.itemView.profilePhoto.setImageResource(R.drawable.ic_person_black_24dp)
            holder.itemView.profilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } else {
            holder.itemView.profilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            on<PhotoHelper>().loadCircle(holder.itemView.profilePhoto, "${phone.photo}?s=64")
        }

        holder.itemView.profilePhoto.setOnClickListener { view ->
            on<GroupActivityTransitionHandler>().showGroupForPhone(view, phone.id!!)
        }

        holder.itemView.replyMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendDirectMessage(holder, story, name)
                true
            } else {
                false
            }
        }

        holder.itemView.sendButton.setOnClickListener {
            if (holder.itemView.replyMessage.text.toString().isNotBlank()) {
                sendDirectMessage(holder, story, name)
            } else {
                on<ShareActivityTransitionHandler>().shareStoryToGroup(story.id!!)
            }
        }
    }

    private fun updateSendIcon(holder: StoryViewHolder) {
        if (holder.itemView.replyMessage.text.toString().isNotBlank()) {
            holder.itemView.sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp)
        } else {
            holder.itemView.sendButton.setImageResource(R.drawable.ic_share_black_24dp)
        }
    }

    private fun sendDirectMessage(holder: StoryViewHolder, story: Story, phoneName: String) {
        holder.itemView.replyMessage.text.toString().takeIf { it.isNotBlank() }?.let { text ->
            holder.itemView.replyMessage.setText("")
            on<DataHandler>().getDirectGroup(story.creator!!).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        on<GroupMessageAttachmentHandler>().shareStory(story, it)

                        val groupMessage = GroupMessage()
                        groupMessage.text = text
                        groupMessage.from = on<PersistenceHandler>().phoneId
                        groupMessage.to = it.id
                        groupMessage.created = Date()
                        on<SyncHandler>().sync(groupMessage)
                        on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.message_sent_to_x, phoneName))
                    }, {
                        holder.itemView.replyMessage.setText(text)
                        on<DefaultAlerts>().thatDidntWork()
                    })
        }
    }

    override fun getMixedItemClass() = StoryMixedItem::class
    override fun getMixedItemType() = MixedItemType.Story

    override fun areItemsTheSame(old: StoryMixedItem, new: StoryMixedItem) = old.story.objectBoxId == new.story.objectBoxId

    override fun areContentsTheSame(old: StoryMixedItem, new: StoryMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = StoryViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.story_item, parent, false))

    override fun onViewRecycled(holder: StoryViewHolder) {
        holder.on.off()
        holder.itemView.replyMessage.setText("")
    }
}
