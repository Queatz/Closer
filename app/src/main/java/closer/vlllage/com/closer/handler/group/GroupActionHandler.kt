package closer.vlllage.com.closer.handler.group

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
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
import closer.vlllage.com.closer.ui.RevealAnimatorForConstraintLayout
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.objectbox.reactive.DataSubscription
import kotlinx.android.synthetic.main.add_action_modal.view.*

class GroupActionHandler constructor(private val on: On) {

    private var animator: RevealAnimatorForConstraintLayout? = null
    private var groupActionsDisposable: DataSubscription? = null
    private var disposableGroup = on<DisposableHandler>().group()
    private var isShowing: Boolean = false

    fun attach(container: ConstraintLayout, actionRecyclerView: RecyclerView) {
        isShowing = false
        animator?.cancel()
        groupActionsDisposable?.cancel()
        disposableGroup.clear()

        animator = RevealAnimatorForConstraintLayout(container, (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())

        on<GroupActionRecyclerViewHandler>().attach(actionRecyclerView, GroupActionDisplay.Layout.PHOTO)

        on<GroupHandler>().onGroupChanged(disposableGroup) { group ->
            if (groupActionsDisposable != null) {
                disposableGroup.dispose(groupActionsDisposable!!)
            }

            groupActionsDisposable = on<StoreHandler>().store.box(GroupAction::class).query()
                    .equal(GroupAction_.group, group.id!!)
                    .build()
                    .subscribe()
                    .on(AndroidScheduler.mainThread())
                    .observer { groupActions ->
                        on<GroupActionRecyclerViewHandler>().adapter!!.setGroupActions(groupActions, isShowing)
                        show(groupActions.isNotEmpty(), true)
                    }.also {
                        disposableGroup.add(it)
                    }

            on<RefreshHandler>().refreshGroupActions(group.id!!)
        }
    }

    fun show(show: Boolean, immediate: Boolean = false) {
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

                view.intentPrefix.setOnClickListener { intent.requestFocus() }

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

                    if (model.name.isNullOrBlank() || model.intent.isNullOrBlank()) {
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
