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

import me.mattco.reeva.mfbt.Dtoa
import kotlin.math.ceil

const val LOG_1_10 = 0.30102999566398114

fun normalizedExponent(significand_: ULong, exponent_: Int): Int {
    var significand = significand_
    var exponent = exponent_
    expect(significand != 0UL)
    while ((significand and EDouble.HIDDEN_BIT) == 0UL) {
        significand = significand shl 1
        exponent--
    }
    return exponent
}

fun estimatePower(exponent: Int): Int {
    val estimate = ceil((exponent + EDouble.SIGNIFICAND_SIZE - 1) * LOG_1_10 - 1e-10)
    return estimate.toInt()
}

fun initialScaledStartValuesPositiveExponent(
    significand: ULong,
    exponent: Int,
    estimatedPower: Int,
    needBoundaryDeltas: Boolean,
    numerator: Bignum,
    denominator: Bignum,
    deltaMinus: Bignum,
    deltaPlus: Bignum,
) {
    expect(estimatedPower >= 0)

    numerator.assignUInt64(significand)
    numerator.shiftLeft(exponent)
    denominator.assignPowerUInt16(10U, estimatedPower)

    if (needBoundaryDeltas) {
        denominator.shiftLeft(1)
        numerator.shiftLeft(1)
        deltaPlus.assignUInt16(1U)
        deltaPlus.shiftLeft(exponent)
        deltaMinus.assignUInt16(1U)
        deltaMinus.shiftLeft(exponent)
    }
}

fun initialScaledStartValuesNegativeExponentPositivePower(
    significand: ULong,
    exponent: Int,
    estimatedPower: Int,
    needBoundaryDeltas: Boolean,
    numerator: Bignum,
    denominator: Bignum,
    deltaMinus: Bignum,
    deltaPlus: Bignum,
) {
    numerator.assignUInt64(significand)
    denominator.assignPowerUInt16(10U, estimatedPower)
    denominator.shiftLeft(-exponent)

    if (needBoundaryDeltas) {
        denominator.shiftLeft(1)
        numerator.shiftLeft(1)
        deltaPlus.assignUInt16(1U)
        deltaMinus.assignUInt16(1U)
    }
}

fun initialScaledStartValuesNegativeExponentNegativePower(
    significand: ULong,
    exponent: Int,
    estimatedPower: Int,
    needBoundaryDeltas: Boolean,
    numerator: Bignum,
    denominator: Bignum,
    deltaMinus: Bignum,
    deltaPlus: Bignum,
) {
    numerator.assignPowerUInt16(10U, -estimatedPower)

    if (needBoundaryDeltas) {
        deltaPlus.assignBignum(numerator)
        deltaMinus.assignBignum(numerator)
    }

    numerator.multiplyByUInt64(significand)
    denominator.assignUInt16(1U)
    denominator.shiftLeft(-exponent)

    if (needBoundaryDeltas) {
        numerator.shiftLeft(1)
        denominator.shiftLeft(1)
    }
}

fun initialScaledStartValues(
    significand: ULong,
    exponent: Int,
    lowerBoundaryIsCloser: Boolean,
    estimatedPower: Int,
    needBoundaryDeltas: Boolean,
    numerator: Bignum,
    denominator: Bignum,
    deltaMinus: Bignum,
    deltaPlus: Bignum,
) {
    if (exponent >= 0) {
        initialScaledStartValuesPositiveExponent(
            significand,
            exponent,
            estimatedPower,
            needBoundaryDeltas,
            numerator,
            denominator,
            deltaMinus,
            deltaPlus
        )
    } else if (estimatedPower >= 0) {
        initialScaledStartValuesNegativeExponentPositivePower(
            significand,
            exponent,
            estimatedPower,
            needBoundaryDeltas,
            numerator,
            denominator,
            deltaMinus,
            deltaPlus
        )
    } else {
        initialScaledStartValuesNegativeExponentNegativePower(
            significand,
            exponent,
            estimatedPower,
            needBoundaryDeltas,
            numerator,
            denominator,
            deltaMinus,
            deltaPlus
        )
    }

    if (needBoundaryDeltas && lowerBoundaryIsCloser) {
        denominator.shiftLeft(1)
        numerator.shiftLeft(1)
        deltaPlus.shiftLeft(1)
    }
}

fun fixupMultiply10(
    estimatedPower: Int,
    isEven: Boolean,
    decimalPoint: Ref<Int>,
    numerator: Bignum,
    denominator: Bignum,
    deltaMinus: Bignum,
    deltaPlus: Bignum,
) {
    val inRange = if (isEven) {
        Bignum.plusCompare(numerator, deltaPlus, denominator) >= 0
    } else {
        Bignum.plusCompare(numerator, deltaPlus, denominator) > 0
    }

    if (inRange) {
        decimalPoint.set(estimatedPower + 1)
    } else {
        decimalPoint.set(estimatedPower)
        numerator.times10()
        deltaMinus.times10()
        if (deltaMinus == deltaPlus) {
            deltaPlus.assignBignum(deltaMinus)
        } else {
            deltaPlus.times10()
        }
    }
}

