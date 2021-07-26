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

package com.reevajs.reeva.mfbt

import com.reevajs.reeva.mfbt.impl.DiyFp
import com.reevajs.reeva.mfbt.impl.EDouble
import com.reevajs.reeva.mfbt.impl.ESingle
import com.reevajs.reeva.mfbt.impl.Ref
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isLessThan
import strikt.assertions.isTrue

class IEEETest {
    @Test
    fun `UInt64 conversions`() {
        val ordered = 0x0123456789ABCDEFUL
        expectThat(3512700564088504e-318).isEqualTo(EDouble(ordered).value())

        val minDouble64 = 0x0000000000000001UL
        expectThat(5e-324).isEqualTo(EDouble(minDouble64).value())

        val maxDouble64 = 0x7fefffffffffffffUL
        expectThat(1.7976931348623157e308).isEqualTo(EDouble(maxDouble64).value())
    }

    @Test
    fun `Uint32 conversions`() {
        val ordered = 0x01234567U
        expectThat(2.9988165487136453e-38f).isEqualTo(ESingle(ordered).value())

        val minFloat32 = 0x00000001U
        expectThat(1.4e-45f).isEqualTo(ESingle(minFloat32).value())

        val maxFloat32 = 0x7f7fffffU
        expectThat(3.4028234e38f).isEqualTo(ESingle(maxFloat32).value())
    }

    @Test
    fun `EDouble asDiyFp`() {
        val ordered = 0x0123456789ABCDEFUL
        var diyFp = EDouble(ordered).asDiyFp()
        expectThat(0x12 - 0x3FF - 52).isEqualTo(diyFp.exponent)
        expectThat(0x0013456789ABCDEFUL).isEqualTo(diyFp.significand)

        val minDouble64 = 0x0000000000000001UL
        diyFp = EDouble(minDouble64).asDiyFp()
        expectThat(-0x3FF - 52 + 1).isEqualTo(diyFp.exponent)
        expectThat(1UL).isEqualTo(diyFp.significand)

        val maxDouble64 = 0x7fefffffffffffffUL
        diyFp = EDouble(maxDouble64).asDiyFp()
        expectThat(0x7FE - 0x3FF - 52).isEqualTo(diyFp.exponent)
        expectThat(0x001fffffffffffffUL).isEqualTo(diyFp.significand)
    }

    @Test
    fun `ESingle asDiyFp`() {
        val ordered = 0x01234567U
        var diyFp = ESingle(ordered).asDiyFp()
        expectThat(0x2 - 0x7F - 23).isEqualTo(diyFp.exponent)
        expectThat(0xA34567UL).isEqualTo(diyFp.significand)

        val minFloat32 = 0x00000001U
        diyFp = ESingle(minFloat32).asDiyFp()
        expectThat(-0x7F - 23 + 1).isEqualTo(diyFp.exponent)
        expectThat(1UL).isEqualTo(diyFp.significand)

        val maxFloat32 = 0x7f7fffffU
        diyFp = ESingle(maxFloat32).asDiyFp()
        expectThat(0xFE - 0x7F - 23).isEqualTo(diyFp.exponent)
        expectThat(0x00ffffffUL).isEqualTo(diyFp.significand)
    }

    @Test
    fun `asNormalizedDiyFp`() {
        val ordered = 0x0123456789ABCDEFUL
        var diyFp = EDouble(ordered).asNormalizedDiyFp()
        expectThat(0x12 - 0x3FF - 52 - 11).isEqualTo(diyFp.exponent)
        expectThat(0x0013456789ABCDEFUL shl 11).isEqualTo(diyFp.significand)

        val minDouble64 = 0x0000000000000001UL
        diyFp = EDouble(minDouble64).asNormalizedDiyFp()
        expectThat(-0x3FF - 52 + 1 - 63).isEqualTo(diyFp.exponent)
        expectThat(0x8000000000000000UL).isEqualTo(diyFp.significand)

        val maxDouble64 = 0x7fefffffffffffffUL
        diyFp = EDouble(maxDouble64).asNormalizedDiyFp()
        expectThat(0x7FE - 0x3FF - 52 - 11).isEqualTo(diyFp.exponent)
        expectThat(0x001fffffffffffffUL shl 11).isEqualTo(diyFp.significand)
    }

    @Test
    fun `EDouble isDenormal`() {
        val minDouble64 = 0x0000000000000001UL
        expect {
            that(EDouble(minDouble64).isDenormal()).isTrue()
            that(EDouble(0x000FFFFFFFFFFFFFUL).isDenormal()).isTrue()
            that(!EDouble(0x0010000000000000UL).isDenormal()).isTrue()
        }
    }

