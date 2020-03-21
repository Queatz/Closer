package closer.vlllage.com.closer.handler.group

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.store.Store
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.queatz.on.On
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class MessageDisplay constructor(private val on: On) {

    var pinned: Boolean = false
    var global: Boolean = false

    private fun displayShare(holder: GroupMessagesAdapter.GroupMessageViewHolder,
                             jsonObject: JsonObject,
                             groupMessage: GroupMessage,
                             onEventClickListener: (Event) -> Unit,
                             onGroupClickListener: (Group) -> Unit,
                             onSuggestionClickListener: (Suggestion) -> Unit) {
        val sharedGroupMessage = on<StoreHandler>().store.box(GroupMessage::class).query().equal(GroupMessage_.id, jsonObject.get("share").asString).build().findFirst()

        if (sharedGroupMessage != null) {
            display(holder, sharedGroupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
        } else {
            displayFallback(holder, groupMessage)
        }

        holder.time.text = on<GroupMessageParseHandler>().parseText(on<ResourcesHandler>().resources.getString(R.string.shared_by, on<TimeStr>().pretty(groupMessage.time), "@" + groupMessage.from!!))
    }

    private fun displayAction(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val action = jsonObject.get("action").asJsonObject
        holder.name.visible = true
        holder.name.text = contactName + " " + action.get("intent").asString
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

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

    private fun displayGroupAction(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val activity = jsonObject.get("activity").asJsonObject
        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group_action, contactName)
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        holder.custom.removeAllViews()
        holder.custom.visible = true

        on<StoreHandler>().store.box(GroupAction::class).query()
                .equal(GroupAction_.id, activity.getAsJsonPrimitive("id").asString)
                .build().findFirst()?.let { groupAction ->
                    View.inflate(holder.custom.context, R.layout.group_action_photo_item, holder.custom)
                    val rootView = holder.custom.findViewById<ViewGroup>(R.id.rootView)
                    (rootView.layoutParams as ConstraintLayout.LayoutParams).apply {
                        topMargin = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
                        marginStart = 0
                        startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                        endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                        topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                        bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    }
                    on<GroupActionDisplay>().display(rootView, groupAction, GroupActionDisplay.Layout.PHOTO)
                }

        holder.message.visible = false
        holder.action.visible = false
    }

    private fun displayReview(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val review = jsonObject.get("review").asJsonObject
        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_posted_a_review, contactName)
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

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

    private fun displayGroupMessage(holder: GroupMessagesAdapter.GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        holder.action.visible = false
        holder.name.visible = true
        holder.message.visible = true
        holder.message.gravity = Gravity.START

        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        var phone: Phone? = null

        if (groupMessage.from != null) {
            phone = getPhone(groupMessage.from)
        }

        holder.name.text = on<NameHandler>().getName(phone)
        holder.message.text = on<GroupMessageParseHandler>().parseText(groupMessage.text!!)
    }

    private fun displayMessage(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.name.visible = false
        holder.time.visible = false
        holder.messageLayout.visible = false
        holder.eventMessage.visible = true
        holder.eventMessage.text = on<GroupMessageParseHandler>().parseText(jsonObject.get("message").asString)
        holder.itemView.setOnClickListener(null)
        holder.action.visible = false
    }

    private fun displayEvent(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onEventClickListener: (Event) -> Unit) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val event = on<JsonHandler>().from(jsonObject.get("event"), Event::class.java)

        holder.name.visible = true
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_an_event, contactName)

        holder.message.visible = true
        holder.message.gravity = Gravity.START
        holder.message.text = (if (event.name == null) on<ResourcesHandler>().resources.getString(R.string.unknown) else event.name) +
                "\n" +
                on<EventDetailsHandler>().formatEventDetails(event)

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_event)
        holder.action.setOnClickListener { view ->
            onEventClickListener.invoke(event)
        }
    }

    private fun displayGroup(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onGroupClickListener: (Group) -> Unit) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val group = on<JsonHandler>().from(jsonObject.get("group"), Group::class.java)

        holder.name.visible = true
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group, contactName)

        holder.message.visible = true
        holder.message.gravity = Gravity.START
        holder.message.text = (if (group.name == null) on<ResourcesHandler>().resources.getString(R.string.unknown) else group.name) + if (group.about != null) "\n" + group.about!! else ""

        holder.action.visible = true
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_group)
        holder.action.setOnClickListener { view ->
            onGroupClickListener?.invoke(group)
        }
    }

    private fun displaySuggestion(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onSuggestionClickListener: ((Suggestion) -> Unit)?) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val suggestion: Suggestion = on<JsonHandler>().from(jsonObject.get("suggestion"), Suggestion::class.java)
        val suggestionHasNoName = suggestion.name.isNullOrBlank()

        holder.name.visible = true
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

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

    private fun displayPhoto(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val photo = jsonObject.get("photo").asString + "?s=500"
        holder.name.visible = true
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_photo, contactName)
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)
        holder.message.visible = false
        holder.action.visible = false // or Share / save photo?
        holder.photo.visible = true
        holder.photo.setOnClickListener { view -> on<PhotoActivityTransitionHandler>().show(view, photo) }
        on<ImageHandler>().get().cancelRequest(holder.photo)
        on<ImageHandler>().get().load(photo).transform(RoundedCornersTransformation(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.imageCorners), 0)).into(holder.photo)
    }

    private fun displayFallback(holder: GroupMessagesAdapter.GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visible = false
        holder.messageLayout.visible = true

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.unknown)
        holder.message.text = ""
        holder.time.visible = true
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)
    }

    fun display(holder: GroupMessagesAdapter.GroupMessageViewHolder, groupMessage: GroupMessage,
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
                    else -> displayFallback(holder, groupMessage)
                }
            } catch (e: JsonSyntaxException) {
                displayFallback(holder, groupMessage)
                e.printStackTrace()
            }

        } else {
            displayGroupMessage(holder, groupMessage)
        }

        if (pinned) {
            holder.time.visible = false
            holder.pinnedIndicator.visible = true
            holder.messageActionPin.setText(R.string.unpin)
        }

        if (global) {
            holder.group.visible = true
            holder.group.text = on<ResourcesHandler>().resources.getString(R.string.is_in, groupMessage.to?.let { getGroup(it) }?.name ?: on<ResourcesHandler>().resources.getString(R.string.unknown))
        }
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
