package com.example.weather_mvvm.api

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.CoroutineScope
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


const val APIKEY = "a5a76da3539e55d7a8fd721dc51bc07f"

object RetrofitClient {


    val service by lazy {

        val requestInterceptor = Interceptor { chain ->
            val url = chain.request()
                .url()
                .newBuilder()
                .addQueryParameter("access_key", APIKEY)
                .build()
            val request = chain.request()
                .newBuilder()
                .url(url)
                .build()
            return@Interceptor chain.proceed(request)

        }

        val okhttp = OkHttpClient.Builder().addInterceptor(requestInterceptor).build()

        Retrofit.Builder()
            .client(okhttp)
            .baseUrl("http://api.weatherstack.com/")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(ApiEnd::class.java)
    }

}