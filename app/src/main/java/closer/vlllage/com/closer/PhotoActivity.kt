package closer.vlllage.com.closer

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.abs

class PhotoActivity : CircularRevealActivity() {

    private lateinit var photo: PhotoView
    private val enterAnimationCompleteObservable = BehaviorSubject.create<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        photo = findViewById(R.id.photo)
        photo.maximumScale = 8f

        photo.setOnSingleFlingListener { _, _, velocityX, velocityY ->
            if (photo.scale != 1f) {
                return@setOnSingleFlingListener false
            }

            if (abs(velocityX) > SLOP_RADIUS || abs(velocityY) > SLOP_RADIUS) {
                finish()
                return@setOnSingleFlingListener true
            }

            return@setOnSingleFlingListener false
        }

        enterAnimationCompleteObservable.onNext(true)

        if (intent != null) {
            val photoUrl = intent.getStringExtra(EXTRA_PHOTO)!!
            on<ImageHandler>().get().load(photoUrl).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    onSuccess(photoUrl)
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    onSuccess(photoUrl)
                    return false
                }

            }).into(photo)
        }

        photo.setOnClickListener { finish() }

        findViewById<View>(R.id.actionShare).setOnClickListener {
            if (photo.drawable is BitmapDrawable) {
                on<SystemShareHandler>().share((photo.drawable as BitmapDrawable).bitmap)
            } else {
                on<DefaultAlerts>().thatDidntWork()
            }
        }
    }

    override val backgroundId = R.id.activityLayout

    private fun onSuccess(photoUrl: String) {
        on<DisposableHandler>().add(enterAnimationCompleteObservable
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ loadFullRes(photoUrl) }, {
                    on<ToastHandler>().show(R.string.failed_to_load_photo)
                }))
    }

    private fun loadFullRes(photoUrl: String) {
        on<ImageHandler>().get().load(photoUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "?s=1600")
                .apply(RequestOptions().skipMemoryCache(true))
                .placeholder(photo.drawable)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(photo)
    }

    companion object {
        const val EXTRA_PHOTO = "photo"
        private const val SLOP_RADIUS = 32
    }
}
