package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.databinding.FragmentGroupAboutBinding
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.feed.MixedHeaderAdapter
import closer.vlllage.com.closer.handler.helpers.ConnectionErrorHandler
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.quest.QuestDisplaySettings
import closer.vlllage.com.closer.pool.PoolActivityFragment
import com.queatz.on.On

class GroupAboutFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentGroupAboutBinding
    private lateinit var mixedAdapter: MixedHeaderAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var disposableGroup: DisposableGroup
    private lateinit var groupMessagesDisposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentGroupAboutBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        groupMessagesDisposableGroup = disposableGroup.group()

        layoutManager = LinearLayoutManager(
                binding.recyclerView.context,
                RecyclerView.VERTICAL,
                false
        )
        binding.recyclerView.layoutManager = layoutManager

        mixedAdapter = MixedHeaderAdapter(On(on).apply {
            use<GroupMessageHelper>()
        }, true)

        mixedAdapter.showFeedHeader = false
        mixedAdapter.useHeader = false

        binding.recyclerView.adapter = mixedAdapter

        on<QuestDisplaySettings>().isAbout = true

        on<GroupHandler>().onGroupChanged {
            if (it.ofKind == "quest") {
                setQuest(it.ofId!!)
            } else if (it.ofKind == "quest.progress") {
                on<DataHandler>().getQuestProgress(it.ofId!!).subscribe({
                    on<QuestDisplaySettings>().stageQuestProgressId = it.id
                    setQuest(it.questId!!)
                }, {
                    on<ConnectionErrorHandler>().notifyConnectionError()
                })
            }
        }
    }

    private fun setQuest(questId: String) {
        on<DataHandler>().getQuest(questId).subscribe({
            mixedAdapter.content = FeedContent.QUESTS
            mixedAdapter.quests = mutableListOf(it)
        }, {
            on<ConnectionErrorHandler>().notifyConnectionError()
        }).also {
            disposableGroup.add(it)
        }
    }
}
