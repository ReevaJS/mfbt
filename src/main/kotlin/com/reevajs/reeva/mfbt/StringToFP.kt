package com.reevajs.reeva.mfbt

import com.reevajs.reeva.mfbt.impl.EDouble
import com.reevajs.reeva.mfbt.impl.expect
import com.reevajs.reeva.mfbt.impl.unreachable
import java.math.BigInteger

class StringToFP(input: String) {
    private val input = input.trim()
    private var cursor = 0
    private val isDone: Boolean get() = cursor >= input.length
    private val char: Char get() = input[cursor]
    private var radix = 10

    fun parse(): Double? {
        if (input.isBlank())
            return 0.0

        val sign = when (char) {
            '-' -> {
                cursor++
                -1
            }
            '+' -> {
                cursor++
                1
            }
            else -> 1
        }

        val noSignInput = input.drop(cursor)
        if (noSignInput.startsWith("Infinity"))
            return EDouble.infinity() * sign

        if (char == '0') {
            cursor++
            if (isDone)
                return 0.0

            val radix = when (char) {
                'b', 'B' -> 2
                'o', 'O' -> 8
                'x', 'X' -> 16
                else -> null
            }

            if (radix != null) {
                this.radix = radix
                cursor++
                if (isDone)
                    return null
            }
        }

        while (!isDone && char == '0')
            cursor++

        if (isDone)
            return 0.0

        var seenDot = false
        var seenExponential = false
        val valueStart = cursor
        var exponentStart: Int? = null

        while (!isDone) {
            if (parseDigit()) {
                continue
            } else if (char == '.') {
                if (radix != 10 || seenDot || seenExponential)
                    return null
                seenDot = true
                cursor++
                continue
            } else if (char == 'e' || char == 'E') {
                if (radix != 10 || seenExponential)
                    return null
                seenExponential = true
                exponentStart = cursor
                cursor++
                if (isDone)
                    return null
                if (char == '+' || char == '-') {
                    // We don't have to do anything with this because we use Java's
                    // string -> number conversions for the exponent
                    cursor++
                }
            } else return null
        }

        if (!isDone)
            return null

        return if (radix == 10) {
            java.lang.Double.valueOf(input.substring(valueStart)) * sign
        } else {
            expect(!seenDot)
            val noExponentInput = input.substring(valueStart, exponentStart ?: input.length)
            var bigint = BigInteger(noExponentInput, radix)
            if (exponentStart != null) {
                // Be sure to skip the 'e' or 'E' character
                val exponentStr = input.substring(exponentStart + 1, input.length)
                val int = Integer.valueOf(exponentStr, 10)
                bigint = bigint.pow(int)
            }
            bigint.toDouble() * sign
        }
    }

    private fun parseDigit(): Boolean {
        expect(!isDone)
        val result =  when (radix) {
            2 -> char in '0'..'1'
            8 -> char in '0'..'7'
            10 -> char in '0'..'9'
            16 -> char in '0'..'9' || char in 'a'..'f' || char in 'A'..'F'
            else -> unreachable()
        }

        if (result)
            cursor++

        return result
    }
}
