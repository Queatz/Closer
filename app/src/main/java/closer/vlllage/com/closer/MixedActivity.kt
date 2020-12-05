package closer.vlllage.com.closer

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.feed.MixedHeaderAdapter
import closer.vlllage.com.closer.handler.helpers.DefaultAlerts
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.ui.CircularRevealActivity
import com.queatz.on.On
import io.reactivex.android.schedulers.AndroidSchedulers

class MixedActivity : CircularRevealActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mixedAdapter: MixedHeaderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrib = window.attributes
            attrib.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mixed)
        recyclerView = findViewById(R.id.recyclerView)

        mixedAdapter = MixedHeaderAdapter(On(on), true).apply {
            showFeedHeader = false
            useHeader = false
        }
        recyclerView.adapter = mixedAdapter

        recyclerView.layoutManager = LinearLayoutManager(
                recyclerView.context,
                RecyclerView.VERTICAL,
                false
        )
        recyclerView.itemAnimator = null

        mixedAdapter.content = FeedContent.POSTS

        on<DataHandler>().getStory(intent.getStringExtra(EXTRA_STORY_ID)!!)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            mixedAdapter.stories = mutableListOf(it)
                        },
                        {
                            on<DefaultAlerts>().thatDidntWork()
                        }
                ).also {
                    on<DisposableHandler>().add(it)
                }
    }

        override val backgroundId = R.id.background

    companion object {
        const val EXTRA_STORY_ID = "storyId"
    }
}