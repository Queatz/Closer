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
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import closer.vlllage.com.closer.ui.AnimationDuration

class ReplyLayoutHandler : PoolMember() {

    private var replyLayout: View? = null
    private var replyLayoutName: TextView? = null
    private var replyLayoutPhoto: ImageView? = null
    private var replyLayoutStatus: TextView? = null
    private var sendButton: View? = null
    private var replyMessage: EditText? = null
    private var getDirectionsButton: ImageButton? = null
    private var showOnMapButton: ImageButton? = null
    private var inviteToGroupButton: ImageButton? = null

    private var replyingToMapBubble: MapBubble? = null

    val isVisible: Boolean
        get() = replyLayout!!.visibility != View.GONE

    val height: Int
        get() = replyLayout!!.measuredHeight

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

        sendButton!!.setOnClickListener { view -> reply() }

        replyMessage!!.setOnEditorActionListener { textView, action, keyEvent ->
            if (action == EditorInfo.IME_ACTION_GO) {
                reply()
            }

            false
        }

        replyMessage!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                sendButton!!.isEnabled = !charSequence.toString().trim { it <= ' ' }.isEmpty()
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        getDirectionsButton!!.setOnClickListener { view -> `$`(OutboundHandler::class.java).openDirections(replyingToMapBubble!!.latLng) }

        showOnMapButton!!.setOnClickListener { view -> `$`(MapActivityHandler::class.java).showPhoneOnMap(replyingToMapBubble!!.phone) }

        inviteToGroupButton!!.setOnClickListener { view -> `$`(ShareActivityTransitionHandler::class.java).inviteToGroup(replyingToMapBubble!!.phone!!) }
    }

    private fun reply() {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).sendMessage(replyingToMapBubble!!.phone!!, replyMessage!!.text.toString()).subscribe({ successResult ->
            if (!successResult.success) {
                `$`(DefaultAlerts::class.java).thatDidntWork()
            }
        }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
        replyMessage!!.setText("")
        showReplyLayout(false)
    }

    fun replyTo(mapBubble: MapBubble) {
        replyingToMapBubble = mapBubble

        replyLayoutName!!.text = `$`(Val::class.java).of(replyingToMapBubble!!.name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name))
        replyLayoutName!!.setOnClickListener { v ->
            `$`(DisposableHandler::class.java).add(`$`(DataHandler::class.java).getGroupForPhone(replyingToMapBubble!!.phone!!)
                    .subscribe({ group ->
                        `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(
                                replyLayoutName, group.id
                        )
                    }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
        }

        if (mapBubble.phone != null) {
            val phone = `$`(StoreHandler::class.java).store.box(Phone::class.java).query().equal(Phone_.id, mapBubble.phone!!).build().findFirst()

            if (phone != null && !`$`(Val::class.java).isEmpty(phone.photo)) {
                replyLayoutPhoto!!.visibility = View.VISIBLE
                `$`(PhotoHelper::class.java).loadCircle(replyLayoutPhoto!!, phone.photo!!)
                replyLayoutPhoto!!.setOnClickListener { v -> `$`(PhotoActivityTransitionHandler::class.java).show(replyLayoutPhoto, phone.photo!!) }
            } else {
                replyLayoutPhoto!!.visibility = View.GONE
            }
        } else {
            replyLayoutPhoto!!.visibility = View.GONE
        }

        replyLayoutStatus!!.text = replyingToMapBubble!!.status

        if (replyingToMapBubble!!.latLng != null) {
            `$`(MapHandler::class.java).centerMap(replyingToMapBubble!!.latLng!!)
        }

        showReplyLayout(true)
    }

    fun showReplyLayout(show: Boolean) {
        if (show) {
            if (replyLayout!!.visibility == View.VISIBLE && (replyLayout!!.animation == null || replyLayout!!.animation != null && replyLayout!!.animation.duration == AnimationDuration.ENTER_DURATION.toLong())) {
                return
            }
        } else {
            if (replyLayout!!.visibility == View.GONE) {
                return
            }
        }

        replyLayout!!.visibility = View.VISIBLE
        replyMessage!!.setText("")
        sendButton!!.isEnabled = false
        val animation: Animation

        replyLayout!!.clearAnimation()
        val layoutParams = replyLayout!!.layoutParams as ViewGroup.MarginLayoutParams
        val totalHeight = replyLayout!!.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin

        if (show) {
            animation = TranslateAnimation(0f, 0f, (-totalHeight).toFloat(), 0f)
            animation.setInterpolator(AccelerateDecelerateInterpolator())
            animation.setDuration(AnimationDuration.ENTER_DURATION.toLong())
            replyLayout!!.post {
                replyMessage!!.requestFocus()
                `$`(KeyboardHandler::class.java).showKeyboard(replyMessage!!, true)
            }
        } else {
            animation = TranslateAnimation(0f, 0f, replyLayout!!.translationY, (-totalHeight).toFloat())
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    replyLayout!!.visibility = View.GONE
                    replyLayout!!.animation = null
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            animation.setInterpolator(DecelerateInterpolator())
            animation.setDuration(AnimationDuration.EXIT_DURATION.toLong())
            `$`(KeyboardHandler::class.java).showKeyboard(replyMessage!!, false)
        }

        replyLayout!!.startAnimation(animation)
    }
}
