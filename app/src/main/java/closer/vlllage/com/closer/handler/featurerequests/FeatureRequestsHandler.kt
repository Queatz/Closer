package closer.vlllage.com.closer.handler.featurerequests

import android.content.Intent
import closer.vlllage.com.closer.FeatureRequestsActivity
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.api.models.FeatureRequestResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.helpers.*
import com.queatz.on.On
import io.reactivex.subjects.BehaviorSubject

class FeatureRequestsHandler constructor(private val on: On) {

    companion object {
        val globalFeatureRequestsObservable = BehaviorSubject.createDefault(listOf<FeatureRequestResult>())
    }

    val featureRequestsObservable get() = globalFeatureRequestsObservable

    fun show() {
        on<ActivityHandler>().activity?.startActivity(
                Intent(on<ApplicationHandler>().app, FeatureRequestsActivity::class.java)
        )

        refresh()
    }

    fun add() {
        on<DefaultInput>().showWithDesc(
                R.string.add_feature_request,
                R.string.feature_request_name,
                R.string.details,
                button = R.string.create,
                callback = { name, description ->
                    on<ApiHandler>().addFeatureRequest(name, description).subscribe({
                        refresh()
                    }, { on<DefaultAlerts>().thatDidntWork() })
        })
    }

    fun vote(featureRequestId: String, vote: Boolean) {
        on<DisposableHandler>().add(on<ApiHandler>().voteForFeatureRequest(featureRequestId, vote).subscribe({
            refresh()
        }, { on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun refresh() {
        on<DisposableHandler>().add(on<ApiHandler>().getFeatureRequests().subscribe({
            featureRequestsObservable.onNext(it)
        }, { on<DefaultAlerts>().thatDidntWork() }))
    }
}
