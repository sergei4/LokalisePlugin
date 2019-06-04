package me.eremkin.lokalise.tasks

import me.eremkin.lokalise.ApiConfig
import me.eremkin.lokalise.TranslationsUpdateConfig
import me.eremkin.lokalise.createFileIfNotExist
import me.eremkin.lokalise.createFolderIfNotExist
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.rnazarevych.lokalise.api.Api2
import org.rnazarevych.lokalise.api.dto.DownloadParams
import java.io.File
import java.net.URL
import java.util.zip.ZipFile

open class DownloadTranslationsTask : DefaultTask() {

    @Input
    lateinit var buildFolder: File

    @Input
    lateinit var apiConfig: ApiConfig

    @Input
    lateinit var config: TranslationsUpdateConfig

    init {
        group = me.eremkin.lokalise.taskGroup
    }

    @TaskAction
    fun download() {
        if(config.langs.size == 0){
            println("Warning: there isn't section {langs}")
            return
        }

        val recourseFolder = File(config.resPath)
        val tmpFolder = File(buildFolder, "tmp/lokalise-plugin").apply { mkdirs() }

        val langParam = mutableListOf<String>()
        config.langs.all {
            langParam.add(it.lokaliseLang)
        }

        println("Downloading translations from lokalise...")
        val response = Api2.api.downloadFiles(apiConfig.token, DownloadParams(langs = langParam)).execute()

        if (!response.isSuccessful) {
            throw RuntimeException(response.errorBody()?.string())
        } else {
            println("Download completed successful")
            response.body()?.let {
                File(tmpFolder, "translations.zip").createFileIfNotExist {
                    URL(it.bundleUrl).openStream().copyTo(outputStream())

                    ZipFile(this).use { zip ->
                        zip.entries().asSequence().forEach { zipEntry ->
                            if (!zipEntry.isDirectory) {
                                zip.getInputStream(zipEntry).copyTo(File(tmpFolder, zipEntry.name.split("/").last()).outputStream())
                            }
                        }
                    }

                    delete()
                }
                println("Start apply translations")
                config.langs.all { lang ->
                    println("Locale: ${lang.androidLang}")
                    val langValueFolder = if (lang.androidLang.isEmpty()) "values" else "values-" + lang.androidLang
                    File(recourseFolder, langValueFolder).let { recourseFolder ->
                        recourseFolder.createFolderIfNotExist()
                        val langIso = lang.lokaliseLang.replace("_", "-r")
                        File(tmpFolder, langIso + ".xml").run {
                            if (exists()) {
                                val stringsXmlFile = File(recourseFolder, "strings.xml")
                                when (lang.updateStrategy) {
                                    "replace" -> copyTo(stringsXmlFile, true)
                                    "merge" -> {
                                        if (!stringsXmlFile.exists()) {
                                            copyTo(stringsXmlFile, true)
                                        } else {
                                            //Todo: merge local and remote files
                                        }
                                    }
                                    else -> {
                                        println("Warning: ${lang.name}: unknown update strategy")
                                    }
                                }
                                println("Info: translation applied successful")
                            } else {
                                println("Warning: there isn't transation file ${this.name} for lokalise locale: ${lang.lokaliseLang}")
                            }
                        }
                    }
                }
            }
        }
    }
}