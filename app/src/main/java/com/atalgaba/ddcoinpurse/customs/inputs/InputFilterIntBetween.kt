package com.atalgaba.ddcoinpurse.customs.inputs

import android.text.InputFilter
import android.text.Spanned


class InputFilterIntBetween(private val min: Int, private val max: Int? = null) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            // Using @Zac's initial solution
            val lastVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend)
            val newVal =
                lastVal.substring(0, dstart) + source.toString() + lastVal.substring(dstart)
            val input = newVal.toInt()

            // To avoid deleting all numbers and avoid @Guerneen4's case
            if (input < min && lastVal == "") return min.toString()

            // Normal min, max check
            if ((max == null && input >= min) || (max != null && input in min..max)) {

                // To avoid more than two leading zeros to the left
                val lastDest = dest.toString()
                val checkStr = lastDest.replaceFirst("^0+(?!$)".toRegex(), "")
                return if (checkStr.length < lastDest.length) "" else null
            }
        } catch (ignored: NumberFormatException) {
        }
        return ""
    }
}