package closer.vlllage.com.closer.handler.feed.content

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.GroupPreviewItemBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.query.QueryBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.*
import kotlin.math.min

class GroupPreviewMixedItem(val group: Group) : MixedItem(MixedItemType.GroupPreview)

class GroupPreviewViewHolder(val binding: GroupPreviewItemBinding) : MixedItemViewHolder(binding.root, MixedItemType.GroupPreview) {
    lateinit var on: On
    var textWatcher: TextWatcher? = null
}

class GroupPreviewMixedItemAdapter(private val on: On) : MixedItemAdapter<GroupPreviewMixedItem, GroupPreviewViewHolder> {
    override fun bind(holder: GroupPreviewViewHolder, item: GroupPreviewMixedItem, position: Int) {
        bindGroupPreview(holder, item.group)
    }

    override fun getMixedItemClass() = GroupPreviewMixedItem::class
    override fun getMixedItemType() = MixedItemType.GroupPreview

    override fun areItemsTheSame(old: GroupPreviewMixedItem, new: GroupPreviewMixedItem) = old.group.objectBoxId == new.group.objectBoxId

    override fun areContentsTheSame(old: GroupPreviewMixedItem, new: GroupPreviewMixedItem) = false

    override fun onCreateViewHolder(parent: ViewGroup) = GroupPreviewViewHolder(GroupPreviewItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onViewRecycled(holder: GroupPreviewViewHolder) {
        holder.on.off()
    }

    private fun bindGroupPreview(holder: GroupPreviewViewHolder, group: Group) {
        holder.on = On(on).apply {
            use<DisposableHandler>()
            use<LightDarkHandler>()
            use<GroupMessageHelper> {
                onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
                onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(holder.itemView, event) }
                onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(holder.itemView, group1.id) }
            }
        }

        holder.binding.groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.talk_here))
        holder.binding.groupName.setOnClickListener { on<GroupActivityTransitionHandler>().showGroupMessages(holder.binding.groupName, group.id) }
        holder.binding.groupName.setOnLongClickListener {
            on<GroupMemberHandler>().changeGroupSettings(group)
            true
        }

        val groupMessagesAdapter = GroupMessagesAdapter(holder.on)

        val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
        holder.on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortGroupMessages())
                .equal(GroupMessage_.to, group.id!!, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .transform<List<GroupMessage>> { groupMessages -> groupMessages.subList(0, min(groupMessages.size, 5)) }
                .observer { groupMessagesAdapter.setGroupMessages(it) })

        holder.on<PinnedMessagesHandler>().attach(holder.binding.pinnedMessagesRecyclerView)
        holder.on<PinnedMessagesHandler>().show(group)

        holder.binding.messagesRecyclerView.adapter = groupMessagesAdapter
        holder.binding.messagesRecyclerView.layoutManager = LinearLayoutManager(holder.binding.messagesRecyclerView.context, RecyclerView.VERTICAL, true)

        if (holder.textWatcher != null) {
            holder.binding.replyMessage.removeTextChangedListener(holder.textWatcher)
        }

        holder.binding.replyMessage.setText(on<GroupMessageParseHandler>().parseText(holder.binding.replyMessage, on<GroupDraftHandler>().getDraft(group.id!!)?.message ?: ""))

        holder.textWatcher = object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && holder.on<GroupMessageParseHandler>().isMentionSelected(holder.binding.replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                on<GroupDraftHandler>().saveDraft(group.id!!, text.toString())
                holder.on<GroupMessageMentionHandler>().showSuggestionsForName(holder.on<GroupMessageParseHandler>().extractName(text, holder.binding.replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    holder.on<GroupMessageParseHandler>().deleteMention(holder.binding.replyMessage)
                }
            }
        }

        if (group.name.isNullOrBlank()) {
            holder.binding.replyMessage.hint = on<ResourcesHandler>().resources.getString(R.string.say_something)
        } else {
            holder.binding.replyMessage.hint = on<ResourcesHandler>().resources.getString(R.string.say_something_in, group.name)
        }

        holder.binding.replyMessage.addTextChangedListener(holder.textWatcher)

        holder.on<GroupMessageMentionHandler>().attach(holder.binding.mentionSuggestionsLayout, holder.binding.mentionSuggestionRecyclerView) {
            mention -> holder.on<GroupMessageParseHandler>().insertMention(holder.binding.replyMessage, mention)
        }

        holder.binding.replyMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                holder.binding.sendButton.callOnClick()
            }

            false
        }

        holder.binding.sendButton.setOnClickListener { view ->
            val message = holder.binding.replyMessage.text.toString()

            if (message.isBlank()) {
                return@setOnClickListener
            }

            val groupMessage = GroupMessage()
            groupMessage.text = message
            groupMessage.from = on<PersistenceHandler>().phoneId
            groupMessage.to = group.id
            groupMessage.created = Date()
            on<SyncHandler>().sync(groupMessage)

            holder.binding.replyMessage.setText("")
            on<KeyboardHandler>().showKeyboard(view, false)
        }

        on<ImageHandler>().get().clear(holder.binding.backgroundPhoto)
        if (group.photo != null) {
            holder.binding.backgroundPhoto.visible = true
            holder.binding.backgroundPhoto.setImageDrawable(null)
            on<PhotoLoader>().softLoad(group.photo!!, holder.binding.backgroundPhoto)
        } else {
            holder.binding.backgroundPhoto.visible = false
        }

        on<GroupScopeHandler>().setup(group, holder.binding.scopeIndicatorButton)

        if (group.hasPhone()) {
            holder.on<LightDarkHandler>().setLight(true)
        } else {
            holder.on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.observeOn(AndroidSchedulers.mainThread()).subscribe { holder.on<LightDarkHandler>().setLight(it.light) })
        }

        holder.on<DisposableHandler>().add(holder.on<LightDarkHandler>().onLightChanged
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    holder.binding.groupName.setTextColor(it.text)
                    holder.binding.sendButton.imageTintList = it.tint
                    holder.binding.sendButton.setBackgroundResource(it.clickableRoundedBackground)
                    holder.binding.replyMessage.setTextColor(it.text)
                    holder.binding.replyMessage.setHintTextColor(it.hint)
                    holder.binding.replyMessage.setBackgroundResource(it.clickableRoundedBackground)
                    holder.binding.scopeIndicatorButton.imageTintList = it.tint
                    holder.binding.goToGroup.imageTintList = it.tint

                    if (it.light) {
                        holder.binding.backgroundPhoto.alpha = .15f
                        holder.itemView.setBackgroundResource(R.color.offwhite)
                        holder.binding.backgroundColor.setBackgroundResource(R.drawable.color_white_rounded)
                    } else {
                        holder.binding.backgroundPhoto.alpha = 1f
                        holder.itemView.setBackgroundResource(R.color.white)
                        holder.binding.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
                    }

                    holder.binding.groupName.setBackgroundResource(it.clickableRoundedBackground8dp)
                })
    }

}