package closer.vlllage.com.closer.api

import closer.vlllage.com.closer.api.models.*
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface Backend {

    // Verify Number

    @get:GET("verify")
    val isVerified: Single<VerifiedResult>

    // Member

    @get:GET("member")
    val allGroupMembers: Single<List<GroupMemberResult>>

    // Phone

    @GET("map/{geo}")
    fun getPhonesNear(@Path("geo") geo: String): Single<List<PhoneResult>>

    @POST("phone/{phone}")
    fun sendMessage(@Path("phone") phone: String, @Query("message") message: String): Single<SuccessResult>

    @GET("phone")
    fun phone(): Single<PhoneResult>

    @POST("phone")
    fun phoneUpdate(
            @Query("geo") latLng: String?,
            @Query("name") name: String?,
            @Query("status") status: String?,
            @Query("active") active: Boolean?,
            @Query("deviceToken") pushDeviceToken: String?,
            @Query("introduction") introduction: String?,
            @Query("offtime") offtime: String?,
            @Query("history") history: String?
    ): Single<CreateResult>

    @POST("phone")
    fun phoneUpdatePhoto(@Query("photo") photo: String): Single<CreateResult>

    @POST("phone")
    fun updatePhonePrivateMode(@Query("privateMode") privateMode: Boolean): Single<CreateResult>

    @GET("phone")
    fun searchPhonesNear(@Query("geo") latLng: String, @Query("query") query: String): Single<List<PhoneResult>>

    @GET("phone/{phone}")
    fun getPhone(@Path("phone") phone: String): Single<PhoneResult>

    @GET("phone/{phone}/group")
    fun getGroupForPhone(@Path("phone") phoneId: String): Single<GroupResult>

    @GET("phone/{phone}/messages")
    fun getMessagesForPhone(@Path("phone") phoneId: String): Single<List<GroupMessageResult>>

    @GET("phone/{phone}/groups")
    fun getGroupContactsFromPhone(@Path("phone") phoneId: String): Single<List<GroupContactResult>>

    @POST("verify")
    fun setPhoneNumber(@Query("set-number") phoneNumber: String): Single<SuccessResult>

    @POST("verify")
    fun sendVerificationCode(@Query("verify-code") verificationCode: String): Single<VerifiedResult>

    // Goal

    @POST("goal")
    fun addGoal(@Query("name") goalName: String, @Query("remove") remove: Boolean?): Single<SuccessResult>

    // Lifestyle

    @POST("lifestyle")
    fun addLifestyle(@Query("name") lifestyleName: String, @Query("remove") remove: Boolean?): Single<SuccessResult>

    // Suggestion

    @GET("suggestion/{latLng}")
    fun getSuggestionsNear(@Path("latLng") latLng: String): Single<List<SuggestionResult>>

    @POST("suggestion")
    fun addSuggestion(@Query("name") name: String, @Query("geo") geo: String): Single<CreateResult>

    // Group Message

    @GET("message")
    fun myMessages(@Query("geo") latLng: String): Single<List<GroupMessageResult>>

    @POST("message")
    fun sendGroupMessage(@Query("group") groupId: String, @Query("text") text: String?, @Query(value = "attachment", encoded = true) attachment: String?): Single<CreateResult>

    @POST("message/{id}")
    fun reactToMessage(@Path("id") messageId: String, @Query("react") text: String, @Query("remove") remove: Boolean): Single<SuccessResult>

    @GET("message/{id}/reactions")
    fun groupMessageReactions(@Path("id") messageId: String): Single<List<ReactionResult>>

    @GET("message/{id}")
    fun getGroupMessage(@Path("id") groupMessageId: String): Single<GroupMessageResult>

    @GET("group/{id}/messages")
    fun getGroupMessages(@Path("id") groupId: String): Single<List<GroupMessageResult>>

    // Group

    @GET("group")
    fun myGroups(@Query("geo") latLng: String): Single<StateResult>

    @POST("group")
    fun createGroup(@Query("name") groupName: String): Single<CreateResult>

    @POST("group")
    fun createPublicGroup(@Query("name") groupName: String, @Query("about") about: String, @Query("geo") geo: String, @Query("public") isPublic: Boolean): Single<CreateResult>

    @POST("group")
    fun createPhysicalGroup(@Query("geo") geo: String, @Query("physical") physical: Boolean): Single<CreateResult>

    @GET("group")
    fun getPhysicalGroups(@Query("geo") latLng: String, @Query("kind") kind: String): Single<List<GroupResult>>

    @GET("group/{id}")
    fun getGroup(@Path("id") groupId: String): Single<GroupResult>

    @POST("group/{id}")
    fun inviteToGroup(@Path("id") groupId: String, @Query("name") name: String, @Query("invite") phoneNumber: String): Single<SuccessResult>

    @POST("group/{id}")
    fun inviteToGroup(@Path("id") groupId: String, @Query("invite-phone") phoneId: String): Single<SuccessResult>

    @POST("group/{id}")
    fun leaveGroup(@Path("id") groupId: String, @Query("leave") leaveGroup: Boolean): Single<SuccessResult>

    @POST("group/{id}")
    fun cancelInvite(@Path("id") groupId: String, @Query("cancel-invite") groupInviteId: String): Single<SuccessResult>

    @POST("group/{id}")
    fun convertToHub(@Path("id") groupId: String, @Query("name") name: String, @Query("hub") hub: Boolean): Single<SuccessResult>

    @POST("group/{id}")
    fun setGroupPhoto(@Path("id") groupId: String, @Query("photo") photo: String): Single<SuccessResult>

    @POST("group/{id}")
    fun setGroupAbout(@Path("id") groupId: String, @Query("about") about: String): Single<SuccessResult>

    @POST("group/{id}")
    fun pin(@Path("id") groupId: String, @Query("pin") messageId: String, @Query("remove") remove: Boolean): Single<SuccessResult>

    // Group Action

    @POST("action/{id}/delete")
    fun removeGroupAction(@Path("id") groupActionId: String): Single<SuccessResult>

    @POST("action")
    fun createGroupAction(@Query("group") groupId: String, @Query("name") groupActionName: String, @Query("intent") groupActionIntent: String): Single<CreateResult>

    @GET("group/{id}/actions")
    fun getGroupActions(@Path("id") groupId: String): Single<List<GroupActionResult>>

    @GET("group/{id}/pins")
    fun getPins(@Path("id") groupId: String): Single<List<PinResult>>

    @GET("group/{id}/contacts")
    fun getContacts(@Path("id") groupId: String): Single<List<GroupContactResult>>

    @GET("action/{latLng}")
    fun getGroupActionsNearGeo(@Path("latLng") latLng: String): Single<List<GroupActionResult>>

    @POST("action/{id}")
    fun setGroupActionPhoto(@Path("id") groupActionId: String, @Query("photo") photo: String): Single<SuccessResult>

    // Event

    @GET("event")
    fun getEvents(@Query("geo") latLng: String): Single<List<EventResult>>

    @GET("event/{id}")
    fun getEvent(@Path("id") eventId: String): Single<EventResult>

    @POST("event")
    fun createEvent(@Query("name") name: String, @Query("about") about: String, @Query("public") isPublic: Boolean, @Query("geo") geo: String, @Query(value = "starts-at", encoded = true) startsAt: String, @Query(value = "ends-at", encoded = true) endsAt: String): Single<CreateResult>

    @POST("event/{id}")
    fun cancelEvent(@Path("id") eventId: String, @Query("cancel") cancel: Boolean): Single<SuccessResult>

    @GET("member/of/{group}")
    fun getGroupMember(@Path("group") groupId: String): Single<GroupMemberResult>

    @POST("member/of/{group}")
    fun updateGroupMember(@Path("group") groupId: String, @Query("muted") muted: Boolean, @Query("subscribed") subscribed: Boolean): Single<CreateResult>

    companion object {
        const val BASE_URL = "https://closer.vlllage.com/"
    }
}
