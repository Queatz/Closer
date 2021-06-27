package closer.vlllage.com.closer.extensions

import android.location.Location
import at.bluesource.choicesdk.maps.common.LatLng


fun Location.toLatLng() = LatLng(latitude, longitude)