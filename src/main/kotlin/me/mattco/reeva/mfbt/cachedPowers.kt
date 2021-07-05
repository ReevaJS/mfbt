package me.mattco.reeva.mfbt

import kotlin.math.ceil

const val DECIMAL_EXPONENT_DISTANCE = 8
const val MIN_DECIMAL_EXPONENT = -348
const val MAX_DECIMAL_EXPONENT = 340
const val CACHED_POWERS_OFFSET = 348
const val D_1_LOG2_10 = 0.30102999566398114

data class CachedPower(val significand: ULong, val binaryExponent: Int, val decimalExponent: Int)

val cachedPowers = arrayOf(
    CachedPower(0xfa8fd5a0081c0288UL, -1220, -348),
    CachedPower(0xbaaee17fa23ebf76UL, -1193, -340),
    CachedPower(0x8b16fb203055ac76UL, -1166, -332),
    CachedPower(0xcf42894a5dce35eaUL, -1140, -324),
    CachedPower(0x9a6bb0aa55653b2dUL, -1113, -316),
    CachedPower(0xe61acf033d1a45dfUL, -1087, -308),
    CachedPower(0xab70fe17c79ac6caUL, -1060, -300),
    CachedPower(0xff77b1fcbebcdc4fUL, -1034, -292),
    CachedPower(0xbe5691ef416bd60cUL, -1007, -284),
    CachedPower(0x8dd01fad907ffc3cUL, -980, -276),
    CachedPower(0xd3515c2831559a83UL, -954, -268),
    CachedPower(0x9d71ac8fada6c9b5UL, -927, -260),
    CachedPower(0xea9c227723ee8bcbUL, -901, -252),
    CachedPower(0xaecc49914078536dUL, -874, -244),
    CachedPower(0x823c12795db6ce57UL, -847, -236),
    CachedPower(0xc21094364dfb5637UL, -821, -228),
    CachedPower(0x9096ea6f3848984fUL, -794, -220),
    CachedPower(0xd77485cb25823ac7UL, -768, -212),
    CachedPower(0xa086cfcd97bf97f4UL, -741, -204),
    CachedPower(0xef340a98172aace5UL, -715, -196),
    CachedPower(0xb23867fb2a35b28eUL, -688, -188),
    CachedPower(0x84c8d4dfd2c63f3bUL, -661, -180),
    CachedPower(0xc5dd44271ad3cdbaUL, -635, -172),
    CachedPower(0x936b9fcebb25c996UL, -608, -164),
    CachedPower(0xdbac6c247d62a584UL, -582, -156),
    CachedPower(0xa3ab66580d5fdaf6UL, -555, -148),
    CachedPower(0xf3e2f893dec3f126UL, -529, -140),
    CachedPower(0xb5b5ada8aaff80b8UL, -502, -132),
    CachedPower(0x87625f056c7c4a8bUL, -475, -124),
    CachedPower(0xc9bcff6034c13053UL, -449, -116),
    CachedPower(0x964e858c91ba2655UL, -422, -108),
    CachedPower(0xdff9772470297ebdUL, -396, -100),
    CachedPower(0xa6dfbd9fb8e5b88fUL, -369, -92),
    CachedPower(0xf8a95fcf88747d94UL, -343, -84),
    CachedPower(0xb94470938fa89bcfUL, -316, -76),
    CachedPower(0x8a08f0f8bf0f156bUL, -289, -68),
    CachedPower(0xcdb02555653131b6UL, -263, -60),
    CachedPower(0x993fe2c6d07b7facUL, -236, -52),
    CachedPower(0xe45c10c42a2b3b06UL, -210, -44),
    CachedPower(0xaa242499697392d3UL, -183, -36),
    CachedPower(0xfd87b5f28300ca0eUL, -157, -28),
    CachedPower(0xbce5086492111aebUL, -130, -20),
    CachedPower(0x8cbccc096f5088ccUL, -103, -12),
    CachedPower(0xd1b71758e219652cUL, -77, -4),
    CachedPower(0x9c40000000000000UL, -50, 4),
    CachedPower(0xe8d4a51000000000UL, -24, 12),
    CachedPower(0xad78ebc5ac620000UL, 3, 20),
    CachedPower(0x813f3978f8940984UL, 30, 28),
    CachedPower(0xc097ce7bc90715b3UL, 56, 36),
    CachedPower(0x8f7e32ce7bea5c70UL, 83, 44),
    CachedPower(0xd5d238a4abe98068UL, 109, 52),
    CachedPower(0x9f4f2726179a2245UL, 136, 60),
    CachedPower(0xed63a231d4c4fb27UL, 162, 68),
    CachedPower(0xb0de65388cc8ada8UL, 189, 76),
    CachedPower(0x83c7088e1aab65dbUL, 216, 84),
    CachedPower(0xc45d1df942711d9aUL, 242, 92),
    CachedPower(0x924d692ca61be758UL, 269, 100),
    CachedPower(0xda01ee641a708deaUL, 295, 108),
    CachedPower(0xa26da3999aef774aUL, 322, 116),
    CachedPower(0xf209787bb47d6b85UL, 348, 124),
    CachedPower(0xb454e4a179dd1877UL, 375, 132),
    CachedPower(0x865b86925b9bc5c2UL, 402, 140),
    CachedPower(0xc83553c5c8965d3dUL, 428, 148),
    CachedPower(0x952ab45cfa97a0b3UL, 455, 156),
    CachedPower(0xde469fbd99a05fe3UL, 481, 164),
    CachedPower(0xa59bc234db398c25UL, 508, 172),
    CachedPower(0xf6c69a72a3989f5cUL, 534, 180),
    CachedPower(0xb7dcbf5354e9beceUL, 561, 188),
    CachedPower(0x88fcf317f22241e2UL, 588, 196),
    CachedPower(0xcc20ce9bd35c78a5UL, 614, 204),
    CachedPower(0x98165af37b2153dfUL, 641, 212),
    CachedPower(0xe2a0b5dc971f303aUL, 667, 220),
    CachedPower(0xa8d9d1535ce3b396UL, 694, 228),
    CachedPower(0xfb9b7cd9a4a7443cUL, 720, 236),
    CachedPower(0xbb764c4ca7a44410UL, 747, 244),
    CachedPower(0x8bab8eefb6409c1aUL, 774, 252),
    CachedPower(0xd01fef10a657842cUL, 800, 260),
    CachedPower(0x9b10a4e5e9913129UL, 827, 268),
    CachedPower(0xe7109bfba19c0c9dUL, 853, 276),
    CachedPower(0xac2820d9623bf429UL, 880, 284),
    CachedPower(0x80444b5e7aa7cf85UL, 907, 292),
    CachedPower(0xbf21e44003acdd2dUL, 933, 300),
    CachedPower(0x8e679c2f5e44ff8fUL, 960, 308),
    CachedPower(0xd433179d9c8cb841UL, 986, 316),
    CachedPower(0x9e19db92b4e31ba9UL, 1013, 324),
    CachedPower(0xeb96bf6ebadf77d9UL, 1039, 332),
    CachedPower(0xaf87023b9bf0ee6bUL, 1066, 340),
)

