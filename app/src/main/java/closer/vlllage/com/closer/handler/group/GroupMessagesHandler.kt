package closer.vlllage.com.closer.handler.group

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import java.util.*

class GroupMessagesHandler constructor(private val on: On) {

    private lateinit var groupMessagesAdapter: GroupMessagesAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var replyMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var sendMoreButton: ImageButton
    private lateinit var sendMoreLayout: View
    private lateinit var recyclerView: RecyclerView
    private var groupMessagesSubscription: DataSubscription? = null

    fun attach(recyclerView: RecyclerView, replyMessage: EditText, sendButton: ImageButton, sendMoreButton: ImageButton, sendMoreLayout: View) {
        this.replyMessage = replyMessage
        this.sendButton = sendButton
        this.sendMoreButton = sendMoreButton
        this.sendMoreLayout = sendMoreLayout
        this.recyclerView = recyclerView

        layoutManager = LinearLayoutManager(
                on<ActivityHandler>().activity,
                RecyclerView.VERTICAL,
                true
        )

        recyclerView.layoutManager = layoutManager

        groupMessagesAdapter = GroupMessagesAdapter(on)
        recyclerView.adapter = groupMessagesAdapter

        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> (on<ActivityHandler>().activity as CircularRevealActivity).finish { on<MapActivityHandler>().showSuggestionOnMap(suggestion) } }
        groupMessagesAdapter.onEventClickListener = { event -> (on<ActivityHandler>().activity as CircularRevealActivity).finish { on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) } }
        groupMessagesAdapter.onGroupClickListener = { group -> (on<ActivityHandler>().activity as CircularRevealActivity).finish { on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id) } }


        this.replyMessage.setOnEditorActionListener { textView, action, keyEvent ->
            if (EditorInfo.IME_ACTION_GO == action) {
                if (replyMessage.text.toString().isBlank()) {
                    return@setOnEditorActionListener false
                }
                val success = send(replyMessage.text.toString())
                if (success) {
                    textView.text = ""
                }
                return@setOnEditorActionListener true
            }

            false
        }

        this.sendButton.setOnClickListener {
            if (replyMessage.text.toString().isBlank()) {
                on<CameraHandler>().showCamera { photoUri ->
                    on<PhotoUploadGroupMessageHandler>().upload(photoUri!!) { photoId ->
                        val success = on<GroupMessageAttachmentHandler>().sharePhoto(on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId), on<GroupHandler>().group!!.id!!)
                        if (!success) {
                            on<DefaultAlerts>().thatDidntWork()
                        }
                    }
                }
                return@setOnClickListener
            }

            val success = send(replyMessage.text.toString())
            if (success) {
                replyMessage.setText("")
            }
        }

        updateSendButton()
        on<GroupHandler>().onGroupChanged { group ->
            if (replyMessage.text.toString().isEmpty()) {
                replyMessage.setText(on<GroupMessageParseHandler>().parseText(on<GroupDraftHandler>().getDraft(group)!!))
                updateSendButton()
            }
        }

        this.replyMessage.addTextChangedListener(object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && on<GroupMessageParseHandler>().isMentionSelected(replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                updateSendButton()
                on<GroupDraftHandler>().saveDraft(on<GroupHandler>().group!!, text.toString())
                on<GroupMessageMentionHandler>().showSuggestionsForName(on<GroupMessageParseHandler>().extractName(text, replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    on<GroupMessageParseHandler>().deleteMention(replyMessage)
                }
            }
        })

        this.sendMoreButton.setOnClickListener { view -> showSendMoreOptions(sendMoreLayout.visibility != View.VISIBLE) }

        val sendMoreActionAudio = this.sendMoreLayout.findViewById<View>(R.id.sendMoreActionAudio)
        val sendMoreActionVideo = this.sendMoreLayout.findViewById<View>(R.id.sendMoreActionVideo)
        val sendMoreActionFile = this.sendMoreLayout.findViewById<View>(R.id.sendMoreActionFile)
        val sendMoreActionPhoto = this.sendMoreLayout.findViewById<View>(R.id.sendMoreActionPhoto)

        sendMoreActionAudio.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            on<DefaultAlerts>().message("Woah matey!")
        }
        sendMoreActionVideo.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            on<DefaultAlerts>().message("Woah matey!")
        }
        sendMoreActionFile.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            on<DefaultAlerts>().message("Woah matey!")
        }
        sendMoreActionPhoto.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            on<MediaHandler>().getPhoto { photoUri ->
                on<PhotoUploadGroupMessageHandler>().upload(photoUri) { photoId ->
                    val success = on<GroupMessageAttachmentHandler>().sharePhoto(on<PhotoUploadGroupMessageHandler>().getPhotoPathFromId(photoId), on<GroupHandler>().group!!.id!!)
                    if (!success) {
                        on<DefaultAlerts>().thatDidntWork()
                    }
                }
            }
        }

        on<GroupHandler>().onGroupChanged { group ->
            if (groupMessagesSubscription != null) {
                on<DisposableHandler>().dispose(groupMessagesSubscription!!)
            }

            groupMessagesSubscription = on<StoreHandler>().store.box(GroupMessage::class.java).query()
                    .equal(GroupMessage_.to, group.id!!)
                    .sort(on<SortHandler>().sortGroupMessages())
                    .build()
                    .subscribe().on(AndroidScheduler.mainThread())
                    .observer { this.setGroupMessages(it) }

            on<DisposableHandler>().add(groupMessagesSubscription!!)
        }
    }

    fun showSendMoreOptions(show: Boolean) {
        if (show) {
            sendMoreButton.setImageResource(R.drawable.ic_close_black_24dp)
            sendMoreLayout.visibility = View.VISIBLE
        } else {
            sendMoreButton.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            sendMoreLayout.visibility = View.GONE
        }
    }

    fun insertMention(mention: Phone) {
        on<GroupMessageParseHandler>().insertMention(replyMessage, mention)
    }

    private fun updateSendButton() {
        if (replyMessage.text.toString().isBlank()) {
            sendButton.setImageResource(R.drawable.ic_camera_black_24dp)
            sendMoreButton.visibility = View.VISIBLE
            on<GroupActionHandler>().show(true)
        } else {
            sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp)
            sendMoreButton.visibility = View.GONE
            sendMoreLayout.visibility = View.GONE
            sendMoreButton.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            on<GroupActionHandler>().show(false)
        }
    }

    private fun setGroupMessages(groupMessages: List<GroupMessage>) {
        groupMessagesAdapter.setGroupMessages(groupMessages)
        if (layoutManager.findFirstVisibleItemPosition() < 3) {
            recyclerView.post { recyclerView.smoothScrollToPosition(0) }
        }
    }

    private fun send(text: String): Boolean {
        if (on<PersistenceHandler>().phoneId == null) {
            on<DefaultAlerts>().thatDidntWork()
            return false
        }

        if (on<GroupHandler>().group == null) {
            on<DefaultAlerts>().thatDidntWork()
            return false
        }

        if (on<GroupHandler>().groupContact == null) {
            if (!on<GroupHandler>().group!!.isPublic && !on<GroupHandler>().group!!.hasEvent()) {
                return false
            }
        }

        val groupMessage = GroupMessage()
        groupMessage.text = text
        groupMessage.to = on<GroupHandler>().group!!.id
        groupMessage.from = on<PersistenceHandler>().phoneId
        groupMessage.time = Date()
        on<StoreHandler>().store.box(GroupMessage::class.java).put(groupMessage)
        on<SyncHandler>().sync(groupMessage)

        return true
    }
}
