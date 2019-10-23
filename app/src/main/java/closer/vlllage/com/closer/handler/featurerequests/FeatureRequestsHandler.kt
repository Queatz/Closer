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
                },
                buttonCallback = { name, description ->
                    when {
                        name.isBlank() -> {
                            on<DefaultAlerts>().message(R.string.feature_request_name_missing)
                            false
                        }
                        description.isBlank() -> {
                            on<DefaultAlerts>().message(R.string.feature_request_description_missing)
                            false
                        }
                        else -> true
                    }
        })
    }

    fun vote(featureRequestId: String, vote: Boolean) {
        on<DisposableHandler>().add(on<ApiHandler>().voteForFeatureRequest(featureRequestId, vote).subscribe({
            refresh()

            if (vote) {
                on<ToastHandler>().show(R.string.your_vote_has_been_added)
            } else {
                on<ToastHandler>().show(R.string.your_vote_has_been_removed)
            }
        }, { on<DefaultAlerts>().thatDidntWork() }))
    }

    fun complete(featureRequestId: String, completed: Boolean) {
        on<DisposableHandler>().add(on<ApiHandler>().completeFeatureRequest(featureRequestId, completed).subscribe({
            refresh()
        }, {
            on<DefaultAlerts>().thatDidntWork()
        }))
    }

    private fun refresh() {
        on<DisposableHandler>().add(on<ApiHandler>().getFeatureRequests().subscribe({
            featureRequestsObservable.onNext(it)
        }, { on<DefaultAlerts>().thatDidntWork() }))
    }
}
