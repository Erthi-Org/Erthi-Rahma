package org.erthi.rahma

import org.junit.Assert.assertEquals
import org.junit.Test

class SurahCatalogTest {
    @Test fun hasAll114Surahs() {
        assertEquals(114, SurahCatalog.all.size)
        assertEquals("الفاتحة", SurahCatalog.all.first().name)
        assertEquals("الناس", SurahCatalog.all.last().name)
    }
    @Test fun searchesByNameAndNumber() {
        assertEquals(36, SurahCatalog.search("يس").first().number)
        assertEquals("البقرة", SurahCatalog.search("2").single().name)
    }
}