fun generateShortestDigits(
    numerator: Bignum,
    denominator: Bignum,
    deltaMinus: Bignum,
    deltaPlus: Bignum,
    isEven: Boolean,
    buffer: StringBuilder,
) {
    buffer.clear()
    while (true) {
        val digit = numerator.divideModuloIntBignum(denominator).toInt()
        expect(digit <= 9)
        buffer.append('0' + digit)

        val inDeltaRoomMinus = if (isEven) {
            numerator <= deltaMinus
        } else numerator < deltaMinus
        val inDeltaRoomPlus = if (isEven) {
            Bignum.plusCompare(numerator, deltaPlus, denominator) >= 0
        } else Bignum.plusCompare(numerator, deltaPlus, denominator) > 0

        if (!inDeltaRoomMinus && !inDeltaRoomPlus) {
            numerator.times10()
            deltaMinus.times10()
            deltaPlus.times10()
        } else if (inDeltaRoomMinus && inDeltaRoomPlus) {
            val compare = Bignum.plusCompare(numerator, numerator, denominator)
            if (compare > 0) {
                expect(buffer.last() != '9')
                buffer[buffer.lastIndex]++
            } else if (compare == 0) {
                if ((buffer.last() - '0') % 2 != 0) {
                    expect(buffer.last() != '9')
                    buffer[buffer.lastIndex]++
                }
            }
            return
        } else {
            if (!inDeltaRoomMinus) {
                expect(buffer.last() != '9')
                buffer[buffer.lastIndex]++
            }
            return
        }
    }
}

fun generateCountedDigits(
    count: Int,
    decimalPoint: Ref<Int>,
    numerator: Bignum,
    denominator: Bignum,
    buffer: StringBuilder
) {
    expect(count >= 0)
    buffer.ensureSize(count)
    for (i in 0 until count - 1) {
        val digit = numerator.divideModuloIntBignum(denominator).toInt()
        expect(digit <= 9)
        buffer[i] = '0' + digit
        numerator.times10()
    }
    var digit = numerator.divideModuloIntBignum(denominator).toInt()
    if (Bignum.plusCompare(numerator, numerator, denominator) >= 0)
        digit++

    expect(digit <= 10)
    buffer[count - 1] = '0' + digit

    for (i in count - 1 downTo 1) {
        if (buffer[i] != '0' + 10)
            break
        buffer[i] = '0'
        buffer[i - 1]++
    }
    if (buffer[0] == '0' + 10) {
        buffer[0] = '1'
        decimalPoint.set(decimalPoint.get() + 1)
    }
    buffer.setLength(count)
}

fun bignumToFixed(
    requestedDigits: Int,
    decimalPoint: Ref<Int>,
    numerator: Bignum,
    denominator: Bignum,
    buffer: StringBuilder
) {
    if (-decimalPoint.get() > requestedDigits) {
        decimalPoint.set(-requestedDigits)
        return
    }

    if (-decimalPoint.get() == requestedDigits) {
        denominator.times10()
        if (Bignum.plusCompare(numerator, numerator, denominator) >= 0) {
            buffer[0] = '1'
            decimalPoint.set(decimalPoint.get() + 1)
        }
        return
    }

    generateCountedDigits(decimalPoint.get() + requestedDigits, decimalPoint, numerator, denominator, buffer)
}

fun bignumDtoa(v: Double, mode: Dtoa.Mode, requestedDigits: Int, buffer: StringBuilder, decimalPoint: Ref<Int>) {
    expect(v >= 0)
    expect(!EDouble(v).isSpecial())

    val significand: ULong
    var exponent: Int
    var lowerBoundaryIsCloser: Boolean

    if (mode == Dtoa.Mode.ShortestSingle) {
        val f = ESingle(v.toFloat())
        significand = f.significand().toULong()
        exponent = f.exponent()
        lowerBoundaryIsCloser = f.lowerBoundaryIsCloser()
    } else {
        val d = EDouble(v)
        significand = d.significand()
        exponent = d.exponent()
        lowerBoundaryIsCloser = d.lowerBoundaryIsCloser()
    }

    val needBoundaryDeltas = mode == Dtoa.Mode.Shortest || mode == Dtoa.Mode.ShortestSingle
    val isEven = (significand and 1U) == 0UL
    val normalizedExponent = normalizedExponent(significand, exponent)
    val estimatedPower = estimatePower(normalizedExponent)

    if (mode == Dtoa.Mode.Fixed && -estimatedPower - 1 > requestedDigits) {
        buffer.clear()
        decimalPoint.set(-requestedDigits)
        return
    }

    val numerator = Bignum()
    val denominator = Bignum()
    val deltaMinus = Bignum()
    val deltaPlus = Bignum()

    initialScaledStartValues(
        significand,
        exponent,
        lowerBoundaryIsCloser,
        estimatedPower,
        needBoundaryDeltas,
        numerator,
        denominator,
        deltaMinus,
        deltaPlus
    )

    fixupMultiply10(estimatedPower, isEven, decimalPoint, numerator, denominator, deltaMinus, deltaPlus)

    when (mode) {
        Dtoa.Mode.Shortest, Dtoa.Mode.ShortestSingle ->
            generateShortestDigits(numerator, denominator, deltaMinus, deltaPlus, isEven, buffer)
        Dtoa.Mode.Fixed -> bignumToFixed(requestedDigits, decimalPoint, numerator, denominator, buffer)
        Dtoa.Mode.Precision -> generateCountedDigits(requestedDigits, decimalPoint, numerator, denominator, buffer)
    }
}
