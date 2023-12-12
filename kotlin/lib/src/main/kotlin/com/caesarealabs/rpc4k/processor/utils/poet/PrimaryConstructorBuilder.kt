package com.caesarealabs.rpc4k.processor.utils.poet

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass

/**
 * KotlinPoet doesn't have native support for constructor properties, but it allows generating them by merging parameters of the primary constructor
 * with properties of the class.
 */
class PrimaryConstructorBuilder {
    private data class ConstructorProperty(val name: String, val type: TypeName, val modifiers: List<KModifier>)

    private val properties = mutableListOf<ConstructorProperty>()

    fun addConstructorProperty(name: String, type: TypeName, vararg modifiers: KModifier) {
        properties.add(ConstructorProperty(name, type, modifiers.toList()))
    }

    fun addConstructorProperty(name: String, type: KClass<*>, vararg modifiers: KModifier) {
        properties.add(ConstructorProperty(name, type.asTypeName(), modifiers.toList()))
    }

    fun __addToType(builder: TypeSpec.Builder) {
        // Generate parameters in the constructor
        builder.primaryConstructor(
            FunSpec.constructorBuilder()
                .apply {
                    for (property in properties) {
                        addParameter(property.name, property.type)
                    }
                }
                .build()
        )

        // Generate properties of the same name in the class itself
        for (property in properties) {
            builder.addProperty(
                PropertySpec.builder(property.name, property.type)
                    .initializer(property.name)
                    .addModifiers(*property.modifiers.toTypedArray())
                    .build()
            )
        }

        // Now Kotlinpoet will merge those 2 into constructor properties.

    }
}

fun TypeSpec.Builder.addPrimaryConstructor(builder: PrimaryConstructorBuilder.() -> Unit) {
    PrimaryConstructorBuilder().apply(builder).__addToType(this)
}