package org.rnazarevych.lokalise.api.dto

import com.google.gson.annotations.SerializedName

data class DownloadParams(
    @SerializedName("format") val format: String = "xml",
    @SerializedName("original_filenames") val original_filenames: Boolean = false,
    @SerializedName("replace_breaks") val replaceBreaks: Boolean = true,
    @SerializedName("filter_data") val filterData: List<String> = listOf("translated"),
    @SerializedName("filter_langs") val langs: List<String> = listOf()
)