package me.mattco.reeva.mfbt

import me.mattco.reeva.mfbt.impl.Ref
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BignumDtoaTest {
    @Test
    fun `bignum dtoa various doubles`() {
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
    }

    private fun trimRepresentation(representation: StringBuilder) {
        while (representation.lastOrNull() == '0')
            representation.deleteCharAt(representation.lastIndex)
    }
}