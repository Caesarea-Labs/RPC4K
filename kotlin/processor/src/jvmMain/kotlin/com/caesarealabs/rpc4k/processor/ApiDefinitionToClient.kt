package com.caesarealabs.rpc4k.processor

import com.caesarealabs.rpc4k.processor.utils.poet.funSpec
import com.caesarealabs.rpc4k.runtime.user.EventSubscription
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

/**
 * Common code used in [ApiDefinitionToNetworkClient], [ApiDefinitionToClientInterface], [ApiDefinitionToMemoryClient]
 * (for when those classes are eventually added)
 */
internal object ApiDefinitionToClient {
    /**
     * Specifies the header of normal request functions
     */
    fun createRequest(function: RpcFunction, suspend: Boolean = true, body: FunSpec.Builder.() -> Unit): FunSpec = funSpec(function.name) {
        // We may need to call network methods in this
        if (suspend) addModifiers(KModifier.SUSPEND)

        for (argument in function.parameters) addParameter(requestParameter(argument))
        val returnType = function.returnType
        returns(returnType.typeName)
        body()
    }

    /**
     * Specifies the header of event subscription functions
     */
    fun createEventSubscription(event: RpcEventEndpoint, body: FunSpec.Builder.() -> Unit) = funSpec(event.name) {
        for (argument in event.parameters) {
            if (!argument.isDispatch) addParameter(requestParameter(argument.value))
        }
        val returnType = EventSubscription::class.asClassName().parameterizedBy(event.returnType.typeName)
        returns(returnType)
        body()
    }


    private fun requestParameter(arg: RpcParameter): ParameterSpec {
        return ParameterSpec(arg.name, arg.type.typeName)
    }
}