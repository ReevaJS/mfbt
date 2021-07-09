// Copyright 2012 the V8 project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
//       copyright notice, this list of conditions and the following
//       disclaimer in the documentation and/or other materials provided
//       with the distribution.
//     * Neither the name of Google Inc. nor the names of its
//       contributors may be used to endorse or promote products derived
//       from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package me.mattco.reeva.mfbt

import me.mattco.reeva.mfbt.impl.*
import kotlin.math.max
import kotlin.math.min

const val FIRST_NON_FIXED = 1e60
const val MAX_FIXED_DIGITS_BEFORE_POINT = 60
const val MAX_FIXED_DIGITS_AFTER_POINT = 100
const val MAX_EXPONENTIAL_DIGITS = 120
const val MIN_PRECISION_DIGITS = 1
const val MAX_PRECISION_DIGITS = 120
const val BASE_10_MAXIMAL_LENGTH = 17
const val BASE_10_MAXIMAL_LENGTH_SINGLE = 9
const val MAX_CHARS_ECMA_SCRIPT_SHORTEST = 25
const val MAX_EXPONENT_LENGTH = 5
const val MIN_EXPONENT_WIDTH = 0

// These are arguments to V8's DoubleToStringConverter, but as this one
// doesn't have to be as generic, they can just be constant
const val INFINITY_SYMBOL = "Infinity"
const val NAN_SYMBOL = "NaN"
const val EXPONENT_CHARACTER = "e"
const val DECIMAL_IN_SHORTEST_LOW = -6
const val DECIMAL_IN_SHORTEST_HIGH = 21
const val MAX_LEADING_PADDING_ZEROES_IN_PRECISION_MODE = 6
const val MAX_TRAILING_PADDING_ZEROES_IN_PRECISION_MODE = 0

object Dtoa {
    fun toShortest(value: Double) = toShortestIeeeNumber(value, Mode.Shortest)

    fun toShortest(value: Float) =
        toShortestIeeeNumber(value.toDouble(), Mode.ShortestSingle)

    fun toFixed(value: Double, requestedDigits: Int): String? {
        if (EDouble(value).isSpecial())
            return handleSpecialValues(value)

        if (requestedDigits > MAX_FIXED_DIGITS_AFTER_POINT)
            return null
        if (value > FIRST_NON_FIXED || value <= -FIRST_NON_FIXED)
            return null

        val decimalPoint = Ref<Int>()
        val sign = Ref<Boolean>()
        val decimalRep = StringBuilder()
        val resultBuilder = StringBuilder()

        doubleToAscii(value, Mode.Fixed, requestedDigits, decimalRep, sign, decimalPoint)

        if (sign.get() && value != 0.0)
            resultBuilder.append('-')

        createDecimalRepresentation(decimalRep.toString(), decimalPoint.get(), requestedDigits, resultBuilder)

        return resultBuilder.toString()
    }

    fun toExponential(value: Double, requestedDigits: Int): String? {
        if (EDouble(value).isSpecial())
            return handleSpecialValues(value)

        if (requestedDigits < -1 || requestedDigits > MAX_EXPONENTIAL_DIGITS)
            return null

        val decimalPoint = Ref<Int>()
        val sign = Ref<Boolean>()
        val decimalRep = StringBuilder()
        val resultBuilder = StringBuilder()

        if (requestedDigits == -1) {
            doubleToAscii(value, Mode.Shortest, 0, decimalRep, sign, decimalPoint)
        } else {
            doubleToAscii(value, Mode.Precision, requestedDigits + 1, decimalRep, sign, decimalPoint)
            expect(decimalRep.length <= requestedDigits + 1)

            for (i in decimalRep.length..requestedDigits)
                decimalRep.append('0')
        }

        if (sign.get() && value != 0.0)
            resultBuilder.append('-')

        createExponentialRepresentation(decimalRep.toString(), decimalPoint.get() - 1, resultBuilder)

        return resultBuilder.toString()
    }

    fun toPrecision(value: Double, precision: Int): String? {
        if (EDouble(value).isSpecial())
            return handleSpecialValues(value)

        if (precision !in MIN_PRECISION_DIGITS..MAX_PRECISION_DIGITS)
            return null


        val decimalPoint = Ref<Int>()
        val sign = Ref<Boolean>()
        val decimalRep = StringBuilder()
        val resultBuilder = StringBuilder()

        doubleToAscii(value, Mode.Precision, precision, decimalRep, sign, decimalPoint)
        expect(decimalRep.length <= precision)

        if (sign.get() && value != 0.0)
            resultBuilder.append('-')

        val exponent = decimalPoint.get() - 1

        val asExponential = -decimalPoint.get() + 1 > MAX_LEADING_PADDING_ZEROES_IN_PRECISION_MODE ||
            decimalPoint.get() - precision > MAX_TRAILING_PADDING_ZEROES_IN_PRECISION_MODE

        if (asExponential) {
            for (i in decimalRep.length until precision)
                decimalRep.append('0')

            createExponentialRepresentation(decimalRep.toString(), exponent, resultBuilder)
        } else {
            createDecimalRepresentation(decimalRep.toString(), decimalPoint.get(), max(0, precision - decimalPoint.get()), resultBuilder)
        }

        return resultBuilder.toString()
    }

    fun radixToString(value: Double, radix: Int): String? {
        if (EDouble(value).isSpecial())
            return handleSpecialValues(value)
        return radixDtoa(value, radix)
    }

