package closer.vlllage.com.closer.handler.group

import android.view.Gravity
import android.view.View
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.event.EventDetailsHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.PhoneMessagesHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.*
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class MessageDisplay constructor(private val on: On) {

    private var pinned: Boolean = false

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
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val action = jsonObject.get("action").asJsonObject
        holder.name.visibility = View.VISIBLE
        holder.name.text = contactName + " " + action.get("intent").asString
        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        val comment = action.get("comment").asString
        if (on<Val>().isEmpty(comment)) {
            holder.message.visibility = View.GONE
        } else {
            holder.message.visibility = View.VISIBLE
            holder.message.gravity = Gravity.START
            holder.message.text = action.get("comment").asString
        }
        holder.action.visibility = View.GONE

        holder.action.visibility = View.VISIBLE
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.reply)
        holder.action.setOnClickListener { view -> on<PhoneMessagesHandler>().openMessagesWithPhone(phone!!.id!!, contactName, comment) }
    }

    private fun displayGroupMessage(holder: GroupMessagesAdapter.GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        holder.action.visibility = View.GONE
        holder.name.visibility = View.VISIBLE
        holder.message.visibility = View.VISIBLE
        holder.message.gravity = Gravity.START

        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        var phone: Phone? = null

        if (groupMessage.from != null) {
            phone = getPhone(groupMessage.from)
        }

        holder.name.text = on<NameHandler>().getName(phone)
        holder.message.text = on<GroupMessageParseHandler>().parseText(groupMessage.text!!)
    }

    private fun displayMessage(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.name.visibility = View.GONE
        holder.time.visibility = View.GONE
        holder.messageLayout.visibility = View.GONE
        holder.eventMessage.visibility = View.VISIBLE
        holder.eventMessage.text = jsonObject.get("message").asString
        holder.itemView.setOnClickListener(null)
        holder.action.visibility = View.GONE
    }

    private fun displayEvent(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onEventClickListener: (Event) -> Unit) {
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        val event = on<JsonHandler>().from(jsonObject.get("event"), Event::class.java)

        holder.name.visibility = View.VISIBLE
        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_an_event, contactName)

        holder.message.visibility = View.VISIBLE
        holder.message.gravity = Gravity.START
        holder.message.text = (if (event.name == null) on<ResourcesHandler>().resources.getString(R.string.unknown) else event.name) +
                "\n" +
                on<EventDetailsHandler>().formatEventDetails(event)

        holder.action.visibility = View.VISIBLE
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_event)
        holder.action.setOnClickListener { view ->
            onEventClickListener?.invoke(event)
        }
    }

    private fun displayGroup(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onGroupClickListener: (Group) -> Unit) {
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        val group = on<JsonHandler>().from(jsonObject.get("group"), Group::class.java)

        holder.name.visibility = View.VISIBLE
        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_group, contactName)

        holder.message.visibility = View.VISIBLE
        holder.message.gravity = Gravity.START
        holder.message.text = (if (group.name == null) on<ResourcesHandler>().resources.getString(R.string.unknown) else group.name) + if (group.about != null) "\n" + group.about!! else ""

        holder.action.visibility = View.VISIBLE
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.open_group)
        holder.action.setOnClickListener { view ->
            onGroupClickListener?.invoke(group)
        }
    }

    private fun displaySuggestion(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage, onSuggestionClickListener: ((Suggestion) -> Unit)?) {
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        val suggestion = on<JsonHandler>().from(jsonObject.get("suggestion"), Suggestion::class.java)

        val suggestionHasNoName = suggestion == null || suggestion.name == null || suggestion.name!!.isEmpty()

        holder.name.visibility = View.VISIBLE
        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)

        val phone = getPhone(groupMessage.from)

        val contactName = on<NameHandler>().getName(phone)

        if (suggestionHasNoName) {
            holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_location, contactName)
        } else {
            holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_suggestion, contactName)
        }

        if (suggestionHasNoName) {
            holder.message.visibility = View.GONE
        } else {
            holder.message.visibility = View.VISIBLE
            holder.message.gravity = Gravity.START
            holder.message.text = suggestion.name
        }

        holder.action.visibility = View.VISIBLE
        holder.action.text = on<ResourcesHandler>().resources.getString(R.string.show_on_map)
        holder.action.setOnClickListener { view ->
            onSuggestionClickListener?.invoke(suggestion)
        }
    }

    private fun displayPhoto(holder: GroupMessagesAdapter.GroupMessageViewHolder, jsonObject: JsonObject, groupMessage: GroupMessage) {
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        val phone = getPhone(groupMessage.from)
        val contactName = on<NameHandler>().getName(phone)

        val photo = jsonObject.get("photo").asString + "?s=500"
        holder.name.visibility = View.VISIBLE
        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.phone_shared_a_photo, contactName)
        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)
        holder.message.visibility = View.GONE
        holder.action.visibility = View.GONE// or Share / save photo?
        holder.photo.visibility = View.VISIBLE
        holder.photo.setOnClickListener { view -> on<PhotoActivityTransitionHandler>().show(view, photo) }
        on<ImageHandler>().get().cancelRequest(holder.photo)
        on<ImageHandler>().get().load(photo).transform(RoundedCornersTransformation(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.imageCorners), 0)).into(holder.photo)
    }

    private fun displayFallback(holder: GroupMessagesAdapter.GroupMessageViewHolder, groupMessage: GroupMessage) {
        holder.eventMessage.visibility = View.GONE
        holder.messageLayout.visibility = View.VISIBLE

        holder.name.text = on<ResourcesHandler>().resources.getString(R.string.unknown)
        holder.message.text = ""
        holder.time.visibility = View.VISIBLE
        holder.time.text = on<TimeStr>().pretty(groupMessage.time)
    }

    fun setPinned(pinned: Boolean) {
        this.pinned = pinned
    }

    fun display(holder: GroupMessagesAdapter.GroupMessageViewHolder, groupMessage: GroupMessage,
                onEventClickListener: (Event) -> Unit,
                onGroupClickListener: (Group) -> Unit,
                onSuggestionClickListener: (Suggestion) -> Unit) {
        if (groupMessage.attachment != null) {
            try {
                val jsonObject = on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
                if (jsonObject.has("action")) {
                    displayAction(holder, jsonObject, groupMessage)
                } else if (jsonObject.has("message")) {
                    displayMessage(holder, jsonObject, groupMessage)
                } else if (jsonObject.has("event")) {
                    displayEvent(holder, jsonObject, groupMessage, onEventClickListener)
                } else if (jsonObject.has("group")) {
                    displayGroup(holder, jsonObject, groupMessage, onGroupClickListener)
                } else if (jsonObject.has("suggestion")) {
                    displaySuggestion(holder, jsonObject, groupMessage, onSuggestionClickListener)
                } else if (jsonObject.has("photo")) {
                    displayPhoto(holder, jsonObject, groupMessage)
                } else if (jsonObject.has("share")) {
                    displayShare(holder, jsonObject, groupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
                } else {
                    displayFallback(holder, groupMessage)
                }
            } catch (e: JsonSyntaxException) {
                displayFallback(holder, groupMessage)
                e.printStackTrace()
            }

        } else {
            displayGroupMessage(holder, groupMessage)
        }

        if (pinned) {
            holder.time.visibility = View.GONE
            holder.pinnedIndicator.visibility = View.VISIBLE
            holder.messageActionPin.setText(R.string.unpin)
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
}
