package closer.vlllage.com.closer.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class RecyclerViewHeader {

    private var headerViewHolder: RecyclerView.ViewHolder? = null
    private var footerViewHolder: RecyclerView.ViewHolder? = null
    private var recyclerView: RecyclerView? = null
    private var pad: Int = 0
    private var lastGoodExtend: Int = 0

    private var originalHeaderPadding: Int = 0
    private var originalFooterPadding: Int = 0

    private val layoutChangeListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        setHeaderMargin()
    }

    fun onBind(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == recyclerView!!.adapter!!.itemCount - 1) {
            originalFooterPadding = holder.itemView.paddingBottom

            holder.itemView.setPaddingRelative(
                    holder.itemView.paddingStart,
                    holder.itemView.paddingTop,
                    holder.itemView.paddingEnd,
                    holder.itemView.paddingBottom * 2
            )

            footerViewHolder = holder
            footerViewHolder?.itemView?.addOnLayoutChangeListener(layoutChangeListener)
            setHeaderMargin()
        }

        if (position == 0) {
            originalHeaderPadding = holder.itemView.paddingTop

            holder.itemView.setPaddingRelative(
                    holder.itemView.paddingStart,
                    holder.itemView.paddingTop * 2,
                    holder.itemView.paddingEnd,
                    holder.itemView.paddingBottom
            )

            headerViewHolder = holder
            headerViewHolder?.itemView?.addOnLayoutChangeListener(layoutChangeListener)
            setHeaderMargin()
        }
    }

    fun onRecycled(holder: RecyclerView.ViewHolder) {
        if (holder === headerViewHolder) {
            headerViewHolder?.itemView?.removeOnLayoutChangeListener(layoutChangeListener)
            holder.itemView.setPaddingRelative(
                    holder.itemView.paddingStart,
                    originalHeaderPadding,
                    holder.itemView.paddingEnd,
                    holder.itemView.paddingBottom
            )

            headerViewHolder = null
            val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            holder.itemView.layoutParams = params
        }

        if (holder === footerViewHolder) {
            footerViewHolder?.itemView?.removeOnLayoutChangeListener(layoutChangeListener)
            holder.itemView.setPaddingRelative(
                    holder.itemView.paddingStart,
                    holder.itemView.paddingTop,
                    holder.itemView.paddingEnd,
                    originalFooterPadding
            )

            footerViewHolder = null
        }
    }

    fun attach(recyclerView: RecyclerView, pad: Int) {
        this.pad = pad
        this.recyclerView = recyclerView
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        recyclerView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (top != oldTop || bottom != oldBottom) {
                setHeaderMargin()
            }
        }
    }

    private fun setHeaderMargin() {
        if (headerViewHolder == null) {
            return
        }

        val params = headerViewHolder!!.itemView.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = if (recyclerView == null) 0 else recyclerView!!.height - pad + extend()
        headerViewHolder!!.itemView.layoutParams = params
    }

    private fun extend(): Int {
        if (footerViewHolder == null) {
            lastGoodExtend = 0
            return 0
        }

        if (footerViewHolder!!.itemView.bottom == 0) {
            lastGoodExtend = 0
            return 0
        }

        lastGoodExtend = max(lastGoodExtend, recyclerView!!.height - footerViewHolder!!.itemView.bottom)

        return lastGoodExtend
    }
}
