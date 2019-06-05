package org.rnazarevych.lokalise.api

import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.rnazarevych.lokalise.api.converters.TranslationsResponseConverter
import org.rnazarevych.lokalise.api.dto.TranslationsResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface LokaliseApi {

    @Multipart
    @POST("project/import")
    fun importFile(@Part("api_token") apiToken: RequestBody,
                   @Part("id") id: RequestBody,
                   @Part file: MultipartBody.Part,
                   @Part("lang_iso") language: RequestBody,
                   @Part("replace_breaks") replace_breaks: RequestBody
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("project/export")
    fun exportProjectData(
            @Field("api_token") apiToken: String,
            @Field("id") id: String,
            @Field("type") type: String = "xml",
            @Field("langs") langs: List<String>? = null
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("string/list")
    fun fetchTranslations(
            @Field("api_token") apiToken: String,
            @Field("id") id: String,
            @Field("langs") type: String,
            @Field("platform_mask") platforms: Int = 2
    ): Call<TranslationsResponse>

}

object Api {

    private val gson = GsonBuilder()
            .registerTypeAdapter(
                TranslationsResponse::class.java,
                TranslationsResponseConverter()
            )
            .create()

    private val loggingInterceptor: Interceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    private val client = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .build()

    val api: LokaliseApi = Retrofit.Builder()
            .baseUrl("https://api.lokalise.co/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(LokaliseApi::class.java)

}