package closer.vlllage.com.closer.handler.map

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.ContentViewType
import closer.vlllage.com.closer.handler.data.AccountHandler
import closer.vlllage.com.closer.handler.data.AccountHandler.Companion.ACCOUNT_FIELD_PRIVATE
import closer.vlllage.com.closer.handler.feed.FeedContent
import closer.vlllage.com.closer.handler.feed.MixedHeaderAdapter
import closer.vlllage.com.closer.handler.group.GroupToolbarHandler
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.KeyboardVisibilityHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.settings.SettingsHandler
import closer.vlllage.com.closer.handler.settings.UserLocalSetting
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Group_
import closer.vlllage.com.closer.store.models.Notification
import com.queatz.on.On
import io.objectbox.android.AndroidScheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class FeedHandler constructor(private val on: On) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var mixedAdapter: MixedHeaderAdapter

    fun attach(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        layoutManager = LinearLayoutManager(
                recyclerView.context,
                RecyclerView.VERTICAL,
                false
        )
        recyclerView.layoutManager = layoutManager

        on<DisposableHandler>().add(on<SettingsHandler>()
                .observe(UserLocalSetting.CLOSER_SETTINGS_USE_LIGHT_THEME)
                .subscribe {
                    on<LightDarkHandler>().setLight(it)
                })

        mixedAdapter = MixedHeaderAdapter(on)
        recyclerView.adapter = mixedAdapter

        on<DisposableHandler>().add( on<StoreHandler>().store.box(Notification::class).query()
                .sort(on<SortHandler>().sortNotifications())
                .build()
                .subscribe()
                .on(AndroidScheduler.mainThread())
                .observer { setNotifications(it) })

        val distance = .12f

        on<DisposableHandler>().add(on<AccountHandler>().changes(ACCOUNT_FIELD_PRIVATE)
                .flatMap {
                    on<MapHandler>().onMapIdleObservable()
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cameraPosition ->
                    val groupPreviewQueryBuilder = on<StoreHandler>().store.box(Group::class).query().apply {
                        if (!on<AccountHandler>().privateOnly) {
                            between(Group_.latitude, cameraPosition.target.latitude - distance, cameraPosition.target.latitude + distance)
                                    .and()
                                    .between(Group_.longitude, cameraPosition.target.longitude - distance, cameraPosition.target.longitude + distance)
                                    .or()
                        }
                    }
                            .equal(Group_.isPublic, false)
                    on<DisposableHandler>().add(groupPreviewQueryBuilder
                            .sort(on<SortHandler>().sortGroups(false))
                            .build()
                            .subscribe()
                            .single()
                            .on(AndroidScheduler.mainThread())
                            .observer { setGroups(it) })
                })

        on<DisposableHandler>().add(on<KeyboardVisibilityHandler>().isKeyboardVisible.subscribe { visible ->
            if (visible) {
                recyclerView.setPadding(0, 0, 0, on<KeyboardVisibilityHandler>().lastKeyboardHeight)
            } else {
                recyclerView.setPadding(0, 0, 0, 0)
            }
        })
    }

    private fun setGroups(groups: List<Group>) {
        mixedAdapter.groups = groups.toMutableList()
    }

    private fun setNotifications(notifications: List<Notification>) {
        mixedAdapter.notifications = notifications.toMutableList()
    }

    fun hide() {
        if (layoutManager.findFirstVisibleItemPosition() > 2) {
            recyclerView.scrollToPosition(2)
        }

        recyclerView.smoothScrollToPosition(0)
    }

    fun show(item: GroupToolbarHandler.ToolbarItem) {
        mixedAdapter.content = when (item.value) {
            ContentViewType.HOME_ACTIVITY -> FeedContent.NOTIFICATIONS
            else -> FeedContent.GROUPS
        }
    }
}
