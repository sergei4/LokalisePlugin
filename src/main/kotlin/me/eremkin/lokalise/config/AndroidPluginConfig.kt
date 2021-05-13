package me.eremkin.lokalise.config

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

open class UploadEntry(
    var path: String = "",
    var lang: String = ""
)

data class Lang(val name: String = "") {
    var androidLang: String = ""
    var lokaliseLang: String = ""
    var updateStrategy: String = "merge"
    var forceSetRTL = false
}

class TranslationsUpdateConfig(var resPath: String = "") {

    lateinit var langs: NamedDomainObjectContainer<Lang>

    fun langs(closure: Closure<Lang>) {
        langs.configure(closure)
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