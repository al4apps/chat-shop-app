package com.al4apps.chatshop.features.auth.phone.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Маска номера Таджикистана: 9 цифр → `+992 988009090` (по мере ввода). */
class PhoneVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        val formatted = buildString {
            append(PREFIX)
            if (digits.isNotEmpty()) {
                append(' ')
                append(digits)
            }
        }
        // Позиция i-й цифры оригинала в отформатированной строке: после "+992 ".
        val firstDigitPosition = PREFIX.length + 1

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                digits.isEmpty() -> formatted.length
                else -> offset + firstDigitPosition
            }

            override fun transformedToOriginal(offset: Int): Int =
                (offset - firstDigitPosition).coerceIn(0, digits.length)
        }

        return TransformedText(AnnotatedString(formatted), mapping)
    }

    private companion object {
        const val PREFIX = "+992"
    }
}
