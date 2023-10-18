//package io.github.natanfudge.rpc4k.processor
//
//import com.squareup.kotlinpoet.ClassName
//import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
//import com.squareup.kotlinpoet.TypeName
//import kotlinx.serialization.Serializable
//
//
//data class KotlinService(
//    val name: String,
//    val packageName: String,
//    val methods: List<RpcDefinition>,
//) {
//    val className = ClassName(packageName, name)
//    val qualifiedName = "$packageName.$name"
//}
//
//@Serializable
//data class ServiceMethod(val name: String, val parameters: List<RpcParameter>, val returnType: KotlinTypeReference)
//
///**
// * More precise description of an [RpcType] that comes from the JVM, and makes it easier to generate kotlin code as it includes the package name of the type
// * and uses kotlin class names and not RPC class names
// */
//@Serializable(with = KotlinTypeReferenceSerializer::class)
//data class KotlinTypeReference(
//    val packageName: String,
//    val simpleName: String,
//    val isNullable: Boolean = false,
//    // True in cases where the value is initialized by a default value
//    val hasDefaultValue: Boolean = false,
//    val typeArguments: List<KotlinTypeReference> = listOf(),
//    val isTypeParameter: Boolean = false,
//    val inlinedType: KotlinTypeReference? = null
//) {
//    companion object {
//        val string = KotlinTypeReference("kotlin", "String")
//    }
//
//    // Inner classes are dot seperated
//    val qualifiedName = "$packageName.$name"
//
//    val className = ClassName(packageName, simpleName)
//
//    val typeName: TypeName = className.let { name ->
//        if (typeArguments.isEmpty()) name else name.parameterizedBy(typeArguments.map { it.typeName })
//    }.copy(nullable = isNullable)
//    val isUnit get() = packageName == "kotlin" && simpleName == "Unit"
//}