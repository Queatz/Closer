package closer.vlllage.com.closer.handler.group

import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameCacheHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import closer.vlllage.com.closer.ui.RevealAnimatorForConstraintLayout
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.queatz.on.On
import com.vdurmont.emoji.EmojiManager
import io.objectbox.query.QueryBuilder
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*


class MessageDisplay constructor(private val on: On) {

    private fun displayShare(holder: GroupMessageViewHolder,
                             jsonObject: JsonObject,
                             groupMessage: GroupMessage,
                             onEventClickListener: (Event) -> Unit,
                             onGroupClickListener: (Group) -> Unit,
                             onSuggestionClickListener: (Suggestion) -> Unit) {
        displayFallback(holder, groupMessage)
        holder.showGroupInsteadOfProfile = null

        holder.disposableGroup.add(on<DataHandler>().getGroupMessage(jsonObject.get("share").asString).subscribe({ sharedGroupMessage ->
            display(holder, sharedGroupMessage, null, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
            holder.time.text = on<GroupMessageParseHandler>().parseText(holder.time, on<ResourcesHandler>().resources.getString(R.string.shared_by, on<TimeStr>().pretty(groupMessage.created), "@" + groupMessage.from!!))

            val group = sharedGroupMessage.to?.let { getGroup(it) }
            holder.showGroupInsteadOfProfile = group?.id

            holder.group.visible = true
            holder.group.text = on<ResourcesHandler>().resources.getString(R.string.from, group?.name?.let { if (it.isBlank()) on<ResourcesHandler>().resources.getString(R.string.unknown) else group.name } ?: on<ResourcesHandler>().resources.getString(R.string.unknown))

            holder.messageActionProfile.setText(R.string.group)
            holder.messageActionProfile.setOnClickListener {
                on<NavigationHandler>().showGroup(sharedGroupMessage.to!!)
                toggleMessageActionLayout(groupMessage, holder)
            }
        }, {
            holder.message.setText(R.string.unavailable_message)
            holder.message.visible = true
        }))

        holder.time.text = on<GroupMessageParseHandler>().parseText(holder.time, on<ResourcesHandler>().resources.getString(R.string.shared_by, on<TimeStr>().pretty(groupMessage.created), "@" + groupMessage.from!!))
    }

    private fun displayPost(holder: GroupMessageViewHolder,
                            jsonObject: JsonObject,
                            groupMessage: GroupMessage,
                            onEventClickListener: (Event) -> Unit,
                            onGroupClickListener: (Group) -> Unit,
                            onSuggestionClickListener: (Suggestion) -> Unit) {
        displayFallback(holder, groupMessage)

        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_post, it)
        }

        holder.message.visible = false
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
        holder.action.visible = false

        val post = jsonObject.get("post").asJsonObject
        val sections = post.get("sections").asJsonArray

        holder.custom.removeAllViews()

        val layout = LinearLayout(holder.custom.context).also { it.orientation = LinearLayout.VERTICAL }

