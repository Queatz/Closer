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
import closer.vlllage.com.closer.pool.Pool.Companion.tempPool
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.pool.TempPool
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupMessage
import closer.vlllage.com.closer.store.models.GroupMessage_
import closer.vlllage.com.closer.ui.CombinedRecyclerAdapter
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import io.objectbox.android.AndroidScheduler
import java.lang.Math.max
import java.lang.Math.min
import java.util.*

class GroupPreviewAdapter(poolMember: PoolMember) : HeaderAdapter<RecyclerView.ViewHolder>(poolMember), CombinedRecyclerAdapter.PrioritizedAdapter {

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
        var position = position
        super.onBindViewHolder(viewHolder, position)

        if (position < HEADER_COUNT) {
            val holder = viewHolder as HeaderViewHolder
            holder.pool = tempPool()
            holder.pool.`$set`(`$`(StoreHandler::class.java))
            holder.pool.`$set`(`$`(SyncHandler::class.java))
            holder.pool.`$set`(`$`(MapHandler::class.java))
            holder.pool.`$set`(`$`(ApplicationHandler::class.java))
            holder.pool.`$set`(`$`(ActivityHandler::class.java))
            holder.pool.`$set`(`$`(SortHandler::class.java))
            holder.pool.`$set`(`$`(KeyboardHandler::class.java))
            holder.pool.`$set`(`$`(GroupMemberHandler::class.java))
            holder.pool.`$`(PublicGroupFeedItemHandler::class.java).attach(holder.itemView)
            return
        }
        val holder = viewHolder as ViewHolder
        holder.pool = tempPool()
        holder.pool.`$set`(`$`(ApiHandler::class.java))
        holder.pool.`$set`(`$`(ApplicationHandler::class.java))
        holder.pool.`$set`(`$`(ActivityHandler::class.java))
        holder.pool.`$set`(`$`(ResourcesHandler::class.java))
        position--

        val group = groups[position]
        holder.groupName.text = `$`(Val::class.java).of(group.name, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name))
        holder.groupName.setOnClickListener { `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(holder.groupName, group.id) }
        holder.groupName.setOnLongClickListener {
            `$`(GroupMemberHandler::class.java).changeGroupSettings(group)
            true
        }

        val groupMessagesAdapter = GroupMessagesAdapter(`$pool`())
        groupMessagesAdapter.onSuggestionClickListener = { suggestion -> `$`(MapActivityHandler::class.java).showSuggestionOnMap(suggestion) }
        groupMessagesAdapter.onEventClickListener = { event -> `$`(GroupActivityTransitionHandler::class.java).showGroupForEvent(holder.itemView, event) }
        groupMessagesAdapter.onGroupClickListener = { group1 -> `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(holder.itemView, group1.id) }

        val queryBuilder = `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).query()
        holder.pool.`$`(DisposableHandler::class.java).add(queryBuilder
                .sort(`$`(SortHandler::class.java).sortGroupMessages())
                .equal(GroupMessage_.to, group.id!!)
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .transform<List<GroupMessage>> { groupMessages -> groupMessages.subList(0, min(groupMessages.size, 5)) }
                .observer { groupMessagesAdapter.setGroupMessages(it) })

        holder.pool.`$`(PinnedMessagesHandler::class.java).attach(holder.pinnedMessagesRecyclerView)
        holder.pool.`$`(PinnedMessagesHandler::class.java).show(group)

        holder.messagesRecyclerView.adapter = groupMessagesAdapter
        holder.messagesRecyclerView.layoutManager = LinearLayoutManager(holder.messagesRecyclerView.context, RecyclerView.VERTICAL, true)

        if (holder.textWatcher != null) {
            holder.replyMessage.removeTextChangedListener(holder.textWatcher)
        }

        holder.replyMessage.setText(`$`(GroupMessageParseHandler::class.java).parseText(`$`(GroupDraftHandler::class.java).getDraft(group)!!))

        holder.textWatcher = object : TextWatcher {

            private var isDeleteMention: Boolean = false
            private var shouldDeleteMention: Boolean = false

            override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                shouldDeleteMention = !isDeleteMention && after == 0 && holder.pool.`$`(GroupMessageParseHandler::class.java).isMentionSelected(holder.replyMessage)
                isDeleteMention = false
            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable) {
                `$`(GroupDraftHandler::class.java).saveDraft(group, text.toString())
                holder.pool.`$`(GroupMessageMentionHandler::class.java).showSuggestionsForName(holder.pool.`$`(GroupMessageParseHandler::class.java).extractName(text, holder.replyMessage.selectionStart))

                if (shouldDeleteMention) {
                    isDeleteMention = true
                    holder.pool.`$`(GroupMessageParseHandler::class.java).deleteMention(holder.replyMessage)
                }
            }
        }

        holder.replyMessage.addTextChangedListener(holder.textWatcher)

        holder.pool.`$`(GroupMessageMentionHandler::class.java).attach(holder.mentionSuggestionsLayout, holder.mentionSuggestionRecyclerView, { mention -> holder.pool.`$`(GroupMessageParseHandler::class.java).insertMention(holder.replyMessage, mention) })

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
            groupMessage.from = `$`(PersistenceHandler::class.java).phoneId
            groupMessage.to = group.id
            groupMessage.time = Date()
            `$`(StoreHandler::class.java).store.box(GroupMessage::class.java).put(groupMessage)
            `$`(SyncHandler::class.java).sync(groupMessage)

            holder.replyMessage.setText("")
            `$`(KeyboardHandler::class.java).showKeyboard(view, false)
        }

        holder.backgroundColor.setBackgroundResource(`$`(GroupColorHandler::class.java).getColorBackground(group))

        `$`(ImageHandler::class.java).get().cancelRequest(holder.backgroundPhoto)
        if (group.photo != null) {
            holder.backgroundPhoto.visibility = View.VISIBLE
            holder.backgroundPhoto.setImageDrawable(null)
            `$`(PhotoLoader::class.java).softLoad(group.photo!!, holder.backgroundPhoto)
        } else {
            holder.backgroundPhoto.visibility = View.GONE
        }

        `$`(GroupScopeHandler::class.java).setup(group, holder.scopeIndicatorButton)
    }

    override fun getItemViewType(position: Int) = when (position) {
        0 -> 1
        else -> 0
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is HeaderViewHolder -> holder.pool.end()
            is ViewHolder -> holder.pool.end()
        }
    }

    override fun getItemCount() = groups.size + HEADER_COUNT

    override fun getItemPriority(position: Int): Int {
        return max(0, position - if (`$`(DistanceHandler::class.java).isUserNearGroup(groups[position - 1])) 100 else 0)
    }

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var pool: TempPool
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var pool: TempPool
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
