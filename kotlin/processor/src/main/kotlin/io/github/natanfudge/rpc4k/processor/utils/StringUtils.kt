package io.github.natanfudge.rpc4k.processor.utils

inline fun String.appendIf(condition: Boolean, toAppend: () -> String) = if(condition) this + toAppend() else this