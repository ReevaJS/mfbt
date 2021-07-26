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
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DiyFpTest {
    @Test
    fun subtract() {
        var diyFp1 = DiyFp(3UL, 0)
        val diyFp2 = DiyFp(1UL, 0)
        val diff = diyFp1 - diyFp2

        expect {
            that(diff.significand).isEqualTo(2UL)
            that(diff.exponent).isEqualTo(0)
        }
        diyFp1 -= diyFp2
        expect {
            that(diyFp1.significand).isEqualTo(2UL)
            that(diyFp1.exponent).isEqualTo(0)
        }
    }

    @Test
    fun multiply() {
        var diyFp1 = DiyFp(3UL, 0)
        var diyFp2 = DiyFp(2UL, 0)
        var product = diyFp1 * diyFp2

        expectThat(product.significand).isEqualTo(0UL)
        expectThat(product.exponent).isEqualTo(64)
        diyFp1 *= diyFp2
        expectThat(diyFp1.significand).isEqualTo(0UL)
        expectThat(diyFp1.exponent).isEqualTo(64)

        diyFp1 = DiyFp(0x8000000000000000UL, 11)
        diyFp2 = DiyFp(2UL, 13)
        product = diyFp1 * diyFp2
        expectThat(product.significand).isEqualTo(1UL)
        expectThat(product.exponent).isEqualTo(64 + 13 + 11)

        diyFp1 = DiyFp(0x8000000000000001UL, 11)
        diyFp2 = DiyFp(1UL, 13)
        product = diyFp1 * diyFp2
        expectThat(product.significand).isEqualTo(1UL)
        expectThat(product.exponent).isEqualTo(64 + 13 + 11)

        diyFp1 = DiyFp(0x7fffffffffffffffUL, 11)
        diyFp2 = DiyFp(1UL, 13)
        product = diyFp1 * diyFp2
        expectThat(product.significand).isEqualTo(0UL)
        expectThat(product.exponent).isEqualTo(64 + 13 + 11)

        diyFp1 = DiyFp(0xFFFFFFFFFFFFFFFFUL, 11)
        diyFp2 = DiyFp(0xFFFFFFFFFFFFFFFFUL, 13)
        product = diyFp1 * diyFp2
        expectThat(product.significand).isEqualTo(0xFFFFFFFFFFFFFFFEUL)
        expectThat(product.exponent).isEqualTo(64 + 13 + 11)
    }
}