package closer.vlllage.com.closer.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesBackend {
    @GET("api")
    fun query(@Query("q") query: String, @Query("lat") latitude: Double, @Query("lon") longitude: Double, @Query("limit") limit: Int, @Query("location_bias_scale") locationBiasScale: Int = 10): Single<GeoJsonResponse>

    @GET("reverse")
    fun reverse(@Query("lat") latitude: Double, @Query("lon") longitude: Double, @Query("limit") limit: Int): Single<GeoJsonResponse>

    companion object {
        const val BASE_URL = "https://photon.komoot.io/"
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
        val name: String? = null,
        @SerializedName("housenumber") val houseNumber: String? = null,
        val street: String? = null
)

data class GeoJsonGeometry constructor(
        val coordinates: GeoJsonCoordinates,
        val type: String
)

typealias GeoJsonCoordinates = Array<Double>