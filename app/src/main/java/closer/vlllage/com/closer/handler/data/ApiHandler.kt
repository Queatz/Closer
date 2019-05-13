package closer.vlllage.com.closer.handler.data

import com.google.android.gms.maps.model.LatLng

import java.io.IOException
import java.io.InputStream
import java.util.Date

import closer.vlllage.com.closer.api.ApiService
import closer.vlllage.com.closer.api.models.CreateResult
import closer.vlllage.com.closer.api.models.EventResult
import closer.vlllage.com.closer.api.models.GroupActionResult
import closer.vlllage.com.closer.api.models.GroupContactResult
import closer.vlllage.com.closer.api.models.GroupMemberResult
import closer.vlllage.com.closer.api.models.GroupMessageResult
import closer.vlllage.com.closer.api.models.GroupResult
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.api.models.PinResult
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.api.models.StateResult
import closer.vlllage.com.closer.api.models.SuccessResult
import closer.vlllage.com.closer.api.models.SuggestionResult
import closer.vlllage.com.closer.handler.helpers.DateFormatter
import closer.vlllage.com.closer.handler.helpers.HttpEncode
import closer.vlllage.com.closer.handler.helpers.LatLngStr
import closer.vlllage.com.closer.handler.helpers.Val
import closer.vlllage.com.closer.pool.PoolMember
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio

class ApiHandler : PoolMember() {

    private val api = ApiService()

    val isVerified: Single<Boolean>
        get() = uiThread(api.getBackend().isVerified)
                .map { verifiedResult -> verifiedResult.verified }

    val allGroupMember: Single<List<GroupMemberResult>>
        get() = uiThread(api.getBackend().allGroupMembers)

    fun setAuthorization(auth: String) {
        api.setAuthorization(auth)
    }

    fun getPhonesNear(latLng: LatLng): Single<List<PhoneResult>> {
        return uiThread(api.getBackend().getPhonesNear(`$`(LatLngStr::class.java).from(latLng)))
    }

    fun getSuggestionsNear(latLng: LatLng): Single<List<SuggestionResult>> {
        return uiThread(api.getBackend().getSuggestionsNear(`$`(LatLngStr::class.java).from(latLng)))
    }

    fun addSuggestion(name: String, latLng: LatLng): Single<CreateResult> {
        return uiThread(api.getBackend().addSuggestion(name, `$`(LatLngStr::class.java).from(latLng)))
    }

    fun updatePhone(latLng: String?, name: String?, status: String?, active: Boolean?, deviceToken: String?): Single<CreateResult> {
        return uiThread(api.getBackend().phoneUpdate(latLng, name, status, active, deviceToken))
    }

    fun updatePhonePhoto(photoUrl: String): Single<CreateResult> {
        return uiThread(api.getBackend().phoneUpdatePhoto(photoUrl))
    }

    fun updatePhonePrivateMode(privateMode: Boolean): Single<CreateResult> {
        return uiThread(api.getBackend().updatePhonePrivateMode(privateMode))
    }

    fun phone(): Single<PhoneResult> {
        return uiThread(api.getBackend().phone())
    }

    fun searchPhonesNear(latLng: LatLng, query: String): Single<List<PhoneResult>> {
        return uiThread(api.getBackend().searchPhonesNear(`$`(LatLngStr::class.java).from(latLng), query))
    }

    fun getPhone(phoneId: String): Single<PhoneResult> {
        return uiThread(api.getBackend().getPhone(phoneId))
    }

    fun sendMessage(phone: String, message: String): Single<SuccessResult> {
        return uiThread(api.getBackend().sendMessage(phone, message))
    }

    fun myMessages(latLng: LatLng): Single<List<GroupMessageResult>> {
        return uiThread(api.getBackend().myMessages(`$`(LatLngStr::class.java).from(latLng)))
    }

    fun myGroups(latLng: LatLng): Single<StateResult> {
        return uiThread(api.getBackend().myGroups(`$`(LatLngStr::class.java).from(latLng)))
    }

    fun getGroup(groupId: String): Single<GroupResult> {
        return uiThread(api.getBackend().getGroup(groupId))
    }

    fun setPhoneNumber(phoneNumber: String): Single<SuccessResult> {
        return uiThread(api.getBackend().setPhoneNumber(phoneNumber))
    }

    fun sendVerificationCode(verificationCode: String): Single<SuccessResult> {
        return uiThread(api.getBackend().sendVerificationCode(verificationCode))
    }

    fun sendGroupMessage(groupId: String, text: String, attachment: String): Single<CreateResult> {
        return uiThread(api.getBackend().sendGroupMessage(groupId, text, attachment))
    }

    fun reactToMessage(messageId: String, reaction: String, removeReaction: Boolean): Single<SuccessResult> {
        return uiThread(api.getBackend().reactToMessage(messageId, reaction, removeReaction))
    }

    fun groupMessageReactions(messageId: String): Single<List<ReactionResult>> {
        return uiThread(api.getBackend().groupMessageReactions(messageId))
    }

    fun createGroup(groupName: String): Single<CreateResult> {
        return uiThread(api.getBackend().createGroup(groupName))
    }

    fun createPublicGroup(groupName: String, about: String, latLng: LatLng): Single<CreateResult> {
        return uiThread(api.getBackend().createPublicGroup(groupName, about, `$`(LatLngStr::class.java).from(latLng), true))
    }

    fun createPhysicalGroup(latLng: LatLng): Single<CreateResult> {
        return uiThread(api.getBackend().createPhysicalGroup(`$`(LatLngStr::class.java).from(latLng), true))
    }

