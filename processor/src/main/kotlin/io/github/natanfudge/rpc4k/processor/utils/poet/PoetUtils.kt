package io.github.natanfudge.rpc4k.processor.utils.poet

import com.squareup.kotlinpoet.*
import kotlinx.serialization.builtins.serializer
import kotlin.reflect.KClass

internal fun fileSpec(packageName: String, className: String, builder: FileSpec.Builder.() -> Unit): FileSpec {
    return FileSpec.builder(packageName, className).apply(builder).build()
}

internal fun FileSpec.Builder.addClass(name: String, builder: TypeSpec.Builder.() -> Unit) = addType(
    TypeSpec.classBuilder(name).apply(builder).build()
)

internal fun funSpec(name: String, builder: FunSpec.Builder.() -> Unit) = FunSpec.builder(name).apply(builder).build()

internal fun FunSpec.Builder.addControlFlow(start: String, vararg args: Any, controlFlow: () -> Unit) {
    beginControlFlow(start, *args)
    controlFlow()
    endControlFlow()
}

internal fun FunSpec.Builder.addControlFlow(format: FormattedString, controlFlow: () -> Unit) {
    addControlFlow(format.string, *format.formatArguments.toTypedArray()) { controlFlow() }
}

internal fun KClass<*>.methodName(method: String) = MemberName(this.asClassName(), method)