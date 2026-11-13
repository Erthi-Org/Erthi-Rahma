package org.erthi.rahma

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Each method is invoked in a different process on a disposable CI emulator. */
class ProcessRestartProbe {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val marker get() = context.getSharedPreferences("restart_probe", 0)

    @Test fun prepare() = runBlocking {
        val id = ObituaryDraftDatabase.open(context).drafts().save(
            ObituaryDraft(name = "مسودة اختبار الاستعادة", details = "بيانات اختبار محلية", phraseIndex = 2, updatedAt = 123L)
        )
        assertTrue(marker.edit().putLong("id", id).commit())
        assertTrue(context.quranProgress().edit()
            .putString(LAST_RECITATION, "ar.mohamedtablawi:55")
            .putLong(LAST_POSITION, 3210L).commit())
    }

    @Test fun verify() = runBlocking {
        val id = marker.getLong("id", -1)
        assertTrue(id > 0)
        val dao = ObituaryDraftDatabase.open(context).drafts()
        val draft = dao.observe().first().single { it.id == id }
        assertEquals("مسودة اختبار الاستعادة", draft.name)
        assertEquals("بيانات اختبار محلية", draft.details)
        assertEquals(2, draft.phraseIndex)
        assertEquals("ar.mohamedtablawi:55", context.quranProgress().getString(LAST_RECITATION, null))
        assertEquals(3210L, context.quranProgress().getLong(LAST_POSITION, -1))
        dao.delete(id)
        assertTrue(marker.edit().clear().commit())
        assertTrue(context.quranProgress().edit().clear().commit())
    }
}
