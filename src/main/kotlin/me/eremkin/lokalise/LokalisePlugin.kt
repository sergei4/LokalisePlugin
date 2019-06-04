package me.eremkin.lokalise

import com.android.build.gradle.BaseExtension
import groovy.lang.Closure
import me.eremkin.lokalise.tasks.DownloadTranslationsTask
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.rnazarevych.lokalise.api.Api2
import java.io.File

const val taskGroup = "lokalise"

class LokalisePlugin : Plugin<Project> {

    lateinit var config: Config

    override fun apply(project: Project) {

        project.plugins.apply {
            if (findPlugin("com.android.application") == null && findPlugin("com.android.library") == null) {
                throw RuntimeException("You must apply the Android plugin or the Android library plugin before using the lokalise plugin")
            }
        }

        config = project.extensions.create("lokalise", Config::class.java)
        val langs = project.container(Lang::class.java)
        config.translationsUpdateConfig.langs = langs

        project.afterEvaluate {

            val android : BaseExtension = project.extensions.findByName("android") as BaseExtension

            val dirs = android.sourceSets.getByName("main").res.srcDirs
            if(dirs.size == 1){
                config.translationsUpdateConfig.resPath = dirs.iterator().next().absolutePath
            }

            Api2.configure(config.api.projectId)

            with(project.tasks) {
                create("downloadTranslations", DownloadTranslationsTask::class.java) {
                    it.apiConfig = config.api
                    it.config = config.translationsUpdateConfig
                    it.buildFolder = File(project.buildDir.absolutePath)
                }
            }
        }
    }
}

open class Config() {
    val api = ApiConfig()
    fun api(action: Action<in ApiConfig>) = action.execute(api)

    val translationsUpdateConfig = TranslationsUpdateConfig()
    fun translationsUpdateConfig(action: Action<TranslationsUpdateConfig>) = action.execute(translationsUpdateConfig)

//    val stringsUploadConfig = StringsUploadConfig()
//    fun stringsUploadConfig(action: Action<in StringsUploadConfig>) = action.execute(stringsUploadConfig)
//
//    val uploadEntries: MutableList<UploadEntry> = mutableListOf()
//    fun uploadEntry(action: Action<in UploadEntry>) {
//        val newEntry = UploadEntry()
//        action.execute(newEntry)
//        uploadEntries.add(newEntry)
//    }
}

data class ApiConfig(
    var projectId: String = "",
    var token: String = ""
)

class TranslationsUpdateConfig(var resPath: String = "") {

    lateinit var langs: NamedDomainObjectContainer<Lang>

    fun langs(closure: Closure<Lang>) {
        langs.configure(closure)
    }
}

data class Lang(val name: String = "") {
    var androidLang: String = ""
    var lokaliseLang: String = ""
    var updateStrategy: String = "merge"
}

open class StringsUploadConfig {
    val api = ApiConfig()
    fun api(action: Action<in ApiConfig>) = action.execute(api)
    var str: MutableList<String> = mutableListOf()
    val uploadEntries: MutableList<UploadEntry> = mutableListOf()
    fun uploadEntry(action: Action<in UploadEntry>) {
        val newEntry = UploadEntry()
        action.execute(newEntry)
        uploadEntries.add(newEntry)
    }
}

open class UploadEntry(
    var path: String = "",
    var lang: String = ""
)

fun File.createFileIfNotExist(block: (File.() -> Unit)? = null) {
    if (exists()) {
        createNewFile()
    }
    block?.let{
        it(this)
    }
}

fun File.createFolderIfNotExist() {
    if (!this.exists()) {
        this.mkdirs()
    }
}