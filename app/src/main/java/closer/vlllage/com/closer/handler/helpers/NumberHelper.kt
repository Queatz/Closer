package closer.vlllage.com.closer.handler.helpers

import com.queatz.on.On
import java.text.DecimalFormat

class NumberHelper(private val on: On) {
    private val format = DecimalFormat("#,###.##")

    fun format(number: Number) = format.format(number)!!
}