    @Test
    fun `ESingle isDenormal`() {
        val minFloat32 = 0x00000001U
        expect {
            that(ESingle(minFloat32).isDenormal()).isTrue()
            that(ESingle(0x007FFFFFU).isDenormal()).isTrue()
            that(!ESingle(0x00800000U).isDenormal()).isTrue()
        }
    }

    @Test
    fun `Double isSpecial`() {
        expect {
            that(EDouble(EDouble.infinity()).isSpecial()).isTrue()
            that(EDouble(-EDouble.infinity()).isSpecial()).isTrue()
            that(EDouble(EDouble.nan()).isSpecial()).isTrue()
            that(EDouble(0xFFF1234500000000UL).isSpecial()).isTrue()
            that(!EDouble(5e-324).isSpecial()).isTrue()
            that(!EDouble(-5e-324).isSpecial()).isTrue()
            that(!EDouble(0.0).isSpecial()).isTrue()
            that(!EDouble(-0.0).isSpecial()).isTrue()
            that(!EDouble(1.0).isSpecial()).isTrue()
            that(!EDouble(-1.0).isSpecial()).isTrue()
            that(!EDouble(1000000.0).isSpecial()).isTrue()
            that(!EDouble(-1000000.0).isSpecial()).isTrue()
            that(!EDouble(1e23).isSpecial()).isTrue()
            that(!EDouble(-1e23).isSpecial()).isTrue()
            that(!EDouble(1.7976931348623157e308).isSpecial()).isTrue()
            that(!EDouble(-1.7976931348623157e308).isSpecial()).isTrue()
        }
    }

    @Test
    fun `Single isSpecial`() {
        expect {
            that(ESingle(ESingle.infinity()).isSpecial()).isTrue()
            that(ESingle(-ESingle.infinity()).isSpecial()).isTrue()
            that(ESingle(ESingle.nan()).isSpecial()).isTrue()
            that(ESingle(0xFFF12345U).isSpecial()).isTrue()
            that(!ESingle(1.4e-45f).isSpecial()).isTrue()
            that(!ESingle(-1.4e-45f).isSpecial()).isTrue()
            that(!ESingle(0.0f).isSpecial()).isTrue()
            that(!ESingle(-0.0f).isSpecial()).isTrue()
            that(!ESingle(1.0f).isSpecial()).isTrue()
            that(!ESingle(-1.0f).isSpecial()).isTrue()
            that(!ESingle(1000000.0f).isSpecial()).isTrue()
            that(!ESingle(-1000000.0f).isSpecial()).isTrue()
            that(!ESingle(1e23f).isSpecial()).isTrue()
            that(!ESingle(-1e23f).isSpecial()).isTrue()
            that(!ESingle(1.18e-38f).isSpecial()).isTrue()
            that(!ESingle(-1.18e-38f).isSpecial()).isTrue()
        }
    }

    @Test
    fun `Double isInfinite`() {
        expect {
            that(EDouble(EDouble.infinity()).isInfinite()).isTrue()
            that(EDouble(-EDouble.infinity()).isInfinite()).isTrue()
            that(!EDouble(EDouble.nan()).isInfinite()).isTrue()
            that(!EDouble(0.0).isInfinite()).isTrue()
            that(!EDouble(-0.0).isInfinite()).isTrue()
            that(!EDouble(1.0).isInfinite()).isTrue()
            that(!EDouble(-1.0).isInfinite()).isTrue()
            val minDouble64 = 0x0000000000000001UL
            that(!EDouble(minDouble64).isInfinite()).isTrue()
        }
    }

    @Test
    fun `Single isInfinite`() {
        expect {
            that(ESingle(ESingle.infinity()).isInfinite()).isTrue()
            that(ESingle(-ESingle.infinity()).isInfinite()).isTrue()
            that(!ESingle(ESingle.nan()).isInfinite()).isTrue()
            that(!ESingle(0.0f).isInfinite()).isTrue()
            that(!ESingle(-0.0f).isInfinite()).isTrue()
            that(!ESingle(1.0f).isInfinite()).isTrue()
            that(!ESingle(-1.0f).isInfinite()).isTrue()
            val minFloat32 = 0x00000001U
            that(!ESingle(minFloat32).isInfinite()).isTrue()
        }
    }

    @Test
    fun `Double isNaN`() {
        expect {
            that(EDouble(EDouble.nan()).isNaN()).isTrue()
            val otherNaN = 0xFFFFFFFF00000001UL
            that(EDouble(otherNaN).isNaN()).isTrue()
            that(!EDouble(EDouble.infinity()).isNaN()).isTrue()
            that(!EDouble(-EDouble.infinity()).isNaN()).isTrue()
            that(!EDouble(0.0).isNaN()).isTrue()
            that(!EDouble(-0.0).isNaN()).isTrue()
            that(!EDouble(1.0).isNaN()).isTrue()
            that(!EDouble(-1.0).isNaN()).isTrue()
            val minDouble64 = 0x0000000000000001UL
            that(!EDouble(minDouble64).isNaN()).isTrue()
        }
    }

