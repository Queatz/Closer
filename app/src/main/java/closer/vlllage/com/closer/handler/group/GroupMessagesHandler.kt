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
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.ui.CircularRevealActivity
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataObserver
import io.objectbox.reactive.DataSubscription
import java.util.*

class GroupMessagesHandler : PoolMember() {

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
                `$`(ActivityHandler::class.java).activity,
                RecyclerView.VERTICAL,
                true
        )

        recyclerView.layoutManager = layoutManager

        groupMessagesAdapter = GroupMessagesAdapter(this)
        recyclerView.adapter = groupMessagesAdapter

        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> (`$`(ActivityHandler::class.java).activity as CircularRevealActivity).finish { `$`(MapActivityHandler::class.java).showSuggestionOnMap(suggestion) } }
        groupMessagesAdapter.onEventClickListener = { event -> (`$`(ActivityHandler::class.java).activity as CircularRevealActivity).finish { `$`(GroupActivityTransitionHandler::class.java).showGroupForEvent(null, event) } }
        groupMessagesAdapter.onGroupClickListener = { group -> (`$`(ActivityHandler::class.java).activity as CircularRevealActivity).finish { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(null, group.id) } }


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
                `$`(CameraHandler::class.java).showCamera { photoUri ->
                    `$`(PhotoUploadGroupMessageHandler::class.java).upload(photoUri!!) { photoId ->
                        val success = `$`(GroupMessageAttachmentHandler::class.java).sharePhoto(`$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId), `$`(GroupHandler::class.java).group!!.id!!)
                        if (!success) {
                            `$`(DefaultAlerts::class.java).thatDidntWork()
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
        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onGroupChanged().subscribe({ group ->
            if (replyMessage.text.toString().isEmpty()) {
                replyMessage.setText(`$`(GroupMessageParseHandler::class.java).parseText(`$`(GroupDraftHandler::class.java).getDraft(group)!!))
                updateSendButton()
            }
        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))

        this.replyMessage.addTextChangedListener(object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && `$`(GroupMessageParseHandler::class.java).isMentionSelected(replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                updateSendButton()
                `$`(GroupDraftHandler::class.java).saveDraft(`$`(GroupHandler::class.java).group!!, text.toString())
                `$`(GroupMessageMentionHandler::class.java).showSuggestionsForName(`$`(GroupMessageParseHandler::class.java).extractName(text, replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    `$`(GroupMessageParseHandler::class.java).deleteMention(replyMessage)
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
            `$`(DefaultAlerts::class.java).message("Woah matey!")
        }
        sendMoreActionVideo.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            `$`(DefaultAlerts::class.java).message("Woah matey!")
        }
        sendMoreActionFile.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            `$`(DefaultAlerts::class.java).message("Woah matey!")
        }
        sendMoreActionPhoto.setOnClickListener { view ->
            this.sendMoreButton.callOnClick()
            `$`(MediaHandler::class.java).getPhoto { photoUri ->
                `$`(PhotoUploadGroupMessageHandler::class.java).upload(photoUri) { photoId ->
                    val success = `$`(GroupMessageAttachmentHandler::class.java).sharePhoto(`$`(PhotoUploadGroupMessageHandler::class.java).getPhotoPathFromId(photoId), `$`(GroupHandler::class.java).group!!.id!!)
                    if (!success) {
                        `$`(DefaultAlerts::class.java).thatDidntWork()
                    }
                }
            }
        }

        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onGroupChanged().subscribe { group ->
            if (groupMessagesSubscription != null) {
                `$`(DisposableHandler::class.java).dispose(groupMessagesSubscription!!)
            }

            groupMessagesSubscription = `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).query()
                    .equal(GroupMessage_.to, group.id!!)
                    .sort(`$`(SortHandler::class.java).sortGroupMessages())
                    .build()
                    .subscribe().on(AndroidScheduler.mainThread())
                    .observer(DataObserver<List<GroupMessage>> { this.setGroupMessages(it) })

            `$`(DisposableHandler::class.java).add(groupMessagesSubscription!!)
        })
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
        `$`(GroupMessageParseHandler::class.java).insertMention(replyMessage, mention)
    }

    private fun updateSendButton() {
        if (replyMessage.text.toString().isBlank()) {
            sendButton.setImageResource(R.drawable.ic_camera_black_24dp)
            sendMoreButton.visibility = View.VISIBLE
            `$`(GroupActionHandler::class.java).show(true)
        } else {
            sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp)
            sendMoreButton.visibility = View.GONE
            sendMoreLayout.visibility = View.GONE
            sendMoreButton.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            `$`(GroupActionHandler::class.java).show(false)
        }
    }

    private fun setGroupMessages(groupMessages: List<GroupMessage>) {
        groupMessagesAdapter.setGroupMessages(groupMessages)
        if (layoutManager.findFirstVisibleItemPosition() < 3) {
            recyclerView.post { recyclerView.smoothScrollToPosition(0) }
        }
    }

    private fun send(text: String): Boolean {
        if (`$`(PersistenceHandler::class.java).phoneId == null) {
            `$`(DefaultAlerts::class.java).thatDidntWork()
            return false
        }

        if (`$`(GroupHandler::class.java).group == null) {
            return false
        }

        if (`$`(GroupHandler::class.java).groupContact == null) {
            if (!`$`(GroupHandler::class.java).group!!.isPublic && !`$`(GroupHandler::class.java).group!!.hasEvent()) {
                return false
            }
        }

        val groupMessage = GroupMessage()
        groupMessage.text = text
        groupMessage.to = `$`(GroupHandler::class.java).group!!.id
        groupMessage.from = `$`(PersistenceHandler::class.java).phoneId
        groupMessage.time = Date()
        `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).put(groupMessage)
        `$`(SyncHandler::class.java).sync(groupMessage)

        return true
    }
}
