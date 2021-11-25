package closer.vlllage.com.closer.handler.feed.content

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
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
import kotlinx.android.synthetic.main.group_preview_item.view.*
import java.util.*
import kotlin.math.min

class GroupPreviewMixedItem(val group: Group) : MixedItem(MixedItemType.GroupPreview)

class GroupPreviewViewHolder(itemView: View) : MixedItemViewHolder(itemView, MixedItemType.GroupPreview) {
    lateinit var on: On
    var groupName = itemView.groupName!!
    var messagesRecyclerView = itemView.messagesRecyclerView!!
    var pinnedMessagesRecyclerView = itemView.pinnedMessagesRecyclerView!!
    var sendButton = itemView.sendButton!!
    var replyMessage = itemView.replyMessage!!
    var backgroundPhoto = itemView.backgroundPhoto!!
    var scopeIndicatorButton = itemView.scopeIndicatorButton!!
    var goToGroup = itemView.goToGroup!!
    var mentionSuggestionsLayout = itemView.mentionSuggestionsLayout!!
    var mentionSuggestionRecyclerView = itemView.mentionSuggestionRecyclerView!!
    var backgroundColor = itemView.backgroundColor!!
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

    override fun onCreateViewHolder(parent: ViewGroup) = GroupPreviewViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.group_preview_item, parent, false))

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

        holder.groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.talk_here))
        holder.groupName.setOnClickListener { on<GroupActivityTransitionHandler>().showGroupMessages(holder.groupName, group.id) }
        holder.groupName.setOnLongClickListener {
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

        holder.on<PinnedMessagesHandler>().attach(holder.pinnedMessagesRecyclerView)
        holder.on<PinnedMessagesHandler>().show(group)

        holder.messagesRecyclerView.adapter = groupMessagesAdapter
        holder.messagesRecyclerView.layoutManager = LinearLayoutManager(holder.messagesRecyclerView.context, RecyclerView.VERTICAL, true)

        if (holder.textWatcher != null) {
            holder.replyMessage.removeTextChangedListener(holder.textWatcher)
        }

        holder.replyMessage.setText(on<GroupMessageParseHandler>().parseText(holder.replyMessage, on<GroupDraftHandler>().getDraft(group.id!!)?.message ?: ""))

        holder.textWatcher = object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && holder.on<GroupMessageParseHandler>().isMentionSelected(holder.replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                on<GroupDraftHandler>().saveDraft(group.id!!, text.toString())
                holder.on<GroupMessageMentionHandler>().showSuggestionsForName(holder.on<GroupMessageParseHandler>().extractName(text, holder.replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    holder.on<GroupMessageParseHandler>().deleteMention(holder.replyMessage)
                }
            }
        }

        if (group.name.isNullOrBlank()) {
            holder.replyMessage.hint = on<ResourcesHandler>().resources.getString(R.string.say_something)
        } else {
            holder.replyMessage.hint = on<ResourcesHandler>().resources.getString(R.string.say_something_in, group.name)
        }

        holder.replyMessage.addTextChangedListener(holder.textWatcher)

        holder.on<GroupMessageMentionHandler>().attach(holder.mentionSuggestionsLayout, holder.mentionSuggestionRecyclerView) {
            mention -> holder.on<GroupMessageParseHandler>().insertMention(holder.replyMessage, mention)
        }

        holder.replyMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                holder.sendButton.callOnClick()
            }

            false
        }

        holder.sendButton.setOnClickListener { view ->
            val message = holder.replyMessage.text.toString()

            if (message.isBlank()) {
                return@setOnClickListener
            }

            val groupMessage = GroupMessage()
            groupMessage.text = message
            groupMessage.from = on<PersistenceHandler>().phoneId
            groupMessage.to = group.id
            groupMessage.created = Date()
            on<SyncHandler>().sync(groupMessage)

            holder.replyMessage.setText("")
            on<KeyboardHandler>().showKeyboard(view, false)
        }

        on<ImageHandler>().get().clear(holder.backgroundPhoto)
        if (group.photo != null) {
            holder.backgroundPhoto.visible = true
            holder.backgroundPhoto.setImageDrawable(null)
            on<PhotoLoader>().softLoad(group.photo!!, holder.backgroundPhoto)
        } else {
            holder.backgroundPhoto.visible = false
        }

        on<GroupScopeHandler>().setup(group, holder.scopeIndicatorButton)

        if (group.hasPhone()) {
            holder.on<LightDarkHandler>().setLight(true)
        } else {
            holder.on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.observeOn(AndroidSchedulers.mainThread()).subscribe { holder.on<LightDarkHandler>().setLight(it.light) })
        }

        holder.on<DisposableHandler>().add(holder.on<LightDarkHandler>().onLightChanged
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    holder.groupName.setTextColor(it.text)
                    holder.sendButton.imageTintList = it.tint
                    holder.sendButton.setBackgroundResource(it.clickableRoundedBackground)
                    holder.replyMessage.setTextColor(it.text)
                    holder.replyMessage.setHintTextColor(it.hint)
                    holder.replyMessage.setBackgroundResource(it.clickableRoundedBackground)
                    holder.scopeIndicatorButton.imageTintList = it.tint
                    holder.goToGroup.imageTintList = it.tint

                    if (it.light) {
                        holder.backgroundPhoto.alpha = .15f
                        holder.itemView.setBackgroundResource(R.color.offwhite)
                        holder.backgroundColor.setBackgroundResource(R.drawable.color_white_rounded)
                    } else {
                        holder.backgroundPhoto.alpha = 1f
                        holder.itemView.setBackgroundResource(R.color.white)
                        holder.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))
                    }

                    holder.groupName.setBackgroundResource(it.clickableRoundedBackground8dp)
                })
    }

}