package com.example.weather_mvvm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.example.weather_mvvm.api.ApiEnd
import com.example.weather_mvvm.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch (Dispatchers.Main){
            callApi()
        }
    }

    suspend fun callApi(){

        var client : ApiEnd = RetrofitClient.service
        var myValue = client.getCurrentWeather("london").await()

        Toast.makeText(this,myValue.location.toString(), Toast.LENGTH_LONG).show()


    }
}