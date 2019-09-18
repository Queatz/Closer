package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import com.google.gson.JsonObject
import io.objectbox.android.AndroidScheduler
import kotlinx.android.synthetic.main.fragment_group_events.messagesRecyclerView
import kotlinx.android.synthetic.main.fragment_group_reviews.*

class GroupReviewsFragment : PoolActivityFragment() {

    private lateinit var groupMessagesAdapter: GroupMessagesAdapter
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var groupDisposableGroup: DisposableGroup

    private var selectedRating = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_group_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        groupDisposableGroup = disposableGroup.group()

        groupMessagesAdapter = GroupMessagesAdapter(on)
        groupMessagesAdapter.global = true
        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        groupMessagesAdapter.onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(view, event) }
        groupMessagesAdapter.onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(view, group1.id) }

        messagesRecyclerView.layoutManager = LinearLayoutManager(messagesRecyclerView.context)
        messagesRecyclerView.adapter = groupMessagesAdapter

        on<GroupHandler> {
            onGroupChanged(disposableGroup) { group ->
                observeReviews(group)

                stars1Button.setOnClickListener { selectRating(group, 1) }
                stars2Button.setOnClickListener { selectRating(group, 2) }
                stars3Button.setOnClickListener { selectRating(group, 3) }
                stars4Button.setOnClickListener { selectRating(group, 4) }
                stars5Button.setOnClickListener { selectRating(group, 5) }
            }
        }

    }

    private fun observeReviews(group: Group) {
        groupDisposableGroup.clear()

        val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
        groupDisposableGroup.add(queryBuilder
                .sort(on<SortHandler>().sortGroupMessages())
                .equal(GroupMessage_.to, group.id!!)
                .contains(GroupMessage_.attachment, "\"review\"")
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { groupMessages ->
                    groupMessagesAdapter.setGroupMessages(groupMessages.let {
                        if (selectedRating > 0) {
                            it.filter { groupMessage ->
                                on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
                                        .get("review").asJsonObject
                                        .get("rating").asInt == selectedRating }
                        } else {
                            it
                        }
                    })
                    refreshSummary(groupMessages)
                })
    }

    private fun selectRating(group: Group, rating: Int) {
        selectedRating = if (selectedRating == rating) 0 else rating
        observeReviews(group)

        stars1Button.setBackgroundResource(if (selectedRating == 1) R.drawable.clickable_white_25_512dp else R.drawable.clickable_light_50_rounded_512dp)
        stars2Button.setBackgroundResource(if (selectedRating == 2) R.drawable.clickable_white_25_512dp else R.drawable.clickable_light_50_rounded_512dp)
        stars3Button.setBackgroundResource(if (selectedRating == 3) R.drawable.clickable_white_25_512dp else R.drawable.clickable_light_50_rounded_512dp)
        stars4Button.setBackgroundResource(if (selectedRating == 4) R.drawable.clickable_white_25_512dp else R.drawable.clickable_light_50_rounded_512dp)
        stars5Button.setBackgroundResource(if (selectedRating == 5) R.drawable.clickable_white_25_512dp else R.drawable.clickable_light_50_rounded_512dp)
    }

    private fun refreshSummary(groupMessages: List<GroupMessage>) {
        val ratings = groupMessages.map { groupMessage ->
            on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
                    .get("review").asJsonObject
                    .get("rating").asInt
        }
        stars1ProgressBar.progress = (ratings.count { rating -> rating == 1 }.toFloat() / ratings.size * 100).toInt()
        stars2ProgressBar.progress = (ratings.count { rating -> rating == 2 }.toFloat() / ratings.size * 100).toInt()
        stars3ProgressBar.progress = (ratings.count { rating -> rating == 3 }.toFloat() / ratings.size * 100).toInt()
        stars4ProgressBar.progress = (ratings.count { rating -> rating == 4 }.toFloat() / ratings.size * 100).toInt()
        stars5ProgressBar.progress = (ratings.count { rating -> rating == 5 }.toFloat() / ratings.size * 100).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }

}
