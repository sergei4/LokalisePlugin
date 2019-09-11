package me.eremkin.lokalise.tasks

import me.eremkin.lokalise.ApiConfig
import me.eremkin.lokalise.IosDownloadConfig
import me.eremkin.lokalise.api.Api2
import me.eremkin.lokalise.api.dto.DownloadParams
import me.eremkin.lokalise.createFileIfNotExist
import me.eremkin.lokalise.taskGroup
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
        val response = Api2.api.downloadFiles(apiConfig.token, DownloadParams(format = "strings", langs = langParam)).execute()

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
                                val tmpLangFile = File(tmpFolder, "$langFile.tmp")
                                val langOutFile = File(tmpFolder, "$langFile.out")

                                zip.getInputStream(zipEntry).copyTo(tmpLangFile.outputStream())
                                println("Found localization file $langFile")

                                langOutFile.writeText("") // could be a comment here
                                tmpLangFile.readLines().all { line ->
                                    val line1 = line.replace(Regex("%\\d+\\\$@"), "%@")
                                    langOutFile.appendText("$line1\n")
                                    true
                                }
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
                            // strings
                            File(tmpFolder, langCode + ".strings.out").run {
                                if (exists()) {
                                    val resultFile = File(langFolder, "Localizable.strings")
                                    copyTo(resultFile, true)
                                    println("Info: translation applied successful")
                                } else {
                                    println("Warning: there isn't transation file ${this.name} for lang: ${downloadConfig.lokaliseLang}")
                                }
                            }
                            // stringdict
                            File(tmpFolder, langCode + ".stringsdict.out").run {
                                if (exists()) {
                                    val resultFile = File(langFolder, "Localizable.stringsdict")
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