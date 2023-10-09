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

/**
 * This is extra info that is passed when converting classes, that is intrinsic to the JVM and other languages don't know about.
 */
data class JvmContext(val userClassName: ClassName, val userCompanionClassName: ClassName, val userClassIsInterface: Boolean)

