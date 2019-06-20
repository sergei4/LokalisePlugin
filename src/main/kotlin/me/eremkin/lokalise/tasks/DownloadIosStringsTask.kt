package me.eremkin.lokalise.tasks

import me.eremkin.lokalise.*
import me.eremkin.lokalise.api.IosLokalizeApi2
import me.eremkin.lokalise.api.dto.DownloadParams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.util.zip.ZipFile

open class DownloadIosStringsTask : DefaultTask() {

    @Input
    lateinit var buildFolder: File

    @Input
    lateinit var projectFolder: File

    @Input
    lateinit var apiConfig: ApiConfig

    @Input
    lateinit var downloadsConfigs: List<IosDownloadConfig>

    init {
        group = taskGroup
    }

    @TaskAction
    fun download() {
        if (downloadsConfigs.size == 0) {
            println("Warning: there isn't at least one section {langs}")
            return
        }

        val tmpFolder = File(buildFolder, "tmp/lokalise-plugin").apply { mkdirs() }

        val langParam = mutableListOf<String>()
        downloadsConfigs.all {
            langParam.add(it.lokaliseLang)
        }

        println("Downloading translations from lokalise...")
        val response = IosLokalizeApi2.api.downloadFiles(apiConfig.token, DownloadParams(format = "strings", langs = langParam)).execute()

        if (!response.isSuccessful) {
            throw RuntimeException(response.errorBody()?.string())
        } else {
            println("Download completed successful")
            response.body()?.let {
                println("Start unzip lokalize archive")
                File(tmpFolder, "Localizable.strings.zip").createFileIfNotExist {
                    URL(it.bundleUrl).openStream().copyTo(outputStream())

                    ZipFile(this).use { zip ->
                        zip.entries().asSequence().forEach { zipEntry ->
                            if (!zipEntry.isDirectory) {
                                val langFile = zipEntry.name.split("/").last()
                                zip.getInputStream(zipEntry).copyTo(File(tmpFolder, langFile).outputStream())
                                println("Found localization file $langFile")
                            }
                        }
                    }
                    delete()
                }
                println("Start apply translations")
                downloadsConfigs.all { downloadConfig ->
                    println("Locale: ${downloadConfig.lokaliseLang}")
                    File(projectFolder, downloadConfig.path).let { langFolder ->
                        if (langFolder.exists()) {
                            val langCode = if (downloadConfig.langCode != "") downloadConfig.langCode else downloadConfig.lokaliseLang
                            File(tmpFolder, langCode + ".strings").run {
                                if (exists()) {
                                    val resultFile = File(langFolder, "Localizable.strings")
                                    copyTo(resultFile, true)
                                    println("Info: translation applied successful")
                                } else {
                                    println("Warning: there isn't transation file ${this.name} for lang: ${downloadConfig.lokaliseLang}")
                                }
                            }
                        } else {
                            println("Warning: there isn't destination folder ${langFolder.absolutePath} for lokalise locale: ${downloadConfig.lokaliseLang}")
                        }
                    }
                    true
                }
            }
        }
    }
}