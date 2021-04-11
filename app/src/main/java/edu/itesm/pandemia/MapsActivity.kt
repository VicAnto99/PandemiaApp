package edu.itesm.pandemia

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


data class Pais(
    var nombre: String,
    var latitude: Double,
    var longitude: Double,
    var casos: Double,
    var recuperados: Double,
    var defunciones: Double,
    var pruebas: Double
)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val url = "https://disease.sh/v3/covid-19/countries"
        // "https://gist.githubusercontent.com/VicAnto99/6222d880ba1fbc4ec91f31b3355b8718/raw/7d41de5c5c32db2a8ffb70a47d43600f4bfd79a8/db.json"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        cargaDatos()
        getCountries()
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
    fun viewPrue(view: View){
        mMap.clear()
        data.sortByDescending {
            it.pruebas
        }
        var i = 0
        for(pais in data){
            if(i < 10){
                mMap.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.prueba2))
                        .position(LatLng(pais.latitude, pais.longitude))
                        .title(pais.nombre)
                )
                i = 1 + i
            }
        }
    }
    fun viewCaso(view: View){
        mMap.clear()
        data.sortBy {
            it.casos
        }
        var i = 0
        for(pais in data){
            if(i < 10){
                mMap.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.casos))
                        .position(LatLng(pais.latitude, pais.longitude))
                        .title(pais.nombre)
                )
                i = 1 + i
            }
        }
    }
    fun viewDefu(view: View){
        mMap.clear()
        data.sortByDescending {
            it.defunciones
        }
        var i = 0
        for(pais in data){
            if(i < 10){
                mMap.addMarker(
                    MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.muerte))
                        .position(LatLng(pais.latitude, pais.longitude))
                        .title(pais.nombre)
                )
                i = 1 + i
            }
        }
    }
    fun viewData(view: View){
        mMap.clear()
        for(pais in data){
            mMap.addMarker(
                MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(LatLng(pais.latitude, pais.longitude))
                    .title(pais.nombre)
            )
        }
    }
    fun viewRetrofit(view: View){
        mMap.clear()
        for(pais in paisesGson){
            mMap.addMarker(
                MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .position(LatLng(pais?.countryInfo.lat ?: 0.0, pais?.countryInfo.long ?: 0.0))
                    .title(pais.nombre)
            )
        }
    }
    private val data = mutableListOf<Pais>()
    fun cargaDatos (){
        val requestQueue = Volley.newRequestQueue(this)
        val peticion = JsonArrayRequest(Request.Method.GET, url, null, Response.Listener {
            val jsonArray = it
            for (i in 0 until jsonArray.length()) {
                val pais = jsonArray.getJSONObject(i)
                val nombre = pais.getString("country")
                val countryInfoData = pais.getJSONObject("countryInfo")
                val latitude = countryInfoData.getDouble("lat")
                val longitude = countryInfoData.getDouble("long")
                val casos = pais.getDouble("cases")
                val recuperados = pais.getDouble("recovered")
                val defunciones = pais.getDouble("deaths")
                val pruebas = pais.getDouble("tests")
                val paisObject = Pais(
                    nombre,
                    latitude,
                    longitude,
                    casos,
                    recuperados,
                    defunciones,
                    pruebas
                )
                data.add(paisObject)
            }
        }, Response.ErrorListener {

        })
        requestQueue.add(peticion)
    }
    private fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://disease.sh/v3/covid-19/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private lateinit var paisesGson : ArrayList<PaisGson>
    private fun getCountries(){
        val callToService = getRetrofit().create(APIService::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            val responseFromService = callToService.getCountries()
            runOnUiThread { paisesGson = responseFromService.body() as ArrayList<PaisGson>
                if(responseFromService.isSuccessful){
                    Toast.makeText(applicationContext, "Datos obtenidos", Toast.LENGTH_LONG).show()
                }
                else{
                    Toast.makeText(applicationContext, "Error!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}