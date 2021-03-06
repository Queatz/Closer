package closer.vlllage.com.closer.handler.helpers

import android.widget.ImageButton
import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.extensions.visible
import closer.vlllage.com.closer.store.models.Group
import closer.vlllage.com.closer.store.models.Quest
import com.queatz.on.On

class GroupScopeHandler constructor(private val on: On) {
    fun setup(group: Group, imageButton: ImageButton) {
        imageButton.visible = true

        when {
            group.direct || group.hasPhone() -> {
                imageButton.setImageResource(R.drawable.ic_person_black_24dp)
                imageButton.setOnClickListener { on<DefaultAlerts>().message(R.string.human_found) }
            }
            group.isPublic -> {
                imageButton.setImageResource(R.drawable.ic_public_black_24dp)
                imageButton.setOnClickListener { on<DefaultAlerts>().message(R.string.public_group_title, R.string.public_group_message) }
            }
            else -> {
                imageButton.setImageResource(R.drawable.ic_group_black_18dp)
                imageButton.setOnClickListener { on<DefaultAlerts>().message(R.string.private_group_title, R.string.private_group_message) }
            }
        }
    }

    fun setup(quest: Quest, imageButton: ImageButton) {
        imageButton.visible = true

        when {
            quest.isPublic -> {
                imageButton.setImageResource(R.drawable.ic_public_black_24dp)
                imageButton.setOnClickListener { on<DefaultAlerts>().message(R.string.public_group_title, R.string.public_group_message) }
            }
            else -> {
                imageButton.setImageResource(R.drawable.ic_group_black_18dp)
                imageButton.setOnClickListener { on<DefaultAlerts>().message(R.string.private_group_title, R.string.private_group_message) }
            }
        }
    }
}
