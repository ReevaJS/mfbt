// Copyright 2010 the V8 project authors. All rights reserved.
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

package me.mattco.reeva.mfbt.impl

const val MAX_UINT_32 = 0xFFFFFFFFU
const val DOUBLE_SIGNIFICAND_SIZE = 53
const val FIVE_17 = 0xB1A2BC2EC5UL
const val TEN_7 = 10000000U

fun fillDigits32FixedLength(number_: UInt, requestedLength: Int, buffer: StringBuilder) {
    var number = number_
    val length = buffer.length
    var i = requestedLength - 1
    repeat(requestedLength) { buffer.append('?') }

    while (i >= 0) {
        buffer[length + i--] = '0' + (number % 10U).toInt()
        number /= 10U
    }
}

fun fillDigits32(number_: UInt, buffer: StringBuilder) {
    var number = number_
    var numberLength = 0
    val length = buffer.length

    while (number != 0U) {
        val digit = number % 10U
        number /= 10U
        buffer.append('0' + digit.toInt())
        numberLength++
    }

    var i = length
    var j = length + numberLength - 1
    while (i < j) {
        val tmp = buffer[i]
        buffer[i] = buffer[j]
        buffer[j] = tmp
        i++
        j--
    }
}

fun fillDigits64FixedLength(number_: ULong, buffer: StringBuilder) {
    var number = number_
    val part2 = (number % TEN_7).toUInt()
    number /= TEN_7
    val part1 = (number % TEN_7).toUInt()
    val part0 = (number / TEN_7).toUInt()

    fillDigits32FixedLength(part0, 3, buffer)
    fillDigits32FixedLength(part1, 7, buffer)
    fillDigits32FixedLength(part2, 7, buffer)
}

fun fillDigits64(number_: ULong, buffer: StringBuilder) {
    var number = number_
    val part2 = (number % TEN_7).toUInt()
    number /= TEN_7
    val part1 = (number % TEN_7).toUInt()
    val part0 = (number / TEN_7).toUInt()

    if (part0 != 0U) {
        fillDigits32(part0, buffer)
        fillDigits32FixedLength(part1, 7, buffer)
        fillDigits32FixedLength(part2, 7, buffer)
    } else if (part1 != 0U) {
        fillDigits32(part1, buffer)
        fillDigits32FixedLength(part2, 7, buffer)
    } else {
        fillDigits32(part2, buffer)
    }
}

fun roundUp(buffer: StringBuilder, decimalPoint: Ref<Int>) {
    if (buffer.isEmpty()) {
        buffer.append('1')
        decimalPoint.set(1)
        return
    }

    buffer[buffer.lastIndex]++
    for (i in buffer.lastIndex downTo 1) {
        if (buffer[i] != '0' + 10)
            return
        buffer[i] = '0'
        buffer[i - 1]++
    }

    if (buffer[0] == '0' + 10) {
        buffer[0] = '1'
        decimalPoint.set(decimalPoint.get() + 1)
    }
}

fun fillFractionals(
    fractionals_: ULong,
    exponent: Int,
    fractionalCount: Int,
    buffer: StringBuilder,
    decimalPoint: Ref<Int>
) {
    var fractionals = fractionals_
    expect(exponent in -128..0)

    if (-exponent <= 64) {
        expect(fractionals shr 56 == 0UL)
        var point = -exponent
        for (i in 0 until fractionalCount) {
            if (fractionals == 0UL)
                break

            fractionals *= 5UL
            point--
            val digit = (fractionals shr point).toInt()
            expect(digit <= 9)
            buffer.append('0' + digit)
            fractionals -= digit.toULong() shl point
        }

        expect(fractionals == 0UL || point - 1 >= 0)
        if (fractionals != 0UL && ((fractionals shr (point - 1)) and 1UL) == 1UL)
            roundUp(buffer, decimalPoint)
    } else {
        val fractionals128 = UInt128(fractionals, 0UL)
        fractionals128.shift(-exponent - 64)
        var point = 128
        for (i in 0 until fractionalCount) {
            if (fractionals128.isZero())
                break

            fractionals128.multiply(5U)
            point--
            val digit = fractionals128.divModPowerOf2(point)
            expect(digit <= 9)
            buffer.append('0' + digit)
        }
        if (fractionals128.bitAt(point - 1) == 1)
            roundUp(buffer, decimalPoint)
    }
}

fun trimZeros(buffer: StringBuilder, decimalPoint: Ref<Int>) {
    while (buffer.isNotEmpty() && buffer.last() == '0') {
        buffer.deleteCharAt(buffer.lastIndex)
    }

    var firstNonZero = 0
    while (firstNonZero < buffer.length && buffer[firstNonZero] == '0')
        firstNonZero++

    if (firstNonZero != 0) {
        for (i in firstNonZero until buffer.length)
            buffer[i - firstNonZero] = buffer[i]
        buffer.setLength(buffer.length - firstNonZero)
        decimalPoint.set(decimalPoint.get() - firstNonZero)
    }
}

fun fastFixedDtoa(v: Double, fractionalCount: Int, buffer: StringBuilder, decimalPoint: Ref<Int>): Boolean {
    var significand = EDouble(v).significand()
    val exponent = EDouble(v).exponent()

    if (exponent > 20 || fractionalCount > 20)
        return false

    if (exponent + DOUBLE_SIGNIFICAND_SIZE > 64) {
        var divisor = FIVE_17
        val divisorPower = 17
        var dividend = significand
        val quotient: UInt
        val remainder: ULong

        if (exponent > divisorPower) {
            dividend = dividend shl (exponent - divisorPower)
            quotient = (dividend / divisor).toUInt()
            remainder = (dividend % divisor) shl divisorPower
        } else {
            divisor = divisor shl (divisorPower - exponent)
            quotient = (dividend / divisor).toUInt()
            remainder = (dividend % divisor) shl exponent
        }

        fillDigits32(quotient, buffer)
        fillDigits64FixedLength(remainder, buffer)
        decimalPoint.set(buffer.length)
    } else if (exponent >= 0) {
        significand = significand shl exponent
        fillDigits64(significand, buffer)
        decimalPoint.set(buffer.length)
    } else if (exponent > -DOUBLE_SIGNIFICAND_SIZE) {
        val integrals = significand shr -exponent
        val fractionals = significand - (integrals shl -exponent)
        if (integrals > MAX_UINT_32) {
            fillDigits64(integrals, buffer)
        } else {
            fillDigits32(integrals.toUInt(), buffer)
        }
        decimalPoint.set(buffer.length)
        fillFractionals(fractionals, exponent, fractionalCount, buffer, decimalPoint)
    } else if (exponent < -128) {
        expect(fractionalCount <= 20)
        buffer.clear()
        buffer.append('0')
        decimalPoint.set(-fractionalCount)
    } else {
        decimalPoint.set(0)
        fillFractionals(significand, exponent, fractionalCount, buffer, decimalPoint)
    }

    trimZeros(buffer, decimalPoint)
    if (buffer.isEmpty())
        decimalPoint.set(-fractionalCount)

    return true
}