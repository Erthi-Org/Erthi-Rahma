package org.erthi.rahma

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.graphics.Typeface
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

private val Ink = Color(0xFF0A0A0A)
private val Green = Color(0xFF81B99B)

private enum class ObituaryTemplate(val title: String, val background: Int, val accent: Int) {
    CLASSIC("الأخضر الهادئ", 0xFF0A0A0A.toInt(), 0xFF81B99B.toInt()),
    BLUE("الأزرق الليلي", 0xFF101D2E.toInt(), 0xFFA9D2FF.toInt()),
    GOLD("الذهبي الدافئ", 0xFF251C15.toInt(), 0xFFFFD78B.toInt())
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme(colorScheme = darkColorScheme(primary = Green, background = Ink, surface = Color(0xFF1A1D1B))) {
                    RahmaApp(onShare = { name, prayer, selectedPhrase, template ->
                        shareObituary(name, prayer, selectedPhrase, template)
                    })
                }
            }
        }
    }

    private fun shareObituary(name: String, prayer: String, selectedPhrase: String, template: ObituaryTemplate) {
        val image = renderObituary(name, prayer, selectedPhrase, template)
        val folder = File(cacheDir, "shared").apply { mkdirs() }
        val file = File(folder, "obituary.png")
        FileOutputStream(file).use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = android.content.ClipData.newUri(contentResolver, "نعي", uri)
        }
        startActivity(Intent.createChooser(intent, "مشاركة النعي"))
    }
}

@Composable
private fun RahmaApp(onShare: (String, String, String, ObituaryTemplate) -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = page != 0) { page = 0 }
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Button(onClick = { page = 0 }) { Text("النعي") } }
            item { Button(onClick = { page = 1 }) { Text("المصحف الصوتي") } }
            item { Button(onClick = { page = 2 }) { Text("أحكام الجنائز") } }
        }
        if (page != 0) {
            TextButton(onClick = { page = 0 }) { Text("الرجوع إلى النعي") }
        }
        if (page == 1) {
            QuranAudioScreen()
        } else if (page == 2) {
            FuneralGuidanceScreen()
        } else {
            ObituaryScreen(onShare)
        }
    }
}

