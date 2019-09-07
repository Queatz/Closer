package closer.vlllage.com.closer.handler.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.store.models.Suggestion
import com.queatz.on.On
import kotlinx.android.synthetic.main.suggestion_item.view.*

class SuggestionsAdapter(private val on: On) : RecyclerView.Adapter<SuggestionViewHolder>() {

    var suggestions = mutableListOf<Suggestion>()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].id == value[newItemPosition].id
                }

                override fun getOldListSize() = field.size
                override fun getNewListSize() = value.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].name == value[newItemPosition].name
                }
            })
            field.clear()
            field.addAll(value)
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        return SuggestionViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.suggestion_item, parent, false))
    }

    override fun getItemCount() = suggestions.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.itemView.name.text = "\"${suggestion.name}\""

        holder.itemView.setOnClickListener {
            on<MapActivityHandler>().showSuggestionOnMap(suggestion)
        }
    }
}

class SuggestionViewHolder(view: View) : RecyclerView.ViewHolder(view)