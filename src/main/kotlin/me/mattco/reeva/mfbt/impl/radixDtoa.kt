package me.mattco.reeva.mfbt.impl

import kotlin.math.floor
import kotlin.math.max

private const val CHARS = "0123456789abcdefghijklmnopqrstuvwxyz"

// v8/src/numbers/conversions.cc DoubleToRadixCString (as of commit bb78e6281039874d72b9f52608b8fbc695160da4)
// See LICENSE-V8
fun radixDtoa(value_: Double, radix: Int): String {
    var value = value_

    expect(radix in 2..36)
    expect(radix != 10)

    val buffer = CharArray(2200)
    var integerCursor = buffer.size / 2
    var fractionCursor = integerCursor
    val negative = value < 0.0
    if (negative)
        value = -value

    var integer = floor(value)
    var fraction = value - integer
    var delta = max(EDouble(0.0).nextDouble(), (0.5 * (EDouble(value).nextDouble() - value)))
    expect(delta >= 0.0)

    if (fraction >= delta) {
        buffer[fractionCursor++] = '.'
        do {
            fraction *= radix
            delta *= radix
            val digit = fraction.toInt()
            buffer[fractionCursor++] = CHARS[digit]
            fraction -= digit
            if (fraction > 0.5 || (fraction == 0.5 && (digit and 1) != 0)) {
                if (fraction + delta > 1) {
                    while (true) {
                        fractionCursor--
                        if (fractionCursor == buffer.size / 2) {
                            expect('.' == buffer[fractionCursor])
                            integer += 1
                            break
                        }

                        val c = buffer[fractionCursor]
                        val digit = if (c > '9') c - 'a' + 10 else c - '0'
                        if (digit + 1 < radix) {
                            buffer[fractionCursor++] = CHARS[digit + 1]
                            break
                        }
                    }
                    break
                }
            }
        } while (fraction >= delta)
    }

    while (EDouble(integer / radix.toDouble()).exponent() > 0) {
        integer /= radix
        buffer[--integerCursor] = '0'
    }

    do {
        val remainder = integer % radix
        expect(floor(remainder) == remainder)
        buffer[--integerCursor] = CHARS[remainder.toInt()]
        integer = (integer - remainder) / radix
    } while (integer > 0)

    if (negative)
        buffer[--integerCursor] = '-'

    return buffer.slice(integerCursor until fractionCursor).joinToString(separator = "")
}
