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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import com.reevajs.reeva.mfbt.impl.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isTrue

class FastDtoaTest {
    @Test
    fun `fast dtoa shortest various doubles`() {
        val minDouble = 5e-324
        val maxDouble = 1.7976931348623157e308

        val buffer = StringBuilder()
        val point = Ref<Int>()
        var status: Boolean

        status = fastDtoa(minDouble, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("5").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-323)

        buffer.clear()
        status = fastDtoa(maxDouble, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("17976931348623157").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(309)

        buffer.clear()
        status = fastDtoa(4294967272.0, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("4294967272").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(10)

        buffer.clear()
        status = fastDtoa(4.1855804968213567e298, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("4185580496821357").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(299)

        buffer.clear()
        status = fastDtoa(5.5626846462680035e-309, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("5562684646268003").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-308)

        // buffer.clear()
        // status = fastDtoa(3.5844466002796428e+298, FastDtoaMode.Shortest, 0, buffer, point)
        // expectThat(status).isTrue()
        // expectThat("35844466002796428").isEqualTo(buffer.toString())
        // expectThat(point.get()).isEqualTo(299)

        buffer.clear()
        val smallestNormal64 = 0x0010000000000000UL
        var v = EDouble(smallestNormal64).value()
        status = fastDtoa(v, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("22250738585072014").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-307)

        buffer.clear()
        val largestDenormal64 = 0x000FFFFFFFFFFFFFUL
        v = EDouble(largestDenormal64).value()
        status = fastDtoa(v, FastDtoaMode.Shortest, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("2225073858507201").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-307)
    }

    @Test
    fun `fast dtoa shortest various floats`() {
        val minFloat = 1e-45f
        val maxFloat = 3.4028234e38f

        val buffer = StringBuilder()
        val point = Ref<Int>()
        var status: Boolean

        status = fastDtoa(minFloat.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("1").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-44)

        buffer.clear()
        status = fastDtoa(maxFloat.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("34028235").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(39)

        buffer.clear()
        status = fastDtoa(4294967272.0f.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("42949673").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(10)

        buffer.clear()
        status = fastDtoa(3.32306998946228968226e+35f.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("332307").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(36)

        buffer.clear()
        status = fastDtoa(1.2341e-41f.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("12341").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-40)

        buffer.clear()
        status = fastDtoa(3.3554432e7f.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("33554432").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(8)

        buffer.clear()
        status = fastDtoa(3.26494756798464e14f.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("32649476").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(15)

        buffer.clear()
        status = fastDtoa(3.91132223637771935344e37f.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("39113222").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(38)

        buffer.clear()
        val smallestNormal32 = 0x00800000U
        var v = ESingle(smallestNormal32).value()
        status = fastDtoa(v.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("11754944").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-37)

        buffer.clear()
        val largestDenormal32 = 0x007FFFFFU
        v = ESingle(largestDenormal32).value()
        // TODO: Make a fastDtoaSingle that accepts floats
        status = fastDtoa(v.toDouble(), FastDtoaMode.ShortestSingle, 0, buffer, point)
        expectThat(status).isTrue()
        expectThat("11754942").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-37)
    }

    @Test
    fun `fast dtoa precision various doubles`() {
        val buffer = StringBuilder()
        val point = Ref<Int>()
        var status: Boolean

        status = fastDtoa(1.0, FastDtoaMode.Precision, 3, buffer, point)
        expectThat(status).isTrue()
        trimRepresentation(buffer)
        expectThat("1").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(1)

        buffer.clear()
        status = fastDtoa(1.5, FastDtoaMode.Precision, 10, buffer, point)
        if (status) {
            trimRepresentation(buffer)
            expectThat("15").isEqualTo(buffer.toString())
            expectThat(point.get()).isEqualTo(1)
        }

        val minDouble = 5e-324
        val maxDouble = 1.7976931348623157e308

        buffer.clear()
        status = fastDtoa(minDouble, FastDtoaMode.Precision, 5, buffer, point)
        expectThat(status).isTrue()
        expectThat("49407").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-323)

        buffer.clear()
        status = fastDtoa(maxDouble, FastDtoaMode.Precision, 7, buffer, point)
        expectThat(status).isTrue()
        expectThat("1797693").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(309)

        buffer.clear()
        status = fastDtoa(4294967272.0, FastDtoaMode.Precision, 14, buffer, point)
        if (status) {
            trimRepresentation(buffer)
            expectThat("4294967272").isEqualTo(buffer.toString())
            expectThat(point.get()).isEqualTo(10)
        }

        buffer.clear()
        status = fastDtoa(4.1855804968213567e298, FastDtoaMode.Precision, 17, buffer, point)
        expectThat(status).isTrue()
        expectThat("41855804968213567").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(299)

        buffer.clear()
        status = fastDtoa(5.5626846462680035e-309, FastDtoaMode.Precision, 1, buffer, point)
        expectThat(status).isTrue()
        expectThat("6").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-308)

        buffer.clear()
        status = fastDtoa(2147483648.0, FastDtoaMode.Precision, 5, buffer, point)
        expectThat(status).isTrue()
        expectThat("21475").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(10)

        buffer.clear()
        status = fastDtoa(3.5844466002796428e+298, FastDtoaMode.Precision, 10, buffer, point)
        expectThat(status).isTrue()
        trimRepresentation(buffer)
        expectThat("35844466").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(299)

        buffer.clear()
        val smallestNormal64 = 0x0010000000000000UL
        status = fastDtoa(EDouble(smallestNormal64).value(), FastDtoaMode.Precision, 17, buffer, point)
        expectThat(status).isTrue()
        expectThat("22250738585072014").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-307)

        buffer.clear()
        val largestDenormal64 = 0x000FFFFFFFFFFFFFUL
        status = fastDtoa(EDouble(largestDenormal64).value(), FastDtoaMode.Precision, 17, buffer, point)
        expectThat(status).isTrue()
        trimRepresentation(buffer)
        expectThat("22250738585072009").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-307)

        buffer.clear()
        status = fastDtoa(3.3161339052167390562200598e-237, FastDtoaMode.Precision, 18, buffer, point)
        expectThat(status).isTrue()
        expectThat("331613390521673906").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(-236)

        buffer.clear()
        status = fastDtoa(7.9885183916008099497815232e+191, FastDtoaMode.Precision, 4, buffer, point)
        expectThat(status).isTrue()
        expectThat("7989").isEqualTo(buffer.toString())
        expectThat(point.get()).isEqualTo(192)
    }

    @Serializable
    data class PrecomputedDouble(
        @SerialName("v")
        val value: Double,
        @SerialName("r")
        val representation: String,
        @SerialName("dp")
        val decimalPoint: Int,
    )

    @Test
    fun `fast dtoa gay shortest`() {
        val content = this::class.java.getResource("/shortest_double.json")!!.readText()
        val representations: List<PrecomputedDouble> = Json.Default.decodeFromString(content)

        var total = 0
        var succeeded = 0
        var neededMaxLength = false

        for ((v, rep, decimalPoint) in representations) {
            total++
            val builder = StringBuilder()
            val point = Ref<Int>()
            val status = fastDtoa(v, FastDtoaMode.Shortest, 0, builder, point)
            expectThat(FAST_DTOA_MAXIMAL_LENGTH).isGreaterThanOrEqualTo(builder.length)
            if (!status)
                continue
            if (builder.length == FAST_DTOA_MAXIMAL_LENGTH)
                neededMaxLength = true
            succeeded++
            expectThat(point.get()).isEqualTo(decimalPoint)
            expectThat(builder.toString()).isEqualTo(rep)
        }

        expectThat(succeeded * 1.0 / total).isGreaterThan(0.99)
        expectThat(neededMaxLength).isTrue()
    }

    @Serializable
    data class PrecomputedSingle(
        @SerialName("v")
        val value: Float,
        @SerialName("r")
        val representation: String,
        @SerialName("dp")
        val decimalPoint: Int,
    )

    @Test
    fun `fast dtoa gay shortest single`() {
        val content = this::class.java.getResource("/shortest_single.json")!!.readText()
        val representations: List<PrecomputedSingle> = Json.Default.decodeFromString(content)

        var total = 0
        var succeeded = 0
        var neededMaxLength = false

        for ((v, rep, decimalPoint) in representations) {
            total++
            val builder = StringBuilder()
            val point = Ref<Int>()
            val status = fastDtoa(v.toDouble(), FastDtoaMode.ShortestSingle, 0, builder, point)
            expectThat(FAST_DTOA_MAXIMAL_SINGLE_LENGTH).isGreaterThanOrEqualTo(builder.length)
            if (!status)
                continue
            if (builder.length == FAST_DTOA_MAXIMAL_SINGLE_LENGTH)
                neededMaxLength = true
            succeeded++
            expectThat(point.get()).isEqualTo(decimalPoint)
            expectThat(builder.toString()).isEqualTo(rep)
        }

        expectThat(succeeded * 1.0 / total).isGreaterThan(0.98)
        expectThat(neededMaxLength).isTrue()
    }

    @Serializable
    data class PrecomputedPrecision(
        @SerialName("v")
        val value: Double,
        @SerialName("nd")
        val numDigits: Int,
        @SerialName("r")
        val representation: String,
        @SerialName("dp")
        val decimalPoint: Int,
    )

    @Test
    fun `fast dtoa gay precision`() {
        val content = this::class.java.getResource("/precision.json")!!.readText()
        val representations: List<PrecomputedPrecision> = Json { allowSpecialFloatingPointValues = true }.decodeFromString(content)

        var total = 0
        var succeeded = 0
        var total15 = 0
        var succeeded15 = 0

        for ((value, numDigits, representation, decimalPoint) in representations) {
            total++
            if (numDigits <= 15)
                total15++
            val builder = StringBuilder()
            val point = Ref<Int>()
            val status = fastDtoa(value, FastDtoaMode.Precision, numDigits, builder, point)
            expectThat(numDigits).isGreaterThanOrEqualTo(builder.length)
            if (!status) {
                if (numDigits <= 15)
                    println()
                continue
            }
            succeeded++
            if (numDigits <= 15)
                succeeded15++
            trimRepresentation(builder)
            expectThat(point.get()).isEqualTo(decimalPoint)
            expectThat(builder.toString()).isEqualTo(representation)
        }

        expectThat(succeeded * 1.0 / total).isGreaterThan(0.85)
        expectThat(succeeded15 * 1.0 / total15).isGreaterThan(0.9999)
    }

    private fun trimRepresentation(representation: StringBuilder) {
        while (representation.lastOrNull() == '0')
            representation.deleteCharAt(representation.lastIndex)
    }
}