package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.MemberName
import io.github.natanfudge.rpc4k.processor.utils.poet.FormattedString
import io.github.natanfudge.rpc4k.processor.utils.poet.withArgumentList
import io.github.natanfudge.rpc4k.processor.utils.toSerializerString

internal object ApiDefinitionConverters  {
     const val Package = "io.github.natanfudge.rpc4k.generated"
      val listOfFunction = MemberName("kotlin.collections", "listOf")

     fun listOfSerializers(rpc: RpcDefinition): FormattedString {
          return listOfFunction.withArgumentList(rpc.args.map { it.type.toSerializerString() })
     }
}