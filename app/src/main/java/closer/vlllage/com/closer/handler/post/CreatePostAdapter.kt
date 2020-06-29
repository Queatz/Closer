package closer.vlllage.com.closer.handler.post

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.queatz.on.On
import kotlinx.android.synthetic.main.create_post_item.view.*
import kotlinx.android.synthetic.main.create_post_section_header.view.input
import kotlinx.android.synthetic.main.create_post_section_text.view.*
import kotlinx.android.synthetic.main.create_post_select_group_action.view.*

open class CreatePostAdapter(protected val on: On, private val action: (CreatePostAction) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var groupName: String = on<ResourcesHandler>().resources.getString(R.string.app_name)

    var items: List<PostSection> = listOf()
        set(value) {
//            val diffResult  = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
//                override fun getOldListSize() = field.size
//                override fun getNewListSize() = value.size
//
//                override fun areItemsTheSame(oldPosition: Int, newPosition: Int) =
//                    field[oldPosition].tempId == value[newPosition].tempId
//
//                override fun areContentsTheSame(oldPosition: Int, newPosition: Int) = false
//            })
            field = value
//            diffResult.dispatchUpdatesTo(this)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) =
            CreatePostViewHolder(LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.create_post_item, viewGroup, false)) as RecyclerView.ViewHolder

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as? CreatePostViewHolder)?.let { holder ->
            holder.disposableGroup = on<DisposableHandler>().group()
            holder.on = On(on).apply {
                use<GroupMessageMentionHandler>() // They each need their own
                use<GroupActionDisplay>()
            }

            val item = items[position]

            with(holder.itemView) {
                actionAddHeading.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddHeading, position)) }
                actionAddText.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddText, position)) }
                actionAddPhoto.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddPhoto, position)) }
                actionAddActivity.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddGroupAction, position)) }
                actionDelete.setOnClickListener { action(CreatePostAction(CreatePostActionType.Delete, position)) }

                actionPhotoOptions.visible = item.attachment.has("photo")
                actionPhotoOptions.setOnClickListener { action(CreatePostAction(CreatePostActionType.EditPhoto, position) {
                    render(holder, item)
                }) }

                postButton.visible = position == items.size - 1
                postButton.text = on<ResourcesHandler>().resources.getString(R.string.post_in, groupName)
                postButton.setOnClickListener { action(CreatePostAction(CreatePostActionType.Post, position)) }
            }

            render(holder, item)

            on<LightDarkHandler>().onLightChanged.subscribe {
                with(holder.itemView) {
                    actionAddHeading.setTextColor(it.text)
                    actionAddText.setTextColor(it.text)
                    actionAddPhoto.setTextColor(it.text)
                    actionAddActivity.setTextColor(it.text)
                    actionPhotoOptions.setTextColor(it.text)
                    actionDelete.setTextColor(it.text)
                    actionAddHeading.compoundDrawableTintList = it.tint
                    actionAddText.compoundDrawableTintList = it.tint
                    actionAddPhoto.compoundDrawableTintList = it.tint
                    actionAddActivity.compoundDrawableTintList = it.tint
                    actionPhotoOptions.compoundDrawableTintList = it.tint
                    actionDelete.compoundDrawableTintList = it.tint
                    actionAddHeading.setBackgroundResource(it.clickableRoundedBackground)
                    actionAddText.setBackgroundResource(it.clickableRoundedBackground)
                    actionAddPhoto.setBackgroundResource(it.clickableRoundedBackground)
                    actionAddActivity.setBackgroundResource(it.clickableRoundedBackground)
                    actionPhotoOptions.setBackgroundResource(it.clickableRoundedBackground)
                    actionDelete.setBackgroundResource(it.clickableRoundedBackground)
                }
            }.also { holder.disposableGroup.add(it) }
        }
    }

    private fun render(holder: CreatePostViewHolder, item: PostSection) {
        with(holder.itemView) {
            content.removeAllViews()

            when {
                item.attachment.has("header") -> {
                    val view = LayoutInflater.from(context).inflate(R.layout.create_post_section_header, content, false)
                    view.input.setText(holder.on<GroupMessageParseHandler>().parseText(view.input, item.attachment.get("header").asJsonObject.get("text").asString))

                    view.input.addTextChangedListener(object : TextWatcher {

                        private var isDeleteMention: Boolean = false
                        private var shouldDeleteMention: Boolean = false

                        override fun afterTextChanged(text: Editable) {
                            holder.on<GroupMessageMentionHandler>().showSuggestionsForName(holder.on<GroupMessageParseHandler>().extractName(text, view.input.selectionStart))

                            if (shouldDeleteMention) {
                                isDeleteMention = true
                                holder.on<GroupMessageParseHandler>().deleteMention(view.input)
                            }
                        }

                        override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                            shouldDeleteMention = !isDeleteMention && after == 0 && holder.on<GroupMessageParseHandler>().isMentionSelected(view.input)
                            isDeleteMention = false
                        }

                        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                            item.attachment.get("header").asJsonObject.add("text", JsonPrimitive(text.toString()))
                        }
                    })

                    holder.on<GroupMessageMentionHandler>().attach(view.mentionSuggestionsLayout, view.mentionSuggestionRecyclerView) {
                        mention -> holder.on<GroupMessageParseHandler>().insertMention(view.input, mention)
                    }

                    content.addView(view)
                }
                item.attachment.has("text") -> {
                    val view = LayoutInflater.from(context).inflate(R.layout.create_post_section_text, content, false)
                    view.input.setText(holder.on<GroupMessageParseHandler>().parseText(view.input, item.attachment.get("text").asJsonObject.get("text").asString))
                    view.input.addTextChangedListener(object : TextWatcher {

                        private var isDeleteMention: Boolean = false
                        private var shouldDeleteMention: Boolean = false

                        override fun afterTextChanged(text: Editable) {
                            holder.on<GroupMessageMentionHandler>().showSuggestionsForName(holder.on<GroupMessageParseHandler>().extractName(text, view.input.selectionStart))

                            if (shouldDeleteMention) {
                                isDeleteMention = true
                                holder.on<GroupMessageParseHandler>().deleteMention(view.input)
                            }
                        }

                        override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
                            shouldDeleteMention = !isDeleteMention && after == 0 && holder.on<GroupMessageParseHandler>().isMentionSelected(view.input)
                            isDeleteMention = false
                        }

                        override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                            item.attachment.get("text").asJsonObject.add("text", JsonPrimitive(text.toString()))
                        }
                    })

                    holder.on<GroupMessageMentionHandler>().attach(view.mentionSuggestionsLayout, view.mentionSuggestionRecyclerView) {
                        mention -> holder.on<GroupMessageParseHandler>().insertMention(view.input, mention)
                    }

                    content.addView(view)
                }
                item.attachment.has("photo") -> {
                    on<MessageSections>().renderPhotoSection(item.attachment.get("photo").asJsonObject, content).subscribe { view ->
                        content.addView(view)
                    }.also { holder.disposableGroup.add(it) }
                }
                item.attachment.has("activity") -> {
                    val activity = item.attachment.get("activity")

                    if (activity.isJsonObject) {
                        on<MessageSections>().renderGroupActionSection(item.attachment.get("activity").asJsonObject, content).subscribe { view ->
                            content.addView(view)
                        }.also { holder.disposableGroup.add(it) }
                    } else {
                        val view = LayoutInflater.from(context).inflate(R.layout.create_post_select_group_action, content, false)

                        val adapter = GroupActionAdapter(holder.on, GroupActionDisplay.Layout.PHOTO) { it, _ ->
                            item.attachment.add("activity", on<JsonHandler>().toJsonTree(it))
                            notifyDataSetChanged()
                        }
                        view.actionRecyclerView.adapter = adapter
                        view.actionRecyclerView.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)

                        view.searchActivities.doOnTextChanged { text, _, _, _ ->
                            searchGroupActivities(holder, adapter, text.toString())
                        }

                        searchGroupActivities(holder, adapter, null)

                        content.addView(view)
                    }
                }
                else -> {}
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? CreatePostViewHolder)?.apply {
            disposableGroup.clear()
            on.off()
        }
    }

    override fun getItemCount() = items.size

    private fun searchGroupActivities(holder: CreatePostViewHolder, adapter: GroupActionAdapter, queryString: String?) {
        holder.disposableGroup.clear()

        on<Search>().groupActions(queryString = queryString) { groupActions ->
            adapter.setGroupActions(groupActions)
        }.also { holder.disposableGroup.add(it) }
    }
}

data class PostSection constructor(
    var tempId: String,
    var attachment: JsonObject
)

class CreatePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var disposableGroup: DisposableGroup
    lateinit var on: On
}
