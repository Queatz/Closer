package closer.vlllage.com.closer.handler.group

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import closer.vlllage.com.closer.pool.PoolRecyclerAdapter
import closer.vlllage.com.closer.store.models.GroupMessage
import com.queatz.on.On

class GroupMessagesAdapter(on: On) : PoolRecyclerAdapter<GroupMessageViewHolder>(on) {

    private var groupMessages = listOf<GroupMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMessageViewHolder {
        return on<GroupMessageHelper>().createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: GroupMessageViewHolder, position: Int) {
        on<GroupMessageHelper>().onBind(groupMessages[position], if (position < groupMessages.size - 2) groupMessages[position + 1] else null, holder)
    }

    override fun onViewRecycled(holder: GroupMessageViewHolder) {
        on<GroupMessageHelper>().recycleViewHolder(holder)
    }

    override fun getItemCount() = groupMessages.size

    fun setGroupMessages(groupMessages: List<GroupMessage>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = this@GroupMessagesAdapter.groupMessages.size
            override fun getNewListSize() = groupMessages.size

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return on<GroupMessageHelper>().areItemsTheSame(
                        this@GroupMessagesAdapter.groupMessages[oldPosition],
                        groupMessages[newPosition]
                )
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return on<GroupMessageHelper>().areContentsTheSame(
                        this@GroupMessagesAdapter.groupMessages[oldPosition],
                        groupMessages[newPosition]
                )
            }
        })

        this.groupMessages = groupMessages
        diffResult.dispatchUpdatesTo(this)
    }
}
