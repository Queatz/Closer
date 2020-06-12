package closer.vlllage.com.closer.extensions

import android.location.Location
import com.google.android.gms.maps.model.LatLng


fun Location.toLatLng() = LatLng(latitude, longitude)