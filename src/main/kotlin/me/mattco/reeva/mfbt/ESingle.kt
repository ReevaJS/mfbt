package me.mattco.reeva.mfbt

class ESingle(val value: UInt) {
    constructor() : this(0U)
    constructor(value: Float) : this(value.toRawBits().toUInt())

    fun value() = Float.fromBits(value.toInt())

    fun significand(): UInt {
        val significand = value and SIGNIFICAND_MASK
        return if (isDenormal()) {
            significand
        } else significand + HIDDEN_BIT
    }

    fun exponent(): Int {
        if (isDenormal())
            return DENORMAL_EXPONENT

        val biasedE = ((value and EXPONENT_MASK) shr PHYSICAL_SIGNIFICAND_SIZE).toInt()
        return biasedE - EXPONENT_BIAS
    }

    fun asDiyFp(): DiyFp {
        expect(sign() > 0)
        expect(!isSpecial())
        return DiyFp(significand().toULong(), exponent())
    }

    fun isDenormal() = (value and EXPONENT_MASK) == 0U

    fun isSpecial() = (value and EXPONENT_MASK) == EXPONENT_MASK

    fun isNan() = isSpecial() && (value and SIGNIFICAND_MASK) != 0U

    fun isQuietNan() = isNan() && (value and QUIET_NAN_BIT) != 0U

    fun isSignalingNan() = isNan() && (value and QUIET_NAN_BIT) == 0U

    fun isInfinite() = isSpecial() && (value and SIGNIFICAND_MASK) == 0U

    fun sign() = if ((value and SIGN_MASK) == 0U) 1 else -1

    fun upperBoundary() = DiyFp((significand() * 2U + 1U).toULong(), exponent() - 1)

    fun normalizeBoundaries(outMinus: Ref<DiyFp>, outPlus: Ref<DiyFp>) {
        expect(value() > 0.0)

        val v = asDiyFp()
        val plus = DiyFp((v.significand shl 1) + 1UL, v.exponent - 1)
        plus.normalize()

        val minus = if (lowerBoundaryIsCloser()) {
            DiyFp((v.significand shl 2) - 1UL, v.exponent - 2)
        } else DiyFp((v.significand shl 1) - 1UL, v.exponent - 1)

        minus.significand = minus.significand shl (minus.exponent - plus.exponent)
        minus.exponent = plus.exponent

        outMinus.set(minus)
        outPlus.set(plus)
    }

    fun lowerBoundaryIsCloser(): Boolean {
        val physicalSignificandIsZero = (value and SIGNIFICAND_MASK) == 0U
        return physicalSignificandIsZero && exponent() != DENORMAL_EXPONENT
    }

    companion object {
        const val SIGN_MASK = 0x80000000U
        const val EXPONENT_MASK = 0x7F800000U
        const val SIGNIFICAND_MASK = 0x007FFFFFU
        const val HIDDEN_BIT = 0x00800000U
        const val QUIET_NAN_BIT = 0x00400000U
        const val PHYSICAL_SIGNIFICAND_SIZE = 23
        const val SIGNIFICAND_SIZE = 24
        const val EXPONENT_BIAS = 0x7F + PHYSICAL_SIGNIFICAND_SIZE
        const val DENORMAL_EXPONENT = -EXPONENT_BIAS + 1
        const val MAX_EXPONENT = 0xFF - EXPONENT_BIAS
        const val INFINITY = 0x7F800000U
        const val NAN = 0x7FC00000U

        fun infinity() = ESingle(INFINITY).value()

        fun nan() = ESingle(NAN).value()
    }
}