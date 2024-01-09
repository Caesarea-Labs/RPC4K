package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.utils.poet.FormattedString
import com.caesarealabs.rpc4k.processor.utils.poet.toSerializerString
import com.caesarealabs.rpc4k.processor.utils.poet.withArgumentList
import com.squareup.kotlinpoet.MemberName

internal object ApiDefinitionUtils {
    val listOfFunction = MemberName("kotlin.collections", "listOf")

    fun listOfSerializers(rpc: RpcFunction): FormattedString {
        return listOfFunction.withArgumentList(rpc.parameters.map { it.type.toSerializerString() })
    }

    fun listOfEventSerializers(rpc: RpcEventEndpoint): FormattedString {
        return listOfFunction.withArgumentList(rpc.parameters.filter { !it.isDispatch }.map { it.value.type.toSerializerString() })
    }
}
