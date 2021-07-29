package io.github.natanfudge.rpc4k.impl

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

internal fun KSClassDeclaration.getActualFunctions() = getDeclaredFunctions().filter { !it.isConstructor() }

internal fun KSTypeReference.toTypeName(): TypeName {
    val type = resolve()
    val typeName = type.declaration.qualifiedName!!.toTypeName()
    return if (type.arguments.isNotEmpty()) typeName.parameterizedBy(type.arguments.map { it.type!!.toTypeName() })
    else typeName
}


internal fun KSName.toTypeName() = ClassName.bestGuess(asString())

