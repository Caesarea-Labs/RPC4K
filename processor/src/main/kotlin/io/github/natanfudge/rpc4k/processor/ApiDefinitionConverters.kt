package io.github.natanfudge.rpc4k.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import io.github.natanfudge.rpc4k.processor.utils.poet.FormattedString
import io.github.natanfudge.rpc4k.processor.utils.poet.withArgumentList
import io.github.natanfudge.rpc4k.processor.utils.toSerializerString

internal object ApiDefinitionConverters  {
      val listOfFunction = MemberName("kotlin.collections", "listOf")

     fun listOfSerializers(rpc: RpcDefinition): FormattedString {
          return listOfFunction.withArgumentList(rpc.args.map { it.type.toSerializerString() })
     }
}

fun ApiDefinition.toClassName() = ClassName(implementationPackageName, name)
fun ApiDefinition.toCompanionClassName() = ClassName(implementationPackageName, name, "Companion")