package closer.vlllage.com.closer.handler.group

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.ResourcesHandler
import closer.vlllage.com.closer.handler.helpers.SortHandler
import closer.vlllage.com.closer.handler.helpers.TimeAgo
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.StoreHandler
import closer.vlllage.com.closer.store.models.Phone
import closer.vlllage.com.closer.store.models.Phone_
import closer.vlllage.com.closer.ui.MaxSizeFrameLayout
import closer.vlllage.com.closer.ui.RevealAnimator
import io.objectbox.query.QueryBuilder

class GroupMessageMentionHandler : PoolMember() {
    private var animator: RevealAnimator? = null
    private var adapter: MentionAdapter? = null
    private var container: MaxSizeFrameLayout? = null

    fun attach(container: MaxSizeFrameLayout, recyclerView: RecyclerView, onMentionClickListener: (Phone) -> Unit) {
        this.container = container
        animator = RevealAnimator(container, (`$`(ResourcesHandler::class.java).resources.getDimensionPixelSize(R.dimen.groupActionCombinedHeight) * 1.5f).toInt())
        adapter = MentionAdapter(this, onMentionClickListener)

        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
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
            val phones = `$`(StoreHandler::class.java).store.box(Phone::class.java).query()
                    .contains(Phone_.name, name.toString(), QueryBuilder.StringOrder.CASE_INSENSITIVE)
                    .greater(Phone_.updated, `$`(TimeAgo::class.java).oneMonthAgo())
                    .sort(`$`(SortHandler::class.java).sortPhones())
                    .build()
                    .find()

            if (phones.isEmpty()) {
                show(false)
            } else {
                show(true)
                adapter!!.setItems(phones)
            }
        }
    }

    fun show(show: Boolean) {
        var show = show
        if (animator == null) {
            return
        }

        if (adapter!!.itemCount < 1) {
            show = false
        }

        if (!show && container!!.visibility == View.VISIBLE) {
            animator!!.show(false, true)
        } else if (show && container!!.visibility == View.GONE) {
            animator!!.show(true, true)
        }
    }
}
