package closer.vlllage.com.closer

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ImageHandler
import closer.vlllage.com.closer.handler.helpers.TimerHandler
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
        const val EXTRA_CALL_PHONE_ID = "callPhoneId"
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

        if (intent != null) onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.hasExtra(EXTRA_CALL_PHONE_ID)) {
            on<DataHandler>().getPhone(intent.getStringExtra(EXTRA_CALL_PHONE_ID)!!).observeOn(AndroidSchedulers.mainThread()).subscribe({
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

    private fun loadHighRes(photoUrl: String) {
        on<ImageHandler>().get().load("$photoUrl?s=512")
                .placeholder(background.drawable)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(background)
    }
}