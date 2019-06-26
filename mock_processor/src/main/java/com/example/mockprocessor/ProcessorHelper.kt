package com.example.mockprocessor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import kotlin.reflect.KClass

object ProcessorHelper {

    private val list = ClassName("kotlin.collections", "List")
    private val arrayList = ClassName("kotlin.collections", "ArrayList")
    private val mutable = ClassName("kotlin.collections", "MutableList")

    lateinit var messager: Messager

    private val classes: MutableList<String> = mutableListOf()
    private val objects: MutableList<String> = mutableListOf()

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

    internal fun loadGson (typeSpec: TypeSpec.Builder, className: ClassName, nameClass: String = "", packageName: String, map: Map<String, Any>, filer: Filer, messager: Messager){
        var lastKey = ""
        var listCodeBlock: MutableList<CodeBlock> = mutableListOf()

        val parameters: MutableList<Any> = mutableListOf()

        for ((key, value) in map) {

            parameters.add(value)

            if (value is List<*>) {
                value as List<Map<String, Any>>

                val newName: String = key.substring(0,1).toUpperCase() + key.substring(1)
                val newTypeSpec = TypeSpec.classBuilder(newName)
                classes.add(newName)

                loadListGson(newTypeSpec, newName, packageName, value, filer).apply {
                    typeSpec.addProperty(this)
                }

            } else if (value is Map<*, *>) {
                value as Map<String, Any>

                val newName: String = key.substring(0,1).toUpperCase() + key.substring(1)
                val newClassName = ClassName(packageName, newName)
                val newTypeSpec = TypeSpec.objectBuilder(newName)
                objects.add(newName)

                loadGson(newTypeSpec, newClassName, newName, packageName, value, filer, messager)

                val property = PropertySpec.builder(key, newClassName.topLevelClassName()).let {
                    it.initializer(newName)
                    it.build()
                }

                typeSpec.addProperty(property)
            } else {
                if (lastKey != key) {
                    lastKey = key
                    val typeClass = typeClass(value = value)
                    val initializer = format(value = value)


                    //messager.printMessage(Diagnostic.Kind.WARNING, "$key - $value")

                    createField(key, typeClass, value, initializer).apply {
                        typeSpec.addProperty(this)
                    }
                }

            }

        }

        createMultiParameters(parameters).apply {
            listCodeBlock.add(this)
        }

        typeSpec.build().let {
            objects.add(it.name ?: "null")
            messager.printMessage(Diagnostic.Kind.WARNING, "File Created - ${it.name}")
            createFile(packageName, nameClass, it, filer)
        }

        val nameList = className.simpleName.substring(0, 1).toLowerCase() + className.simpleName.substring(1)

        createInitializerMultiParametersWithCodeBlock(className, listCodeBlock, true).apply {
            createMutableListWithCodeBlock(nameList, className, this).apply {
                typeSpec.addProperty(this)
            }
        }

        for (string in objects) {
            //messager.printMessage(Diagnostic.Kind.WARNING, string)
        }

    }

    internal fun loadListGson (typeSpec: TypeSpec.Builder, nameClass: String, packageName: String, map: List<Map<String, Any>>, filer: Filer) : PropertySpec {
        val classListType = ClassName(packageName, nameClass)
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

                    val newName: String = key.substring(0,1).toUpperCase() + key.substring(1)
                    val newTypeSpec = TypeSpec.classBuilder(newName)

                    loadListGson(newTypeSpec, newName, packageName, value, filer).apply {
                        typeSpec.addProperty(this)
                    }
                } else {
                    parameters.add(value)
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

        typeSpec.let { type ->
            type.primaryConstructor(constructor.build())

            for (field in constructor.parameters) {

                type.addProperty(PropertySpec.builder(field.name, field.type).initializer(field.name).build())

            }

            //val file = FileSpec.builder(packageName, typeSpec)
            type.build().let {
                messager.printMessage(Diagnostic.Kind.WARNING, "File List Created - ${it.name}")
                createFile(packageName, nameClass, it, filer)
            }
        }

        val nameList = classListType.simpleName.substring(0, 1).toLowerCase() + classListType.simpleName.substring(1)

        createInitializerMultiParametersWithCodeBlock(classListType, listCodeBlock, true).apply {
            createMutableListWithCodeBlock(nameList, classListType, this).apply {
                typeSpec.addProperty(this)
                return this
            }
        }
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
        key: String,
        classType: ClassName,
        listCodeBlock: MutableList<CodeBlock>
    ): PropertySpec {
        val mutClassType = mutable.parameterizedBy(classType)

        return PropertySpec.builder(key, mutClassType).let {
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

    internal fun createObjectField (name: String, type: KClass<*>, initializer: String) : PropertySpec {
        return PropertySpec.builder(name, type::class.asTypeName()).let {
            it.initializer(buildCodeBlock { this.add("$initializer = $initializer") })
            it.build()
        }
    }

    internal fun createField(name: String, type: KClass<*>, value: Any?, initializer: String): PropertySpec {


        return PropertySpec.builder(name, type.asTypeName().copy(nullable = value == null)).let {
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