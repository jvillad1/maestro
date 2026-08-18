package com.maestro.shared.util

import kotlin.test.Test
import kotlin.test.assertEquals

class DatesTest {
    @Test
    fun `retrocede y avanza dentro del mismo anio`() {
        assertEquals("2026-07", shiftMonth("2026-08", -1))
        assertEquals("2026-09", shiftMonth("2026-08", 1))
        assertEquals("2026-02", shiftMonth("2026-08", -6))
    }

    @Test
    fun `cruza el cambio de anio`() {
        assertEquals("2025-12", shiftMonth("2026-01", -1))
        assertEquals("2026-01", shiftMonth("2025-12", 1))
        assertEquals("2024-11", shiftMonth("2026-01", -14))
    }

    @Test
    fun `entradas invalidas se devuelven tal cual`() {
        assertEquals("2026", shiftMonth("2026", -1))
        assertEquals("2026-13", shiftMonth("2026-13", -1))
        assertEquals("", shiftMonth("", -1))
        assertEquals("abc-de", shiftMonth("abc-de", 1))
    }
}
