package org.erthi.rahma

import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private data class Reader(val name: String, val edition: String?, val bitrate: Int = 128)

@Composable
fun QuranAudioScreen() {
    val readers = remember {
        listOf(
            Reader("محمود خليل الحصري", "ar.husary"),
            Reader("محمد صديق المنشاوي", "ar.minshawi"),
            Reader("عبد الباسط عبد الصمد", "ar.abdulbasit", 192),
            Reader("محمد محمود الطبلاوي", null),
            Reader("فارس عباد", null),
            Reader("ماهر المعيقلي", null),
            Reader("السيد سعيد", null)
        )
    }
    var readerIndex by remember { mutableIntStateOf(0) }
    var surah by remember { mutableIntStateOf(1) }
    var search by remember { mutableStateOf("") }
    var playing by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("اختر القارئ والسورة") }
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    DisposableEffect(context) {
        val token = SessionToken(context, ComponentName(context, QuranPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        future.addListener({
            try { controller = future.get() } catch (_: Exception) { status = "تعذر الاتصال بمشغل الصوت" }
        }, MoreExecutors.directExecutor())
        onDispose {
            controller = null
            MediaController.releaseFuture(future)
        }
    }
    fun stop() {
        controller?.stop()
        controller?.clearMediaItems()
        playing = false
        paused = false
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("الاستماع إلى القرآن الكريم", style = MaterialTheme.typography.headlineSmall)
        Text("المصدر: AlQuran.cloud — بث تعليمي دون تنزيل أو إعادة استضافة")
        readers.forEachIndexed { index, reader ->
            Row(Modifier.fillMaxWidth()) {
                RadioButton(selected = readerIndex == index, onClick = {
                    stop()
                    readerIndex = index
                    status = if (reader.edition == null) "بانتظار مصدر مصرح به لهذا القارئ" else "جاهز للاستماع"
                })
                Text(reader.name + if (reader.edition == null) " — قريبًا" else " — متاح", modifier = Modifier.padding(top = 12.dp))
            }
        }
        Text("السورة المختارة: ${SurahCatalog.all[surah - 1].name} ($surah/114)")
        OutlinedTextField(
            value = search,
            onValueChange = { search = it.take(60) },
            label = { Text("ابحث عن سورة بالاسم أو الرقم") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (search.isNotBlank()) {
            SurahCatalog.search(search).take(8).forEach { result ->
                TextButton(onClick = { stop(); surah = result.number; search = "" }) {
                    Text("${result.number} — ${result.name}")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { stop(); surah = (surah - 1).coerceAtLeast(1) }, enabled = surah > 1) { Text("السابقة") }
            OutlinedButton(onClick = { stop(); surah = (surah + 1).coerceAtMost(114) }, enabled = surah < 114) { Text("التالية") }
        }
        Button(onClick = {
            val reader = readers[readerIndex]
            val c = controller
            if (c == null) {
                status = "جارٍ تهيئة المشغل"
            } else if (playing) {
                c.pause()
                playing = false
                paused = true
                status = "متوقف مؤقتًا"
            } else if (paused) {
                c.play()
                playing = true
                paused = false
                status = "جارٍ التشغيل"
            } else if (reader.edition != null) {
                val url = "https://cdn.islamic.network/quran/audio-surah/${reader.bitrate}/${reader.edition}/$surah.mp3"
                c.setMediaItem(MediaItem.fromUri(url))
                c.prepare()
                c.play()
                playing = true
                status = "جارٍ تشغيل السورة"
            }
        }, enabled = readers[readerIndex].edition != null && controller != null) {
            Text(if (playing) "إيقاف مؤقت" else if (paused) "استئناف التلاوة" else "تشغيل السورة")
        }
        Text(status)
        Text("التلاوات التي لم تتوفر لها موافقة ومصدر موثوق لا يمكن تشغيلها حاليًا.")
    }
}
