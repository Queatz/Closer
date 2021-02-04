package closer.vlllage.com.closer.handler.data

import closer.vlllage.com.closer.api.ApiService
import closer.vlllage.com.closer.api.Backend
import closer.vlllage.com.closer.api.models.QuestProgressResult
import closer.vlllage.com.closer.api.models.QuestResult
import closer.vlllage.com.closer.api.models.SuccessResult
import closer.vlllage.com.closer.handler.event.EventReminder
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.store.models.QuestFlow
import closer.vlllage.com.closer.store.models.QuestProgressFlow
import com.google.android.gms.maps.model.LatLng
import com.queatz.on.On
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.Util
import okio.BufferedSink
import okio.Okio
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStream
import java.util.*

class ApiHandler constructor(private val on: On) {

    private val api = ApiService()

    fun isVerified() = api { isVerified() }.map { it.verified }

    fun allGroupMember() = api { allGroupMembers() }

    fun setAuthorization(auth: String) {
        api.authorization = auth
    }

    fun terms(phoneId: String, good: Boolean) = api { terms(phoneId, good) }

    fun call(phoneId: String, event: String, data: String) = api { call(phoneId, event, data) }

    fun getPhonesNear(latLng: LatLng) = api { getPhonesNear(on<LatLngStr>().from(latLng)) }

    fun getSuggestionsNear(latLng: LatLng) = api { getSuggestionsNear(on<LatLngStr>().from(latLng)) }

    fun addSuggestion(name: String, latLng: LatLng) = api { addSuggestion(name, on<LatLngStr>().from(latLng)) }

    fun getStoriesNear(latLng: LatLng) = api { getStoriesNear(on<LatLngStr>().from(latLng)) }

    fun addStory(text: String?, photo: String?, latLng: LatLng) = api { addStory(text, photo, on<LatLngStr>().from(latLng)) }

    fun storyViewed(storyId: String) = api { storyViewed(storyId) }

    fun getStory(storyId: String) = api { getStory(storyId) }

    fun updatePhone(latLng: String? = null,
                    name: String? = null,
                    status: String? = null,
                    active: Boolean? = null,
                    deviceToken: String? = null,
                    introduction: String? = null,
                    offtime: String? = null,
                    occupation: String? = null,
                    history: String? = null) = api { phoneUpdate(latLng, name, status, active, deviceToken, introduction, offtime, occupation, history) }

    fun updatePhonePhoto(photoUrl: String) = api { phoneUpdatePhoto(photoUrl) }

    fun updatePhonePrivateMode(privateMode: Boolean) = api { updatePhonePrivateMode(privateMode) }

    fun phone() = api { phone() }

    fun searchPhonesNear(latLng: LatLng, query: String) = api { searchPhonesNear(on<LatLngStr>().from(latLng), query) }

    fun getPhone(phoneId: String) = api { getPhone(phoneId) }

    fun sendMessage(phone: String, message: String) = api { sendMessage(phone, message) }

    fun myMessages(latLng: LatLng) = api { myMessages(on<LatLngStr>().from(latLng)) }

    fun myGroups(latLng: LatLng) = api { myGroups(on<LatLngStr>().from(latLng)) }

    fun myEvents() = api { myEvents() }

    fun getGroup(groupId: String) = api { getGroup(groupId) }

    fun getDirectGroup(phoneId: String) = api { getDirectGroup(phoneId) }

    fun setPhoneNumber(phoneNumber: String) = api { setPhoneNumber(phoneNumber) }

    fun sendVerificationCode(verificationCode: String) = api { sendVerificationCode(verificationCode) }

    fun addGoal(goalName: String, remove: Boolean? = null) = api { addGoal(goalName, remove) }

    fun addLifestyle(lifestyleName: String, remove: Boolean? = null) = api { addLifestyle(lifestyleName, remove) }

    fun phonesForGoal(goalName: String) = api { phonesForGoal(goalName) }

    fun phonesForLifestyle(lifestyleName: String) = api { phonesForLifestyle(lifestyleName) }

    fun getNewPhonesToMeetNear(latLng: LatLng) = api { getNewPhonesToMeetNear(on<LatLngStr>().from(latLng)) }

    fun setMeet(phoneId: String, meet: Boolean) = api { setMeet(phoneId, meet) }

    fun sendGroupMessage(groupId: String, text: String?, attachment: String?) = api { sendGroupMessage(groupId, text, attachment) }

    fun deleteGroupMessage(messageId: String) = api { deleteGroupMessage(messageId, true) }

    fun reactToMessage(messageId: String, reaction: String, removeReaction: Boolean) = api { reactToMessage(messageId, reaction, removeReaction) }