        holder.custom.addView(layout, ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            constrainedWidth = true
            topToTop = PARENT_ID
            bottomToBottom = PARENT_ID
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
        })

        Single.concat(sections.map { on<MessageSections>().renderSection(it.asJsonObject, layout) })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    holder.custom.visible = true
                    holder.custom.requestLayout()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { layout.addView(it) }.also {
                    holder.disposableGroup.add(it)
                }

        if (
                groupMessage.from != on<PersistenceHandler>().phoneId &&
                !holder.pinned &&
                (groupMessage.created ?: Date(0)).after(on<TimeAgo>().daysAgo(2)) &&
                !hasMyReaction(groupMessage)
        ) {
            toggleMessageActionLayout(groupMessage, holder, true)
        }
    }

    private fun displayAction(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val action = jsonObject.get("action").asJsonObject
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        loadName(groupMessage.from!!, holder.name) {
            it + " " + action.get("intent").asString
        }

        val comment = action.get("comment").asString
        if (comment.isNullOrBlank()) {
            holder.message.visible = false
        } else {
            holder.message.visible = true
            holder.message.gravity = Gravity.START
            holder.message.text = action.get("comment").asString
        }
        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.profile)
        holder.action.setOnClickListener { on<NavigationHandler>().showProfile(groupMessage.from!!) }
    }

    private fun displayGroupAction(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group_action, it)
        }

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        holder.message.visible = false
        holder.action.visible = false

        holder.disposableGroup.add(on<MessageSections>().renderSection(jsonObject, holder.custom).subscribe { view ->
            holder.custom.removeAllViews()
            holder.custom.addView(view)
            holder.custom.visible = true
        })
    }

    private fun displayReview(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_posted_a_review, it)
        }

        val review = jsonObject.get("review").asJsonObject
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        val rating = review.get("rating").asFloat
        val comment = review.get("comment").asString

        if (comment.isNullOrBlank()) {
            holder.message.visible = false
        } else {
            holder.message.visible = true
            holder.message.gravity = Gravity.START
            holder.message.text = comment
        }

        holder.rating.visible = true
        holder.rating.rating = rating

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.profile)
        holder.action.setOnClickListener { on<NavigationHandler>().showProfile(groupMessage.from!!) }
    }

    private fun displayGroupMessage(holder: GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        holder.action.visible = false
        holder.message.visible = true
        holder.message.gravity = Gravity.START

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        loadName(groupMessage.from!!, holder.name) { it }

        holder.message.text = on<GroupMessageParseHandler>().parseText(holder.message, groupMessage.text ?: "")

        if (EmojiManager.isOnlyEmojis(groupMessage.text)) {
            holder.messageLayout.background = null
            holder.message.setTextSize(TypedValue.COMPLEX_UNIT_PX, on<ResourcesHandler>().resources.getDimension(R.dimen.textSizeEmoji))
        }
    }

    private fun displayMessage(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.name.visible = false
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
        holder.messageLayout.visible = false
        holder.eventMessage.visible = true
        holder.eventMessage.text = on<GroupMessageParseHandler>().parseText(holder.eventMessage, jsonObject.get("message").asString)
        holder.itemView.setOnClickListener(null)
        holder.action.visible = false
    }

    private fun displayEvent(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onEventClickListener: (Event) -> Unit) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val event = on<JsonHandler>().from(jsonObject.get("event"), Event::class.java)

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_shared_an_event, it)
        }

        holder.message.visible = true
        holder.message.gravity = Gravity.START
        holder.message.text = "${if (event.name == null) on<ResourcesHandler>().resources.getString(R.string.unknown) else event.name}\n${on<EventDetailsHandler>().formatEventDetails(event)}"

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_event)
        holder.action.setOnClickListener {
            onEventClickListener.invoke(event)
        }
    }

    private fun displayGroup(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onGroupClickListener: (Group) -> Unit) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val group = on<JsonHandler>().from(jsonObject.get("group"), Group::class.java)

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group, it)
        }

        holder.message.visible = true
        holder.message.gravity = Gravity.START
        holder.message.text = "${if (group.name.isNullOrBlank()) on<ResourcesHandler>().resources.getString(R.string.unknown) else group.name}${if (!group.about.isNullOrBlank()) "\n${group.about}" else ""}"

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_group)
        holder.action.setOnClickListener { onGroupClickListener.invoke(group) }
    }

    private fun displaySuggestion(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onSuggestionClickListener: ((Suggestion) -> Unit)?) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val suggestion: Suggestion = on<JsonHandler>().from(jsonObject.get("suggestion"), Suggestion::class.java)
        val suggestionHasNoName = suggestion.name.isNullOrBlank()

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        loadName(groupMessage.from!!, holder.name) {
            if (suggestion.id.isNullOrBlank()) {
                on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_location, it)
            } else {
                on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_suggestion, it)
            }
        }

        if (suggestionHasNoName) {
            holder.message.visible = false
        } else {
            holder.message.visible = true
            holder.message.gravity = Gravity.START
            holder.message.text = suggestion.name
        }

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.show_on_map)
        holder.action.setOnClickListener { view ->
            onSuggestionClickListener?.invoke(suggestion)
        }
    }

    private fun displayStory(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_story, it)
        }

        val storyDef = jsonObject.get("story").asJsonObject

        val photo = if (!storyDef.has("photo") || storyDef.get("photo").isJsonNull) null else storyDef.get("photo").asString + "?s=256"
        val text = if (!storyDef.has("text") || storyDef.get("text").isJsonNull) null else storyDef.get("text").asString

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
        holder.photo.visible = photo.isNullOrBlank().not()

        if (holder.photo.visible) {
            val gl = object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent?) = true

                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    react(groupMessage, on<ResourcesHandler>().resources.getString(R.string.heart))
                    return true
                }

                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    on<NavigationHandler>().showStory(storyDef.get("id").asString!!, holder.photo)
                    return true
                }

                override fun onLongPress(e: MotionEvent?) {
                    on<MessageDisplay>().toggleMessageActionLayout(groupMessage, holder)
                }
            }

            val gd = GestureDetector(holder.photo.context, gl)

            holder.photo.setOnTouchListener { _, event -> gd.onTouchEvent(event) }
        }

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.view_story)
        holder.action.setOnClickListener { on<NavigationHandler>().showStory(storyDef.get("id").asString!!, holder.action) }

        if (text.isNullOrBlank()) {
            holder.message.visible = false
        } else {
            holder.message.visible = true
            holder.message.gravity = Gravity.START
            holder.message.text = text
        }

        on<ImageHandler>().get().clear(holder.photo)

        if (photo != null) {
            on<ImageHandler>().get()
                    .load(photo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(RequestOptions().transform(RoundedCorners(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.imageCorners))))
                    .into(holder.photo)
        }
    }

    private fun displayPhoto(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        loadName(groupMessage.from!!, holder.name) {
            on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_photo, it)
        }

        val photo = jsonObject.get("photo").asString + "?s=500"
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
        holder.message.visible = false
        holder.action.visible = false // or Share / save photo?
        holder.photo.visible = true

        val gl = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?) = true

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                react(groupMessage, on<ResourcesHandler>().resources.getString(R.string.heart))
                return true
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                on<PhotoActivityTransitionHandler>().show(holder.photo, photo)
                return true
            }

            override fun onLongPress(e: MotionEvent?) {
                on<MessageDisplay>().toggleMessageActionLayout(groupMessage, holder)
            }
        }

        val gd = GestureDetector(holder.photo.context, gl)

        holder.photo.setOnTouchListener { _, event -> gd.onTouchEvent(event) }

        on<ImageHandler>().get().clear(holder.photo)
        on<ImageHandler>().get()
                .load(photo)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(RequestOptions().transform(RoundedCorners(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.imageCorners))))
                .into(holder.photo)
    }

    private fun displayFallback(holder: GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.unknown)
        holder.message.text = ""
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
    }

    fun displayText(groupMessage: GroupMessage): Single<String>? = when {
        groupMessage.text?.isNotBlank() == true -> Single.just(groupMessage.text!!)
        groupMessage.attachment != null -> {
            val jsonObject = on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
            when {
                jsonObject.has("activity") -> Single.just("Shared an activity")
                jsonObject.has("action") -> on<NameCacheHandler>()[groupMessage.from!!]?.let {
                    Single.just(it + " " + jsonObject.get("action").asJsonObject.get("intent").asString)
                }
                jsonObject.has("review") -> Single.just("Posted a review")
                jsonObject.has("message") -> on<GroupMessageParseHandler>().parseString(jsonObject.get("message").asString)
                jsonObject.has("event") -> Single.just("Shared an event")
                jsonObject.has("group") -> Single.just("Shared a group")
                jsonObject.has("suggestion") -> {
                    val suggestion: Suggestion = on<JsonHandler>().from(jsonObject.get("suggestion"), Suggestion::class.java)

                    if (suggestion.id.isNullOrBlank()) {
                        Single.just("Shared a location")
                    } else {
                        Single.just("Shared a suggestion")
                    }
                }
                jsonObject.has("story") -> Single.just("Shared a story")
                jsonObject.has("photo") -> Single.just("Shared a photo")
                jsonObject.has("share") -> Single.just("Shared a message")
                jsonObject.has("post") -> Single.just("Shared a post")
                else -> null
            }
        }
        else -> null
    }

    fun attachmentType(groupMessage: GroupMessage): String? {
        val jsonObject = on<JsonHandler>().from(groupMessage.attachment ?: return null, JsonObject::class.java)

        return when {
            jsonObject.has("activity") -> "activity"
            jsonObject.has("action") -> "action"
            jsonObject.has("review") -> "review"
            jsonObject.has("message") -> "message"
            jsonObject.has("event") -> "event"
            jsonObject.has("group") -> "group"
            jsonObject.has("suggestion") -> "suggestion"
            jsonObject.has("story") -> "story"
            jsonObject.has("photo") -> "photo"
            jsonObject.has("share") -> "share"
            jsonObject.has("post") -> "post"
            else -> null
        }
    }

    fun display(holder: GroupMessageViewHolder,
                groupMessage: GroupMessage,
                previousGroupMessage: GroupMessage?,
                onEventClickListener: (Event) -> Unit,
                onGroupClickListener: (Group) -> Unit,
                onSuggestionClickListener: (Suggestion) -> Unit) {
        holder.group.visible = false

        var showProfile = true

        if (groupMessage.attachment != null) {
            try {
                val jsonObject = on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
                when {
                    jsonObject.has("activity") -> displayGroupAction(holder, jsonObject, groupMessage)
                    jsonObject.has("action") -> displayAction(holder, jsonObject, groupMessage)
                    jsonObject.has("review") -> displayReview(holder, jsonObject, groupMessage)
                    jsonObject.has("message") -> { displayMessage(holder, jsonObject, groupMessage); showProfile = false }
                    jsonObject.has("event") -> displayEvent(holder, jsonObject, groupMessage, onEventClickListener)
                    jsonObject.has("group") -> displayGroup(holder, jsonObject, groupMessage, onGroupClickListener)
                    jsonObject.has("suggestion") -> displaySuggestion(holder, jsonObject, groupMessage, onSuggestionClickListener)
                    jsonObject.has("story") -> displayStory(holder, jsonObject, groupMessage)
                    jsonObject.has("photo") -> displayPhoto(holder, jsonObject, groupMessage)
                    jsonObject.has("share") -> displayShare(holder, jsonObject, groupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
                    jsonObject.has("post") -> displayPost(holder, jsonObject, groupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
                    else -> displayFallback(holder, groupMessage)
                }
            } catch (e: JsonSyntaxException) {
                displayFallback(holder, groupMessage)
                e.printStackTrace()
            }
        } else {
            displayGroupMessage(holder, groupMessage)
        }

        if (holder.pinned) {
            holder.time.visible = false
            holder.pinnedIndicator.visible = true
            holder.messageActionPin.setText(R.string.unpin)
        }

        if (holder.global) {
            holder.group.visible = true
            holder.showGroupInsteadOfProfile = groupMessage.to
            val group = groupMessage.to?.let { getGroup(it) }
            holder.group.text = on<ResourcesHandler>().resources.getString(R.string.is_in, group?.name?.let { if (it.isBlank()) on<ResourcesHandler>().resources.getString(R.string.unknown) else group.name } ?: on<ResourcesHandler>().resources.getString(R.string.unknown))
        }

        if (
                groupMessage.from != on<PersistenceHandler>().phoneId &&
                !holder.pinned &&
                (groupMessage.created ?: Date(0)).after(on<TimeAgo>().minutesAgo(5)) &&
                !hasMyReaction(groupMessage)
        ) {
            toggleMessageActionLayout(groupMessage, holder, true, shorthand = true)
        }

        holder.profilePhoto.visible = showProfile && !holder.pinned
        holder.activeNowIndicator.visible = false

        if (showProfile && !holder.pinned) {
            val invisi = previousGroupMessage?.attachment?.startsWith("{\"message\":") != true && previousGroupMessage?.from == groupMessage.from

            if (invisi) {
                holder.profilePhoto.visibility = View.INVISIBLE
                return
            }

            holder.profilePhoto.setOnClickListener {
                on<GroupActivityTransitionHandler>().showGroupForPhone(holder.profilePhoto, groupMessage.from!!)
            }

            holder.profilePhoto.setImageResource(R.drawable.ic_person_black_24dp)
            holder.profilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE

            on<DataHandler>().getPhone(groupMessage.from!!).subscribe({
                holder.activeNowIndicator.visible = on<TimeAgo>().fifteenMinutesAgo().before(it.updated ?: Date(0))

                if (it.photo.isNullOrBlank()) {
                    holder.profilePhoto.setImageResource(R.drawable.ic_person_black_24dp)
                    holder.profilePhoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
                } else {
                    holder.profilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                    on<PhotoHelper>().loadCircle(holder.profilePhoto, "${it.photo}?s=256")
                }
            }, {}).also {
                holder.disposableGroup.add(it)
            }
        }
    }

    private fun loadName(phoneId: String, textView: TextView, callback: (String) -> String) {
        on<NameCacheHandler>()[phoneId]?.let {
            textView.visible = true
            textView.text = callback(it)
            return@loadName
        }

        textView.visible = false
        on<NameHandler>().getNameAsync(phoneId).subscribe({
            textView.visible = true
            textView.text = callback(it)
        }, {}).also { on<DisposableHandler>().add(it) }
    }

    fun toggleMessageActionLayout(groupMessage: GroupMessage, holder: GroupMessageViewHolder, show: Boolean? = null, shorthand: Boolean = false) {
        if (holder.messageActionLayoutRevealAnimator == null) {
            holder.messageActionLayoutRevealAnimator = RevealAnimatorForConstraintLayout(holder.messageActionLayout, (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())
        }

        val visible = show?.let { it } ?: holder.messageActionLayout.visible.not()

        holder.messageActionLayoutRevealAnimator!!.show(visible)

        if (visible) renderMessageActionLayout(groupMessage, holder, shorthand)
    }

    private fun renderMessageActionLayout(groupMessage: GroupMessage, holder: GroupMessageViewHolder, shorthand: Boolean) {
        holder.messageActionVote.setOnClickListener { react(holder, groupMessage, "♥") }
        holder.messageActionVoteLaugh.setOnClickListener { react(holder, groupMessage, "\uD83D\uDE02")}
        holder.messageActionVoteYummy.setOnClickListener { react(holder, groupMessage, "\uD83D\uDE2F") }
        holder.messageActionVoteKiss.setOnClickListener { react(holder, groupMessage, "\uD83D\uDE18") }
        holder.messageActionVoteCool.setOnClickListener { react(holder, groupMessage, "\uD83D\uDE0E") }

        if (shorthand) {
            listOf(
                    holder.messageRepliesCount,
                    holder.messageActionLayout,
                    holder.messageActionProfile,
                    holder.messageActionShare,
                    holder.messageActionRemind,
                    holder.messageActionReply,
                    holder.messageActionDelete,
                    holder.messageActionPin
            ).forEach { it.visible = false }

            holder.messageActionShorthand.visible = true

            holder.messageActionShorthand.setOnClickListener {
                renderMessageActionLayout(groupMessage, holder, false)
            }

            return
        }

        holder.messageActionShorthand.visible = false

        holder.messageActionProfile.text = on<ResourcesHandler>().resources.getString(
                if (holder.showGroupInsteadOfProfile != null) R.string.group else R.string.profile
        )

        holder.messageActionReply.visible = true
        holder.messageActionShare.visible = groupMessage.from != null
        holder.messageActionPin.visible = groupMessage.from != null && !holder.global && !holder.inFeed
        holder.messageActionDelete.visible = groupMessage.from == on<PersistenceHandler>().phoneId && !holder.pinned

        holder.messageActionProfile.visible = holder.showGroupInsteadOfProfile != null || groupMessage.from != null

        if (holder.showGroupInsteadOfProfile != null) {
            holder.messageActionProfile.setOnClickListener {
                on<NavigationHandler>().showGroup(holder.showGroupInsteadOfProfile!!)
                toggleMessageActionLayout(groupMessage, holder)
            }
        } else if (groupMessage.from != null) {
            holder.messageActionProfile.setOnClickListener {
                on<NavigationHandler>().showProfile(groupMessage.from!!)
                toggleMessageActionLayout(groupMessage, holder)
            }
        }

        holder.messageActionShare.setOnClickListener {
            on<ShareActivityTransitionHandler>().shareGroupMessage(groupMessage.id!!)
            toggleMessageActionLayout(groupMessage, holder)
        }

        holder.messageActionRemind.setOnClickListener {
            on<DefaultAlerts>().message("That doesn't work yet!")
            toggleMessageActionLayout(groupMessage, holder)
        }

        holder.messageActionReply.setOnClickListener {
            openGroup(groupMessage)
            toggleMessageActionLayout(groupMessage, holder)
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
                    toggleMessageActionLayout(groupMessage, holder)
                }
                show()
            }
        }

        holder.messageActionPin.setOnClickListener {
            if (holder.pinned) {
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
            toggleMessageActionLayout(groupMessage, holder)
        }
    }

    private fun react(holder: GroupMessageViewHolder, groupMessage: GroupMessage, reaction: String) {
        react(groupMessage, reaction, hasMyReaction(groupMessage, reaction))
        toggleMessageActionLayout(groupMessage, holder)
    }

    private fun react(groupMessage: GroupMessage, reaction: String, remove: Boolean = false) {
        on<ApplicationHandler>().app.on<DisposableHandler>().add(on<ApiHandler>()
                .reactToMessage(groupMessage.id!!, reaction, remove)
                .subscribe({
                    on<RefreshHandler>().refreshGroupMessage(groupMessage.id!!)
                }, {
                    on<DefaultAlerts>().thatDidntWork()
                }))
    }

    private fun getGroup(groupId: String?): Group? {
        return if (groupId == null)
            null
        else on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupId, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .findFirst()
    }

    fun openGroup(groupMessage: GroupMessage) {
        on<DisposableHandler>().add(on<ApiHandler>().getGroupForGroupMessage(groupMessage.id!!).subscribe({ group ->
            on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id, true)
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }))
    }

    fun hasMyReaction(groupMessage: GroupMessage) = groupMessage.reactions
            .any { it.preview.any { it == on<PersistenceHandler>().phoneId } }

    fun hasMyReaction(groupMessage: GroupMessage, reaction: String) = groupMessage.reactions
            .firstOrNull { it.reaction == reaction }
            ?.preview
            ?.any { it == on<PersistenceHandler>().phoneId } ?: false

    fun isFullWidth(groupMessage: GroupMessage): Boolean {
        if (groupMessage.attachment != null) {
            try {
                val jsonObject = on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)

                when {
                    jsonObject.has("post") -> {
                        return jsonObject.get("post").asJsonObject.get("sections").asJsonArray.any { on<MessageSections>().isFullWidth(it.asJsonObject) }
                    }
                }
            } catch (throwable: Throwable) { }
        }

        return false
    }
}
