package org.erthi.rahma

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class GuidanceTopic(val title: String, val overview: String, val review: String = "مسودة تعليمية — بانتظار مراجعة شرعية")

private val topics = listOf(
    GuidanceTopic("الصبر عند المصيبة", "الحزن والبكاء الطبيعي ليسا مثل النياحة ورفع الصوت بالندب. لا يُلام أهل الميت على مجرد الحزن."),
    GuidanceTopic("الاحتضار", "تعلم آداب الاحتضار والتعامل مع المتوفى، مع الرجوع إلى المختصين عند الحاجة."),
    GuidanceTopic("الغسل والتكفين", "تختلف التفاصيل باختلاف حال المتوفى؛ يُستعان بالمغسلين والمغسلات المؤهلين."),
    GuidanceTopic("صلاة الجنازة", "تُعرض الصفة والأدعية والأحكام بعد مراجعة المصادر الشرعية المعتمدة."),
    GuidanceTopic("الدفن وأحكام القبر", "التفريق بين ما ثبت شرعًا والعادات المحلية، مع احترام الأنظمة المنظمة للدفن."),
    GuidanceTopic("التعزية", "مواساة أهل الميت برفق، وتجنب تحميلهم أعباء اجتماعية أو مالية."),
    GuidanceTopic("زيارة المقابر", "بيان آداب الزيارة وما يجوز وما لا يجوز مع توضيح المسائل المختلف فيها."),
    GuidanceTopic("العادات والبدع", "لا توصف ممارسة بأنها بدعة محرمة لمجرد كونها عادة؛ يلزم الدليل وتحرير محل الخلاف.")
)

@Composable
fun FuneralGuidanceScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("الجنائز على هدي السنة", style = MaterialTheme.typography.headlineSmall)
        Text("محتوى تمهيدي لا يمثل فتوى أو اعتمادًا شرعيًا. ستضاف الأدلة والتخريج والمراجعة قبل النشر المعتمد.")
        topics.forEach { topic ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(topic.title, style = MaterialTheme.typography.titleMedium)
                    Text(topic.overview)
                    Text(topic.review, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