    fun convertToHub(groupId: String, name: String): Single<SuccessResult> {
        return uiThread(api.getBackend().convertToHub(groupId, name, true))
    }

    fun inviteToGroup(groupId: String, name: String, phoneNumber: String): Single<SuccessResult> {
        return uiThread(api.getBackend().inviteToGroup(groupId, name, phoneNumber))
    }

    fun inviteToGroup(groupId: String, phoneId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().inviteToGroup(groupId, phoneId))
    }

    fun leaveGroup(groupId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().leaveGroup(groupId, true))
    }

    fun setGroupPhoto(groupId: String, photo: String): Single<SuccessResult> {
        return uiThread(api.getBackend().setGroupPhoto(groupId, photo))
    }

    fun setGroupAbout(groupId: String, about: String): Single<SuccessResult> {
        return uiThread(api.getBackend().setGroupAbout(groupId, about))
    }

    fun removeGroupAction(groupActionId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().removeGroupAction(groupActionId))
    }

    fun createGroupAction(groupId: String, name: String, intent: String): Single<CreateResult> {
        return uiThread(api.getBackend().createGroupAction(groupId, name, intent))
    }

    fun getGroupActions(groupId: String): Single<List<GroupActionResult>> {
        return uiThread(api.getBackend().getGroupActions(groupId))
    }

    fun getGroupActions(latLng: LatLng): Single<List<GroupActionResult>> {
        return uiThread(api.getBackend().getGroupActionsNearGeo(`$`(LatLngStr::class.java).from(latLng)))
    }

    fun getPins(groupId: String): Single<List<PinResult>> {
        return uiThread(api.getBackend().getPins(groupId))
    }

    fun getContacts(groupId: String): Single<List<GroupContactResult>> {
        return uiThread(api.getBackend().getContacts(groupId))
    }

    fun addPin(messageId: String, groupId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().pin(groupId, messageId, false))
    }

    fun removePin(messageId: String, groupId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().pin(groupId, messageId, true))
    }

    fun getGroupMessages(groupId: String): Single<List<GroupMessageResult>> {
        return uiThread(api.getBackend().getGroupMessages(groupId))
    }

    fun getGroupMessage(groupMessageId: String): Single<GroupMessageResult> {
        return uiThread(api.getBackend().getGroupMessage(groupMessageId))
    }

    fun setGroupActionPhoto(groupActionId: String, photo: String): Single<SuccessResult> {
        return uiThread(api.getBackend().setGroupActionPhoto(groupActionId, photo))
    }

    fun getGroupMember(groupId: String): Single<GroupMemberResult> {
        return uiThread(api.getBackend().getGroupMember(groupId))
    }

    fun updateGroupMember(groupId: String, muted: Boolean, subscribed: Boolean): Single<CreateResult> {
        return uiThread(api.getBackend().updateGroupMember(groupId, muted, subscribed))
    }

    fun getGroupForPhone(phoneId: String): Single<GroupResult> {
        return uiThread(api.getBackend().getGroupForPhone(phoneId))
    }

    fun cancelInvite(groupId: String, groupInviteId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().cancelInvite(groupId, groupInviteId))
    }

    fun createEvent(name: String, about: String, isPublic: Boolean, latLng: LatLng, startsAt: Date, endsAt: Date): Single<CreateResult> {
        return uiThread(api.getBackend().createEvent(name, about, isPublic, `$`(LatLngStr::class.java).from(latLng), `$`(HttpEncode::class.java).encode(`$`(DateFormatter::class.java).format(startsAt))!!, `$`(HttpEncode::class.java).encode(`$`(DateFormatter::class.java).format(endsAt))!!))
    }

    fun getEvents(latLng: LatLng): Single<List<EventResult>> {
        return uiThread(api.getBackend().getEvents(`$`(LatLngStr::class.java).from(latLng)))
    }

    fun getEvent(eventId: String): Single<EventResult> {
        return uiThread(api.getBackend().getEvent(eventId))
    }

    fun cancelEvent(eventId: String): Single<SuccessResult> {
        return uiThread(api.getBackend().cancelEvent(eventId, true))
    }

    fun getPhysicalGroups(latLng: LatLng): Single<List<GroupResult>> {
        return uiThread(api.getBackend().getPhysicalGroups(`$`(LatLngStr::class.java).from(latLng), "physical"))
    }

    fun uploadPhoto(photo: InputStream): Single<String> {
        val body = object : RequestBody() {
            override fun contentType(): MediaType? {
                return MediaType.parse("image/*")
            }

            override fun contentLength(): Long {
                try {
                    return photo.available().toLong()
                } catch (e: IOException) {
                    e.printStackTrace()
                    return 0
                }

            }

            override fun writeTo(sink: BufferedSink) {
                val source = Okio.source(photo)
                try {
                    sink.writeAll(source)
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    Util.closeQuietly(source)
                }
            }
        }
        val part = MultipartBody.Part.createFormData("photo", "closer-photo", body)
        val id = `$`(Val::class.java).rndId()
        return uiThread(api.photoUploadBackend.uploadPhoto(id, part))
                .map { responseBody -> id }
    }

    fun privacy(): Single<String> {
        return uiThread(api.contentBackend.privacy()).map { it.string() }
    }

    private fun <T> uiThread(observable: Single<T>): Single<T> {
        return observable.observeOn(AndroidSchedulers.mainThread())
    }
}
