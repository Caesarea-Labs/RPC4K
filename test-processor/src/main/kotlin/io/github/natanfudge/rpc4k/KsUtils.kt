package io.github.natanfudge.rpc4k

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

fun KSClassDeclaration.getActualFunctions() = getDeclaredFunctions().filter { !it.isConstructor() }

fun KSTypeReference.toTypeName(): TypeName = resolve().declaration.qualifiedName!!.toTypeName()
fun KSName.toTypeName() = ClassName.bestGuess(asString())