    @Test
    fun `Single isNaN`() {
        expect {
            that(ESingle(ESingle.nan()).isNaN()).isTrue()
            val otherNaN = 0xFFFFF001U
            that(ESingle(otherNaN).isNaN()).isTrue()
            that(!ESingle(ESingle.infinity()).isNaN()).isTrue()
            that(!ESingle(-ESingle.infinity()).isNaN()).isTrue()
            that(!ESingle(0.0f).isNaN()).isTrue()
            that(!ESingle(-0.0f).isNaN()).isTrue()
            that(!ESingle(1.0f).isNaN()).isTrue()
            that(!ESingle(-1.0f).isNaN()).isTrue()
            val minFloat32 = 0x00000001U
            that(!ESingle(minFloat32).isNaN()).isTrue()
        }
    }

    @Test
    fun `Double sign`() {
        expect {
            that(1).isEqualTo(EDouble(1.0).sign())
            that(1).isEqualTo(EDouble(EDouble.infinity()).sign())
            that(-1).isEqualTo(EDouble(-EDouble.infinity()).sign())
            that(1).isEqualTo(EDouble(0.0).sign())
            that(-1).isEqualTo(EDouble(-0.0).sign())
            val minDouble64 = 0x0000000000000001UL
            that(1).isEqualTo(EDouble(minDouble64).sign())
        }
    }

    @Test
    fun `Single sign`() {
        expect {
            that(1).isEqualTo(ESingle(1.0f).sign())
            that(1).isEqualTo(ESingle(ESingle.infinity()).sign())
            that(-1).isEqualTo(ESingle(-ESingle.infinity()).sign())
            that(1).isEqualTo(ESingle(0.0f).sign())
            that(-1).isEqualTo(ESingle(-0.0f).sign())
            val minFloat32 = 0x00000001U
            that(1).isEqualTo(ESingle(minFloat32).sign())
        }
    }

    @Test
    fun `Double normalizeBoundaries()`() {
        val boundaryPlus = Ref<DiyFp>()
        val boundaryMinus = Ref<DiyFp>()
        var diyFp = EDouble(1.5).asNormalizedDiyFp()
        EDouble(1.5).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that((1 shl 10).toULong()).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        diyFp = EDouble(1.0).asNormalizedDiyFp()
        EDouble(1.0).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(boundaryPlus.get().significand - diyFp.significand).isGreaterThan(diyFp.significand - boundaryMinus.get().significand)
            that((1 shl 9).toULong()).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
            that((1 shl 10).toULong()).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
        }

