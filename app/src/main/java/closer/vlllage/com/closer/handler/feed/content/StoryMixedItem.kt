package closer.vlllage.com.closer.handler.feed.content

import android.animation.ObjectAnimator
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.StoryItemBinding
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.math.abs


class StoryMixedItem(val story: Story) : MixedItem(MixedItemType.Story)

class StoryViewHolder(val binding: StoryItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.Story) {
    lateinit var on: On
}

class StoryMixedItemAdapter(private val on: On) : MixedItemAdapter<StoryMixedItem, StoryViewHolder> {
    @SuppressLint("ClickableViewAccessibility")
    override fun bind(holder: StoryViewHolder, item: StoryMixedItem, position: Int) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
        }

        holder.binding.text.movementMethod = ScrollingMovementMethod.getInstance()

        holder.binding.text.apply {
            post { scrollTo(0, 0) }
        }

        var isChildScrolling = false
        val originPosition = Point()

        holder.binding.text.setOnTouchListener { v, event ->
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

        holder.binding.replyMessage.setOnEditorActionListener(null)
        holder.binding.sendButton.setOnClickListener(null)

        updateSendIcon(holder)

        holder.binding.replyMessage.doAfterTextChanged {
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

        holder.binding.photo.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = null
        }

        var zoomed = false

        val onclick = { _: View ->
            val aspect = holder.itemView.measuredHeight.toFloat() / holder.itemView.measuredWidth.toFloat()
            val photoAspect = holder.binding.photo.drawable.intrinsicHeight.toFloat() / holder.binding.photo.drawable.intrinsicWidth.toFloat()
            (if (!zoomed) ObjectAnimator.ofFloat(aspect, photoAspect) else ObjectAnimator.ofFloat(photoAspect, aspect))
                .apply {
                    if (zoomed) doOnEnd {
                        holder.binding.photo.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            dimensionRatio = null
                        }
                    }
                    addUpdateListener {
                        holder.binding.photo.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            dimensionRatio = "1:${it.animatedValue}"
                        }
                    }
                }
                .start()

            zoomed = !zoomed
        }

        holder.binding.photo.setOnClickListener(onclick)
        holder.binding.text.setOnClickListener(onclick)

        item.story.photo?.let {
            holder.binding.photo.visible = true

            on<ImageHandler>().get().load("${it}?s=512")
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.binding.photo)
        } ?: run {
            holder.binding.photo.visible = false
            on<ImageHandler>().get().clear(holder.binding.photo)
        }

        val isMe = on<PersistenceHandler>().phoneId == item.story.creator

        holder.binding.replyMessage.isEnabled = !isMe
        holder.binding.replyMessage.alpha = if (isMe) .5f else 1f

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

        holder.binding.replyMessage.hint = on<ResourcesHandler>().resources.getString(R.string.reply_to_x, name)
        holder.binding.text.text = story.text

        holder.binding.name.text = "$name • ${on<TimeStr>().tiny(story.created)}"

        holder.binding.activeNowIndicator.visible = on<TimeAgo>().fifteenMinutesAgo().before(phone.updated
                ?: Date(0))

        if (phone.photo.isNullOrBlank()) {
            holder.binding.profilePhoto.setImageResource(R.drawable.ic_person_black_24dp)
            holder.binding.profilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
        } else {
            holder.binding.profilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            on<PhotoHelper>().loadCircle(holder.binding.profilePhoto, "${phone.photo}?s=64")
        }

        holder.binding.profilePhoto.setOnClickListener { view ->
            on<GroupActivityTransitionHandler>().showGroupForPhone(view, phone.id!!)
        }

        holder.binding.replyMessage.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendDirectMessage(holder, story, name)
                true
            } else {
                false
            }
        }

        holder.binding.sendButton.setOnClickListener {
            if (holder.binding.replyMessage.text.toString().isNotBlank()) {
                sendDirectMessage(holder, story, name)
            } else {
                on<ShareActivityTransitionHandler>().shareStoryToGroup(story.id!!)
            }
        }
    }

    private fun updateSendIcon(holder: StoryViewHolder) {
        if (holder.binding.replyMessage.text.toString().isNotBlank()) {
            holder.binding.sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp)
        } else {
            holder.binding.sendButton.setImageResource(R.drawable.ic_share_black_24dp)
        }
    }

    private fun sendDirectMessage(holder: StoryViewHolder, story: Story, phoneName: String) {
        holder.binding.replyMessage.text.toString().takeIf { it.isNotBlank() }?.let { text ->
            holder.binding.replyMessage.setText("")
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
                        holder.binding.replyMessage.setText(text)
                        on<DefaultAlerts>().thatDidntWork()
                    })
        }
    }

    override fun getMixedItemClass() = StoryMixedItem::class
    override fun getMixedItemType() = MixedItemType.Story

    override fun areItemsTheSame(old: StoryMixedItem, new: StoryMixedItem) = old.story.objectBoxId == new.story.objectBoxId

    override fun areContentsTheSame(old: StoryMixedItem, new: StoryMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = StoryViewHolder(StoryItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: StoryViewHolder) {
        holder.on.off()
        holder.binding.replyMessage.setText("")
    }
}
