package me.eremkin.lokalise.config

import org.gradle.api.Action

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
    var forceSetRTL = false
    var createInfoPlist = false
}