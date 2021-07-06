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

package me.mattco.reeva.mfbt.impl

/**
 * Extended double, serves as the "Double" class from mfbt
 */
class EDouble(val value: ULong) {
    constructor() : this(0UL)
    constructor(value: Double) : this(value.toRawBits().toULong())
    constructor(value: DiyFp) : this(value.toULong())

    fun value() = Double.fromBits(value.toLong())

    fun significand(): ULong {
        val significand = value and SIGNIFICAND_MASK
        return if (isDenormal()) {
            significand
        } else significand + HIDDEN_BIT
    }

    fun exponent(): Int {
        if (isDenormal())
            return DENORMAL_EXPONENT

        val biasedE = ((value and EXPONENT_MASK) shr PHYSICAL_SIGNIFICAND_SIZE).toInt()
        return biasedE - EXPONENT_BIAS
    }

    fun asDiyFp() = DiyFp(significand(), exponent())

    fun isDenormal() = (value and EXPONENT_MASK) == 0UL

    fun isSpecial() = (value and EXPONENT_MASK) == EXPONENT_MASK

    fun isNaN() = isSpecial() && (value and SIGNIFICAND_MASK) != 0UL

    fun isQuietNaN() = isNaN() && (value and QUIET_NAN_BIT) != 0UL

    fun isSignalingNaN() = isNaN() && (value and QUIET_NAN_BIT) == 0UL

    fun isInfinite() = isSpecial() && (value and SIGNIFICAND_MASK) == 0UL

    fun sign() = if ((value and SIGN_MASK) == 0UL) 1 else -1

    fun upperBoundary() = DiyFp(significand() * 2UL + 1UL, exponent() - 1)

    fun normalizeBoundaries(outMinus: Ref<DiyFp>, outPlus: Ref<DiyFp>) {
        expect(value() > 0.0)

        val v = asDiyFp()
        val plus = DiyFp((v.significand shl 1) + 1UL, v.exponent - 1)
        plus.normalize()

        val minus = if (lowerBoundaryIsCloser()) {
            DiyFp((v.significand shl 2) - 1UL, v.exponent - 2)
        } else DiyFp((v.significand shl 1) - 1UL, v.exponent - 1)

        minus.significand = minus.significand shl (minus.exponent - plus.exponent)
        minus.exponent = plus.exponent

        outMinus.set(minus)
        outPlus.set(plus)
    }

    fun lowerBoundaryIsCloser(): Boolean {
        val physicalSignificandIsZero = (value and SIGNIFICAND_MASK) == 0UL
        return physicalSignificandIsZero && exponent() != DENORMAL_EXPONENT
    }

    fun asNormalizedDiyFp(): DiyFp {
        expect(value() > 0.0)
        var f = significand()
        var e = exponent()

        while ((f and HIDDEN_BIT) == 0UL) {
            f = f shl 1
            e--
        }

        f = f shl (DiyFp.SIGNIFICAND_SIZE - SIGNIFICAND_SIZE)
        e -= DiyFp.SIGNIFICAND_SIZE - SIGNIFICAND_SIZE
        return DiyFp(f, e)
    }

    fun nextDouble(): Double {
        if (value == INFINITY)
            return EDouble(INFINITY).value()
        if (sign() < 0 && significand() == 0UL)
            return 0.0

        if (sign() < 0)
            return EDouble(value - 1UL).value()
        return EDouble(value + 1UL).value()
    }

    fun previousDouble(): Double {
        if (value == (INFINITY or SIGN_MASK))
            return -infinity()

        if (sign() < 0)
            return EDouble(value + 1UL).value()

        if (significand() == 0UL)
            return -0.0

        return EDouble(value - 1UL).value()
    }

    companion object {
        const val SIGN_MASK = 0x8000000000000000UL
        const val EXPONENT_MASK = 0x7FF0000000000000UL
        const val SIGNIFICAND_MASK = 0x000FFFFFFFFFFFFFUL
        const val HIDDEN_BIT = 0x0010000000000000UL
        const val QUIET_NAN_BIT = 0x0008000000000000UL
        const val PHYSICAL_SIGNIFICAND_SIZE = 52
        const val SIGNIFICAND_SIZE = 53
        val EXPONENT_BIAS = 0x3FF + PHYSICAL_SIGNIFICAND_SIZE
        val MAX_EXPONENT = 0x7FF - EXPONENT_BIAS
        val DENORMAL_EXPONENT = -EXPONENT_BIAS + 1
        const val INFINITY = 0x7FF0000000000000UL
        const val NAN = 0x7FF8000000000000UL

        fun infinity() = EDouble(INFINITY).value()

        fun nan() = EDouble(NAN).value()
    }
}
