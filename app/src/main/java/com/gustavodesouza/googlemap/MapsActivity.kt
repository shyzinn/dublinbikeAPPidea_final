package com.gustavodesouza.googlemap
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {


    private lateinit var mMap: GoogleMap
    private lateinit var listOfBikeStations: List<BikeStation>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        retrieveFavourites()


    }



    override fun onResume() {
        super.onResume()
        Log.i(getString(R.string.MAPLOGGING), "onResume")

        retrieveFavourites()
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













        try {

            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json))

            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ", e)
        }



        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isRotateGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        // Add a marker in Sydney and move the camera

        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))

        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Only fetch data when map is ready

        getBikeStationJsonData()
        setMarkerListener()

       // addMarkers()


        
    }

    fun getBikeStationJsonData() {
        Log.i(getString(R.string.MAPLOGGING), "Loading JSON data")

        var url = getString(R.string.DUBLIN_BIKE_API_URL) + getString(R.string.DUBLIN_BIKE_API_KEY)

        Log.i(getString(R.string.MAPLOGGING), url)

        //  a request object

        val request = Request.Builder().url(url).build()

        // Create a client

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {


                Log.i(getString(R.string.MAPLOGGING), "http fail")
            }

            override fun onResponse(call: Call, response: Response) {


                Log.i(getString(R.string.MAPLOGGING), "http success")

                // Get the response body
                val body = response.body?.string()


                Log.i(getString(R.string.MAPLOGGING), body)

                // Create a json builder object
                val gson = GsonBuilder().create()

                listOfBikeStations = gson.fromJson(body, Array<BikeStation>::class.java).toList()

                renderListOfBikeStationMarkers()


            }


        })
    }

    fun renderListOfBikeStationMarkers() {

        runOnUiThread {

            listOfBikeStations.forEach {

                val position = LatLng(it.position.lat, it.position.lng)
                var marker1 =



                    mMap.addMarker(

                        MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.iconbikenew)).position(position).title("Marker in ${it.address}")
                    )

                marker1.tag = it.number
                Log.i(getString(R.string.MAPLOGGING), it.address)
            }

            val centreLocation = LatLng(53.349562, -6.278198)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centreLocation, 16.0f))

        }

    }




    fun setMarkerListener() {

        mMap.setOnMarkerClickListener { marker ->
            Log.i(getString(R.string.MAPLOGGING), getString(R.string.MAKERCLICKED))

            Log.i(
                getString(R.string.MAPLOGGING),
                "Marker id (tag) is " + marker.tag.toString()
            )
            if (marker.isInfoWindowShown) {

                marker.hideInfoWindow()
            } else {

                marker.showInfoWindow()
            }

            // Pass in data and add a layout
            val intent = Intent(this, StationActivity::class.java)

           intent.putExtra("lat", marker.position.latitude.toString())
           intent.putExtra("id", marker.tag.toString())
           intent.putExtra("lng", marker.position.longitude.toString())
            startActivity(intent)

            true
        }



    }

    fun addMarkers() {

        val smithfield = LatLng(53.349562, -6.278198)
        var marker1 =
            mMap.addMarker(MarkerOptions().position(smithfield).title("Marker in smithfield"))
        marker1.tag = 42


        val Parnell = LatLng(53.353462, -6.265305)
        var marker2 = mMap.addMarker(MarkerOptions().position(Parnell).title("Marker in Parnell"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Parnell))
        marker2.tag = 30


        val Clonmel = LatLng(53.336021, -6.26298)
        var marker3 = mMap.addMarker(MarkerOptions().position(Clonmel).title("Marker in Clonmel"))

        // List Example
        listOfBikeStations = listOf(
            BikeStation(48, "Excise Walk", Position(53.347777, -6.244239)),
            BikeStation(52, "YORK STREET EAST", Position(53.338755, -6.262003)),
            BikeStation(66, "NEW CENTRAL BANK", Position(53.347122, -6.234749))

        )
        listOfBikeStations.forEach {
            val position = LatLng(it.position.lat, it.position.lng)
            var marker1 =
                mMap.addMarker(MarkerOptions().position(position).title("Marker in ${it.address}"))
                      marker1.tag = it.number
            Log.i(getString(R.string.MAPLOGGING), it.address)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Clonmel, 16.0f))

    }

    fun retrieveFavourites() {
        Log.i(getString(R.string.MAPLOGGING), "Marker Preferences are loaded")
        var prefs =
            getSharedPreferences("com.gustavodesouza.googlemap", Context.MODE_PRIVATE)
        var markers = prefs.getStringSet("stationmarkers", setOf())?.toMutableSet()

       markers?.forEach{m ->  Log.i(getString(R.string.MAPLOGGING), "Favourite Marker: ${m}")}

    }





    override fun onMarkerClick(p0: Marker?): Boolean {




            return true
    }
    }















