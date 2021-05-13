package me.eremkin.lokalise.tasks.android

import me.eremkin.lokalise.config.ApiConfig
import me.eremkin.lokalise.config.UploadEntry
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.rnazarevych.lokalise.api.Api
import java.io.File

open class UploadStrings : DefaultTask() {

    lateinit var apiConfig: ApiConfig

    var uploadEntries: List<UploadEntry> = emptyList()

    @TaskAction
    fun upload() {
        uploadEntries.forEach { entry ->
            println("Uploading (${entry.lang})...")

            val file = File(entry.path)
            val requestFile = RequestBody.create(MediaType.parse("application/xml"), file)
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val apiToken = RequestBody.create(MediaType.parse("text/plain"), apiConfig.token)
            val id = RequestBody.create(MediaType.parse("text/plain"), apiConfig.projectId)
            val language = RequestBody.create(MediaType.parse("text/plain"), entry.lang)
            val replaceBreaks = RequestBody.create(MediaType.parse("text/plain"), "true")

            val response = Api.api.importFile(apiToken, id, filePart, language, replaceBreaks).execute()

            if (response.isSuccessful) {
                println(response.body()?.string())
            } else {
                println(response.errorBody()?.string())
            }
        }
    }
}