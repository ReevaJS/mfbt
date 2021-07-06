// Copyright 2006-2008 the V8 project authors. All rights reserved.
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

import me.mattco.reeva.mfbt.impl.DiyFp
import me.mattco.reeva.mfbt.impl.EDouble
import me.mattco.reeva.mfbt.impl.ESingle
import me.mattco.reeva.mfbt.impl.Ref
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class IEEETest {
    @Test
    fun `UInt64 conversions`() {
        val ordered = 0x0123456789ABCDEFUL
        Assertions.assertEquals(3512700564088504e-318, EDouble(ordered).value())

        val minDouble64 = 0x0000000000000001UL
        Assertions.assertEquals(5e-324, EDouble(minDouble64).value())

        val maxDouble64 = 0x7fefffffffffffffUL
        Assertions.assertEquals(1.7976931348623157e308, EDouble(maxDouble64).value())
    }

    @Test
    fun `Uint32 conversions`() {
        val ordered = 0x01234567U
        Assertions.assertEquals(2.9988165487136453e-38f, ESingle(ordered).value())

        val minFloat32 = 0x00000001U
        Assertions.assertEquals(1.4e-45f, ESingle(minFloat32).value())

        val maxFloat32 = 0x7f7fffffU
        Assertions.assertEquals(3.4028234e38f, ESingle(maxFloat32).value())
    }

    @Test
    fun `EDouble asDiyFp`() {
        val ordered = 0x0123456789ABCDEFUL
        var diyFp = EDouble(ordered).asDiyFp()
        Assertions.assertEquals(0x12 - 0x3FF - 52, diyFp.exponent)
        Assertions.assertTrue(0x0013456789ABCDEFUL == diyFp.significand)

        val minDouble64 = 0x0000000000000001UL
        diyFp = EDouble(minDouble64).asDiyFp()
        Assertions.assertEquals(-0x3FF - 52 + 1, diyFp.exponent)
        Assertions.assertEquals(1UL, diyFp.significand)

        val maxDouble64 = 0x7fefffffffffffffUL
        diyFp = EDouble(maxDouble64).asDiyFp()
        Assertions.assertEquals(0x7FE - 0x3FF - 52, diyFp.exponent)
        Assertions.assertEquals(0x001fffffffffffffUL, diyFp.significand)
    }
    
    @Test
    fun `ESingle asDiyFp`() {
        val ordered = 0x01234567U
        var diyFp = ESingle(ordered).asDiyFp()
        Assertions.assertEquals(0x2 - 0x7F - 23, diyFp.exponent)
        Assertions.assertEquals(0xA34567UL, diyFp.significand)

        val minFloat32 = 0x00000001U
        diyFp = ESingle(minFloat32).asDiyFp()
        Assertions.assertEquals(-0x7F - 23 + 1, diyFp.exponent)
        Assertions.assertEquals(1UL, diyFp.significand)

        val maxFloat32 = 0x7f7fffffU
        diyFp = ESingle(maxFloat32).asDiyFp()
        Assertions.assertEquals(0xFE - 0x7F - 23, diyFp.exponent)
        Assertions.assertEquals(0x00ffffffUL, diyFp.significand)
    }
    
    @Test
    fun `asNormalizedDiyFp`() {
        val ordered = 0x0123456789ABCDEFUL
        var diyFp = EDouble(ordered).asNormalizedDiyFp()
        Assertions.assertEquals(0x12 - 0x3FF - 52 - 11, diyFp.exponent)
        Assertions.assertEquals(0x0013456789ABCDEFUL shl 11, diyFp.significand)

        val minDouble64 = 0x0000000000000001UL
        diyFp = EDouble(minDouble64).asNormalizedDiyFp()
        Assertions.assertEquals(-0x3FF - 52 + 1 - 63, diyFp.exponent)
        Assertions.assertEquals(0x8000000000000000UL, diyFp.significand)

        val maxDouble64 = 0x7fefffffffffffffUL
        diyFp = EDouble(maxDouble64).asNormalizedDiyFp()
        Assertions.assertEquals(0x7FE - 0x3FF - 52 - 11, diyFp.exponent)
        Assertions.assertEquals(0x001fffffffffffffUL shl 11, diyFp.significand)
    }

    @Test
    fun `EDouble isDenormal`() {
        val minDouble64 = 0x0000000000000001UL
        Assertions.assertTrue(EDouble(minDouble64).isDenormal())
        var bits = 0x000FFFFFFFFFFFFFUL
        Assertions.assertTrue(EDouble(bits).isDenormal())
        bits = 0x0010000000000000UL
        Assertions.assertTrue(!EDouble(bits).isDenormal())
    }

    @Test
    fun `ESingle isDenormal`() {
        val minFloat32 = 0x00000001U
        Assertions.assertTrue(ESingle(minFloat32).isDenormal())
        var bits = 0x007FFFFFU
        Assertions.assertTrue(ESingle(bits).isDenormal())
        bits = 0x00800000U
        Assertions.assertTrue(!ESingle(bits).isDenormal())
    }

    @Test
    fun `Double isSpecial`() {
        Assertions.assertTrue(EDouble(EDouble.infinity()).isSpecial())
        Assertions.assertTrue(EDouble(-EDouble.infinity()).isSpecial())
        Assertions.assertTrue(EDouble(EDouble.nan()).isSpecial())
        val bits = 0xFFF1234500000000UL
        Assertions.assertTrue(EDouble(bits).isSpecial())
        // Denormals are not special:
        Assertions.assertTrue(!EDouble(5e-324).isSpecial())
        Assertions.assertTrue(!EDouble(-5e-324).isSpecial())
        // And some random numbers:
        Assertions.assertTrue(!EDouble(0.0).isSpecial())
        Assertions.assertTrue(!EDouble(-0.0).isSpecial())
        Assertions.assertTrue(!EDouble(1.0).isSpecial())
        Assertions.assertTrue(!EDouble(-1.0).isSpecial())
        Assertions.assertTrue(!EDouble(1000000.0).isSpecial())
        Assertions.assertTrue(!EDouble(-1000000.0).isSpecial())
        Assertions.assertTrue(!EDouble(1e23).isSpecial())
        Assertions.assertTrue(!EDouble(-1e23).isSpecial())
        Assertions.assertTrue(!EDouble(1.7976931348623157e308).isSpecial())
        Assertions.assertTrue(!EDouble(-1.7976931348623157e308).isSpecial())
    }
    
    @Test
    fun `Single isSpecial`() {
        Assertions.assertTrue(ESingle(ESingle.infinity()).isSpecial())
        Assertions.assertTrue(ESingle(-ESingle.infinity()).isSpecial())
        Assertions.assertTrue(ESingle(ESingle.nan()).isSpecial())
        val bits = 0xFFF12345U
        Assertions.assertTrue(ESingle(bits).isSpecial())
        // Denormals are not special:
        Assertions.assertTrue(!ESingle(1.4e-45f).isSpecial())
        Assertions.assertTrue(!ESingle(-1.4e-45f).isSpecial())
        // And some random numbers:
        Assertions.assertTrue(!ESingle(0.0f).isSpecial())
        Assertions.assertTrue(!ESingle(-0.0f).isSpecial())
        Assertions.assertTrue(!ESingle(1.0f).isSpecial())
        Assertions.assertTrue(!ESingle(-1.0f).isSpecial())
        Assertions.assertTrue(!ESingle(1000000.0f).isSpecial())
        Assertions.assertTrue(!ESingle(-1000000.0f).isSpecial())
        Assertions.assertTrue(!ESingle(1e23f).isSpecial())
        Assertions.assertTrue(!ESingle(-1e23f).isSpecial())
        Assertions.assertTrue(!ESingle(1.18e-38f).isSpecial())
        Assertions.assertTrue(!ESingle(-1.18e-38f).isSpecial())
    }

    @Test
    fun `Double isInfinite`() {
        Assertions.assertTrue(EDouble(EDouble.infinity()).isInfinite())
        Assertions.assertTrue(EDouble(-EDouble.infinity()).isInfinite())
        Assertions.assertTrue(!EDouble(EDouble.nan()).isInfinite())
        Assertions.assertTrue(!EDouble(0.0).isInfinite())
        Assertions.assertTrue(!EDouble(-0.0).isInfinite())
        Assertions.assertTrue(!EDouble(1.0).isInfinite())
        Assertions.assertTrue(!EDouble(-1.0).isInfinite())
        val minDouble64 = 0x0000000000000001UL
        Assertions.assertTrue(!EDouble(minDouble64).isInfinite())
    }
    
    @Test
    fun `Single isInfinite`() {
        Assertions.assertTrue(ESingle(ESingle.infinity()).isInfinite())
        Assertions.assertTrue(ESingle(-ESingle.infinity()).isInfinite())
        Assertions.assertTrue(!ESingle(ESingle.nan()).isInfinite())
        Assertions.assertTrue(!ESingle(0.0f).isInfinite())
        Assertions.assertTrue(!ESingle(-0.0f).isInfinite())
        Assertions.assertTrue(!ESingle(1.0f).isInfinite())
        Assertions.assertTrue(!ESingle(-1.0f).isInfinite())
        val minFloat32 = 0x00000001U
        Assertions.assertTrue(!ESingle(minFloat32).isInfinite())
    }
    
    @Test
    fun `Double isNaN`() {
        Assertions.assertTrue(EDouble(EDouble.nan()).isNaN())
        val otherNaN = 0xFFFFFFFF00000001UL
        Assertions.assertTrue(EDouble(otherNaN).isNaN())
        Assertions.assertTrue(!EDouble(EDouble.infinity()).isNaN())
        Assertions.assertTrue(!EDouble(-EDouble.infinity()).isNaN())
        Assertions.assertTrue(!EDouble(0.0).isNaN())
        Assertions.assertTrue(!EDouble(-0.0).isNaN())
        Assertions.assertTrue(!EDouble(1.0).isNaN())
        Assertions.assertTrue(!EDouble(-1.0).isNaN())
        val minDouble64 = 0x0000000000000001UL
        Assertions.assertTrue(!EDouble(minDouble64).isNaN())
    }
    
    @Test
    fun `Single isNaN`() {
        Assertions.assertTrue(ESingle(ESingle.nan()).isNaN())
        val otherNaN = 0xFFFFF001U
        Assertions.assertTrue(ESingle(otherNaN).isNaN())
        Assertions.assertTrue(!ESingle(ESingle.infinity()).isNaN())
        Assertions.assertTrue(!ESingle(-ESingle.infinity()).isNaN())
        Assertions.assertTrue(!ESingle(0.0f).isNaN())
        Assertions.assertTrue(!ESingle(-0.0f).isNaN())
        Assertions.assertTrue(!ESingle(1.0f).isNaN())
        Assertions.assertTrue(!ESingle(-1.0f).isNaN())
        val minFloat32 = 0x00000001U
        Assertions.assertTrue(!ESingle(minFloat32).isNaN())
    }
    
    @Test
    fun `Double sign`() {
        Assertions.assertEquals(1, EDouble(1.0).sign())
        Assertions.assertEquals(1, EDouble(EDouble.infinity()).sign())
        Assertions.assertEquals(-1, EDouble(-EDouble.infinity()).sign())
        Assertions.assertEquals(1, EDouble(0.0).sign())
        Assertions.assertEquals(-1, EDouble(-0.0).sign())
        val minDouble64 = 0x0000000000000001UL
        Assertions.assertEquals(1, EDouble(minDouble64).sign())
    }

    @Test
    fun `Single sign`() {
        Assertions.assertEquals(1, ESingle(1.0f).sign())
        Assertions.assertEquals(1, ESingle(ESingle.infinity()).sign())
        Assertions.assertEquals(-1, ESingle(-ESingle.infinity()).sign())
        Assertions.assertEquals(1, ESingle(0.0f).sign())
        Assertions.assertEquals(-1, ESingle(-0.0f).sign())
        val minFloat32 = 0x00000001U
        Assertions.assertEquals(1, ESingle(minFloat32).sign())
    }

    @Test
    fun `Double normalizeBoundaries()`() {
        val boundaryPlus = Ref<DiyFp>()
        val boundaryMinus = Ref<DiyFp>()
        var diyFp = EDouble(1.5).asNormalizedDiyFp()
        EDouble(1.5).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertTrue(diyFp.significand - boundaryMinus.get().significand == boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals((1 shl 10).toULong(), diyFp.significand - boundaryMinus.get().significand)

        diyFp = EDouble(1.0).asNormalizedDiyFp()
        EDouble(1.0).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertTrue(boundaryPlus.get().significand - diyFp.significand > diyFp.significand - boundaryMinus.get().significand)
        Assertions.assertEquals((1 shl 9).toULong(), diyFp.significand - boundaryMinus.get().significand)
        Assertions.assertEquals((1 shl 10).toULong(), boundaryPlus.get().significand - diyFp.significand)

        val minDouble64 = 0x0000000000000001UL
        diyFp = EDouble(minDouble64).asNormalizedDiyFp()
        EDouble(minDouble64).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals(1UL shl 62, diyFp.significand - boundaryMinus.get().significand)

        val smallestNormal64 = 0x0010000000000000UL
        diyFp = EDouble(smallestNormal64).asNormalizedDiyFp()
        EDouble(smallestNormal64).normalizeBoundaries(boundaryMinus,
        boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals((1 shl 10).toULong(), diyFp.significand - boundaryMinus.get().significand)

        val largestDenormal64 = 0x000FFFFFFFFFFFFFUL
        diyFp = EDouble(largestDenormal64).asNormalizedDiyFp()
        EDouble(largestDenormal64).normalizeBoundaries(boundaryMinus,
        boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals((1 shl 11).toULong(), diyFp.significand - boundaryMinus.get().significand)

        val maxDouble64 = 0x7fefffffffffffffUL
        diyFp = EDouble(maxDouble64).asNormalizedDiyFp()
        EDouble(maxDouble64).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals((1 shl 10).toULong(), diyFp.significand - boundaryMinus.get().significand)
    }
    
    @Test
    fun `Single normalizedBoundaries`() {
        val kOne64 = 1UL
        val boundaryPlus = Ref<DiyFp>()
        val boundaryMinus = Ref<DiyFp>()
        var diyFp = ESingle (1.5f).asDiyFp()
        diyFp.normalize()
        ESingle(1.5f).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        // 1.5 does not have a significand of the form 2^p (for some p).
        // Therefore its boundaries are at the same distance.
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        // Normalization shifts the significand by 8 bits. Add 32 bits for the bigger
        // data-type, and remove 1 because boundaries are at half a ULP.
        Assertions.assertEquals(kOne64 shl 39, diyFp.significand - boundaryMinus.get().significand)

        diyFp = ESingle(1.0f).asDiyFp()
        diyFp.normalize()
        ESingle(1.0f).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertTrue(boundaryPlus.get().significand - diyFp.significand > diyFp.significand - boundaryMinus.get().significand)
        Assertions.assertEquals(kOne64 shl 38, diyFp.significand - boundaryMinus.get().significand)
        Assertions.assertEquals(kOne64 shl 39, boundaryPlus.get().significand - diyFp.significand)

        val minFloat32 = 0x00000001U
        diyFp = ESingle(minFloat32).asDiyFp()
        diyFp.normalize()
        ESingle(minFloat32).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertTrue(diyFp.significand - boundaryMinus.get().significand == boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertTrue((kOne64 shl 62) == diyFp.significand - boundaryMinus.get().significand)

        val smallestNormal32 = 0x00800000U
        diyFp = ESingle(smallestNormal32).asDiyFp()
        diyFp.normalize()
        ESingle(smallestNormal32).normalizeBoundaries(boundaryMinus,
        boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        // Even though the significand is of the form 2^p (for some p), its boundaries
        // are at the same distance. (This is the only exception).
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals(kOne64 shl 39, diyFp.significand - boundaryMinus.get().significand)

        val largestDenormal32 = 0x007FFFFFU
        diyFp = ESingle(largestDenormal32).asDiyFp()
        diyFp.normalize()
        ESingle(largestDenormal32).normalizeBoundaries(boundaryMinus,
        boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals(kOne64 shl 40, diyFp.significand - boundaryMinus.get().significand)

        val maxFloat32 = 0x7f7fffffU
        diyFp = ESingle(maxFloat32).asDiyFp()
        diyFp.normalize()
        ESingle(maxFloat32).normalizeBoundaries(boundaryMinus, boundaryPlus)
        Assertions.assertEquals(diyFp.exponent, boundaryMinus.get().exponent)
        Assertions.assertEquals(diyFp.exponent, boundaryPlus.get().exponent)
        Assertions.assertEquals(diyFp.significand - boundaryMinus.get().significand, boundaryPlus.get().significand - diyFp.significand)
        Assertions.assertEquals(kOne64 shl 39, diyFp.significand - boundaryMinus.get().significand)
    }
    
    @Test
    fun nextDouble() {
        Assertions.assertEquals(4e-324, EDouble(0.0).nextDouble())
        Assertions.assertEquals(0.0, EDouble(-0.0).nextDouble())
        Assertions.assertEquals(-0.0, EDouble(-4e-324).nextDouble())
        Assertions.assertTrue(EDouble(EDouble(-0.0).nextDouble()).sign() > 0)
        Assertions.assertTrue(EDouble(EDouble(-4e-324).nextDouble()).sign() < 0)
        val d0 = EDouble(-4e-324)
        val d1 = EDouble(d0.nextDouble())
        val d2 = EDouble(d1.nextDouble())
        Assertions.assertEquals(-0.0, d1.value())
        Assertions.assertTrue(d1.sign() < 0)
        Assertions.assertEquals(0.0, d2.value())
        Assertions.assertTrue(d2.sign() > 0)
        Assertions.assertEquals(4e-324, d2.nextDouble())
        Assertions.assertEquals(-1.7976931348623157e308, EDouble(-EDouble.infinity()).nextDouble())
        Assertions.assertEquals(EDouble.infinity(), EDouble(0x7fefffffffffffffUL).nextDouble())
    }

    @Test
    fun previousDouble() {
        Assertions.assertEquals(0.0, EDouble(4e-324).previousDouble())
        Assertions.assertEquals(-0.0, EDouble(0.0).previousDouble())
        Assertions.assertTrue(EDouble(EDouble(0.0).previousDouble()).sign() < 0)
        Assertions.assertEquals(-4e-324, EDouble(-0.0).previousDouble())
        val d0 = EDouble(4e-324)
        val d1 = EDouble(d0.previousDouble())
        val d2 = EDouble(d1.previousDouble())
        Assertions.assertEquals(0.0, d1.value())
        Assertions.assertTrue(d1.sign() > 0)
        Assertions.assertEquals(-0.0, d2.value())
        Assertions.assertTrue(d2.sign() < 0)
        Assertions.assertEquals(-4e-324, d2.previousDouble())
        Assertions.assertEquals(1.7976931348623157e308, EDouble(EDouble.infinity()).previousDouble())
        Assertions.assertEquals(-EDouble.infinity(), EDouble(0xffefffffffffffffUL).previousDouble())
    }

    @Test
    fun signalingNaN() {
        val nan = EDouble(EDouble.nan())
        Assertions.assertTrue(nan.isNaN())
        Assertions.assertTrue(nan.isQuietNaN())
        // TODO: How do I get these values on the JVM?
        // Assertions.assertTrue(EDouble(std::numeric_limits<double>::quiet_NaN()).isQuietNan())
        // Assertions.assertTrue(EDouble(std::numeric_limits<double>::signaling_NaN()).IsSignalingNan())
    }

    @Test
    fun `SignalingNanSingle`() {
        val nan = ESingle(ESingle.nan())
        Assertions.assertTrue(nan.isNaN())
        Assertions.assertTrue(nan.isQuietNaN())
        // TODO: How do I get these values on the JVM?
        // Assertions.assertTrue(ESingle(std::numeric_limits<float>::quiet_NaN()).isQuietNaN())
        // Assertions.assertTrue(ESingle(std::numeric_limits<float>::signaling_NaN()).IsSignalingNan())
    }
}