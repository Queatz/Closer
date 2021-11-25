package closer.vlllage.com.closer.handler.group

import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.FragmentGroupMessagesBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.call.CallHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.pool.PoolActivityFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class GroupMessagesFragment : PoolActivityFragment() {

    private lateinit var binding: FragmentGroupMessagesBinding
    private lateinit var disposableGroup: DisposableGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentGroupMessagesBinding.inflate(inflater, container, false).let {
            binding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposableGroup = on<DisposableHandler>().group()
        on<GroupActionDisplay>().launchGroup = false
        on<GroupActionDisplay>().showGroupName = false
        on<GroupActionHandler>().attach(binding.actionFrameLayout, binding.actionRecyclerView)
        on<GroupMessagesHandler>().attach(binding.messagesRecyclerView, binding.replyMessage, binding.sendButton, binding.sendMoreButton, binding.sendMoreLayout)
        on<PinnedMessagesHandler>().attach(binding.pinnedMessagesRecyclerView)
        on<GroupMessageMentionHandler>().attach(binding.mentionSuggestionsLayout, binding.mentionSuggestionRecyclerView) {
            mention -> on<GroupMessagesHandler>().insertMention(mention)
        }

        binding.messagesRecyclerView.dispatchTouchEventListener = { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> on<GroupActionHandler>().show(false)
                MotionEvent.ACTION_UP -> on<GroupActionHandler>().show(true)
                MotionEvent.ACTION_CANCEL -> on<GroupActionHandler>().show(true)
            }
        }

        disposableGroup.add(on<LightDarkHandler>().onLightChanged.subscribe {
            binding.sendButton.imageTintList = it.tint
            binding.sendButton.setBackgroundResource(it.clickableRoundedBackground)
            binding.callButton.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(
                    if (it.light) R.color.green else R.color.text
            ))
            binding.callButton.setBackgroundResource(it.clickableRoundedBackground)
            binding.replyMessage.setTextColor(it.text)
            binding.replyMessage.setHintTextColor(it.hint)
            binding.replyMessage.setBackgroundResource(it.clickableRoundedBackground)
        })

        on<GroupHandler> {
            onGroupChanged {
                binding.callButton?.visible = it.direct

                if (it.direct) {
                    binding.callButton?.setOnClickListener { _ ->
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
                binding.typingIndicator.visible = typers.isNotEmpty()
                binding.typingIndicator.text = typers.joinToString { on<NameHandler>().getName(it) }

                if (typers.isNotEmpty()) {
                    binding.typingIndicator.apply { post { compoundDrawablesRelative.forEach { (it as? AnimationDrawable)?.start() } } }
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