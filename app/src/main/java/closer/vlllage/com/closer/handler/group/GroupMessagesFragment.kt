package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.call.CallHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import io.reactivex.android.schedulers.AndroidSchedulers
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
            callButton.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(
                    if (it.light) R.color.green else R.color.text
            ))
            callButton.setBackgroundResource(it.clickableRoundedBackground)
            replyMessage.setTextColor(it.text)
            replyMessage.setHintTextColor(it.hint)
            replyMessage.setBackgroundResource(it.clickableRoundedBackground)
        })

        on<GroupHandler> {
            onGroupChanged {
                callButton?.visible = it.direct

                if (it.direct) {
                    callButton?.setOnClickListener { _ ->
                        on<DirectGroupHandler>().getContactPhone(it.id!!).subscribe({
                            on<CallHandler>().startCall(it.id!!)
                        }, {
                            on<DefaultAlerts>().thatDidntWork()
                        })
                    }
                }
            }

            onGroupUpdated(disposableGroup) {
                on<PinnedMessagesHandler>().show(it)
            }
        }

        on<TypingHandler>().whoIsTyping.observeOn(AndroidSchedulers.mainThread()).subscribe {
            val me = on<PersistenceHandler>().phoneId

            it.filter { it != me }.let { typers ->
                typingIndicator.visible = typers.isNotEmpty()
                typingIndicator.text = typers.joinToString { on<NameHandler>().getName(it) }

                if (typers.isNotEmpty()) {
                    typingIndicator.apply { post { compoundDrawablesRelative.forEach { (it as? AnimationDrawable)?.start() } } }
                }
            }
        }.also {
            on<DisposableHandler>().add(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposableGroup.dispose()
    }
}