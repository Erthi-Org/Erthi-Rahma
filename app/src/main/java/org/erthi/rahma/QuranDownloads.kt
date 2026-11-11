package org.erthi.rahma

import android.content.Context
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal data class QuranRecording(val edition: String, val surah: Int) {
    init {
        require(edition in editions && surah in 1..114)
    }

    val mediaId get() = "$edition:$surah"
    val url get() = "https://cdn.islamic.network/quran/audio-surah/128/$edition/$surah.mp3"

    companion object {
        // Complete 128 kbps editions in the provider's by-surah index.
        private val editions = setOf(
            "ar.abdulbasitmurattal", "ar.muhammadsiddiqalminshawimujawwad",
            "ar.mohamedtablawi", "ar.faresabbad"
        )

        fun fromId(mediaId: String): QuranRecording? {
            val edition = mediaId.substringBefore(':')
            val surah = mediaId.substringAfter(':', "").toIntOrNull() ?: return null
            return if (edition in editions && surah in 1..114) QuranRecording(edition, surah) else null
        }
    }
}

internal class QuranDownloads(context: Context) {
    private val directory = File(context.noBackupFilesDir, "quran-audio")

    private fun target(recording: QuranRecording) =
        File(directory, "${recording.edition}-${recording.surah}.mp3")

    fun localFile(recording: QuranRecording): File? =
        target(recording).takeIf { it.isFile && it.length() > 0L }

    fun usedBytes(): Long = directory.listFiles().orEmpty()
        .filter { it.isFile && it.extension == "mp3" }.sumOf { it.length() }

    suspend fun delete(recording: QuranRecording) = withContext(Dispatchers.IO) {
        lock.withLock {
            val file = target(recording)
            if (file.exists() && !file.delete()) throw IOException("Could not remove download")
        }
    }

    suspend fun download(recording: QuranRecording): Unit = withContext(Dispatchers.IO) {
        lock.withLock {
            if (localFile(recording) != null) return@withLock
            if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Could not create directory")
            directory.listFiles().orEmpty().filter { it.extension == "part" }.forEach { it.delete() }
            val available = minOf(MAX_FILE_BYTES, MAX_TOTAL_BYTES - usedBytes(), directory.usableSpace)
            if (available <= 0) throw IOException("Download storage is full")

            val pending = File.createTempFile("quran-", ".part", directory)
            try {
                val connection = (URL(recording.url).openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }
                try {
                    if (connection.responseCode != HttpURLConnection.HTTP_OK ||
                        connection.contentType?.substringBefore(';') != "audio/mpeg"
                    ) throw IOException("Invalid audio response")
                    val length = connection.contentLengthLong
                    if (length > available) throw IOException("Audio exceeds available download storage")
                    var copied = 0L
                    connection.inputStream.use { input ->
                        pending.outputStream().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                copied += count
                                if (copied > available) throw IOException("Audio exceeds available download storage")
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                    ensureActive()
                    if (copied == 0L || (length >= 0 && copied != length) || !pending.renameTo(target(recording))) {
                        throw IOException("Incomplete audio download")
                    }
                } finally {
                    connection.disconnect()
                }
            } finally {
                pending.delete()
            }
        }
    }

    private companion object {
        val lock = Mutex()
        const val MAX_FILE_BYTES = 64L * 1024 * 1024
        const val MAX_TOTAL_BYTES = 256L * 1024 * 1024
    }
}
