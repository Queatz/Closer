package closer.vlllage.com.closer

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.call.CallConnectionHandler
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.data.PermissionHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_call.*

class CallActivity : PoolActivity() {

    companion object {
        /**
         * The ID of the other phone
         */
        const val EXTRA_CALL_PHONE_ID = "callPhoneId"

        /**
         * True if I'm being called, false if I'm the one calling
         */
        const val EXTRA_INCOMING = "incoming"
    }

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
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        if (intent != null) onNewIntent(intent)

        on<PermissionHandler>().check(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO).`when` {
            if (!it) {
                on<DefaultAlerts>().thatDidntWork("No audio permission")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        var incoming = intent.getBooleanExtra(EXTRA_INCOMING, false)

        showAnswer(incoming)

        answerButton.setOnClickListener {
            showAnswer(false)

            if (incoming) {
                incoming = false
                on<ApplicationHandler>().app.on<CallConnectionHandler>().answerIncomingCall()
            } else {
                on<ApplicationHandler>().app.on<CallConnectionHandler>().endCall()
                finish()
            }
        }

        if (intent.hasExtra(EXTRA_CALL_PHONE_ID)) {
            val otherPhoneId = intent.getStringExtra(EXTRA_CALL_PHONE_ID)!!

            on<ApplicationHandler>().app.on<CallConnectionHandler>().attach(otherPhoneId, localView, remoteView)

            if (!incoming) {
                on<ApplicationHandler>().app.on<CallConnectionHandler>().call()
            }

            on<DataHandler>().getPhone(otherPhoneId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                it.photo?.let {
                    background.visible = true
                    on<ImageHandler>().get().load("$it?s=12")
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                    on<TimerHandler>().post(Runnable { loadHighRes(it) })
                                    return false
                                }

                                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    on<TimerHandler>().post(Runnable { loadHighRes(it) })
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

    private fun showAnswer(show: Boolean) {
        answerButton.setImageResource(if (show) R.drawable.ic_baseline_phone_24 else R.drawable.ic_baseline_call_end_24)
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