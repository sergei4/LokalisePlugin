package me.eremkin.lokalise

import com.android.build.gradle.BaseExtension
import groovy.lang.Closure
import me.eremkin.lokalise.api.Api2
import me.eremkin.lokalise.tasks.DownloadAndroidStringsTask
import me.eremkin.lokalise.tasks.DownloadIosStringsTask
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.rnazarevych.lokalise.UploadEntry
import org.rnazarevych.lokalise.tasks.UploadStrings
import java.io.File

const val taskGroup = "lokalise"
const val lokaliseConfig = "lokalise"

const val exceptionTag = "lokalise-plugin"

class LokalisePlugin : Plugin<Project> {

    lateinit var androidConfig: AndroidConfig

    override fun apply(project: Project) {

        androidConfig = project.extensions.create(lokaliseConfig, AndroidConfig::class.java)
        val langs = project.container(Lang::class.java)
        androidConfig.translationsUpdateConfig.langs = langs

        project.afterEvaluate {

            project.plugins.apply {
                if (findPlugin("com.android.application") == null && findPlugin("com.android.library") == null) {
                    throw RuntimeException("You must apply the Android plugin or the Android library plugin before using the lokalise plugin")
                }
            }

            val android: BaseExtension = project.extensions.findByName("android") as BaseExtension

            val resPath = androidConfig.translationsUpdateConfig.resPath

            if (resPath == "") {
                try {
                    val resDirs = android.sourceSets.getByName("main").res.srcDirs
                    androidConfig.translationsUpdateConfig.resPath = resDirs.iterator().next().absolutePath
                } catch (exeption: Exception) {
                    throw RuntimeException("$exceptionTag: ${exeption.message ?: ""}")
                }
            } else {
                if (!File(resPath).exists()) {
                    throw RuntimeException("$exceptionTag: invalid resource path: $resPath")
                }
            }

            Api2.configure(androidConfig.apiConfig.projectId)

            with(project.tasks) {
                create("downloadAndroidStrings", DownloadAndroidStringsTask::class.java) {
                    it.apiConfig = androidConfig.apiConfig
                    it.config = androidConfig.translationsUpdateConfig
                    it.buildFolder = project.buildDir
                }

                create("uploadAndroidStrings", UploadStrings::class.java) {
                    it.apiConfig = androidConfig.apiConfig
                    it.uploadEntries = androidConfig.uploadEntries
                }
            }
        }
    }
}


class LokalisePluginIos : Plugin<Project> {

    lateinit var iosConfig: IosConfig;

    override fun apply(project: Project) {

        iosConfig = project.extensions.create(lokaliseConfig, IosConfig::class.java)

        project.afterEvaluate {
            Api2.configure(iosConfig.apiConfig.projectId)

            with(project.tasks) {
                create("downloadIosLocalizableStrings", DownloadIosStringsTask::class.java) {
                    it.apiConfig = iosConfig.apiConfig
                    it.downloadsConfigs = iosConfig.downloadConfigEntries
                    it.projectFolder = project.projectDir
                    it.buildFolder = project.buildDir
                }
            }
        }
    }
}


open class AndroidConfig {
    val apiConfig = ApiConfig()
    fun api(action: Action<in ApiConfig>) = action.execute(apiConfig)

    val translationsUpdateConfig = TranslationsUpdateConfig()
    fun translationsUpdateConfig(action: Action<TranslationsUpdateConfig>) = action.execute(translationsUpdateConfig)

    val uploadEntries: MutableList<UploadEntry> = mutableListOf()
    fun uploadEntry(action: Action<in UploadEntry>) {
        val newEntry = UploadEntry()
        action.execute(newEntry)
        uploadEntries.add(newEntry)
    }
}

open class IosConfig {
    val apiConfig = ApiConfig()
    fun api(action: Action<in ApiConfig>) = action.execute(apiConfig)

    val downloadConfigEntries: MutableList<IosDownloadConfig> = mutableListOf();

    fun lang(action: Action<in IosDownloadConfig>) {
        val newConfig = IosDownloadConfig()
        action.execute(newConfig)
        downloadConfigEntries.add(newConfig)
    }
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


open class IosDownloadConfig {
    var path: String = ""
    var lokaliseLang: String = ""
    var langCode = ""
}

fun File.createFileIfNotExist(block: (File.() -> Unit)? = null) {
    if (exists()) {
        createNewFile()
    }
    block?.let {
        it(this)
    }
}

fun File.createFolderIfNotExist() {
    if (!this.exists()) {
        this.mkdirs()
    }
}