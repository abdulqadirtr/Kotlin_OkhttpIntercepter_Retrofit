package com.example.weather_mvvm.api

import com.example.weather_mvvm.data.WeatherResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query



interface ApiEnd {
    //http://api.weatherstack.com/current?access_key=a5a76da3539e55d7a8fd721dc51bc07f&query=islamabad

    // The api's Data  take someTime soo that's why we will be using Defferend
    @GET("current")
    fun getCurrentWeather(@Query("query") mLocation: String ): Deferred <WeatherResponse>

}