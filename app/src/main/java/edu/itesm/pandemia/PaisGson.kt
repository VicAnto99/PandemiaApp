package edu.itesm.pandemia

import com.google.gson.annotations.SerializedName

data class PaisGson(
    @SerializedName("country")
    var nombre: String?,
    var countryInfo: CountryInfo,
    var cases:Double?,
    var recovered:Double?
)
data class CountryInfo(
    var lat:Double?,
    var long:Double?
)
