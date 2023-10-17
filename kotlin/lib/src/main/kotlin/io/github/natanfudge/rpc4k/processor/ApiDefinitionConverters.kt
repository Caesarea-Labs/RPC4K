package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.MemberName
import io.github.natanfudge.rpc4k.processor.utils.poet.FormattedString
import io.github.natanfudge.rpc4k.processor.utils.poet.toSerializerString
import io.github.natanfudge.rpc4k.processor.utils.poet.withArgumentList

internal object ApiDefinitionConverters {
    val listOfFunction = MemberName("kotlin.collections", "listOf")

    const val UnionTypeDiscriminator = "type"
    const val EnumNameProperty = "name"

    fun listOfSerializers(rpc: RpcDefinition): FormattedString {
        return listOfFunction.withArgumentList(rpc.parameters.map { it.type.toSerializerString() })
    }
}
