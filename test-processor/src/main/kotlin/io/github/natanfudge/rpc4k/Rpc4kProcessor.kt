@file:JvmName("AsdfKt")

package io.github.natanfudge.rpc4k

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStream
import java.io.OutputStreamWriter

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Api

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

internal const val GeneratedClientImplSuffix ="ClientImpl"
internal const val GeneratedServerImplSuffix ="Decoder"

class Rpc4kProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = env.codeGenerator
    private var invoked = false

    private val resolveCache = mutableMapOf<KSTypeReference,KSType>()

    private fun KSTypeReference.resolveCached() : KSType = resolveCache.computeIfAbsent(this){resolve()}

    override fun process(resolver: Resolver): List<KSAnnotated> {
        env.logger.warn("Invoking processor")
        println("asdf")
        if (invoked) {
            return emptyList()
        }
        env.logger.warn("Invoking processor for the first time")

        val apiClasses = resolver.getSymbolsWithAnnotation("io.github.natanfudge.rpc4k.Api")
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        for (apiClass in apiClasses) {
            generateRpc(apiClass)
        }

        invoked = true
        return apiClasses
    }

    private fun generateRpc(apiClass: KSClassDeclaration) {
        env.logger.warn("Generating RPC classes for: ${apiClass.qualifiedName!!.asString()}")

        val usedTypes = apiClass.getActualFunctions().map {func-> func.parameters.map{it.type}  + func.returnType!!}.flatten()
        val usingBuiltinTypes = usedTypes.any { it.resolveCached().declaration.qualifiedName!!.asString() in builtinSerializableTypes }

        generateClientImplementation(usingBuiltinTypes,apiClass)
        generateServerDecoder(usingBuiltinTypes,apiClass)
    }

    private fun generateServerDecoder(usingBuiltinTypes: Boolean, apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedServerImplSuffix
        val apiClassTypeName = apiClass.qualifiedName!!.toTypeName()
        apiClass.createKtFile(apiClass.packageName.asString(),generatedClassName){
            if (usingBuiltinTypes) {
                addImport("kotlinx.serialization.builtins", "serializer")
            }
            addType(
                TypeSpec.classBuilder(generatedClassName)
                    .constructorProperty(
                        name = "protocol",
                        type = apiClassTypeName,
                        KModifier.PRIVATE
                    )
                    .addSuperinterface(ProtocolDecoder::class.asTypeName().parameterizedBy(apiClassTypeName))
                    .generateServerMethodImplementation( apiClass,this@createKtFile)
                    .build()
            )
        }
    }

    private fun TypeSpec.Builder.generateServerMethodImplementation(
        apiClass: KSClassDeclaration,
        fileSpec: FileSpec.Builder
    ) : TypeSpec.Builder{
//        val methodName = apiMethod.simpleName.asString()
//        val parameterTypes = apiMethod.parameters.map { it.type.resolve() }
//        val returnType = apiMethod.returnType!!.resolve()
//        val returnTypeName = returnType.toTypeName()
//
//        val usingBuiltinTypes =
//            (parameterTypes + returnType).any { it.declaration.qualifiedName!!.asString() in builtinSerializableTypes }
//        if (usingBuiltinTypes) {
//            fileSpec.addImport("kotlinx.serialization.builtins", "serializer")
//        }

        addFunction(FunSpec.builder("accept")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(name = "route",type = String::class)
            .addParameter(name = "args", type = List::class.parameterizedBy(String::class))
            .returns(String::class)
            .apply {
                val (routesString,routeTypes) = apiClass.getActualFunctions()
                    .map { generateServerRouteDecoder(it) }.split()

                val generatedUtils = Rpc4kGeneratedServerUtils::class.asTypeName()

                val types = routeTypes.flatten() + generatedUtils

                addStatement("""
                    |return when (route) {
                    |    ${routesString.joinToString("\n    ")}
                    |    else -> %T.invalidRoute(route)
                    |}
                """.trimMargin(), *types.toList().toTypedArray())
//                val clientUtils = Rpc4KGeneratedClientUtils::class
////                val sendMethod  = Rpc4KGeneratedClientUtils::send
//                val parameterSerializersString = apiMethod.parameters
//                    .joinToString(",\n") { parameter -> "\t\t\t${parameter.name!!.asString()} to %T.serializer()" }
//
//                val types = listOf(
//                    clientUtils.asTypeName()
//                ) + parameterTypes.map { it.toTypeName() } +
//                        listOf(returnTypeName)
//
//                addStatement(
//                    "return %T.send(\n\t\tclient,\n\t\t\"$methodName\",\n\t\tlistOf(\n$parameterSerializersString\n\t\t),\n\t\t%T.serializer()\n\t)",
//                    *types.toTypedArray()
//                )
            }
            .build()
        )
        return this
    }
    private fun generateServerRouteDecoder(route: KSFunctionDeclaration) : Pair<String,List<TypeName>>{
        val methodName = route.simpleName.asString()
        val (parametersString,parameterTypes) = route.parameters.mapIndexed  (::generateServerParameterDecoder).split()
        val types = listOf(Rpc4kGeneratedServerUtils::class.asTypeName(), route.returnType!!.toTypeName()) + parameterTypes.flatten()
        val formatString = """
            |"$methodName" -> %T.encodeResponse(
            |        %T.serializer(), protocol.$methodName(
            |            ${parametersString.joinToString(",\n            ")}
            |        )
            |    )
        """.trimMargin()
        return formatString to types
    }

    private fun generateServerParameterDecoder( index: Int,parameter: KSValueParameter) : Pair<String,List<TypeName>>{
        val formatString = """
            %T.decodeParameter(%T.serializer(), args[$index])
        """.trimIndent()
        val types = listOf(Rpc4kGeneratedServerUtils::class.asTypeName(), parameter.type.toTypeName())
        return formatString to types
    }

    private fun generateClientImplementation(usingBuiltinTypes: Boolean, apiClass: KSClassDeclaration) {
        val generatedClassName = apiClass.simpleName.asString() + GeneratedClientImplSuffix
        apiClass.createKtFile(apiClass.packageName.asString(), generatedClassName) {
            if (usingBuiltinTypes) {
                addImport("kotlinx.serialization.builtins", "serializer")
            }
            addType(
                TypeSpec.classBuilder(generatedClassName)
                    .constructorProperty(
                        name = "client",
                        type = ClassName.bestGuess(RpcClient::class.qualifiedName!!),
                        KModifier.PRIVATE
                    )
                    .superclass(apiClass.qualifiedName!!.toTypeName())
                    .apply {
                        apiClass.getActualFunctions()
                            .forEach { generateClientMethodImplementation(it, this@createKtFile) }
                    }
                    .build()
            )
        }
    }

    private fun KSClassDeclaration.getActualFunctions() = getDeclaredFunctions().filter { !it.isConstructor() }

    private val builtinSerializableTypes = listOf(
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
        UShort::class
    ).map { it.qualifiedName!! }.toHashSet()

    private fun TypeSpec.Builder.generateClientMethodImplementation(
        apiMethod: KSFunctionDeclaration,
        fileSpec: FileSpec.Builder
    ) {
        val methodName = apiMethod.simpleName.asString()

        val returnTypeName = apiMethod.returnType!!.toTypeName()
//        val returnTypeName = returnType.toTypeName()



        addFunction(FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .generateClientMethodImplementationParameters(apiMethod)
            .apply {
                val clientUtils = Rpc4KGeneratedClientUtils::class
//                val sendMethod  = Rpc4KGeneratedClientUtils::send
                val parameterSerializersString = apiMethod.parameters
                    .joinToString(",\n\t\t\t") { parameter -> "${parameter.name!!.asString()} to %T.serializer()" }

                val types = listOf(
                    clientUtils.asTypeName()
                ) + apiMethod.parameters.map { it.type.toTypeName() } + returnTypeName


                addStatement(
                    """
                    |return %T.send(
                    |       client,
                    |       "$methodName",
                    |       listOf(
                    |          $parameterSerializersString
                    |       ),
                    |       %T.serializer()
                    |    )
                        """.trimMargin(),
                    *types.toTypedArray()
                )
            }
            .returns(returnTypeName)
            .build()
        )
    }

    private fun KSTypeReference.toTypeName() = resolveCached().declaration.qualifiedName!!.toTypeName()

    private fun FunSpec.Builder.generateClientMethodImplementationParameters(apiMethod: KSFunctionDeclaration): FunSpec.Builder {
        for (parameter in apiMethod.parameters) {
            addParameter(
                name = parameter.name!!.asString(),
                type = parameter.type.toTypeName()
            )
        }
        return this
    }

    private fun KSName.toTypeName() = ClassName.bestGuess(asString())

    private fun TypeSpec.Builder.constructorProperty(
        name: String,
        type: TypeName,
        vararg modifiers: KModifier
    ): TypeSpec.Builder {
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(name, type)
                .build()
        )

        addProperty(PropertySpec.builder(name, type).initializer(name).addModifiers(*modifiers).build())
        return this
    }

    private fun KSClassDeclaration.createKtFile(
        packageName: String,
        className: String,
        builder: FileSpec.Builder.() -> Unit
    ) {
        val fileOutputStream = codeGenerator.createNewFile(
            dependencies = Dependencies(false, this.containingFile!!),
            packageName = packageName,
            fileName = className,
            extensionName = "kt"
        )

        val ktFile = FileSpec.builder(packageName, className).apply(builder)
            .build()

        val writer = OutputStreamWriter(fileOutputStream)
        writer.use(ktFile::writeTo)
    }

    private fun <K,V> Sequence<Pair<K,V>>.split() : Pair<Sequence<K>, Sequence<V>>{
        return map { it.first } to map { it.second }
    }
    private fun <K,V> List<Pair<K,V>>.split() : Pair<List<K>, List<V>>{
        return map { it.first } to map { it.second }
    }

    //        println("test processing...")
