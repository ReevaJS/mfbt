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

const val MINIMAL_TARGET_EXPONENT = -60
const val MAXIMAL_TARGET_EXPONENT = -32
const val FAST_DTOA_MAXIMAL_LENGTH = 17
const val FAST_DTOA_MAXIMAL_SINGLE_LENGTH = 9
val smallPowersOfTen = arrayOf(0U, 1U, 10U, 100U, 1000U, 10000U, 100000U, 1000000U, 10000000U, 100000000U, 1000000000U)

fun roundWeed(
    buffer: StringBuilder,
    distanceTooHighW: ULong,
    unsafeInterval: ULong,
    rest_: ULong,
    tenKappa: ULong,
    unit: ULong,
): Boolean {
    var rest = rest_
    val smallDistance = distanceTooHighW - unit
    val bigDistance = distanceTooHighW + unit

    expect(rest <= unsafeInterval)

    while (
        rest < smallDistance && unsafeInterval - rest >= tenKappa &&
        (rest + tenKappa < smallDistance || smallDistance - rest >= rest + tenKappa - smallDistance)
    ) {
        buffer[buffer.lastIndex]--
        rest += tenKappa
    }

    if (
        rest < bigDistance && unsafeInterval - rest >= tenKappa &&
        (rest + tenKappa < bigDistance || bigDistance - rest > rest + tenKappa - bigDistance)
    ) {
        return false
    }

    return unit * 2UL <= rest && rest <= unsafeInterval - unit * 4UL
}

fun roundWeedCounted(
    buffer: StringBuilder,
    rest: ULong,
    tenKappa: ULong,
    unit: ULong,
    kappa: Ref<Int>,
): Boolean {
    if (unit >= tenKappa)
        return false

    if (tenKappa - unit <= unit)
        return false

    if (tenKappa - rest > rest && tenKappa - 2UL * rest >= 2UL * unit)
        return true

    if (rest > unit && tenKappa - (rest - unit) <= rest - unit) {
        buffer[buffer.lastIndex]++

        for (i in buffer.lastIndex downTo 1) {
            if (buffer[i] != '0' + 10)
                break
            buffer[i] = '0'
            buffer[i - 1]++
        }

        if (buffer[0] == '0' + 10) {
            buffer[0] = '1'
            kappa.set(kappa.get() + 1)
        }

        return true
    }

    return false
}

fun biggestPowerTen(number: UInt, numberBits: Int, power: Ref<UInt>, exponentPlusOne: Ref<Int>) {
    var exponentPlusOneGuess = ((numberBits + 1) * 1233 shr 12)
    exponentPlusOneGuess++

    if (number < smallPowersOfTen[exponentPlusOneGuess])
        exponentPlusOneGuess--

    power.set(smallPowersOfTen[exponentPlusOneGuess])
    exponentPlusOne.set(exponentPlusOneGuess)
}

fun digitGen(low: DiyFp, w: DiyFp, high: DiyFp, buffer: StringBuilder, kappa: Ref<Int>): Boolean {
    expect(low.exponent == w.exponent && w.exponent == high.exponent)
    expect(low.significand + 1UL <= high.significand - 1UL)
    expect(w.exponent in MINIMAL_TARGET_EXPONENT..MAXIMAL_TARGET_EXPONENT)

    var unit = 1UL
    val tooLow = DiyFp(low.significand - unit, low.exponent)
    val tooHigh = DiyFp(high.significand + unit, high.exponent)

    val unsafeInterval = tooHigh - tooLow
    val one = DiyFp(1UL shl -w.exponent, w.exponent)
    var integrals = (tooHigh.significand shr -one.exponent).toUInt()
    var fractionals = tooHigh.significand and (one.significand - 1UL)

    val divisorRef = Ref<UInt>()
    val divisorExponentPlusOneRef = Ref<Int>()

    biggestPowerTen(integrals, DiyFp.SIGNIFICAND_SIZE - (-one.exponent), divisorRef, divisorExponentPlusOneRef)

    var divisor = divisorRef.get()
    val divisorExponentPlusOne = divisorExponentPlusOneRef.get()

    kappa.set(divisorExponentPlusOne)
    buffer.clear()

    while (kappa.get() > 0) {
        val digit = (integrals / divisor).toInt()
        expect(digit <= 9)
        buffer.append('0' + digit)
        integrals %= divisor
        kappa.set(kappa.get() - 1)

        val rest = (integrals.toULong() shl -one.exponent) + fractionals
        if (rest < unsafeInterval.significand) {
            return roundWeed(
                buffer,
                (tooHigh - w).significand,
                unsafeInterval.significand,
                rest,
                divisor.toULong() shl -one.exponent,
                unit
            )
        }

        divisor /= 10U
    }

    expect(one.exponent >= -60)
    expect(fractionals < one.significand)
    expect(0xFFFFFFFFFFFFFFFFUL / 10UL >= one.significand)

    while (true) {
        fractionals *= 10U
        unit *= 10UL
        unsafeInterval.significand *= 10UL
        val digit = (fractionals shr -one.exponent).toInt()
        expect(digit <= 9)
        buffer.append('0' + digit)
        fractionals = fractionals and (one.significand - 1UL)
        kappa.set(kappa.get() - 1)
        if (fractionals < unsafeInterval.significand) {
            return roundWeed(
                buffer,
                (tooHigh - w).significand * unit,
                unsafeInterval.significand,
                fractionals,
                one.significand,
                unit,
            )
        }
    }
}