    private fun toShortestIeeeNumber(value: Double, mode: Mode): String? {
        expect(mode == Mode.Shortest || mode == Mode.ShortestSingle)

        if (EDouble(value).isSpecial())
            return handleSpecialValues(value)

        val decimalPoint = Ref<Int>()
        val sign = Ref<Boolean>()
        val decimalRep = StringBuilder()
        val resultBuilder = StringBuilder()

        doubleToAscii(value, mode, 0, decimalRep, sign, decimalPoint)

        if (sign.get() && value != 0.0)
            resultBuilder.append('-')

        val exponent = decimalPoint.get() - 1
        if (exponent in DECIMAL_IN_SHORTEST_LOW until DECIMAL_IN_SHORTEST_HIGH) {
            createDecimalRepresentation(
                decimalRep.toString(),
                decimalPoint.get(),
                max(0, decimalRep.length - decimalPoint.get()),
                resultBuilder
            )
        } else {
            createExponentialRepresentation(decimalRep.toString(), exponent, resultBuilder)
        }

        return resultBuilder.toString()
    }

    private fun handleSpecialValues(value: Double): String? {
        val double = EDouble(value)
        return when {
            double.isInfinite() -> if (value < 0) "-Infinity" else "Infinity"
            double.isNaN() -> "NaN"
            else -> null
        }
    }

    internal fun doubleToAscii(
        value_: Double,
        mode: Mode,
        requestedDigits: Int,
        buffer: StringBuilder,
        sign: Ref<Boolean>,
        point: Ref<Int>
    ) {
        var value = value_
        expect(!EDouble(value).isSpecial())
        expect(mode == Mode.Shortest || mode == Mode.ShortestSingle || requestedDigits >= 0)

        if (EDouble(value).sign() < 0) {
            sign.set(true)
            value = -value
        } else {
            sign.set(false)
        }

        if (mode == Mode.Precision && requestedDigits == 0)
            return

        if (value == 0.0) {
            buffer.append('0')
            point.set(1)
            return
        }

        val worked = when (mode) {
            Mode.Shortest ->
                fastDtoa(value, FastDtoaMode.Shortest, 0, buffer, point)
            Mode.ShortestSingle ->
                fastDtoa(value, FastDtoaMode.ShortestSingle, 0, buffer, point)
            Mode.Fixed ->
                fastFixedDtoa(value, requestedDigits, buffer, point)
            Mode.Precision ->
                fastDtoa(value, FastDtoaMode.Precision, requestedDigits, buffer, point)
        }

        if (!worked)
            bignumDtoa(value, mode, requestedDigits, buffer, point)
    }

    private fun StringBuilder.appendRepeated(char: Char, times: Int) {//
        repeat(times) { append(char) }
    }

    private fun createDecimalRepresentation(
        decimalDigits: String,
        decimalPoint: Int,
        digitsAfterPoint: Int,
        resultBuilder: StringBuilder
    ) {
        if (decimalPoint <= 0) {
            resultBuilder.append('0')
            if (digitsAfterPoint > 0) {
                resultBuilder.append('.')
                resultBuilder.appendRepeated('0', -decimalPoint)
                expect(decimalDigits.length <= digitsAfterPoint + decimalPoint)
                resultBuilder.append(decimalDigits)
                val remainingDigits = digitsAfterPoint + decimalPoint - decimalDigits.length
                resultBuilder.appendRepeated('0', remainingDigits)
            }
        } else if (decimalPoint >= decimalDigits.length) {
            resultBuilder.append(decimalDigits)
            resultBuilder.appendRepeated('0', decimalPoint - decimalDigits.length)
            if (digitsAfterPoint > 0) {
                resultBuilder.append('.')
                resultBuilder.appendRepeated('0', digitsAfterPoint)
            }
        } else {
            expect(digitsAfterPoint > 0)
            resultBuilder.append(decimalDigits.take(decimalPoint))
            resultBuilder.append('.')
            expect(decimalDigits.length - decimalPoint <= digitsAfterPoint)
            resultBuilder.append(decimalDigits.substring(decimalPoint))
            val remainingDigits = digitsAfterPoint - (decimalDigits.length - decimalPoint)
            resultBuilder.appendRepeated('0', remainingDigits)
        }
    }

    private fun createExponentialRepresentation(decimalDigits: String, exponent_: Int, resultBuilder: StringBuilder) {
        var exponent = exponent_
        expect(decimalDigits.isNotEmpty())
        resultBuilder.append(decimalDigits.first())

        if (decimalDigits.length != 1) {
            resultBuilder.append('.')
            resultBuilder.append(decimalDigits.drop(1))
        }

        resultBuilder.append(EXPONENT_CHARACTER)
        if (exponent < 0) {
            resultBuilder.append('-')
            exponent = -exponent
        } else {
            resultBuilder.append('+')
        }

        expect(exponent < 1e4)

        val exponentBuilder = StringBuilder()
        exponentBuilder.appendRepeated('?', MAX_EXPONENT_LENGTH)
        var firstCharPos = MAX_EXPONENT_LENGTH
        if (exponent == 0) {
            exponentBuilder[--firstCharPos] = '0'
        } else {
            while (exponent > 0) {
                exponentBuilder[--firstCharPos] = '0' + (exponent % 10)
                exponent /= 10
            }
        }

        while (MAX_EXPONENT_LENGTH - firstCharPos < min(MIN_EXPONENT_WIDTH, MAX_EXPONENT_LENGTH))
            exponentBuilder[--firstCharPos] = '0'

        resultBuilder.append(exponentBuilder.toString().drop(firstCharPos))
    }

    enum class Mode {
        Shortest,
        ShortestSingle,
        Precision,
        Fixed,
    }
}