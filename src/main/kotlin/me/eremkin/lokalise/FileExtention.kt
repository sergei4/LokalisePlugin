package me.eremkin.lokalise

import java.io.File
import java.net.URL
import java.util.zip.ZipFile

fun File.ifExists(block: File.() -> Unit) {
    if (exists()) block(this)
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
    if (!exists()) {
        mkdirs()
    }
}

fun unzip(bundleUrl: String, targetFolder: File) {
    File(targetFolder, "temp.zip").createFileIfNotExist {
        URL(bundleUrl).openStream().copyTo(outputStream())
        ZipFile(this).use { zip ->
            zip.entries().asSequence().forEach { zipEntry ->
                if (!zipEntry.isDirectory) {
                    zip.getInputStream(zipEntry)
                        .copyTo(File(targetFolder, zipEntry.name.split("/").last()).outputStream())
                }
            }
        }
        delete()
    }
}