fun digitGenCounted(
    w: DiyFp,
    requestedDigits_: Int,
    buffer: StringBuilder,
    kappa: Ref<Int>
): Boolean {
    var requestedDigits = requestedDigits_

    expect(w.exponent in MINIMAL_TARGET_EXPONENT..MAXIMAL_TARGET_EXPONENT)

    var wError = 1UL
    val one = DiyFp(1UL shl -w.exponent, w.exponent)
    var integrals = (w.significand shr -one.exponent).toUInt()
    var fractionals = w.significand and (one.significand - 1UL)

    val divisorRef = Ref<UInt>()
    val divisorExponentPlusOneRef = Ref<Int>()

    biggestPowerTen(integrals, DiyFp.SIGNIFICAND_SIZE - (-one.exponent), divisorRef, divisorExponentPlusOneRef)

    var divisor = divisorRef.get()
    val divisorExponentPlusOne = divisorExponentPlusOneRef.get()

    kappa.set(divisorExponentPlusOne)
    buffer.clear()

    while (kappa.get() > 0) {
        val digit = (integrals / divisor).toInt()
        expect(digit <= 9)
        buffer.append('0' + digit)
        requestedDigits--
        integrals %= divisor
        kappa.set(kappa.get() - 1)

        if (requestedDigits == 0)
            break

        divisor /= 10U
    }

    if (requestedDigits == 0) {
        val rest = (integrals.toULong() shl -one.exponent) + fractionals
        return roundWeedCounted(
            buffer,
            rest,
            divisor.toULong() shl -one.exponent,
            wError,
            kappa,
        )
    }

    expect(one.exponent >= -60)
    expect(fractionals < one.significand)
    expect(0xFFFFFFFFFFFFFFFFUL / 10UL >= one.significand)

    while (requestedDigits > 0 && fractionals > wError) {
        fractionals *= 10UL
        wError *= 10UL
        val digit = (fractionals shr -one.exponent).toInt()
        expect(digit <= 9)
        buffer.append('0' + digit)
        requestedDigits--
        fractionals = fractionals and (one.significand - 1UL)
        kappa.set(kappa.get() - 1)
    }

    if (requestedDigits != 0)
        return false

    return roundWeedCounted(buffer, fractionals, one.significand, wError, kappa)
}

