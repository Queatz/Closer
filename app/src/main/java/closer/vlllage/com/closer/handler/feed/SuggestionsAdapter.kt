package closer.vlllage.com.closer.handler.feed

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import closer.vlllage.com.closer.databinding.SuggestionItemBinding
import closer.vlllage.com.closer.handler.map.MapActivityHandler
import closer.vlllage.com.closer.store.models.Suggestion
import com.queatz.on.On

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
        return SuggestionViewHolder(SuggestionItemBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = suggestions.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.binding.name.text = "\"${suggestion.name}\""

        holder.itemView.setOnClickListener {
            on<MapActivityHandler>().showSuggestionOnMap(suggestion)
        }
    }
}

class SuggestionViewHolder(val binding: SuggestionItemBinding) : RecyclerView.ViewHolder(binding.root)