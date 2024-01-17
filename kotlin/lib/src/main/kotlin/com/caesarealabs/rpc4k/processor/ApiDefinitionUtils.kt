package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.utils.poet.FormattedString
import com.caesarealabs.rpc4k.processor.utils.poet.toSerializerString
import com.caesarealabs.rpc4k.processor.utils.poet.withArgumentList
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.MemberName
import kotlinx.serialization.ExperimentalSerializationApi

internal object ApiDefinitionUtils {
    val listOfFunction = MemberName("kotlin.collections", "listOf")
    // Kotlin doesn't let us reference these annotations directly sadly so we need to resort to strings
    private val experimentalUnsignedTypes = ClassName("kotlin", "ExperimentalUnsignedTypes")
    private val experimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
    private val optIn = ClassName("kotlin", "OptIn")

    fun listOfSerializers(rpc: RpcFunction): FormattedString {
        return listOfFunction.withArgumentList(rpc.parameters.map { it.type.toSerializerString() })
    }

    fun listOfEventSerializers(rpc: RpcEventEndpoint): FormattedString {
        return listOfFunction.withArgumentList(rpc.parameters.filter { !it.isDispatch }.map { it.value.type.toSerializerString() })
    }
    fun FileSpec.Builder.ignoreExperimentalWarnings() {
        addAnnotation(AnnotationSpec.builder(optIn).addMember("%T::class, %T::class", experimentalUnsignedTypes, experimentalSerializationApi).build())
//        addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "UNCHECKED_CAST").build())
//
//        addAnnotation()
//        addAnnotation(experimentalSerializationApi)
    }
}
