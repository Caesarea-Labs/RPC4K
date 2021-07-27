package io.github.natanfudge.rpc4k//import com.google.devtools.ksp.processing.*
//import com.google.devtools.ksp.symbol.*
//import com.google.devtools.ksp.validate
//import java.io.OutputStream
//

//class io.github.natanfudge.rpc4k.Rpc4kProcessor(val env:SymbolProcessorEnvironment) : SymbolProcessor {
//    override fun process(resolver: Resolver): List<KSAnnotated> {
//        println("I exist!")
//        val symbols = resolver.getSymbolsWithAnnotation("io.github.natanfudge.rpc4k.Builder")
//        val ret = symbols.filter { !it.validate() }.toList()
//        symbols
//            .filter { it is KSClassDeclaration && it.validate() }
//            .forEach { it.accept(BuilderVisitor(), Unit) }
//        return ret
//    }
//
//    inner class BuilderVisitor : KSVisitorVoid() {
//        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
//            classDeclaration.primaryConstructor!!.accept(this, data)
//        }
//
//        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//            val parent = function.parentDeclaration as KSClassDeclaration
//            val packageName = parent.containingFile!!.packageName.asString()
//            val className = "${parent.simpleName.asString()}Builder"
//            val file = codeGenerator.createNewFile(Dependencies(true, function.containingFile!!), packageName , className)
//            file.io.github.natanfudge.rpc4k.appendText("package $packageName\n\n")
//            file.io.github.natanfudge.rpc4k.appendText("import HELLO\n\n")
//            file.io.github.natanfudge.rpc4k.appendText("class $className{\n")
//            function.parameters.forEach {
//                val name = it.name!!.asString()
//                val typeName = StringBuilder(it.type.resolve().declaration.qualifiedName?.asString() ?: "<ERROR>")
//                val typeArgs = it.type.element!!.typeArguments
//                if (it.type.element!!.typeArguments.isNotEmpty()) {
//                    typeName.append("<")
//                    typeName.append(
//                            typeArgs.map {
//                                val type = it.type?.resolve()
//                                "${it.variance.label} ${type?.declaration?.qualifiedName?.asString() ?: "ERROR"}" +
//                                        if (type?.nullability == Nullability.NULLABLE) "?" else ""
//                            }.joinToString(", ")
//                    )
//                    typeName.append(">")
//                }
//                file.io.github.natanfudge.rpc4k.appendText("    private var $name: $typeName? = null\n")
//                file.io.github.natanfudge.rpc4k.appendText("    internal fun with${name.capitalize()}($name: $typeName): $className {\n")
//                file.io.github.natanfudge.rpc4k.appendText("        this.$name = $name\n")
//                file.io.github.natanfudge.rpc4k.appendText("        return this\n")
//                file.io.github.natanfudge.rpc4k.appendText("    }\n\n")
//            }
//            file.io.github.natanfudge.rpc4k.appendText("    internal fun build(): ${parent.qualifiedName!!.asString()} {\n")
//            file.io.github.natanfudge.rpc4k.appendText("        return ${parent.qualifiedName!!.asString()}(")
//            file.io.github.natanfudge.rpc4k.appendText(
//                function.parameters.map {
//                    "${it.name!!.asString()}!!"
//                }.joinToString(", ")
//            )
//            file.io.github.natanfudge.rpc4k.appendText(")\n")
//            file.io.github.natanfudge.rpc4k.appendText("    }\n")
//            file.io.github.natanfudge.rpc4k.appendText("}\n")
//            file.close()
//        }
//    }
//
//}
//
//class Rpc4kProvider : SymbolProcessorProvider {
//    override fun create(
//        env: SymbolProcessorEnvironment
//    ): SymbolProcessor {
//        return io.github.natanfudge.rpc4k.Rpc4kProcessor(env)
//    }
//}