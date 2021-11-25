package closer.vlllage.com.closer

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_PERCENT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
import androidx.core.view.updateLayoutParams
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.call.CallConnectionHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.NotificationHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.pool.PoolActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_call.*


class CallActivity : PoolActivity() {

    companion object {
        /**
         * The ID of the other phone
         */
        const val EXTRA_CALL_PHONE_ID = "callPhoneId"
        /**
         * The name of the other phone
         */
        const val EXTRA_CALL_PHONE_NAME = "callPhoneName"

        /**
         * True if I'm being called, false if I'm the one calling
         */
        const val EXTRA_INCOMING = "incoming"

        /**
         * True if should answer the call
         */
        const val EXTRA_ANSWER = "answer"
    }

    private var ring: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        if (intent != null) onNewIntent(intent)

//        remoteView!!.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
//        localView!!.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)

        on<ApplicationHandler>().app.on<CallConnectionHandler>().active
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (!it) {
                        finish()
                    }
                }.also {
                    on<DisposableHandler>().add(it)
                }

        on<NotificationHandler>().hideFullScreen()

        updateLayout(resources.configuration.orientation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        updateLayout(newConfig.orientation)
    }

    private fun updateLayout(orientation: Int) {
        if (orientation == ORIENTATION_LANDSCAPE) {
            localView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                constrainedWidth = false
                constrainedHeight = true
                matchConstraintDefaultWidth = MATCH_CONSTRAINT_SPREAD
                matchConstraintDefaultHeight = MATCH_CONSTRAINT_PERCENT
                matchConstraintPercentWidth = 0f
                matchConstraintPercentHeight = .33f
                dimensionRatio = "1.5:1"
            }
        } else {
            localView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                constrainedWidth = true
                constrainedHeight = false
                matchConstraintDefaultWidth = MATCH_CONSTRAINT_PERCENT
                matchConstraintDefaultHeight = MATCH_CONSTRAINT_SPREAD
                matchConstraintPercentWidth = .33f
                matchConstraintPercentHeight = 0f
                dimensionRatio = "1:1.5"
            }
        }

        localView.requestLayout()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        var incoming = intent.getBooleanExtra(EXTRA_INCOMING, false)
        val autoAnswer = on<SettingsHandler>().get(UserLocalSetting.CLOSER_SETTINGS_AUTO_ANSWER_CALLS) ||
                intent.getBooleanExtra(EXTRA_ANSWER, false)

        showAnswer(incoming && !autoAnswer)

        ring?.release()

        if (incoming) {
            if (!autoAnswer) {
                ring = MediaPlayer.create(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)).apply {
                    setOnPreparedListener {
                        isLooping = true
                        start()
                    }
                }
            }
        } else {
            ring = MediaPlayer.create(this, R.raw.ring).apply {
                setOnPreparedListener {
                    isLooping = true
                    start()
                }
            }

            on<ApplicationHandler>().app.on<CallConnectionHandler>().active.take(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        ring?.release()
                        ring = null
                    }.also {
                        on<DisposableHandler>().add(it)
                    }
        }

        answerButton.setOnClickListener {
            showAnswer(false)

            if (incoming && !autoAnswer) {
                ring?.stop()
                incoming = false
                on<ApplicationHandler>().app.on<CallConnectionHandler>().answerIncomingCall()
            } else {
                on<ApplicationHandler>().app.on<CallConnectionHandler>().endCall()
                finish()
            }
        }

        if (intent.hasExtra(EXTRA_CALL_PHONE_ID)) {
            val otherPhoneId = intent.getStringExtra(EXTRA_CALL_PHONE_ID)!!

            handleCall(otherPhoneId, incoming, autoAnswer)

            on<DataHandler>().getPhone(otherPhoneId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.photo?.let {
                            background.visible = true
                            on<ImageHandler>().get().load("$it?s=12")
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            on<TimerHandler>().post { loadHighRes(it) }
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            on<TimerHandler>().post { loadHighRes(it) }
                                            return false
                                        }
                                    })
                                    .into(background)
                        }
                        name.text = on<NameHandler>().getName(it)
                    }, {
                        on<ConnectionErrorHandler>().notifyConnectionError()
                    }).also {
                on<DisposableHandler>().add(it)
            }
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        if (on<ApplicationHandler>().app.on<CallConnectionHandler>().isInCall()) {
            on<ApplicationHandler>().app.on<CallConnectionHandler>().endCall()
        }

        super.onBackPressed()
    }

    override fun onDestroy() {
        ring?.release()
        on<ApplicationHandler>().app.on<CallConnectionHandler>().detach()
        super.onDestroy()
    }

    private fun handleCall(otherPhoneId: String, incoming: Boolean, answer: Boolean) {
        on<PermissionHandler>().check(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).`when` {
            when (it) {
                true -> {
                    on<ApplicationHandler>().app.on<CallConnectionHandler>().attach(otherPhoneId, localView, remoteView)

                    if (!incoming) {
                        on<ApplicationHandler>().app.on<CallConnectionHandler>().call()
                    } else if (answer) {
                        on<ApplicationHandler>().app.on<CallConnectionHandler>().answerIncomingCall()
                    }
                }
                false -> on<DefaultAlerts>().thatDidntWork("No calling permissions")
            }
        }
    }

    private fun showAnswer(show: Boolean) {
        answerButton.setImageResource(if (show) R.drawable.ic_baseline_videocam_24 else R.drawable.ic_baseline_call_end_24)
        answerButton.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(
                if (show) R.color.green else R.color.red
        ))
    }

    private fun loadHighRes(photoUrl: String) {
        on<ImageHandler>().get().load("$photoUrl?s=512")
                .placeholder(background.drawable)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(background)
    }
}