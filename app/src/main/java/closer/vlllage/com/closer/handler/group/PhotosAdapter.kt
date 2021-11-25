package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.PhotoItemBinding
import closer.vlllage.com.closer.handler.helpers.JsonHandler
import closer.vlllage.com.closer.handler.helpers.MenuHandler
import closer.vlllage.com.closer.handler.helpers.PhotoLoader
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupMessage
import com.google.gson.JsonObject
import com.queatz.on.On

class PhotosAdapter(on: On) : PoolRecyclerAdapter<PhotoViewHolder>(on) {

    var items = listOf<GroupMessage>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(PhotoItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val groupMessage = items[position]

        val jsonObject = on<JsonHandler>().from(groupMessage.attachment!!, JsonObject::class.java)
        val photo = jsonObject.get("photo").asString + "?s=500"

        holder.binding.photo.setImageDrawable(null)
        on<PhotoLoader>().softLoad(photo, holder.binding.photo)

        holder.binding.photo.setOnClickListener { view -> on<PhotoActivityTransitionHandler>().show(view, photo) }
        holder.binding.photo.setOnLongClickListener { view ->
            on<MenuHandler>().show(
                    MenuHandler.MenuOption(R.drawable.ic_launch_black_24dp, R.string.open_group) {
                        on<GroupActivityTransitionHandler>().showGroupMessages(view, groupMessage.to!!)

                    })
            true
        }

    }
}

class PhotoViewHolder(val binding: PhotoItemBinding) : RecyclerView.ViewHolder(binding.root)
