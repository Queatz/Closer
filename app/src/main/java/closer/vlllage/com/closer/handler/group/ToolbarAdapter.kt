package closer.vlllage.com.closer.handler.group

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.helpers.DisposableHandler
import closer.vlllage.com.closer.handler.helpers.LightDarkHandler
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import com.queatz.on.On
import io.reactivex.disposables.Disposable

internal class ToolbarAdapter(on: On) : PoolRecyclerAdapter<ToolbarAdapter.ToolbarViewHolder>(on) {

    var items = mutableListOf<GroupToolbarHandler.ToolbarItem>()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolbarViewHolder {
        return ToolbarViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.toolbar_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ToolbarViewHolder, position: Int) {
        val item = items[position]

        viewHolder.button.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, item.icon, 0, 0
        )

        viewHolder.button.setText(item.name)

        viewHolder.button.setOnClickListener(item.onClickListener)

        viewHolder.lightDarkSubscription = on<LightDarkHandler>().onLightChanged.subscribe {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                viewHolder.button.compoundDrawableTintList = it.tint
            }

            viewHolder.button.setTextColor(it.text)

            viewHolder.button.setBackgroundResource(it.clickableBackground)
        }
    }

    override fun onViewRecycled(holder: ToolbarViewHolder) {
        on<DisposableHandler>().dispose(holder.lightDarkSubscription)
    }

    override fun getItemCount() = items.size

    internal class ToolbarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.button)
        lateinit var lightDarkSubscription: Disposable
    }
}
