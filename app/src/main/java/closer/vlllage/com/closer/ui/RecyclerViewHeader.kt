package closer.vlllage.com.closer.ui

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.queatz.on.On
import kotlin.math.max

class RecyclerViewHeader(private val on: On) {

    var headerMargin: Int = 0
        private set
    private var headerViewHolder: RecyclerView.ViewHolder? = null
    private var footerViewHolder: RecyclerView.ViewHolder? = null
    private var recyclerView: RecyclerView? = null
    private var pad: Int = 0

    private var originalHeaderPadding: Int = 0
    private var originalFooterPadding: Int = 0

    private var enabled: Boolean = true

    private val headerLayoutChangeListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        setHeaderMargin()
    }

    private val footerLayoutChangeListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
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
            footerViewHolder?.itemView?.addOnLayoutChangeListener(footerLayoutChangeListener)
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
            headerViewHolder?.itemView?.addOnLayoutChangeListener(headerLayoutChangeListener)
            setHeaderMargin()
        }

        if (recyclerView!!.adapter!!.itemCount == 1) setHeaderMargin()
    }

    fun onRecycled(holder: RecyclerView.ViewHolder) {
        if (holder === headerViewHolder) {
            headerViewHolder?.apply {
                itemView.removeOnLayoutChangeListener(headerLayoutChangeListener)
                itemView.setPaddingRelative(
                        itemView.paddingStart,
                        originalHeaderPadding,
                        itemView.paddingEnd,
                        itemView.paddingBottom
                )
            }

            val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            holder.itemView.layoutParams = params

            headerViewHolder = null
            setHeaderMargin()
        }

        if (holder === footerViewHolder) {
            footerViewHolder?.apply {
                itemView.removeOnLayoutChangeListener(footerLayoutChangeListener)
                itemView.setPaddingRelative(
                        itemView.paddingStart,
                        itemView.paddingTop,
                        itemView.paddingEnd,
                        originalFooterPadding
                )
            }

            footerViewHolder = null
            setHeaderMargin()
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
        recyclerView.doOnAttach {  }
    }

    private fun setHeaderMargin() {
        if (headerViewHolder == null || recyclerView == null || !enabled) {
            footerViewHolder?.itemView?.updateLayoutParams<RecyclerView.LayoutParams> {
                bottomMargin = 0
            }

            return
        }

        val r = Rect()

        recyclerView?.getGlobalVisibleRect(r)

        val params = headerViewHolder!!.itemView.layoutParams as ViewGroup.MarginLayoutParams
        val m = r.height() - pad
        headerMargin = m + (if (headerViewHolder === footerViewHolder && recyclerView!!.adapter!!.itemCount > 1) 0 else (footerViewHolder?.itemView?.bottom?.let { max(0, r.height() - (m + (it - headerViewHolder!!.itemView.top))) } ?: 0))
        params.topMargin = headerMargin
        headerViewHolder?.itemView?.layoutParams = params

        footerViewHolder?.itemView?.updateLayoutParams<RecyclerView.LayoutParams> {
            bottomMargin = recyclerView?.measuredHeight ?: 0
        }

        footerViewHolder?.itemView?.post {
            footerViewHolder?.itemView?.updateLayoutParams<RecyclerView.LayoutParams> {
                bottomMargin = 0
            }
        }

        recyclerView?.postInvalidate()
        headerViewHolder?.itemView?.postInvalidate()
        footerViewHolder?.itemView?.postInvalidate()
    }

    fun enable(enabled: Boolean) {
        this.enabled = enabled
    }
}
