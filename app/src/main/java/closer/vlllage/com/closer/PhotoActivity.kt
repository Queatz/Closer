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

        enterAnimationCompleteObservable.onNext(true)

        if (intent != null) {
            val photoUrl = intent.getStringExtra(EXTRA_PHOTO)
            `$`(ImageHandler::class.java).get().load(photoUrl)
                    .transform(RoundedCornersTransformation(`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.imageCorners), 0))
                    .into(photo, object : Callback {
                        override fun onSuccess() {
                            `$`(DisposableHandler::class.java).add(enterAnimationCompleteObservable
                                    .subscribeOn(Schedulers.computation())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ loadFullRes(photoUrl) }, { it.printStackTrace() }))
                        }

                        override fun onError(e: Exception) {
                            onSuccess()
                        }
                    })
        }

        photo.setOnClickListener { view -> finish() }

        findViewById<View>(R.id.actionShare).setOnClickListener { view ->
            if (photo.drawable is BitmapDrawable) {
                `$`(SystemShareHandler::class.java).share((photo.drawable as BitmapDrawable).bitmap)
            } else {
                `$`(DefaultAlerts::class.java).thatDidntWork()
            }
        }
    }

    override val backgroundId = R.id.activityLayout

    private fun loadFullRes(photoUrl: String) {
        `$`(ImageHandler::class.java).get().load(photoUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "?s=1600")
                .noPlaceholder()
                .transform(RoundedCornersTransformation(`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.imageCorners), 0))
                .into(photo)
    }

    companion object {
        val EXTRA_PHOTO = "photo"
    }
}
