package closer.vlllage.com.closer.handler.share

import android.content.Intent
import closer.vlllage.com.closer.ShareActivity
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_EVENT_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_GROUP_MESSAGE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_INVITE_TO_GROUP_PHONE_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_LAT_LNG
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_SEARCH
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_SHARE_GROUP_ACTION_TO_GROUP_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_SHARE_GROUP_TO_GROUP_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_STORY_ID
import closer.vlllage.com.closer.ShareActivity.Companion.EXTRA_SUGGESTION_ID
import closer.vlllage.com.closer.handler.helpers.ActivityHandler
import closer.vlllage.com.closer.handler.helpers.ApplicationHandler
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import at.bluesource.choicesdk.maps.common.LatLng
import com.queatz.on.On

class ShareActivityTransitionHandler constructor(private val on: On) {
    fun shareGroupMessage(groupMessageId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_GROUP_MESSAGE_ID, groupMessageId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun shareStoryToGroup(storyId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_STORY_ID, storyId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun inviteToGroup(phoneId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_INVITE_TO_GROUP_PHONE_ID, phoneId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun shareGroupToGroup(groupId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SHARE_GROUP_TO_GROUP_ID, groupId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun shareEvent(eventId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_EVENT_ID, eventId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun shareGroupActionToGroup(groupActionId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SHARE_GROUP_ACTION_TO_GROUP_ID, groupActionId)

        on<ActivityHandler>().activity!!.startActivity(intent)
    }

    fun shareSuggestion(suggestionId: String) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_SUGGESTION_ID, suggestionId)

        on<ActivityHandler>().activity!!.startActivity(intent)

    }

    fun performArcheology(latLng: LatLng, searchString: String? = null) {
        val intent = Intent(on<ApplicationHandler>().app, ShareActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(EXTRA_LAT_LNG, on<LatLngStr>().from(latLng))
        searchString?.let { intent.putExtra(EXTRA_SEARCH, it) }

        on<ActivityHandler>().activity!!.startActivity(intent)
    }
}
