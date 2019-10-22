package closer.vlllage.com.closer


import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestAdapter
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.MiniWindowHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.ui.CircularRevealActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_feature_requests.*

class FeatureRequestsActivity : CircularRevealActivity() {

    private lateinit var adapter: FeatureRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_requests)

        addButton.setOnClickListener {
            on<FeatureRequestsHandler>().add()
        }

        betaMessage.setOnClickListener {
            it.visible = false
        }

        adapter = FeatureRequestAdapter(on)

        featureRequestsRecyclerView.let {
            it.layoutManager = LinearLayoutManager(this@FeatureRequestsActivity)
            it.adapter = adapter
        }

        on<DisposableHandler>().add(on<FeatureRequestsHandler>().featureRequestsObservable
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
            adapter.items = it.sortedByDescending { it.created?.after(on<TimeAgo>().oneDayAgo()) ?: false }.toMutableList()
        })

        on<MiniWindowHandler>().attach(windowTitle, backgroundColor) { finish() }
    }

    override val backgroundId = R.id.activityLayout
}