    fun groupMessageReactions(messageId: String) = api { groupMessageReactions(messageId) }

    fun createGroup(groupName: String) = api { createGroup(groupName) }

    fun createPublicGroup(groupName: String, about: String, latLng: LatLng) = api { createPublicGroup(groupName, about, on<LatLngStr>().from(latLng), true) }

    fun createPhysicalGroup(name: String, about: String, isPublic: Boolean, latLng: LatLng, hub: Boolean) = api { createPhysicalGroup(name, about, on<LatLngStr>().from(latLng), isPublic, true, hub) }

    fun convertToHub(groupId: String, name: String) = api { convertToHub(groupId, name, true) }

    fun inviteToGroup(groupId: String, name: String, phoneNumber: String) = api { inviteToGroup(groupId, name, phoneNumber) }

    fun inviteToGroup(groupId: String, phoneId: String) = api { inviteToGroup(groupId, phoneId) }

    fun leaveGroup(groupId: String) = api { leaveGroup(groupId, true) }

    fun updateGroupStatus(groupId: String, status: String) = api { updateGroupStatus(groupId, status) }

    fun updateGroupPhoto(groupId: String, photo: String) = api { updateGroupPhoto(groupId, photo) }

    fun setGroupPhoto(groupId: String, photo: String) = api { setGroupPhoto(groupId, photo) }

    fun setGroupAbout(groupId: String, about: String) = api { setGroupAbout(groupId, about) }

    fun removeGroupAction(groupActionId: String) = api { removeGroupAction(groupActionId) }

    fun createGroupAction(groupId: String, name: String, intent: String) = api { createGroupAction(groupId, name, intent) }

    fun updateGroupActionFlow(groupActionId: String, flow: String) = api { updateGroupActionFlow(groupActionId, flow) }

    fun updateGroupActionAbout(groupActionId: String, about: String) = api { updateGroupActionAbout(groupActionId, about) }

    fun usedGroupAction(groupActionId: String) = api { usedGroupAction(groupActionId, true) }

    fun getGroupAction(groupActionId: String) = api { getGroupAction(groupActionId) }

    fun getGroupActions(groupId: String) = api { getGroupActions(groupId) }

    fun getGroupActions(latLng: LatLng) = api { getGroupActionsNearGeo(on<LatLngStr>().from(latLng)) }

    fun getPins(groupId: String) = api { getPins(groupId) }

    fun getContacts(groupId: String) = api { getContacts(groupId) }

    fun addPin(messageId: String, groupId: String) = api { pin(groupId, messageId, false) }

    fun removePin(messageId: String, groupId: String) = api { pin(groupId, messageId, true) }

    fun getGroupMessages(groupId: String) = api { getGroupMessages(groupId) }

    fun getGroupMessage(groupMessageId: String) = api { getGroupMessage(groupMessageId) }

    fun setGroupActionPhoto(groupActionId: String, photo: String) = api { setGroupActionPhoto(groupActionId, photo) }

    fun getGroupMember(groupId: String) = api { getGroupMember(groupId) }

    fun updateGroupMember(groupId: String, muted: Boolean, subscribed: Boolean) = api { updateGroupMember(groupId, muted, subscribed) }

    fun getGroupForPhone(phoneId: String) = api { getGroupForPhone(phoneId) }

    fun getGroupForGroupMessage(groupMessageId: String) = api { getGroupForGroupMessage(groupMessageId) }

    fun getMessagesForPhone(phoneId: String) = api { getMessagesForPhone(phoneId) }

    fun getGroupContactsForPhone(phoneId: String) = api { getGroupContactsFromPhone(phoneId) }

    fun cancelInvite(groupId: String, groupInviteId: String) = api { cancelInvite(groupId, groupInviteId) }

    fun createQuest(name: String, isPublic: Boolean, latLng: LatLng, flow: QuestFlow) = api { createQuest(QuestResult().also {
        it.name = name
        it.isPublic = isPublic
        it.geo = listOf(latLng.latitude, latLng.longitude)
        it.flow = flow
    }) }

    fun getQuests(latLng: LatLng) = api { getQuests(on<LatLngStr>().from(latLng)) }

    fun getQuest(questId: String) = api { getQuest(questId) }

    fun getQuestProgresses(questId: String) = api { getQuestProgresses(questId) }

    fun getQuestActions(questId: String) = api { getQuestActions(questId) }

    fun createQuestProgress(questId: String, ofId: String, isPublic: Boolean, active: Boolean, progress: QuestProgressFlow) = api { createQuestProgress(QuestProgressResult().also {
        it.questId = questId
        it.ofId = ofId
        it.isPublic = isPublic
        it.progress = progress
        it.active = active
    }) }