fun grisu3(
    v: Double,
    mode: FastDtoaMode,
    buffer: StringBuilder,
    decimalExponent: Ref<Int>
): Boolean {
    val w = EDouble(v).asNormalizedDiyFp()
    val boundaryMinusRef = Ref<DiyFp>()
    val boundaryPlusRef = Ref<DiyFp>()

    if (mode == FastDtoaMode.Shortest) {
        EDouble(v).normalizeBoundaries(boundaryMinusRef, boundaryPlusRef)
    } else {
        expect(mode == FastDtoaMode.ShortestSingle)
        val singleV = v.toFloat()
        ESingle(singleV).normalizeBoundaries(boundaryMinusRef, boundaryPlusRef)
    }

    val boundaryMinus = boundaryMinusRef.get()
    val boundaryPlus = boundaryPlusRef.get()

    expect(boundaryPlus.exponent == w.exponent)

    val tenMkRef = Ref<DiyFp>()
    val mkRef = Ref<Int>()

    val tenMkMinimalBinaryExponent = MINIMAL_TARGET_EXPONENT - (w.exponent + DiyFp.SIGNIFICAND_SIZE)
    val tenMkMaximalBinaryExponent = MAXIMAL_TARGET_EXPONENT - (w.exponent + DiyFp.SIGNIFICAND_SIZE)

    getCachedPowerForBinaryExponentRange(tenMkMinimalBinaryExponent, tenMkMaximalBinaryExponent, tenMkRef, mkRef)

    val tenMk = tenMkRef.get()
    val mk = mkRef.get()

    expect(
        (MINIMAL_TARGET_EXPONENT <= w.exponent + tenMk.exponent + DiyFp.SIGNIFICAND_SIZE) &&
            (MAXIMAL_TARGET_EXPONENT >= w.exponent + tenMk.exponent + DiyFp.SIGNIFICAND_SIZE)
    )

    val scaledW = w * tenMk
    expect(scaledW.exponent == boundaryPlus.exponent + tenMk.exponent + DiyFp.SIGNIFICAND_SIZE)

    val scaledBoundaryMinus = boundaryMinus * tenMk
    val scaledBoundaryPlus = boundaryPlus * tenMk

    val kappa = Ref<Int>()
    val result = digitGen(scaledBoundaryMinus, scaledW, scaledBoundaryPlus, buffer, kappa)
    decimalExponent.set(-mk + kappa.get())
    return result
}

fun printBits(num: Double) {
    printBits(java.lang.Double.doubleToRawLongBits(num).toULong())
}

fun printBits(num: Float) {
    printBits(java.lang.Float.floatToRawIntBits(num).toUInt())
}

fun printBits(num_: ULong) {
    var num = num_
    val maxPow = 1UL shl 63
    for (i in 0..63) {
        val b = (num and maxPow) != 0UL
        print(if (b) '1' else '0')
        num = num shl 1
    }
    println()
}

fun printBits(num_: UInt) {
    var num = num_
    val maxPow = 1U shl 31
    for (i in 0..31) {
        val b = (num and maxPow) != 0U
        print(if (b) '1' else '0')
        num = num shl 1
    }
    println()
}

fun grisu3Counted(
    v: Double,
    requestedDigits: Int,
    buffer: StringBuilder,
    decimalExponent: Ref<Int>
): Boolean {
    val w = EDouble(v).asNormalizedDiyFp()

    val tenMkRef = Ref<DiyFp>()
    val mkRef = Ref<Int>()

    val tenMkMinimalBinaryExponent = MINIMAL_TARGET_EXPONENT - (w.exponent + DiyFp.SIGNIFICAND_SIZE)
    val tenMkMaximalBinaryExponent = MAXIMAL_TARGET_EXPONENT - (w.exponent + DiyFp.SIGNIFICAND_SIZE)

    getCachedPowerForBinaryExponentRange(tenMkMinimalBinaryExponent, tenMkMaximalBinaryExponent, tenMkRef, mkRef)

    val tenMk = tenMkRef.get()
    val mk = mkRef.get()

    expect(
        (MINIMAL_TARGET_EXPONENT <= w.exponent + tenMk.exponent + DiyFp.SIGNIFICAND_SIZE) &&
            (MAXIMAL_TARGET_EXPONENT >= w.exponent + tenMk.exponent + DiyFp.SIGNIFICAND_SIZE)
    )

    val scaledW = w * tenMk
    val kappa = Ref<Int>()
    val result = digitGenCounted(scaledW, requestedDigits, buffer, kappa)
    decimalExponent.set(-mk + kappa.get())
    return result
}

fun fastDtoa(
    v: Double,
    mode: FastDtoaMode,
    requestedDigits: Int,
    buffer: StringBuilder,
    decimalPoint: Ref<Int>
): Boolean {
    expect(v > 0.0)
    expect(!EDouble(v).isSpecial())

    val decimalExponent = Ref<Int>()

    val result = if (mode == FastDtoaMode.Precision) {
        grisu3Counted(v, requestedDigits, buffer, decimalExponent)
    } else {
        grisu3(v, mode, buffer, decimalExponent)
    }

    if (result)
        decimalPoint.set(buffer.length + decimalExponent.get())

    return result
}

enum class FastDtoaMode {
    Shortest,
    ShortestSingle,
    Precision,
}