//        file = codeGenerator.createNewFile(Dependencies(false), "", "TestProcessor", "log")
//        emit("TestProcessor: init($options)", "")
//
//        val javaFile = codeGenerator.createNewFile(Dependencies(false), "", "Generated", "java")
//        javaFile.io.github.natanfudge.rpc4k.appendText("class Generated {}")
//
//        val fileKt = codeGenerator.createNewFile(Dependencies(false), "", "HELLO", "java")
//        fileKt.io.github.natanfudge.rpc4k.appendText("public class HELLO{\n")
//        fileKt.io.github.natanfudge.rpc4k.appendText("public int foo() { return 1234; }\n")
//        fileKt.io.github.natanfudge.rpc4k.appendText("}")
//
//        val files = resolver.getAllFiles()
//        emit("TestProcessor: process()", "")
//        val visitor = TestVisitor()
//        for (file in files) {
//            emit("TestProcessor: processing ${file.fileName}", "")
//            file.accept(visitor, "")
//        }

//    inner class TestVisitor : KSVisitor<String, Unit> {
//
//        override fun visitReferenceElement(element: KSReferenceElement, data: String) {
//        }
//
//        override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: String) {
//            TODO("Not yet implemented")
//        }
//
//        override fun visitNode(node: KSNode, data: String) {
//            TODO("Not yet implemented")
//        }
//
//        override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: String) {
//            TODO("Not yet implemented")
//        }
//
//        override fun visitDynamicReference(reference: KSDynamicReference, data: String) {
//            TODO("Not yet implemented")
//        }
//
//        val visited = HashSet<Any>()
//
//        private fun checkVisited(symbol: Any): Boolean {
//            return if (visited.contains(symbol)) {
//                true
//            } else {
//                visited.add(symbol)
//                false
//            }
//        }
//
//        private fun invokeCommonDeclarationApis(declaration: KSDeclaration, indent: String) {
//            emit(
//                "${declaration.modifiers.joinToString(" ")} ${declaration.simpleName.asString()}", indent
//            )
//            declaration.annotations.forEach { it.accept(this, "$indent  ") }
//            if (declaration.parentDeclaration != null)
//                emit("  enclosing: ${declaration.parentDeclaration!!.qualifiedName?.asString()}", indent)
//            declaration.containingFile?.let { emit("${it.packageName.asString()}.${it.fileName}", indent) }
//            declaration.typeParameters.forEach { it.accept(this, "$indent  ") }
//        }
//
//        override fun visitFile(file: KSFile, data: String) {
//            if (checkVisited(file)) return
//            file.annotations.forEach { it.accept(this, "$data  ") }
//            emit(file.packageName.asString(), data)
//            for (declaration in file.declarations) {
//                declaration.accept(this, data)
//            }
//        }
//
//        override fun visitAnnotation(annotation: KSAnnotation, data: String) {
//            if (checkVisited(annotation)) return
//            emit("annotation", data)
//            annotation.annotationType.accept(this, "$data  ")
//            annotation.arguments.forEach { it.accept(this, "$data  ") }
//        }
//
//        override fun visitCallableReference(reference: KSCallableReference, data: String) {
//            if (checkVisited(reference)) return
//            emit("element: ", data)
//            reference.functionParameters.forEach { it.accept(this, "$data  ") }
//            reference.receiverType?.accept(this, "$data receiver")
//            reference.returnType.accept(this, "$data  ")
//        }
//
//        override fun visitPropertyGetter(getter: KSPropertyGetter, data: String) {
//            if (checkVisited(getter)) return
//            emit("propertyGetter: ", data)
//            getter.annotations.forEach { it.accept(this, "$data  ") }
//            emit(getter.modifiers.joinToString(" "), data)
//            getter.returnType?.accept(this, "$data  ")
//        }
//
//        override fun visitPropertySetter(setter: KSPropertySetter, data: String) {
//            if (checkVisited(setter)) return
//            emit("propertySetter: ", data)
//            setter.annotations.forEach { it.accept(this, "$data  ") }
//            emit(setter.modifiers.joinToString(" "), data)
//        }
//
//        override fun visitTypeArgument(typeArgument: KSTypeArgument, data: String) {
//            if (checkVisited(typeArgument)) return
//            typeArgument.annotations.forEach { it.accept(this, "$data  ") }
//            emit(
//                when (typeArgument.variance) {
//                    Variance.STAR -> "*"
//                    Variance.COVARIANT -> "out"
//                    Variance.CONTRAVARIANT -> "in"
//                    else -> ""
//                }, data
//            )
//            typeArgument.type?.accept(this, "$data  ")
//        }
//
//        override fun visitTypeParameter(typeParameter: KSTypeParameter, data: String) {
//            if (checkVisited(typeParameter)) return
//            typeParameter.annotations.forEach { it.accept(this, "$data  ") }
//            if (typeParameter.isReified) {
//                emit("reified ", data)
//            }
//            emit(
//                when (typeParameter.variance) {
//                    Variance.COVARIANT -> "out "
//                    Variance.CONTRAVARIANT -> "in "
//                    else -> ""
//                } + typeParameter.name.asString(), data
//            )
//            if (typeParameter.bounds.toList().isNotEmpty()) {
//                typeParameter.bounds.forEach { it.accept(this, "$data  ") }
//            }
//        }
//
//        override fun visitValueParameter(valueParameter: KSValueParameter, data: String) {
//            if (checkVisited(valueParameter)) return
//            valueParameter.annotations.forEach { it.accept(this, "$data  ") }
//            if (valueParameter.isVararg) {
//                emit("vararg", "$data  ")
//            }
//            if (valueParameter.isNoInline) {
//                emit("noinline", "$data  ")
//            }
//            if (valueParameter.isCrossInline) {
//                emit("crossinline ", "$data  ")
//            }
//            emit(valueParameter.name?.asString() ?: "_", "$data  ")
//            valueParameter.type.accept(this, "$data  ")
//        }
//
//        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: String) {
//            if (checkVisited(function)) return
//            invokeCommonDeclarationApis(function, data)
//            for (declaration in function.declarations) {
//                declaration.accept(this, "$data  ")
//            }
//            function.parameters.forEach { it.accept(this, "$data  ") }
//            function.typeParameters.forEach { it.accept(this, "$data  ") }
//            function.extensionReceiver?.accept(this, "$data extension:")
//            emit("returnType:", data)
//            function.returnType?.accept(this, "$data  ")
//        }
//
//        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: String) {
//            if (checkVisited(classDeclaration)) return
//            invokeCommonDeclarationApis(classDeclaration, data)
//            emit(classDeclaration.classKind.type, data)
//            for (declaration in classDeclaration.declarations) {
//                declaration.accept(this, "$data ")
//            }
//            classDeclaration.superTypes.forEach { it.accept(this, "$data  ") }
//            classDeclaration.primaryConstructor?.accept(this, "$data  ")
//        }
//
//        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: String) {
//            if (checkVisited(property)) return
//            invokeCommonDeclarationApis(property, data)
//            property.type.accept(this, "$data  ")
//            property.extensionReceiver?.accept(this, "$data extension:")
//            property.setter?.accept(this, "$data  ")
//            property.getter?.accept(this, "$data  ")
//        }
//
//        override fun visitTypeReference(typeReference: KSTypeReference, data: String) {
//            if (checkVisited(typeReference)) return
//            typeReference.annotations.forEach { it.accept(this, "$data  ") }
//            val type = typeReference.resolve()
//            type.let {
//                emit("resolved to: ${it.declaration.qualifiedName?.asString()}", data)
//            }
//            try {
//                typeReference.element?.accept(this, "$data  ")
//            } catch (e: IllegalStateException) {
//                emit("TestProcessor: exception: $e", data)
//            }
//        }
//
//        override fun visitAnnotated(annotated: KSAnnotated, data: String) {
//        }
//
//        override fun visitDeclaration(declaration: KSDeclaration, data: String) {
//        }
//
//        override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: String) {
//        }
//
//        override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: String) {
//        }
//
//        override fun visitClassifierReference(reference: KSClassifierReference, data: String) {
//            if (checkVisited(reference)) return
//            if (reference.typeArguments.isNotEmpty()) {
//                reference.typeArguments.forEach { it.accept(this, "$data  ") }
//            }
//        }
//
//        override fun visitTypeAlias(typeAlias: KSTypeAlias, data: String) {
//        }
//
//        override fun visitValueArgument(valueArgument: KSValueArgument, data: String) {
//            if (checkVisited(valueArgument)) return
//            val name = valueArgument.name?.asString() ?: "<no name>"
//            emit("$name: ${valueArgument.value}", data)
//            valueArgument.annotations.forEach { it.accept(this, "$data  ") }
//        }
//    }

}

class Rpc4kProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return Rpc4kProcessor(env)
    }
}
