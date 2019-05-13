package closer.vlllage.com.closer.handler.group

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.data.ApiHandler
import closer.vlllage.com.closer.handler.data.PersistenceHandler
import closer.vlllage.com.closer.handler.data.RefreshHandler
import closer.vlllage.com.closer.handler.data.SyncHandler
import closer.vlllage.com.closer.handler.helpers.*
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription

class GroupActionHandler : PoolMember() {

    private var animator: RevealAnimator? = null
    private var groupActionsDisposable: DataSubscription? = null
    private var isShowing: Boolean = false

    fun attach(container: MaxSizeFrameLayout, actionRecyclerView: RecyclerView) {
        animator = RevealAnimator(container, (`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())

        `$`(GroupActionRecyclerViewHandler::class.java).attach(actionRecyclerView, GroupActionAdapter.Layout.PHOTO)

        `$`(DisposableHandler::class.java).add(`$`(GroupHandler::class.java).onGroupChanged().subscribe { group ->
            if (groupActionsDisposable != null) {
                `$`(DisposableHandler::class.java).dispose(groupActionsDisposable!!)
            }

            groupActionsDisposable = `$`(StoreHandler::class.java).store.box(GroupAction::class.java).query()
                    .equal(GroupAction_.group, group.id!!)
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { groupActions ->
                        `$`(GroupActionRecyclerViewHandler::class.java).adapter!!.setGroupActions(groupActions)
                        show(!groupActions.isEmpty(), true)
                    }

            `$`(DisposableHandler::class.java).add(groupActionsDisposable!!)

            `$`(RefreshHandler::class.java).refreshGroupActions(group.id!!)
        })
    }

    fun cancelPendingAnimation() {
        animator!!.cancel()
    }

    fun show(show: Boolean) {
        show(show, false)
    }

    private fun show(show: Boolean, immediate: Boolean) {
        var show = show
        if (animator == null) {
            return
        }

        if (`$`(GroupActionRecyclerViewHandler::class.java).adapter != null && `$`(GroupActionRecyclerViewHandler::class.java).adapter!!.itemCount == 0) {
            show = false
        }

        if (isShowing == show) {
            return
        }

        isShowing = show

        animator!!.show(show, immediate)
    }

    fun addActionToGroup(group: Group) {
        `$`(AlertHandler::class.java).make().apply {
            title = `$`(ResourcesHandler::class.java).resources.getString(R.string.add_an_action)
            negativeButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.nope)
            positiveButton = `$`(ResourcesHandler::class.java).resources.getString(R.string.add_action)
            layoutResId = R.layout.add_action_modal
            onAfterViewCreated = { alertConfig, view ->
                val name = view.findViewById<EditText>(R.id.name)
                val intent = view.findViewById<EditText>(R.id.intent)
                val model = AddToGroupModalModel()
                alertConfig.alertResult = model

                name.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable) {
                        model.name = name.text.toString()
                    }
                })
                intent.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable) {
                        model.intent = intent.text.toString()
                    }
                })
            }

            positiveButtonCallback = { alertResult ->
                    val model = alertResult as AddToGroupModalModel

                    if (`$`(Val::class.java).isEmpty(model.name) || `$`(Val::class.java).isEmpty(model.name)) {
                        `$`(DefaultAlerts::class.java).message(R.string.enter_a_name_and_intent)
                    } else {
                        createGroupAction(group, model.name, model.intent)
                    }

                }
            show()
        }
    }

    fun joinGroup(group: Group) {
        `$`(DisposableHandler::class.java).add(`$`(ApiHandler::class.java).inviteToGroup(group.id!!, `$`(PersistenceHandler::class.java).phoneId!!).subscribe(
                { successResult ->
                    if (successResult.success) {
                        `$`(ToastHandler::class.java).show(`$`(ResourcesHandler::class.java).resources.getString(R.string.you_joined_group, group.name))
                    } else {
                        `$`(DefaultAlerts::class.java).thatDidntWork()
                    }
                }, { error -> `$`(DefaultAlerts::class.java).thatDidntWork() }))
    }

    private fun createGroupAction(group: Group, name: String?, intent: String?) {
        val groupAction = GroupAction()
        groupAction.group = group.id
        groupAction.name = name
        groupAction.intent = intent

        `$`(StoreHandler::class.java).store.box(GroupAction::class.java).put(groupAction)
        `$`(SyncHandler::class.java).sync(groupAction)
    }

    private class AddToGroupModalModel {
        internal var name: String? = null
        internal var intent: String? = null
    }
}
