package closer.vlllage.com.closer


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestAdapter
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.MiniWindowHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.ui.CircularRevealActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.activity_feature_requests.*

class FeatureRequestsActivity : CircularRevealActivity() {

    private lateinit var adapter: FeatureRequestAdapter
    private val searchString = BehaviorSubject.createDefault("")

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

        featureRequestsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (betaMessage.visible) {
                        betaMessage.visible = false
                    }
                }
            }
        })

        on<DisposableHandler>().add(on<FeatureRequestsHandler>().featureRequestsObservable
                .switchMap { features -> searchString.map { features.filter { feature ->
                    it.isBlank() || feature.name!!.contains(Regex(it)) || feature.description!!.contains(Regex(it, RegexOption.IGNORE_CASE))
                } } }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
            adapter.items = it
                    .sortedByDescending { it.created?.after(on<TimeAgo>().daysAgo(3)) ?: false }
                    .sortedByDescending { !it.completed }
                    .toMutableList()
        })

        on<MiniWindowHandler>().attach(windowTitle, backgroundColor) { finish() }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchString.onNext(s.toString())
            }
        })
    }

    override val backgroundId = R.id.activityLayout
}
