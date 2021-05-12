package me.eremkin.lokalise.tasks.android2

import me.eremkin.lokalise.Lang
import me.eremkin.lokalise.TranslationsUpdateConfig
import me.eremkin.lokalise.api.LocaliseService
import me.eremkin.lokalise.createFileIfNotExist
import me.eremkin.lokalise.tasks.TranslationUpdater
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import java.util.zip.ZipFile

open class DownloadAndroidStringsTask : DefaultTask() {

    lateinit var localiseService: LocaliseService

    lateinit var buildFolder: File

    lateinit var config: TranslationsUpdateConfig

    val langMap = mapOf(
        "he" to "iw"
    )

    private fun validate(): Boolean {
        if (config.langs.size == 0) {
            println("Warning: there isn't section {langs}")
            return false
        }
        return true
    }

    @TaskAction
    fun download() {
        if (!validate()) {
            return
        }

        val recourseFolder = File(config.resPath)
        val tmpFolder = File(buildFolder, "tmp/lokalise-plugin").apply { mkdirs() }

        val langParam = mutableListOf<String>()
        config.langs.forEach { langParam.add(it.lokaliseLang) }

        println("Downloading translations from lokalise...")
        localiseService.downloadFiles(langParam)?.let {
            println("Unzip translations")
            unzip(bundleUrl = it.bundleUrl, tmpFolder = tmpFolder)
            println("Start apply translations...")
            applyTranslations(config.langs, tmpFolder, recourseFolder)
        }
    }

    private fun unzip(bundleUrl: String, tmpFolder: File) {
        File(tmpFolder, "strings.zip").createFileIfNotExist {
            URL(bundleUrl).openStream().copyTo(outputStream())
            ZipFile(this).use { zip ->
                zip.entries().asSequence().forEach { zipEntry ->
                    if (!zipEntry.isDirectory) {
                        zip.getInputStream(zipEntry)
                            .copyTo(File(tmpFolder, zipEntry.name.split("/").last()).outputStream())
                    }
                }
            }
            delete()
        }
    }

    private fun applyTranslations(langs: Set<Lang>, tmpFolder: File, recourseFolder: File) {
        langs.forEach() { lang ->
            println("Locale: ${if (lang.androidLang.isEmpty()) "default locale" else lang.androidLang}")
            findLangFile(lang.lokaliseLang, tmpFolder)?.let {
                applyLang(
                    srcFile1 = it,
                    destFile = androidStringsFile(recourseFolder, lang.androidLang),
                    updateStrategy = lang.updateStrategy,
                    forceSetRtl = lang.forceSetRTL
                )
            }
        }
    }

    private fun findLangFile(localiseLang: String, tmpFolder: File): File? {
        var langIso = localiseLang.apply {
            replace("_", "-r")
        }
        langMap.forEach {
            langIso = langIso.replace(it.key, it.value)
        }
        val langFile = File(tmpFolder, "$langIso.xml")
        if (!langFile.exists()) {
            println("File for $langIso doesn't exist ")
            return null
        }
        return langFile
    }

    private fun androidStringsFile(androidResources: File, androidLang: String): File {
        val langFolderName = if (androidLang.isEmpty()) "values" else "values-$androidLang"
        val langFolder = File(androidResources, langFolderName)
        langFolder.createFileIfNotExist()
        return File(langFolder, "strings.xml")
    }

    private fun applyLang(srcFile1: File, destFile: File, updateStrategy: String, forceSetRtl: Boolean) {
        val srcFile = normalizeSourceFile(srcFile1, forceSetRtl)
        when (updateStrategy) {
            "replace" -> srcFile.copyTo(destFile, true)
            "merge" -> mergeFile(destFile, srcFile)
            else -> println("Warning: ${destFile}: unknown update strategy")
        }
        println("Info: translation applied successful")
    }

    private fun mergeFile(oldFile: File, newFile: File) {
        if (!oldFile.exists()) {
            newFile.copyTo(oldFile)
        } else {
            TranslationUpdater.mergeStrings(oldFile, newFile)
        }
    }

    private fun normalizeSourceFile(source: File, forceSetRtl: Boolean): File {
        if (!forceSetRtl) {
            return source
        }
        return File("${source.absolutePath}.rtl").apply {
            if (exists()) delete()
            source.forEachLine { line ->
                if (line.trim().startsWith("<string")) {
                    appendText(line.replaceFirst(">", ">\\u200f"))
                } else {
                    appendText(line)
                }
                appendText("\n")
            }
        }
    }
}