package closer.vlllage.com.closer.handler.feed

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.HeaderAdapter
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.map.MapHandler
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.ui.CombinedRecyclerAdapter
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import java.lang.Math.max
import java.lang.Math.min
import java.util.*

class GroupPreviewAdapter(on: On) : HeaderAdapter<RecyclerView.ViewHolder>(on), CombinedRecyclerAdapter.PrioritizedAdapter {

    var groups = mutableListOf<Group>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = field.size + HEADER_COUNT
                override fun getNewListSize() = value.size + HEADER_COUNT

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    if (newPosition < HEADER_COUNT != oldPosition < HEADER_COUNT) {
                        return false
                    } else if (newPosition < HEADER_COUNT) {
                        return true
                    }

                    return field[oldPosition - 1].objectBoxId == value[newPosition - 1].objectBoxId
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    return if (newPosition < HEADER_COUNT != oldPosition < HEADER_COUNT)
                        false
                    else
                        newPosition < HEADER_COUNT
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> HeaderViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.feed_item_public_groups, parent, false))
            else -> ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_preview_item, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(viewHolder, position)

        if (position < HEADER_COUNT) {
            bindHeader(viewHolder as HeaderViewHolder)
            return
        }

        val holder = viewHolder as ViewHolder
        holder.on = On()
        holder.on.use(on<ApiHandler>())
        holder.on.use(on<ApplicationHandler>())
        holder.on.use(on<ActivityHandler>())
        holder.on.use(on<ResourcesHandler>())
        holder.on.use(on<StoreHandler>())

        val group = groups[position - HEADER_COUNT]
        holder.groupName.text = on<Val>().of(group.name, on<ResourcesHandler>().resources.getString(R.string.app_name))
        holder.groupName.setOnClickListener { on<GroupActivityTransitionHandler>().showGroupMessages(holder.groupName, group.id) }
        holder.groupName.setOnLongClickListener {
            on<GroupMemberHandler>().changeGroupSettings(group)
            true
        }

        val groupMessagesAdapter = GroupMessagesAdapter(on)
        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> on<MapActivityHandler>().showSuggestionOnMap(suggestion) }
        groupMessagesAdapter.onEventClickListener = { event -> on<GroupActivityTransitionHandler>().showGroupForEvent(holder.itemView, event) }
        groupMessagesAdapter.onGroupClickListener = { group1 -> on<GroupActivityTransitionHandler>().showGroupMessages(holder.itemView, group1.id) }

        val queryBuilder = on<StoreHandler>().store.box(GroupMessage::class).query()
        holder.on<DisposableHandler>().add(queryBuilder
                .sort(on<SortHandler>().sortGroupMessages())
                .equal(GroupMessage_.to, group.id!!)
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

        holder.replyMessage.setText(on<GroupMessageParseHandler>().parseText(on<GroupDraftHandler>().getDraft(group)!!))

        holder.textWatcher = object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && holder.on<GroupMessageParseHandler>().isMentionSelected(holder.replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                on<GroupDraftHandler>().saveDraft(group, text.toString())
                holder.on<GroupMessageMentionHandler>().showSuggestionsForName(holder.on<GroupMessageParseHandler>().extractName(text, holder.replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    holder.on<GroupMessageParseHandler>().deleteMention(holder.replyMessage)
                }
            }
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
            groupMessage.time = Date()
            on<StoreHandler>().store.box(GroupMessage::class).put(groupMessage)
            on<SyncHandler>().sync(groupMessage)

            holder.replyMessage.setText("")
            on<KeyboardHandler>().showKeyboard(view, false)
        }

        holder.backgroundColor.setBackgroundResource(on<GroupColorHandler>().getColorBackground(group))

        on<ImageHandler>().get().cancelRequest(holder.backgroundPhoto)
        if (group.photo != null) {
            holder.backgroundPhoto.visibility = View.VISIBLE
            holder.backgroundPhoto.setImageDrawable(null)
            on<PhotoLoader>().softLoad(group.photo!!, holder.backgroundPhoto)
        } else {
            holder.backgroundPhoto.visibility = View.GONE
        }

        on<GroupScopeHandler>().setup(group, holder.scopeIndicatorButton)
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> 1
        else -> 0
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is HeaderViewHolder -> holder.on.off()
            is ViewHolder -> holder.on.off()
        }
    }

    override fun getItemCount() = groups.size + HEADER_COUNT

    override fun getItemPriority(position: Int): Int {
        return max(0, position - if (on<DistanceHandler>().isPhoneNearGroup(groups[position - 1])) 100 else 0)
    }

    private fun bindHeader(holder: HeaderViewHolder) {
        holder.on = On()
        holder.on.use(on<StoreHandler>())
        holder.on.use(on<SyncHandler>())
        holder.on.use(on<MapHandler>())
        holder.on.use(on<ApplicationHandler>())
        holder.on.use(on<ActivityHandler>())
        holder.on.use(on<SortHandler>())
        holder.on.use(on<KeyboardHandler>())
        holder.on.use(on<GroupMemberHandler>())
        holder.on<PublicGroupFeedItemHandler>().attach(holder.itemView)
    }

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var on: On
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var on: On
        var groupName: TextView = itemView.findViewById(R.id.groupName)
        var messagesRecyclerView: RecyclerView = itemView.findViewById(R.id.messagesRecyclerView)
        var pinnedMessagesRecyclerView: RecyclerView = itemView.findViewById(R.id.pinnedMessagesRecyclerView)
        var sendButton: ImageButton = itemView.findViewById(R.id.sendButton)
        var replyMessage: EditText = itemView.findViewById(R.id.replyMessage)
        var backgroundPhoto: ImageView = itemView.findViewById(R.id.backgroundPhoto)
        var scopeIndicatorButton: ImageButton = itemView.findViewById(R.id.scopeIndicatorButton)
        var mentionSuggestionsLayout: MaxSizeFrameLayout = itemView.findViewById(R.id.mentionSuggestionsLayout)
        var mentionSuggestionRecyclerView: RecyclerView = itemView.findViewById(R.id.mentionSuggestionRecyclerView)
        var backgroundColor: View = itemView.findViewById(R.id.backgroundColor)
        var textWatcher: TextWatcher? = null
    }

    companion object {
        private const val HEADER_COUNT = 1
    }
}
