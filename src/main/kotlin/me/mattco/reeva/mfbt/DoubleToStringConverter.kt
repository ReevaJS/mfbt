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

import kotlin.math.max
import kotlin.math.min

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

object DoubleToStringConverter {
    fun toShortest(value: Double, builder: StringBuilder): Boolean {
        return toShortestIeeeNumber(value, builder, DtoaMode.Shortest)
    }

    fun toShortestSingle(value: Double, builder: StringBuilder): Boolean {
        return toShortestIeeeNumber(value, builder, DtoaMode.ShortestSingle)
    }

    private fun toShortestIeeeNumber(value: Double, result: StringBuilder, mode: DtoaMode): Boolean {
        expect(mode == DtoaMode.Shortest || mode == DtoaMode.ShortestSingle)

        if (EDouble(value).isSpecial())
            return handleSpecialValues(value, result)

        val decimalRep = StringBuilder()
        val sign = Ref<Boolean>()
        val decimalPoint = Ref<Int>()

        doubleToAscii(value, mode, 0, decimalRep, sign, decimalPoint)

        // ECMAScript always has the unique zero flag
        if (sign.get() && value != 0.0)
            result.append('-')

        val exponent = decimalPoint.get() - 1
        if (exponent in DECIMAL_IN_SHORTEST_LOW until DECIMAL_IN_SHORTEST_HIGH) {
            createDecimalRepresentation(
                decimalRep.toString(),
                decimalPoint.get(),
                max(0, decimalRep.length - decimalPoint.get()),
                result,
            )
        } else {
            createExponentialRepresentation(decimalRep.toString(), exponent, result)
        }

        return true
    }

    private fun StringBuilder.appendRepeated(char: Char, amount: Int) {
        repeat(amount) { append(char) }
    }

    private fun createDecimalRepresentation(
        decimalDigits: String,
        decimalPoint: Int,
        digitsAfterPoint: Int,
        result: StringBuilder
    ) {
        val length = decimalDigits.length
        if (decimalPoint <= 0) {
            result.append('0')
            if (digitsAfterPoint > 0) {
                result.append('.')
                result.appendRepeated('0', -decimalPoint)
                expect(length <= digitsAfterPoint - (-decimalPoint))
                result.append(decimalDigits)
                result.appendRepeated('0', digitsAfterPoint - (-decimalPoint) - length)
            }
        } else if (decimalPoint >= length) {
            result.append(decimalDigits)
            result.appendRepeated('0', decimalPoint - length)
            if (digitsAfterPoint > 0) {
                result.append('.')
                result.appendRepeated('0', digitsAfterPoint)
            }
        } else {
            expect(digitsAfterPoint > 0)
            result.append(decimalDigits.substring(0, decimalPoint))
            result.append('.')
            expect(length - decimalPoint <= digitsAfterPoint)
            result.append(decimalDigits.substring(decimalPoint, length - decimalPoint))
            result.appendRepeated('0', digitsAfterPoint - (length - decimalPoint))
        }
    }

    private fun createExponentialRepresentation(
        decimalDigits: String,
        exponent_: Int,
        result: StringBuilder
    ) {
        var exponent = exponent_
        val length = decimalDigits.length
        expect(length != 0)
        result.append(decimalDigits[0])

        if (length != 1) {
            result.append('.')
            result.append(decimalDigits.substring(1, length - 1))
        }

        result.append(EXPONENT_CHARACTER)
        if (exponent < 0) {
            result.append('-')
            exponent = -exponent
        } else {
            // EMIT_POSITIVE_EXPONENT_FLAG is always set for ECMAScript
            result.append('+')
        }

        expect(exponent < 1e4)
        val buffer = StringBuilder()
        var firstCharPos = MAX_EXPONENT_LENGTH
        if (exponent == 0) {
            buffer[--firstCharPos] = '0'
        } else {
            while (exponent > 0) {
                buffer[--firstCharPos] = '0' + (exponent % 10)
                exponent /= 10
            }
        }

        // MIN_EXPONENT_WIDTH = 0
        while (MAX_EXPONENT_LENGTH - firstCharPos < min(MIN_EXPONENT_WIDTH, MAX_EXPONENT_LENGTH))
            buffer[--firstCharPos] = '0'

        result.append(buffer.substring(firstCharPos, MAX_EXPONENT_LENGTH - firstCharPos))
    }

    private fun handleSpecialValues(value: Double, builder: StringBuilder): Boolean {
        val doubleInspect = EDouble(value)
        if (doubleInspect.isInfinite()) {
            if (value < 0)
                builder.append('-')
            builder.append(INFINITY_SYMBOL)
            return true
        }

        if (doubleInspect.isNaN()) {
            builder.append(NAN_SYMBOL)
            return true
        }

        return false
    }

    private fun doubleToAscii(
        v_: Double,
        mode: DtoaMode,
        requestedDigits: Int,
        buffer: StringBuilder,
        sign: Ref<Boolean>,
        point: Ref<Int>
    ) {
        var v = v_
        expect(!EDouble(v).isSpecial())
        expect(mode == DtoaMode.Shortest || mode == DtoaMode.ShortestSingle || requestedDigits >= 0)

        if (EDouble(v).sign() < 0) {
            sign.set(true)
            v = -v
        } else {
            sign.set(false)
        }

        if (mode == DtoaMode.Precision && requestedDigits == 0)
            return

        if (v == 0.0) {
            buffer.append('0')
            point.set(1)
            return
        }

        val fastWorked = when (mode) {
            DtoaMode.Shortest -> fastDtoa(v, FastDtoaMode.Shortest, 0, buffer, point)
            DtoaMode.ShortestSingle -> fastDtoa(v, FastDtoaMode.ShortestSingle, 0, buffer, point)
            DtoaMode.Precision -> fastDtoa(v, FastDtoaMode.Precision, requestedDigits, buffer, point)
            DtoaMode.Fixed -> TODO()
        }

        if (fastWorked)
            return

        TODO()
    }

    enum class DtoaMode {
        Shortest,
        ShortestSingle,
        Fixed,
        Precision,
    }
}