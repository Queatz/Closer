package closer.vlllage.com.closer


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.databinding.ActivityFeatureRequestsBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestAdapter
import closer.vlllage.com.closer.handler.featurerequests.FeatureRequestsHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.MiniWindowHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.pool.PoolActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class FeatureRequestsActivity : PoolActivity() {

    private lateinit var binding: ActivityFeatureRequestsBinding
    private lateinit var adapter: FeatureRequestAdapter
    private val searchString = BehaviorSubject.createDefault("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeatureRequestsBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        binding.addButton.setOnClickListener {
            on<FeatureRequestsHandler>().add()
        }

        binding.betaMessage.setOnClickListener {
            it.visible = false
        }

        adapter = FeatureRequestAdapter(on)

        binding.featureRequestsRecyclerView.let {
            it.layoutManager = LinearLayoutManager(this@FeatureRequestsActivity)
            it.adapter = adapter
        }

        binding.featureRequestsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (binding.betaMessage.visible) {
                        binding.betaMessage.visible = false
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

        on<MiniWindowHandler>().attach(binding.windowTitle, binding.backgroundColor) { finish() }

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchString.onNext(s.toString())
            }
        })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.activityLayout.setOnTouchListener { view, motionEvent ->
            view.setOnTouchListener(null)
            finish()
            true
        }
    }
}
