package closer.vlllage.com.closer.handler.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.updateLayoutParams
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.DataHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.ImageHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonObject
import com.queatz.on.On
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.message_item.view.*

class MessageSections constructor(private val on: On) {
    fun renderSection(jsonObject: JsonObject, parent: ViewGroup): Single<View> {
        return when {
            jsonObject.has("activity") -> renderGroupActionSection(jsonObject.get("activity").asJsonObject, parent)
            jsonObject.has("header") -> renderHeaderSection(jsonObject.get("header").asJsonObject, parent)
            jsonObject.has("text") -> renderTextSection(jsonObject.get("text").asJsonObject, parent)
//            jsonObject.has("action") -> displayAction(holder, jsonObject, groupMessage)
//            jsonObject.has("review") -> displayReview(holder, jsonObject, groupMessage)
//            jsonObject.has("message") -> displayMessage(holder, jsonObject, groupMessage)
//            jsonObject.has("event") -> displayEvent(holder, jsonObject, groupMessage, onEventClickListener)
//            jsonObject.has("group") -> displayGroup(holder, jsonObject, groupMessage, onGroupClickListener)
//            jsonObject.has("suggestion") -> displaySuggestion(holder, jsonObject, groupMessage, onSuggestionClickListener)
            jsonObject.has("photo") -> renderPhotoSection(jsonObject.get("photo").asJsonObject, parent)
//            jsonObject.has("share") -> displayShare(holder, jsonObject, groupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
//            jsonObject.has("post") -> displayPost(holder, jsonObject, groupMessage, onEventClickListener, onGroupClickListener, onSuggestionClickListener)
            else -> Single.just(View(parent.context))
        }
    }

    fun isFullWidth(jsonObject: JsonObject) = when {
        jsonObject.has("photo") -> jsonObject.get("photo").asJsonObject.get("large")?.asBoolean == true
        else -> false
    }

    fun renderPhotoSection(photo: JsonObject, parent: ViewGroup) = Single.just(LayoutInflater.from(parent.context).inflate(R.layout.group_photo_item, parent, false).also { rootView ->
        val isFullWidth = photo.get("large")?.asBoolean == true
        val url = photo.get("photo").asString + "?s=500"

        if (!isFullWidth) {
            rootView.updateLayoutParams {
                width = WRAP_CONTENT
            }

            rootView.photo.updateLayoutParams {
                width = WRAP_CONTENT
            }

            rootView.photo.maxHeight = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.maxPhotoHeight)
        }

        rootView.photo.setOnClickListener { view -> on<PhotoActivityTransitionHandler>().show(view, url) }
        on<ImageHandler>().get().clear(rootView.photo)
        on<ImageHandler>().get().load(url)
                .apply(RequestOptions().transform(RoundedCorners(on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.imageCorners))))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(rootView.photo)
    })

    fun renderHeaderSection(header: JsonObject, parent: ViewGroup) = LayoutInflater.from(parent.context).inflate(R.layout.group_header_item, parent, false).let { rootView ->
        rootView.message.text = on<GroupMessageParseHandler>().parseText(rootView.message, header.getAsJsonPrimitive("text").asString)

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            rootView.message.setTextColor(it.text)
        })

        Single.just(rootView)
    }

    fun renderTextSection(text: JsonObject, parent: ViewGroup) = LayoutInflater.from(parent.context).inflate(R.layout.group_text_item, parent, false).let { rootView ->
        rootView.message.text = on<GroupMessageParseHandler>().parseText(rootView.message, text.getAsJsonPrimitive("text").asString)

        on<DisposableHandler>().add(on<LightDarkHandler>().onLightChanged.subscribe {
            rootView.message.setTextColor(it.text)
        })

        Single.just(rootView)
    }

    fun renderGroupActionSection(activity: JsonObject, parent: ViewGroup) = on<DataHandler>().getGroupAction(activity.getAsJsonPrimitive("id").asString)
            .observeOn(AndroidSchedulers.mainThread())
            .map { groupAction ->
                val rootView = LayoutInflater.from(parent.context).inflate(R.layout.group_action_photo_item, parent, false)
                (rootView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
                    marginStart = 0
                }
                on<GroupActionDisplay>().display(rootView, groupAction, GroupActionDisplay.Layout.PHOTO)
                return@map rootView
            }.onErrorReturn {
                val rootView = LayoutInflater.from(parent.context).inflate(R.layout.group_action_photo_unavailable, parent, false)
                (rootView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.pad)
                    marginStart = 0
                }
                return@onErrorReturn rootView
            }
}