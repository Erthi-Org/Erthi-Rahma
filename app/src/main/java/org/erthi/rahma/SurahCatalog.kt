package org.erthi.rahma

data class Surah(val number: Int, val name: String)

/** Fixed Arabic surah names; no network needed to browse the Quran index. */
object SurahCatalog {
    val all = listOf(
        "الفاتحة","البقرة","آل عمران","النساء","المائدة","الأنعام","الأعراف","الأنفال","التوبة","يونس",
        "هود","يوسف","الرعد","إبراهيم","الحجر","النحل","الإسراء","الكهف","مريم","طه",
        "الأنبياء","الحج","المؤمنون","النور","الفرقان","الشعراء","النمل","القصص","العنكبوت","الروم",
        "لقمان","السجدة","الأحزاب","سبأ","فاطر","يس","الصافات","ص","الزمر","غافر",
        "فصلت","الشورى","الزخرف","الدخان","الجاثية","الأحقاف","محمد","الفتح","الحجرات","ق",
        "الذاريات","الطور","النجم","القمر","الرحمن","الواقعة","الحديد","المجادلة","الحشر","الممتحنة",
        "الصف","الجمعة","المنافقون","التغابن","الطلاق","التحريم","الملك","القلم","الحاقة","المعارج",
        "نوح","الجن","المزمل","المدثر","القيامة","الإنسان","المرسلات","النبأ","النازعات","عبس",
        "التكوير","الانفطار","المطففين","الانشقاق","البروج","الطارق","الأعلى","الغاشية","الفجر","البلد",
        "الشمس","الليل","الضحى","الشرح","التين","العلق","القدر","البينة","الزلزلة","العاديات",
        "القارعة","التكاثر","العصر","الهمزة","الفيل","قريش","الماعون","الكوثر","الكافرون","النصر",
        "المسد","الإخلاص","الفلق","الناس"
    ).mapIndexed { index, name -> Surah(index + 1, name) }
    private val arabicMarks = Regex("[\\p{M}ـ]")
    private val spaces = Regex("\\s+")

    private fun normalize(text: String): String =
        arabicMarks.replace(text.trim(), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ٱ', 'ا')
            .replace('ى', 'ي')
            .replace(spaces, " ")

    private val normalizedNames = all.map { normalize(it.name) }

    fun search(query: String): List<Surah> {
        val normalized = normalize(query)
        if (normalized.isEmpty()) return all
        val number = normalized.map { it.digitToIntOrNull()?.digitToChar() ?: it }
            .joinToString("").toIntOrNull()
        if (number != null) return listOfNotNull(all.getOrNull(number - 1))
        return all.filterIndexed { index, _ -> normalizedNames[index].contains(normalized) }
    }
}
