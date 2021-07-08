package me.mattco.reeva.mfbt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.mattco.reeva.mfbt.impl.Ref
import me.mattco.reeva.mfbt.impl.fastFixedDtoa
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isTrue

class FixedDtoaTest {
    @Test
    fun `fast fixed various doubles`() {
        val point = Ref<Int>()
        val buffer = StringBuilder()

        fun doTest(v: Double, fractionalCount: Int, bufferResult: String, expectedPoint: Int) {
            buffer.clear()
            expectThat(fastFixedDtoa(v, fractionalCount, buffer, point)).isTrue()
            expectThat(bufferResult).isEqualTo(buffer.toString())
            expectThat(expectedPoint).isEqualTo(point.get())
        }

        doTest(1.0, 1, "1", 1)
        doTest(1.0, 15, "1", 1)
        doTest(1.0, 0, "1", 1)
        doTest(0xFFFFFFFF.toDouble(), 5, "4294967295", 10)
        doTest(4294967296.0, 5, "4294967296", 10)
        doTest(1e21, 5, "1", 22)
        doTest(999999999999999868928.00, 2, "999999999999999868928", 21)
        doTest(6.9999999999999989514240000e+21, 5, "6999999999999998951424", 22)
        doTest(1.5, 5, "15", 1)
        doTest(1.55, 5, "155", 1)
        doTest(1.55, 1, "16", 1)
        doTest(1.00000001, 15, "100000001", 1)
        doTest(0.1, 10, "1", 0)
        doTest(0.01, 10, "1", -1)
        doTest(0.001, 10, "1", -2)
        doTest(0.0001, 10, "1", -3)
        doTest(0.00001, 10, "1", -4)
        doTest(0.000001, 10, "1", -5)
        doTest(0.0000001, 10, "1", -6)
        doTest(0.00000001, 10, "1", -7)
        doTest(0.000000001, 10, "1", -8)
        doTest(0.0000000001, 15, "1", -9)
        doTest(0.00000000001, 15, "1", -10)
        doTest(0.000000000001, 15, "1", -11)
        doTest(0.0000000000001, 15, "1", -12)
        doTest(0.00000000000001, 15, "1", -13)
        doTest(0.000000000000001, 20, "1", -14)
        doTest(0.0000000000000001, 20, "1", -15)
        doTest(0.00000000000000001, 20, "1", -16)
        doTest(0.000000000000000001, 20, "1", -17)
        doTest(0.0000000000000000001, 20, "1", -18)
        doTest(0.00000000000000000001, 20, "1", -19)
        doTest(0.10000000004, 10, "1", 0)
        doTest(0.01000000004, 10, "1", -1)
        doTest(0.00100000004, 10, "1", -2)
        doTest(0.00010000004, 10, "1", -3)
        doTest(0.00001000004, 10, "1", -4)
        doTest(0.00000100004, 10, "1", -5)
        doTest(0.00000010004, 10, "1", -6)
        doTest(0.00000001004, 10, "1", -7)
        doTest(0.00000000104, 10, "1", -8)
        doTest(0.0000000001000004, 15, "1", -9)
        doTest(0.0000000000100004, 15, "1", -10)
        doTest(0.0000000000010004, 15, "1", -11)
        doTest(0.0000000000001004, 15, "1", -12)
        doTest(0.0000000000000104, 15, "1", -13)
        doTest(0.000000000000001000004, 20, "1", -14)
        doTest(0.000000000000000100004, 20, "1", -15)
        doTest(0.000000000000000010004, 20, "1", -16)
        doTest(0.000000000000000001004, 20, "1", -17)
        doTest(0.000000000000000000104, 20, "1", -18)
        doTest(0.000000000000000000014, 20, "1", -19)
        doTest(0.10000000006, 10, "1000000001", 0)
        doTest(0.01000000006, 10, "100000001", -1)
        doTest(0.00100000006, 10, "10000001", -2)
        doTest(0.00010000006, 10, "1000001", -3)
        doTest(0.00001000006, 10, "100001", -4)
        doTest(0.00000100006, 10, "10001", -5)
        doTest(0.00000010006, 10, "1001", -6)
        doTest(0.00000001006, 10, "101", -7)
        doTest(0.00000000106, 10, "11", -8)
        doTest(0.0000000001000006, 15, "100001", -9)
        doTest(0.0000000000100006, 15, "10001", -10)
        doTest(0.0000000000010006, 15, "1001", -11)
        doTest(0.0000000000001006, 15, "101", -12)
        doTest(0.0000000000000106, 15, "11", -13)
        doTest(0.000000000000001000006, 20, "100001", -14)
        doTest(0.000000000000000100006, 20, "10001", -15)
        doTest(0.000000000000000010006, 20, "1001", -16)
        doTest(0.000000000000000001006, 20, "101", -17)
        doTest(0.000000000000000000106, 20, "11", -18)
        doTest(0.000000000000000000016, 20, "2", -19)
        doTest(0.6, 0, "1", 1)
        doTest(0.96, 1, "1", 1)
        doTest(0.996, 2, "1", 1)
        doTest(0.9996, 3, "1", 1)
        doTest(0.99996, 4, "1", 1)
        doTest(0.999996, 5, "1", 1)
        doTest(0.9999996, 6, "1", 1)
        doTest(0.99999996, 7, "1", 1)
        doTest(0.999999996, 8, "1", 1)
        doTest(0.9999999996, 9, "1", 1)
        doTest(0.99999999996, 10, "1", 1)
        doTest(0.999999999996, 11, "1", 1)
        doTest(0.9999999999996, 12, "1", 1)
        doTest(0.99999999999996, 13, "1", 1)
        doTest(0.999999999999996, 14, "1", 1)
        doTest(0.9999999999999996, 15, "1", 1)
        doTest(0.00999999999999996, 16, "1", -1)
        doTest(0.000999999999999996, 17, "1", -2)
        doTest(0.0000999999999999996, 18, "1", -3)
        doTest(0.00000999999999999996, 19, "1", -4)
        doTest(0.000000999999999999996, 20, "1", -5)
        doTest(323423.234234, 10, "323423234234", 6)
        doTest(12345678.901234, 4, "123456789012", 8)
        doTest(98765.432109, 5, "9876543211", 5)
        doTest(42.toDouble(), 20, "42", 2)
        doTest(0.5, 0, "1", 1)
        doTest(1e-23, 10, "", -10)
        doTest(1e-123, 2, "", -2)
        doTest(1e-123, 0, "", 0)
        doTest(1e-23, 20, "", -20)
        doTest(1e-21, 20, "", -20)
        doTest(1e-22, 20, "", -20)
        doTest(6e-21, 20, "1", -19)
        doTest(9.1193616301674545152000000e+19, 0, "91193616301674545152", 20)
        doTest(4.8184662102767651659096515e-04, 19, "4818466210276765", -3)
        doTest(1.9023164229540652612705182e-23, 8, "", -8)
        doTest(1000000000000000128.0, 0, "1000000000000000128", 19)
        doTest(2.10861548515811875e+15, 17, "210861548515811875", 16)
    }

    @Serializable
    data class PrecomputedFixed(
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
    fun `fast fixed dtoa gay fixed`() {
        val content = this::class.java.getResource("/fixed.json")!!.readText()
        val representations: List<PrecomputedFixed> = Json.Default.decodeFromString(content)

        for ((value, numDigits, representation, decimalPoint) in representations) {
            val builder = StringBuilder()
            val point = Ref<Int>()
            val status = fastFixedDtoa(value, numDigits, builder, point)
            expectThat(status).isTrue()
            expectThat(decimalPoint).isEqualTo(point.get())
            expectThat(numDigits).isGreaterThanOrEqualTo(builder.length - point.get())
            expectThat(representation).isEqualTo(builder.toString())
        }
    }
}