package me.eremkin.lokalise.api

import com.google.gson.GsonBuilder
import me.eremkin.lokalise.ApiConfig
import me.eremkin.lokalise.api.dto.DownloadParams
import me.eremkin.lokalise.api.dto.DownloadResponse
import me.eremkin.lokalise.throwError
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

private interface Api {
    @Headers("content-type:application/json")
    @POST("files/download")
    fun downloadFiles(
        @Header("x-api-token") apiToken: String,
        @Body params: DownloadParams
    ): Call<DownloadResponse>
}

class LocaliseService(private val apiConfig: ApiConfig) {

    private val gson = GsonBuilder().create()

    private val loggingInterceptor: Interceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        //.addInterceptor(loggingInterceptor)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl("https://api.lokalise.co/api2/projects/${apiConfig.projectId}/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(Api::class.java)

    fun downloadFiles(langs: List<String>): DownloadResponse? {

        val response = api.downloadFiles(apiConfig.token, DownloadParams(langs = langs)).execute()
        if (!response.isSuccessful) {
            throwError(response.errorBody()?.string() ?: "Download common error")
        }
        return response.body()
    }
}