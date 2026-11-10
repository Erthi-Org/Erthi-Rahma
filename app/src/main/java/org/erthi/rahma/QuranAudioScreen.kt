package org.erthi.rahma

import android.media.MediaPlayer
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
    var playing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("اختر القارئ والسورة") }
    val player = remember { MediaPlayer() }
    DisposableEffect(Unit) { onDispose { player.release() } }
    fun stop() {
        player.reset()
        playing = false
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
        Text("رقم السورة: $surah من 114")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { stop(); surah = (surah - 1).coerceAtLeast(1) }, enabled = surah > 1) { Text("السابقة") }
            OutlinedButton(onClick = { stop(); surah = (surah + 1).coerceAtMost(114) }, enabled = surah < 114) { Text("التالية") }
        }
        Button(onClick = {
            val reader = readers[readerIndex]
            if (playing) {
                player.pause()
                playing = false
                status = "متوقف مؤقتًا"
            } else if (reader.edition != null) {
                try {
                    stop()
                    status = "جارٍ التحميل..."
                    val url = "https://cdn.islamic.network/quran/audio-surah/${reader.bitrate}/${reader.edition}/$surah.mp3"
                    player.setDataSource(url)
                    player.setOnPreparedListener { it.start(); playing = true; status = "جارٍ التشغيل" }
                    player.setOnCompletionListener { playing = false; status = "انتهت السورة" }
                    player.setOnErrorListener { _, _, _ -> playing = false; status = "تعذر تشغيل التلاوة"; true }
                    player.prepareAsync()
                } catch (e: Exception) {
                    status = "تعذر بدء التشغيل"
                }
            }
        }, enabled = readers[readerIndex].edition != null) {
            Text(if (playing) "إيقاف مؤقت" else "تشغيل السورة")
        }
        Text(status)
        Text("التلاوات التي لم تتوفر لها موافقة ومصدر موثوق لا يمكن تشغيلها حاليًا.")
    }
}
