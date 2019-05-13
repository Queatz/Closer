package closer.vlllage.com.closer.store.models

class ReactionCount {
    var reaction: String? = null
    var count: Long = 0

    override fun equals(other: Any?): Boolean {
        return other is ReactionCount &&
                reaction == other.reaction &&
                count == other.count
    }
}

