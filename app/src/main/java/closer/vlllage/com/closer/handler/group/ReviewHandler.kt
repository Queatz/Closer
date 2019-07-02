package closer.vlllage.com.closer.handler.group

import android.widget.EditText
import android.widget.RatingBar
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.AlertHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.ToastHandler
import closer.vlllage.com.closer.store.models.Group
import com.queatz.on.On
import kotlinx.android.synthetic.main.review_modal.view.*

class ReviewHandler constructor(private val on: On) {
    fun postReview(group: Group) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.review_modal
            onAfterViewCreated = { alertConfig, view ->
                val rating = view.rating
                val input = view.input

                rating.setOnRatingBarChangeListener { _, _, fromUser ->
                    if (fromUser && rating.rating < 1f) {
                        rating.rating = 1f
                    }
                }

                alertConfig.alertResult = ReviewViewHolder(
                        rating,
                        input
                )
            }
            title = on<ResourcesHandler>().resources.getString(R.string.review_place, group.name)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.review)
            buttonClickCallback = {
                if ((it as ReviewViewHolder).rating.rating < 1f) {
                    on<ToastHandler>().show(R.string.choose_a_rating)
                    false
                } else true
            }
            positiveButtonCallback = {
                on<GroupMessageAttachmentHandler>().postReview(
                        group.id!!,
                        (it as ReviewViewHolder).rating.rating.toInt(),
                        (it as ReviewViewHolder).review.text.toString()
                )
            }
            show()
        }
    }
}

class ReviewViewHolder constructor(
        val rating: RatingBar,
        val review: EditText
)