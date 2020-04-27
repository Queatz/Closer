package closer.vlllage.com.closer.handler.group

import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.view.updateLayoutParams
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.queatz.on.On
import io.reactivex.Single
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import java.lang.RuntimeException

class MessageDisplay constructor(private val on: On) {

    private fun displayShare(holder: GroupMessageViewHolder,
                             jsonObject: JsonObject,
                             groupMessage: GroupMessage,
                             onEventClickListener: (Event) -> Unit,
                             onGroupClickListener: (Group) -> Unit,
                             onSuggestionClickListener: (Suggestion) -> Unit) {
        displayFallback(holder, groupMessage)

        holder.disposableGroup.add(on<DataHandler>().getGroupMessage(jsonObject.get("share").asString).subscribe({ sharedGroupMessage ->
            display(holder, sharedGroupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
            holder.time.text = on<GroupMessageParseHandler>().parseText(holder.time, on<ResourcesHandler>().resources.getString(R.string.shared_by, on<TimeStr>().pretty(groupMessage.created), "@" + groupMessage.from!!))

            holder.messageActionProfile.setText(R.string.group)
            holder.messageActionProfile.setOnClickListener {
                on<NavigationHandler>().showGroup(sharedGroupMessage.to!!)
                toggleMessageActionLayout(holder)
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

        val contactName = on<NameHandler>().getName(getPhone(groupMessage.from))

        val post = jsonObject.get("post").asJsonObject
        val sections = post.get("sections").asJsonArray

        holder.message.visible = false
        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_post, contactName)
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
        holder.action.visible = false

        if (sections.any { on<MessageSections>().isFullWidth(it.asJsonObject) }) {
            holder.messageLayout.updateLayoutParams { width = MATCH_PARENT }
            holder.custom.updateLayoutParams { width = MATCH_PARENT }
        }

        holder.custom.removeAllViews()

        val layout = LinearLayout(holder.custom.context).also {
            it.orientation = LinearLayout.VERTICAL
            it.layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topToTop = PARENT_ID
                bottomToBottom = PARENT_ID
                startToStart = PARENT_ID
                endToEnd = PARENT_ID
            }
        }

        holder.disposableGroup.add(Single.concat(sections.map { on<MessageSections>().renderSection(it.asJsonObject, holder.custom) }).doOnComplete {
            holder.custom.addView(layout)
            holder.custom.visible = true
        }.subscribe { layout.addView(it) })
    }

    private fun displayAction(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val action = jsonObject.get("action").asJsonObject
        holder.name.visible = true
        holder.name.text = contactName + " " + action.get("intent").asString
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        val comment = action.get("comment").asString
        if (on<Val>().isEmpty(comment)) {
            holder.message.visible = false
        } else {
            holder.message.visible = true
            holder.message.gravity = Gravity.START
            holder.message.text = action.get("comment").asString
        }
        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.profile)
        holder.action.setOnClickListener { on<NavigationHandler>().showProfile(phone!!.id!!) }
    }

    private fun displayGroupAction(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group_action, contactName)
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

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val review = jsonObject.get("review").asJsonObject
        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_posted_a_review, contactName)
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        val rating = review.get("rating").asFloat
        val comment = review.get("comment").asString

        if (on<Val>().isEmpty(comment)) {
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
        holder.action.setOnClickListener { on<NavigationHandler>().showProfile(phone!!.id!!) }
    }

    private fun displayGroupMessage(holder: GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        holder.action.visible = false
        holder.name.visible = true
        holder.message.visible = true
        holder.message.gravity = Gravity.START

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        var phone: Phone? = null

        if (groupMessage.from != null) {
            phone = getPhone(groupMessage.from)
        }

        holder.name.text = on<NameHandler>().getName(phone)
        holder.message.text = on<GroupMessageParseHandler>().parseText(holder.message, groupMessage.text!!)
    }

    private fun displayMessage(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.name.visible = false
        holder.time.visible = false
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

        holder.name.visible = true
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_an_event, contactName)

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

        holder.name.visible = true
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group, contactName)

        holder.message.visible = true
        holder.message.gravity = Gravity.START
        holder.message.text = "${if (group.name == null) on<ResourcesHandler>().resources.getString(R.string.unknown) else group.name}${if (group.about != null) "\n" + group.about!! else ""}"

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_group)
        holder.action.setOnClickListener { view ->
            onGroupClickListener.invoke(group)
        }
    }

    private fun displaySuggestion(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onSuggestionClickListener: ((Suggestion) -> Unit)?) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val suggestion: Suggestion = on<JsonHandler>().from(jsonObject.get("suggestion"), Suggestion::class.java)
        val suggestionHasNoName = suggestion.name.isNullOrBlank()

        holder.name.visible = true
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        if (suggestionHasNoName) {
            holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_location, contactName)
        } else {
            holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_suggestion, contactName)
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

    private fun displayPhoto(holder: GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val photo = jsonObject.get("photo").asString + "?s=500"
        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_photo, contactName)
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
        holder.message.visible = false
        holder.action.visible = false // or Share / save photo?
        holder.photo.visible = true
        holder.photo.setOnClickListener { view -> on<PhotoActivityTransitionHandler>().show(view, photo) }
        on<ImageHandler>().get().cancelRequest(holder.photo)
        on<ImageHandler>().get().load(photo).transform(RoundedCornersTransformation(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.imageCorners), 0)).into(holder.photo)
    }

    private fun displayFallback(holder: GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.unknown)
        holder.message.text = ""
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.created)
    }

    fun display(holder: GroupMessageViewHolder,
                groupMessage: GroupMessage,
                onEventClickListener: (Event) -> Unit,
                onGroupClickListener: (Group) -> Unit,
                onSuggestionClickListener: (Suggestion) -> Unit) {
        if (groupMessage.attachment != null) {
            try {
                val jsonObject = on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
                when {
                    jsonObject.has("activity") -> displayGroupAction(holder, jsonObject, groupMessage)
                    jsonObject.has("action") -> displayAction(holder, jsonObject, groupMessage)
                    jsonObject.has("review") -> displayReview(holder, jsonObject, groupMessage)
                    jsonObject.has("message") -> displayMessage(holder, jsonObject, groupMessage)
                    jsonObject.has("event") -> displayEvent(holder, jsonObject, groupMessage, onEventClickListener)
                    jsonObject.has("group") -> displayGroup(holder, jsonObject, groupMessage, onGroupClickListener)
                    jsonObject.has("suggestion") -> displaySuggestion(holder, jsonObject, groupMessage, onSuggestionClickListener)
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
            holder.group.text = on<ResourcesHandler>().resources.getString(R.string.is_in, groupMessage.to?.let { getGroup(it) }?.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown))
        }
    }

    fun toggleMessageActionLayout(holder: GroupMessageViewHolder) {
        if (holder.messageActionLayoutRevealAnimator == null) {
            holder.messageActionLayoutRevealAnimator = RevealAnimator(holder.messageActionLayout, (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())
        }

        holder.messageActionLayoutRevealAnimator!!.show(holder.messageActionLayout.visible.not())
    }

    private fun getPhone(phoneId: String?): Phone? {
        return if (phoneId == null)
            null
        else on<StoreHandler>().store.box(Phone::class).query()
                .equal(Phone_.id, phoneId)
                .build()
                .findFirst()
    }

    private fun getGroup(groupId: String?): Group? {
        return if (groupId == null)
            null
        else on<StoreHandler>().store.box(Group::class).query()
                .equal(Group_.id, groupId)
                .build()
                .findFirst()
    }
}
