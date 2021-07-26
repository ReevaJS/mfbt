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

package com.reevajs.reeva.mfbt.impl

import com.reevajs.reeva.mfbt.impl.EDouble.Companion.DENORMAL_EXPONENT
import com.reevajs.reeva.mfbt.impl.EDouble.Companion.EXPONENT_BIAS
import com.reevajs.reeva.mfbt.impl.EDouble.Companion.HIDDEN_BIT
import com.reevajs.reeva.mfbt.impl.EDouble.Companion.MAX_EXPONENT
import com.reevajs.reeva.mfbt.impl.EDouble.Companion.PHYSICAL_SIGNIFICAND_SIZE
import com.reevajs.reeva.mfbt.impl.EDouble.Companion.SIGNIFICAND_MASK

class DiyFp(var significand: ULong, var exponent: Int) {
    constructor() : this(0UL, 0)

    operator fun minus(other: DiyFp): DiyFp {
        return DiyFp(significand - other.significand, exponent)
    }

    operator fun times(other: DiyFp): DiyFp {
        val a = significand shr 32
        val b = significand and M32
        val c = other.significand shr 32
        val d = other.significand and M32
        val ac = a * c
        val bc = b * c
        val ad = a * d
        val bd = b * d

        val tmp = (bd shr 32) + (ad and M32) + (bc and M32) + (1U shl 31)
        return DiyFp(ac + (ad shr 32) + (bc shr 32) + (tmp shr 32), exponent + other.exponent + 64)
    }

    fun normalize() {
        expect(significand != 0UL)
        var s = significand
        var e = exponent

        while ((s and MS10BITS) == 0UL) {
            s = s shl 10
            e -= 10
        }

        while ((s and UINT64MSB) == 0UL) {
            s = s shl 1
            e--
        }

        significand = s
        exponent = e
    }

    // ieee.h: DiyFpToUin64
    fun toULong(): ULong {
        var s = significand
        var e = exponent
        while (s > HIDDEN_BIT + SIGNIFICAND_MASK) {
            s = s shr 1
            e++
        }

        if (e >= MAX_EXPONENT)
            return EDouble.INFINITY
        if (e < DENORMAL_EXPONENT)
            return 0UL

        while (e > DENORMAL_EXPONENT && (s and HIDDEN_BIT) == 0UL) {
            s = s shl 1
            e--
        }

        val biasedExponent = if (e == DENORMAL_EXPONENT && (s and HIDDEN_BIT) == 0UL) {
            0UL
        } else (e + EXPONENT_BIAS).toULong()
        return (s and SIGNIFICAND_MASK) or (biasedExponent shl PHYSICAL_SIGNIFICAND_SIZE)
    }

    companion object {
        const val SIGNIFICAND_SIZE = 64
        const val M32 = 0xFFFFFFFFUL
        const val MS10BITS = 0xFFC0000000000000UL
        const val UINT64MSB = 0x8000000000000000UL

    }
}