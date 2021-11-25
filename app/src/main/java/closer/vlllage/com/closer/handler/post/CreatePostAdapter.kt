package closer.vlllage.com.closer.handler.post

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.CreatePostItemBinding
import closer.vlllage.com.closer.databinding.CreatePostSectionHeaderBinding
import closer.vlllage.com.closer.databinding.CreatePostSectionTextBinding
import closer.vlllage.com.closer.databinding.CreatePostSelectGroupActionBinding
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.*
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.Group
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.queatz.on.On

open class CreatePostAdapter(protected val on: On, private val action: (CreatePostAction) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var group: Group? = null
    var groupName: String = on<ResourcesHandler>().resources.getString(R.string.app_name)
    set(value) {
        field = value
        notifyDataSetChanged()
    }

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
            CreatePostViewHolder(CreatePostItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)) as RecyclerView.ViewHolder

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        (viewHolder as? CreatePostViewHolder)?.let { holder ->
            holder.disposableGroup = on<DisposableHandler>().group()
            holder.on = On(on).apply {
                use<GroupMessageMentionHandler>() // They each need their own
                use<GroupActionDisplay>()
            }

            val item = items[position]

            holder.binding.actionAddHeading.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddHeading, position)) }
            holder.binding.actionAddText.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddText, position)) }
            holder.binding.actionAddPhoto.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddPhoto, position)) }
            holder.binding.actionAddActivity.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddGroupAction, position)) }
            holder.binding.actionDelete.setOnClickListener { action(CreatePostAction(CreatePostActionType.Delete, position)) }

            holder.binding.actionPhotoOptions.visible = item.attachment.has("photo")
            holder.binding.actionPhotoOptions.setOnClickListener { action(CreatePostAction(CreatePostActionType.EditPhoto, position) {
                render(holder, item)
            }) }

            holder.binding.postButton.visible = position == items.size - 1
            holder.binding.postButton.text = on<GroupActionDisplay>().postText(group, groupName)
            holder.binding.postButton.setOnClickListener { action(CreatePostAction(CreatePostActionType.Post, position)) }

            render(holder, item)

            on<LightDarkHandler>().onLightChanged.subscribe {
                holder.binding.actionAddHeading.setTextColor(it.text)
                holder.binding.actionAddText.setTextColor(it.text)
                holder.binding.actionAddPhoto.setTextColor(it.text)
                holder.binding.actionAddActivity.setTextColor(it.text)
                holder.binding.actionPhotoOptions.setTextColor(it.text)
                holder.binding.actionDelete.setTextColor(it.text)
                holder.binding.actionAddHeading.compoundDrawableTintList = it.tint
                holder.binding.actionAddText.compoundDrawableTintList = it.tint
                holder.binding.actionAddPhoto.compoundDrawableTintList = it.tint
                holder.binding.actionAddActivity.compoundDrawableTintList = it.tint
                holder.binding.actionPhotoOptions.compoundDrawableTintList = it.tint
                holder.binding.actionDelete.compoundDrawableTintList = it.tint
                holder.binding.actionAddHeading.setBackgroundResource(it.clickableRoundedBackground)
                holder.binding.actionAddText.setBackgroundResource(it.clickableRoundedBackground)
                holder.binding.actionAddPhoto.setBackgroundResource(it.clickableRoundedBackground)
                holder.binding.actionAddActivity.setBackgroundResource(it.clickableRoundedBackground)
                holder.binding.actionPhotoOptions.setBackgroundResource(it.clickableRoundedBackground)
                holder.binding.actionDelete.setBackgroundResource(it.clickableRoundedBackground)
            }.also { holder.disposableGroup.add(it) }
        }
    }

    private fun render(holder: CreatePostViewHolder, item: PostSection) {
        holder.binding.content.removeAllViews()

        when {
            item.attachment.has("header") -> {
                val view = CreatePostSectionHeaderBinding.inflate(LayoutInflater.from(holder.binding.content.context), holder.binding.content, false)
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

                holder.binding.content.addView(view.root)
            }
            item.attachment.has("text") -> {
                val view = CreatePostSectionTextBinding.inflate(LayoutInflater.from(holder.binding.content.context), holder.binding.content, false)
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

                holder.binding.content.addView(view.root)
            }
            item.attachment.has("photo") -> {
                on<MessageSections>().renderPhotoSection(item.attachment.get("photo").asJsonObject, holder.binding.content).subscribe { view ->
                    holder.binding.content.addView(view.root)
                }.also { holder.disposableGroup.add(it) }
            }
            item.attachment.has("activity") -> {
                val activity = item.attachment.get("activity")

                if (activity.isJsonObject) {
                    on<MessageSections>().renderGroupActionSection(item.attachment.get("activity").asJsonObject, holder.binding.content).subscribe { view ->
                        holder.binding.content.addView(view)
                    }.also { holder.disposableGroup.add(it) }
                } else {
                    val view = CreatePostSelectGroupActionBinding.inflate(LayoutInflater.from(holder.binding.content.context), holder.binding.content, false)

                    val adapter = GroupActionAdapter(holder.on, GroupActionDisplay.Layout.PHOTO) { it, _ ->
                        item.attachment.add("activity", on<JsonHandler>().toJsonTree(it))
                        notifyDataSetChanged()
                    }
                    view.actionRecyclerView.adapter = adapter
                    view.actionRecyclerView.layoutManager = LinearLayoutManager(holder.binding.content.context, RecyclerView.HORIZONTAL, false)

                    view.searchActivities.doOnTextChanged { text, _, _, _ ->
                        searchGroupActivities(holder, adapter, text.toString())
                    }

                    searchGroupActivities(holder, adapter, null)

                    holder.binding.content.addView(view.root)
                }
            }
            else -> {}
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

class CreatePostViewHolder(val binding: CreatePostItemBinding) : RecyclerView.ViewHolder(binding.root) {
    lateinit var disposableGroup: DisposableGroup
    lateinit var on: On
}
