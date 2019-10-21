package closer.vlllage.com.closer.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesBackend {
    @GET("api")
    fun query(@Query("q") query: String, @Query("lat") latitude: Double, @Query("lon") longitude: Double, @Query("limit") limit: Int): Single<GeoJsonResponse>

    companion object {
        const val BASE_URL = "https://photon.komoot.de/"
    }
}

data class GeoJsonResponse constructor(
    val features: List<GeoJsonFeature>
)

data class GeoJsonFeature constructor(
        val geometry: GeoJsonGeometry,
        val properties: GeoJsonProperties
)

data class GeoJsonProperties constructor(
        val name: String
)

data class GeoJsonGeometry constructor(
        val coordinates: GeoJsonCoordinates,
        val type: String
)

typealias GeoJsonCoordinates = Array<Double>