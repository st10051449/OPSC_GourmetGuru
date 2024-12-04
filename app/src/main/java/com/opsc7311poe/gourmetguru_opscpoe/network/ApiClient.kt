package com.opsc7311poe.gourmetguru_opscpoe.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

object ApiClient {
    //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 1 October 2024].
    private const val BASE_URL = "http://gourmet-guru-rest-api-hs.onrender.com/api/"
    //https://gourmet-guru-rest-api-hs.onrender.com/api/

    // Increase timeout settings for OkHttp client
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(60, TimeUnit.SECONDS) // Read timeout
        .writeTimeout(60, TimeUnit.SECONDS) // Write timeout
        .build()

    // Retrofit instance
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // Attach OkHttp client with custom timeout
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }
}