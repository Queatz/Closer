package closer.vlllage.com.closer.handler.helpers

import android.view.View
import android.widget.ImageButton

import closer.vlllage.com.closer.R
import closer.vlllage.com.closer.pool.PoolMember
import closer.vlllage.com.closer.store.models.Group

class GroupScopeHandler : PoolMember() {
    fun setup(group: Group, imageButton: ImageButton) {
        imageButton.visibility = View.VISIBLE

        if (group.hasPhone()) {
            imageButton.setImageResource(R.drawable.ic_person_black_24dp)
            imageButton.setOnClickListener { `$`(DefaultAlerts::class.java).message(R.string.huaman_found) }
        } else if (group.isPublic) {
            imageButton.setImageResource(R.drawable.ic_public_black_24dp)
            imageButton.setOnClickListener { `$`(DefaultAlerts::class.java).message(R.string.public_group_title, R.string.public_group_message) }
        } else {
            imageButton.setImageResource(R.drawable.ic_lock_black_18dp)
            imageButton.setOnClickListener { `$`(DefaultAlerts::class.java).message(R.string.private_group_title, R.string.private_group_message) }
        }
    }
}
