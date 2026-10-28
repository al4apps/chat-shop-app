package com.al4apps.chatshop

import com.al4apps.chatshop.core.util.Money
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyTest {

    @Test
    fun displayStringGroupsThousandsAndUsesComma() {
        assertEquals("0,00", Money(0).asDisplayString())
        assertEquals("1,05", Money(105).asDisplayString())
        assertEquals("1 250,00", Money(125_000).asDisplayString())
        assertEquals("1 234 567,89", Money(123_456_789).asDisplayString())
    }

    @Test
    fun displayStringKeepsNegativeSign() {
        assertEquals("-10,25", Money(-1_025).asDisplayString())
        assertEquals("-0,50", Money(-50).asDisplayString())
    }

    @Test
    fun decimalWireFormatIsUnchanged() {
        assertEquals("1250.00", Money(125_000).asDecimalString())
        assertEquals("-10.25", Money(-1_025).asDecimalString())
    }
}
