package closer.vlllage.com.closer.handler.map

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.bubble.MapBubble
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.group.GroupActivityTransitionHandler
import closer.vlllage.com.closer.handler.group.PhotoActivityTransitionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.share.ShareActivityTransitionHandler
import com.queatz.on.On
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import closer.vlllage.com.closer.ui.AnimationDuration

class ReplyLayoutHandler constructor(private val on: On) {

    private lateinit var replyLayout: View
    private lateinit var replyLayoutName: TextView
    private lateinit var replyLayoutPhoto: ImageView
    private lateinit var replyLayoutStatus: TextView
    private lateinit var sendButton: View
    private lateinit var replyMessage: EditText
    private lateinit var getDirectionsButton: ImageButton
    private lateinit var showOnMapButton: ImageButton
    private lateinit var inviteToGroupButton: ImageButton

    private var replyingToMapBubble: MapBubble? = null

    val isVisible: Boolean
        get() = replyLayout.visibility != View.GONE

    val height: Int
        get() = replyLayout.measuredHeight

    fun attach(replyLayout: View) {
        this.replyLayout = replyLayout
        this.sendButton = replyLayout.findViewById(R.id.sendButton)
        this.replyMessage = replyLayout.findViewById(R.id.message)
        this.replyLayoutName = replyLayout.findViewById(R.id.replyLayoutName)
        this.replyLayoutPhoto = replyLayout.findViewById(R.id.replyLayoutPhoto)
        this.replyLayoutStatus = replyLayout.findViewById(R.id.replyLayoutStatus)
        this.getDirectionsButton = replyLayout.findViewById(R.id.getDirectionsButton)
        this.showOnMapButton = replyLayout.findViewById(R.id.showOnMapButton)
        this.inviteToGroupButton = replyLayout.findViewById(R.id.inviteToGroupButton)

        sendButton.setOnClickListener { view -> reply() }

        replyMessage.setOnEditorActionListener { textView, action, keyEvent ->
            if (action == EditorInfo.IME_ACTION_GO) {
                reply()
            }

            false
        }

        replyMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                sendButton.isEnabled = !charSequence.toString().isBlank()
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        getDirectionsButton.setOnClickListener { view -> on<OutboundHandler>().openDirections(replyingToMapBubble!!.latLng) }

        showOnMapButton.setOnClickListener { view -> on<MapActivityHandler>().showPhoneOnMap(replyingToMapBubble!!.phone) }

        inviteToGroupButton.setOnClickListener { view -> on<ShareActivityTransitionHandler>().inviteToGroup(replyingToMapBubble!!.phone!!) }
    }

    private fun reply() {
        on<DisposableHandler>().add(on<ApiHandler>().sendMessage(replyingToMapBubble!!.phone!!, replyMessage.text.toString()).subscribe({ successResult ->
            if (!successResult.success) {
                on<DefaultAlerts>().thatDidntWork()
            }
        }, { error -> on<DefaultAlerts>().thatDidntWork() }))
        replyMessage.setText("")
        showReplyLayout(false)
    }

    fun replyTo(mapBubble: MapBubble) {
        replyingToMapBubble = mapBubble

        replyLayoutName.text = on<Val>().of(replyingToMapBubble?.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
        replyLayoutName.setOnClickListener { v ->
            on<DisposableHandler>().add(on<DataHandler>().getGroupForPhone(replyingToMapBubble!!.phone!!)
                    .subscribe({ group ->
                        on<GroupActivityTransitionHandler>().showGroupMessages(
                                replyLayoutName, group.id
                        )
                    }, { error -> on<DefaultAlerts>().thatDidntWork() }))
        }

        if (mapBubble.phone != null) {
            val phone = on<StoreHandler>().store.box(Phone::class.java).query().equal(Phone_.id, mapBubble.phone!!).build().findFirst()

            if (phone != null && !on<Val>().isEmpty(phone.photo)) {
                replyLayoutPhoto.visibility = View.VISIBLE
                on<PhotoHelper>().loadCircle(replyLayoutPhoto, phone.photo!!)
                replyLayoutPhoto.setOnClickListener { v -> on<PhotoActivityTransitionHandler>().show(replyLayoutPhoto, phone.photo!!) }
            } else {
                replyLayoutPhoto.visibility = View.GONE
            }
        } else {
            replyLayoutPhoto.visibility = View.GONE
        }

        replyLayoutStatus.text = replyingToMapBubble!!.status

        if (replyingToMapBubble!!.latLng != null) {
            on<MapHandler>().centerMap(replyingToMapBubble!!.latLng!!)
        }

        showReplyLayout(true)
    }

    fun showReplyLayout(show: Boolean) {
        if (show) {
            if (replyLayout.visibility == View.VISIBLE && (replyLayout.animation == null || replyLayout.animation != null && replyLayout.animation.duration == AnimationDuration.ENTER_DURATION.toLong())) {
                return
            }
        } else {
            if (replyLayout.visibility == View.GONE) {
                return
            }
        }

        replyLayout.visibility = View.VISIBLE
        replyMessage.setText("")
        sendButton.isEnabled = false
        val animation: Animation

        replyLayout.clearAnimation()
        val layoutParams = replyLayout.layoutParams as ViewGroup.MarginLayoutParams
        val totalHeight = replyLayout.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin

        if (show) {
            animation = TranslateAnimation(0f, 0f, (-totalHeight).toFloat(), 0f)
            animation.setInterpolator(AccelerateDecelerateInterpolator())
            animation.setDuration(AnimationDuration.ENTER_DURATION.toLong())
            replyLayout.post {
                replyMessage.requestFocus()
                on<KeyboardHandler>().showKeyboard(replyMessage, true)
            }
        } else {
            animation = TranslateAnimation(0f, 0f, replyLayout.translationY, (-totalHeight).toFloat())
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    replyLayout.visibility = View.GONE
                    replyLayout.animation = null
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            animation.setInterpolator(DecelerateInterpolator())
            animation.setDuration(AnimationDuration.EXIT_DURATION.toLong())
            on<KeyboardHandler>().showKeyboard(replyMessage, false)
        }

        replyLayout.startAnimation(animation)
    }
}
