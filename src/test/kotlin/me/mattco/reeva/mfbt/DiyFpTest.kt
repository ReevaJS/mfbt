package me.mattco.reeva.mfbt

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DiyFpTest {
    @Test
    fun subtract() {
        var diyFp1 = DiyFp(3UL, 0)
        val diyFp2 = DiyFp(1UL, 0)
        val diff = diyFp1 - diyFp2

        Assertions.assertEquals(diff.significand, 2UL)
        Assertions.assertEquals(diff.exponent, 0)
        diyFp1 -= diyFp2
        Assertions.assertEquals(diyFp1.significand, 2UL)
        Assertions.assertEquals(diyFp1.exponent, 0)
    }

    @Test
    fun multiply() {
        var diyFp1 = DiyFp(3UL, 0)
        var diyFp2 = DiyFp(2UL, 0)
        var product = diyFp1 * diyFp2

        Assertions.assertEquals(product.significand, 0UL)
        Assertions.assertEquals(product.exponent, 64)
        diyFp1 *= diyFp2
        Assertions.assertEquals(diyFp1.significand, 0UL)
        Assertions.assertEquals(diyFp1.exponent, 64)

        diyFp1 = DiyFp(0x8000000000000000UL, 11)
        diyFp2 = DiyFp(2UL, 13)
        product = diyFp1 * diyFp2
        Assertions.assertEquals(product.significand, 1UL)
        Assertions.assertEquals(product.exponent, 64 + 13 + 11)

        diyFp1 = DiyFp(0x8000000000000001UL, 11)
        diyFp2 = DiyFp(1UL, 13)
        product = diyFp1 * diyFp2
        Assertions.assertEquals(product.significand, 1UL)
        Assertions.assertEquals(product.exponent, 64 + 13 + 11)

        diyFp1 = DiyFp(0x7fffffffffffffffUL, 11)
        diyFp2 = DiyFp(1UL, 13)
        product = diyFp1 * diyFp2
        Assertions.assertEquals(product.significand, 0UL)
        Assertions.assertEquals(product.exponent, 64 + 13 + 11)

        diyFp1 = DiyFp(0xFFFFFFFFFFFFFFFFUL, 11)
        diyFp2 = DiyFp(0xFFFFFFFFFFFFFFFFUL, 13)
        product = diyFp1 * diyFp2
        Assertions.assertEquals(product.significand, 0xFFFFFFFFFFFFFFFEUL)
        Assertions.assertEquals(product.exponent, 64 + 13 + 11)
    }
}