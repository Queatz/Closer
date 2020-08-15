package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import com.queatz.on.On
import kotlinx.android.synthetic.main.menu_modal.view.*

class MenuHandler constructor(private val on: On) {
    fun show(vararg menuOptions: MenuOption, title: String? = null, button: String? = null, buttonCallback: (() -> Unit)? = null) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.menu_modal
            onAfterViewCreated = { alertConfig, view ->
                val menuRecyclerView = view.menuRecyclerView
                menuRecyclerView.layoutManager = LinearLayoutManager(on<ActivityHandler>().activity, RecyclerView.VERTICAL, false)
                val options = mutableListOf<MenuOption>()

                for (option in menuOptions) {
                    if (option.visible) options.add(option)
                }

                menuRecyclerView.adapter = MenuOptionAdapter(options) { menuOption ->
                    menuOption.callback.invoke()
                    alertConfig.dialog?.dismiss()
                }

                view.title.visible = title != null
                view.title.text = title
            }
            positiveButtonCallback = { buttonCallback?.invoke() }
            positiveButton = button ?: on<ResourcesHandler>().resources.getString(R.string.close)
            show()
        }
    }

    class MenuOption(@DrawableRes internal var iconRes: Int,
                     @StringRes internal var titleRes: Int? = null,
                     internal var title: String? = null,
                     internal var callback: () -> Unit) {
        internal var visible = true

        fun visible(visible: Boolean): MenuOption {
            this.visible = visible
            return this
        }
    }
}
