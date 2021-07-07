package me.mattco.reeva.mfbt.impl

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun unreachable(): Nothing {
    throw IllegalStateException("Encountered unreachable() call")
}

@OptIn(ExperimentalContracts::class)
fun expect(condition: Boolean, message: String? = null) {
    contract {
        returns() implies condition
    }

    if (!condition)
        throw ExpectationError(message ?: "Expectation failed")
}

@OptIn(ExperimentalContracts::class)
fun expect(condition: Boolean, messageProvider: () -> String) {
    contract {
        returns() implies condition
    }

    if (!condition)
        throw ExpectationError(messageProvider())
}

class ExpectationError(message: String) : Exception(message)

class Ref<T> {
    private var value: T? = null

    constructor()

    constructor(value: T) {
        this.value = value
    }

    fun get(): T = value!!

    fun set(value: T) {
        this.value = value
    }
}

fun StringBuilder.ensureSize(n: Int) = repeat(n - length) { append('?') }

