package io.github.natanfudge.rpc4k.processor.old

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import java.io.OutputStreamWriter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

internal fun KSClassDeclaration.getPublicApiFunctions() = getDeclaredFunctions()
    .filter { !it.isConstructor() && it.isPublic() }

internal fun KSTypeReference.toTypeName() = resolve().toTypeName()
internal fun KSType.toTypeName(): TypeName {
    val typeName = declaration.qualifiedName!!.toTypeName()
    val actualArguments = getActualArguments()
    val parameterized =
        if (actualArguments.isNotEmpty()) typeName.parameterizedBy(actualArguments.map { it.type!!.toTypeName() })
        else typeName
    return parameterized.copy(nullable = isMarkedNullable)
}

private fun KSType.getActualArguments(): List<KSTypeArgument> {
    val implClass = Class.forName("com.google.devtools.ksp.symbol.impl.kotlin.KSTypeImpl")
    return if (this::class.java.isAssignableFrom(implClass)) {
        val ksTypeArgumentsProperty = implClass.kotlin.declaredMemberProperties
            .single { it.name == "ksTypeArguments" } as KProperty1<Any?, List<KSTypeArgument>?>
        ksTypeArgumentsProperty.isAccessible = true
        val ksTypeArguments = ksTypeArgumentsProperty.get(this)
        ksTypeArguments ?: arguments
    } else arguments
}


internal fun KSName.toTypeName() = ClassName.bestGuess(asString())

internal val TypeName.rawType get() = if(this is ParameterizedTypeName) rawType else this

fun KSClassDeclaration.createKtFile(
    env: SymbolProcessorEnvironment,
    packageName: String,
    className: String,
    builder: FileSpec.Builder.() -> Unit
) {
    val fileOutputStream = env.codeGenerator.createNewFile(
        dependencies = Dependencies(false, this.containingFile!!),
        packageName = packageName,
        fileName = className,
        extensionName = "kt"
    )

    val ktFile = FileSpec.builder(packageName, className).apply(builder).build()

    val writer = OutputStreamWriter(fileOutputStream)
    writer.use(ktFile::writeTo)
}