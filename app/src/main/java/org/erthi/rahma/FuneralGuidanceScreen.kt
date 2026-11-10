package org.erthi.rahma

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class GuidanceTopic(
    val title: String,
    val guidance: String,
    val references: List<String>,
    val review: String = "بانتظار المراجعة الشرعية"
)

private val topics = listOf(
    GuidanceTopic(
        "الاحتضار",
        "ارفق بالمحتضر وذكّره بالشهادة بلطف. وبعد تحقق الوفاة، يُستعان بالمختصين في إجراءاتها، ويُدعى للمتوفى بخير.",
        listOf("صحيح مسلم 916 — تلقين المحتضر", "صحيح مسلم 920 — الدعاء وإغماض العينين")
    ),
    GuidanceTopic(
        "الغسل والتكفين",
        "يُعهد بالغسل والتكفين إلى من يعرف أحكامهما ويحفظ ستر المتوفى وكرامته. ومن الوارد في الغسل البدء بالميامن ومواضع الوضوء. وفي وصف كفن النبي ﷺ أنه كُفّن في ثلاثة أثواب بيض. تُحال الحالات الخاصة إلى مغسّل مؤهل والجهة المختصة.",
        listOf("صحيح البخاري 1255 — غسل ابنة النبي ﷺ", "صحيح البخاري 1264 — صفة كفن النبي ﷺ")
    ),
    GuidanceTopic(
        "صلاة الجنازة",
        "المقصود من الصلاة الدعاء للميت. ثبتت أربع تكبيرات في الصلاة على النجاشي. ومن دعاء الجنازة المأثور طلب المغفرة والرحمة والعفو للميت. في تفاصيل القراءة والأذكار خلاف فقهي يُرجع فيه إلى الإمام أو مختص.",
        listOf("صحيح البخاري 1333 — أربع تكبيرات", "صحيح مسلم 963 — الدعاء للميت")
    ),
    GuidanceTopic(
        "الدفن",
        "تُصان كرامة المتوفى أثناء نقله ودفنه، ويُنسّق مع المسؤولين عن المقبرة. ورد الحث على الإسراع بالجنازة، وتراعى إجراءات التحقق والتصريح بالدفن.",
        listOf("صحيح البخاري 1315 — الإسراع بالجنازة")
    ),
    GuidanceTopic(
        "التعزية",
        "تُراعى حال الأسرة وتُواسَى بكلام طيب ودعاء. من معاني التعزية الواردة تذكير المصاب بأن لله ما أخذ وله ما أعطى، والدعاء له بالصبر والاحتساب.",
        listOf("سورة البقرة 155–157", "صحيح البخاري 1284 — تعزية المصاب")
    ),
    GuidanceTopic(
        "زيارة القبور",
        "من آداب الزيارة السلام على أهل المقابر والدعاء لهم. في بعض تفاصيل الزيارة خلاف فقهي، وتُراعى حرمة المقبرة وتعليماتها.",
        listOf("صحيح مسلم 975 — السلام على أهل المقابر والدعاء لهم")
    ),
    GuidanceTopic(
        "الحزن والنياحة والعادات",
        "البكاء الطبيعي والحزن مباحان. النياحة ورفع الصوت بالندب من الممارسات التي ورد النهي عنها. تختلف عادات المواساة بين الناس، ويحتاج الحكم على ممارسة بعينها إلى معرفة صورتها ودليلها وموضع الخلاف فيها.",
        listOf("صحيح البخاري 1303 — دمع العين وحزن القلب", "صحيح مسلم 934 — النهي عن النياحة")
    )
)

@Composable
fun FuneralGuidanceScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("الجنائز على هدي السنة", style = MaterialTheme.typography.headlineSmall)
        Text("مادة تعليمية أولية بانتظار المراجعة الشرعية. في الحالات الخاصة يُرجع إلى مختص مؤهل.")
        topics.forEach { topic ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(topic.title, style = MaterialTheme.typography.titleMedium)
                    Text(topic.guidance)
                    Text("الأدلة والمراجع", style = MaterialTheme.typography.labelLarge)
                    topic.references.forEach { reference ->
                        Text(reference, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(topic.review, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
