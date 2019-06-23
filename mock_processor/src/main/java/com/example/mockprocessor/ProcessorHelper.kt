package com.example.mockprocessor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.Filer
import javax.tools.StandardLocation
import kotlin.reflect.KClass

object ProcessorHelper {

    private val list = ClassName("kotlin.collections", "List")
    private val arrayList = ClassName("kotlin.collections", "ArrayList")
    private val mutable = ClassName("kotlin.collections", "MutableList")

    internal fun typeClass (value: Any? = null, type: TypeName? = null) : KClass<*> {
        if (type != null) {
            return when (type) {
                INT -> Int::class
                DOUBLE -> Double::class
                else -> String::class
            }
        }

        return when (value) {
            is Int -> Int::class
            is Double -> Double::class
            else -> String::class
        }
    }

    internal fun format (value: Any? = null, type: TypeName? = null) : String {
        if (type != null) {
            return when (type) {
                DOUBLE -> "%L"
                INT -> "%L"
                else -> "%S"
            }
        }
        return when (value) {
            is Double -> "%L"
            is Int -> "%L"
            else -> "%S"
        }
    }

    internal fun createMutableListField (classType: ClassName, size: Int) : PropertySpec {
        val mutClassType = mutable.parameterizedBy(classType)

        return PropertySpec.builder("list", mutClassType).let {

            val codeBlock = buildCodeBlock {
                this.add("mutableListOf(")
                for (i in 0..size) {
                    if (i != size)
                        this.add("%T,", classType)
                    else
                        this.add("%T", classType)
                }
                this.add(")")
            }

            it.initializer(codeBlock)
            it.build()
        }
    }

    internal fun  createField (name: String, type: KClass<*>, value: Any, initializer: String) : PropertySpec {
        return PropertySpec.builder(name, type).let {
            it.initializer(initializer, value)
            it.build()
        }
    }

    internal fun createFile(packageName: String, className: String, builder: TypeSpec, filer: Filer) {
        val kotlinFile = FileSpec.builder(packageName, className).addType(builder).build()

        val kotlinFileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$className.kt")
        kotlinFileObject.openWriter().use { kotlinFile.writeTo(it) }
    }

}