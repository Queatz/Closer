package closer.vlllage.com.closer.handler.feed

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class GroupPreviewAdapter(poolMember: PoolMember) : HeaderAdapter<GroupPreviewAdapter.ViewHolder>(poolMember), CombinedRecyclerAdapter.PrioritizedAdapter {

    var groups = mutableListOf<Group>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return field.size + HEADER_COUNT
                }

                override fun getNewListSize(): Int {
                    return value.size + HEADER_COUNT
                }

                override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    if (newPosition < HEADER_COUNT != oldPosition < HEADER_COUNT) {
                        return false
                    } else if (newPosition < HEADER_COUNT) {
                        return true
                    }

                    return field[oldPosition - 1].objectBoxId == value[newPosition - 1].objectBoxId
                }

                override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                    return if (newPosition < HEADER_COUNT != oldPosition < HEADER_COUNT) {
                        false
                    } else
                        newPosition < HEADER_COUNT

                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId: Int
        when (viewType) {
            1 -> layoutResId = R.layout.feed_item_public_groups
            else -> layoutResId = R.layout.group_preview_item
        }
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(layoutResId, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var position = position
        super.onBindViewHolder(holder, position)
        holder.pool = tempPool()

        if (position < HEADER_COUNT) {
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
        } else {
            holder.pool.`$set`(`$`(ApiHandler::class.java))
            holder.pool.`$set`(`$`(ApplicationHandler::class.java))
            holder.pool.`$set`(`$`(ActivityHandler::class.java))
            holder.pool.`$set`(`$`(ResourcesHandler::class.java))
            position--
        }

        val group = groups[position]
        holder.groupName.text = `$`(Val::class.java).of(group.name!!, `$`(ResourcesHandler::class.java).resources.getString(R.string.app_name))
        holder.groupName.setOnClickListener { view -> `$`(GroupActivityTransitionHandler::class.java).showGroupMessages(holder.groupName, group.id) }
        holder.groupName.setOnLongClickListener { view ->
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
        holder.messagesRecyclerView.layoutManager = LinearLayoutManager(holder.messagesRecyclerView.context, LinearLayoutManager.VERTICAL, true)

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

        holder.replyMessage.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                holder.sendButton.callOnClick()
            }

            false
        }

        holder.sendButton.setOnClickListener { view ->
            val message = holder.replyMessage.text.toString()

            if (message.trim { it <= ' ' }.isEmpty()) {
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

    override fun getItemViewType(position: Int): Int {
        when (position) {
            0 -> return 1
            else -> return 0
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.pool.end()
    }

    override fun getItemCount(): Int {
        return groups.size + HEADER_COUNT
    }

    override fun getItemPriority(position: Int): Int {
        return max(0, position - if (`$`(DistanceHandler::class.java).isUserNearGroup(groups[position - 1])) 100 else 0)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal lateinit var pool: TempPool

        internal var groupName: TextView
        internal var messagesRecyclerView: RecyclerView
        internal var pinnedMessagesRecyclerView: RecyclerView
        internal var sendButton: ImageButton
        internal var replyMessage: EditText
        internal var backgroundPhoto: ImageView
        internal var scopeIndicatorButton: ImageButton
        internal var mentionSuggestionsLayout: MaxSizeFrameLayout
        internal var mentionSuggestionRecyclerView: RecyclerView
        internal var backgroundColor: View

        internal var textWatcher: TextWatcher? = null

        init {
            groupName = itemView.findViewById(R.id.groupName)
            messagesRecyclerView = itemView.findViewById(R.id.messagesRecyclerView)
            pinnedMessagesRecyclerView = itemView.findViewById(R.id.pinnedMessagesRecyclerView)
            sendButton = itemView.findViewById(R.id.sendButton)
            replyMessage = itemView.findViewById(R.id.replyMessage)
            backgroundPhoto = itemView.findViewById(R.id.backgroundPhoto)
            scopeIndicatorButton = itemView.findViewById(R.id.scopeIndicatorButton)
            mentionSuggestionsLayout = itemView.findViewById(R.id.mentionSuggestionsLayout)
            mentionSuggestionRecyclerView = itemView.findViewById(R.id.mentionSuggestionRecyclerView)
            backgroundColor = itemView.findViewById(R.id.backgroundColor)
        }
    }

    companion object {
        private const val HEADER_COUNT = 1
    }
}
