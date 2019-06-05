package me.eremkin.lokalise.api.dto

import com.google.gson.annotations.SerializedName

data class DownloadResponse(
    @SerializedName("project_id") val projectId: String = "",
    @SerializedName("bundle_url") val bundleUrl: String = ""
)