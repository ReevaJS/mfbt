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

package me.mattco.reeva.mfbt

import me.mattco.reeva.mfbt.impl.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DtoaTest {
    @Test
    fun `dtoa various doubles`() {
        val buffer = StringBuilder()
        val point = Ref<Int>()
        val sign = Ref<Boolean>()

        fun doTest(
            v: Double,
            mode: Dtoa.Mode,
            requestedDigits: Int,
            expectedString: String,
            expectedPoint: Int,
            trim: Boolean = false
        ) {
            buffer.clear()
            Dtoa.doubleToAscii(v, mode, requestedDigits, buffer, sign, point)
            if (trim)
                trimRepresentation(buffer)
            Assertions.assertEquals(sign.get(), v < 0.0)
            Assertions.assertEquals(expectedString, buffer.toString())
            Assertions.assertEquals(expectedPoint, point.get())
        }

        val minDouble = 5e-324
        val minFloat = 1e-45f.toDouble()
        val maxDouble = 1.7976931348623157e308
        val maxFloat = 3.4028234e38f.toDouble()
        val smallestNormal64 = EDouble(0x0010000000000000UL).value()
        val smallestNormal32 = ESingle(0x00800000U).value()
        val largestDenormal64 = EDouble(0x000FFFFFFFFFFFFFUL).value()
        val largestDenormal32 = ESingle(0x007FFFFFU).value()

        doTest(0.0, Dtoa.Mode.Shortest, 0, "0", 1)
        doTest(0.0, Dtoa.Mode.ShortestSingle, 0, "0", 1)
        doTest(0.0, Dtoa.Mode.Fixed, 2, "0", 1)
        doTest(0.0, Dtoa.Mode.Precision, 3, "0", 1)
        doTest(1.0, Dtoa.Mode.Shortest, 0, "1", 1)
        doTest(1.0, Dtoa.Mode.ShortestSingle, 0, "1", 1)
        doTest(1.0, Dtoa.Mode.Fixed, 3, "1", 1, trim = true)
        doTest(1.0, Dtoa.Mode.Precision, 3, "1", 1, trim = true)
        doTest(1.5, Dtoa.Mode.Shortest, 0, "15", 1)
        doTest(1.5, Dtoa.Mode.ShortestSingle, 0, "15", 1)
        doTest(1.5, Dtoa.Mode.Fixed, 10, "15", 1, trim = true)
        doTest(1.5, Dtoa.Mode.Precision, 10, "15", 1, trim = true)
        doTest(minDouble, Dtoa.Mode.Shortest, 0, "5", -323)
        doTest(minFloat, Dtoa.Mode.ShortestSingle, 0, "1", -44)
        doTest(minDouble, Dtoa.Mode.Fixed, 5, "", -5)
        doTest(minDouble, Dtoa.Mode.Precision, 5, "49407", -323)
        doTest(maxDouble, Dtoa.Mode.Shortest, 0, "17976931348623157", 309)
        doTest(maxFloat, Dtoa.Mode.ShortestSingle, 0, "34028235", 39)
        doTest(maxDouble, Dtoa.Mode.Precision, 7, "1797693", 309, trim = true)
        doTest(4294967272.0, Dtoa.Mode.Shortest, 0, "4294967272", 10)
        doTest(4294967272.0f.toDouble(), Dtoa.Mode.ShortestSingle, 0, "42949673", 10)
        doTest(4294967272.0, Dtoa.Mode.Fixed, 5, "4294967272", 10, trim = true)
        doTest(4294967272.0, Dtoa.Mode.Precision, 14, "4294967272", 10, trim = true)
        doTest(4.1855804968213567e298, Dtoa.Mode.Shortest, 0, "4185580496821357", 299)
        doTest(4.1855804968213567e298, Dtoa.Mode.Precision, 20, "41855804968213567225", 299)
        doTest(5.5626846462680035e-309, Dtoa.Mode.Shortest, 0, "5562684646268003", -308)
        doTest(5.5626846462680035e-309, Dtoa.Mode.Precision, 1, "6", -308, trim = true)
        doTest(-2147483648.0, Dtoa.Mode.Shortest, 0, "2147483648", 10)
        doTest(-2147483648.0f.toDouble(), Dtoa.Mode.ShortestSingle, 0, "21474836", 10)
        doTest(-2147483648.0, Dtoa.Mode.Fixed, 2, "2147483648", 10, trim = true)
        doTest(-2147483648.0, Dtoa.Mode.Precision, 5, "21475", 10)
        doTest(-3.5844466002796428e+298, Dtoa.Mode.Shortest, 0, "35844466002796428", 299)
        doTest(-3.5844466002796428e+298, Dtoa.Mode.Precision, 10, "35844466", 299, trim = true)
        doTest(smallestNormal64, Dtoa.Mode.Shortest, 0, "22250738585072014", -307)
        doTest(smallestNormal32.toDouble(), Dtoa.Mode.ShortestSingle, 0, "11754944", -37)
        doTest(smallestNormal64, Dtoa.Mode.Precision, 20, "22250738585072013831", -307)
        doTest(largestDenormal64, Dtoa.Mode.Shortest, 0, "2225073858507201", -307)
        doTest(largestDenormal32.toDouble(), Dtoa.Mode.ShortestSingle, 0, "11754942", -37)
        doTest(largestDenormal64, Dtoa.Mode.Precision, 20, "2225073858507200889", -307, trim = true)
        doTest(4128420500802942e-24, Dtoa.Mode.Shortest, 0, "4128420500802942", -8)
        doTest(-3.9292015898194142585311918e-10, Dtoa.Mode.Shortest, 0, "39292015898194143", -9)
        doTest((-3.9292015898194142585311918e-10f).toDouble(), Dtoa.Mode.ShortestSingle, 0, "39292017", -9)
        doTest(4194304.0, Dtoa.Mode.Fixed, 5, "4194304", 7)
        doTest(3.3161339052167390562200598e-237, Dtoa.Mode.Precision, 19, "3316133905216739056", -236)
    }

    private fun trimRepresentation(representation: StringBuilder) {
        while (representation.lastOrNull() == '0')
            representation.deleteCharAt(representation.lastIndex)
    }
}