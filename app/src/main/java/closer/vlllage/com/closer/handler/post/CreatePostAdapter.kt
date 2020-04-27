package closer.vlllage.com.closer.handler.post

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.handler.group.MessageSections
import closer.vlllage.com.closer.handler.helpers.*
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.queatz.on.On
import kotlinx.android.synthetic.main.create_post_item.view.*
import kotlinx.android.synthetic.main.create_post_section_header.view.*

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

            val item = items[position]

            with(holder.itemView) {
                actionAddHeading.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddHeading, position)) }
                actionAddText.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddText, position)) }
                actionAddPhoto.setOnClickListener { action(CreatePostAction(CreatePostActionType.AddPhoto, position)) }
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
                    actionPhotoOptions.setTextColor(it.text)
                    actionDelete.setTextColor(it.text)
                    actionAddHeading.compoundDrawableTintList = it.tint
                    actionAddText.compoundDrawableTintList = it.tint
                    actionAddPhoto.compoundDrawableTintList = it.tint
                    actionPhotoOptions.compoundDrawableTintList = it.tint
                    actionDelete.compoundDrawableTintList = it.tint
                    actionAddHeading.setBackgroundResource(it.clickableRoundedBackground)
                    actionAddText.setBackgroundResource(it.clickableRoundedBackground)
                    actionAddPhoto.setBackgroundResource(it.clickableRoundedBackground)
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
                    view.input.setText(item.attachment.get("header").asJsonObject.get("text").asString)
                    view.input.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable) {}

                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            item.attachment.get("header").asJsonObject.add("text", JsonPrimitive(s.toString()))
                        }
                    })
                    content.addView(view)
                }
                item.attachment.has("text") -> {
                    val view = LayoutInflater.from(context).inflate(R.layout.create_post_section_text, content, false)
                    view.input.setText(item.attachment.get("text").asJsonObject.get("text").asString)
                    view.input.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(s: Editable) {}

                        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                            item.attachment.get("text").asJsonObject.add("text", JsonPrimitive(s.toString()))
                        }
                    })
                    content.addView(view)
                }
                item.attachment.has("photo") -> {
                    on<MessageSections>().renderPhotoSection(item.attachment.get("photo").asJsonObject, content).subscribe { view ->
                        content.addView(view)
                    }
                }
                item.attachment.has("activity") -> {

                }
                else -> {}
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        (holder as? CreatePostViewHolder)?.apply { disposableGroup.clear() }
    }

    override fun getItemCount() = items.size
}

data class PostSection constructor(
    var tempId: String,
    var attachment: JsonObject
)

class CreatePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    lateinit var disposableGroup: DisposableGroup
}
