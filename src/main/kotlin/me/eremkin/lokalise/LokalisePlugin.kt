package me.eremkin.lokalise

import com.android.build.gradle.BaseExtension
import me.eremkin.lokalise.api.LocaliseService
import me.eremkin.lokalise.tasks.android.DownloadAndroidStringsTask
import me.eremkin.lokalise.tasks.android.UploadStrings
import me.eremkin.lokalise.tasks.ios.DownloadIosStringsTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

const val taskGroup = "lokalise"
const val lokaliseConfig = "lokalise"

class LokalisePlugin : Plugin<Project> {

    lateinit var androidConfig: AndroidConfig

    override fun apply(project: Project) {

        androidConfig = project.extensions.create(lokaliseConfig, AndroidConfig::class.java)
        val langs = project.container(Lang::class.java)
        androidConfig.translationsUpdateConfig.langs = langs

        project.afterEvaluate {

            project.plugins.apply {
                if (findPlugin("com.android.application") == null && findPlugin("com.android.library") == null) {
                    throwError("You must apply the Android plugin or the Android library plugin before using the lokalise plugin")
                }
            }

            val android: BaseExtension = project.extensions.findByName("android") as BaseExtension

            val resPath = androidConfig.translationsUpdateConfig.resPath

            if (resPath.isEmpty()) {
                try {
                    val resDirs = android.sourceSets.getByName("main").res.srcDirs
                    androidConfig.translationsUpdateConfig.resPath = resDirs.iterator().next().absolutePath
                } catch (exeption: Exception) {
                    throwError(exeption.message ?: "")
                }
            }

            with(project.tasks) {
                val localiseService = LocaliseService(androidConfig.apiConfig)
                create("downloadAndroidStrings", DownloadAndroidStringsTask::class.java) {
                    it.group = taskGroup
                    it.localiseService = localiseService
                    it.config = androidConfig.translationsUpdateConfig
                    it.buildFolder = project.buildDir
                }

                create("uploadAndroidStrings", UploadStrings::class.java) {
                    it.group = taskGroup
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
            with(project.tasks) {
                val localiseService = LocaliseService(iosConfig.apiConfig)
                create("downloadIOSLocalizableStrings", DownloadIosStringsTask::class.java) {
                    it.group = taskGroup
                    it.localiseService = localiseService
                    it.downloadsConfigs = iosConfig.downloadConfigEntries
                    it.projectFolder = project.projectDir
                    it.buildFolder = project.buildDir
                }
            }
        }
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

open class IosDownloadConfig {
    var path: String = ""
    var lokaliseLang: String = ""
    var langCode = ""
}