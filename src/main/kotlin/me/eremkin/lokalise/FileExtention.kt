package me.eremkin.lokalise

import java.io.File

fun File.createFileIfNotExist(block: (File.() -> Unit)? = null) {
    if (exists()) {
        createNewFile()
    }
    block?.let {
        it(this)
    }
}

fun File.createFolderIfNotExist() {
    if (!exists()) {
        mkdirs()
    }
}