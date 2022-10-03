package com.example.weather_mvvm

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.weather_mvvm.PermissionUtils.DEFAULT_LOCATION
import com.example.weather_mvvm.databinding.ActivityMainBinding
import com.example.weather_mvvm.network.ApiInterface
import com.example.weather_mvvm.network.RetrofitClient
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import java.util.*


private const val LOCATION_PERMISSION_REQUEST_CODE =34

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    var cityName: String = ""

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /**  GlobalScope is a singleton scope that returns a completely empty coroutineContext. Since there's no Job associated with it, you cannot cancel it, so its lifecycle is basically "forever".
          * A separate instance of viewModelScope is attached to every instance of ViewModel.
            It runs out when the ViewModel is destroyed.
          * coroutineScope and supervisorScope are suspendable functions that establish their own local scope, run the block you pass to them within that scope,
           and return when all the work inside is done, including all the coroutines launched within their scope.
        **/
        CoroutineScope(Dispatchers.IO).launch {
           if (isNetworkAvailable())
            weatherApi()
        }
    }

   private suspend fun weatherApi() {

        var client: ApiInterface = RetrofitClient.service
        var weatherResponse = client.getCurrentWeatherAsync(cityName.ifEmpty {DEFAULT_LOCATION }).await()

       // to show the value on the main thread
        withContext(Dispatchers.Main) {
            with(binding) {
                temperatureTextView.text = weatherResponse.current?.temperature.toString()
                countryTextView.text = weatherResponse.location?.country.toString()
                Glide.with(this@MainActivity).load(weatherResponse.current?.weatherIcons?.get(0).toString()).into(weatherIconImageView)

                if (cityName.isNotEmpty()) {
                    cityTextView.text = cityName
                } else
                    cityTextView.text = DEFAULT_LOCATION
                    weatherDescriptionTextView.text = weatherResponse.current?.weatherDescriptions?.get(0).toString()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                       if(isNetworkAvailable())
                           setUpLocationListener()
                    }
                }
            }
            else -> {
                PermissionUtils.requestAccessFineLocationPermission(
                    this,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> {
                            setUpLocationListener()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Permission not granted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        // for getting the current location update after every 2 seconds with high accuracy
        val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
           fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        currentLocation = locationResult.lastLocation

                        var lat = currentLocation!!.latitude
                        var long = currentLocation!!.longitude
                        // Few more things we can do here:
                        // For example: Update the location of user on server
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val addresses: List<Address> = geocoder.getFromLocation(lat, long, 1)
                        cityName = addresses[0].getAddressLine(0)
                    }
                },
               Looper.myLooper()!!
            )
}

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}