@Composable
private fun ObituaryScreen(onShare: (String, String, String, ObituaryTemplate) -> Unit) {
    var name by remember { mutableStateOf("") }
    var prayer by remember { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf(0) }
    var template by remember { mutableStateOf(ObituaryTemplate.CLASSIC) }
    var showPreview by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val draftDao = remember(context) { ObituaryDraftDatabase.open(context).drafts() }
    val draftFlow = remember(draftDao) { draftDao.observe() }
    val drafts by draftFlow.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var draftId by remember { mutableLongStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<ObituaryDraft?>(null) }

    fun writeDraft(success: String, operation: suspend () -> Unit) {
        busy = true
        scope.launch {
            try {
                operation()
                message = success
            } catch (error: CancellationException) {
                throw error
            } catch (_: android.database.SQLException) {
                message = "تعذر حفظ التغيير. حاول مرة أخرى."
            } finally {
                busy = false
            }
        }
    }

    val phrases = listOf("إنا لله وإنا إليه راجعون", "اللهم اغفر له وارحمه", "اللهم ارحمها واغفر لها")
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text("إرثي | رحمة", fontSize = 30.sp, color = Green)
        Text("إنشاء نعي إسلامي دون حساب أو اتصال بالإنترنت", color = Color.LightGray)
        HorizontalDivider()
        OutlinedTextField(value = name, onValueChange = { name = it.take(120) },
            label = { Text("اسم المتوفى") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text("صيغة الدعاء")
        phrases.forEachIndexed { index, phrase ->
            Row {
                RadioButton(selected = selected == index, onClick = { selected = index })
                Text(phrase, modifier = Modifier.padding(top = 12.dp))
            }
        }
        OutlinedTextField(value = prayer, onValueChange = { prayer = it.take(250) },
            label = { Text("مكان وموعد صلاة الجنازة (اختياري)") },
            modifier = Modifier.fillMaxWidth())
        Text("قالب صورة النعي", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            ObituaryTemplate.entries.forEach { option ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .selectable(
                            selected = template == option,
                            role = Role.RadioButton,
                            onClick = { template = option }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = template == option, onClick = null)
                    Text(option.title, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
        Button(onClick = { showPreview = true }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("معاينة النعي")
        }
        if (showPreview && name.isNotBlank()) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(template.background), contentColor = Color.White)
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(phrases[selected], color = Color(template.accent), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text("انتقل إلى رحمة الله تعالى", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Text(name, color = Color(template.accent), fontSize = 24.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    if (prayer.isNotBlank()) Text(prayer, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
            Button(onClick = { onShare(name, prayer, phrases[selected], template) }, modifier = Modifier.fillMaxWidth()) { Text("مشاركة صورة النعي") }
        }
        OutlinedButton(
            onClick = {
                val draft = ObituaryDraft(draftId, name, prayer, selected, System.currentTimeMillis())
                writeDraft("حُفظت المسودة على الجهاز") {
                    val insertedId = draftDao.save(draft)
                    draftId = if (draft.id == 0L) insertedId else draft.id
                }
            },
            enabled = !busy && name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (draftId == 0L) "حفظ مسودة محلية" else "حفظ تعديل المسودة") }
        if (draftId != 0L) {
            TextButton(onClick = {
                draftId = 0
                name = ""
                prayer = ""
                selected = 0
                template = ObituaryTemplate.CLASSIC
                showPreview = false
                message = ""
            }, enabled = !busy) { Text("مسودة جديدة") }
        }
        if (message.isNotEmpty()) Text(message)
        if (drafts.isNotEmpty()) Text("المسودات المحلية", style = MaterialTheme.typography.titleMedium)
        drafts.forEach { draft ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(draft.name)
                    Row {
                        TextButton(onClick = {
                            draftId = draft.id
                            name = draft.name
                            prayer = draft.details
                            selected = draft.phraseIndex.coerceIn(phrases.indices)
                            template = ObituaryTemplate.CLASSIC
                            showPreview = false
                            message = "فُتحت المسودة للتعديل"
                        }, enabled = !busy) { Text("تعديل") }
                        TextButton(onClick = { pendingDelete = draft }, enabled = !busy) { Text("حذف") }
                    }
                }
            }
        }
        pendingDelete?.let { draft ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("حذف المسودة؟") },
                text = { Text(draft.name) },
                confirmButton = {
                    TextButton(onClick = {
                        pendingDelete = null
                        writeDraft("حُذفت المسودة") {
                            draftDao.delete(draft.id)
                            if (draftId == draft.id) {
                                draftId = 0
                                name = ""
                                prayer = ""
                                selected = 0
                                template = ObituaryTemplate.CLASSIC
                                showPreview = false
                            }
                        }
                    }) { Text("حذف") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) { Text("إلغاء") }
                }
            )
        }
        Text("المشاركة اختيارية. لا تُرفع البيانات إلى أي خادم.", color = Color.Gray, fontSize = 12.sp)
    }
}

private fun renderObituary(name: String, prayer: String, selectedPhrase: String, template: ObituaryTemplate): Bitmap {
    val width = 1080
    val inset = 70
    val spacing = 48
    val accent = template.accent

    fun paragraph(text: String, size: Float, color: Int): StaticLayout {
        val paint = TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width - 2 * inset)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
            .build()
    }

    val paragraphs = buildList {
        add(paragraph(selectedPhrase, 56f, accent))
        add(paragraph("انتقل إلى رحمة الله تعالى", 50f, AndroidColor.WHITE))
        add(paragraph(name, 70f, accent))
        if (prayer.isNotBlank()) add(paragraph(prayer, 40f, AndroidColor.WHITE))
        add(paragraph("إرثي | رحمة", 32f, accent))
    }
    val contentHeight = paragraphs.sumOf { it.height } + spacing * (paragraphs.size - 1)
    val height = maxOf(1350, contentHeight + 2 * inset)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(template.background)
    canvas.translate(inset.toFloat(), (height - contentHeight) / 2f)
    for (paragraph in paragraphs) {
        canvas.save()
        paragraph.draw(canvas)
        canvas.restore()
        canvas.translate(0f, (paragraph.height + spacing).toFloat())
    }
    return bitmap
}
