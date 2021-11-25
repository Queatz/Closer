package closer.vlllage.com.closer.handler.group

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.media.MediaHandler
import closer.vlllage.com.closer.handler.post.CreatePostActivityTransitionHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_group_messages.view.*
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
    private var isRespond: Boolean = false

    fun attach(recyclerView: RecyclerView, replyMessage: EditText, sendButton: ImageButton, sendMoreButton: ImageButton, sendMoreLayout: View) {
        this.replyMessage = replyMessage
        this.sendButton = sendButton
        this.sendMoreButton = sendMoreButton
        this.sendMoreLayout = sendMoreLayout
        this.recyclerView = recyclerView

        if (isRespond) {
            replyMessage.postDelayed({
                replyMessage.requestFocus()
                on<KeyboardHandler>().showKeyboard(replyMessage, true)
            }, 500)
        }

        layoutManager = LinearLayoutManager(
                on<ActivityHandler>().activity,
                RecyclerView.VERTICAL,
                true
        )

        recyclerView.layoutManager = layoutManager

        groupMessagesAdapter = GroupMessagesAdapter(On(on).apply {
            use<GroupMessageHelper> {
                onSuggestionClickListener = { suggestion -> (on<ActivityHandler>().activity as CircularRevealActivity).finish { on<MapActivityHandler>().showSuggestionOnMap(suggestion) } }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(null, event) }
                onGroupClickListener = { group -> on<GroupActivityTransitionHandler>().showGroupMessages(null, group.id) }
            }
        })
        recyclerView.adapter = groupMessagesAdapter

        this.replyMessage.setOnEditorActionListener { textView, action, _ ->
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
            on<TypingHandler>().setGroup(group.id!!)
            replyMessage.setText(on<GroupMessageParseHandler>().parseText(replyMessage, on<GroupDraftHandler>().getDraft(group.id!!)?.message ?: ""))
            updateSendButton()
        }

        sendMoreLayout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updatePadding()
        }

        var stopTypingDisposable: Disposable? = null

        this.replyMessage.doAfterTextChanged {
            on<TypingHandler>().isTyping = it!!.isNotEmpty()

            if (on<TypingHandler>().isTyping) {
                stopTypingDisposable?.let { on<DisposableHandler>().dispose(it) }
                stopTypingDisposable = on<TimerHandler>().postDisposable({
                    on<TypingHandler>().isTyping = false
                }, 5000)
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

                on<GroupHandler>().group?.let { group ->
                    on<GroupDraftHandler>().saveDraft(group.id!!, text.toString())
                    on<GroupMessageMentionHandler>().showSuggestionsForName(on<GroupMessageParseHandler>().extractName(text, replyMessage.selectionStart))
                }

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    on<GroupMessageParseHandler>().deleteMention(replyMessage)
                }
            }
        })

        this.sendMoreButton.setOnClickListener { showSendMoreOptions(!sendMoreLayout.visible) }

        val sendMoreActionAudio = this.sendMoreLayout.sendMoreActionAudio
        val sendMoreActionVideo = this.sendMoreLayout.sendMoreActionVideo
        val sendMoreActionFile = this.sendMoreLayout.sendMoreActionFile
        val sendMoreActionPhoto = this.sendMoreLayout.sendMoreActionPhoto
        val sendMoreActionPost = this.sendMoreLayout.sendMoreActionPost

        sendMoreActionAudio.setOnClickListener {
            this.sendMoreButton.callOnClick()
            on<DefaultAlerts>().message("Woah matey!")
        }
        sendMoreActionVideo.setOnClickListener {
            this.sendMoreButton.callOnClick()
            on<DefaultAlerts>().message("Woah matey!")
        }
        sendMoreActionFile.setOnClickListener {
            this.sendMoreButton.callOnClick()
            on<DefaultAlerts>().message("Woah matey!")
        }
        sendMoreActionPost.setOnClickListener {
            this.sendMoreButton.callOnClick()
            on<CreatePostActivityTransitionHandler>().show(on<GroupHandler>().group!!.id!!)
        }
        sendMoreActionPhoto.setOnClickListener {
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

            on<GroupNameHelper>().loadName(group, replyMessage, true) {
                on<ResourcesHandler>().resources.getString(when {
                    group.direct -> R.string.talk_with_x
                    group.hasPhone() -> R.string.talk_on_x_profile
                    else -> R.string.say_something_in
                }, it)
            }

            groupMessagesSubscription = on<StoreHandler>().store.box(GroupMessage::class).query(
                    GroupMessage_.to.equal(group.id!!)
            )
                    .sort(on<SortHandler>().sortGroupMessages())
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { setGroupMessages(it) }

            on<DisposableHandler>().add(groupMessagesSubscription!!)
        }

        on<DisposableHandler>().add(
                on<LightDarkHandler>().onLightChanged.subscribe {
                    sendMoreButton.imageTintList = it.tint
                    sendMoreActionAudio.imageTintList = it.tint
                    sendMoreActionVideo.imageTintList = it.tint
                    sendMoreActionFile.imageTintList = it.tint
                    sendMoreActionPhoto.imageTintList = it.tint
                    sendMoreActionPost.imageTintList = it.tint
                }
        )
    }

    private fun updatePadding() {
        replyMessage.setPadding(
                replyMessage.paddingLeft,
                replyMessage.paddingTop,
                (sendMoreLayout.width.takeIf { sendMoreLayout.visible } ?: 0) +
                (sendMoreButton.width.takeIf { sendMoreButton.visible } ?: 0) +
                (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.padDouble).takeIf {
                    !sendMoreLayout.visible && !sendMoreButton.visible
                } ?: 0),
                replyMessage.paddingBottom
        )
    }

    fun insertMention(mention: Phone) {
        on<GroupMessageParseHandler>().insertMention(replyMessage, mention)
    }

    private fun showSendMoreOptions(show: Boolean) {
        sendMoreButton.setImageResource(if (show) R.drawable.ic_close_black_24dp else R.drawable.ic_more_horiz_black_24dp)
        sendMoreLayout.visible = show
        updatePadding()
    }

    private fun updateSendButton() {
        val blank = replyMessage.text.toString().isBlank()

        if (blank) {
            sendButton.setImageResource(R.drawable.ic_camera_black_24dp)
            sendMoreButton.visible = true
            on<GroupActionHandler>().show(true)
        } else {
            sendButton.setImageResource(R.drawable.ic_chevron_right_black_24dp)
            sendMoreButton.visible = false
            sendMoreLayout.visible = false
            sendMoreButton.setImageResource(R.drawable.ic_more_horiz_black_24dp)
            on<GroupActionHandler>().show(false)
        }

        updatePadding()
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

        val groupMessage = GroupMessage()
        groupMessage.text = text
        groupMessage.to = on<GroupHandler>().group!!.id
        groupMessage.from = on<PersistenceHandler>().phoneId
        groupMessage.created = Date()
        on<StoreHandler>().store.box(GroupMessage::class).put(groupMessage)
        on<SyncHandler>().sync(groupMessage)

        return true
    }

    fun setIsRespond() {
        isRespond = true
    }
}
