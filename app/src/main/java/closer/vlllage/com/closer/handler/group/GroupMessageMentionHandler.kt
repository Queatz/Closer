package closer.vlllage.com.closer.handler.group

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import closer.vlllage.com.closer.ui.RevealAnimatorForConstraintLayout
import com.queatz.on.On
import io.objectbox.query.QueryBuilder

class GroupMessageMentionHandler constructor(private val on: On) {
    private var animator: RevealAnimatorForConstraintLayout? = null
    private lateinit var adapter: MentionAdapter
    private lateinit var container: ConstraintLayout

    fun attach(container: ConstraintLayout, recyclerView: RecyclerView, onMentionClickListener: (Phone) -> Unit) {
        this.container = container
        animator = RevealAnimatorForConstraintLayout(container, (on<ResourcesHandler>().resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())
        adapter = MentionAdapter(on, onMentionClickListener)

        recyclerView.layoutManager = object : LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false) {
            init {
            }
        }
        recyclerView.adapter = adapter
    }

    fun showSuggestionsForName(name: CharSequence?) {
        var name = name
        if (name == null) {
            show(false)
        } else {
            if (name[0] == '@') {
                name = name.subSequence(1, name.length)
            }
            val phones = on<StoreHandler>().store.box(Phone::class).query()
                    .contains(Phone_.name, name.toString(), QueryBuilder.StringOrder.CASE_INSENSITIVE)
                    .greater(Phone_.updated, on<TimeAgo>().oneMonthAgo(6))
                    .sort(on<SortHandler>().sortPhones())
                    .build()
                    .find()

            if (phones.isEmpty()) {
                show(false)
            } else {
                show(true)
                adapter.setItems(phones)
            }
        }
    }

    fun show(show: Boolean) {
        var show = show
        if (animator == null) {
            return
        }

        if (adapter.itemCount < 1) {
            show = false
        }

        if (!show && container.visibility == View.VISIBLE) {
            animator!!.show(false, true)
        } else if (show && container.visibility == View.GONE) {
            animator!!.show(true, true)
        }
    }
}
