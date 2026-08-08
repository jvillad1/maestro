package com.maestro.shared.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationTest {
    @Test
    fun `emails validos e invalidos`() {
        assertTrue(isValidEmail("sofia@mail.com"))
        assertTrue(isValidEmail("a.b+c@sub.dominio.co"))
        assertFalse(isValidEmail("iosdemo"))
        assertFalse(isValidEmail("a@b"))
        assertFalse(isValidEmail("@mail.com"))
        assertFalse(isValidEmail(""))
    }

    @Test
    fun `fechas ISO validas`() {
        assertTrue(isValidIsoDate("2026-08-07"))
        assertTrue(isValidIsoDate("2024-02-29")) // bisiesto
        assertTrue(isValidIsoDate("2026-12-31"))
    }

    @Test
    fun `fechas ISO invalidas`() {
        assertFalse(isValidIsoDate("2026-13-01"))  // mes 13
        assertFalse(isValidIsoDate("2026-02-30"))  // febrero corto
        assertFalse(isValidIsoDate("2025-02-29"))  // no bisiesto
        assertFalse(isValidIsoDate("26-08-07"))    // formato corto
        assertFalse(isValidIsoDate("2026/08/07"))  // separador
        assertFalse(isValidIsoDate("mañana"))
        assertFalse(isValidIsoDate(""))
    }

    @Test
    fun `edad y cuota`() {
        assertTrue(isValidAge("14"))
        assertFalse(isValidAge("0"))
        assertFalse(isValidAge("-3"))
        assertFalse(isValidAge("abc"))
        assertTrue(isValidFee("150000"))
        assertFalse(isValidFee("0"))
        assertFalse(isValidFee("12mil"))
    }
}
