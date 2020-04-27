package closer.vlllage.com.closer.handler.post

class CreatePostAction(
        val action: CreatePostActionType,
        val position: Int,
        val callback: (() -> Unit)? = null
)

enum class CreatePostActionType {
    AddHeading,
    AddText,
    AddPhoto,
    AddGroupAction,
    EditPhoto,
    Delete,
    Post,
}