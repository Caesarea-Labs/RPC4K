package com.caesarealabs.rpc4k.processor.utils.poet

import com.caesarealabs.rpc4k.runtime.implementation.KotlinClassName
import com.caesarealabs.rpc4k.runtime.implementation.KotlinMethodName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass

internal val KotlinClassName.kotlinPoet: ClassName get() = ClassName(pkg, simple.split("."))
internal val KotlinMethodName.kotlinPoet: MemberName get() = MemberName(pkg, simple)
