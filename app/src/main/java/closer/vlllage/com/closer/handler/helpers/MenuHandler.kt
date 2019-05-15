package closer.vlllage.com.closer.handler.helpers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import com.queatz.on.On
import java.util.*

class MenuHandler constructor(private val on: On) {
    fun show(vararg menuOptions: MenuOption) {
        on<AlertHandler>().make().apply {
            layoutResId = R.layout.menu_modal
            onAfterViewCreated = { alertConfig, view ->
                val menuRecyclerView = view.findViewById<RecyclerView>(R.id.menuRecyclerView)
                menuRecyclerView.layoutManager = LinearLayoutManager(on<ActivityHandler>().activity, RecyclerView.VERTICAL, false)
                val options = ArrayList<MenuOption>()

                for (option in menuOptions) {
                    if (option.visible) options.add(option)
                }

                menuRecyclerView.adapter = MenuOptionAdapter(options) { menuOption ->
                    menuOption.callback.invoke()
                    alertConfig.dialog?.dismiss()
                }
            }
            positiveButton = on<ResourcesHandler>().resources.getString(R.string.close)
            show()
        }
    }

    class MenuOption(@DrawableRes internal var iconRes: Int,
                     @StringRes internal var titleRes: Int,
                     internal var callback: () -> Unit) {
        internal var visible = true

        fun visible(visible: Boolean): MenuOption {
            this.visible = visible
            return this
        }
    }
}