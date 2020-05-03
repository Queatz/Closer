package closer.vlllage.com.closer.store.models

class ReactionCount {
    var reaction: String? = null
    var count: Long = 0
    var preview: List<String> = listOf()

    override fun equals(other: Any?): Boolean {
        return other is ReactionCount &&
                reaction == other.reaction &&
                count == other.count &&
                preview == other.preview
    }

    override fun hashCode(): Int {
        var result = reaction?.hashCode() ?: 0
        result = 31 * result + count.hashCode()
        result = 31 * result + preview.hashCode()
        return result
    }
}

