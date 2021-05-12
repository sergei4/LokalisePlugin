package me.eremkin.lokalise

const val exceptionTag = "lokalise-plugin"

fun throwError(message: String) {
    throw RuntimeException("$exceptionTag: $message")
}