        val minDouble64 = 0x0000000000000001UL
        diyFp = EDouble(minDouble64).asNormalizedDiyFp()
        EDouble(minDouble64).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that(1UL shl 62).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        val smallestNormal64 = 0x0010000000000000UL
        diyFp = EDouble(smallestNormal64).asNormalizedDiyFp()
        EDouble(smallestNormal64).normalizeBoundaries(
            boundaryMinus,
            boundaryPlus
        )
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that((1 shl 10).toULong()).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        val largestDenormal64 = 0x000FFFFFFFFFFFFFUL
        diyFp = EDouble(largestDenormal64).asNormalizedDiyFp()
        EDouble(largestDenormal64).normalizeBoundaries(
            boundaryMinus,
            boundaryPlus
        )
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that((1 shl 11).toULong()).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        val maxDouble64 = 0x7fefffffffffffffUL
        diyFp = EDouble(maxDouble64).asNormalizedDiyFp()
        EDouble(maxDouble64).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that((1 shl 10).toULong()).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }
    }

    @Test
    fun `Single normalizedBoundaries`() {
        val kOne64 = 1UL
        val boundaryPlus = Ref<DiyFp>()
        val boundaryMinus = Ref<DiyFp>()

        var diyFp = ESingle(1.5f).asDiyFp()
        diyFp.normalize()
        ESingle(1.5f).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that(kOne64 shl 39).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        diyFp = ESingle(1.0f).asDiyFp()
        diyFp.normalize()
        ESingle(1.0f).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(boundaryPlus.get().significand - diyFp.significand).isGreaterThan(diyFp.significand - boundaryMinus.get().significand)
            that(kOne64 shl 38).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
            that(kOne64 shl 39).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
        }

        val minFloat32 = 0x00000001U
        diyFp = ESingle(minFloat32).asDiyFp()
        diyFp.normalize()
        ESingle(minFloat32).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that(kOne64 shl 62).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        val smallestNormal32 = 0x00800000U
        diyFp = ESingle(smallestNormal32).asDiyFp()
        diyFp.normalize()
        ESingle(smallestNormal32).normalizeBoundaries(
            boundaryMinus,
            boundaryPlus
        )
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that(kOne64 shl 39).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        val largestDenormal32 = 0x007FFFFFU
        diyFp = ESingle(largestDenormal32).asDiyFp()
        diyFp.normalize()
        ESingle(largestDenormal32).normalizeBoundaries(
            boundaryMinus,
            boundaryPlus
        )
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that(kOne64 shl 40).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }

        val maxFloat32 = 0x7f7fffffU
        diyFp = ESingle(maxFloat32).asDiyFp()
        diyFp.normalize()
        ESingle(maxFloat32).normalizeBoundaries(boundaryMinus, boundaryPlus)
        expect {
            that(diyFp.exponent).isEqualTo(boundaryMinus.get().exponent)
            that(diyFp.exponent).isEqualTo(boundaryPlus.get().exponent)
            that(diyFp.significand - boundaryMinus.get().significand).isEqualTo(boundaryPlus.get().significand - diyFp.significand)
            that(kOne64 shl 39).isEqualTo(diyFp.significand - boundaryMinus.get().significand)
        }
    }

    @Test
    fun nextDouble() {
        expect {
            that(4e-324).isEqualTo(EDouble(0.0).nextDouble())
            that(0.0).isEqualTo(EDouble(-0.0).nextDouble())
            that(-0.0).isEqualTo(EDouble(-4e-324).nextDouble())
            that(EDouble(EDouble(-0.0).nextDouble()).sign()).isGreaterThan(0)
            that(EDouble(EDouble(-4e-324).nextDouble()).sign()).isLessThan(0)
        }
        val d0 = EDouble(-4e-324)
        val d1 = EDouble(d0.nextDouble())
        val d2 = EDouble(d1.nextDouble())
        expect {
            that(-0.0).isEqualTo(d1.value())
            that(d1.sign()).isLessThan(0)
            that(0.0).isEqualTo(d2.value())
            that(d2.sign()).isGreaterThan(0)
            that(4e-324).isEqualTo(d2.nextDouble())
            that(-1.7976931348623157e308).isEqualTo(EDouble(-EDouble.infinity()).nextDouble())
            that(EDouble.infinity()).isEqualTo(EDouble(0x7fefffffffffffffUL).nextDouble())
        }
    }

    @Test
    fun previousDouble() {
        expect {
            that(0.0).isEqualTo(EDouble(4e-324).previousDouble())
            that(-0.0).isEqualTo(EDouble(0.0).previousDouble())
            that(EDouble(EDouble(0.0).previousDouble()).sign()).isLessThan(0)
            that(-4e-324).isEqualTo(EDouble(-0.0).previousDouble())
        }
        val d0 = EDouble(4e-324)
        val d1 = EDouble(d0.previousDouble())
        val d2 = EDouble(d1.previousDouble())
        expect {
            that(0.0).isEqualTo(d1.value())
            that(d1.sign()).isGreaterThan(0)
            that(-0.0).isEqualTo(d2.value())
            that(d2.sign()).isLessThan(0)
            that(-4e-324).isEqualTo(d2.previousDouble())
            that(1.7976931348623157e308).isEqualTo(EDouble(EDouble.infinity()).previousDouble())
            that(-EDouble.infinity()).isEqualTo(EDouble(0xffefffffffffffffUL).previousDouble())
        }
    }

    @Test
    fun signalingNaN() {
        val nan = EDouble(EDouble.nan())
        expect {
            that(nan.isNaN()).isTrue()
            that(nan.isQuietNaN()).isTrue()
            // TODO: How do I get these values on the JVM?
            // that(EDouble(std::numeric_limits<double>::quiet_NaN()).isQuietNan()).isTrue()
            // that(EDouble(std::numeric_limits<double>::signaling_NaN()).IsSignalingNan()).isTrue()
        }
    }

    @Test
    fun `SignalingNanSingle`() {
        val nan = ESingle(ESingle.nan())
        expect {
            that(nan.isNaN()).isTrue()
            that(nan.isQuietNaN()).isTrue()
            // TODO: How do I get these values on the JVM?
            // that(ESingle(std::numeric_limits<float>::quiet_NaN()).isQuietNaN()).isTrue()
            // that(ESingle(std::numeric_limits<float>::signaling_NaN()).IsSignalingNan()).isTrue()
        }
    }
}
