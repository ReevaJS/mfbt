package me.mattco.reeva.mfbt

import me.mattco.reeva.mfbt.EDouble.Companion.DENORMAL_EXPONENT
import me.mattco.reeva.mfbt.EDouble.Companion.EXPONENT_BIAS
import me.mattco.reeva.mfbt.EDouble.Companion.HIDDEN_BIT
import me.mattco.reeva.mfbt.EDouble.Companion.MAX_EXPONENT
import me.mattco.reeva.mfbt.EDouble.Companion.PHYSICAL_SIGNIFICAND_SIZE
import me.mattco.reeva.mfbt.EDouble.Companion.SIGNIFICAND_MASK

class DiyFp(var significand: ULong, var exponent: Int) {
    constructor() : this(0UL, 0)

    operator fun minus(other: DiyFp): DiyFp {
        return DiyFp(significand - other.significand, exponent)
    }

    operator fun times(other: DiyFp): DiyFp {
        val a = significand shr 32
        val b = significand and M32
        val c = other.significand shr 32
        val d = other.significand and M32
        val ac = a * c
        val bc = b * c
        val ad = a * d
        val bd = b * d

        val tmp = (bd shr 32) + (ad and M32) + (bc and M32) + (1U shl 31)
        return DiyFp(ac + (ad shr 32) + (bc shr 32) + (tmp shr 32), exponent + other.exponent + 64)
    }

    fun normalize() {
        expect(significand != 0UL)
        var s = significand
        var e = exponent

        while ((s and MS10BITS) == 0UL) {
            s = s shl 10
            e -= 10
        }

        while ((s and UINT64MSB) == 0UL) {
            s = s shl 1
            e--
        }

        significand = s
        exponent = e
    }

    // ieee.h: DiyFpToUin64
    fun toULong(): ULong {
        var s = significand
        var e = exponent
        while (s > HIDDEN_BIT + SIGNIFICAND_MASK) {
            s = s shr 1
            e++
        }

        if (e >= MAX_EXPONENT)
            return EDouble.INFINITY
        if (e < DENORMAL_EXPONENT)
            return 0UL

        while (e > DENORMAL_EXPONENT && (s and HIDDEN_BIT) == 0UL) {
            s = s shl 1
            e--
        }

        val biasedExponent = if (e == DENORMAL_EXPONENT && (s and HIDDEN_BIT) == 0UL) {
            0UL
        } else (e + EXPONENT_BIAS).toULong()
        return (s and SIGNIFICAND_MASK) or (biasedExponent shl PHYSICAL_SIGNIFICAND_SIZE)
    }

    companion object {
        const val SIGNIFICAND_SIZE = 64
        const val M32 = 0xFFFFFFFFUL
        const val MS10BITS = 0xFFC0000000000000UL
        const val UINT64MSB = 0x8000000000000000UL

    }
}