package com.al4apps.chatshop.core.util

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.abs

/**
 * Деньги в минорных единицах (сотых долях). Парсинг только из decimal-строки
 * провода (`"4500.00"`), Double/Float запрещены контрактом (CLIENT_GUIDE §2).
 */
@Serializable
@JvmInline
value class Money(val minorUnits: Long) : Comparable<Money> {

    /** Каноничная decimal-строка провода: `4500.00`. */
    fun asDecimalString(): String {
        val sign = if (minorUnits < 0) "-" else ""
        val units = abs(minorUnits)
        val whole = units / 100
        val cents = units % 100
        return "$sign$whole.${cents.toString().padStart(2, '0')}"
    }

    /** Отображаемая сумма: разряды через пробел и запятая — `1 250,00`. */
    fun asDisplayString(): String {
        val sign = if (minorUnits < 0) "-" else ""
        val units = abs(minorUnits)
        val whole = units / 100
        val cents = units % 100
        val groupedWhole = whole.toString()
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()
        return "$sign$groupedWhole,${cents.toString().padStart(2, '0')}"
    }

    override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

    companion object {

        /**
         * Разбирает decimal-строку (`"4500"`, `"4500.5"`, `"4500.00"`, `"-10.25"`).
         *
         * @return значение или null, если строка не является decimal-числом
         *   или содержит больше двух знаков после точки.
         */
        fun parseOrNull(decimal: String): Money? {
            val trimmed = decimal.trim()
            if (trimmed.isEmpty()) return null
            val negative = trimmed.startsWith("-")
            val unsigned = trimmed.removePrefix("-")
            val parts = unsigned.split(".")
            if (parts.size > 2) return null
            val wholePart = parts[0]
            val fracPart = parts.getOrElse(1) { "" }
            if (wholePart.isEmpty() || wholePart.any { !it.isDigit() }) return null
            if (fracPart.length > 2 || fracPart.any { !it.isDigit() }) return null
            val whole = wholePart.toLongOrNull() ?: return null
            val cents = fracPart.padEnd(2, '0').toLong()
            val units = whole * 100 + cents
            return Money(if (negative) -units else units)
        }
    }
}
