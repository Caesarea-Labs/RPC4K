@file:Suppress("DuplicatedCode", "UNCHECKED_CAST")

package io.github.natanfudge.rpc4k.typescript

import dev.adamko.kxstsgen.KxsTsGenerator
import io.github.natanfudge.rpc4k.processor.RpcType
import io.github.natanfudge.rpc4k.processor.utils.KotlinSerializer
import io.github.natanfudge.rpc4k.processor.utils.getKSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import java.lang.reflect.Method
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

/**
 * This has to be done separately, in the runtime of the project itself so we can take advantage of .serializer().
 *  The model list itself is only viable to get from the compilation process itself (i.e. KSP) because generic types
 *  are not available in runtime.
 */
object TypescriptModelGenerator {
    private val json = Json
//    fun generate(rpcClasses: List<KClass<*>>, path: Path) {
//        for(rpcClass in rpcClasses) {
//            generate(rpcClass, path = path.resolve("${rpcClass.simpleName}"))
//        }
//    }

    //TODO: test this with generic classes

    fun generate(modelsPath: Path, result: Path) {
        require(modelsPath.exists()) { "Models file at $modelsPath doesn't exist" }
        generate(modelsPath.readText(), result)
    }

    fun generate(modelsString: String, result: Path) {
        val models: List<RpcType> = json.decodeFromString(modelsString)
        val generator = KxsTsGenerator()
        val typescriptModels = models.map { generator.generate(getSerializer(it)) }
        println(typescriptModels.joinToString("\n"))
    }


    private fun getSerializer(model: RpcType): KSerializer<*> {
        val serializerParameters = model.typeArguments.map { getSerializer(it) }
        val serializerRepresentation = model.getKSerializer()

        val kClass = try {
            getClassFromKotlinName(model.qualifiedDollarName)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Invalid class name specified as a model of an API: ${model.qualifiedDollarName}", e)
        }

        val serializerMethod = getSerializerMethod(serializerRepresentation, kClass, argumentSerializerCount = serializerParameters.size)
        // User serializer - the .serializer() is a real method so it has a real receiver
        val receiver = if (serializerRepresentation is KotlinSerializer.User) kClass.companionObjectInstance else null

        // Builtin serializer extension - the .serializer is an extension method
        // Top-level serializer - no receiver.
        val extensionArgument =
            if (serializerRepresentation is KotlinSerializer.BuiltinExtension) listOf(kClass.companionObjectInstance) else listOf()

        // Remember - kotlin extension methods are static methods with the receiver as the first parameter!
        val actualSerializerParameters = (extensionArgument + serializerParameters).toTypedArray()

        return try {
            val serializer = serializerMethod.invoke(receiver, *actualSerializerParameters) as KSerializer<Any>
            if (model.isNullable) serializer.nullable else serializer
        } catch (e: Throwable) {
            throw IllegalArgumentException(
                "Class ${model.qualifiedDollarName} specified as model of API is not valid as a model since a serializer could not be created from it.",
                e
            )
        }
    }

    /**
     * Need to differentiate between different ways of getting KSerializers because kotlinx.serialization is weird
     */
    private fun getSerializerMethod(serializer: KotlinSerializer, kClass: KClass<*>, argumentSerializerCount: Int): Method {
        return when (serializer) {
            // ListSerializer()
            is KotlinSerializer.BuiltinToplevel -> Class.forName("kotlinx.serialization.builtins.BuiltinSerializersKt")
                .getDeclaredMethod(serializer.functionName, *serializerClasses(argumentSerializerCount))

            // Int.Companion.serializer()
            is KotlinSerializer.BuiltinExtension -> Class.forName("kotlinx.serialization.builtins.BuiltinSerializersKt")
                .getDeclaredMethod("serializer", getClassFromKotlinName(serializer.className).companionObject!!.java)

            // MyClass.Companion.serializer()
            is KotlinSerializer.User -> kClass.getUserSerializerMethod(argumentSerializerCount)
        }
    }

    private fun KClass<*>.getUserSerializerMethod(argumentSerializerCount: Int): Method {
        val companion = companionObject
        requireNotNull(companion) { "Class $this specified as model of API is not actually @Serializable since it doesn't have a companion object." }
        val serializerMethod = companion.java.getDeclaredMethod("serializer", *serializerClasses(argumentSerializerCount))
        requireNotNull(serializerMethod) { "Class $this specified as model of API is not actually @Serializable since the companion object doesn't have a .serializer() method" }

        return serializerMethod
    }

    private fun serializerClasses(amount: Int) = List(amount) { KSerializer::class.java }.toTypedArray()


    /**
     * This methods handles cases such as kotlin.Int which the jvm can't handle by itself.
     */
    private fun getClassFromKotlinName(name: String): KClass<*> {
        return kotlinFakeTypeToRealType[name] ?: Class.forName(name).kotlin
    }

    private val kotlinFakeTypeToRealType = listOf(
        Byte::class,
        Short::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        Boolean::class,
        Unit::class,
        String::class,
        Char::class,
        UInt::class,
        ULong::class,
        UByte::class,
        UShort::class,
        Set::class,
        List::class,
        Pair::class,
        Triple::class,
        Map.Entry::class,
        Map::class,
    ).associateBy { it.qualifiedName }

}