package me.eremkin.lokalise.tasks.ios

import me.eremkin.lokalise.api.LocaliseService
import me.eremkin.lokalise.api.dto.DownloadParams
import me.eremkin.lokalise.config.IosDownloadConfig
import me.eremkin.lokalise.unzip
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

open class DownloadIosStringsTask : DefaultTask() {

    lateinit var localiseService: LocaliseService

    lateinit var buildFolder: File

    lateinit var projectFolder: File

    lateinit var downloadsConfigs: List<IosDownloadConfig>

    @TaskAction
    fun download() {
        if (downloadsConfigs.isEmpty()) {
            println("Warning: there isn't at least one section {langs}")
            return
        }

        val tmpFolder = File(buildFolder, "tmp/lokalise-plugin").also { it.mkdirs() }

        val langParam = mutableListOf<String>().apply {
            downloadsConfigs.forEach { add(it.lokaliseLang) }
        }

        println("Downloading translations from lokalise...")
        localiseService.downloadFiles(DownloadParams(format = "strings", langs = langParam))?.let {
            println("Unzip translations")
            unzip(bundleUrl = it.bundleUrl, targetFolder = tmpFolder)
        }
        println("Start apply translations...")
        applyTranslations(downloadsConfigs, tmpFolder)
    }

    private fun applyTranslations(downloadsConfigs: List<IosDownloadConfig>, tmpFolder: File) {
        downloadsConfigs.forEach { downloadConfig ->
            println("Locale: ${downloadConfig.lokaliseLang}")
            val langFolder = File(projectFolder, downloadConfig.path)
            if (!langFolder.exists()) {
                println("Warning: there isn't destination folder ${downloadConfig.path} for lokalise locale: ${downloadConfig.lokaliseLang}")
                return@forEach
            }
            val langCode =
                if (downloadConfig.langCode.isNotEmpty()) downloadConfig.langCode else downloadConfig.lokaliseLang
            // strings
            File(tmpFolder, "$langCode.strings").run {
                if (exists()) {
                    println("Info: Apply strings file: ${this.name}")
                    val stringsFile = normalizeStringFile(downloadConfig.forceSetRTL)
                        .copyTo(File(langFolder, "Localizable.strings"), true)
                    if (downloadConfig.createInfoPlist) {
                        println("Info: Extract strings into InfoPlist: ${this.name}")
                        File(langFolder, "InfoPlist.strings").apply { if (exists()) delete() }
                            .extractFrom(stringsFile) { it.startsWith("\"NS") }
                    }
                } else {
                    println("Warning: there isn't .strings file $name for lang: ${downloadConfig.lokaliseLang}")
                }
            }
            // stringdict
            File(tmpFolder, "$langCode.stringsdict").run {
                if (exists()) {
                    println("Info: Apply stringsdict file: ${this.name}")
                    normalizeStringdictFile().copyTo(File(langFolder, "Localizable.stringsdict"), true)
                } else {
                    println("Warning: there isn't .stringsdict file $name for lang: ${downloadConfig.lokaliseLang}")
                }
            }
        }
    }

    private fun File.normalizeStringFile(forceSetRtl: Boolean): File {
        val source = this
        return File("${this.absolutePath}.out").apply {
            if (exists()) delete()
            source.forEachLine { inputLine ->
                val outputLine = if (forceSetRtl) {
                    inputLine.replaceFirst("\" = \"", "\" = \"\\U200F")
                } else {
                    inputLine
                }
                appendText("${outputLine.fixNumericPlaceHolder()}\n")
            }
        }
    }

    private fun File.normalizeStringdictFile(): File {
        return this
    }

    private fun String.fixNumericPlaceHolder() = replace(Regex("%\\d+\\\$@"), "%@")

    private fun File.extractFrom(textFile: File, predicate: (String) -> Boolean) {
        textFile.forEachLine { if (predicate(it)) appendText(it) }
    }
}