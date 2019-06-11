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
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.GroupAction
import closer.vlllage.com.closer.store.models.GroupAction_
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription

class GroupActionHandler constructor(private val on: On) {

    private var animator: RevealAnimator? = null
    private var groupActionsDisposable: DataSubscription? = null
    private var isShowing: Boolean = false

    fun attach(container: MaxSizeFrameLayout, actionRecyclerView: RecyclerView) {
        animator = RevealAnimator(container, (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())

        on<GroupActionRecyclerViewHandler>().attach(actionRecyclerView, GroupActionAdapter.Layout.PHOTO)

        on<GroupHandler>().onGroupChanged { group ->
            if (groupActionsDisposable != null) {
                on<DisposableHandler>().dispose(groupActionsDisposable!!)
            }

            groupActionsDisposable = on<StoreHandler>().store.box(GroupAction::class).query()
                    .equal(GroupAction_.group, group.id!!)
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { groupActions ->
                        on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions)
                        show(!groupActions.isEmpty(), true)
                    }

            on<DisposableHandler>().add(groupActionsDisposable!!)

            on<RefreshHandler>().refreshGroupActions(group.id!!)
        }
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

        if (on<GroupActionRecyclerViewHandler>().adapter != null && on<GroupActionRecyclerViewHandler>().adapter!!.itemCount == 0) {
            show = false
        }

        if (isShowing == show) {
            return
        }

        isShowing = show

        animator!!.show(show, immediate)
    }

    fun addActionToGroup(group: Group) {
        on<AlertHandler>().make().apply {
            title = on<ResourcesHandler>().resources.getString(R.string.add_an_action)
            negativeButton = on<ResourcesHandler>().resources.getString(R.string.nope)
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.add_action)
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

                    if (on<Val>().isEmpty(model.name) || on<Val>().isEmpty(model.name)) {
                        on<DefaultAlerts>().message(R.string.enter_a_name_and_intent)
                    } else {
                        createGroupAction(group, model.name, model.intent)
                    }

                }
            show()
        }
    }

    fun joinGroup(group: Group) {
        on<DisposableHandler>().add(on<ApiHandler>().inviteToGroup(group.id!!, on<PersistenceHandler>().phoneId!!).subscribe(
                { successResult ->
                    if (successResult.success) {
                        on<ToastHandler>().show(on<ResourcesHandler>().resources.getString(R.string.you_joined_group, group.name))
                    } else {
                        on<DefaultAlerts>().thatDidntWork()
                    }
                }, { error -> on<DefaultAlerts>().thatDidntWork() }))
    }

    private fun createGroupAction(group: Group, name: String?, intent: String?) {
        val groupAction = GroupAction()
        groupAction.group = group.id
        groupAction.name = name
        groupAction.intent = intent

        on<StoreHandler>().store.box(GroupAction::class).put(groupAction)
        on<SyncHandler>().sync(groupAction)
    }

    private class AddToGroupModalModel {
        internal var name: String? = null
        internal var intent: String? = null
    }
}
