package closer.vlllage.com.closer.extensions

import android.content.Context
import android.util.TypedValue

fun Int.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    toFloat(),
    context.resources.displayMetrics
).toInt()
