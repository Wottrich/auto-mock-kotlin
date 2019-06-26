package com.example.mockprocessor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.lang.NullPointerException
import java.lang.RuntimeException
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KClass

object ProcessorHelper {

    private val list = ClassName("kotlin.collections", "List")
    private val arrayList = ClassName("kotlin.collections", "ArrayList")
    private val mutable = ClassName("kotlin.collections", "MutableList")

    private val packageName: String = ""

    internal fun typeClass(value: Any? = null, type: TypeName? = null): KClass<*> {
        if (type != null) {
            return when (type) {
                INT -> Int::class
                DOUBLE -> Double::class
                BOOLEAN -> Boolean::class
                else -> String::class
            }
        }

        return when (value) {
            is Int -> Int::class
            is Double -> Double::class
            is Boolean -> Boolean::class
            else -> String::class
        }
    }

    internal fun format(value: Any? = null, type: TypeName? = null): String {
        if (type != null) {
            return when (type) {
                DOUBLE -> "%L"
                INT -> "%L"
                BOOLEAN -> "%L"
                else -> "%S"
            }
        }
        return when (value) {
            is Double -> "%L"
            is Int -> "%L"
            is Boolean -> "%L"
            else -> "%S"
        }
    }

    internal fun loadListGson (nameClass: String, map: List<Map<String, Any>>, filer: Filer) : TypeSpec.Builder {
        val typeSpec = TypeSpec.classBuilder(nameClass)
        var lastKey = ""
        var lastIndex: Int
        val constructor = FunSpec.constructorBuilder()
        val listCodeBlock: MutableList<CodeBlock> = mutableListOf()

        for ((index, mapper) in map.withIndex()) {
            lastIndex = index
            val parameters: MutableList<Any> = mutableListOf()

            for ((key, value) in mapper) {
                if (value is List<*>) {
                    value as List<Map<String, Any>>
                    loadListGson(key, map, filer).apply {
                        this.let { type ->
                            type.primaryConstructor(constructor.build())
                            createFile(packageName, nameClass, type.build(), filer)
                        }
                        parameters.add(this)
                    }
                } else {
                    if (lastKey != key || lastIndex != index) {
                        lastKey = key

                        val typeClass = typeClass(value = value)
                        createParameter(key, typeClass).apply {
                            constructor.addParameter(this)
                        }
                    }
                }
            }

            createMultiParameters(parameters).apply {
                listCodeBlock.add(this)
            }

        }

        return typeSpec
    }

    internal fun createInitializerWithCodeBlock(
        classType: ClassName,
        init: Boolean = false,
        value: Any? = null,
        initializer: String = "%S"
    ): CodeBlock {
        return buildCodeBlock {
            if (init) {
                if (value != null) {
                    this.add("%T", classType)
                    this.add("($initializer)", value)
                } else {
                    this.add("%T()", classType)
                }
            } else {
                this.add("%T", classType)
            }
        }
    }

    internal fun createMultiParameters(values: MutableList<Any>): CodeBlock {
        return buildCodeBlock {
            for ((index, value) in values.withIndex()) {

                val type = format(value)

                if (index != values.lastIndex) {
                    this.add(type, value)
                    this.add(", ")
                } else {
                    this.add(type, value)
                }


            }
        }
    }


    internal fun createInitializerMultiParametersWithCodeBlock(
        classType: ClassName,
        listCodeBlock: MutableList<CodeBlock>,
        init: Boolean = false
    ): MutableList<CodeBlock> {
        val mutCodeBlock: MutableList<CodeBlock> = mutableListOf()

        for (code in listCodeBlock) {
            buildCodeBlock {
                if (init) {
                    this.add("%T", classType)
                    this.add("(")
                    this.add(code)
                    this.add(")")

                } else {
                    this.add("%T", classType)
                }
                mutCodeBlock.add(this.build())
            }
        }

        return mutCodeBlock
    }

    internal fun createMutableListWithCodeBlock(
        classType: ClassName,
        listCodeBlock: MutableList<CodeBlock>
    ): PropertySpec {
        val mutClassType = mutable.parameterizedBy(classType)

        return PropertySpec.builder("list", mutClassType).let {
            val codeBlock = buildCodeBlock {

                this.add("mutableListOf(")
                for ((index, block) in listCodeBlock.withIndex()) {
                    if (index != listCodeBlock.lastIndex) {
                        this.add(block)
                        this.add(", ")
                    } else {
                        this.add(block)
                    }
                }
                this.add(")")
            }
            it.initializer(codeBlock)
            it.build()
        }

    }

    internal fun createMutableListField(
        classType: ClassName,
        size: Int,
        init: Boolean = false,
        value: Any? = null,
        initializer: String = "%S"
    ): PropertySpec {
        val mutClassType = mutable.parameterizedBy(classType)

        return PropertySpec.builder("list", mutClassType).let {

            val codeBlock = buildCodeBlock {
                this.add("mutableListOf(")
                for (i in 0..size) {
                    if (init) {
                        if (i != size) {
                            if (value != null) this.add("%T($initializer),", classType, value)
                            this.add("%T(),", classType)
                        } else {
                            if (value != null) this.add("%T($initializer)", classType, value)
                            else this.add("%T()", classType)
                        }
                    } else {
                        if (i != size) this.add("%T,", classType)
                        else this.add("%T", classType)
                    }
                }
                this.add(")")
            }

            it.initializer(codeBlock)
            it.build()
        }
    }

    internal fun createField(name: String, type: KClass<*>, value: Any, initializer: String): PropertySpec {
        return PropertySpec.builder(name, type).let {
            it.initializer(initializer, value)
            it.build()
        }
    }

    internal fun createParameter(name: String, type: KClass<*>): ParameterSpec {
        return ParameterSpec.builder(name, type).build()
    }

    internal fun createFile(packageName: String, className: String, builder: TypeSpec, filer: Filer) {
        val kotlinFile = FileSpec.builder(packageName, className).addType(builder).build()

        val kotlinFileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$className.kt")
        kotlinFileObject.openWriter().use { kotlinFile.writeTo(it) }
    }

}