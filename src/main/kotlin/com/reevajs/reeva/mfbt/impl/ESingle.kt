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

package com.reevajs.reeva.mfbt.impl

class ESingle(val value: UInt) {
    constructor() : this(0U)
    constructor(value: Float) : this(value.toRawBits().toUInt())

    fun value() = Float.fromBits(value.toInt())

    fun significand(): UInt {
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

    fun asDiyFp(): DiyFp {
        expect(sign() > 0)
        expect(!isSpecial())
        return DiyFp(significand().toULong(), exponent())
    }

    fun isDenormal() = (value and EXPONENT_MASK) == 0U

    fun isSpecial() = (value and EXPONENT_MASK) == EXPONENT_MASK

    fun isNaN() = isSpecial() && (value and SIGNIFICAND_MASK) != 0U

    fun isQuietNaN() = isNaN() && (value and QUIET_NAN_BIT) != 0U

    fun isSignalingNan() = isNaN() && (value and QUIET_NAN_BIT) == 0U

    fun isInfinite() = isSpecial() && (value and SIGNIFICAND_MASK) == 0U

    fun sign() = if ((value and SIGN_MASK) == 0U) 1 else -1

    fun upperBoundary() = DiyFp((significand() * 2U + 1U).toULong(), exponent() - 1)

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
        val physicalSignificandIsZero = (value and SIGNIFICAND_MASK) == 0U
        return physicalSignificandIsZero && exponent() != DENORMAL_EXPONENT
    }

    companion object {
        const val SIGN_MASK = 0x80000000U
        const val EXPONENT_MASK = 0x7F800000U
        const val SIGNIFICAND_MASK = 0x007FFFFFU
        const val HIDDEN_BIT = 0x00800000U
        const val QUIET_NAN_BIT = 0x00400000U
        const val PHYSICAL_SIGNIFICAND_SIZE = 23
        const val SIGNIFICAND_SIZE = 24
        const val EXPONENT_BIAS = 0x7F + PHYSICAL_SIGNIFICAND_SIZE
        const val DENORMAL_EXPONENT = -EXPONENT_BIAS + 1
        const val MAX_EXPONENT = 0xFF - EXPONENT_BIAS
        const val INFINITY = 0x7F800000U
        const val NAN = 0x7FC00000U

        fun infinity() = ESingle(INFINITY).value()

        fun nan() = ESingle(NAN).value()
    }
}