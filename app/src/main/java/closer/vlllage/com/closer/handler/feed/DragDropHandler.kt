package closer.vlllage.com.closer.handler.feed

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.queatz.on.On

class DragDropHandler(private val on: On) {

    private lateinit var onDrag: (Boolean) -> Unit
    private lateinit var onMoveListener: (from: Int, to: Int) -> Unit

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(UP or
                        DOWN or
                        START or
                        END, 0) {

                    override fun onMove(recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder,
                                        target: RecyclerView.ViewHolder): Boolean {

                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        onMoveListener(from, to)

                        return true
                    }

                    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                        super.onSelectedChanged(viewHolder, actionState)
                        if (actionState == ACTION_STATE_DRAG) {
                            viewHolder?.itemView?.alpha = 0.5f
                            onDrag.invoke(true)
                        } else if (actionState == ACTION_STATE_IDLE) {
                            onDrag.invoke(false)
                        }
                    }

                    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        viewHolder.itemView.alpha = 1.0f
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder,
                                          direction: Int) {}
                }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    fun attach(recyclerView: RecyclerView, onDrag: (Boolean) -> Unit, onMoveListener: (from: Int, to: Int) -> Unit) {
        this.onMoveListener = onMoveListener
        this.onDrag = onDrag
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}
