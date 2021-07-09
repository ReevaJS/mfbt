package me.mattco.reeva.mfbt

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class StringToFPTest {
    @Test
    fun basic() {
        doTest("123", 123.0)
        doTest("123.5", 123.5)
        doTest("-123.5", -123.5)
        doTest("999999999.77777", 999999999.77777)
    }

    @Test
    fun exponent() {
        doTest("1.6e10", 1.6e10)
        doTest("1.6e+10", 1.6e10)
        doTest("1.6e-10", 1.6e-10)
        doTest("1.45e-20", 1.45e-20)
        doTest("5.6234e124", 5.6234e124)
        doTest("5.6234e 124", null)
        doTest("5.6234e+ 124", null)
    }

    @Test
    fun infinity() {
        doTest("Infinity", Double.POSITIVE_INFINITY)
        doTest("-Infinity", Double.NEGATIVE_INFINITY)
        doTest("infinity", null)
        doTest("-infinity", null)
    }

    @Test
    fun blankSpace() {
        doTest("", 0.0)
        doTest("   ", 0.0)
        doTest("  42", 42.0)
        doTest("  + 42", null)
        doTest("  -42", -42.0)
        doTest("  - 42", null)
        doTest("123 ", 123.0)
        doTest("  123  ", 123.0)
        doTest("  Infinity  ", Double.POSITIVE_INFINITY)
        doTest("  -Infinity  ", Double.NEGATIVE_INFINITY)
        doTest("  5.6234e124  ", 5.6234e124)
    }

    @Test
    fun binary() {
        doTest("0b", null)
        doTest("0B", null)
        doTest("0b0", 0.0)
        doTest("0b1", 1.0)
        doTest("0b1000101", 0b1000101.toDouble())
        doTest("+0b1000101", 0b1000101.toDouble())
        doTest("-0b1000101", (-0b1000101).toDouble())
        doTest(
            "0b10010010100101001010101000011010010110100101110101010111000100101101011111010111010101111011001011" +
                "100101110100100101001010010011010101000011010010110100101110101010111000100101101011111010111010" +
                "101111011001011100101110",
            241199243704970887767784410176522416946418205519550223058663741230.0
        )
        doTest("0b10016", null)
        doTest("  0b1000101  ", 0b1000101.toDouble())
        doTest("0B1000101", 0b1000101.toDouble())
    }

    @Test
    fun octal() {
        doTest("0o", null)
        doTest("0O", null)
        doTest("0o0", 0.0)
        doTest("0o1", 1.0)
        doTest("0o1275", 701.0)
        doTest("+0o1275", 701.0)
        doTest("-0o1275", -701.0)
        doTest(
            "0o1237647671254371625347612534761344453354172347123466457312734123463461743",
            138186974733066607189064667016113110585120939925949657762426151907.0
        )
        doTest("0o1258", null)
        doTest("  0o1275  ", 701.0)
        doTest("0O1275", 701.0)
    }

    @Test
    fun hex() {
        doTest("0x", null)
        doTest("0X", null)
        doTest("0x0", 0.0)
        doTest("0x1", 1.0)
        doTest("0x123", 0x123.toDouble())
        doTest("0xababab123", 0xababab123.toDouble())
        doTest("+0xababab123", 0xababab123.toDouble())
        doTest("-0xababab123", (-0xababab123).toDouble())
        doTest(
            "0xfefefefefefefefefefefefefefefefefefefefefefefefefefefe",
            104899302289464805573612466790793293368624918555841075523901914878.0
        )
        doTest("0x123g", null)
        doTest("  0x123  ", 0x123.toDouble())
        doTest("0X123", 0x123.toDouble())
    }

    private fun doTest(input: String, expected: Double?) {
        expectThat(StringToFP(input).parse()).isEqualTo(expected)
    }
}