fun getCachedPowerForBinaryExponentRange(
    minExponent: Int,
    maxExponent: Int,
    power: Ref<DiyFp>,
    decimalExponent: Ref<Int>
) {
    val q = DiyFp.SIGNIFICAND_SIZE
    val k = ceil((minExponent + q - 1) * D_1_LOG2_10)
    val foo = CACHED_POWERS_OFFSET
    val index = (foo + k.toInt() - 1) / DECIMAL_EXPONENT_DISTANCE + 1
    expect(index <= cachedPowers.lastIndex)
    val cachedPower = cachedPowers[index]
    expect(cachedPower.binaryExponent in minExponent..maxExponent)
    decimalExponent.set(cachedPower.decimalExponent)
    power.set(DiyFp(cachedPower.significand, cachedPower.binaryExponent))
}

fun getCachedPowerForDecimalExponent(requestedExponent: Int, power: Ref<DiyFp>, foundExponent: Ref<Int>) {
    expect(requestedExponent in MIN_DECIMAL_EXPONENT until MAX_DECIMAL_EXPONENT + DECIMAL_EXPONENT_DISTANCE)
    val index = (requestedExponent + CACHED_POWERS_OFFSET) / DECIMAL_EXPONENT_DISTANCE
    val cachedPower = cachedPowers[index]
    power.set(DiyFp(cachedPower.significand, cachedPower.binaryExponent))
    foundExponent.set(cachedPower.decimalExponent)
    expect(requestedExponent in foundExponent.get() until foundExponent.get() + DECIMAL_EXPONENT_DISTANCE)
}