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

import java.math.BigInteger
import kotlin.math.log10
import kotlin.math.pow

const val MAX_SIGNIFICANT_BITS = 3584

class Bignum {
    private var impl = BigInteger.ZERO

    fun assignUInt16(value: UInt) {
        // expect(value <= UShort.MAX_VALUE)
        impl = BigInteger.valueOf(value.toLong())
    }

    fun assignUInt64(value: ULong) {
        if (value > MAX_LONG) {
            val overflow = value - MAX_LONG
            impl = MAX_LONG_BIGINT.add(BigInteger.valueOf(overflow.toLong()))
        } else {
            impl = BigInteger.valueOf(value.toLong())
        }
    }

    fun assignPowerUInt16(base: UInt, exponent: Int) {
        impl = BigInteger.valueOf(base.toLong()).pow(exponent)
    }

    fun assignBignum(value: Bignum) {
        impl = value.impl
    }

    fun assignDecimalString(value: String) {
        impl = BigInteger(value, 10)
    }

    fun assignHexString(value: String) {
        impl = BigInteger(value, 16)
    }

    fun addUInt64(value: ULong) {
        if (value > MAX_LONG) {
            val overflow = value - MAX_LONG
            impl = impl.add(MAX_LONG_BIGINT).add(BigInteger.valueOf(overflow.toLong()))
        }
    }

    fun addBignum(value: Bignum) {
        impl = impl.add(value.impl)
    }

    fun subtractBignum(value: Bignum) {
        expect(this >= value)
        impl = impl.subtract(value.impl)
    }

    fun square() {
        impl = impl.pow(2)
    }

    fun shiftLeft(value: Int) {
        impl = impl.shiftLeft(value)
    }

    fun multiplyByUInt32(value: UInt) {
        impl = impl.multiply(BigInteger.valueOf(value.toLong()))
    }

    fun multiplyByUInt64(value: ULong) {
        if (value > MAX_LONG) {
            val original = impl
            val dividedBy2 = BigInteger.valueOf((value shr 2).toLong())
            impl = impl.multiply(dividedBy2).multiply(TWO_BIGINT)
            if (value % 2U != 0UL)
                impl = impl.add(original)
        } else {
            impl = impl.multiply(BigInteger.valueOf(value.toLong()))
        }
    }

    fun multiplyByPowerOfTen(value: Int) {
        expect(value < log10(Long.MAX_VALUE.toDouble()))
        var n = 10L
        repeat(value - 1) { n *= 10L }
        impl = impl.multiply(BigInteger.valueOf(n))
    }

    fun times10() = multiplyByUInt32(10U)

    fun divideModuloIntBignum(value: Bignum): UInt {
        val (divisionResult, moduloResult) = impl.divideAndRemainder(value.impl)
        impl = moduloResult
        return divisionResult.toInt().toUInt()
    }

    fun toHexString(out: Ref<String>) = impl.toString(16)

    operator fun compareTo(other: Bignum) = impl.compareTo(other.impl)

    override fun equals(other: Any?) = other is Bignum && compareTo(other) == 0

    private fun zero() {
        impl = BigInteger.ZERO
    }

    override fun toString() = impl.toString(16)

    companion object {
        private val MAX_LONG = Long.MAX_VALUE.toULong()
        private val MAX_LONG_BIGINT = BigInteger.valueOf(Long.MAX_VALUE)
        private val TWO_BIGINT = BigInteger.valueOf(2L)

        fun plusCompare(a: Bignum, b: Bignum, c: Bignum) = (a.impl.add(b.impl)).compareTo(c.impl)

        fun plusEqual(a: Bignum, b: Bignum, c: Bignum): Boolean = plusCompare(a, b, c) == 0

        fun plusLessThan(a: Bignum, b: Bignum, c: Bignum) = plusCompare(a, b, c) <= 0

        fun plusLess(a: Bignum, b: Bignum, c: Bignum) = plusCompare(a, b, c) < 0
    }
}