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

class UInt128(private var highBits: ULong, private var lowBits: ULong) {
    constructor() : this(0UL, 0UL)

    fun multiply(multiplicand: UInt) {
        var accumulator: ULong = (lowBits and MASK32) * multiplicand
        var part = (accumulator and MASK32).toUInt()
        accumulator = accumulator shr 32
        accumulator = accumulator + (lowBits shr 32) * multiplicand
        lowBits = (accumulator shl 32) + part
        accumulator = accumulator shr 32
        accumulator = accumulator + (highBits and MASK32) * multiplicand
        part = (accumulator and MASK32).toUInt()
        accumulator = accumulator shr 32
        accumulator = accumulator + (highBits shr 32) * multiplicand
        highBits = (accumulator shl 32) + part
        expect((accumulator shr 32) == 0UL)
    }

    fun shift(shiftAmount: Int) {
        expect(shiftAmount in -64..64)

        when {
            shiftAmount == 0 -> return
            shiftAmount == -64 -> {
                highBits = lowBits
                lowBits = 0UL
            }
            shiftAmount == 64 -> {
                lowBits = highBits
                highBits = 0UL
            }
            shiftAmount <= 0 -> {
                highBits = highBits shl -shiftAmount
                highBits += lowBits shr (64 + shiftAmount)
                lowBits = lowBits shl -shiftAmount
            }
            else -> {
                lowBits = lowBits shr shiftAmount
                lowBits += highBits shl (64 - shiftAmount)
                highBits = highBits shr shiftAmount
            }
        }
    }

    fun divModPowerOf2(power: Int): Int {
        return if (power >= 64) {
            val result = (highBits shr (power - 64)).toInt()
            highBits -= result.toULong() shl (power - 64)
            result
        } else {
            val partLow = lowBits shr power
            val partHigh = highBits shl (64 - power)
            val result = (partLow + partHigh).toInt()
            highBits = 0UL
            lowBits -= partLow shl power
            result
        }
    }

    fun isZero() = highBits == 0UL && lowBits == 0UL

    fun bitAt(position: Int): Int {
        return if (position >= 64) {
            (highBits shr (position - 64)).toInt() and 1
        } else {
            (lowBits shr position).toInt() and 1
        }
    }

    companion object {
        private const val MASK32 = 0xFFFFFFFFUL
    }
}