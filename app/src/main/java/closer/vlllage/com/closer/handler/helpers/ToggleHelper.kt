package closer.vlllage.com.closer.handler.helpers

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.google.android.material.button.MaterialButtonToggleGroup
import com.queatz.on.On

class ToggleHelper(private val on: On) {
    fun updateToggleButtonWeights(group: MaterialButtonToggleGroup) {
        group.children.forEach {
            it.updateLayoutParams<LinearLayout.LayoutParams> {
                width = if (it.id == group.checkedButtonId) ViewGroup.LayoutParams.WRAP_CONTENT else 0
            }
        }
    }
}