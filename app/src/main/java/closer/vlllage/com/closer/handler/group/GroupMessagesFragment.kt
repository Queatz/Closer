package closer.vlllage.com.closer.handler.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableGroup
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import kotlinx.android.synthetic.main.fragment_group_messages.*

class GroupMessagesFragment : PoolActivityFragment() {

    private lateinit var disposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_group_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        on<GroupActionDisplay>().launchGroup = false
        on<GroupActionDisplay>().showGroupName = false
        on<GroupActionHandler>().attach(actionFrameLayout, actionRecyclerView)
        on<GroupMessagesHandler>().attach(messagesRecyclerView, replyMessage, sendButton, sendMoreButton, sendMoreLayout)
        on<PinnedMessagesHandler>().attach(pinnedMessagesRecyclerView)
        on<GroupMessageMentionHandler>().attach(mentionSuggestionsLayout, mentionSuggestionRecyclerView) {
            mention -> on<GroupMessagesHandler>().insertMention(mention)
        }

        messagesRecyclerView.dispatchTouchEventListener = { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> on<GroupActionHandler>().show(false)
                MotionEvent.ACTION_UP -> on<GroupActionHandler>().show(true)
                MotionEvent.ACTION_CANCEL -> on<GroupActionHandler>().show(true)
            }
        }

        disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            sendButton.imageTintList = it.tint
            sendButton.setBackgroundResource(it.clickableRoundedBackground)
            replyMessage.setTextColor(it.text)
            replyMessage.setHintTextColor(it.hint)
            replyMessage.setBackgroundResource(it.clickableRoundedBackground)
        })

        on<GroupHandler> {
            onGroupUpdated(disposableGroup) {
                on<PinnedMessagesHandler>().show(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}