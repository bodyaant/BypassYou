package com.example.bypassyou

import com.example.bypassyou.data.repository.VipRepository
import com.example.bypassyou.util.SystemIntegrityHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ScheduleAndNumberMatchingTest {

    private fun active(day: Int, hh: Int, mm: Int, days: Set<String>, start: Int = 23 * 60, end: Int = 7 * 60) =
        SystemIntegrityHelper.isScheduleActive(day, hh * 60 + mm, start, end, days)

    @Test
    fun overnightMorningBelongsToPreviousDay() {
        val fridayOnly = setOf("5")
        assertTrue(active(Calendar.FRIDAY, 23, 30, fridayOnly))
        assertTrue(active(Calendar.SATURDAY, 2, 0, fridayOnly))
        assertFalse(active(Calendar.FRIDAY, 2, 0, fridayOnly)) // Thursday night, not selected
        assertFalse(active(Calendar.SATURDAY, 23, 30, fridayOnly))
    }

    @Test
    fun overnightWrapsSundayToSaturday() {
        assertTrue(active(Calendar.SUNDAY, 6, 59, setOf("6"))) // Saturday night
        assertTrue(active(Calendar.MONDAY, 1, 0, setOf("7"))) // Sunday night
    }

    @Test
    fun sameDayRange() {
        val weekdays = setOf("1", "2", "3", "4", "5")
        assertTrue(active(Calendar.MONDAY, 9, 0, weekdays, start = 9 * 60, end = 18 * 60))
        assertFalse(active(Calendar.MONDAY, 18, 1, weekdays, start = 9 * 60, end = 18 * 60))
        assertFalse(active(Calendar.SATURDAY, 12, 0, weekdays, start = 9 * 60, end = 18 * 60))
    }

    @Test
    fun phoneNumbersMatchByTrailingDigits() {
        assertTrue(VipRepository.isSamePhoneNumber("+7 (912) 345-67-89", "89123456789"))
        assertTrue(VipRepository.isSamePhoneNumber("912-345-6789", "+19123456789"))
        assertFalse(VipRepository.isSamePhoneNumber("+79123456789", "+79123456780"))
        assertFalse(VipRepository.isSamePhoneNumber("", "89123456789"))
        assertFalse(VipRepository.isSamePhoneNumber("112", "911")) // short numbers need exact match
    }
}
