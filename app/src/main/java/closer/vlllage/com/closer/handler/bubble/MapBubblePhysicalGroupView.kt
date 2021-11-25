package closer.vlllage.com.closer.handler.bubble

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.databinding.MapBubblePhysicalGroupBinding
import closer.vlllage.com.closer.handler.group.PhysicalGroupHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ImageHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Phone
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.queatz.on.On
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

class MapBubblePhysicalGroupView constructor(private val on: On) {

    fun from(layer: ViewGroup, mapBubble: MapBubble, onClickListener: MapBubblePhysicalGroupClickListener): MapBubblePhysicalGroupBinding {
        val binding = MapBubblePhysicalGroupBinding.inflate(LayoutInflater.from(layer.context), layer, false)

        binding.click.setOnClickListener { onClickListener.invoke(mapBubble) }
        update(binding, mapBubble)

        return binding
    }

    fun update(binding: MapBubblePhysicalGroupBinding, mapBubble: MapBubble) {
        var margin = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
        var size = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.physicalGroupIcon)
        if (mapBubble.tag != null) {
            when (mapBubble.tag) {
                is Group -> {
                    if (!mapBubble.inProxy)
                        binding.click.setBackgroundResource(R.drawable.clickable_purple)

                    on<PhysicalGroupHandler>().physicalGroupName(mapBubble.tag as Group)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                if (binding.name.isAttachedToWindow) {
                                    binding.name.text = it
                                }
                            }, {}).also {
                                on<DisposableHandler>().add(it)
                            }
                }
                is Phone -> {
                    if (!mapBubble.inProxy)
                        binding.click.setBackgroundResource(R.drawable.clickable_white_rounded)

                    val status = (mapBubble.tag as Phone).status
                    binding.name.text = if (status.isNullOrBlank()) on<NameHandler>().getName((mapBubble.tag as Phone)) else "\"$status\""
                }
            }

            val photoUrl = when (mapBubble.tag) {
                is Group -> (mapBubble.tag as Group).photo
                is Phone -> (mapBubble.tag as Phone).photo
                else -> null
            }

            if (photoUrl != null) {
                margin /= 4
                size = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.physicalGroupPhoto)
                binding.photo.colorFilter = null
                binding.photo.imageTintList = ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.transparent))
                on<ImageHandler>().get()
                        .load("$photoUrl?s=128")
                        .apply(RequestOptions().circleCrop())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.photo)
            } else {
                binding.photo.setImageResource(if (mapBubble.tag is Phone) R.drawable.ic_person_black_24dp else R.drawable.ic_chat_black_24dp)
                binding.photo.imageTintList = if (mapBubble.tag is Phone) null else ColorStateList.valueOf(on<ResourcesHandler>().resources.getColor(android.R.color.white))
            }
        }

        (binding.photo.layoutParams as ViewGroup.MarginLayoutParams).setMargins(margin, margin, margin, margin)
        (binding.photo.layoutParams as ViewGroup.MarginLayoutParams).height = size
        (binding.photo.layoutParams as ViewGroup.MarginLayoutParams).width = size
    }

}

typealias MapBubblePhysicalGroupClickListener = (mapBubble: MapBubble) -> Unit
