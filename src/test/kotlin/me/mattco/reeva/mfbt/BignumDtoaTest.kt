package me.mattco.reeva.mfbt

import me.mattco.reeva.mfbt.impl.Ref
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.assertions.isEqualTo

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
            expect {
                that(sign.get()).isEqualTo(v < 0.0)
                that(expectedString).isEqualTo(buffer.toString())
                that(expectedPoint).isEqualTo(point.get())
            }
        }
    }

    private fun trimRepresentation(representation: StringBuilder) {
        while (representation.lastOrNull() == '0')
            representation.deleteCharAt(representation.lastIndex)
    }
}