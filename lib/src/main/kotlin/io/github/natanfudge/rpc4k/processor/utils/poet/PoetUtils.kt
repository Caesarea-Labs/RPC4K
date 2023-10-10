package io.github.natanfudge.rpc4k.processor.utils.poet

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass

internal inline fun fileSpec(packageName: String, className: String, builder: FileSpec.Builder.() -> Unit): FileSpec {
    return FileSpec.builder(packageName, className).apply(builder).build()
}

internal inline fun FileSpec.Builder.addClass(name: String, builder: TypeSpec.Builder.() -> Unit) = addType(
    TypeSpec.classBuilder(name).apply(builder).build()
)

internal inline fun extensionFunction(receiver: TypeName, name: String, builder: FunSpec.Builder.() -> Unit) = FunSpec.builder(name).apply {
    receiver(receiver)
    builder()
}.build()


internal fun companionObject(name: String? = null, contents: TypeSpec.Builder.() -> Unit = {}) =
    TypeSpec.companionObjectBuilder(name).apply(contents).build()


internal inline fun funSpec(name: String, builder: FunSpec.Builder.() -> Unit) = FunSpec.builder(name).apply(builder).build()
internal inline fun TypeSpec.Builder.addFunction(name: String, builder: FunSpec.Builder.() -> Unit) = addFunction(funSpec(name, builder))


internal inline fun FunSpec.Builder.addControlFlow(start: String, vararg args: Any, controlFlow: () -> Unit) {
    beginControlFlow(start, *args)
    controlFlow()
    endControlFlow()
}

internal inline fun FunSpec.Builder.addControlFlow(format: FormattedString, controlFlow: () -> Unit) {
    addControlFlow(format.string, *format.formatArguments.toTypedArray()) { controlFlow() }
}

internal fun KClass<*>.methodName(method: String) = MemberName(this.asClassName(), method)

internal fun ClassName.companion() = ClassName(packageName,simpleName,"Companion")