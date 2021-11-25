package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import closer.vlllage.com.closer.databinding.FragmentGroupReviewsBinding
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import com.google.gson.JsonObject
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder

class GroupReviewsFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentGroupReviewsBinding
    private lateinit var groupMessagesAdapter: GroupMessagesAdapter
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var groupDisposableGroup: DisposableGroup

    private var selectedRating = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentGroupReviewsBinding.inflate(inflater, container, false). let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        groupDisposableGroup = disposableGroup.group()

        groupMessagesAdapter = GroupMessagesAdapter(on)
        on<GroupMessageHelper>().global = true
        on<GroupMessageHelper>().onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        on<GroupMessageHelper>().onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(view, event) }
        on<GroupMessageHelper>().onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(view, group1.id) }

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(binding.messagesRecyclerView.context)
        binding.messagesRecyclerView.adapter = groupMessagesAdapter

        on<GroupHandler> {
            onGroupChanged(disposableGroup) { group ->
                observeReviews(group)

                binding.stars1Button.setOnClickListener { selectRating(group, 1) }
                binding.stars2Button.setOnClickListener { selectRating(group, 2) }
                binding.stars3Button.setOnClickListener { selectRating(group, 3) }
                binding.stars4Button.setOnClickListener { selectRating(group, 4) }
                binding.stars5Button.setOnClickListener { selectRating(group, 5) }
            }
        }

        disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            binding.stars1Button.setBackgroundResource(it.clickableRoundedBackgroundLight)
            binding.stars2Button.setBackgroundResource(it.clickableRoundedBackgroundLight)
            binding.stars3Button.setBackgroundResource(it.clickableRoundedBackgroundLight)
            binding.stars4Button.setBackgroundResource(it.clickableRoundedBackgroundLight)
            binding.stars5Button.setBackgroundResource(it.clickableRoundedBackgroundLight)

            binding.reviewSummaryTitle.setTextColor(it.text)
            binding.stars1.setTextColor(it.text)
            binding.stars2.setTextColor(it.text)
            binding.stars3.setTextColor(it.text)
            binding.stars4.setTextColor(it.text)
            binding.stars5.setTextColor(it.text)

            binding.stars1.compoundDrawableTintList = it.tint
            binding.stars2.compoundDrawableTintList = it.tint
            binding.stars3.compoundDrawableTintList = it.tint
            binding.stars4.compoundDrawableTintList = it.tint
            binding.stars5.compoundDrawableTintList = it.tint
        })
    }

    private fun observeReviews(group: Group) {
        groupDisposableGroup.clear()

        val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
        groupDisposableGroup.add(queryBuilder
                .sort(on<SortHandler>().sortGroupMessages())
                .equal(GroupMessage_.to, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .contains(GroupMessage_.attachment, "\"review\"", QueryBuilder.StringOrder.CASE_SENSITIVE)
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

        binding.stars1Button.setBackgroundResource(if (selectedRating == 1) on<LightDarkHandler>().get().clickableRoundedBackground else on<LightDarkHandler>().get().clickableRoundedBackgroundLight)
        binding.stars2Button.setBackgroundResource(if (selectedRating == 2) on<LightDarkHandler>().get().clickableRoundedBackground else on<LightDarkHandler>().get().clickableRoundedBackgroundLight)
        binding.stars3Button.setBackgroundResource(if (selectedRating == 3) on<LightDarkHandler>().get().clickableRoundedBackground else on<LightDarkHandler>().get().clickableRoundedBackgroundLight)
        binding.stars4Button.setBackgroundResource(if (selectedRating == 4) on<LightDarkHandler>().get().clickableRoundedBackground else on<LightDarkHandler>().get().clickableRoundedBackgroundLight)
        binding.stars5Button.setBackgroundResource(if (selectedRating == 5) on<LightDarkHandler>().get().clickableRoundedBackground else on<LightDarkHandler>().get().clickableRoundedBackgroundLight)
    }

    private fun refreshSummary(groupMessages: List<GroupMessage>) {
        val ratings = groupMessages.map { groupMessage ->
            on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
                    .get("review").asJsonObject
                    .get("rating").asInt
        }
        binding.stars1ProgressBar.progress = (ratings.count { rating -> rating == 1 }.toFloat() / ratings.size * 100).toInt()
        binding.stars2ProgressBar.progress = (ratings.count { rating -> rating == 2 }.toFloat() / ratings.size * 100).toInt()
        binding.stars3ProgressBar.progress = (ratings.count { rating -> rating == 3 }.toFloat() / ratings.size * 100).toInt()
        binding.stars4ProgressBar.progress = (ratings.count { rating -> rating == 4 }.toFloat() / ratings.size * 100).toInt()
        binding.stars5ProgressBar.progress = (ratings.count { rating -> rating == 5 }.toFloat() / ratings.size * 100).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }

}
