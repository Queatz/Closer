package closer.vlllage.com.closer

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Callback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlin.math.abs

class PhotoActivity : CircularRevealActivity() {

    private lateinit var photo: PhotoView
    private val enterAnimationCompleteObservable = BehaviorSubject.create<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
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
            on<ImageHandler>().get().load(photoUrl)
                    .into(photo, object : Callback {
                        override fun onSuccess() {
                            on<DisposableHandler>().add(enterAnimationCompleteObservable
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ loadFullRes(photoUrl) }, {
                                        on<ToastHandler>().show(R.string.failed_to_load_photo)
                                    }))
                        }

                        override fun onError(e: Exception) {
                            onSuccess()
                        }
                    })
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

    private fun loadFullRes(photoUrl: String) {
        on<ImageHandler>().get().load(photoUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "?s=1600")
                .noPlaceholder()
                .into(photo)
    }

    companion object {
        const val EXTRA_PHOTO = "photo"
        private const val SLOP_RADIUS = 32
    }
}
