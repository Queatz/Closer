package closer.vlllage.com.closer

import android.content.Intent
import android.os.Bundle
import closer.vlllage.com.closer.api.models.ReactionResult
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.handler.phone.NavigationHandler
import closer.vlllage.com.closer.handler.phone.PhoneAdapterHeaderAdapter
import java.util.*

class PhoneListActivity : ListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = PhoneAdapterHeaderAdapter(on) { reactionResult ->
            if (reactionResult.from == on<PersistenceHandler>().phoneId) {
                on<DefaultAlerts>().message(on<ResourcesHandler>().resources.getString(R.string.remove_x, reactionResult.reaction!!)) { result ->
                    on<DisposableHandler>().add(on<ApiHandler>().reactToMessage(reactionResult.to!!, reactionResult.reaction!!, true)
                            .subscribe({ successResult ->
                                if (successResult.success) {
                                    on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.x_removed, reactionResult.reaction!!))
                                    on<ApplicationHandler>().app.on<RefreshHandler>().refreshGroupMessage(reactionResult.to!!)
                                    finish()
                                } else {
                                    on<DefaultAlerts>().thatDidntWork()
                                }
                            }, { on<DefaultAlerts>().thatDidntWork() }))
                }
                return@PhoneAdapterHeaderAdapter
            }

            finish(Runnable {
                on<NavigationHandler>().showProfile(reactionResult.phone!!.id!!)
            })
        }

        recyclerView.adapter = adapter

        if (intent != null && Intent.ACTION_VIEW == intent.action) {
            val groupMessageId = intent.getStringExtra(EXTRA_GROUP_MESSAGE_ID)

            if (groupMessageId == null) {
                on<DefaultAlerts>().thatDidntWork()
            } else {
                on<DisposableHandler>().add(on<ApiHandler>().groupMessageReactions(groupMessageId)
                        .map { this.sortItems(it) }
                        .subscribe({ adapter.items = it }, { on<DefaultAlerts>().thatDidntWork() }))
            }
        }

    }

    private fun sortItems(reactionResults: List<ReactionResult>): List<ReactionResult> {
        val phoneId = on<PersistenceHandler>().phoneId

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
