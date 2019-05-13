package closer.vlllage.com.closer

import android.content.Intent
import android.os.Bundle
import closer.vlllage.com.closer.api.models.PhoneResult
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.handler.phone.NameHandler
import closer.vlllage.com.closer.handler.phone.PhoneAdapterHeaderAdapter
import closer.vlllage.com.closer.pool.PoolMember
import com.google.android.gms.maps.model.LatLng
import java.util.*

class PhoneListActivity : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        `$`(ApiHandler::class.java).setAuthorization(`$`(AccountHandler::class.java).phone)

        val adapter = PhoneAdapterHeaderAdapter(`$`(PoolMember::class.java)) { reactionResult ->
            if (reactionResult.from == `$`(PersistenceHandler::class.java).phoneId) {
                `$`(DefaultAlerts::class.java).message(`$`(ResourcesHandler::class.java).resources.getString(R.string.remove_heart)) { result ->
                    `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).reactToMessage(reactionResult.to!!, "♥", true)
                            .subscribe({ successResult ->
                                if (successResult.success) {
                                    `$`(ToastHandler::class.java).show(R.string.heart_removed)
                                    `$`(ApplicationHandler::class.java).app.`$`(RefreshHandler::class.java).refreshGroupMessage(reactionResult.to!!)
                                    finish()
                                } else {
                                    `$`(DefaultAlerts::class.java).thatDidntWork()
                                }
                            }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
                }
                return@PhoneAdapterHeaderAdapter
            }

            finish(Runnable {
                `$`(MapActivityHandler::class.java).replyToPhone(
                        reactionResult.phone!!.id!!,
                        `$`(NameHandler::class.java).getName(PhoneResult.from(reactionResult.phone!!)),
                        reactionResult.reaction!!,
                        if (reactionResult.phone!!.geo == null)
                            null
                        else
                            LatLng(
                                    reactionResult.phone!!.geo!![0],
                                    reactionResult.phone!!.geo!![1]
                            ))
            })
        }
        recyclerView.adapter = adapter

        if (intent != null && Intent.ACTION_VIEW == intent.action) {
            val groupMessageId = intent.getStringExtra(EXTRA_GROUP_MESSAGE_ID)

            if (groupMessageId == null) {
                `$`(DefaultAlerts::class.java).thatDidntWork()
            } else {
                `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).groupMessageReactions(groupMessageId)
                        .map { this.sortItems(it) }
                        .subscribe({ adapter.items = it }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
            }
        }

    }

    private fun sortItems(reactionResults: List<ReactionResult>): List<ReactionResult> {
        val phoneId = `$`(PersistenceHandler::class.java).phoneId

        Collections.sort(reactionResults) { o1, o2 ->
            if (o1.from == phoneId && o2.from != phoneId) {
                return@sort - 1
            }

            0
        }
        return reactionResults
    }

    companion object {
        const val EXTRA_GROUP_MESSAGE_ID = "groupMessageId"
    }
}
