package me.eremkin.lokalise.api

import com.google.gson.GsonBuilder
import me.eremkin.lokalise.api.dto.DownloadParams
import me.eremkin.lokalise.api.dto.DownloadResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface LokaliseApi2 {
    @Headers("content-type:application/json")
    @POST("files/download")
    fun downloadFiles(
        @Header("x-api-token") apiToken: String,
        @Body params : DownloadParams
    ): Call<DownloadResponse>
}

object Api2 {
    private val gson = GsonBuilder().create()

    private val loggingInterceptor: Interceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        //.addInterceptor(loggingInterceptor)
        .build()

    lateinit var api: LokaliseApi2

    fun configure(projectId: String) {
        api = Retrofit.Builder()
            .baseUrl("https://api.lokalise.co/api2/projects/$projectId/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LokaliseApi2::class.java)
    }
}