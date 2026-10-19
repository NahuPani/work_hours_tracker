package com.example.workhourstracker.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val releaseNotes: String,
    val downloadUrl: String
)

object UpdateChecker {
    private const val GITHUB_RELEASE_API =
        "https://api.github.com/repos/NahuPani/work_hours_tracker/releases/latest"
    private const val RAW_VERSION_URL =
        "https://raw.githubusercontent.com/NahuPani/work_hours_tracker/main/version.json"

    suspend fun checkForUpdates(context: Context): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val currentVersion = try {
                context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            }

            var latestVer = ""
            var notes = ""
            var downloadUrl = "https://github.com/NahuPani/work_hours_tracker/releases"

            // 1. Check version.json from repository main branch (direct, no GitHub API rate-limit)
            try {
                val rawJson = httpGetJson(RAW_VERSION_URL)
                if (rawJson != null) {
                    val rawVer = rawJson.optString("version", "").removePrefix("v").removePrefix("V").trim()
                    if (rawVer.isNotBlank()) {
                        latestVer = rawVer
                        notes = rawJson.optString("releaseNotes", "")
                        downloadUrl = rawJson.optString("downloadUrl", downloadUrl)
                    }
                }
            } catch (e: Exception) {
                // Ignore and try releases API
            }

            // 2. Check GitHub Releases API and prioritize whichever version is newer
            try {
                val releaseJson = httpGetJson(GITHUB_RELEASE_API)
                if (releaseJson != null) {
                    val releaseVer = releaseJson.optString("tag_name", "").removePrefix("v").removePrefix("V").trim()
                    if (releaseVer.isNotBlank() && (latestVer.isBlank() || isVersionNewer(releaseVer, latestVer))) {
                        latestVer = releaseVer
                        notes = releaseJson.optString("body", notes).trim()
                        downloadUrl = releaseJson.optString("html_url", downloadUrl)

                        // Check if an .apk asset was uploaded to the release
                        val assets = releaseJson.optJSONArray("assets")
                        if (assets != null && assets.length() > 0) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                val name = asset.optString("name", "")
                                if (name.endsWith(".apk", ignoreCase = true)) {
                                    downloadUrl = asset.optString("browser_download_url", downloadUrl)
                                    break
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }

            if (latestVer.isBlank()) {
                return@withContext Result.failure(Exception("No se encontró información de versiones en el repositorio"))
            }

            val isNewer = isVersionNewer(latestVer, currentVersion)
            Result.success(
                UpdateInfo(
                    isUpdateAvailable = isNewer,
                    latestVersion = latestVer,
                    currentVersion = currentVersion,
                    releaseNotes = notes,
                    downloadUrl = downloadUrl
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    private fun httpGetJson(urlString: String): JSONObject? {
        val url = URL(urlString)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "WorkHoursTracker-Android")
        }

        try {
            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                return JSONObject(sb.toString())
            }
            return null
        } finally {
            conn.disconnect()
        }
    }
}