    fun updateQuestProgress(questProgressId: String, finished: Date? = null, stopped: Date? = null, active: Boolean? = null, progress: QuestProgressFlow? = null) = api {
        updateQuestProgress(questProgressId, QuestProgressResult().also {
            it.finished = finished
            it.stopped = stopped
            it.active = active
            it.progress = progress
        })
    }

    fun getQuestProgresses() = api { getQuestProgresses() }

    fun addQuestLink(questId: String, toQuestId: String) = api { addQuestLink(questId, toQuestId) }

    fun removeQuestLink(questId: String, toQuestId: String) = api { removeQuestLink(questId, toQuestId) }

    fun getQuestLinks(questId: String) = api { getQuestLinks(questId) }

    fun getQuestProgress(questProgressId: String) = api { getQuestProgress(questProgressId) }

    fun createEvent(name: String, about: String, isPublic: Boolean, latLng: LatLng, startsAt: Date, endsAt: Date, allDay: Boolean) = api { createEvent(name, about, isPublic, on<LatLngStr>().from(latLng), on<HttpEncode>().encode(on<DateFormatter>().format(startsAt))!!, on<HttpEncode>().encode(on<DateFormatter>().format(endsAt))!!, allDay) }

    fun getEvents(latLng: LatLng) = api { getEvents(on<LatLngStr>().from(latLng)) }

    fun getEvent(eventId: String) = api { getEvent(eventId) }

    fun getEventRemindersOnDay(date: Date) = api { getEventRemindersOnDay(on<HttpEncode>().encode(on<DateFormatter>().format(date))!!) }

    fun cancelEvent(eventId: String) = api { cancelEvent(eventId, true) }

    fun updateEventReminders(eventId: String, reminders: List<EventReminder>) = api { updateEventReminders(eventId, reminders) }

    fun getPhysicalGroups(latLng: LatLng) = api { getGroups("physical", on<LatLngStr>().from(latLng)) }

    fun getInactiveGroups(latLng: LatLng) = api { getGroups("inactive", on<LatLngStr>().from(latLng)) }

    fun getDirectGroups() = api { getGroups("direct") }

    fun uploadPhoto(photo: InputStream): Single<String> {
        val body = object : RequestBody() {
            override fun contentType(): MediaType? {
                return MediaType.parse("image/*")
            }

            override fun contentLength(): Long {
                return try {
                    photo.available().toLong()
                } catch (e: IOException) {
                    e.printStackTrace()
                    0
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
        val id = on<Val>().rndId()
        return uiThread(api.photoUploadBackend.uploadPhoto(id, part))
                .map { id }
    }

    fun getRecentlyActivePhones(limit: Int) = api { getRecentlyActivePhones(limit) }

    fun getRecentlyActiveGroups(limit: Int) = api { getRecentlyActiveGroups(limit) }

    fun getFeatureRequests() = api { getFeatureRequests() }

    fun addFeatureRequest(name: String, description: String) = api { addFeatureRequest(name, description) }

    fun voteForFeatureRequest(featureRequestId: String, vote: Boolean) = api { voteForFeatureRequest(featureRequestId, vote) }

    fun completeFeatureRequest(featureRequestId: String, completed: Boolean) = api { completeFeatureRequest(featureRequestId, completed) }

    fun privacy() = uiThread(api.contentBackend.privacy()).map { it.string() }

    fun terms() = uiThread(api.contentBackend.terms()).map { it.string() }

    fun getPlaces(query: String, bias: LatLng, limit: Int = 3) = uiThread(api.placesBackend.query(query, bias.latitude, bias.longitude, limit))

    fun reverseGeocode(latLng: LatLng, limit: Int = 3) = uiThread(api.placesBackend.reverse(latLng.latitude, latLng.longitude, limit))

    fun getInviteCode(code: String) = api { getInviteCode(code) }

    fun createInviteCode(groupId: String, singleUse: Boolean = true) = api { createInviteCode(groupId, singleUse) }

    fun useInviteCode(code: String) = api { useInviteCode(code) }

    private fun <T> api(call: Backend.() -> Single<T>) = uiThread(call(api.backend))

    private fun <T> uiThread(observable: Single<T>): Single<T> {
        return observable.observeOn(AndroidSchedulers.mainThread()).onErrorResumeNext {
            Single.error(ApiError(it, when (it) {
                is HttpException -> it.response().errorBody()?.string()?.let { on<JsonHandler>().from(it, SuccessResult::class.java) }
                else -> SuccessResult().apply {
                    success = false
                    error = it.message
                }
            }))
        }
    }
}

class ApiError(error: Throwable, val successResult: SuccessResult?) : RuntimeException(error) {
    val error get() = successResult?.error
    val success get() = successResult?.success
}