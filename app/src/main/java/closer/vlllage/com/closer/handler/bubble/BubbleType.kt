package closer.vlllage.com.closer.handler.bubble

enum class BubbleType
    constructor(val score: Int) {

    STATUS(2),
    MENU(1),
    SUGGESTION(5),
    EVENT(3),
    PHYSICAL_GROUP(4),
    PROXY(0)
}
